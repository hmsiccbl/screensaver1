// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.db.datafetcher.EntityDataFetcher;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.PropertyPath;
import edu.harvard.med.screensaver.model.RelationshipPath;
import edu.harvard.med.screensaver.ui.table.Criterion;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.EntityColumn;
import edu.harvard.med.screensaver.ui.util.ValueReference;

import org.apache.log4j.Logger;

public class VirtualPagingEntityDataModel<K,E extends AbstractEntity> extends VirtualPagingDataModel<K,E>
{
  // static members

  private static Logger log = Logger.getLogger(VirtualPagingEntityDataModel.class);


  // instance data members

  private List<PropertyPath<E>> _lastOrderByProperties;

  
  // public constructors and methods

  public VirtualPagingEntityDataModel(EntityDataFetcher<E,K> dataFetcher,
                                      ValueReference<Integer> rowsToFetch)
  {
    super(dataFetcher, rowsToFetch);
  }

  @Override
  public void fetch(List<? extends TableColumn<E,?>> columns)
  {
    List<RelationshipPath<E>> newRelationshipsToFetch = VirtualPagingEntityDataModel.getRelationshipPaths(columns);
    ((EntityDataFetcher<E,K>) _dataFetcher).setRelationshipsToFetch(newRelationshipsToFetch);
    _fetchedRows.clear();
    log.debug("cleared sorted/filtered row data (forces future re-query of row data)");
  }
  
  public void sort(List<? extends TableColumn<E,?>> sortColumns,
                   SortDirection sortDirection)
  {
    List<PropertyPath<E>> newOrderByProperties = VirtualPagingEntityDataModel.getPropertyPaths(sortColumns);
    // if only sortDirection has changed do not clear sortKeys, as this does not require a database fetch
    if (!newOrderByProperties.equals(_lastOrderByProperties)) {
      ((EntityDataFetcher<E,K>) _dataFetcher).setOrderBy(newOrderByProperties);
      _sortedKeys = null; // force re-fetch
      _lastOrderByProperties = new ArrayList<PropertyPath<E>>(newOrderByProperties);
      log.debug("cleared sort order (forces future re-query of sorted/filtered keys)");
    }
    _sortDirection = sortDirection;
    _rowIndex = -1;
  }
  
  public void filter(List<? extends TableColumn<E,?>> columns)
  {
    Map<PropertyPath<E>,List<? extends Criterion<?>>> newFilterCriteria = VirtualPagingEntityDataModel.getFilteringCriteria(columns);
    ((EntityDataFetcher<E,K>) _dataFetcher).setFilteringCriteria(newFilterCriteria);
    _sortedKeys = null; // force re-fetch
    log.debug("cleared filter (forces future re-query of sorted/filtered keys)");
    _rowIndex = -1;
  }

  
  // private methods

  // static methods
  
  public static <E extends AbstractEntity> List<RelationshipPath<E>> getRelationshipPaths(List<? extends TableColumn<E,?>> columns)
  {
    List<RelationshipPath<E>> relationshipPaths = new ArrayList<RelationshipPath<E>>();
    for (TableColumn<E,?> column : columns) {
      if (column instanceof EntityColumn) {
        if (column.isVisible()) {
          relationshipPaths.addAll(((EntityColumn) column).getRelationshipPaths());
        }
      }
    }
    return relationshipPaths;
  }

  public static <E extends AbstractEntity> Map<PropertyPath<E>,List<? extends Criterion<?>>> getFilteringCriteria(List<? extends TableColumn<E,?>> columns)
  {
    Map<PropertyPath<E>,List<? extends Criterion<?>>> criteria = new HashMap<PropertyPath<E>,List<? extends Criterion<?>>>();
    for (TableColumn<E,?> column : columns) {
      if (column instanceof EntityColumn) {
        if (column.isVisible()) {
          EntityColumn<E,?> entityColumn = (EntityColumn) column;
          if (entityColumn.getPropertyPath() != null) {
            criteria.put(entityColumn.getPropertyPath(),
                         entityColumn.getCriteria());
          }
        }
      }
    }
    return criteria;
  }

  public static <E extends AbstractEntity> List<PropertyPath<E>> getPropertyPaths(List<? extends TableColumn<E,?>> columns)
  {
    List<PropertyPath<E>> propertyPaths = new ArrayList<PropertyPath<E>>();
    for (TableColumn<E,?> column : columns) {
      if (column instanceof EntityColumn) {
        if (column.isVisible()) {
          EntityColumn<E,?> entityColumn = (EntityColumn) column;
          if (entityColumn.getPropertyPath() != null) {
            propertyPaths.add(entityColumn.getPropertyPath());
          }
        }
      }
    }
    return propertyPaths;
  }

}
