// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import edu.harvard.med.screensaver.db.ReagentsSortQuery.SortByReagentProperty;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.ui.table.TableColumn;

import org.apache.log4j.Logger;

public abstract class ReagentColumn<T> extends TableColumn<Reagent,T>
{
  // static members

  private static Logger log = Logger.getLogger(ReagentColumn.class);


  // instance data members

  private SortByReagentProperty _reagentProperty;

  // public constructors and methods

  public ReagentColumn(SortByReagentProperty reagentProperty,
                       String name,
                       String description,
                       boolean isNumeric)
  {
    super(name,
          description,
          reagentProperty == SortByReagentProperty.ID ? ColumnType.INTEGER : ColumnType.TEXT);
    _reagentProperty = reagentProperty;
  }

  public SortByReagentProperty getReagentProperty()
  {
    return _reagentProperty;
  }


  // private methods

}
