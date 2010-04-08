// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.service.screens;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.screens.LabActivity;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenDataSharingLevel;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.StatusValue;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.service.OperationRestrictedException;
import edu.harvard.med.screensaver.service.ServiceMessages;
import edu.harvard.med.screensaver.util.Pair;

/**
 * The Screen Expiration Service is used to adjust the DataSharingLevel of the Screen.  Screens that are more restrictive have 
 * the ScreenDataSharingLevel of "shared" or "mutual" sharing levels.  These more restrictive Screens are subject expiration of 
 * this privacy to the "mutual" level after an expiration period. <br>
 * The expiration period is calculated as the "ageToExpireFromActivityDateInDays" days since the last Screening (Lab) Activity 
 * for a Screen.  
 * <ul>
 * <li>The calculated expiration date will be stored in the Screen.dataPrivacyExpirationDate (DPED).  The DPED is set 
 * independently, using this updater, from a batch process.  
 * <li>Independently, other batch process will use this service: 
 * <li>to query for Screens that will be expiring in a certain period, in order to notify concerned parties
 * <li>to query for Screens that have had a publication (as this is an indicator that the privacy should be manually expired).
 * <li>to expire those Screens that should expire, as indicated by a DPED that has passed. 
 * @author sde4
 */
public class ScreenDataSharingLevelUpdater
{
  private static Logger log = Logger.getLogger(ScreenDataSharingLevelUpdater.class);
  private GenericEntityDAO _dao;
  private ServiceMessages _messages;
  private boolean testMode = false;
  
  protected ScreenDataSharingLevelUpdater() {}
  
  public ScreenDataSharingLevelUpdater(GenericEntityDAO dao, ServiceMessages serviceMessages)
  {
    _dao = dao;
    _messages = serviceMessages;
  }

  @Transactional
  public AdministrativeActivity updateScreen(Screen screen, 
                                             ScreenDataSharingLevel screenDataSharingLevel, 
                                             AdministratorUser recordedBy)
  {
    verifyOperationPermitted(recordedBy);
    ScreenDataSharingLevel oldLevel = screen.getDataSharingLevel();
    screen.setDataSharingLevel(screenDataSharingLevel);
    return screen.createUpdateActivity(recordedBy,
                                       "Data Sharing Level updated from : " + oldLevel 
                                       + " to  " + screenDataSharingLevel);
  }
  
  /**
   * Returns a unique list of Small Molecule Screens that:
   * <ul>
   * <li>have ScreenResults
   * <li>have ScreenDataSharingLevel that is more restrictive than {@link ScreenDataSharingLevel#MUTUAL_SCREENS}
   * <li>have a {@link Screen#getDataPrivacyExpirationDate()} on or before the expireDate
   * <li>do not have a status of {@link StatusValue#DROPPED_TECHNICAL} or {@link StatusValue#TRANSFERRED_TO_BROAD_INSTITUTE}
   * @param expireDate
   * @return not null, empty Set if nothing is found
   */
  public List<Screen> findNewExpiredNotNotified(LocalDate expireDate)
  {
    String hql = "select distinct(s) from Screen s " + 
        " join s.screenResult sr " +
        " where" +
        " s.dataPrivacyExpirationDate <= ? " +
        " and s.dataSharingLevel > ? " +
        " and s.screenType = ? " +
        " and s.dataPrivacyExpirationNotifiedDate is null" +
        " and s.screenId not in (select s.screenId from Screen s join s.statusItems si where si.statusValue in ( ?, ? ) and si.screen = s ) " + 
        " order by s.screenNumber";
    List<Screen> list = _dao.findEntitiesByHql(Screen.class, hql, 
                                  expireDate, 
                                  ScreenDataSharingLevel.MUTUAL_SCREENS, 
                                  ScreenType.SMALL_MOLECULE ,
                                  StatusValue.DROPPED_TECHNICAL, 
                                  StatusValue.TRANSFERRED_TO_BROAD_INSTITUTE
                                  );
    log.info("Hql: " + hql + ", " + list.size());
    return list;
  }
  
