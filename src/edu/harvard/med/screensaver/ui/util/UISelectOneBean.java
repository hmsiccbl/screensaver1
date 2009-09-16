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
  /** never null */
  private String _selectionKey = EMPTY_KEY;
  private int _selectionIndex;
  private T _defaultSelection;


  // public constructors and methods

  public UISelectOneBean()
  {
    this(new ArrayList<T>());
  }

  public UISelectOneBean(Collection<T> objects)
  {
    this(objects, false);
  }
  
  public UISelectOneBean(Collection<T> objects, boolean isEmptyValueAllowed)
  {
    this(objects, null, isEmptyValueAllowed);
  }
  
  public UISelectOneBean(Collection<T> objects, T defaultSelection)
  {
    this(objects, defaultSelection, false);
  }
  
  public UISelectOneBean(Collection<T> objects, T defaultSelection, boolean isEmptyValueAllowed)
  {
    super(objects, isEmptyValueAllowed);
    if (defaultSelection == null) {
      if (!_isEmptyValueAllowed) {
        if (objects.size() > 0) {
          defaultSelection = objects.iterator().next(); 
        }
      }
    }
    _defaultSelection = defaultSelection;
    setSelection(_defaultSelection);
  }
  
  @Override
  public void setDomain(Collection<T> objects)
  {
    super.setDomain(objects);
    
    // HACK: deal with fact that _selectionKey is not set until parent
    // constructor returns, although this method is called before that!
    if (_selectionKey == null) {
      _selectionKey = EMPTY_KEY;
    }
    
    // set or restore selection
    if (objects.size() == 0) {
      _selection = null;
      _selectionKey = EMPTY_KEY;
      _selectionIndex = 0;
    }
    else {
      _defaultSelection = objects.iterator().next();
      if (!_key2Obj.containsKey(_selectionKey)) {
        setSelection(_defaultSelection);
      }
    }
  }

  

  /**
   * Change the selection by specifying a new selection key.
   */
  public void setKey(String newSelectionKey)
  {
    if (newSelectionKey == null) {
      newSelectionKey = EMPTY_KEY;
    }

    if (!_key2Obj.containsKey(newSelectionKey)) {
      // unknown selection key! (client code error) recover...
      return;
    }
    if (_selectionKey.equals(newSelectionKey)) {
      return;
    }
      
    _selectionKey = newSelectionKey;
    _selection = _key2Obj.get(newSelectionKey);
    int i = 0;
    for (SelectItem selectItem : getSelectItems()) {
      if (selectItem.getValue().equals(newSelectionKey)) {
        _selectionIndex = i;
        break;
      }
      ++i;
    }
    setChanged();
    notifyObservers(_selection);
  }

  public String getKey()
  {
    return _selectionKey;
  }

  /**
   * Alias for setKey(), to match naming convention of JSF "selectItems" "value"
   * attribute.
   */
  public void setValue(String newSelectionKey)
  {
    setKey(newSelectionKey);
  }

  /**
   * Alias for getKey(), to match naming convention of JSF "selectItems" "value"
   * attribute.
   */
  public String getValue()
  {
    return getKey();
  }

  /**
   * Change the selection by specifying a new selection object.
   */
  public void setSelection(T t)
  {
    setKey(_makeKey(t));
  }

  public T getSelection()
  {
    return _selection;
  }
  
  public T getDefaultSelection()
  {
    return _defaultSelection;
  }

  /**
   * Change the selection by specifying a new selection index.
   */
  public void setSelectionIndex(int index)
  {
    setSelection(_key2Obj.get(getSelectItems().get(index).getValue()));
  }

  public int getSelectionIndex()
  {
    return _selectionIndex;
  }

}
