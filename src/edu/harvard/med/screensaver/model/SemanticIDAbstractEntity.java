// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;


/**
 * An {@link AbstractEntity} with a semantic ID.
 * <code>SemanticIDAbstractEntities</code> have identifiers with "assigned"
 * generators. The identifiers are always set in the object constructors, and
 * can therefore be used to determine entity equivalence even before the
 * entity is persisted in a Hibernate session. Their {@link #equals} and
 * {@link #hashCode} methods are defined here, based on the
 * {@link #getEntityId() entity ID}.
 * <p>
 * Semantic IDs are particularly useful if you need to be able to look up an
 * existing entity in an unflushed Hibernate session, since the only way to look
 * up a newly created entity in an unflushed session is by the entity id.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public abstract class SemanticIDAbstractEntity extends AbstractEntity
{
  public boolean equals(Object object)
  {
    if (object != null) {
      if (object instanceof SemanticIDAbstractEntity) {
        SemanticIDAbstractEntity that = (SemanticIDAbstractEntity) object;
        if (getEntityClass().equals(that.getEntityClass())) {
          return getEntityId().equals(that.getEntityId());
        }
      }
    }
    return false;
  }

  public int hashCode()
  {
    return getEntityId().hashCode();
  }
}

