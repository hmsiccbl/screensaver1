// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.util.Date;

import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUserClassification;

import org.apache.log4j.Logger;

public class MakeDummyEntities
{
  // static members

  private static Logger log = Logger.getLogger(MakeDummyEntities.class);

  public static ScreeningRoomUser makeDummyUser(int screenNumber, String first, String last)
  {
    return new ScreeningRoomUser(new Date(),
                                 first,
                                 last + "_" + screenNumber,
                                 first.toLowerCase() + "_" + last.toLowerCase() + "_" + screenNumber + "@hms.harvard.edu",
                                 "",
                                 "",
                                 "",
                                 "",
                                 "",
                                 ScreeningRoomUserClassification.ICCBL_NSRB_STAFF,
                                 true);
  }

  public static Screen makeDummyScreen(int screenNumber, ScreenType screenType)
  {
    ScreeningRoomUser labHead = makeDummyUser(screenNumber, "Lab", "Head");
    ScreeningRoomUser leadScreener = makeDummyUser(screenNumber, "Lead", "Screener");
    Screen screen = new Screen(labHead,
                               leadScreener,
                               screenNumber,
                               new Date(),
                               screenType,
                               "Dummy screen");
    return screen;
  }

  public static Screen makeDummyScreen(int screenNumber)
  {
    return makeDummyScreen(screenNumber, ScreenType.SMALL_MOLECULE);
  }


  // instance data members

  // public constructors and methods

  // private methods

}

