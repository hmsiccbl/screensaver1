// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.meta;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.Entity;
import edu.harvard.med.screensaver.util.CollectionUtils;
import edu.harvard.med.screensaver.util.ParallelIterator;
import edu.harvard.med.screensaver.util.StringUtils;

/**
 * Defines the path of relationships between a root entity and a related entity
 * <i>instance<i>. Given a root entity instance, the RelationshipPath can be
 * used to resolve the related entity instance.
 * <p>
 * Since some relationships in the path may be "to-many", a single entity on the
 * "many" side of the relationship must be selected by restricting the
 * relationship collection to only one of its elements. To this end,
 * RelationshipPath supports adding a "restriction" predicate to each of its
 * "to-many" path node. (Currently, the only type of predicate supported is
 * entity ID equality.)
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class RelationshipPath<E extends Entity/* ,L */>
{
  private static Logger log = Logger.getLogger(RelationshipPath.class);
  private static Pattern collectionIndexPattern = Pattern.compile("(.+)\\[(.+)\\]");
  private static final Cardinality DEFAULT_CARDINALITY = Cardinality.TO_MANY;
  private static final String ID_PROPERTY = "id";

  private Class<E> _rootEntityClass;
  protected List<String> _path = Lists.newArrayList();
  protected List<Class<? extends Entity>> _entityClasses = Lists.newArrayList();
  protected List<String> _inversePath = Lists.newArrayList();
  protected List<PropertyNameAndValue> _restrictions = Lists.newArrayList();
  protected List<Cardinality> _cardinality = Lists.newArrayList();
  protected Integer _hashCode;
  protected String _asFormattedPath;
  protected String _asString;


  public static <E extends Entity> RelationshipPath<E> from(Class<E> entityClass)
  {
    return new RelationshipPath<E>(entityClass);
  }

  RelationshipPath(Class<E> rootEntityClass)
  {
    _rootEntityClass = rootEntityClass;
  }

  public RelationshipPath<E> restrict(String restrictionPropertyName, 
                                      Object restrictionValue)
  {
    List<PropertyNameAndValue> newRestrictions = Lists.newArrayList(_restrictions);
    newRestrictions.set(newRestrictions.size() - 1,
                        new PropertyNameAndValue(restrictionPropertyName, restrictionValue));
    return new RelationshipPath<E>(_rootEntityClass, _entityClasses, _path, _inversePath, newRestrictions, _cardinality);
  }

  public RelationshipPath<E> to(String relatedEntityName)
  {
    return to(relatedEntityName, DEFAULT_CARDINALITY);
  }

  public RelationshipPath<E> to(String relatedEntityName, Cardinality cardinality)
  {
    return to(relatedEntityName, null, null, cardinality);
  }

  public RelationshipPath<E> to(String relatedEntityName,
                                Class<? extends Entity> relatedEntityClass,
                                String inverseEntityName,
                                Cardinality cardinality)
  {
    List<Class<? extends Entity>> newEntityClasses = Lists.newArrayList(_entityClasses);
    newEntityClasses.add(relatedEntityClass);
    List<String> newPath = Lists.newArrayList(_path);
    newPath.add(relatedEntityName);
    List<String> newInversePath = Lists.newArrayList(_inversePath);
    newInversePath.add(inverseEntityName);
    List<PropertyNameAndValue> newRestrictions = Lists.newArrayList(_restrictions);
    newRestrictions.add(null);
    List<Cardinality> newCardinality = Lists.newArrayList(_cardinality);
    newCardinality.add(cardinality);
    return new RelationshipPath<E>(_rootEntityClass, newEntityClasses, newPath, newInversePath, newRestrictions, newCardinality);
  }

  public RelationshipPath<E> to(RelationshipPath<? extends Entity> relationship)
  {
    assert !!!(this instanceof PropertyPath);
    List<Class<? extends Entity>> newEntityClasses = Lists.newArrayList(Iterables.concat(_entityClasses, relationship._entityClasses));
    List<String> newPath = Lists.newArrayList(Iterables.concat(_path, relationship._path));
    List<String> newInversePath = Lists.newArrayList(Iterables.concat(_inversePath, relationship._inversePath));
    List<PropertyNameAndValue> newRestrictions = Lists.newArrayList(Iterables.concat(_restrictions, relationship._restrictions));
    List<Cardinality> newCardinality = Lists.newArrayList(Iterables.concat(_cardinality, relationship._cardinality));
    return new RelationshipPath<E>(_rootEntityClass, newEntityClasses, newPath, newInversePath, newRestrictions, newCardinality);
  }

  public PropertyPath<E> to(PropertyPath<? extends Entity> path)
  {
    RelationshipPath<E> relPath = to(path.getAncestryPath());
    if (path.isCollectionOfValues()) {
      return relPath.toCollectionOfValues(path.getPropertyName());
    }
    return relPath.toProperty(path.getPropertyName());
  }
  
  public PropertyPath<E> toProperty(String propertyName)
  {
    return new PropertyPath<E>(this, propertyName, false);
  }

  public PropertyPath<E> toId()
  {
    return toProperty(ID_PROPERTY);
  }

  public PropertyPath<E> toCollectionOfValues(String collectionName)
  {
    return new PropertyPath<E>(this, collectionName, true);
  }

  public PropertyPath<E> toFullEntity()
  {
    return toProperty(PropertyPath.FULL_ENTITY);
  }

  protected RelationshipPath(Class<E> rootEntityClass,
                             List<Class<? extends Entity>> entityClasses,
                             List<String> path,
                             List<String> inversePath,
                             List<PropertyNameAndValue> restrictions,
                             List<Cardinality> cardinality)
  {
    _rootEntityClass = rootEntityClass;
    _entityClasses.addAll(entityClasses);
    _path.addAll(path);
    _inversePath.addAll(inversePath);
    _restrictions.addAll(restrictions);
    _cardinality.addAll(cardinality);
  }

  public Class<E> getRootEntityClass()
  {
    return _rootEntityClass;
  }

  public int getPathLength()
  {
    return _path.size();
  }

  /**
   * @return the path as a String of dot-separated concatenation of the path
   *         nodes. Does not contain any restriction information, as does
   *         {@link #toString()}.
   * @deprecated code that needs a string representation of a RelationshipPath
   *             should be updated to use RelationshipPath directly to inspect
   *             the nodes comprising the path
   */
  @Deprecated
  public String getPath()
  {
    if (_asFormattedPath == null) {
      _asFormattedPath = StringUtils.makeListString(_path, ".");
    }
    return _asFormattedPath;
  }

  public Iterator<String> pathIterator()
  {
    return Iterators.unmodifiableIterator(_path.iterator());
  }

  public Iterator<String> inversePathIterator()
  {
    return Iterators.unmodifiableIterator(_inversePath.iterator());
  }

  public Iterator<Class<? extends Entity>> entityClassIterator()
  {
    return Iterators.unmodifiableIterator(_entityClasses.iterator());
  }

  public RelationshipPath<E> getAncestryPath()
  {
    if (_path.size() == 0) {
      return null;
    }
    int i = _path.size() - 1;
    return new RelationshipPath<E>(_rootEntityClass,
                                   _entityClasses,
                                   _path.subList(0, i),
                                   _inversePath.subList(0, i),
                                   _restrictions.subList(0, i),
                                   _cardinality.subList(0, i));
  }

  public RelationshipPath<E> getUnrestrictedPath()
  {
    List<PropertyNameAndValue> newRestrictions = Lists.newArrayList();
    CollectionUtils.fill(newRestrictions, null, _restrictions.size());
    return new RelationshipPath<E>(_rootEntityClass,
                                   _entityClasses,
                                   _path,
                                   _inversePath,
                                   newRestrictions,
                                   _cardinality);
  }

  public String getLeaf()
  {
    if (_path.size() > 0) {
      return _path.get(_path.size() - 1);
    }
    return "";
  }

  public boolean equals(Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj instanceof RelationshipPath) {
      RelationshipPath other = (RelationshipPath) obj;
      return hashCode() == other.hashCode();
    }
    return false;
  }

  @Override
  public int hashCode()
  {
    if (_hashCode == null) {
      _hashCode = toString().hashCode();
    }
    return _hashCode;
  }

  public String toString()
  {
    if (_asString == null) {
      StringBuilder s = new StringBuilder();
      s.append('<').append(_rootEntityClass.getSimpleName()).append('>');
      ParallelIterator<String,PropertyNameAndValue> iter = new ParallelIterator<String,PropertyNameAndValue>(_path.iterator(), _restrictions.iterator());
      while (iter.hasNext()) {
        iter.next();
        appendPathElement(s, iter.getFirst(), iter.getSecond());
      }
      _asString = s.toString();
    }
    return _asString;
  }

  public boolean hasRestrictions()
  {
    return Iterables.any(_restrictions, Predicates.notNull());
  }

  public Iterator<PropertyNameAndValue> restrictionIterator()
  {
    return Iterators.unmodifiableIterator(_restrictions.iterator());
  }

  public PropertyNameAndValue getLeafRestriction()
  {
    if (_path.size() > 0) {
      return _restrictions.get(_path.size() - 1);
    }
    return null;
  }

  public Cardinality getCardinality()
  {
    ParallelIterator<Cardinality,PropertyNameAndValue> iterator = new ParallelIterator<Cardinality,PropertyNameAndValue>(_cardinality.iterator(), _restrictions.iterator());
    while (iterator.hasNext()) {
      iterator.next();
      if (iterator.getFirst() == Cardinality.TO_MANY &&
        iterator.getSecond() == null) {
        return Cardinality.TO_MANY;
      }
    }
    return Cardinality.TO_ONE;
  }

  private void appendPathElement(StringBuilder s,
                                 String pathElementName,
                                 PropertyNameAndValue propertyNameAndValue)
  {
    s.append('.').append(pathElementName);
    if (propertyNameAndValue != null) {
      s.append('[')
       .append(propertyNameAndValue.getName())
       .append('=')
       .append(propertyNameAndValue.getValue())
       .append(']');
    }
  }
}
