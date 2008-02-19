// $HeadURL$
// $Id$
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
 * Base class for beans the back UISelectOne and UISelectMany JSF components.
 * Maintains three "views" of each selection:
 * <dl>
 * <dt>value</dt>
 * <dd>A simple value for a SelectItem.value. Becomes the "value" attribute of
 * a rendered "option" HTML element.</dd>
 * <dt>label</dt>
 * <dd>Human-readable label for a SelectItem.label object. Becomes the text of
 * a rendered "option" HTML element.</dd>
 * <dt>selection</dt>
 * <dd>The domain object represented by the SelectItem. (This is the object
 * that a JSF converter would otherwise provide.)
 * </dl>
 * Generates and provides a list of SelectItems that can be bound to a child
 * UISelectItem component's 'value' attribute.
 *
 * @motivation Guaranteed type consistency between set/get methods and
 *             getSelectItems() method.
 * @motivation JSF converters appear to serialize the objects in from its
 *             associated SelectItems. This is bad if the object is not
 *             serializable or very large (e.g., a data model entity). This is
 *             also bad if the object is a persisted Hibernate entity, which may
 *             become invalid if the Hibernate session is closed, and the entity
 *             has uninitialized lazy persistent sets that need to be acc
 * @motivation JSF converter definitions are a pain in the ass
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
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
    setDomain(objects);
  }
  
  /**
   * Set the domain of items that can be selected from. Allows items to be
   * changed after UISelectBean is instantiated.
   */
  public void setDomain(Collection<T> objects)
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

