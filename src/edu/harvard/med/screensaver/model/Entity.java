// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.io.Serializable;

import com.google.common.base.Function;

public interface Entity<K extends Serializable>
{
  public static final Function<Entity,Serializable> ToEntityId = new Function<Entity,Serializable>() {
    public Serializable apply(Entity entity)
    {
      return entity.getEntityId();
    }
  }; 

  K getEntityId();
  boolean isTransient();
  boolean isRestricted();
  Class<Entity<K>> getEntityClass();
  /**
   * To enable visitor to visit a particular subclass, override this method and
   * insert <code>visitor.acceptVisitor(this);</code>
   * 
   * @param visitor
   * @motivation to keep most of our AbstractEntity subclasses clean, as we
   *             currently only have the DataAccessPolicy visitor, which does
   *             not actually need to visit every subclass.
   */
   Object acceptVisitor(AbstractEntityVisitor visitor);
}
