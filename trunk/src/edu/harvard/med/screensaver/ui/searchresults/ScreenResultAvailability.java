// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

public enum ScreenResultAvailability {
  AVAILABLE("available"),
  NOT_SHARED("not shared"),
  NONE("none");

  private String _text;

  ScreenResultAvailability(String text)
  {
    _text = text;
  }

  public String getText() { return _text; }

  @Override
  public String toString()
  {
    return _text;
  }
}
