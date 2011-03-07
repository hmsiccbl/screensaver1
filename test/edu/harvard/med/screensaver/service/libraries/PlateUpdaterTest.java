// $HeadURL: $
// $Id: $
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.libraries;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.hibernate.Session;
import org.joda.time.LocalDate;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.Query;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.db.hqlbuilder.JoinType;
import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.libraries.PlateLocation;
import edu.harvard.med.screensaver.model.libraries.PlateSize;
import edu.harvard.med.screensaver.model.libraries.PlateStatus;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.ui.arch.datatable.Criterion.Operator;

public class PlateUpdaterTest extends AbstractSpringPersistenceTest
{
  protected PlateUpdater plateUpdater;

  private AdministratorUser _admin;
  private Copy _copyC;
  private Copy _copyD;

  @Override
  public void onSetUp() throws Exception
  {
    super.onSetUp();
    _admin = new AdministratorUser("Admin", "User", "", "", "", "", "", "");
    genericEntityDao.persistEntity(_admin);
    Library library = new Library(null, "lib", "lib", ScreenType.SMALL_MOLECULE, LibraryType.COMMERCIAL, 1, 6, PlateSize.WELLS_96);
    _copyC = library.createCopy(null, CopyUsageType.LIBRARY_SCREENING_PLATES, "C");
    _copyD = library.createCopy(null, CopyUsageType.LIBRARY_SCREENING_PLATES, "D");
    genericEntityDao.persistEntity(library);
  }

