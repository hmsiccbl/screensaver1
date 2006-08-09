// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

public class QueryController extends AbstractController
{
  private String _stockPlateNumber;
  private String _wellName;

  public String getStockPlateNumber()
  {
    return _stockPlateNumber;
  }

  public void setStockPlateNumber(String stockPlateNumber)
  {
    _stockPlateNumber = stockPlateNumber;
  }

  public String getWellName()
  {
    return _wellName;
  }

  public void setWellName(String WellName)
  {
    _wellName = WellName;
  }
}
