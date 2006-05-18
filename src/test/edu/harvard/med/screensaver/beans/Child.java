// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.beans;

/**
 * @hibernate.class
 */
public class Child
{
  private Parent _parent;
  private Integer _id;
  private String _name;
  
  public Child() {
  }

  public Child(String name) {
    _name = name;
  }

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
  
  public void addToParent(Parent parent) {
    setParent(parent);
    parent.getChildren().add(this);
  }
  
  public void setParent(Parent parent) {
    _parent = parent;

    // NOTE: Cannot make the following call, as it causes
    // a Hibernate LazyInitializationException, as we're trying
    // to update the PersisentSet (of children) in our Parent class.
    // For some reason, this is not possible WHILE HIBERNATE IS LOADING
    // THIS OBJECTED (i.e., calling its setter methods).
    // Calling addToParent() after the loaded object is returned our
    // application code is OKAY.

    // parent.getChildren().add(this)
  }

  
  /**
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.beans.Parent"
   *   column="parent_id"
   *   not-null="true"
   */
  public Parent getParent() {
    return _parent;
  }

  /**
   * @hibernate.property
   *   type="java.lang.String"
   *   not-null="true"
   * @return
   */
  public String getName() {
    return _name;
  }

  public void setName(String name) {
    _name = name;
  }
  
  @Override
  public int hashCode()
  {
    return getName().hashCode();
  }
  
  @Override
  public boolean equals(Object other)
  {
    return getName().equals(((Child) other).getName());
  }
}
