// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.view.screenresults;

import java.text.DecimalFormat;

public class HackedDecimalFormat extends DecimalFormat
{
  private static final long serialVersionUID = -3035125451589014319L;

  private int _hashCode;
  
  public HackedDecimalFormat(String pattern)
  {
    super(pattern);
    _hashCode = pattern.hashCode();
  }
  
  @Override
  public int hashCode()
  {
    return _hashCode;
  }

}

