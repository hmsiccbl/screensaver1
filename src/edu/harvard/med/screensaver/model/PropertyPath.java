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

package edu.harvard.med.screensaver.model;

import java.util.Arrays;

import org.apache.log4j.Logger;

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
  // static members

  private static Logger log = Logger.getLogger(PropertyPath.class);
  
  /**
   * Use to communicate the intent of the 'propertyName' constructor argument,
   * when the property path represents a collection of values, which have no
   * particular property name other than the collection element itself.
   */
  public final static String COLLECTION_OF_VALUES = "";


  // instance data members

  private String _propertyName;


  // public constructors and methods

  /**
   * @propertyName the name of the property. Can be COLLECTION_OF_VALUES if the leaf of
   *               the path is a collection of values (which have no further
   *               properties except the values themselves)
   */
  public PropertyPath(Class<E> rootEntityClass,
                      String propertyName,
                      Object... restrictionValues)
  {
    super(rootEntityClass,
         "",
         Arrays.asList(restrictionValues));
    _propertyName = propertyName;
  }

  /**
   * @propertyName the name of the property. Can be COLLECTION_OF_VALUES if the leaf of
   *               the path is a collection of values (which have no further
   *               properties except the values themselves)
   */
  public PropertyPath(Class<E> rootEntityClass,
                      String path,
                      String propertyName,
                      Object... restrictionValues)
  {
    super(rootEntityClass,
         path,
         Arrays.asList(restrictionValues));
    _propertyName = propertyName;
  }

  /**
   * @propertyName the name of the property. Can be COLLECTION_OF_VALUES if the leaf of
   *               the path is a collection of values (which have no further
   *               properties except the values themselves)
   */
  public PropertyPath(RelationshipPath<E> path,
                      String propertyName)
  {
    super(path.getRootEntityClass(),
          path._path,
          path._restrictions);
    _propertyName = propertyName;
  }

  public RelationshipPath<E> getRelationshipPath()
  {
    return new RelationshipPath<E>(
      getRootEntityClass(),
      _path,
      _restrictions);
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
  public String getFormattedPath()
  {
    if (_asFormattedPath == null) {
      _asFormattedPath = super.getFormattedPath();
      if (_asFormattedPath.length() > 0) {
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
}
