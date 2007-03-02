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
import java.util.Set;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.AbstractEntity;


/**
 * A Hibernate entity bean representing a lab affiliation.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @hibernate.class lazy="false"
 */
public class LabAffiliation extends AbstractEntity
{
  
  // static fields

  private static final Logger log = Logger.getLogger(LabAffiliation.class);
  private static final long serialVersionUID = 0L;


  // instance fields

  private Integer _version;
  private Set<ScreeningRoomUser> _screeningRoomUsers;
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
  public String getAffiliationName()
  {
    return _affiliationName;
  }

  /**
   * Set the affiliation name.
   *
   * @param affiliationName the new affiliation name
   */
  public void setAffiliationName(String affiliationName)
  {
    _affiliationName = affiliationName;
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
  private LabAffiliation() {}


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
   * Get the screening room users with this affiliation.
   * 
   * @return the screening room users with this affiliation
   * @hibernate.set
   *   inverse="true"
   * @hibernate.collection-key
   *   column="lab_affiliation_id"
   * @hibernate.collection-one-to-many
   *   class="edu.harvard.med.screensaver.model.users.ScreeningRoomUser"
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  private Set<ScreeningRoomUser> getHbnScreeningRoomUsers()
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
