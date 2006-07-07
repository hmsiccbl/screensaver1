// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.application.Application;
import javax.faces.component.UIColumn;
import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;

public class JSFUtils
{
  private static Logger log = Logger.getLogger(JSFUtils.class);

  /**
   * Dynamically add a column to a JSF UIData (table) component.
   * 
   * @param facesCtx
   * @param table
   * @param columnId the ID to assign to the JSF UIColumn component (corresponds
   *          to the "id" attribute when defining via XML)
   * @param columnDisplayName the name to be displayed in the column header
   * @param bindingExpression an EL deferred expression specifying the property
   *          that the column's values will be bound to. For example,
   *          "${rowBean.aColumnValue}", where "rowBean" is the value of the
   *          UIData component's "var" property, and "aColumnValue" is a
   *          property of "rowBean".
   */
  @SuppressWarnings("unchecked")
  public static void addTableColumn(
    FacesContext facesCtx,
    UIData table,
    String columnId,
    String columnDisplayName,
    String bindingExpression)
  {
    Application facesAppl = facesCtx.getApplication();
    UIColumn newColumn = (UIColumn) facesAppl.createComponent("javax.faces.Column");
    newColumn.setId(columnId);
    newColumn.setParent(table);
    table.getChildren().add(newColumn);

    UIOutput newColumnOutputText = (UIOutput) facesAppl.createComponent("javax.faces.HtmlOutputText");
    newColumnOutputText.setParent(newColumn);
    newColumn.getChildren().add(newColumnOutputText);
    ValueBinding valueBinding = FacesContext.getCurrentInstance()
                                            .getApplication()
                                            .createValueBinding(bindingExpression);
    newColumnOutputText.setValueBinding("value", valueBinding);
    
    UIOutput facetOutputText = (UIOutput) facesAppl.createComponent("javax.faces.HtmlOutputText");
    facetOutputText.setValue(columnDisplayName);
    facetOutputText.setParent(newColumn);
    newColumn.getFacets().put("header",
                              facetOutputText);

    log.debug("add new UIColumn '" + newColumn.getId() + "' to table '" + table.getId() + "' with header '" + facetOutputText.getValue().toString() + "'");
  }
  
  /**
   * Creates a UISelectItems object that can be assigned to the "value"
   * attribute of a UISelectItems JSF component.
   * 
   * @param values
   * @return
   */
  public static List<SelectItem> createUISelectItems(Collection values)
  {
    List<SelectItem> result = new ArrayList<SelectItem>();
    for (Object value : values) {
      result.add(new SelectItem(value.toString(), 
                                value.toString()));
    }
    return result;
  }
  
  /**
   * Creates a UISelectItems object that can be assigned to the "value"
   * attribute of a UISelectItems JSF component.
   * 
   * @param values
   * @return
   */
  public static List<SelectItem> createUISelectItems(Map values)
  {
    List<SelectItem> result = new ArrayList<SelectItem>();
    for (Iterator iter = values.entrySet().iterator(); iter.hasNext();) {
      Map.Entry entry = (Map.Entry) iter.next();
      result.add(new SelectItem(entry.getValue(),
                                entry.getKey().toString()));
    }
    return result;
  }
  
  /**
   * Output the names of all registered JSF components to the logger, as debug output.
   * 
   * @motivation allows developer to determine the valid set of JSF component
   *             names that can be passed to Application.createComponent();
   * @param component
   * @param level
   */
  private void logComponentNames()
  {
    log.debug("JSF registered component names:");
    Iterator iter = FacesContext.getCurrentInstance().getApplication().getComponentTypes();
    while (iter.hasNext()) {
      log.debug("component name: " + iter.next().toString());
    }
  }
                             
  /**
   * Output the hierarchical structure of a JSF component with nested children.
   */
  private void logComponentStructure(UIComponent component, int level)
  {
    char[] indent = new char[level];
    Arrays.fill(indent, ' ');
    log.debug(new String(indent) + component.getClass() + ": " + component.getId());
    for (Iterator iter = component.getChildren().iterator(); iter.hasNext();) {
      UIComponent child = (UIComponent) iter.next();
      logComponentStructure(child, level + 1);
    }
  }

  
}
