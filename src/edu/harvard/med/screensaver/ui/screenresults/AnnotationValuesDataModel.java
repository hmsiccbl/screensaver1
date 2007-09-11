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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.model.DataModel;

import edu.harvard.med.screensaver.db.AnnotationsDAO;
import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;

import org.apache.log4j.Logger;

public class AnnotationValuesDataModel extends DataModel
{
  // static members

  private static Logger log = Logger.getLogger(AnnotationValuesDataModel.class);


  // instance data members

  private AnnotationsDAO _annotationsDao;
  private List<AnnotationType> _annotationTypes;
  private int _sortBy;
  private SortDirection _sortDirection;
  private int _rowCount;
  private List<Map<String,Object>> _data;
  private int _rowIndex;


  // public constructors and methods

  public AnnotationValuesDataModel(List<AnnotationType> annotationTypes,
                                   int sortBy,
                                   SortDirection sortDirection,
                                   AnnotationsDAO annotationsDao)
  {
    _sortBy = sortBy;
    _sortDirection = sortDirection;
    _annotationTypes = annotationTypes;
    _annotationsDao = annotationsDao;
  }

  @Override
  public int getRowCount()
  {
    if (_data == null) {
      lazyBuildData();
    }
    return _rowCount;
  }

  @Override
  public void setRowIndex(int rowIndex)
  {
    _rowIndex = rowIndex;
  }

  @Override
  public int getRowIndex()
  {
    return _rowIndex;
  }

  @Override
  public boolean isRowAvailable()
  {
    return _rowIndex < _rowCount && _rowIndex >= 0;
  }

  @Override
  public Map<String,Object> getRowData()
  {
    if (_data == null) {
      lazyBuildData();
    }

    return _data.get(_rowIndex);
  }

  @Override
  public void setWrappedData(Object data)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object getWrappedData()
  {
    throw new UnsupportedOperationException();
  }


  // private methods

  private void lazyBuildData()
  {
    _data = new ArrayList<Map<String,Object>>();
    Map<String,List<AnnotationValue>> data =
      _annotationsDao.findSortedAnnotationValuesTableByRange(_annotationTypes,
                                                             _sortBy,
                                                             _sortDirection,
                                                             0,
                                                             null,
                                                             null);

    for (String vendorId : data.keySet()) {
      Map<String,Object> row = new HashMap<String,Object>();
      // add fixed key column
      row.put(AnnotationValuesTable.VENDOR_ID_COLUMN_NAME, vendorId);
      // add variable annotation type columns
      Iterator<AnnotationValue> colIter = data.get(vendorId).iterator();
      for (AnnotationType annotationType : _annotationTypes) {
        row.put(annotationType.getName(), colIter.next().getFormattedValue());
      }
      _data.add(row);
      ++_rowCount;
    }
  }
}
