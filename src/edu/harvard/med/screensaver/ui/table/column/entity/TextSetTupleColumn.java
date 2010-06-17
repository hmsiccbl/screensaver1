// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table.column.entity;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import edu.harvard.med.screensaver.db.datafetcher.Tuple;
import edu.harvard.med.screensaver.db.datafetcher.TupleDataFetcher;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.ui.table.column.ColumnType;
import edu.harvard.med.screensaver.ui.table.column.SetColumn;

public class TextSetTupleColumn<E extends AbstractEntity,K> extends SetColumn<Tuple<K>,String> implements HasFetchPaths<E>
{
  private FetchPaths<E,Tuple<K>> _fetchPaths;
  private String _propertyKey;
  
  public TextSetTupleColumn(PropertyPath<E> propertyPath, String name, String description, String group)
  {
    super(name, description, group, ColumnType.INTEGER_SET);
    _fetchPaths = new FetchPaths<E,Tuple<K>>(propertyPath);
    _propertyKey = TupleDataFetcher.makePropertyKey(_fetchPaths.getPropertyPath());
  }

  public void addRelationshipPath(RelationshipPath<E> path)
  {
    _fetchPaths.addRelationshipPath(path);
  }

  public PropertyPath<E> getPropertyPath()
  {
    return _fetchPaths.getPropertyPath();
  }

  public Set<RelationshipPath<E>> getRelationshipPaths()
  {
    return _fetchPaths.getRelationshipPaths();
  }

  public boolean isFetchableProperty()
  {
    return _fetchPaths.isFetchableProperty();
  }

  @Override
  public Set<String> getCellValue(Tuple<K> tuple)
  {
    List<String> values = (List<String>) tuple.getProperty(_propertyKey);
    if (values == null) {
      return Collections.emptySet();
    }
    return ImmutableSet.copyOf(values);
  }

}
