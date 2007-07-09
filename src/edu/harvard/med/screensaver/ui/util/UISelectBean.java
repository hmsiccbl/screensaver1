// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;

/**
 * @motivation Guaranteed type consistency between set/get methods and
 *             getSelectItems() method.
 * @motivation JSF converters appear to serialize the objects in from its
 *             associated SeletItems. This is bad if the object is not
 *             serializable or very large (e.g., a data model entity). This is
 *             also bad if the object is a persisted Hibernate entity, which may
 *             become invalid if the Hibernate session is closed, and the entity
 *             has uninitialized lazy persistent sets that need to be acc
 * @motivation JSF converter definitions are a pain in the ass
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public abstract class UISelectBean<T> extends Observable
{
  // static members

  private static Logger log = Logger.getLogger(UISelectBean.class);


  // instance data members

  List<SelectItem> _selectItems;
  protected Map<String,T> _key2Obj;

  
  // public constructors and methods

  public UISelectBean(Collection<T> objects)
  {
    _selectItems = new ArrayList<SelectItem>();
    _key2Obj = new HashMap<String,T>();
    for (T t : objects) {
      String key = getKey(t);
      String label = getLabel(t);
      if (label == null) {
        log.warn("null label returned for select item of object " + t);
        label = t.toString();
      }
      _selectItems.add(new SelectItem(key, label));
      _key2Obj.put(key, t);
    }
  }
  
  /**
   * called by JSF UISelect component
   */
  public List<SelectItem> getSelectItems()
  {
    return _selectItems;
  }
  
  /**
   * @motivation JSF EL does not have a size or length operator
   * @return the total number of SelectItems (<i>not</i> the number of user-selected items)
   */
  public int getSize()
  {
    return _selectItems.size();
  }

  
  // protected methods
  
  protected String getKey(T t)
  {
    if (t == null) {
      return "";
    }
    return Integer.toString(t.hashCode());
  }
  
  protected String getLabel(T t)
  {
    if (t == null) { 
      return "<none>";
    }
    return t.toString();
  }

}

