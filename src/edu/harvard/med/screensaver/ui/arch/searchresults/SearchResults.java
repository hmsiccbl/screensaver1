// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.searchresults;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.datafetcher.NoOpDataFetcher;
import edu.harvard.med.screensaver.ui.arch.datatable.EditableDataTable;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.model.DataTableModel;
import edu.harvard.med.screensaver.ui.arch.datatable.model.InMemoryDataModel;
import edu.harvard.med.screensaver.ui.arch.util.UISelectOneBean;


/**
 * DataTable subclass that automatically builds its own DataTableModel using a
 * provided DataFetcher.
 * 
 * @param R the row type of the data to be fetched from the database
 * @param K the key type used to uniquely identify each row
 * @param P type used to identify "properties" to be filtered and ordered
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
abstract public class SearchResults<R, K, P> extends EditableDataTable<R>
{
  private static final Logger log = Logger.getLogger(SearchResults.class);

  public static final List<Integer> DEFAULT_ROWS_PER_PAGE_SELECTIONS = Arrays.asList(10,
                                                                                     20,
                                                                                     50,
                                                                                     100);

  private String _title;
  private Map<String,Boolean> _capabilities = new HashMap<String,Boolean>();

  /**
   * @motivation for CGLIB2
   */
  protected SearchResults()
  {}

  protected SearchResults(String[] capabilities)
  {
    if (capabilities != null) {
      for (String capability : capabilities) {
        _capabilities.put(capability, true);
      }
    }
    getIsPanelCollapsedMap().put("columns", true);
    getIsPanelCollapsedMap().put("search", true);
    initialize();
  }

  /**
   * Initializes the SearchResults for a new data set. Columns are (re)built,
   * existing data is cleared and (re)fetched via the specified DataFetcher.
   */
  public void initialize(DataTableModel<R> dataTableModel)
  {
    initialize(dataTableModel, buildColumns());
  }
  
  public void initialize(DataTableModel<R> dataTableModel, List<? extends TableColumn<R,?>> columns)
  {
    initialize(dataTableModel,
               columns,
               buildRowsPerPageSelector(),
               !getApplicationProperties().isFeatureEnabled("data_table_tree_column_selector"));
  }
  
  /** 
   * Initialize with empty search result and no columns 
   */
  public void initialize()
  {
    initialize(new InMemoryDataModel<R>(new NoOpDataFetcher<R,K,P>()),
               new ArrayList<TableColumn<R,?>>(),
               new UISelectOneBean<Integer>(Arrays.asList(0)),
               false);  
  }


  // abstract methods

  abstract protected List<? extends TableColumn<R,?>> buildColumns();

  // public methods

  public String getTitle()
  {
    return _title;
  }

  public void setTitle(String title)
  {
    _title = title;
  }

  /**
   * @motivation to allow JSF pages to know what subclass methods are available;
   *             this is a hack that is the JSF-equivalent of using the Java
   *             'instanceof' operator before downcasting an object to get at
   *             its subclass methods
   */
  public Map<String,Boolean> getCapabilities()
  {
    return _capabilities;
  }

  public boolean isRowRestricted()
  {
    return false;
  }

  // protected instance methods

  /**
   * Subclass should override if it needs to specify a custom
   * RowsPerPageSelector
   * 
   * @return a DataTableRowsPerPageUISelectOneBean or null if the default
   *         DataTableRowsPerPageUISelectOneBean, as built by DataTable, is
   *         acceptable.
   */
  protected UISelectOneBean<Integer> buildRowsPerPageSelector()
  {
    return new UISelectOneBean<Integer>(DEFAULT_ROWS_PER_PAGE_SELECTIONS,
                                        DEFAULT_ROWS_PER_PAGE_SELECTIONS.get(1));
  }
}
