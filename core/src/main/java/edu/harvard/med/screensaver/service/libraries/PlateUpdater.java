// $HeadURL: $
// $Id: $
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.libraries;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import org.apache.commons.collections.BagUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.Query;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.MolarConcentration;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivity;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.libraries.PlateLocation;
import edu.harvard.med.screensaver.model.libraries.PlateStatus;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.ui.arch.util.converter.MgMlConcentrationConverter;
import edu.harvard.med.screensaver.util.NullSafeUtils;

public class PlateUpdater
{
  private static final Logger log = Logger.getLogger(PlateUpdater.class);

  public static final String NO_BIN = "<not specified>";
  public static final String NO_SHELF = "<not specified>";
  public static final String NO_FREEZER = "<not specified>";
  public static final String NO_ROOM = "<not specified>";

  private static final Function<Collection<?>,Integer> CollectionSize = new Function<Collection<?>,Integer>() {
    public Integer apply(Collection<?> c)
    {
      return c.size();
    }
  };

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
    
    if (!!!plate.getStatus().canTransitionTo(newStatus)) {
      throw new BusinessRuleViolationException("plate status cannot be changed from '" + plate.getStatus() + "' to '" + newStatus +
        "'");
    }

    plate = _dao.reloadEntity(plate);
    recordedBy = _dao.reloadEntity(recordedBy);
    performedBy = _dao.reloadEntity(performedBy);

    StringBuilder comments = new StringBuilder().append("Status changed from '").append(plate.getStatus()).append("' to '").append(newStatus).append("'");
    AdministrativeActivity updateActivity = plate.createUpdateActivity(AdministrativeActivityType.PLATE_STATUS_UPDATE,
                                                                       recordedBy,
                                                                       comments.toString());
    updateActivity.setPerformedBy(performedBy);
    updateActivity.setDateOfActivity(datePerformed);
    if (plate.getStatus() != newStatus) {
      plate.setStatus(newStatus);
      // TODO: if this method is being called repeatedly for multiple plates, it is inefficient to make the following call each time
      updatePrimaryPlateStatus(plate.getCopy());
    }

