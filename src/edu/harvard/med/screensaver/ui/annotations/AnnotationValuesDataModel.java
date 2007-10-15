// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.annotations;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.harvard.med.screensaver.db.AnnotationsDAO;
import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;
import edu.harvard.med.screensaver.ui.table.VirtualPagingDataModel;

import org.apache.log4j.Logger;

public class AnnotationValuesDataModel extends VirtualPagingDataModel<ReagentVendorIdentifier,AnnotationValue>
{
  // static members

  private static Logger log = Logger.getLogger(AnnotationValuesDataModel.class);

  public static final int FIXED_KEY_COLUMNS = 1;


  // instance data members

  private AnnotationsDAO _annotationsDao;
  private List<AnnotationType> _annotationTypes;


  // public constructors and methods

  public AnnotationValuesDataModel(List<AnnotationType> annotationTypes,
                                   int rowsToFetch,
                                   int totalRows,
                                   int sortColumnIndex,
                                   SortDirection sortDirection,
                                   AnnotationsDAO annotationsDao)
  {
    super(rowsToFetch,
          totalRows,
          sortColumnIndex - FIXED_KEY_COLUMNS,
          sortDirection);
    _annotationTypes = annotationTypes;
    _annotationsDao = annotationsDao;
  }


  // abstract method implementations

  @Override
  protected Map<ReagentVendorIdentifier,List<AnnotationValue>> fetchData(int firstRowIndex, int rowsToFetch)
  {
    return _annotationsDao.findSortedAnnotationValuesTableByRange(_annotationTypes,
                                                                  _sortColumnIndex,
                                                                  _sortDirection,
                                                                  firstRowIndex,
                                                                  rowsToFetch,
                                                                  null);
  }


  @Override
  protected Map<String,Object> makeRow(int rowIndex,
                                       ReagentVendorIdentifier rowKey,
                                       List<AnnotationValue> rowValues)
  {
    Map<String,Object> row = new HashMap<String,Object>();
    // add fixed key columns
    row.put(AnnotationValuesTable.VENDOR_REAGENT_ID_COLUMN_NAME, rowKey);
    // add variable annotation type columns
    Iterator<AnnotationValue> colIter = rowValues.iterator();
    for (AnnotationType annotationType : _annotationTypes) {
      row.put(annotationType.getName(), colIter.next().getFormattedValue());
    }
    return row;
  }
}
