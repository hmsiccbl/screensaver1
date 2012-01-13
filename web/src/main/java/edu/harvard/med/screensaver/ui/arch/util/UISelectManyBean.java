// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

public class UISelectManyBean<T> extends UISelectBean<T>
{
  // static members

  private static Logger log = Logger.getLogger(UISelectManyBean.class);


  // instance data members
  
  private List<T> _selections;
  private List<String> _selectionKeys;

  
  // public constructors and methods

  public UISelectManyBean(Collection<T> objects)
  {
    this(objects, new ArrayList<T>());
  }

  public UISelectManyBean(Collection<T> objects, Collection<T> defaultSelections)
  {
    this(objects, defaultSelections, false);
  }
  
  public UISelectManyBean(Collection<T> objects, Collection<T> defaultSelections, boolean isEmptyValueAllowed)
  {
    super(objects, isEmptyValueAllowed);
    _selections = new ArrayList<T>();
    setSelections(defaultSelections);
  }
  
  /**
   * called by JSF UISelect component
   */
  public void setValue(List<String> selectionKeys)
  {
    if (_selectionKeys != null && _selectionKeys.equals(selectionKeys)) {
      return;
    }
    _selectionKeys = selectionKeys;
    _selections.clear();
    for (String key : selectionKeys) {
      _selections.add(_key2Obj.get(key));
    }
    setChanged();
    notifyObservers(_selections);
  }
  
  public void setSelections(Collection<T> selections)
  {
    List<String> selectionKeys = new ArrayList<String>();
    for (T t : selections) {
      selectionKeys.add(makeKey(t));
    }
    setValue(selectionKeys);
  }
  
  public List<T> getSelections()
  {
    return _selections;
  }

  public List<String> getValue()
  {
    return _selectionKeys;
  }
  
  // TODO: setSelectionIndexes()
  // TODO: getSelectionIndexes()

}

