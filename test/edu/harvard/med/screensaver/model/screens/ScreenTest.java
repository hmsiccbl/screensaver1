// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import java.beans.IntrospectionException;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import edu.harvard.med.screensaver.io.screenresults.MockDaoForScreenResultImporter;
import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.util.DateUtil;

import org.apache.log4j.Logger;

public class ScreenTest extends AbstractEntityInstanceTest
{
  // static members

  private static Logger log = Logger.getLogger(ScreenTest.class);


  // instance data members

  
  // public constructors and methods

  public ScreenTest() throws IntrospectionException
  {
    super(Screen.class);
  }
  

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
                                                         7));
    LibraryScreening screening2 = new LibraryScreening(screen,
                                                       screen.getLeadScreener(),
                                                       DateUtil.makeDate(2007,
                                                         1,
                                                         1),
                                                       DateUtil.makeDate(2007,
                                                                         3,
                                                                         8));
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
                                                                 cpr);

    Set<LibraryScreening> libraryScreenings =
      screen.getScreeningRoomActivitiesOfType(LibraryScreening.class);
    assertEquals("library screening activities",
                 new TreeSet<LibraryScreening>(Arrays.asList(screening1, screening2)),
                 libraryScreenings);

    Set<CherryPickLiquidTransfer> cherryPickLiquidTransfers =
      screen.getScreeningRoomActivitiesOfType(CherryPickLiquidTransfer.class);
    assertEquals("cherry pick liquid transfer activities",
               new TreeSet<CherryPickLiquidTransfer>(Arrays.asList(cplt)),
               cherryPickLiquidTransfers);

    Set<ScreeningRoomActivity> activities =
      screen.getScreeningRoomActivitiesOfType(ScreeningRoomActivity.class);
    assertEquals("cherry pick liquid transfer activities",
                 new TreeSet<ScreeningRoomActivity>(Arrays.asList(screening1, screening2, cplt)),
                 activities);
  }
}

