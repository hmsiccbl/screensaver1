// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.datatable.model;

import java.util.Iterator;
import java.util.List;

import javax.faces.model.DataModel;

import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.ui.arch.datatable.DataTableModelType;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;

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
   * 
   * @param columns tells the DataTableModel <i>what</i> needs to be fetched
   *          in order to populate a given row.
   */
  public abstract void fetch(List<? extends TableColumn<R,?>> columns);

  public abstract void sort(List<? extends TableColumn<R,?>> sortColumns,
                            SortDirection sortDirection);
  
  public abstract void filter(List<? extends TableColumn<R,?>> filterColumns);

  // TODO
  // public abstract R getRowData(int rowIndex);
  
  public abstract Iterator<R> iterator();
  
  /**
   * @motivation for unit tests
   */
  public abstract DataTableModelType getModelType();
}
