// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.beans;

import java.util.HashSet;
import java.util.Set;

/**
 * @hibernate.class
 */
public class Parent
{
  private Set<Child> _children = new HashSet<Child>();
  private Integer _id;
  
  public void setId(Integer id) {
    _id = id;
  }
  
  /**
   * @hibernate.id
   *   generator-class="sequence"
   * @hibernate.generator-param
   *   name="sequence"
   *   value="parent_id_seq"
   */  
  public Integer getId() {
    return _id;
  }
  
  public void addChild(Child c) {
    _children.add(c);
    c.setParent(this);
  }
  
  /**
   * @hibernate.set
   *   cascade="all-delete-orphan"
   *   inverse="true"
   * @hibernate.collection-key
   *   column="parent_id"
   * @hibernate.collection-one-to-many
   *   class="edu.harvard.med.screensaver.beans.Child"
   */  
  public Set<Child> getChildren() {
    return _children;
  }

  protected void setChildren(Set<Child> children) {
    _children = children;
  }
  
  @Override
  public boolean equals(Object other)
  {
    return _id.equals(((Parent) other).getId());
  }
}
