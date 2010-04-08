// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
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
   * The "root" name of the method used to add and remove values to this
   * collection. This annotation value is optional, since it is valid to modify
   * the collection by calling methods on the collection itself (Collection.add,
   * Collection.addAll, Map.put, etc.). It is also not necessary to specify this
   * method if the add and remove method names can be inferred by removing the
   * trailing 's' from the collection property name, and prepending 'add'
   * or'remove'; in this case the model unit tests will automatically find the
   * add method and test it.
   */
  String singularPropertyName() default "";

  /**
   * Indicates whether this collection cannot be tested via normal add/remove
   * methods.
   */
  boolean hasNonconventionalMutation() default false;
}

