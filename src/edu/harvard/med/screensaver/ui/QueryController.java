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
  private String _quickFindStockPlateNumber;
  private String _quickFindWellName;

  public String getQuickFindStockPlateNumber()
  {
    return _quickFindStockPlateNumber;
  }

  public void setQuickFindStockPlateNumber(String quickFindStockPlateNumber)
  {
    _quickFindStockPlateNumber = quickFindStockPlateNumber;
  }

  public String getQuickFindWellName()
  {
    return _quickFindWellName;
  }

  public void setQuickFindWellName(String quickFindWellName)
  {
    _quickFindWellName = quickFindWellName;
  }
}
