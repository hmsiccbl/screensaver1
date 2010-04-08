// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import edu.harvard.med.screensaver.model.VocabularyTerm;
import edu.harvard.med.screensaver.model.VocabularyUserType;

public enum ScreenDataSharingLevel implements VocabularyTerm 
{
  SHARED("Shared"), 
  MUTUAL_SCREENS("Level 1 (Mutual Screens)"), 
  MUTUAL_POSITIVES("Level 2 (Mutual Positives)"), 
  PRIVATE("Level 3 (Private)");
  
  public static class UserType extends VocabularyUserType<ScreenDataSharingLevel>
  {
    public UserType()
    {
      super(ScreenDataSharingLevel.values());
    }
  }

  private String _value;
  
  private ScreenDataSharingLevel(String value)
  {
    _value = value;
  }
  
  public String getValue()
  {
    return _value; 
  }

  @Override
  public String toString()
  {
    return getValue();
  }
}
