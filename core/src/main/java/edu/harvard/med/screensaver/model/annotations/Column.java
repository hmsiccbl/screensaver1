// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import edu.harvard.med.screensaver.model.AbstractEntity;

/**
 * Annotates a mapped column for a persistent property. Provides custom extensions for
 * javax.persistence.Column.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {

  /**
   * True whenever this property has any kind of non-conventional way to update
   * its value. Used by the unit tests to know not to look for a setter method
   * with the standard naming convention for bean properties. This is not
   * intended to be used for immutable fields - instead, use
   * org.hibernate.annotations.Immutable. If true, it is the developer's
   * responsibility to write a custom unit test should be written for this
   * property.
   * 
   * @return true whenever this property has a non-conventional way to update
   *         its value
   */
  boolean hasNonconventionalSetterMethod() default false;

  /**
   * True whenever this property should not be used to test for entity equivalence in
   * {@link AbstractEntity#isEquivalent(AbstractEntity)}.
   * @return true whenever this property should not be used to test for entity equivalence
   */
  boolean isNotEquivalenceProperty() default false;
}