  public List<Screen> findExpired(LocalDate expireDate)
  {
    String hql = "select distinct(s) from Screen s " + 
        " join s.screenResult sr " +
        " where" +
        " s.dataPrivacyExpirationDate <= ? " +
        " and s.dataSharingLevel > ? " +
        " and s.screenType = ? " +
        " and s.screenId not in (select s.screenId from Screen s join s.statusItems si where si.statusValue in ( ?, ? ) and si.screen = s ) " + 
        " order by s.screenNumber";
    List<Screen> list = _dao.findEntitiesByHql(Screen.class, hql, 
                                  expireDate, 
                                  ScreenDataSharingLevel.MUTUAL_SCREENS, 
                                  ScreenType.SMALL_MOLECULE ,
                                  StatusValue.DROPPED_TECHNICAL, 
                                  StatusValue.TRANSFERRED_TO_BROAD_INSTITUTE
                                  );
    log.info("Hql: " + hql + ", " + list.size());
    return list;
  }

  /**
   * For Screens that:
   * <ul>
   * <li>have ScreenResults
   * <li>have ScreenDataSharingLevel that is more restrictive than {@link ScreenDataSharingLevel#MUTUAL_SCREENS}
   * <li>have a {@link Screen#getDataPrivacyExpirationDate()} on or before the expireDate
   * <li>do not have a status of {@link StatusValue#DROPPED_TECHNICAL} or {@link StatusValue#TRANSFERRED_TO_BROAD_INSTITUTE}
   * </ul>
   * Expire any Screen returned from {@link ScreenDataSharingLevelUpdater#findNewExpiredNotNotified(LocalDate)} wherein the 
   * {@link Screen#getDataPrivacyExpirationDate()} is less than or equal to the passed in date.
   * @param date
   * @return Screens updated
   */
  @Transactional
  public List<Pair<Screen,AdministrativeActivity>> expireScreenDataSharingLevels(LocalDate date, AdministratorUser recordedBy)
  {
    verifyOperationPermitted(recordedBy);
    List<Screen> screens = findExpired(date);
    List<Pair<Screen,AdministrativeActivity>> results = Lists.newLinkedList();
    for(Screen screen:screens)
    {
      assert(screen.getDataPrivacyExpirationDate().compareTo(date) <= 0);
      AdministrativeActivity activity = updateScreen(screen, ScreenDataSharingLevel.MUTUAL_SCREENS, recordedBy);
      results.add(new Pair<Screen,AdministrativeActivity>(screen,activity));
    }
    return results;
  }

  public static class DataPrivacyAdjustment
  {
    public boolean isEmpty(boolean considerOverrides)
    {
      return (screensAdjusted.isEmpty() && screensAdjustedToAllowed.isEmpty() && !considerOverrides)
      || ( screensAdjusted.isEmpty() && screensAdjustedToAllowed.isEmpty() && considerOverrides && screenPrivacyAdjustmentNotAllowed.isEmpty() );
    }
    public List<Pair<Screen,AdministrativeActivity>> screensAdjusted = Lists.newLinkedList();
    public List<Pair<Screen,AdministrativeActivity>> screensAdjustedToAllowed = Lists.newLinkedList();
    public List<Pair<Screen,String>> screenPrivacyAdjustmentNotAllowed = Lists.newLinkedList();
  }
  
