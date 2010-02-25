// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.screens;

import java.util.SortedSet;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickLiquidTransfer;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickLiquidTransferStatus;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.screens.CherryPickScreening;
import edu.harvard.med.screensaver.model.screens.LabActivity;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.Screening;
import edu.harvard.med.screensaver.model.users.AdministratorUser;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.joda.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adds a new {@link Screening} to a {@link Screen}, copying the following properties from a
 * the most recent {@link Screening} of the same type, if it exists:
 * <ul>
 * <li>assayProtocol</li>
 * <li>assayProtocolLastModifiedDate</li>
 * <li>assayProtocolType</li>
 * <li>numberOfReplicates</li>
 * <li>volumeTransferredPerWell</li>
 * <li>concentration</li>
 * <li>performedBy</li>
 * </ul>
 * 
 * @author atolopko
 */
public class ScreeningDuplicator
{
  private static Logger log = Logger.getLogger(ScreeningDuplicator.class);
  
  private GenericEntityDAO _dao;
  
  /**
   * @motivation for CGLIB2
   */
  protected ScreeningDuplicator()
  {
  }
  
  public ScreeningDuplicator(GenericEntityDAO dao)
  {
    _dao = dao;
  }
  
  @Transactional
  public LibraryScreening addLibraryScreening(Screen screen, AdministratorUser recordedBy)
  {
    screen = _dao.reloadEntity(screen);
    SortedSet<LibraryScreening> activities = screen.getLabActivitiesOfType(LibraryScreening.class);
    Screening lastScreening = null;
    if (!activities.isEmpty()) {
      lastScreening = activities.last();
    }
    LibraryScreening newScreening = screen.createLibraryScreening(recordedBy, screen.getLeadScreener(), new LocalDate());
    if (lastScreening != null) { 
      copyActivityProperties(lastScreening, newScreening);
    }
    return newScreening;
  }
  
  @Transactional
  // TODO: can we only pass in cpr
  public CherryPickScreening addCherryPickScreening(Screen screen, CherryPickRequest cpr, AdministratorUser recordedBy)
  {
    screen = _dao.reloadEntity(screen);
    cpr = _dao.reloadEntity(cpr, true, CherryPickRequest.requestedBy.getPath());
    SortedSet<CherryPickScreening> activities = screen.getLabActivitiesOfType(CherryPickScreening.class);
    CherryPickScreening lastScreening = null;
    if (!activities.isEmpty()) {
      lastScreening = activities.last();
    }
    CherryPickScreening newScreening = screen.createCherryPickScreening(recordedBy,
                                                                        cpr.getRequestedBy(),
                                                                        new LocalDate(),
                                                                        cpr);
    
    if (lastScreening != null) {
      copyActivityProperties(lastScreening, newScreening);
    }
    return newScreening;
  }
  
  @Transactional
  // TODO: can we only pass in cpr
  public CherryPickLiquidTransfer addCherryPickLiquidTransfer(Screen screen, 
                                                              CherryPickRequest cpr, 
                                                              AdministratorUser recordedBy, 
                                                              CherryPickLiquidTransferStatus status)
  {
    screen = _dao.reloadEntity(screen);
    cpr = _dao.reloadEntity(cpr, true, CherryPickRequest.requestedBy.getPath());
    SortedSet<CherryPickLiquidTransfer> activities = screen.getLabActivitiesOfType(CherryPickLiquidTransfer.class);
    CherryPickLiquidTransfer lastCplt = null;
    if (!activities.isEmpty()) {
      lastCplt = activities.last();
    }
    CherryPickLiquidTransfer newCplt = screen.createCherryPickLiquidTransfer(recordedBy,
                                                                             cpr.getRequestedBy(),
                                                                             new LocalDate(),
                                                                             status);
    
    if (lastCplt != null) {
      copyActivityProperties(lastCplt, newCplt);
    }
    return newCplt;
  }
  
  private void copyActivityProperties(Screening fromScreening, Screening toScreening)
  {
    copyActivityProperties((LabActivity) fromScreening, (LabActivity) toScreening);
    toScreening.setAssayProtocol(fromScreening.getAssayProtocol());
    toScreening.setAssayProtocolLastModifiedDate(fromScreening.getAssayProtocolLastModifiedDate());
    toScreening.setAssayProtocolType(fromScreening.getAssayProtocolType());
    toScreening.setNumberOfReplicates(fromScreening.getNumberOfReplicates());
  }

  private void copyActivityProperties(LabActivity fromActivity, LabActivity toActivity)
  {
    toActivity.setVolumeTransferredPerWell(fromActivity.getVolumeTransferredPerWell());
    toActivity.setConcentration(fromActivity.getConcentration());
    toActivity.setPerformedBy(fromActivity.getPerformedBy());
  }

}
