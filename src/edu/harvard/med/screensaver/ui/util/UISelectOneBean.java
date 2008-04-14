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
  private T _defaultSelection;


  // public constructors and methods

  public UISelectOneBean(Collection<T> objects)
  {
    super(objects);
  }
  
  @Override
  public void setDomain(Collection<T> objects)
  {
    super.setDomain(objects);
    // set or restore selection
    if (objects.size() == 0) {
      _selection = null;
      _selectionKey = null;
      _selectionIndex = 0;
    }
    else {
      _defaultSelection = objects.iterator().next();
      if (_selectionKey == null || !_key2Obj.containsKey(_selectionKey)) {
        setSelection(_defaultSelection);
      }
    }
  }

  public UISelectOneBean(Collection<T> objects, T defaultSelection)
  {
    this(objects);
    _defaultSelection = defaultSelection;
    setSelection(_defaultSelection);
  }

  public UISelectOneBean()
  {
    this(new ArrayList<T>());
  }

  /**
   * Set the selected item's key. Called by JSF UISelect component. Naming of
   * this method corresponds to the JSF UISelect component's "value" attribute.
   * @throws IllegalArgumentException if selectionKey is unknown
   */
  public void setValue(String newSelectionKey)
  {
    if (newSelectionKey != null && !_key2Obj.containsKey(newSelectionKey))
    {
      return;
      //throw new IllegalArgumentException("unknown selection key " + newSelectionKey);
    }

    if ((_selectionKey == null && newSelectionKey != null) || 
      (_selectionKey != null && !_selectionKey.equals(newSelectionKey))) {
      _selectionKey = newSelectionKey;
      _selection = _key2Obj.get(newSelectionKey);
      int i = 0;
      for (SelectItem selectItem : getSelectItems()) {
        if ((selectItem.getValue() == null && newSelectionKey == null) ||
          selectItem.getValue().equals(newSelectionKey)) {
          _selectionIndex = i;
          break;
        }
        ++i;
      }
      setChanged();
      notifyObservers(_selection);
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
   */
  public T getSelection()
  {
    return _selection;
  }
  
  public T getDefaultSelection()
  {
    return _defaultSelection;
  }

  public void setSelectionIndex(int index)
  {
    setSelection(_key2Obj.get(getSelectItems().get(index).getValue()));
  }

  public int getSelectionIndex()
  {
    return _selectionIndex;
  }

}

