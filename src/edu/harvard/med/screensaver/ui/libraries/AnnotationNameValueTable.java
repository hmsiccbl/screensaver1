// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;
import edu.harvard.med.screensaver.ui.namevaluetable.NameValueTable;
import edu.harvard.med.screensaver.ui.namevaluetable.ValueType;
import edu.harvard.med.screensaver.ui.table.SimpleCell;

public class AnnotationNameValueTable extends NameValueTable
{
  // static members

  private static Logger log = Logger.getLogger(AnnotationNameValueTable.class);

  private List<SimpleCell> _rows;  // Study#, name, value, description

  private Map<Integer,List<SimpleCell>> _studyNumberToStudyInfoMap; // Study# -> List[name, value]
  private String _footerInfo;
  
  /**
   * Constructs a AnnotationNameValueTable object.
   * 
   * @param annotationValues
   * @param studyNumberToStudyInfoMap Study# -> List[name, value]
   */
  public AnnotationNameValueTable(List<AnnotationValue> annotationValues,
                                  Map<Integer,List<SimpleCell>> studyNumberToStudyInfoMap, String footerInfo)
  {
    _studyNumberToStudyInfoMap = studyNumberToStudyInfoMap;
    _footerInfo = footerInfo; 
    
    Collections.sort(
        annotationValues, 
        new Comparator<AnnotationValue>()
        {
           public int compare(AnnotationValue o1,
                              AnnotationValue o2)
           {
             return o1.getAnnotationType().getStudy().getStudyNumber()
                 .compareTo(o2.getAnnotationType().getStudy().getStudyNumber());
           }
        });
    
    _rows = new ArrayList<SimpleCell>(annotationValues.size());
    for(AnnotationValue value: annotationValues)
    {
      _rows.add(
               new SimpleCell(
                 value.getAnnotationType().getName(),
                 value.getFormattedValue(),
                 value.getAnnotationType().getDescription())
               .setGroupId(value.getAnnotationType().getStudy().getStudyNumber()));
    }
    
    setDataModel(new ListDataModel(_rows));
  }
  
  @Override
  public Object getGroupId()
  {
    return "" + _rows.get(getRowIndex()).getGroupId(); 
  }
  
  @Override
  public DataModel getGroupingValueTable()
  {
    return new ListDataModel(_studyNumberToStudyInfoMap.get(_rows.get(getRowIndex()).getGroupId()));
  }
  
  public String getGroupingFooter()
  {
    return _footerInfo;
  }
  
  @Override
  public boolean getHasGroupingInfo()
  {
    return true;
  }

  @Override
  public ValueType getValueType(int index)
  {
    return ValueType.TEXT;
  }

  @Override
  public String getDescription(int index)
  {
    return _rows.get(index).getDescription();
  }
  
  @Override
  public String getName(int index)
  {
    return _rows.get(index).getTitle();
  }
  
  /////////////////////////////////////////////
  //// support for the NameValueTable: (JSF could use the DataModel, not this object)
  /////////////////////////////////////////////
  
  @Override
  public String getAction(int index, String value)
  {
    return null;
  }

  @Override
  public String getLink(int index, String value)
  {
    return null;
  }

  @Override
  public int getNumRows()
  {
    return _rows.size();
  }

  /**
   * Note: this method is only used if the NameValueTable is being queried directly from the jsp; that is, 
   * if the datatable is not iterating over the TableModel, but rather, and index is being used.
   */
  @Override
  public Object getValue(int index)
  {
    return null;
  }
  
}

