// $HeadURL: $
// $Id: $
//
// Copyright © 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.MolarConcentration;
import edu.harvard.med.screensaver.model.MolarUnit;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.VolumeUnit;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryContentsVersion;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.libraries.PlateSize;
import edu.harvard.med.screensaver.model.libraries.PlateStatus;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.service.libraries.LibraryContentsVersionManager;
import edu.harvard.med.screensaver.service.libraries.LibraryCreator;
import edu.harvard.med.screensaver.service.libraries.PlateUpdater;
import edu.harvard.med.screensaver.ui.arch.datatable.Criterion.Operator;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;
import edu.harvard.med.screensaver.ui.arch.view.AbstractBackingBeanTest;

public class LibraryCopyPlatesBatchEditorTest extends AbstractBackingBeanTest
{
  @Autowired
  protected LibraryCopyPlateSearchResults libraryCopyPlatesBrowser;
  @Autowired
  protected LibraryContentsVersionManager libraryContentsVersionManager;
  @Autowired
  protected LibraryCreator libraryCreator;
  @Autowired
  protected LibrariesDAO librariesDao;

  private Copy _copyC;
  private Copy _copyD;
  private Library _library;

  private MolarConcentration minMolarConcentration = MolarConcentration.makeConcentration("100", MolarUnit.PICOMOLAR);
  private MolarConcentration maxMolarConcentration = MolarConcentration.makeConcentration("100", MolarUnit.MILLIMOLAR);
  private MolarConcentration primaryMolarConcentration = MolarConcentration.makeConcentration("100", MolarUnit.MICROMOLAR);
  private MolarConcentration unavailableMolarConcentration = MolarConcentration.makeConcentration("200", MolarUnit.MICROMOLAR);
  private BigDecimal minMgMlConcentration = new BigDecimal("0.010");
  private BigDecimal maxMgMlConcentration = new BigDecimal("5.000");
  private BigDecimal primaryMgMlConcentration = new BigDecimal("3.000");
  private BigDecimal unAvailableMgMlConcentration = new BigDecimal("4.000");

  protected void setUp() throws Exception
  {
    super.setUp();

    genericEntityDao.doInTransaction(new DAOTransaction() {

      @Override
      public void runTransaction()
      {
        _admin.addScreensaverUserRole(ScreensaverUserRole.LIBRARY_COPIES_ADMIN);
        _admin.addScreensaverUserRole(ScreensaverUserRole.LIBRARIES_ADMIN);
        _admin = genericEntityDao.mergeEntity(_admin);
        _library = new Library(null, "lib", "lib", ScreenType.SMALL_MOLECULE, LibraryType.COMMERCIAL, 1, 6, PlateSize.WELLS_96);
        libraryCreator.createLibrary(_library);
        for(Well well:_library.getWells())
        {
          well.setLibraryWellType(LibraryWellType.EXPERIMENTAL);
        }
        
        _library = genericEntityDao.mergeEntity(_library);
        _copyC = _library.createCopy(null, CopyUsageType.LIBRARY_SCREENING_PLATES, "C");
        _copyD = _library.createCopy(null, CopyUsageType.LIBRARY_SCREENING_PLATES, "D");

        libraryCopyPlatesBrowser.getCurrentScreensaverUser().setScreensaverUser(_admin);
        libraryCopyPlatesBrowser.getBatchEditor().getCurrentScreensaverUser().setScreensaverUser(_admin);
        genericEntityDao.flush();

        // TODO: figure out why an "UnsupportedOperationException: entityViewPolicy not set" is thrown for libraryCopyPlatesBrowser.getRowCount() if an LCV is not created
        LibraryContentsVersion lcv = _library.createContentsVersion(_admin);
        genericEntityDao.persistEntity(lcv);
        genericEntityDao.flush(); //?
        _admin = genericEntityDao.reloadEntity(_admin);
        _library = genericEntityDao.reloadEntity(_library, false, Library.contentsVersions);
        libraryContentsVersionManager.releaseLibraryContentsVersion(_library.getLatestContentsVersion(), _admin);

        // Create a non-available plate, which should be ignored for concentration calculations at the copy level
        Map<String,Object> properties = Maps.newHashMap();
        properties.put("copy", _copyC);
        properties.put("plateNumber", new Integer(2));
        Plate p = genericEntityDao.findEntityByProperties(Plate.class, properties, true);
        p.setStatus(PlateStatus.LOST);
      }
    });
  }



