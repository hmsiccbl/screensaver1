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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a persistent property that is a collection of elements. Provides custom extensions
 * for org.hibernate.annotations.CollectionOfElements.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CollectionOfElements {
  
  /**
   * The expected initial cardinality of the set. Used by unit tests to test that the initial
   * collection is the right size, and also to test that adding an element to a collection has
   * the desired result.
   * @return the initial cardinality of the collection of elements
   */
  int initialCardinality() default 0;
}

