// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.model.SelectItem;

import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.ui.util.JSFUtils;

import org.apache.log4j.Logger;

public class HeatMapViewerController extends AbstractController
{

  // static data members

  private static Logger log = Logger.getLogger(ScreenResultViewerController.class);
  
  
  // instance data members

  private ScreenResult _screenResult;
  private Integer _dataHeaderIndex;
  private List<String> _plateNumbers;
  private UniqueDataHeaderNames _uniqueDataHeaderNames;

  
  // bean property methods
  
  public void setScreenResult(ScreenResult screenResult)
  {
    if (_screenResult != screenResult) {
      resetView();
    }
    _screenResult = screenResult;
  }
  
  public ScreenResult getScreenResult()
  {
    return _screenResult;
  }
  
  public void setPlateNumbers(List<String> plateNumbers)
  {
    log.debug("setPlateNumbers() called");
    _plateNumbers = plateNumbers;
  }
  
  public List<String> getPlateNumbers()
  {
    log.debug("getPlateNumbers() called");
    if (_plateNumbers == null) {
      selectAllPlates();
    }
    return _plateNumbers;
  }
  
  public void setDataHeaderIndex(Integer dataHeaderIndex)
  {
    _dataHeaderIndex = dataHeaderIndex;
  }
  
  public Integer getDataHeaderIndex()
  {
    return _dataHeaderIndex;
  }

  public List<SelectItem> getPlateSelectItems()
  {
    return JSFUtils.createUISelectItems(_screenResult.getDerivedPlateNumbers());
  }
  
  public List<SelectItem> getDataHeaderSelectItems()
  {
    if (_uniqueDataHeaderNames == null) {
      _uniqueDataHeaderNames = new UniqueDataHeaderNames(_screenResult);
      // TODO: only show the ResultValueTypes that have numeric data (and raw only?)
    }
    return JSFUtils.createUISelectItemsWithIndexValues(_uniqueDataHeaderNames.asList());
  }
  

  // JSF application methods

  public String update()
  {
    return null; // redisplay
  }
  
  public String showAllPlates()
  {
    log.debug("handling showAllPlates command");
    selectAllPlates();
    return null;
  }
  
  public String done()
  {
    return "done";
  }

  
  // private methods
  
  private void resetView()
  {
    _dataHeaderIndex = null;
    _plateNumbers = null;
    _uniqueDataHeaderNames = null;
  }
  
  private void selectAllPlates()
  {
    _plateNumbers = new ArrayList<String>();
    for (Integer plateNumber : _screenResult.getDerivedPlateNumbers()) {
      _plateNumbers.add(plateNumber.toString());
    }
  }
  
  
}