  @Transactional
  public void testBatchEditPlateTypeAndVolumeAndComment() throws Exception
  {
    libraryCopyPlatesBrowser.searchAll();
    ((TableColumn<Plate,String>) libraryCopyPlatesBrowser.getColumnManager().getColumn("Copy")).getCriterion().setOperatorAndValue(Operator.EQUAL, "C");
    libraryCopyPlatesBrowser.getRowCount();
    assertTrue(libraryCopyPlatesBrowser.getDataTableModel().iterator().hasNext());
    libraryCopyPlatesBrowser.getBatchEditor().getPlateType().setSelection(PlateType.ABGENE);
    libraryCopyPlatesBrowser.getBatchEditor().setVolumeValue("10.0");
    libraryCopyPlatesBrowser.getBatchEditor().getVolumeType().setSelection(VolumeUnit.MICROLITERS);
    libraryCopyPlatesBrowser.getBatchEditor().setComments("test comment");
    libraryCopyPlatesBrowser.batchUpdate();

    _copyC = genericEntityDao.reloadEntity(_copyC);
    List<Plate> plates = genericEntityDao.findEntitiesByProperty(Plate.class, "copy", _copyC, true, Plate.updateActivities.castToSubtype(Plate.class));
    assertEquals(6, plates.size());
    assertTrue(Iterables.all(plates,
                             new Predicate<Plate>()
                             {
                               @Override
                               public boolean apply(Plate plate)
                               {
                                 boolean result = plate.getPlateType() == PlateType.ABGENE &&
                                   plate.getWellVolume().equals(new Volume(10, VolumeUnit.MICROLITERS));
                                 //                                 result &= plate.getUpdateActivities().size() == 4;
                                 //                                 result &= plate.getLastUpdateActivityOfType(AdministrativeActivityType.COMMENT).getComments().equals("test comment");
                                 return result;
                               }
                             }));
  }


