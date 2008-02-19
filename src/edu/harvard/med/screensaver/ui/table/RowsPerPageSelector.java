// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/search-result-redesign/src/edu/harvard/med/screensaver/ui/table/RowsPerPageSelector.java $
// $Id: RowsPerPageSelector.java 2152 2008-02-07 16:34:46Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import edu.harvard.med.screensaver.ui.util.UISelectOneBean;

import org.apache.log4j.Logger;

public class RowsPerPageSelector extends UISelectOneBean<Integer>
{
  // static members

  public static final Integer SHOW_ALL_VALUE = -1;
  private static Logger log = Logger.getLogger(RowsPerPageSelector.class);

  // instance data members

  public RowsPerPageSelector(List<Integer> values)
  {
    super(values);
  }

  public RowsPerPageSelector(List<Integer> values, Integer defaultSelection)
  {
    super(values, defaultSelection);
  }

  // public constructors and methods

  @Override
  public String getLabel(Integer value)
  {
    if (SHOW_ALL_VALUE.equals(value)) {
      return "All";
    }
    return super.getLabel(value);
  }

  @Override
  public Integer getSelection()
  {
    if (SHOW_ALL_VALUE.equals(super.getSelection())) {
      return getAllRowsValue();
    }
    return super.getSelection();
  }

  @Override
  public List<SelectItem> getSelectItems()
  {
    List<SelectItem> selectItems = new ArrayList<SelectItem>(super.getSelectItems());
//    if (getAllRowsValue() == null &&
//      selectItems.size() > 0 && SHOW_ALL_VALUE.equals(selectItems.get(selectItems.size() - 1))) {
//      selectItems.remove(selectItems.size() - 1);
//    }
    return selectItems;
  }


  // protected methods

  /**
   * Subclasses should override this method to return the actual value returned
   * by getSelection() when the user selects the "all" value. Overriding is only
   * necessary if the selection values include the SHOW_ALL_VALUE value.
   */
  protected Integer getAllRowsValue()
  {
    return null;
  }

}