    if (plate.getPlatedActivity() == null && (newStatus == PlateStatus.AVAILABLE || newStatus == PlateStatus.NOT_AVAILABLE)) {
      plateCreated(plate, updateActivity);
    }
    if (newStatus.compareTo(PlateStatus.RETIRED) >= 0) {
      if (plate.getRetiredActivity() == null) {
        plate.setRetiredActivity(updateActivity);
      }
      if (newStatus.compareTo(PlateStatus.RETIRED) > 0) {
        plateNoLongerMaintained(plate, newStatus, recordedBy, performedBy, datePerformed);
      }
    }
    return true;
  }

  private static Comparator<Map.Entry<PlateStatus,Integer>> plateStatusFrequencyComparator =
    new Comparator<Map.Entry<PlateStatus,Integer>>() {
      @Override
      public int compare(Entry<PlateStatus,Integer> o1, Entry<PlateStatus,Integer> o2)
      {
        int result = o1.getValue().compareTo(o2.getValue());
        if (result == 0) {
          result = -1 * o1.getKey().compareTo(o2.getKey());
        }
        return result;
      }
    };

  private void updatePrimaryPlateStatus(Copy copy)
  {
    Map<PlateStatus,Integer> statusCounts = Maps.transformValues(Multimaps.index(copy.getPlates().values(),
                                                           Plate.ToStatus).asMap(), CollectionSize);
    PlateStatus primaryPlateStatus = Collections.max(statusCounts.entrySet(), plateStatusFrequencyComparator).getKey();
    copy.setPrimaryPlateStatus(primaryPlateStatus);
    copy.setPlatesAvailable(statusCounts.get(PlateStatus.AVAILABLE));
  }
        
  private void updatePrimaryPlateConcentrations(Copy copy)
  {
    Map<BigDecimal,Integer> mgMlCounts = Maps.transformValues(Multimaps.index(
                                                                              Lists.newArrayList(Iterators.filter(copy.getPlates().values().iterator(),
                                                                                                                  Predicates.compose(Predicates.notNull(),Plate.ToMgMlConcentration))),
                                                                                                                  Plate.ToMgMlConcentration).asMap(), CollectionSize);
                                                                                                                  
    if(!mgMlCounts.isEmpty()) copy.setPrimaryPlateMgMlConcentration(findMaxByValueThenKey(mgMlCounts).getKey());

    Map<MolarConcentration,Integer> molarCounts = Maps.transformValues(Multimaps.index(
                                                                                       Lists.newArrayList(Iterators.filter(copy.getPlates().values().iterator(),
                                                                                                        Predicates.compose(Predicates.notNull(), Plate.ToMolarConcentration))),
                                                                                                        Plate.ToMolarConcentration).asMap(), CollectionSize);
    if(!molarCounts.isEmpty()) copy.setPrimaryPlateMolarConcentration(findMaxByValueThenKey(molarCounts).getKey());
  }
  
  private <T extends Comparable<? super T>, U extends Comparable<? super U>> Map.Entry<T,U> findMaxByValueThenKey(Map<T,U> map)
  {
    // This method avoids having to define a comparator for each type
    // TODO: figure out a nice generic/functional way to do this
    if(map.isEmpty()) return null;
    Map.Entry<T,U> max = null;
    for(Map.Entry<T,U> entry: map.entrySet())
    {
      if(max == null || entry.getValue().compareTo(max.getValue()) > 0 ) max = entry;
      if(entry.getValue().compareTo(max.getValue()) == 0) {
        if(entry.getKey().compareTo(max.getKey()) > 0 ) max = entry;
      }
    }
    return max;
  }

  private void plateNoLongerMaintained(Plate plate,
                                      PlateStatus newStatus,
                                      AdministratorUser recordedBy,
                                      AdministratorUser performedBy,
                                      LocalDate datePerformed)
  {
    updatePlateLocation(plate,
                        null,
                        recordedBy,
                        performedBy,
                        datePerformed);
    AdministrativeActivity lastActivity = plate.getLastRecordedUpdateActivityOfType(AdministrativeActivityType.PLATE_LOCATION_TRANSFER);
    lastActivity.setComments(lastActivity.getComments() + " (plate no longer has a location due to change of status to " +
      newStatus + ")");
  }

  private void plateCreated(Plate plate, AdministrativeActivity updateActivity)
  {
    plate.setPlatedActivity(updateActivity);
    LocalDate datePlated = updateActivity.getDateOfActivity();
    plate.getCopy().setDatePlated(datePlated);
    LocalDate dateLibraryScreenable = plate.getCopy().getLibrary().getDateScreenable();
    if (dateLibraryScreenable == null || datePlated.compareTo(dateLibraryScreenable) < 0) {
      plate.getCopy().getLibrary().setDateScreenable(datePlated);
    }
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
    if (!!!plate.getStatus().isInventoried() && newLocationDto != null) {
      throw new BusinessRuleViolationException("plate location cannot be specified for non-inventoried plate status " +
        plate.getStatus());
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
    // TODO: if this method is being called repeatedly for multiple plates, it is inefficient to make the following call each time
    updatePrimaryPlateLocation(plate.getCopy());

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

  private static Comparator<Map.Entry<PlateLocation,Integer>> plateLocationFrequencyComparator =
    new Comparator<Map.Entry<PlateLocation,Integer>>() {
      @Override
      public int compare(Entry<PlateLocation,Integer> o1, Entry<PlateLocation,Integer> o2)
      {
        int result = o1.getValue().compareTo(o2.getValue());
        if (result == 0) {
          result = -1 * o1.getKey().toDisplayString().compareTo(o2.getKey().toDisplayString());
        }
        return result;
      }
    };

  private void updatePrimaryPlateLocation(Copy copy)
  {
    Set<Entry<PlateLocation,Integer>> plateLocationCounts = Maps.transformValues(Multimaps.index(Iterables.filter(copy.getPlates().values(), Predicates.compose(Predicates.notNull(), Plate.ToLocation)), Plate.ToLocation).asMap(), CollectionSize).entrySet();
    if (plateLocationCounts.isEmpty()) {
      copy.setPrimaryPlateLocation(null);
    }
    else {
      PlateLocation primaryPlateLocation =
        Collections.max(plateLocationCounts, plateLocationFrequencyComparator).getKey();
      copy.setPrimaryPlateLocation(primaryPlateLocation);
      copy.setPlateLocationsCount(plateLocationCounts.size());
    }
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
  public boolean updateMolarConcentration(Plate plate, MolarConcentration newConcentration, AdministratorUser recordByAdmin)
  {
    if (!!!NullSafeUtils.nullSafeEquals(newConcentration, plate.getMolarConcentration())) {
      plate = _dao.reloadEntity(plate);
      StringBuilder updateComments = new StringBuilder().append("Concentration (molar) changed from '").append(NullSafeUtils.toString(plate.getMolarConcentration(), "<not specified>")).append("' to '").append(newConcentration).append("'");
      plate.createUpdateActivity(recordByAdmin, updateComments.toString());
      plate.setMolarConcentration(newConcentration);
      updatePrimaryPlateConcentrations(plate.getCopy());
      return true;
    }
    return false;
  }

  @Transactional
  public boolean updateMgMlConcentration(Plate plate, BigDecimal newConcentration, AdministratorUser recordByAdmin)
  {
    if (!!!NullSafeUtils.nullSafeEquals(newConcentration, plate.getMgMlConcentration())) {
      plate = _dao.reloadEntity(plate);
      StringBuilder updateComments = new StringBuilder().append("Concentration (mg/mL) changed from '").append(NullSafeUtils.toString(plate.getMgMlConcentration(), "<not specified>")).append("' to '").append(newConcentration).append("'");
      plate.createUpdateActivity(recordByAdmin, updateComments.toString());
      plate.setMgMlConcentration(newConcentration);
      updatePrimaryPlateConcentrations(plate.getCopy());
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
    plate.createComment(recordedByAdmin, comment);
  }
}
