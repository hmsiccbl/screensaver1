// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.datatable.column.entity;

import java.util.Set;

import org.joda.time.DateTime;

import edu.harvard.med.screensaver.db.datafetcher.Tuple;
import edu.harvard.med.screensaver.db.datafetcher.TupleDataFetcher;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.ui.arch.datatable.column.DateTimeColumn;

public class DateTimeTupleColumn<E extends AbstractEntity,K> extends DateTimeColumn<Tuple<K>> implements HasFetchPaths<E>
{
  private FetchPaths<E,Tuple<K>> _fetchPaths;
  private String _propertyKey;
  
  public DateTimeTupleColumn(PropertyPath<E> propertyPath, String name, String description, String group)
  {
    super(name, description, group);
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
  protected DateTime getDateTime(Tuple<K> tuple)
  {
    return (DateTime) tuple.getProperty(_propertyKey);
  }
}
