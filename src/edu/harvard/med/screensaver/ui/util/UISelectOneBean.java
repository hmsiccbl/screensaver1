// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.util;

import java.util.Collection;

import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;

public class UISelectOneBean<T> extends UISelectBean<T>
{
  // static members

  private static Logger log = Logger.getLogger(UISelectOneBean.class);


  // instance data members
  
  private T _selection;
  private String _selectionKey;
  private int _selectionIndex;

  
  // public constructors and methods

  public UISelectOneBean(Collection<T> objects)
  {
    super(objects);
    // set default selection
    if (objects.size() > 0) {
      setValue(getKey(objects.iterator().next()));
    }
  }

  
  /**
   * Get the selected item's key. Called by JSF UISelect component. Naming of
   * this method corresponds to the JSF UISelect component's "value" attribute.
   */
  public void setValue(String selectionKey)
  {
    _selectionKey = selectionKey;
    _selection = _key2Obj.get(selectionKey);
    
    // TODO: linear search! yuck!
    int i = 0;
    for (SelectItem selectItem : getSelectItems()) {
      if (selectItem.getValue().equals(selectionKey)) {
        _selectionIndex = i;
        break;
      }
      ++i;
    }
  }
  
  /**
   * Returns selection key. Called by JSF UISelect component. Naming of this
   * method corresponds to the JSF UISelect component's "value" attribute.
   */
  public String getValue()
  {
    return _selectionKey;
  }

  public void setSelection(T t)
  {
    setValue(getKey(t));
  }
  
  /**
   * called by controller 
   * @param selections
   */
  public T getSelection()
  {
    return _selection;
  }
  
  public void setSelectionIndex(int index)
  {
    _selectionIndex = index;
    setSelection(_key2Obj.get(getSelectItems().get(index).getValue()));
  }
  
  public int getSelectionIndex()
  {
    return _selectionIndex;
  }

}

