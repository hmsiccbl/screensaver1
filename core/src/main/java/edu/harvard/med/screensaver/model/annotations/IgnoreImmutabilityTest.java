// $HeadURL: http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/serickson/3200/core/src/main/java/edu/harvard/med/screensaver/model/annotations/Derived.java $
// $Id: Derived.java 6946 2012-01-13 18:24:30Z seanderickson1 $
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
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

import org.hibernate.annotations.Immutable;


/**
 * Instruct the testing framework to ignore setter violations for Immutable classes.<br/>
 * This should be used with care and is for convenience only.<br/>  
 * Note that if a class is marked as {@link Immutable} then the testing framework will fail for public setter methods; however
 * Hibernate will still require private setter methods.  Additionally, for creation, all values will have to be set either in the constructor, in "non-conventional" (i.e. not following the bean pattern) 
 * setter methods, or by exposing the class members as public.  If this is overly inconvenient, then this class may be used to override the testing framework behavior.
 * 
 * @author sde4
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface IgnoreImmutabilityTest {
}