  /**
   * For Screens that:
   * <ul>
   * <li>have ScreenResults
   * <li>have ScreenDataSharingLevel that is more restrictive than {@link ScreenDataSharingLevel#MUTUAL_SCREENS}
   * <li>have a {@link Screen#getDataPrivacyExpirationDate()} on or before the expireDate
   * <li>do not have a status of {@link StatusValue#DROPPED_TECHNICAL} or {@link StatusValue#TRANSFERRED_TO_BROAD_INSTITUTE}
   * </ul>
   * This method invokes {@link Screen#setDataPrivacyExpirationDate(LocalDate)} with a value that is ageToExpireFromActivityDateInDays
   * days from the latest LabActivity (by date) for the Screen.  
   * @param ageToExpireFromActivityDateInDays the expiration period, in days
   * @param admin user performing
   * @return List<Screen, Pair<Admin-activity-if-any, message-if-no-action> >
   */
  @Transactional
  public DataPrivacyAdjustment
    adjustDataPrivacyExpirationByActivities(int ageToExpireFromActivityDateInDays, AdministratorUser admin)
  {
    String hql = "select distinct(s) from Screen as s " +
        " join s.screenResult sr " +
    		" inner join s.labActivities la " +
    		" where " +
    		" s.screenType = ? " +
    		" and s.dataSharingLevel > ? " +
        " and s.screenId not in (select s.screenId from Screen s join s.statusItems si where si.statusValue in ( ?, ? ) and si.screen = s ) " +
    		" order by s.screenNumber"  ;

    List<Screen> screens = _dao.findEntitiesByHql(Screen.class, hql, 
                                                  ScreenType.SMALL_MOLECULE, 
                                                  ScreenDataSharingLevel.MUTUAL_SCREENS,
                                                  StatusValue.DROPPED_TECHNICAL, 
                                                  StatusValue.TRANSFERRED_TO_BROAD_INSTITUTE);
    
    // Note, have to iterate in code to examine the date values as a suitable date arithmetic method was not determined for the HQL - sde4

    DataPrivacyAdjustment dataPrivacyAdjustment = new DataPrivacyAdjustment();
    
    for(Screen s:screens)
    {
      // note that getLabActivities does a "natural" sort, and that the comparator for the LabActivity uses the dateOfActivity to sort.
      
      SortedSet<LibraryScreening> libraryScreenings = s.getLabActivitiesOfType(LibraryScreening.class);
      if(libraryScreenings.isEmpty())
      {
        log.debug("No LibraryScreenings for the screen: " + s);
      }
      else
      {
        //for [#2285] - don't consider Library Screenings of user provided plates 
        for(Iterator<LibraryScreening> iter = libraryScreenings.iterator(); iter.hasNext();)
        {
          if(iter.next().isForScreenerProvidedPlates()) iter.remove();
        }
        LibraryScreening libraryScreening = libraryScreenings.last();
        
        LocalDate requestedDate = libraryScreening.getDateOfActivity().plusDays(ageToExpireFromActivityDateInDays);
        LocalDate currentExpiration = s.getDataPrivacyExpirationDate();
        
        if( currentExpiration != null && currentExpiration.equals(requestedDate) )
        {
          log.info("DataPrivacyExpirationDate is already set to the correct value for screen number: " + s.getScreenNumber());
        } else {
          s.setDataPrivacyExpirationDate(requestedDate);
          LocalDate setDate = s.getDataPrivacyExpirationDate();
  
          AdministrativeActivity adminActivity= null;
          
          if(currentExpiration != null && currentExpiration.equals(setDate))
          {
            // adjustment not allowed - overridden
            String msg = _messages.getMessage("admin.screens.dataPrivacyExpiration.dataPrivacyExpirationdate.adjustment.adjustmentNotAllowed.comment",
                                              currentExpiration,
                                              libraryScreening.getDateOfActivity(),
                                              requestedDate);
  
            dataPrivacyAdjustment.screenPrivacyAdjustmentNotAllowed.add(Pair.newPair(s, msg));
          } 
          else 
          {
            if(! requestedDate.equals(setDate))
            {
              // adjustment allowed - but overridden to...
              String msg = _messages.getMessage("admin.screens.dataPrivacyExpiration.dataPrivacyExpirationdate.adjustment.adjustedBasedOnAllowed.comment",
                                                currentExpiration,
                                                libraryScreening.getDateOfActivity(),
                                                requestedDate,
                                                setDate);
              adminActivity = s.createUpdateActivity(admin,msg);
              dataPrivacyAdjustment.screensAdjustedToAllowed.add(Pair.newPair(s,adminActivity));
            }
            else
            {
              // adjustment allowed
              String msg =  _messages.getMessage("admin.screens.dataPrivacyExpiration.dataPrivacyExpirationdate.adjustment.basedOnActivity.comment", 
                                           currentExpiration,
                                           libraryScreening.getDateOfActivity(),
                                           setDate);
              adminActivity = s.createUpdateActivity(admin,msg);
              dataPrivacyAdjustment.screensAdjusted.add(Pair.newPair(s,adminActivity));
            }
            // we null the notified date, since it should be re-notified -sde4
            s.setDataPrivacyExpirationNotifiedDate(null);
          }
        }
      }
    }
    log.info("adjustDataPrivacyExpirationByActivities, " +
        " adjusted: " + dataPrivacyAdjustment.screensAdjusted.size() +
        " adjusted to allowed: " + dataPrivacyAdjustment.screensAdjustedToAllowed.size() +
    		", adjustment not allowed: " + dataPrivacyAdjustment.screenPrivacyAdjustmentNotAllowed.size() );
    return dataPrivacyAdjustment;
  }

