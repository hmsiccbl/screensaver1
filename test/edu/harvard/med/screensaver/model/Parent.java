// $HeadURL:svn+ssh://orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/test/edu/harvard/med/screensaver/model/Parent.java $
// $Id:Parent.java 127 2006-05-26 20:43:13Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @hibernate.class
 *   lazy="false"
 */
public class Parent extends AbstractEntity
{

  // static fields
  
  private static final long serialVersionUID = -7056017538369965851L;
  
  
  // instance fields
  
  private Set<Child> _children = new HashSet<Child>();
  private Integer _id;
  
  
  // constructor and instance methods
  
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
   *   class="edu.harvard.med.screensaver.model.Child"
   */  
  private Set<Child> getChildrenHibernate() {
    return _children;
  }

  private void setChildrenHibernate(Set<Child> children) {
    _children = children;
  }
  
  public Set<Child> getChildren() {
    return Collections.unmodifiableSet(_children);
  }

//  protected void setChildren(Set<Child> children) {
//    _children = children;
//  }

  @Override
  public boolean equals(Object other)
  {
    return _id.equals(((Parent) other).getId());
  }
}