  public void testPlateStatusUpdate()
  {
    final AdministratorUser admin2 = new AdministratorUser("Admin2", "User", "", "", "", "", null, "");
    Plate plate = _copyC.findPlate(1);
    genericEntityDao.persistEntity(admin2);

    plateUpdater.updatePlateStatus(plate, PlateStatus.NOT_CREATED, _admin, admin2, new LocalDate(2010, 1, 1));
    genericEntityDao.doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        Plate updatedPlate = genericEntityDao.reloadEntity(_copyC.findPlate(1));
        AdministrativeActivity activity = updatedPlate.getLastUpdateActivityOfType(AdministrativeActivityType.PLATE_STATUS_UPDATE);
        assertEquals(PlateStatus.NOT_CREATED, updatedPlate.getStatus());
        assertEquals(genericEntityDao.reloadEntity(admin2), activity.getPerformedBy());
        assertEquals(genericEntityDao.reloadEntity(_admin), activity.getCreatedBy());
        assertEquals("Status changed from 'Not specified' to 'Not created'", activity.getComments());
        assertEquals(new LocalDate(2010, 1, 1), activity.getDateOfActivity());
        assertNull(updatedPlate.getPlatedActivity());
      }
    });

    plateUpdater.updatePlateLocation(plate, new PlateLocation("W", "X", "Y", "Z"), _admin, admin2, new LocalDate(2010, 1, 2));
    plateUpdater.updatePlateStatus(plate, PlateStatus.AVAILABLE, _admin, admin2, new LocalDate(2010, 1, 2));
    genericEntityDao.doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        Plate updatedPlate = genericEntityDao.reloadEntity(_copyC.findPlate(1));
        AdministrativeActivity activity = updatedPlate.getLastUpdateActivityOfType(AdministrativeActivityType.PLATE_STATUS_UPDATE);
        assertEquals(PlateStatus.AVAILABLE, updatedPlate.getStatus());
        assertEquals(genericEntityDao.reloadEntity(admin2), activity.getPerformedBy());
        assertEquals(genericEntityDao.reloadEntity(_admin), activity.getCreatedBy());
        assertEquals("Status changed from 'Not created' to 'Available'", activity.getComments());
        assertEquals(new LocalDate(2010, 1, 2), activity.getDateOfActivity());
        assertEquals(activity, updatedPlate.getPlatedActivity());
      }
    });

    plateUpdater.updatePlateLocation(plate, new PlateLocation("W", "X", "Y", "Z"), _admin, admin2, new LocalDate(2010, 1, 3));
    plateUpdater.updatePlateStatus(plate, PlateStatus.NOT_AVAILABLE, _admin, admin2, new LocalDate(2010, 1, 3));
    genericEntityDao.doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        Plate updatedPlate = genericEntityDao.reloadEntity(_copyC.findPlate(1));
        AdministrativeActivity activity = updatedPlate.getLastUpdateActivityOfType(AdministrativeActivityType.PLATE_STATUS_UPDATE);
        assertEquals(PlateStatus.NOT_AVAILABLE, updatedPlate.getStatus());
        assertEquals(genericEntityDao.reloadEntity(admin2), activity.getPerformedBy());
        assertEquals(genericEntityDao.reloadEntity(_admin), activity.getCreatedBy());
        assertEquals("Status changed from 'Available' to 'Not available'", activity.getComments());
        assertEquals(new LocalDate(2010, 1, 3), activity.getDateOfActivity());
        assertEquals("original plate status update activity maintained", new LocalDate(2010, 1, 2), updatedPlate.getPlatedActivity().getDateOfActivity());
      }
    });

    plateUpdater.updatePlateLocation(plate, new PlateLocation("W", "X", "Y", "Z"), _admin, admin2, new LocalDate(2010, 1, 4));
    plateUpdater.updatePlateStatus(plate, PlateStatus.AVAILABLE, _admin, admin2, new LocalDate(2010, 1, 4));
    genericEntityDao.doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        Plate updatedPlate = genericEntityDao.reloadEntity(_copyC.findPlate(1));
        AdministrativeActivity activity = updatedPlate.getLastUpdateActivityOfType(AdministrativeActivityType.PLATE_STATUS_UPDATE);
        assertEquals(PlateStatus.AVAILABLE, updatedPlate.getStatus());
        assertEquals(genericEntityDao.reloadEntity(admin2), activity.getPerformedBy());
        assertEquals(genericEntityDao.reloadEntity(_admin), activity.getCreatedBy());
        assertEquals("Status changed from 'Not available' to 'Available'", activity.getComments());
        assertEquals("original plate status update activity maintained", new LocalDate(2010, 1, 2), updatedPlate.getPlatedActivity().getDateOfActivity());
      }
    });

    plateUpdater.updatePlateStatus(plate, PlateStatus.RETIRED, _admin, admin2, new LocalDate(2010, 1, 5));
    genericEntityDao.doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        Plate updatedPlate = genericEntityDao.reloadEntity(_copyC.findPlate(1));
        AdministrativeActivity activity = updatedPlate.getLastUpdateActivityOfType(AdministrativeActivityType.PLATE_STATUS_UPDATE);
        assertEquals(PlateStatus.RETIRED, updatedPlate.getStatus());
        assertEquals(genericEntityDao.reloadEntity(admin2), activity.getPerformedBy());
        assertEquals(genericEntityDao.reloadEntity(_admin), activity.getCreatedBy());
        assertEquals("Status changed from 'Available' to 'Retired'", activity.getComments());
        assertEquals(new LocalDate(2010, 1, 5), activity.getDateOfActivity());
        assertEquals(activity, updatedPlate.getRetiredActivity());
      }
    });
    
    plateUpdater.updatePlateStatus(plate, PlateStatus.DISCARDED, _admin, admin2, new LocalDate(2010, 1, 6));
    genericEntityDao.doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        Plate updatedPlate = genericEntityDao.reloadEntity(_copyC.findPlate(1));
        AdministrativeActivity activity = updatedPlate.getLastUpdateActivityOfType(AdministrativeActivityType.PLATE_STATUS_UPDATE);
        assertEquals(PlateStatus.DISCARDED, updatedPlate.getStatus());
        assertEquals(genericEntityDao.reloadEntity(admin2), activity.getPerformedBy());
        assertEquals(genericEntityDao.reloadEntity(_admin), activity.getCreatedBy());
        assertEquals("Status changed from 'Retired' to 'Discarded'", activity.getComments());
        assertEquals(new LocalDate(2010, 1, 6), activity.getDateOfActivity());
        assertEquals("extant retired date does not change when a retired plate is discarded, etc.",
                     new LocalDate(2010, 1, 5), updatedPlate.getRetiredActivity().getDateOfActivity());
        assertNull(updatedPlate.getLocation());
        AdministrativeActivity locationTransferActivity = updatedPlate.getLastUpdateActivityOfType(AdministrativeActivityType.PLATE_LOCATION_TRANSFER);
        assertEquals(new LocalDate(2010, 1, 6), locationTransferActivity.getDateOfActivity());
        assertTrue(locationTransferActivity.getComments().contains("plate no longer has a location due to change of status to " +
          PlateStatus.DISCARDED));
      }
    });
  }

  public void testPlateStatusUpdateFromLost()
  {
    final AdministratorUser admin2 = new AdministratorUser("Admin2", "User", "", "", "", "", null, "");
    Plate plate = _copyC.findPlate(1);
    genericEntityDao.persistEntity(admin2);

    plateUpdater.updatePlateStatus(plate, PlateStatus.LOST, _admin, admin2, new LocalDate(2010, 1, 1));
    genericEntityDao.doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        Plate updatedPlate = genericEntityDao.reloadEntity(_copyC.findPlate(1));
        AdministrativeActivity activity = updatedPlate.getLastUpdateActivityOfType(AdministrativeActivityType.PLATE_STATUS_UPDATE);
        assertEquals(PlateStatus.LOST, updatedPlate.getStatus());
        assertEquals("Status changed from 'Not specified' to 'Lost'", activity.getComments());
        assertEquals(new LocalDate(2010, 1, 1), activity.getDateOfActivity());
        assertNull(updatedPlate.getPlatedActivity());
      }
    });

    plateUpdater.updatePlateLocation(plate, new PlateLocation("W", "X", "Y", "Z"), _admin, admin2, new LocalDate(2010, 1, 2));
    plateUpdater.updatePlateStatus(plate, PlateStatus.AVAILABLE, _admin, admin2, new LocalDate(2010, 1, 2));
    genericEntityDao.doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        Plate updatedPlate = genericEntityDao.reloadEntity(_copyC.findPlate(1));
        AdministrativeActivity activity = updatedPlate.getLastUpdateActivityOfType(AdministrativeActivityType.PLATE_STATUS_UPDATE);
        assertEquals(PlateStatus.AVAILABLE, updatedPlate.getStatus());
        assertEquals("Status changed from 'Lost' to 'Available'", activity.getComments());
        assertEquals(new LocalDate(2010, 1, 2), activity.getDateOfActivity());
        assertEquals(activity, updatedPlate.getPlatedActivity());
      }
    });
  }

  public void testInvalidPlateStatusUpdateFromNotCreatedToNotSpecified()
  {
    doTestInvalidStatusUpdate(PlateStatus.NOT_CREATED, PlateStatus.NOT_SPECIFIED);
  }

  public void testInvalidPlateStatusUpdateFromNotAvailableToNotSpecified()
  {
    doTestInvalidStatusUpdate(PlateStatus.NOT_AVAILABLE, PlateStatus.NOT_SPECIFIED);
  }

  public void testInvalidPlateStatusUpdateFromNotAvailableToNotCreated()
  {
    doTestInvalidStatusUpdate(PlateStatus.NOT_AVAILABLE, PlateStatus.NOT_CREATED);
  }

  public void testInvalidPlateStatusUpdateFromAvailableToNotSpecified()
  {
    doTestInvalidStatusUpdate(PlateStatus.AVAILABLE, PlateStatus.NOT_SPECIFIED);
  }

  public void testInvalidPlateStatusUpdateFromAvailableToNotCreated()
  {
    doTestInvalidStatusUpdate(PlateStatus.AVAILABLE, PlateStatus.NOT_CREATED);
  }

  public void testInvalidPlateStatusUpdateFromRetiredToNotSpecified()
  {
    doTestInvalidStatusUpdate(PlateStatus.RETIRED, PlateStatus.NOT_SPECIFIED);
  }

  public void testInvalidPlateStatusUpdateFromRetiredToNotCreated()
  {
    doTestInvalidStatusUpdate(PlateStatus.RETIRED, PlateStatus.NOT_CREATED);
  }

  public void testInvalidPlateStatusUpdateFromRetiredToNotAvailable()
  {
    doTestInvalidStatusUpdate(PlateStatus.RETIRED, PlateStatus.NOT_AVAILABLE);
  }

  public void testInvalidPlateStatusUpdateFromRetiredToAvailable()
  {
    doTestInvalidStatusUpdate(PlateStatus.RETIRED, PlateStatus.AVAILABLE);
  }

  public void testInvalidPlateStatusUpdateFromDiscardedToLost()
  {
    doTestInvalidStatusUpdate(PlateStatus.DISCARDED, PlateStatus.LOST);
  }

  private void doTestInvalidStatusUpdate(final PlateStatus initialStatus, final PlateStatus invalidStatus)
  {
    final AdministratorUser admin2 = new AdministratorUser("Admin2", "User", "", "", "", "", null, "");
    Plate plate = _copyC.findPlate(1);
    genericEntityDao.persistEntity(admin2);
    plateUpdater.updatePlateStatus(plate, initialStatus, _admin, admin2, new LocalDate(2010, 1, 1));
    plate = genericEntityDao.reloadEntity(_copyC.findPlate(1));
    assertEquals(initialStatus, plate.getStatus());

    try {
      plateUpdater.updatePlateStatus(plate, invalidStatus, _admin, admin2, new LocalDate(2010, 1, 1));
      fail("expected BusinessRuleViolationException");
    }
    catch (BusinessRuleViolationException e) {}
  }

  public void testPrimaryPlateStatus()
  {
    final AdministratorUser admin2 = new AdministratorUser("Admin2", "User", "", "", "", "", null, "");
    genericEntityDao.persistEntity(admin2);

    plateUpdater.updatePlateStatus(_copyC.findPlate(1), PlateStatus.NOT_AVAILABLE, _admin, admin2, new LocalDate(2010, 1, 1));
    plateUpdater.updatePlateStatus(_copyC.findPlate(2), PlateStatus.AVAILABLE, _admin, admin2, new LocalDate(2010, 1, 1));
    plateUpdater.updatePlateStatus(_copyC.findPlate(3), PlateStatus.AVAILABLE, _admin, admin2, new LocalDate(2010, 1, 1));
    plateUpdater.updatePlateStatus(_copyC.findPlate(4), PlateStatus.LOST, _admin, admin2, new LocalDate(2010, 1, 1));
    plateUpdater.updatePlateStatus(_copyC.findPlate(5), PlateStatus.AVAILABLE, _admin, admin2, new LocalDate(2010, 1, 1));
    plateUpdater.updatePlateStatus(_copyC.findPlate(6), PlateStatus.NOT_CREATED, _admin, admin2, new LocalDate(2010, 1, 1));
    assertEquals(PlateStatus.AVAILABLE, genericEntityDao.reloadEntity(_copyC).getPrimaryPlateStatus());

    plateUpdater.updatePlateStatus(_copyC.findPlate(5), PlateStatus.LOST, _admin, admin2, new LocalDate(2010, 1, 1));
    assertEquals("primary plate status ties are broken by ordering of PlateStatus enum values",
                 PlateStatus.AVAILABLE, genericEntityDao.reloadEntity(_copyC).getPrimaryPlateStatus());

    plateUpdater.updatePlateStatus(_copyC.findPlate(6), PlateStatus.LOST, _admin, admin2, new LocalDate(2010, 1, 1));
    assertEquals(PlateStatus.LOST, genericEntityDao.reloadEntity(_copyC).getPrimaryPlateStatus());
  }

  public void testPrimaryPlateLocation()
  {
    final AdministratorUser admin2 = new AdministratorUser("Admin2", "User", "", "", "", "", null, "");
    genericEntityDao.persistEntity(admin2);

    plateUpdater.updatePlateLocation(_copyC.findPlate(1), new PlateLocation("R1", "F1", "S1", "B1"), _admin, admin2, new LocalDate(2010, 1, 1));
    plateUpdater.updatePlateLocation(_copyC.findPlate(2), new PlateLocation("R1", "F1", "S1", "B1"), _admin, admin2, new LocalDate(2010, 1, 1));
    plateUpdater.updatePlateLocation(_copyC.findPlate(3), new PlateLocation("R1", "F1", "S1", "B1"), _admin, admin2, new LocalDate(2010, 1, 1));
    plateUpdater.updatePlateLocation(_copyC.findPlate(4), new PlateLocation("R1", "F1", "S1", "B1"), _admin, admin2, new LocalDate(2010, 1, 1));
    plateUpdater.updatePlateLocation(_copyC.findPlate(5), new PlateLocation("R1", "F1", "S1", "B1"), _admin, admin2, new LocalDate(2010, 1, 1));
    plateUpdater.updatePlateLocation(_copyC.findPlate(6), new PlateLocation("R1", "F1", "S1", "B1"), _admin, admin2, new LocalDate(2010, 1, 1));
    assertEquals(new PlateLocation("R1", "F1", "S1", "B1").toDisplayString(), genericEntityDao.reloadEntity(_copyC).getPrimaryPlateLocation().toDisplayString());

    plateUpdater.updatePlateLocation(_copyC.findPlate(5), null, _admin, admin2, new LocalDate(2010, 1, 1));
    assertEquals(new PlateLocation("R1", "F1", "S1", "B1").toDisplayString(), genericEntityDao.reloadEntity(_copyC).getPrimaryPlateLocation().toDisplayString());

    plateUpdater.updatePlateLocation(_copyC.findPlate(2), new PlateLocation("R1", "F1", "S1", "B2"), _admin, admin2, new LocalDate(2010, 1, 1));
    plateUpdater.updatePlateLocation(_copyC.findPlate(3), new PlateLocation("R1", "F1", "S1", "B2"), _admin, admin2, new LocalDate(2010, 1, 1));
    assertEquals(new PlateLocation("R1", "F1", "S1", "B1").toDisplayString(), genericEntityDao.reloadEntity(_copyC).getPrimaryPlateLocation().toDisplayString());

    plateUpdater.updatePlateLocation(_copyC.findPlate(4), new PlateLocation("R1", "F1", "S1", "B2"), _admin, admin2, new LocalDate(2010, 1, 1));
    assertEquals(new PlateLocation("R1", "F1", "S1", "B2").toDisplayString(), genericEntityDao.reloadEntity(_copyC).getPrimaryPlateLocation().toDisplayString());
  }

  public void testPlatedDate()
  {
    final AdministratorUser admin2 = new AdministratorUser("Admin2", "User", "", "", "", "", null, "");
    genericEntityDao.persistEntity(admin2);

    Plate plate1 = _copyC.findPlate(1);
    plateUpdater.updatePlateStatus(plate1, PlateStatus.AVAILABLE, _admin, admin2, new LocalDate(2010, 2, 2));
    plate1 = genericEntityDao.reloadEntity(plate1, false, Plate.copy.to(Copy.library).getPath());
    assertEquals(PlateStatus.AVAILABLE, plate1.getStatus());
    assertEquals(new LocalDate(2010, 2, 2), plate1.getCopy().getDatePlated());
    assertEquals(new LocalDate(2010, 2, 2), plate1.getCopy().getLibrary().getDateScreenable());

    Plate plate2 = _copyC.findPlate(2);
    plateUpdater.updatePlateStatus(plate2, PlateStatus.AVAILABLE, _admin, admin2, new LocalDate(2010, 3, 3));
    plate2 = genericEntityDao.reloadEntity(plate2, false, Plate.copy.to(Copy.library).getPath());
    assertEquals(PlateStatus.AVAILABLE, plate2.getStatus());
    assertEquals(new LocalDate(2010, 3, 3), plate2.getCopy().getDatePlated());
    assertEquals(new LocalDate(2010, 2, 2), plate2.getCopy().getLibrary().getDateScreenable());

    Plate plate3 = _copyC.findPlate(3);
    plateUpdater.updatePlateStatus(plate3, PlateStatus.AVAILABLE, _admin, admin2, new LocalDate(2010, 1, 1));
    plate3 = genericEntityDao.reloadEntity(plate3, false, Plate.copy.to(Copy.library).getPath());
    assertEquals(PlateStatus.AVAILABLE, plate3.getStatus());
    assertEquals(new LocalDate(2010, 1, 1), plate3.getCopy().getDatePlated());
    assertEquals(new LocalDate(2010, 1, 1), plate3.getCopy().getLibrary().getDateScreenable());
  }

  public void testPlateLocationUnspecifiedBin()
  {
    Map<Plate,PlateLocation> plateLocations = Maps.newHashMap();
    plateLocations.put(_copyC.findPlate(1), new PlateLocation("R1", "F1", "S1", PlateUpdater.NO_BIN));
    plateLocations.put(_copyC.findPlate(2), new PlateLocation("R1", "F1", "S2", PlateUpdater.NO_BIN));
    plateLocations.put(_copyC.findPlate(3), new PlateLocation("R1", "F1", "S1", "B1"));
    plateLocations.put(_copyC.findPlate(4), new PlateLocation("R1", "F1", "S2", "B2"));
    updatePlateLocations(plateLocations, _admin, _admin, new LocalDate());
    assertPlatesInLocation(Sets.newHashSet(_copyC.findPlate(1),
                                           _copyC.findPlate(3)),
                                           "R1", "F1", "S1", null);
    assertPlatesInLocation(Sets.newHashSet(_copyC.findPlate(2),
                                           _copyC.findPlate(4)),
                                           "R1", "F1", "S2", null);

    plateLocations = Maps.newHashMap();
    plateLocations.put(_copyC.findPlate(1), new PlateLocation("R1", "F1", "", "B1"));
    assertIllegalUpdate(_admin, plateLocations, "bin name must be unique within a freezer");
  }

  // note that this method is not transactional, so each plate update is done within its own txn, but that's okay for testing purposes
  private void updatePlateLocations(Map<Plate,PlateLocation> plateToNewLocation,
                                    AdministratorUser recordedByAdmin,
                                    AdministratorUser performedByAdmin,
                                    LocalDate datePerformed)

  {
    boolean modified = false;
    for (Map.Entry<Plate,PlateLocation> entry : plateToNewLocation.entrySet()) {
      modified |= plateUpdater.updatePlateLocation(entry.getKey(), entry.getValue(), recordedByAdmin, performedByAdmin, datePerformed);
    }
    plateUpdater.validateLocations();
  }

  public void testPlateLocationTransferActivityCreation()
  {
    AdministratorUser performedBy = new AdministratorUser("Admin2", "User", "", "", "", "", null, "");
    genericEntityDao.persistEntity(performedBy);
    Map<Plate,PlateLocation> plateLocations = Maps.newHashMap();
    plateLocations.put(_copyC.findPlate(1), new PlateLocation("R1", "F1", "S1", "B1"));
    plateLocations.put(_copyC.findPlate(2), new PlateLocation("R1", "F1", "S2", "B2"));
    updatePlateLocations(plateLocations, _admin, performedBy, new LocalDate(2010, 1, 1));
    plateLocations.put(_copyC.findPlate(1), new PlateLocation("R1", "F2", "S1", "B1"));
    plateLocations.put(_copyC.findPlate(2), new PlateLocation("R1", "F2", "S2", "B2"));
    updatePlateLocations(plateLocations, _admin, performedBy, new LocalDate(2010, 2, 2));
    genericEntityDao.doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        Plate plate1 = genericEntityDao.reloadEntity(_copyC.findPlate(1));
        AdministrativeActivity locationTransferActivity = plate1.getLastUpdateActivityOfType(AdministrativeActivityType.PLATE_LOCATION_TRANSFER);
        assertEquals("Location changed from 'R1-F1-S1-B1' to 'R1-F2-S1-B1'",
                     locationTransferActivity.getComments());
        Plate plate2 = genericEntityDao.reloadEntity(_copyC.findPlate(2));
        locationTransferActivity = plate2.getLastUpdateActivityOfType(AdministrativeActivityType.PLATE_LOCATION_TRANSFER);
        assertEquals("Location changed from 'R1-F1-S2-B2' to 'R1-F2-S2-B2'",
                     locationTransferActivity.getComments());
      }
    });
  }

  public void testPlateLocationUpdate()
  {
    Map<Plate,PlateLocation> plateLocations = Maps.newHashMap();
    plateLocations.put(_copyC.findPlate(1), new PlateLocation("R1", "F1", "S1", "B1"));
    plateLocations.put(_copyC.findPlate(2), new PlateLocation("R1", "F1", "S1", "B1"));
    plateLocations.put(_copyC.findPlate(3), new PlateLocation("R1", "F1", "S1", "B2"));
    plateLocations.put(_copyC.findPlate(4), new PlateLocation("R1", "F1", "S1", "B2"));
    plateLocations.put(_copyC.findPlate(5), new PlateLocation("R1", "F1", "S2", "B3"));
    plateLocations.put(_copyC.findPlate(6), new PlateLocation("R1", "F1", "S2", "B3"));
    updatePlateLocations(plateLocations, _admin, _admin, new LocalDate());
    assertPlatesInLocation(Sets.newHashSet(_copyC.findPlate(1),
                                           _copyC.findPlate(2),
                                           _copyC.findPlate(3),
                                           _copyC.findPlate(4),
                                           _copyC.findPlate(5),
                                           _copyC.findPlate(6)),
                                           "R1", "F1", null, null);
    assertPlatesInLocation(Sets.newHashSet(_copyC.findPlate(1),
                                           _copyC.findPlate(2),
                                           _copyC.findPlate(3),
                                           _copyC.findPlate(4)),
                                           null, null, "S1", null);
    assertPlatesInLocation(Sets.newHashSet(_copyC.findPlate(5),
                                           _copyC.findPlate(6)),
                                           null, null, "S2", null);
    assertPlatesInLocation(Sets.newHashSet(_copyC.findPlate(1)),
                                           "R1", "F1", "S1", "B1");

    plateLocations = Maps.newHashMap();
    plateLocations.put(_copyD.findPlate(1), new PlateLocation("R1", "F2", "S1", "B1"));
    plateLocations.put(_copyD.findPlate(2), new PlateLocation("R1", "F2", "S1", "B1"));
    plateLocations.put(_copyD.findPlate(3), new PlateLocation("R1", "F2", "S1", "B2"));
    plateLocations.put(_copyD.findPlate(4), new PlateLocation("R1", "F2", "S1", "B2"));
    plateLocations.put(_copyD.findPlate(5), new PlateLocation("R1", "F2", "S2", "B3"));
    plateLocations.put(_copyD.findPlate(6), new PlateLocation("R1", "F2", "S2", "B3"));
    updatePlateLocations(plateLocations, _admin, _admin, new LocalDate());
    assertPlatesInLocation(Sets.newHashSet(_copyD.findPlate(1),
                                           _copyD.findPlate(2),
                                           _copyD.findPlate(3),
                                           _copyD.findPlate(4),
                                           _copyD.findPlate(5),
                                           _copyD.findPlate(6)),
                                           "R1", "F2", null, null);
    assertPlatesInLocation(Sets.newHashSet(_copyD.findPlate(1),
                                           _copyD.findPlate(2),
                                           _copyD.findPlate(3),
                                           _copyD.findPlate(4)),
                                           null, null, "S1", null);
    assertPlatesInLocation(Sets.newHashSet(_copyD.findPlate(5),
                                           _copyD.findPlate(6)),
                                           null, null, "S2", null);
    assertPlatesInLocation(Sets.newHashSet(_copyD.findPlate(1)),
                                           "R1", "F2", "S1", "B1");

    // move copyC to same freezer as copyD (F2)
    plateLocations = Maps.newHashMap();
    plateLocations.put(_copyC.findPlate(1), new PlateLocation("R1", "F2", "S3", "B1"));
    plateLocations.put(_copyC.findPlate(2), new PlateLocation("R1", "F2", "S3", "B1"));
    plateLocations.put(_copyC.findPlate(3), new PlateLocation("R1", "F2", "S3", "B2"));
    plateLocations.put(_copyC.findPlate(4), new PlateLocation("R1", "F2", "S3", "B2"));
    plateLocations.put(_copyC.findPlate(5), new PlateLocation("R1", "F2", "S4", "B3"));
    plateLocations.put(_copyC.findPlate(6), new PlateLocation("R1", "F2", "S4", "B3"));
    assertIllegalUpdate(_admin, plateLocations, "bin name must be unique within a freezer");

    plateLocations = Maps.newHashMap();
    plateLocations.put(_copyC.findPlate(1), new PlateLocation("R1", "F2", "S3", "B4"));
    plateLocations.put(_copyC.findPlate(2), new PlateLocation("R1", "F2", "S3", "B4"));
    plateLocations.put(_copyC.findPlate(3), new PlateLocation("R1", "F2", "S3", "B5"));
    plateLocations.put(_copyC.findPlate(4), new PlateLocation("R1", "F2", "S3", "B5"));
    plateLocations.put(_copyC.findPlate(5), new PlateLocation("R1", "F2", "S4", "B6"));
    plateLocations.put(_copyC.findPlate(6), new PlateLocation("R1", "F2", "S4", "B6"));
    updatePlateLocations(plateLocations, _admin, _admin, new LocalDate());
    assertPlatesInLocation(Sets.newHashSet(Iterables.concat(_copyC.getPlates().values(),
                                                            _copyD.getPlates().values())),
                           "R1", "F2", null, null);

    // move copyD to copyC's original freezer (F1)
    plateLocations = Maps.newHashMap();
    plateLocations.put(_copyD.findPlate(1), new PlateLocation("R1", "F1", "S1", "B1"));
    plateLocations.put(_copyD.findPlate(2), new PlateLocation("R1", "F1", "S1", "B1"));
    plateLocations.put(_copyD.findPlate(3), new PlateLocation("R1", "F1", "S1", "B2"));
    plateLocations.put(_copyD.findPlate(4), new PlateLocation("R1", "F1", "S1", "B2"));
    plateLocations.put(_copyD.findPlate(5), new PlateLocation("R1", "F1", "S2", "B3"));
    plateLocations.put(_copyD.findPlate(6), new PlateLocation("R1", "F1", "S2", "B3"));
    // rename copyC bins back to 1...3
    plateLocations.put(_copyC.findPlate(1), new PlateLocation("R1", "F2", "S1", "B1"));
    plateLocations.put(_copyC.findPlate(2), new PlateLocation("R1", "F2", "S1", "B1"));
    plateLocations.put(_copyC.findPlate(3), new PlateLocation("R1", "F2", "S1", "B2"));
    plateLocations.put(_copyC.findPlate(4), new PlateLocation("R1", "F2", "S1", "B2"));
    plateLocations.put(_copyC.findPlate(5), new PlateLocation("R1", "F2", "S2", "B3"));
    plateLocations.put(_copyC.findPlate(6), new PlateLocation("R1", "F2", "S2", "B3"));
    updatePlateLocations(plateLocations, _admin, _admin, new LocalDate());
    assertPlatesInLocation(Sets.newHashSet(_copyC.getPlates().values()), "R1", "F2", null, null);
    assertPlatesInLocation(Sets.newHashSet(_copyD.getPlates().values()), "R1", "F1", null, null);

    // move freezer F1 to room R2
    plateLocations = Maps.newHashMap();
    plateLocations.put(_copyD.findPlate(1), new PlateLocation("R2", "F1", "S1", "B1"));
    plateLocations.put(_copyD.findPlate(2), new PlateLocation("R2", "F1", "S1", "B1"));
    plateLocations.put(_copyD.findPlate(3), new PlateLocation("R2", "F1", "S1", "B2"));
    plateLocations.put(_copyD.findPlate(4), new PlateLocation("R2", "F1", "S1", "B2"));
    plateLocations.put(_copyD.findPlate(5), new PlateLocation("R2", "F1", "S2", "B3"));
    assertIllegalUpdate(_admin, plateLocations, "freezer name must be unique across all rooms");
    plateLocations.put(_copyD.findPlate(6), new PlateLocation("R2", "F1", "S2", "B3"));
    updatePlateLocations(plateLocations, _admin, _admin, new LocalDate());
    assertPlatesInLocation(Sets.newHashSet(_copyD.getPlates().values()), "R2", "F1", null, null);
    
    // move copyD B1 to new freezer F3
    plateLocations = Maps.newHashMap();
    plateLocations.put(_copyD.findPlate(1), new PlateLocation("R2", "F3", "S1", "B1"));
    plateLocations.put(_copyD.findPlate(2), new PlateLocation("R2", "F3", "S1", "B1"));
    updatePlateLocations(plateLocations, _admin, _admin, new LocalDate());
    assertPlatesInLocation(Sets.newHashSet(_copyD.findPlate(1),
                                           _copyD.findPlate(2)),
                                           "R2", "F3", null, "B1");
    assertPlatesInLocation(Sets.newHashSet(_copyD.findPlate(3),
                                           _copyD.findPlate(4),
                                           _copyD.findPlate(5),
                                           _copyD.findPlate(6)),
                                           "R2", "F1", null, null);
    
    // split copyD B1 into 2 bins, placing new bin on new shelf
    plateLocations = Maps.newHashMap();
    plateLocations.put(_copyD.findPlate(2), new PlateLocation("R2", "F3", "S2", "B2"));
    updatePlateLocations(plateLocations, _admin, _admin, new LocalDate());
    assertPlatesInLocation(Sets.newHashSet(_copyD.findPlate(2)), "R2", "F3", "S2", "B2");
    assertPlatesInLocation(Sets.newHashSet(_copyD.findPlate(1), _copyD.findPlate(2)),
                           "R2", "F3", null, null);

    // move copyD B3 to F1, S1, having all bins on same shelf in F1
    plateLocations = Maps.newHashMap();
    plateLocations.put(_copyD.findPlate(5), new PlateLocation("R2", "F1", "S1", "B3"));
    plateLocations.put(_copyD.findPlate(6), new PlateLocation("R2", "F1", "S1", "B3"));
    updatePlateLocations(plateLocations, _admin, _admin, new LocalDate());
    assertPlatesInLocation(Sets.newHashSet(_copyD.findPlate(3),
                                           _copyD.findPlate(4),
                                           _copyD.findPlate(5),
                                           _copyD.findPlate(6)),
                                           "R2", "F1", "S1", null);
  }

  public void assertIllegalUpdate(AdministratorUser admin,
                                  Map<Plate,PlateLocation> plateLocations,
                                  String exceptionMessageSubstring)
  {
    try {
      updatePlateLocations(plateLocations, admin, admin, new LocalDate());
      fail("expected exception: " + exceptionMessageSubstring);
    }
    catch (BusinessRuleViolationException e) {
      assertTrue(e.getMessage().contains(exceptionMessageSubstring));
    }
  }

  private void assertPlatesInLocation(Set<Plate> plates, String room, String freezer, String shelf, String bin)
  {
    final HqlBuilder hql = new HqlBuilder();
    hql.select("p");
    hql.from(Plate.class, "p").from("p", Plate.location.getPath(), "l", JoinType.INNER);
    hql.whereIn("p", plates);
    if (room != null) {
      hql.where("l", "room", Operator.EQUAL, room);
    }
    if (freezer != null) {
      hql.where("l", "freezer", Operator.EQUAL, freezer);
    }
    if (shelf != null) {
      hql.where("l", "shelf", Operator.EQUAL, shelf);
    }
    if (bin != null) {
      hql.where("l", "bin", Operator.EQUAL, bin);
    }
    Query<Plate> query = new Query<Plate>() {
      @Override
      public List<Plate> execute(Session session)
      {
        return hql.toQuery(session, true).list();
      }
    };
    List<Plate> result = genericEntityDao.runQuery(query);
    assertEquals("plates match location " + room + "-" + freezer + "-" + shelf + "-" + bin,
                 plates,
                 Sets.newHashSet(result));
  }

}
