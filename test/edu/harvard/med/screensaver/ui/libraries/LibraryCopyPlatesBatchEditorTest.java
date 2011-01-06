// $HeadURL: $
// $Id: $
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.joda.time.LocalDate;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.model.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.VolumeUnit;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.libraries.PlateSize;
import edu.harvard.med.screensaver.model.libraries.PlateStatus;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.service.libraries.PlateUpdater;
import edu.harvard.med.screensaver.ui.arch.datatable.Criterion.Operator;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;

public class LibraryCopyPlatesBatchEditorTest extends AbstractSpringPersistenceTest
{
  protected LibraryCopyPlateSearchResults libraryCopyPlatesBrowser;

  private AdministratorUser _admin;
  private Copy _copyC;
  private Copy _copyD;

  @Override
  protected void onSetUp() throws Exception
  {
    super.onSetUp();
    _admin = new AdministratorUser("Admin", "User", "", "", "", "", "", "");
    _admin.addScreensaverUserRole(ScreensaverUserRole.LIBRARY_COPIES_ADMIN);
    genericEntityDao.persistEntity(_admin);
    Library library = new Library(null, "lib", "lib", ScreenType.SMALL_MOLECULE, LibraryType.COMMERCIAL, 1, 6, PlateSize.WELLS_96);
    _copyC = library.createCopy(null, CopyUsageType.LIBRARY_SCREENING_PLATES, "C");
    _copyD = library.createCopy(null, CopyUsageType.LIBRARY_SCREENING_PLATES, "D");
    genericEntityDao.persistEntity(library);
    libraryCopyPlatesBrowser.getCurrentScreensaverUser().setScreensaverUser(_admin);
    libraryCopyPlatesBrowser.getBatchEditor().getCurrentScreensaverUser().setScreensaverUser(_admin);
  }

  public void testBatchEditPlateTypeAndVolumeAndComment()
  {
    libraryCopyPlatesBrowser.searchAll();
    ((TableColumn<Plate,String>) libraryCopyPlatesBrowser.getColumnManager().getColumn("Copy")).getCriterion().setOperatorAndValue(Operator.EQUAL, "C");
    libraryCopyPlatesBrowser.getRowCount();
    libraryCopyPlatesBrowser.getBatchEditor().getPlateType().setSelection(PlateType.ABGENE);
    libraryCopyPlatesBrowser.getBatchEditor().setVolumeValue("10.0");
    libraryCopyPlatesBrowser.getBatchEditor().getVolumeType().setSelection(VolumeUnit.MICROLITERS);
    libraryCopyPlatesBrowser.getBatchEditor().setComments("test comment");
    libraryCopyPlatesBrowser.batchUpdate();
    
    List<Plate> plates = genericEntityDao.findEntitiesByProperty(Plate.class, "copy", _copyC, true, Plate.updateActivities.getPath());
    assertEquals(6, plates.size());
    assertTrue(Iterables.all(plates,
                             new Predicate<Plate>()
                             {
                               @Override
                               public boolean apply(Plate plate)
                               {
                                 boolean result = plate.getPlateType() == PlateType.ABGENE &&
                                   plate.getWellVolume().equals(new Volume(10, VolumeUnit.MICROLITERS));
                                 result &= plate.getUpdateActivities().size() == 4;
                                 result &= plate.getLastUpdateActivityOfType(AdministrativeActivityType.COMMENT).getComments().equals("test comment");
                                 return result;
                               }
                             }));
  }

  public void testBatchEditPlateStatus()
  {
    libraryCopyPlatesBrowser.searchAll();
    ((TableColumn<Plate,String>) libraryCopyPlatesBrowser.getColumnManager().getColumn("Copy")).getCriterion().setOperatorAndValue(Operator.EQUAL, "C");
    libraryCopyPlatesBrowser.getRowCount();
    libraryCopyPlatesBrowser.getBatchEditor().getPlateStatus().setSelection(PlateStatus.RETIRED);
    libraryCopyPlatesBrowser.getBatchEditor().getStatusChangeActivity().setPerformedBy(_admin);
    libraryCopyPlatesBrowser.getBatchEditor().getStatusChangeActivity().setDateOfActivity(new LocalDate(2010, 1, 1));
    libraryCopyPlatesBrowser.batchUpdate();
    
    List<Plate> plates = genericEntityDao.findAllEntitiesOfType(Plate.class, true, Plate.copy.getPath(), Plate.updateActivities.getPath());
    assertEquals(12, plates.size());
    assertTrue(Iterables.all(plates,
                             new Predicate<Plate>()
                             {
                               @Override
                               public boolean apply(Plate p)
                               {
                                 boolean result = false;
                                 if (p.getCopy().getName().equals("C")) {
                                   result |= p.getStatus().equals(PlateStatus.RETIRED);
                                   result |= p.getLastUpdateActivityOfType(AdministrativeActivityType.PLATE_STATUS_UPDATE).getDateOfActivity().equals(new LocalDate(2010, 1, 1));
                                 }
                                 else {
                                   result |= p.getStatus().equals(PlateStatus.NOT_SPECIFIED);
                                 }
                                 return result;
                               }
                             }));
  }
  
