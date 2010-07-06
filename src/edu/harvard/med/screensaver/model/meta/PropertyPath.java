// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.meta;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.util.StringUtils;

/**
 * Mechanism for specifying an entity property within an entity object
 * network. Optionally, allows for selection of a single entity in a path that
 * traverses a to-many relationships by allowing a single element in the to-many
 * relationship to be selected via its entity ID.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class PropertyPath<E extends AbstractEntity> extends RelationshipPath<E>
{
  private static Logger log = Logger.getLogger(PropertyPath.class);
  
  public final static String FULL_ENTITY = "*";

  private String _propertyName;
  private boolean _isCollectionOfValues;


  PropertyPath(RelationshipPath<E> relationshipPath, String propertyName, boolean isCollectionOfValues)
  {
    super(relationshipPath.getRootEntityClass(),
          relationshipPath._entityClasses,
          relationshipPath._path,
          relationshipPath._inversePath,
          relationshipPath._restrictions,
          relationshipPath._cardinality);
    _propertyName = propertyName;
    _isCollectionOfValues = isCollectionOfValues;
    _asFormattedPath = _asString = null; // force init by PropertyPath, not RelatioshipPath
  }

  @Override
  public PropertyPath<E> getUnrestrictedPath()
  {
    return new PropertyPath<E>(super.getUnrestrictedPath(), _propertyName, _isCollectionOfValues);
  }

  @Override
  public RelationshipPath<E> getAncestryPath()
  {
    return new RelationshipPath<E>(getRootEntityClass(), _entityClasses, _path, _inversePath, _restrictions, _cardinality);
  }

  public String getPropertyName()
  {
    return _propertyName;
  }

  public String toString()
  {
    if (_asString == null) {
      _asString = super.toString();
      if (_asString.length() > 0) {
        _asString += ".";
      }
      _asString += _propertyName;
    }
    return _asString;
  }
  
  @Override
  public String getPath()
  {
    if (_asFormattedPath == null) {
      _asFormattedPath = super.getPath();
      if (_asFormattedPath.length() > 0 &&
        !StringUtils.isEmpty(_propertyName) /* handle collection of values */) {
        _asFormattedPath += ".";
      }
      _asFormattedPath += _propertyName;
    }
    return _asFormattedPath;
  }

  @Override
  public int hashCode()
  {
    return super.hashCode() + _propertyName.hashCode() * 17;
  }

  public boolean isCollectionOfValues()
  {
    return _isCollectionOfValues;
  }

  @Override
  public Cardinality getCardinality()
  {
    if (isCollectionOfValues()) {
      return Cardinality.TO_MANY;
    }
    return super.getCardinality();
  }
}
