// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.io.Serializable;


/**
 * An abstract superclass for the entity beans in the data model.
 * 
 * <p>
 * 
 * Conventions for implementing classes:
 * 
 * <ul>
 *   <li>
 *     exactly one public constructor with parameters for all not-null
 *     properties.
 *   </li>
 *   <li>
 *     public constructors have javadoc annotations for all the parameters,
 *     in the correct order, for ease of use of constructor in Eclipse
 *   </li>
 *   <li>
 *     if the public constructor does not have zero parameters, then there
 *     must be a zero parameter constructor for Hibernate.
 *   </li>
 *   <li>
 *     we try to put the properties in the same order in these differing
 *     contexts:
 *     <ul>
 *       <li>
 *         instance fields
 *       </li>
 *       <li>
 *         constructor parameters
 *       </li>
 *       <li>
 *         public accessors
 *       </li>
 *       <li>
 *         private Hibernate accessors
 *       </li>
 *     </ul>
 *   </li>
 *   <li>
 *     lots more that just hasn't been recorded yet...
 *   </li>
 * </ul>
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public abstract class AbstractEntity implements Serializable
{

  /**
   * Return the business key for the entity.
   * @return the business key
   */
  abstract protected Object getBusinessKey();
  
  
  // method overrides from Object
  
  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object object) {
    if (! getClass().equals(object.getClass())) {
      return false;
    }
    AbstractEntity that = (AbstractEntity) object;
    return getBusinessKey().equals(that.getBusinessKey());
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return getBusinessKey().hashCode();
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "(" + getBusinessKey().toString() + ")";
  }
}
