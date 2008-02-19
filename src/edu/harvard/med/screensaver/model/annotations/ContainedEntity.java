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
import edu.harvard.med.screensaver.model.libraries.WellVolumeAdjustment;

/**
 * An annotation for a containment relationship, e.g., Library => Well. {@link
 * #containingEntityClass() Containers} are
 * expected to have <code>createContained</code> type factory methods for the Contained.
 * <p>
 * For contained classes that have multiple parents, {@link
 * #hasAlternateContainingEntityClass()} is set to true, and {@link
 * #alternateContainingEntityClass()} is set to the alternative container. This container
 * is also expected to have <code>createContained</code> factory methods as well. This
 * implementation is a pretty ugly hack, but we only have one example of a contained
 * entity with two potential containers, {@link WellVolumeAdjustment}, and it is much easier
 * to handle this in a special-case way than otherwise. If there end up being multiple
 * contained entities with multiple containing classes, or a contained entity with more than
 * just two containers, then it would be advised to rewrite this interface to have a single
 * <code>containingEntityClasses</code>, returning
 * <code>Class<? extends AbstractEntity> []</code>.
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
   * @return the class of the containing entity
   */
  Class<? extends AbstractEntity> containingEntityClass();

  /**
   * Return true iff this ContainedEntity has an alternate containingEntityClass. If true,
   * then {@link #alternateContainingEntityClass()} must be assigned a non-default value
   * indicating the alternate class.
   * @return true iff this ContainedEntity has an alternate containingEntityClass
   */
  boolean hasAlternateContainingEntityClass() default false;
  
  /**
   * Return the class of the alternate {@link AbstractEntity} container.
   * @return the class of the alternate containing entity
   */
  Class<? extends AbstractEntity> alternateContainingEntityClass() default AbstractEntity.class;
}