  public void testBatchEditVirginPlateLocation()
  {
    libraryCopyPlatesBrowser.searchAll();
    ((TableColumn<Plate,String>) libraryCopyPlatesBrowser.getColumnManager().getColumn("Copy")).getCriterion().setOperatorAndValue(Operator.EQUAL, "C");
    assertTrue(libraryCopyPlatesBrowser.getRowCount() > 0);
    libraryCopyPlatesBrowser.getBatchEditor().getNewPlateLocationFields().setRoom("Room1");
    libraryCopyPlatesBrowser.getBatchEditor().getLocationChangeActivity().setPerformedBy(_admin);
    libraryCopyPlatesBrowser.getBatchEditor().getLocationChangeActivity().setDateOfActivity(new LocalDate());
    libraryCopyPlatesBrowser.batchUpdate();

    List<Plate> plates = genericEntityDao.findAllEntitiesOfType(Plate.class, true, Plate.copy.getPath(), Plate.location.getPath());
    assertEquals(12, plates.size());
    assertTrue(Iterables.all(plates,
                             new Predicate<Plate>()
                             {
                               @Override
                               public boolean apply(Plate p)
                               {
                                 return (p.getCopy().getName().equals("C") &&
                                   p.getLocation().getRoom().equals("Room1") &&
                                   p.getLocation().getFreezer().equals(PlateUpdater.NO_FREEZER) &&
                                   p.getLocation().getShelf().equals(PlateUpdater.NO_SHELF) &&
                                   p.getLocation().getBin().equals(PlateUpdater.NO_BIN)) ||
                                   (p.getCopy().getName().equals("D") && p.getLocation() == null);
                               }
                             }));
  }

