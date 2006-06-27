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
import java.util.List;
import java.util.SortedSet;

import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.util.DevelopmentException;

import org.apache.log4j.Logger;

// TODO: dynamically add columns for data headers

public class ScreenResultViewerController
{
  private static Logger log = Logger.getLogger(MainController.class);
  
  private static final int DEFAULT_ITEMS_PER_PAGE = 10;
  
  private DAO _dao;
  
  private ScreenResult _screenResult;
  private int itemsPerPage = DEFAULT_ITEMS_PER_PAGE;

  private int _pageIndex;
  
  public ScreenResultViewerController()
  {
  }

  public DAO getDAO()
  {
    return _dao;
  }

  public void setDAO(DAO dao)
  {
    _dao = dao;
  }

  public ScreenResult getScreenResult()
  {
    return _screenResult;
  }

  public void setScreenResult(ScreenResult screenResult)
  {
    _screenResult = screenResult;
  }
  
  public List<ResultValue> getResultValues()
  {
    return new ArrayList<ResultValue>(_screenResult.getResultValueTypes().first().getResultValues());
  }
  
  public int getItemsPerPage()
  {
    return itemsPerPage;
  }

  public void setItemsPerPage(int itemsPerPage)
  {
    this.itemsPerPage = itemsPerPage;
  }


  // JSF application methods
  
  public String gotoPage(int pageIndex)
  {
    try {
      UIData dataTable = getDataTable();
      int firstRow = pageIndex * dataTable.getRows();
      if (firstRow >= 0 &&
        firstRow < _screenResult.getResultValueTypes().first().getResultValues().size()) {
        dataTable.setFirst(firstRow);
        _pageIndex = pageIndex;
      }
      return null;
    } 
    catch (Exception e) {
      return "error";
    }
  }
  
  public String nextPage()
  {
    return gotoPage(_pageIndex + 1); 
  }
  
  public String prevPage()
  {
    return gotoPage(_pageIndex - 1); 
  }
  
  public String done()
  {
    return "done";
  }
  
  private UIData getDataTable() throws DevelopmentException
  {
    UIComponent foundComponent = FacesContext.getCurrentInstance().getViewRoot().findComponent(":rawDataForm:rawDataTable");
    if (foundComponent == null || !(foundComponent instanceof UIData)) {
      String errorMessage = "could not find :rawDataForm:rawDataTable JSF component; foundComponent=" + foundComponent;
      log.error(errorMessage);
      throw new DevelopmentException(errorMessage);
    }
    return (UIData) foundComponent;
  }    

}
