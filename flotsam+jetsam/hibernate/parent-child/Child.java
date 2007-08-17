// $HeadURL:svn+ssh://orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/test/edu/harvard/med/screensaver/model/Child.java $
// $Id:Child.java 127 2006-05-26 20:43:13Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

/**
 * @hibernate.class
 *   lazy="false"
 */
public class Child extends AbstractEntity
{
  
  // static fields

  private static final long serialVersionUID = 5164626311748684959L;

  
  // instance fields
  
  private Integer _version;
  private Parent _parent;
  private Integer _childId;
  private String _name;
  
  
  // public constructor and instance methods
  
  /**
   * Constructs an initialized Child object.
   * @param name
   * @param parent
   */
  public Child(String name, Parent parent)
  {
    _name = name;
    setParent(parent);
  }
  
  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.model.AbstractEntity#getEntityId()
   */
  public Integer getEntityId()
  {
    return getChildId();
  }
  
  /**
   * @hibernate.id
   *   generator-class="sequence"
   * @hibernate.generator-param
   *   name="sequence"
   *   value="parent_id_seq"
   */  
  public Integer getChildId()
  {
    return _childId;
  }
  
  public Parent getParent()
  {
    return getHbnParent();
  }
  
  public void setParent(Parent parent)
  {
    setHbnParent(parent);
    parent.getHbnChildren().add(this);
  }
  
  /**
   * @hibernate.property
   *   type="java.lang.String"
   *   not-null="true"
   */
  public String getName()
  {
    return _name;
  }

  public void setName(String name)
  {
    _name = name;
  }
  
  
  // protected getters and setters
  
  void setHbnParent(Parent parent)
  {
    _parent = parent;
  }

  protected Object getBusinessKey()
  {
    return getName();
  }

  // constructor and instance methods
  
  /**
   * Constructs an uninitialized Child object.
   * @motivation for hibernate
   */
  private Child() {}

  private void setChildId(Integer id)
  {
    _childId = id;
  }

  /**
   * Get the version number of the compound.
   * @return     the version number of the compound
   * @motivation for hibernate
   *
   * @hibernate.version
   */
  private Integer getVersion() {
    return _version;
  }

  /**
   * Set the version number of the compound.
   * @param version the new version number for the compound
   * @motivation    for hibernate
   */
  private void setVersion(Integer version) {
    _version = version;
  }

  /**
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.model.Parent"
   *   column="parent_id"
   *   not-null="true"
   *   foreign-key="fk_child_to_parent"
   *   cascade="save-update"
   */
  private Parent getHbnParent()
  {
    return _parent;
  }
}