  public void testBatchEditPlateLocation()
  {
    libraryCopyPlatesBrowser.searchAll();
    ((TableColumn<Plate,String>) libraryCopyPlatesBrowser.getColumnManager().getColumn("Copy")).getCriterion().setOperatorAndValue(Operator.EQUAL, "C");
    assertTrue(libraryCopyPlatesBrowser.getRowCount() > 0);
    libraryCopyPlatesBrowser.getBatchEditor().getNewPlateLocationFields().setRoom("Room1");
    libraryCopyPlatesBrowser.getBatchEditor().getNewPlateLocationFields().setFreezer("Freezer1");
    libraryCopyPlatesBrowser.getBatchEditor().getNewPlateLocationFields().setShelf("Shelf1");
    libraryCopyPlatesBrowser.getBatchEditor().getNewPlateLocationFields().setBin("Bin1");
    libraryCopyPlatesBrowser.getBatchEditor().getLocationChangeActivity().setPerformedBy(_admin);
    libraryCopyPlatesBrowser.getBatchEditor().getLocationChangeActivity().setDateOfActivity(new LocalDate());
    libraryCopyPlatesBrowser.batchUpdate();
    ((TableColumn<Plate,String>) libraryCopyPlatesBrowser.getColumnManager().getColumn("Copy")).getCriterion().setOperatorAndValue(Operator.EQUAL, "D");
    assertTrue(libraryCopyPlatesBrowser.getRowCount() > 0);
    libraryCopyPlatesBrowser.getBatchEditor().initialize();
    libraryCopyPlatesBrowser.getBatchEditor().getNewPlateLocationFields().setRoom("Room2");
    libraryCopyPlatesBrowser.getBatchEditor().getNewPlateLocationFields().setFreezer("Freezer2");
    libraryCopyPlatesBrowser.getBatchEditor().getNewPlateLocationFields().setShelf("Shelf1");
    libraryCopyPlatesBrowser.getBatchEditor().getNewPlateLocationFields().setBin("Bin1");
    libraryCopyPlatesBrowser.getBatchEditor().getLocationChangeActivity().setPerformedBy(_admin);
    libraryCopyPlatesBrowser.getBatchEditor().getLocationChangeActivity().setDateOfActivity(new LocalDate());
    libraryCopyPlatesBrowser.batchUpdate();

    List<Plate> plates = genericEntityDao.findAllEntitiesOfType(Plate.class, true, Plate.copy.getPath(), Plate.location.getPath());
    assertEquals(12, plates.size());
    assertTrue(Iterables.all(plates,
                             new Predicate<Plate>()
                             {
                               @Override
                               public boolean apply(Plate p)
                               {
                                 return p.getLocation().getRoom().equals(p.getCopy().getName().equals("C") ? "Room1"
                                   : "Room2") &&
                                   p.getLocation().getFreezer().equals(p.getCopy().getName().equals("C") ? "Freezer1"
                                     : "Freezer2") &&
                                   p.getLocation().getShelf().equals("Shelf1") &&
                                   p.getLocation().getBin().equals("Bin1");
                               }
                             }));
    
    // test changing just freezer and bin, and not room or shelf (for copy D only)
    libraryCopyPlatesBrowser.getRowCount();
    libraryCopyPlatesBrowser.getBatchEditor().initialize();
    libraryCopyPlatesBrowser.getBatchEditor().getNewPlateLocationFields().setFreezer("Freezer3");
    libraryCopyPlatesBrowser.getBatchEditor().getNewPlateLocationFields().setBin("Bin2");
    libraryCopyPlatesBrowser.getBatchEditor().getLocationChangeActivity().setPerformedBy(_admin);
    libraryCopyPlatesBrowser.getBatchEditor().getLocationChangeActivity().setDateOfActivity(new LocalDate());
    libraryCopyPlatesBrowser.batchUpdate();
    
    plates = genericEntityDao.findEntitiesByProperty(Plate.class, "copy", _copyD, true, Plate.copy.getPath(), Plate.location.getPath());
    assertEquals(6, plates.size());
    assertTrue(Iterables.all(plates,
                             new Predicate<Plate>()
                             {
                               @Override
                               public boolean apply(Plate p)
                               {
                                 return p.getLocation().getRoom().equals(p.getCopy().getName().equals("C") ? "Room1"
                                   : "Room2") &&
                                   p.getLocation().getFreezer().equals(p.getCopy().getName().equals("C") ? "Freezer1"
                                     : "Freezer3") &&
                                   p.getLocation().getShelf().equals("Shelf1") &&
                                   p.getLocation().getBin().equals(p.getCopy().getName().equals("C") ? "Bin1"
                                     : "Bin2");
                               }
                             }));

    libraryCopyPlatesBrowser.getRowCount();
    libraryCopyPlatesBrowser.getBatchEditor().initialize();
    libraryCopyPlatesBrowser.getBatchEditor().getRoom().setSelection("Room1");
    libraryCopyPlatesBrowser.getBatchEditor().getFreezer().setSelection("Freezer1");
    libraryCopyPlatesBrowser.getBatchEditor().getNewPlateLocationFields().setShelf("Shelf2");
    libraryCopyPlatesBrowser.getBatchEditor().getLocationChangeActivity().setPerformedBy(_admin);
    libraryCopyPlatesBrowser.getBatchEditor().getLocationChangeActivity().setDateOfActivity(new LocalDate());
    libraryCopyPlatesBrowser.batchUpdate();

    plates = genericEntityDao.findEntitiesByProperty(Plate.class, "copy", _copyD, true, Plate.copy.getPath(), Plate.location.getPath());
    assertEquals(6, plates.size());
    assertTrue(Iterables.all(plates,
                             new Predicate<Plate>()
                             {
                               @Override
                               public boolean apply(Plate p)
                               {
                                 return p.getLocation().getRoom().equals("Room1") &&
                                   p.getLocation().getFreezer().equals("Freezer1") &&
                                   p.getLocation().getShelf().equals(p.getCopy().getName().equals("C") ? "Shelf1"
                                     : "Shelf2") &&
                                   p.getLocation().getBin().equals(p.getCopy().getName().equals("C") ? "Bin1" : "Bin2");
                               }
                             }));
  }
}
