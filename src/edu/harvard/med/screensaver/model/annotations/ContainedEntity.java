// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import edu.harvard.med.screensaver.model.AbstractEntity;

/**
 * An annotation for a containment relationship.
 * {@link #containingEntityClass() Containers} are expected to have
 * <code>create<Contained></code> factory methods for the Contained.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ContainedEntity {
  /**
   * Return the class of the {@link AbstractEntity} container.
   * 
   * @return the class of the containing entity
   */
  Class<? extends AbstractEntity> containingEntityClass();
}

