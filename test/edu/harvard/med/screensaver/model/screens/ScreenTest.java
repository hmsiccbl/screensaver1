// $HeadURL:
// svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/test/edu/harvard/med/screensaver/TestHibernate.java
// $
// $Id: ComplexDAOTest.java 1110 2007-03-02 23:06:48Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.io.screenresults.MockDaoForScreenResultImporter;
import edu.harvard.med.screensaver.util.DateUtil;

import org.apache.log4j.Logger;


/**
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class ScreenTest extends AbstractSpringTest
{
  
  private static final Logger log = Logger.getLogger(ScreenTest.class);
  
  // public static methods
  
  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(ScreenTest.class);
  }

  
  
  // JUnit test methods

  public void testGetScreeningRoomActivities() throws Exception
  {
    Screen screen = MockDaoForScreenResultImporter.makeDummyScreen(1);
    LibraryScreening screening1 = new LibraryScreening(screen,
                                                       screen.getLeadScreener(),
                                                       DateUtil.makeDate(2007,
                                                         1,
                                                         1),
                                                       DateUtil.makeDate(2007,
                                                         3,
                                                         7),
                                                       AssayProtocolType.ESTABLISHED);
    LibraryScreening screening2 = new LibraryScreening(screen,
                                                       screen.getLeadScreener(),
                                                       DateUtil.makeDate(2007,
                                                         1,
                                                         1),
                                                       DateUtil.makeDate(2007,
                                                                         3,
                                                                         8),
                                                       AssayProtocolType.ESTABLISHED);
    CherryPickRequest cpr = new CompoundCherryPickRequest(screen,
                                                          screen.getLeadScreener(),
                                                          DateUtil.makeDate(2007,
                                                                            3,
                                                                            9));
    CherryPickLiquidTransfer cplt = new CherryPickLiquidTransfer(MockDaoForScreenResultImporter.makeDummyUser(1,
                                                                                                              "Lab",
                                                                                                              "Guy"),
                                                                 DateUtil.makeDate(2007,
                                                                   1,
                                                                   1),
                                                                 new Date(),
                                                                 new BigDecimal(6.5),
                                                                 cpr);

    Set<ScreeningRoomActivity> activities = screen.getScreeningRoomActivitiesOfType(LibraryScreening.class);
    assertEquals("library screening activities",
                 new TreeSet<ScreeningRoomActivity>(Arrays.asList(screening1,
                                                                  screening2)),
                 activities);

    activities = screen.getScreeningRoomActivitiesOfType(CherryPickLiquidTransfer.class);
    assertEquals("cherry pick liquid transfer activities",
               new TreeSet<ScreeningRoomActivity>(Arrays.asList(cplt)),
               activities);

    activities = screen.getScreeningRoomActivitiesOfType(ScreeningRoomActivity.class);
    assertEquals("cherry pick liquid transfer activities",
                 new TreeSet<ScreeningRoomActivity>(Arrays.asList(screening1, screening2, cplt)),
                 activities);
  }

}

