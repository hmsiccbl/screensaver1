// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults;

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

