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

import com.google.common.collect.ImmutableMap;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.Query;
import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.Concentration;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.libraries.PlateLocation;
import edu.harvard.med.screensaver.model.libraries.PlateStatus;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.util.NullSafeUtils;

public class PlateUpdater
{
  private static final Logger log = Logger.getLogger(PlateUpdater.class);

  public static final String NO_BIN = "<not specified>";
  public static final String NO_SHELF = "<not specified>";
  public static final String NO_FREEZER = "<not specified>";
  public static final String NO_ROOM = "<not specified>";

  private GenericEntityDAO _dao;
  private PlateFacilityIdInitializer _plateFacilityIdInitializer;

  protected PlateUpdater()
  {}

  public PlateUpdater(GenericEntityDAO dao,
                      PlateFacilityIdInitializer plateFacilityIdInitializer)
  {
    _dao = dao;
    _plateFacilityIdInitializer = plateFacilityIdInitializer;
  }

  @Transactional
  public boolean updatePlateStatus(Plate plate,
                                   PlateStatus newStatus,
                                   AdministratorUser recordedBy,
                                   AdministratorUser performedBy,
                                   LocalDate datePerformed)
  {
    if (plate.getStatus().equals(newStatus)) {
      return false;
    }
    
    if (newStatus.compareTo(plate.getStatus()) < 0 || plate.getStatus().compareTo(PlateStatus.RETIRED) > 0) {
      throw new BusinessRuleViolationException("plate status cannot be changed from '" + plate.getStatus() + "' to '" + newStatus +
        "'");
    }

    plate = _dao.reloadEntity(plate);
    recordedBy = _dao.reloadEntity(recordedBy);
    performedBy = _dao.reloadEntity(performedBy);
    StringBuilder comments = new StringBuilder();
    comments.append("Status changed from '").append(plate.getStatus()).append("' to '").append(newStatus).append("'");
    AdministrativeActivity updateActivity = plate.createUpdateActivity(AdministrativeActivityType.PLATE_STATUS_UPDATE,
                                                                       recordedBy,
                                                                       comments.toString());
    updateActivity.setPerformedBy(performedBy);
    updateActivity.setDateOfActivity(datePerformed);
    plate.setStatus(newStatus);

    if (newStatus == PlateStatus.AVAILABLE) {
      plate.setPlatedActivity(updateActivity);
      LocalDate datePlated = updateActivity.getDateOfActivity();
      plate.getCopy().setDatePlated(datePlated);
      LocalDate dateLibraryScreenable = plate.getCopy().getLibrary().getDateScreenable();
      if (dateLibraryScreenable == null || datePlated.compareTo(dateLibraryScreenable) < 0) {
        plate.getCopy().getLibrary().setDateScreenable(datePlated);
      }
    }
    else if (newStatus.compareTo(PlateStatus.RETIRED) >= 0) {
      if (plate.getRetiredActivity() == null) {
        plate.setRetiredActivity(updateActivity);
      }
      if (newStatus.compareTo(PlateStatus.RETIRED) > 0) {
        updatePlateLocation(plate,
                            null,
                            recordedBy,
                            performedBy,
                            datePerformed);
        AdministrativeActivity lastActivity = plate.getLastRecordedUpdateActivityOfType(AdministrativeActivityType.PLATE_LOCATION_TRANSFER);
        lastActivity.setComments(lastActivity.getComments() + " (plate no longer has a location due to change of status to " +
          newStatus + ")");
      }
    }
    return true;
  }

  /*
   * Due to Hibernate issues with flush(), the caller should invoke this method in a transaction, and must call
   * validateLocations() before the end of the transaction. Any newly created locations must be flushed before calling
   * this method
   */
  @Transactional
  public boolean updatePlateLocation(Plate plate,
                                     PlateLocation newLocationDto,
                                     AdministratorUser recordedByAdmin,
                                     AdministratorUser performedByAdmin,
                                     LocalDate datePerformed)

  {
    if (performedByAdmin == null) {
      throw new IllegalArgumentException("performedByAdmin required");
    }
    recordedByAdmin = _dao.reloadEntity(recordedByAdmin);
    performedByAdmin = _dao.reloadEntity(performedByAdmin);
    PlateLocation newLocation = null;
    if (newLocationDto != null) {
      newLocation = findLocation(newLocationDto);
      if (newLocation == null) {
        newLocation = createLocation(newLocationDto);
      }
    }

    plate = _dao.reloadEntity(plate);
    PlateLocation oldLocation = plate.getLocation();
    if (NullSafeUtils.nullSafeEquals(oldLocation, newLocation)) {
      return false;
    }

    plate.setLocation(newLocation);

    StringBuilder comments = new StringBuilder();
    comments.append("Location changed from '").append(oldLocation == null ? "<none>" : oldLocation.toDisplayString()).
      append("' to '").append(newLocation == null ? "<none>" : newLocation.toDisplayString()).append("'");
    AdministrativeActivity updateActivity =
      plate.createUpdateActivity(AdministrativeActivityType.PLATE_LOCATION_TRANSFER,
                                 recordedByAdmin,
                                 comments.toString());
    updateActivity.setPerformedBy(performedByAdmin);
    updateActivity.setDateOfActivity(datePerformed);
    
    // TODO: we want to be able to call validateLocations() here, but the resultant flush() call is causing problems with Plate.updateActivities, where newly added elements are not being added to the collection, although the activity itself is being persisted
    //validateLocations();
    
    return true;
  }