  /**
   * Find Screens that:
   * <ul>
   * <li>have Publications
   * <li>have ScreenResults
   * <li>have ScreenDataSharingLevel that is more restrictive than {@link ScreenDataSharingLevel#MUTUAL_SCREENS}
   * <li>have a {@link Screen#getDataPrivacyExpirationDate()} on or before the expireDate
   * <li>do not have a status of {@link StatusValue#DROPPED_TECHNICAL} or {@link StatusValue#TRANSFERRED_TO_BROAD_INSTITUTE}
   * </ul>
   * @return
   */
  public List<Screen> findNewPublishedPrivate()
  {
    String hql = "select distinct (s) from Screen as s " +
    		" join s.publications " +
        " join s.screenResult sr " +
    		" where s.dataSharingLevel > ? " +
    		" and s.screenType = ? " +
        " and s.screenId not in (select s.screenId from Screen s join s.statusItems si where si.statusValue in ( ?, ? ) and si.screen = s ) " + 
    		" order by s.screenNumber";
    return _dao.findEntitiesByHql(Screen.class, hql, 
                                  ScreenDataSharingLevel.SHARED, 
                                  ScreenType.SMALL_MOLECULE,
                                  StatusValue.DROPPED_TECHNICAL, 
                                  StatusValue.TRANSFERRED_TO_BROAD_INSTITUTE);
  }

  @Transactional
  public void setDataPrivacyExpirationNotifiedDate(Screen screen)
  {
    screen.setDataPrivacyExpirationNotifiedDate(new LocalDate());
    _dao.saveOrUpdateEntity(screen);
  }
  
  private void verifyOperationPermitted(AdministratorUser recordedBy)
  throws OperationRestrictedException
  {
    if (!!!recordedBy.getScreensaverUserRoles().contains(ScreensaverUserRole.SCREEN_DATA_SHARING_LEVELS_ADMIN)) {
      throw new OperationRestrictedException("to update a Screen data sharing level, administrator must have " 
                                             + ScreensaverUserRole.SCREEN_DATA_SHARING_LEVELS_ADMIN.getDisplayableRoleName() + " role");
    }
  }
  
  public Set<ScreensaverUser> findDataSharingLevelAdminUsers()
  {
    String hql = "from ScreensaverUser where ? in elements (screensaverUserRoles)" ;
    return Sets.newHashSet(_dao.findEntitiesByHql(ScreensaverUser.class, hql, ScreensaverUserRole.SCREEN_DATA_SHARING_LEVELS_ADMIN.getRoleName()));
  }
}
