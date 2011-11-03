// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.datatable.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.Criterion;
import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.db.datafetcher.DataFetcher;
import edu.harvard.med.screensaver.model.Entity;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.FetchPaths;
import edu.harvard.med.screensaver.util.ValueReference;

public class VirtualPagingEntityDataModel<K extends Serializable,E extends Entity<K>,R> extends VirtualPagingDataModel<K,R>
{
  private static Logger log = Logger.getLogger(VirtualPagingEntityDataModel.class);

  private DataFetcher<R,K,PropertyPath<E>> _dataFetcher;
  private List<PropertyPath<E>> _lastOrderByProperties;

  
  public VirtualPagingEntityDataModel(DataFetcher<R,K,PropertyPath<E>> dataFetcher,
                                      ValueReference<Integer> rowsToFetch)
  {
    super(dataFetcher, rowsToFetch);
    _dataFetcher = dataFetcher;
  }

  @Override
  public void fetch(List<? extends TableColumn<R,?>> columns)
  {
    _dataFetcher.setPropertiesToFetch(FetchPaths.<E,R>getPropertyPaths(columns));
    _fetchedRows.clear();
    log.debug("cleared sorted/filtered row data (forces future re-query of row data)");
  }
  
  public void sort(List<? extends TableColumn<R,?>> sortColumns, SortDirection sortDirection)
  {
    List<PropertyPath<E>> newOrderByProperties = FetchPaths.getPropertyPaths(sortColumns);
    // if only sortDirection has changed do not clear sortKeys, as this does not require a database fetch
    if (!newOrderByProperties.equals(_lastOrderByProperties)) {
      _dataFetcher.setOrderBy(newOrderByProperties);
      _sortedKeys = null; // force re-fetch
      _lastOrderByProperties = new ArrayList<PropertyPath<E>>(newOrderByProperties);
      log.debug("cleared sort order (forces future re-query of sorted/filtered keys)");
    }
    _sortDirection = sortDirection;
    _rowIndex = -1;
  }
  
  public void filter(List<? extends TableColumn<R,?>> filterColumns)
  {
    Map<PropertyPath<E>,List<? extends Criterion<?>>> newFilterCriteria = FetchPaths.getFilteringCriteria(filterColumns);
    _dataFetcher.setFilteringCriteria(newFilterCriteria);
    _sortedKeys = null; // force re-fetch
    log.debug("cleared filter (forces future re-query of sorted/filtered keys)");
    _rowIndex = -1;
  }

}
