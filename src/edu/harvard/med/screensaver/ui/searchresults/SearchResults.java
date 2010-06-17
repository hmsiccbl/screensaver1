// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.datafetcher.NoOpDataFetcher;
import edu.harvard.med.screensaver.ui.table.EditableDataTable;
import edu.harvard.med.screensaver.ui.table.RowsPerPageSelector;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.ui.table.model.DataTableModel;
import edu.harvard.med.screensaver.ui.table.model.InMemoryDataModel;


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

  // public static final data

  private static final Logger log = Logger.getLogger(SearchResults.class);

  public static final List<Integer> DEFAULT_ROWS_PER_PAGE_SELECTIONS = Arrays.asList(10,
                                                                                     20,
                                                                                     50,
                                                                                     100,
                                                                                     RowsPerPageSelector.SHOW_ALL_VALUE);

  // private instance data

  private Map<String,Boolean> _capabilities = new HashMap<String,Boolean>();


  // public constructor

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
    // initialize to do nothing
    initialize(new InMemoryDataModel<R>(new NoOpDataFetcher<R,K,P>()),
               new ArrayList<TableColumn<R,?>>(),
               new RowsPerPageSelector(Arrays.asList(0)),
               false);
  }

  /**
   * Initializes the SearchResults for a new data set. Columns are (re)built,
   * existing data is cleared and (re)fetched via the specified DataFetcher.
   * 
   * @param dataFetcher
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
               !isFeatureEnabled("data_table_tree_column_selector"));               
  }

  // abstract methods

  abstract protected List<? extends TableColumn<R,?>> buildColumns();

  // public methods

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
  protected RowsPerPageSelector buildRowsPerPageSelector()
  {
    RowsPerPageSelector rowsPerPageSelector = new RowsPerPageSelector(DEFAULT_ROWS_PER_PAGE_SELECTIONS,
                                                                      DEFAULT_ROWS_PER_PAGE_SELECTIONS.get(1)) {
      @Override
      protected Integer getAllRowsValue()
      {
        return getRowCount();
      }
    };
    return rowsPerPageSelector;
  }


  // private instance methods

}
