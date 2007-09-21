// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.util.List;

import javax.faces.model.ListDataModel;

import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;
import edu.harvard.med.screensaver.ui.namevaluetable.NameValueTable;
import edu.harvard.med.screensaver.ui.namevaluetable.ValueType;
import edu.harvard.med.screensaver.ui.util.HtmlUtils;

import org.apache.log4j.Logger;

public class AnnotationNameValueTable extends NameValueTable
{
  // static members

  private static Logger log = Logger.getLogger(AnnotationNameValueTable.class);

  private List<AnnotationValue> _annotationValues;

  public AnnotationNameValueTable(List<AnnotationValue> annotationValues)
  {
    _annotationValues = annotationValues;
    setDataModel(new ListDataModel(_annotationValues));
  }

  @Override
  public String getAction(int index, String value)
  {
    return null;
  }

  @Override
  public String getDescription(int index)
  {
    return _annotationValues.get(index).getAnnotationType().getDescription();
  }

  @Override
  public String getLink(int index, String value)
  {
    return null;
  }

  @Override
  public String getName(int index)
  {
    return HtmlUtils.toNonBreakingSpaces(_annotationValues.get(index).getAnnotationType().getName());
  }

  @Override
  public int getNumRows()
  {
    return _annotationValues.size();
  }

  @Override
  public Object getValue(int index)
  {
    return _annotationValues.get(index).getFormattedValue();
  }

  @Override
  public ValueType getValueType(int index)
  {
    return ValueType.TEXT;
  }

  // instance data members

  // public constructors and methods

  // private methods

}

