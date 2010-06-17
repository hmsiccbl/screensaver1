// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table.column.entity;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.faces.convert.Converter;

import edu.harvard.med.screensaver.db.datafetcher.Tuple;
import edu.harvard.med.screensaver.db.datafetcher.TupleDataFetcher;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.ui.table.column.VocabularyColumn;

public class VocabularyTupleColumn<E extends AbstractEntity,K,V> extends VocabularyColumn<Tuple<K>,V> implements HasFetchPaths<E>
{
  private Set<V> _items;
  private FetchPaths<E,Tuple<K>> _fetchPaths;
  private String _propertyKey;
  
  public VocabularyTupleColumn(PropertyPath<E> propertyPath,
                               String name,
                               String description,
                               String group,
                               Converter converter,
                               Set<V> items)
  {
    super(name, description, group, converter, items);
    setConverter(converter);
    _items = new LinkedHashSet<V>(items);
    _fetchPaths = new FetchPaths<E,Tuple<K>>(propertyPath);
    _propertyKey = TupleDataFetcher.makePropertyKey(_fetchPaths.getPropertyPath());
  }

  public VocabularyTupleColumn(PropertyPath<E> propertyPath,
                               String name,
                               String description,
                               String group,
                               Converter converter,
                               V[] items)
  {
    this(propertyPath, name, description, group, converter, new TreeSet<V>(Arrays.asList(items)));
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
  public V getCellValue(Tuple<K> tuple)
  {
    return (V) tuple.getProperty(_propertyKey);
  }
}
