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
import edu.harvard.med.screensaver.model.cherrypicks.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.RNAiCherryPickScreening;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.Screening;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.springframework.transaction.annotation.Propagation;
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
  
  @Transactional(propagation=Propagation.MANDATORY)
  public LibraryScreening addLibraryScreening(Screen screen)
  {
    SortedSet<LibraryScreening> activities = screen.getLabActivitiesOfType(LibraryScreening.class);
    Screening lastScreening = null;
    if (!activities.isEmpty()) {
      lastScreening = activities.last();
    }
    LibraryScreening newScreening = screen.createLibraryScreening(screen.getLeadScreener(), new LocalDate());
    if (lastScreening != null) { 
      copyScreeningProperties(lastScreening, newScreening);
    }
    return newScreening;
  }
  
  @Transactional(propagation=Propagation.MANDATORY)
  public RNAiCherryPickScreening addRnaiCherryPickScreening(Screen screen, RNAiCherryPickRequest cpr)
  {
    SortedSet<RNAiCherryPickScreening> activities = screen.getLabActivitiesOfType(RNAiCherryPickScreening.class);
    RNAiCherryPickScreening lastScreening = null;
    if (!activities.isEmpty()) {
      lastScreening = activities.last();
    }
    RNAiCherryPickScreening newScreening = screen.createRNAiCherryPickScreening(cpr.getRequestedBy(),
                                                                                new LocalDate(),
                                                                                cpr);
    
    if (lastScreening != null) {
      copyScreeningProperties(lastScreening, newScreening);
    }
    return newScreening;
  }
  
  private void copyScreeningProperties(Screening fromScreening, Screening toScreening)
  {
    toScreening.setAssayProtocol(fromScreening.getAssayProtocol());
    toScreening.setAssayProtocolLastModifiedDate(fromScreening.getAssayProtocolLastModifiedDate());
    toScreening.setAssayProtocolType(fromScreening.getAssayProtocolType());
    toScreening.setNumberOfReplicates(fromScreening.getNumberOfReplicates());
    toScreening.setVolumeTransferredPerWell(fromScreening.getVolumeTransferredPerWell());
    toScreening.setConcentration(fromScreening.getConcentration());
    toScreening.setPerformedBy(fromScreening.getPerformedBy());
  }

}
