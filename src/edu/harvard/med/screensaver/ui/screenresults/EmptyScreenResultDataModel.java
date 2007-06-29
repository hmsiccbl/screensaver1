// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.ui.searchresults.SortDirection;

import org.apache.log4j.Logger;

public class EmptyScreenResultDataModel extends ScreenResultDataModel
{
  // static members

  private static final List<Map<String,String>> EMPTY_RESULT = new ArrayList<Map<String,String>>();
  private static final Map<String,String> EMTPY_ROW = new HashMap<String,String>(0);
  private static Logger log = Logger.getLogger(EmptyScreenResultDataModel.class);

  public EmptyScreenResultDataModel()
  {
    super(null, null, null, null);
  }
    
  @Override
  public int getRowCount()
  {
    return 0;
  }

  @Override
  public Map<String,String> getRowData()
  {
    return EMTPY_ROW;
  }

  @Override
  public int getRowIndex()
  {
    return 0;
  }

  @Override
  public List<Map<String,String>> getWrappedData()
  {
    return EMPTY_RESULT;
  }

  @Override
  public boolean isResultValueCellExcluded()
  {
    return false;
  }

  @Override
  public boolean isRowAvailable()
  {
    return false;
  }

  @Override
  public void setRowIndex(int rowIndex)
  {
  }

  @Override
  protected Map<WellKey,List<ResultValue>> fetchData(List<ResultValueType> selectedResultValueTypes,
                                                   int sortBy,
                                                   SortDirection sortDirection)
  {
    return null;
  }

//  @Override
//  public void sort(String sortColumnName, SortDirection sortDirection)
//  {
//  }
}