  @Transactional
  public void testBatchEditPlateStatus()
  {
    libraryCopyPlatesBrowser.searchAll();
    ((TableColumn<Plate,String>) libraryCopyPlatesBrowser.getColumnManager().getColumn("Copy")).getCriterion().setOperatorAndValue(Operator.EQUAL, "C");
    libraryCopyPlatesBrowser.getRowCount();
    libraryCopyPlatesBrowser.getBatchEditor().getPlateStatus().setSelection(PlateStatus.RETIRED);
    libraryCopyPlatesBrowser.getBatchEditor().getStatusChangeActivity().setPerformedBy(_admin);
    libraryCopyPlatesBrowser.getBatchEditor().getStatusChangeActivity().setDateOfActivity(new LocalDate(2010, 1, 1));
    libraryCopyPlatesBrowser.batchUpdate();

    genericEntityDao.doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        List<Plate> plates = genericEntityDao.findAllEntitiesOfType(Plate.class, true);
        assertEquals(12, plates.size());
        assertTrue(Iterables.all(plates,
                                 new Predicate<Plate>()
                                 {
                                   @Override
                                   public boolean apply(Plate p)
          {
            if (p.getCopy().getName().equals("C")) {
              return p.getStatus().equals(PlateStatus.RETIRED) &&
                p.getLastUpdateActivityOfType(AdministrativeActivityType.PLATE_STATUS_UPDATE).getDateOfActivity().equals(new LocalDate(2010, 1, 1));
            }
            else {
              return p.getStatus().equals(PlateStatus.NOT_SPECIFIED);
            }
          }
                                 }));
      }
    });
  }

  @Transactional
  public void testBatchEditVirginPlateLocation()
  {
    libraryCopyPlatesBrowser.searchAll();
    ((TableColumn<Plate,String>) libraryCopyPlatesBrowser.getColumnManager().getColumn("Copy")).getCriterion().setOperatorAndValue(Operator.EQUAL, "C");
    assertTrue(libraryCopyPlatesBrowser.getRowCount() > 0);
    libraryCopyPlatesBrowser.getBatchEditor().getPlateStatus().setSelection(PlateStatus.AVAILABLE);
    libraryCopyPlatesBrowser.getBatchEditor().setNewPlateLocationRoom("Room1");
    libraryCopyPlatesBrowser.getBatchEditor().getLocationChangeActivity().setPerformedBy(_admin);
    libraryCopyPlatesBrowser.getBatchEditor().getLocationChangeActivity().setDateOfActivity(new LocalDate());
    libraryCopyPlatesBrowser.batchUpdate();

    genericEntityDao.doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        List<Plate> plates = genericEntityDao.findAllEntitiesOfType(Plate.class, true);
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
    });
  }

  @Transactional
  public void testBatchEditPlateLocation()
  {
    libraryCopyPlatesBrowser.searchAll();
    ((TableColumn<Plate,String>) libraryCopyPlatesBrowser.getColumnManager().getColumn("Copy")).getCriterion().setOperatorAndValue(Operator.EQUAL, "C");
    assertTrue(libraryCopyPlatesBrowser.getRowCount() > 0);
    libraryCopyPlatesBrowser.getBatchEditor().getPlateStatus().setSelection(PlateStatus.AVAILABLE);
    libraryCopyPlatesBrowser.getBatchEditor().setNewPlateLocationRoom("Room1");
    libraryCopyPlatesBrowser.getBatchEditor().setNewPlateLocationFreezer("Freezer1");
    libraryCopyPlatesBrowser.getBatchEditor().setNewPlateLocationShelf("Shelf1");
    libraryCopyPlatesBrowser.getBatchEditor().setNewPlateLocationBin("Bin1");
    libraryCopyPlatesBrowser.getBatchEditor().getLocationChangeActivity().setPerformedBy(_admin);
    libraryCopyPlatesBrowser.getBatchEditor().getLocationChangeActivity().setDateOfActivity(new LocalDate());
    libraryCopyPlatesBrowser.batchUpdate();
    ((TableColumn<Plate,String>) libraryCopyPlatesBrowser.getColumnManager().getColumn("Copy")).getCriterion().setOperatorAndValue(Operator.EQUAL, "D");
    assertTrue(libraryCopyPlatesBrowser.getRowCount() > 0);
    libraryCopyPlatesBrowser.getBatchEditor().initialize();
    libraryCopyPlatesBrowser.getBatchEditor().getPlateStatus().setSelection(PlateStatus.AVAILABLE);
    libraryCopyPlatesBrowser.getBatchEditor().setNewPlateLocationRoom("Room2");
    libraryCopyPlatesBrowser.getBatchEditor().setNewPlateLocationFreezer("Freezer2");
    libraryCopyPlatesBrowser.getBatchEditor().setNewPlateLocationShelf("Shelf1");
    libraryCopyPlatesBrowser.getBatchEditor().setNewPlateLocationBin("Bin1");
    libraryCopyPlatesBrowser.getBatchEditor().getLocationChangeActivity().setPerformedBy(_admin);
    libraryCopyPlatesBrowser.getBatchEditor().getLocationChangeActivity().setDateOfActivity(new LocalDate());
    libraryCopyPlatesBrowser.batchUpdate();

    List<Plate> plates = genericEntityDao.findAllEntitiesOfType(Plate.class, true, Plate.location);
    logger.info("plates: " + plates);
    genericEntityDao.doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        List<Plate> plates = genericEntityDao.findAllEntitiesOfType(Plate.class, true, Plate.location);
        assertEquals(12, plates.size());
        assertTrue(Iterables.all(plates,
                                 new Predicate<Plate>() {
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
      }
    });

    // test changing just freezer and bin, and not room or shelf (for copy D only)
    libraryCopyPlatesBrowser.getRowCount();
    libraryCopyPlatesBrowser.getBatchEditor().initialize();
    libraryCopyPlatesBrowser.getBatchEditor().setNewPlateLocationRoom("Room2");
    libraryCopyPlatesBrowser.getBatchEditor().setNewPlateLocationFreezer("Freezer3");
    libraryCopyPlatesBrowser.getBatchEditor().setNewPlateLocationShelf("Shelf1");
    libraryCopyPlatesBrowser.getBatchEditor().setNewPlateLocationBin("Bin2");
    libraryCopyPlatesBrowser.getBatchEditor().getLocationChangeActivity().setPerformedBy(_admin);
    libraryCopyPlatesBrowser.getBatchEditor().getLocationChangeActivity().setDateOfActivity(new LocalDate());
    libraryCopyPlatesBrowser.batchUpdate();

    genericEntityDao.doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        List<Plate> plates = genericEntityDao.findEntitiesByProperty(Plate.class, "copy", _copyD, true);
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
      }
    });

    libraryCopyPlatesBrowser.getRowCount();
    libraryCopyPlatesBrowser.getBatchEditor().initialize();
    libraryCopyPlatesBrowser.getBatchEditor().getRoom().setSelection("Room1");
    libraryCopyPlatesBrowser.getBatchEditor().getFreezer().setSelection("Freezer1");
    libraryCopyPlatesBrowser.getBatchEditor().setNewPlateLocationRoom("Room1");
    libraryCopyPlatesBrowser.getBatchEditor().setNewPlateLocationFreezer("Freezer1");
    libraryCopyPlatesBrowser.getBatchEditor().setNewPlateLocationShelf("Shelf2");
    libraryCopyPlatesBrowser.getBatchEditor().setNewPlateLocationBin("Bin2");
    libraryCopyPlatesBrowser.getBatchEditor().getLocationChangeActivity().setPerformedBy(_admin);
    libraryCopyPlatesBrowser.getBatchEditor().getLocationChangeActivity().setDateOfActivity(new LocalDate());
    libraryCopyPlatesBrowser.batchUpdate();

    genericEntityDao.doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        List<Plate> plates = genericEntityDao.findEntitiesByProperty(Plate.class, "copy", _copyD, true);
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
    });
  }
}
