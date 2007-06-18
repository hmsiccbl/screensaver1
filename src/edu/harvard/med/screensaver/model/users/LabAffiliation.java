// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.users;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.EntityIdProperty;

import org.apache.log4j.Logger;


/**
 * A Hibernate entity bean representing a lab affiliation.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @hibernate.class lazy="true"
 */
public class LabAffiliation extends AbstractEntity
{
  
  // static fields

  private static final Logger log = Logger.getLogger(LabAffiliation.class);
  private static final long serialVersionUID = 0L;


  // instance fields

  private Integer _version;
  private Set<ScreeningRoomUser> _screeningRoomUsers = new HashSet<ScreeningRoomUser>();
  private String _affiliationName;
  private AffiliationCategory _affiliationCategory;


  // public constructor

  /**
   * Constructs an initialized <code>LabAffiliation</code> object.
   *
   * @param affiliationName the affiliation name
   * @param affiliationCategory the affiliation category
   */
  public LabAffiliation(String affiliationName, AffiliationCategory affiliationCategory)
  {
    _affiliationName = affiliationName;
    _affiliationCategory = affiliationCategory;
  }


  // public methods

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }
  
  @Override
  public String getEntityId()
  {
    return getAffiliationName();
  }

  /**
   * Get an unmodifiable copy of the set of screening room users with this lab affiliation.
   * 
   * @return the screening room users with this lab affiliation
   */
  public Set<ScreeningRoomUser> getScreeningRoomUsers()
  {
    return Collections.unmodifiableSet(_screeningRoomUsers);
  }
  
  public boolean addScreeningRoomUser(ScreeningRoomUser screeningRoomUser)
  {
    assert screeningRoomUser.isLabHead() : "only lab heads (since they represent the lab as a whole) can have a lab affiliation";
    boolean result = _screeningRoomUsers.add(screeningRoomUser);
    if (!this.equals(screeningRoomUser.getLabAffiliation())) {
      screeningRoomUser.setLabAffiliation(this);
    }
    return result;
  }
  
  public boolean removeScreeningRoomUser(ScreeningRoomUser screeningRoomUser)
  {
    assert !(_screeningRoomUsers.contains(screeningRoomUser) ^ this.equals(screeningRoomUser.getLabAffiliation())) :
      "asymmetric lab affiliation/screening room user encountered";
    boolean result = _screeningRoomUsers.remove(screeningRoomUser);
    if (result) {
      screeningRoomUser.setLabAffiliation(null);
    }
    return result;
  }
  
  /**
   * Get the id for the lab affiliation.
   *
   * @return the id for the lab affiliation
   * @hibernate.id
   *   generator-class="assigned"
   *   length="2047"
   */
  public String getLabAffiliationId()
  {
    return getAffiliationName();
  }

  /**
   * Get the affiliation name.
   *
   * @return the affiliation name
   * @hibernate.property
   *   type="text"
   *   not-null="true"
   *   unique="true"
   */
  @EntityIdProperty
  public String getAffiliationName()
  {
    return _affiliationName;
  }

  /**
   * Get the affiliation category.
   *
   * @return the affiliation category
   * @hibernate.property
   *   type="edu.harvard.med.screensaver.model.users.AffiliationCategory$UserType"
   *   not-null="true"
   */
  public AffiliationCategory getAffiliationCategory()
  {
    return _affiliationCategory;
  }

  /**
   * Set the affiliation category.
   *
   * @param affiliationCategory the new affiliation category
   */
  public void setAffiliationCategory(AffiliationCategory affiliationCategory)
  {
    _affiliationCategory = affiliationCategory;
  }


  // protected methods

  @Override
  protected Object getBusinessKey()
  {
    return _affiliationName;
  }


  // private constructor

  /**
   * Construct an uninitialized <code>LabAffiliation</code> object.
   *
   * @motivation for hibernate
   */
  LabAffiliation() {}


  // private methods

  /**
   * Set the id for the lab affiliation.
   *
   * @param labAffiliationId the new id for the lab affiliation
   * @motivation for hibernate
   */
  private void setLabAffiliationId(String labAffiliationId)
  {
  }

  /**
   * Get the version for the lab affiliation.
   *
   * @return the version for the lab affiliation
   * @motivation for hibernate
   * @hibernate.version
   */
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * Set the version for the lab affiliation.
   *
   * @param version the new version for the lab affiliation
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }
  
  /**
   * Set the affiliation name.
   *
   * @param affiliationName the new affiliation name
   */
  private void setAffiliationName(String affiliationName)
  {
    _affiliationName = affiliationName;
  }

  /**
   * Get the screening room users with this affiliation.
   * 
   * @return the screening room users with this affiliation
   * @hibernate.set inverse="true" cascade="save-update" lazy="true"
   * @hibernate.collection-key column="lab_affiliation_id"
   * @hibernate.collection-one-to-many class="edu.harvard.med.screensaver.model.users.ScreeningRoomUser"
   * @motivation for hibernate and maintenance of bi-directional relationships;
   *             public access since relationship is cross-package
   */
  public Set<ScreeningRoomUser> getHbnScreeningRoomUsers()
  {
    return _screeningRoomUsers;
  }
  
  /**
   * Set the screening room users with this lab affiliation.
   * 
   * @param screeningRoomUsers
   * @motivation for hibernate
   */
  private void setHbnScreeningRoomUsers(Set<ScreeningRoomUser> screeningRoomUsers)
  {
    _screeningRoomUsers = screeningRoomUsers;
  }
}
