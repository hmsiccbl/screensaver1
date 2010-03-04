// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table.column.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.ui.table.Criterion;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;

/**
 * TableColumn that represents a model entity property, maintaining a data
 * binding, enabling a persistence layer to find the data for this property in
 * the underlying data schema. Maintains a PropertyPath that specifies path to
 * the property in the data model, relative to the root entity type of the
 * column. Also maintains one or more RelationshipPaths, which are used by a
 * persistence layer to form a query that fetches the entity (or entities)
 * containing the data needed to populate this column.
 * <p>
 * The constructor that takes a PropertyPath should be used whenever possible,
 * since this PropertyPath is used by a persistence layer to generate queries
 * for <i>filtering</i> and <i>sorting</i> on this column, allowing these
 * operations to be performed at the database level. Specifying a PropertyPath
 * implies a RelationshipPath.
 * <p>
 * The constructor that takes a RelationshipPath is used (as a last resort) for
 * properties that are derived, or otherwise not directly stored by the data
 * schema used to persist the data model. Note that if this Relationship
 * constructor is used, a persistence layer will not be able to filter and sort
 * on this column at the persistence layer (i.e., by the database server), since
 * a particular schema field is not bound to this column.
 * <p>
 * If the property has a dependency on multiple fields in the data schema that
 * are stored in other related entities, additional RelationshipPaths can be
 * added to ensure these entities' fields are fetched as well. For example, a
 * derived property may be calculated from multiple properties of related
 * entities. Or, for example, a relationship that is a Map, keyed on an entity
 * type, there may be performance benefits to fetching the entities that
 * represent the keys of that map (to avoid Hibernate from fetching each of
 * these keys in an individual SELECT); e.g., Well.resultValues is a Map<ResultValueType,ResultValue>.
 * 
 * @param E the root entity type of this column
 * @author ant4
 */
public class FetchPaths<E extends AbstractEntity> implements HasFetchPaths<E>
{
  private PropertyPath<E> _propertyPath;
  private Set<RelationshipPath<E>> _relationshipPaths = new HashSet<RelationshipPath<E>>(1);
  private boolean _isSingleProperty;

  /**
   * Constructs an EntityColumn with a RelationshipPath, thus specifying how to
   * fetch the particular data that is to be displayed by this column, relative
   * to a root entity type.
   */
  public FetchPaths(RelationshipPath<E> relationshipPath)
  {
    if (relationshipPath instanceof PropertyPath) {
      PropertyPath<E> propertyPath = (PropertyPath<E>) relationshipPath;
      _relationshipPaths.add(propertyPath.getRelationshipPath());
      _propertyPath = propertyPath;
      _isSingleProperty = true;
    }
    else {
      _relationshipPaths.add(relationshipPath);
    }
  }

  public Set<RelationshipPath<E>> getRelationshipPaths()
  {
    return _relationshipPaths;
  }

  public PropertyPath<E> getPropertyPath()
  {
    return _propertyPath;
  }
  
  public void addRelationshipPath(RelationshipPath<E> path) 
  {
    _relationshipPaths.add(path);
    _isSingleProperty = false;
  }

  public boolean isFetchableProperty()
  {
    return _isSingleProperty;
  }

  public static <E extends AbstractEntity> List<PropertyPath<E>> getPropertyPaths(List<? extends TableColumn<E,?>> columns)
  {
    List<PropertyPath<E>> propertyPaths = new ArrayList<PropertyPath<E>>();
    for (TableColumn<E,?> column : columns) {
      if (column instanceof HasFetchPaths) {
        if (column.isVisible()) {
          HasFetchPaths<E> entityColumn = (HasFetchPaths<E>) column;
          if (entityColumn.getPropertyPath() != null) {
            propertyPaths.add(entityColumn.getPropertyPath());
          }
        }
      }
    }
    return propertyPaths;
  }

  public static <E extends AbstractEntity> Map<PropertyPath<E>,List<? extends Criterion<?>>> getFilteringCriteria(List<? extends TableColumn<E,?>> columns)
  {
    Map<PropertyPath<E>,List<? extends Criterion<?>>> criteria = new HashMap<PropertyPath<E>,List<? extends Criterion<?>>>();
    for (TableColumn<E,?> column : columns) {
      if (column instanceof HasFetchPaths) {
        if (column.isVisible()) {
          HasFetchPaths<E> fetchPaths = (HasFetchPaths<E>) column;
          if (fetchPaths.getPropertyPath() != null) {
            criteria.put(fetchPaths.getPropertyPath(),
                         column.getCriteria());
          }
        }
      }
    }
    return criteria;
  }

  public static <E extends AbstractEntity> List<RelationshipPath<E>> getRelationshipPaths(List<? extends TableColumn<E,?>> columns)
  {
    List<RelationshipPath<E>> relationshipPaths = new ArrayList<RelationshipPath<E>>();
    for (TableColumn<E,?> column : columns) {
      if (column instanceof HasFetchPaths) {
        if (column.isVisible()) {
          relationshipPaths.addAll(((HasFetchPaths<E>) column).getRelationshipPaths());
        }
      }
    }
    return relationshipPaths;
  }
}