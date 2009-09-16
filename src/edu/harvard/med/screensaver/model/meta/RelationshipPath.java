// $HeadURL:
// svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml
// $
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.meta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.util.Pair;
import edu.harvard.med.screensaver.util.ParallelIterator;
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import com.google.common.collect.Lists;

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
public class RelationshipPath<E extends AbstractEntity/*,L*/> implements Iterable<Pair<String,PropertyNameAndValue>>
{
  // static members

  private static Logger log = Logger.getLogger(RelationshipPath.class);

// private static Pattern collectionIndexPattern = Pattern.compile("(.+)\\*");
  private static Pattern collectionIndexPattern = Pattern.compile("(.+)\\[(.+)\\]");

  // instance data members

  private Class<E> _rootEntityClass;
  /*private Class<L> _leafClass;*/
  protected List<String> _path = new ArrayList<String>();
  protected List<PropertyNameAndValue> _restrictions = new ArrayList<PropertyNameAndValue>();
  protected Integer _hashCode;
  protected String _asFormattedPath;
  protected String _asString;


  // public constructors and methods

//  public RelationshipPath(Class<E> rootEntityClass,
//                          String path,
//                          Object... sparseRestrictionValues)
//  {
//    this(rootEntityClass, path, Arrays.asList(sparseRestrictionValues));
//  }

//  RelationshipPath(Class<E> rootEntityClass,
//                   String path,
//                   List<Object> sparseRestrictionValues)
//  {
//    List<String> pathElements = path == null || path.trim()
//                                                    .length() == 0 ? Collections.<String> emptyList()
//                                                                  : Arrays.asList(path.split("\\."));
//    _rootEntityClass = rootEntityClass;
//    Iterator<Object> restrictionValuesIter = sparseRestrictionValues.iterator();
//    for (int i = 0; i < pathElements.size(); i++) {
//      String pathElement = pathElements.get(i);
//      Matcher matcher = collectionIndexPattern.matcher(pathElement);
//      if (matcher.matches()) {
//        String collectionPropertyName = matcher.group(1);
//        _restrictions.add(new PropertyNameAndValue(matcher.group(2),
//                                                   restrictionValuesIter.next()));
//        _path.add(collectionPropertyName);
//      }
//      else {
//        _path.add(pathElement);
//        _restrictions.add(null);
//      }
//    }
//  }
  
  public RelationshipPath(Class<E> rootEntityClass)
  {
    this(rootEntityClass, null);
  }

  public RelationshipPath(Class<E> rootEntityClass,
                          String relatedEntityName)
  {
    _rootEntityClass = rootEntityClass;
    if (StringUtils.isEmpty(relatedEntityName)) {
      _path = Collections.emptyList();
    }
    else {
      _path = Collections.singletonList(relatedEntityName);
    }
    _restrictions.add(null);
  }

  public RelationshipPath<E> restrict(String restrictionPropertyName, 
                                      Object restrictionValue)
  {
    List<PropertyNameAndValue> newRestrictions = Lists.newArrayList(_restrictions);
    newRestrictions.set(newRestrictions.size() - 1,
                        new PropertyNameAndValue(restrictionPropertyName, restrictionValue));
    return new RelationshipPath<E>(_rootEntityClass, _path, newRestrictions); 
  }

  public RelationshipPath<E> to(String relatedEntityName)
  {
    ArrayList<String> newPath = Lists.newArrayList(_path);
    newPath.add(relatedEntityName);
    ArrayList<PropertyNameAndValue> newRestrictions = Lists.newArrayList(_restrictions);
    newRestrictions.add(null);
    return new RelationshipPath<E>(_rootEntityClass, newPath, newRestrictions);
  }

  public RelationshipPath<E> to(RelationshipPath<? extends AbstractEntity> relationship)
  {
    assert !!!(this instanceof PropertyPath);
    ArrayList<String> newPath = Lists.newArrayList(_path);
    newPath.addAll(relationship._path);
    ArrayList<PropertyNameAndValue> newRestrictions = Lists.newArrayList(_restrictions);
    newRestrictions.addAll(relationship._restrictions);
    return new RelationshipPath<E>(_rootEntityClass, newPath, newRestrictions);
  }
  
  public PropertyPath<E> to(PropertyPath<? extends AbstractEntity> relationship)
  {
    assert !!!(this instanceof PropertyPath);
    return to(relationship.getRelationshipPath()).toProperty(relationship.getPropertyName());
  }
  
  public PropertyPath<E> toProperty(String propertyName)
  {
    return new PropertyPath<E>(this, propertyName);
  }

  public PropertyPath<E> toCollectionOfValues()
  {
    return toProperty(PropertyPath.COLLECTION_OF_VALUES);
  }

  protected RelationshipPath(Class<E> rootEntityClass,
                             List<String> path,
                             List<PropertyNameAndValue> restrictions)
  {
    _rootEntityClass = rootEntityClass;
    _path.addAll(path);
    _restrictions.addAll(restrictions);
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

  public RelationshipPath<E> getAncestryPath()
  {
    int i = Math.max(0, _path.size() - 1);
    return new RelationshipPath<E>(_rootEntityClass,
                                   _path.subList(0, i),
                                   _restrictions.subList(0, i));
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
      _hashCode = _rootEntityClass.hashCode() * 7;
      for (Pair<String,PropertyNameAndValue> node : this) {
        _hashCode += node.hashCode() * 11;
      }
    }
    return _hashCode;
  }

  public String toString()
  {
    if (_asString == null) {
      StringBuilder s = new StringBuilder();
      s.append('<').append(_rootEntityClass.getSimpleName()).append('>');
      RelationshipPathIterator iter = iterator();
      while (iter.hasNext()) {
        iter.next();
        appendPathElement(s,
                          iter.getPathElement(),
                          iter.getRestrictionPropertyNameAndValue());
      }
      _asString = s.toString();
    }
    return _asString;
  }

  public boolean hasRestrictions()
  {
    return CollectionUtils.cardinality(null, _restrictions) != _restrictions.size();
  }

  public PropertyNameAndValue getRestrictionPropertyNameAndValue(int i)
  {
    return _restrictions.get(i);
  }

  public PropertyNameAndValue getLeafRestrictionPropertyNameAndValue()
  {
    if (_path.size() > 0) {
      return _restrictions.get(_path.size() - 1);
    }
    return null;
  }

  public RelationshipPathIterator iterator()
  {
    return new RelationshipPathIterator(_path.iterator(),
                                        _restrictions.iterator());
  }

  public class RelationshipPathIterator extends ParallelIterator<String,PropertyNameAndValue>
  {
    private int _index = -1;

    public RelationshipPathIterator(Iterator<String> pathIterator,
                                    Iterator<PropertyNameAndValue> restrictionIterator)
    {
      super(pathIterator, restrictionIterator);
    }

    @Override
    public Pair<String,PropertyNameAndValue> next()
    {
      ++_index;
      return super.next();
    }

    public String getPathElement()
    {
      return getFirst();
    }

    public PropertyNameAndValue getRestrictionPropertyNameAndValue()
    {
      return getSecond();
    }

    public RelationshipPath getIntermediatePath()
    {
      RelationshipPath intermediatePropertyPath = new RelationshipPath<E>(_rootEntityClass,
                                                                          _path.subList(0,
                                                                                        _index + 1),
                                                                          _restrictions.subList(0,
                                                                                                _index + 1));
      return intermediatePropertyPath;

    }
  }


  // private methods

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