  private PlateLocation findLocation(PlateLocation locationDto)
  {
    if (locationDto.getRoom() == null ||
      locationDto.getFreezer() == null ||
      locationDto.getShelf() == null ||
      locationDto.getBin() == null) {
      return null;
    }

    //_dao.flush(); TODO: reinstate, causing problems with Plate.updateActivities, where newly added elements are not being added to the collection, although the activity itself is being persisted
    PlateLocation plateLocation =
      _dao.findEntityByProperties(PlateLocation.class,
                                  ImmutableMap.<String,Object>of("room", locationDto.getRoom(),
                                                                 "freezer", locationDto.getFreezer(),
                                                                 "shelf", locationDto.getShelf(),
                                                                 "bin", locationDto.getBin()));
    return plateLocation;
  }

  private PlateLocation createLocation(PlateLocation locationDto)
  {
    PlateLocation newPlateLocation = new PlateLocation(locationDto);
    _dao.persistEntity(newPlateLocation);
    // TODO: report which location parts are new ("created new room", "created new freezer", etc.)
    return newPlateLocation;
  }

  // TODO: this should be a pluggable strategy/policy
  public void validateLocations()
  {
    _dao.flush();
    // ICCB-L constraint: freezer implies room
    List<String> splitFreezers = _dao.runQuery(new Query<String>() {
      @SuppressWarnings("unchecked")
      public List<String> execute(org.hibernate.Session session) {
        org.hibernate.Query query = session.createQuery("select pl.freezer " +
                                                        "from PlateLocation pl " +
                                                        "where exists (select p from Plate p where p.location = pl) " +
                                                        "and pl.freezer <> :noFreezer " +
                                                        "group by pl.freezer having count(distinct pl.room) > 1");
        query.setString("noFreezer", NO_FREEZER);
        return query.list();
      }
    });
    if (!!!splitFreezers.isEmpty()) {
      throw new BusinessRuleViolationException("attempted to assign freezer " +
        splitFreezers.get(0) +
        " to multiple rooms, but freezer name must be unique across all rooms (hint: to change a freezer's room, you must move all plates in freezer at once)");
    }

    // ICCB-L constraint: bin number is unique for freezer
    List<Object> splitBins = _dao.runQuery(new Query<Object>() {
      @SuppressWarnings("unchecked")
      public List<Object> execute(org.hibernate.Session session)
      {
        org.hibernate.Query query = session.createQuery("select pl.bin, pl.freezer " +
                                                        "from PlateLocation pl " +
                                                        "where exists (select p from Plate p where p.location = pl) " +
                                                        "and pl.bin <> :noBin " +
                                                        "group by pl.freezer, pl.bin having count(distinct pl.shelf) > 1");
        query.setString("noBin", NO_BIN);
        return query.list();
      }
    });
    if (!!!splitBins.isEmpty()) {
      Object[] row = (Object[]) splitBins.get(0);
      throw new BusinessRuleViolationException("attempted to assign bin " +
        row[0] + " to multiple shelves within freezer " + row[1] +
        ", but bin name must be unique within a freezer (hint: to change a bin's shelf you must move all plates in bin at once)");
    }
  }

  @Transactional
  public boolean updateWellVolume(Plate plate, Volume newWellVolume, AdministratorUser recordedByAdmin)
  {
    if (!!!NullSafeUtils.nullSafeEquals(newWellVolume, plate.getWellVolume())) {
      plate = _dao.reloadEntity(plate);
      StringBuilder updateComments = new StringBuilder().append("Well Volume changed from '").append(NullSafeUtils.toString(plate.getWellVolume(), "<not specified>")).append("' to '").append(newWellVolume).append("'");
      plate.createUpdateActivity(recordedByAdmin, updateComments.toString());
      plate.setWellVolume(newWellVolume);
      return true;
    }
    return false;
  }

  @Transactional
  public boolean updateConcentration(Plate plate, Concentration newConcentration, AdministratorUser recordByAdmin)
  {
    if (!!!NullSafeUtils.nullSafeEquals(newConcentration, plate.getConcentration())) {
      plate = _dao.reloadEntity(plate);
      StringBuilder updateComments = new StringBuilder().append("Concentration changed from '").append(NullSafeUtils.toString(plate.getConcentration(), "<not specified>")).append("' to '").append(newConcentration).append("'");
      plate.createUpdateActivity(recordByAdmin, updateComments.toString());
      plate.setConcentration(newConcentration);
      return true;
    }
    return false;
  }

  @Transactional
  public boolean updatePlateType(Plate plate, PlateType newPlateType, AdministratorUser recordedByAdmin)
  {
    if (!!!newPlateType.equals(plate.getPlateType())) {
      plate = _dao.reloadEntity(plate);
      StringBuilder updateComments = new StringBuilder().append("Plate Type changed from '").append(NullSafeUtils.toString(plate.getPlateType(), "<not specified>")).append("' to '").append(newPlateType).append("'");
      plate.createUpdateActivity(recordedByAdmin, updateComments.toString());
      plate.setPlateType(newPlateType);
      return true;
    }
    return false;
  }

  @Transactional
  public boolean updateFacilityId(Plate plate, AdministratorUser recordedByAdmin)
  {
    plate = _dao.reloadEntity(plate);
    String oldFacilityId = plate.getFacilityId();
    _plateFacilityIdInitializer.initializeFacilityId(plate);
    if (!!!NullSafeUtils.nullSafeEquals(oldFacilityId, plate.getFacilityId())) {
      plate.createUpdateActivity(recordedByAdmin, "Facility ID changed from '" +
                                 NullSafeUtils.toString(oldFacilityId, "<not specified>") + "' to '" +
                                 NullSafeUtils.toString(plate.getFacilityId(), "<not specified>") + "'");
      return true;
    }
    return false;

  }

  @Transactional
  public void addComment(Plate plate, AdministratorUser recordedByAdmin, String comment)
  {
    plate = _dao.reloadEntity(plate);
    plate.createUpdateActivity(AdministrativeActivityType.COMMENT, recordedByAdmin, comment);
  }
}
