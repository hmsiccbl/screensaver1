// $HeadURL:
// svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml
// $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table.model;

import java.util.List;

import javax.faces.model.DataModel;

import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;

/**
 * Persistence-backed data model for JSF data tables.
 * @author drew
 */
public abstract class DataTableModel<R> extends DataModel
{
  /**
   * Fetch (or refetch) all data when data needs to be fully (re)loaded from the
   * database. As opposed to just re-sorting and/or re-filtering data that has
   * already been loaded. For example, this is needed when columns are added
   * (but otherwise do not affect the filter or sort order), when domain
   * restrictions have changed, the database has been updated, etc.
   */
  public abstract void fetch(List<? extends TableColumn<R,?>> allColumns);

  public abstract void sort(List<? extends TableColumn<R,?>> sortColumns,
                            SortDirection sortDirection);
  
  public abstract void filter(List<? extends TableColumn<R,?>> filterColumns);
}
