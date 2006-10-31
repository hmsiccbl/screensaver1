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

import org.apache.log4j.Logger;

public class UISelectOneBean<T> extends UISelectBean<T>
{
  // static members

  private static Logger log = Logger.getLogger(UISelectOneBean.class);


  // instance data members
  
  private T _selection;
  private String _selectionKey;

  
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
   * called by JSF UISelect component
   */
  public void setValue(String selectionKey)
  {
    _selectionKey = selectionKey;
    _selection = _key2Obj.get(selectionKey);
  }
  
  /**
   * called by JSF UISelect component
   */
  public String getValue()
  {
    return _selectionKey;
  }

  /**
   * called by controller 
   * @param selections
   */
  public T getSelection()
  {
    return _selection;
  }

}

