// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.users;


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

  private Integer _labAffiliationId;
  private Integer _version;
  private ScreeningRoomUser _screeningRoomUser;
  private String _affiliationName;
  private AffiliationCategory _affiliationCategory;


  // public constructor

  /**
   * Constructs an initialized <code>LabAffiliation</code> object.
   *
   * @param screeningRoomUser the screening room user
   * @param affiliationName the affiliation name
   * @param affiliationCategory the affiliation category
   */
  public LabAffiliation(
    ScreeningRoomUser screeningRoomUser,
    String affiliationName,
    AffiliationCategory affiliationCategory)
  {
    // TODO: verify the order of assignments here is okay
    _screeningRoomUser = screeningRoomUser;
    _affiliationName = affiliationName;
    _affiliationCategory = affiliationCategory;
  }


  // public methods

  @Override
  public Integer getEntityId()
  {
    return getLabAffiliationId();
  }

  /**
   * Get the id for the lab affiliation.
   *
   * @return the id for the lab affiliation
   * @hibernate.id generator-class="sequence"
   * @hibernate.generator-param name="sequence" value="lab_affiliation_id_seq"
   */
  public Integer getLabAffiliationId()
  {
    return _labAffiliationId;
  }

  /**
   * Get the screening room user.
   *
   * @return the screening room user
   */
  public ScreeningRoomUser getScreeningRoomUser()
  {
    return _screeningRoomUser;
  }

  /**
   * Set the screening room user.
   *
   * @param screeningRoomUser the new screening room user
   */
  public void setScreeningRoomUser(ScreeningRoomUser screeningRoomUser)
  {
    _screeningRoomUser = screeningRoomUser;
    screeningRoomUser.setHbnLabAffiliation(this);
  }

  /**
   * Get the affiliation name.
   *
   * @return the affiliation name
   * @hibernate.property
   *   type="text"
   *   not-null="true"
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

  /**
   * A business key class for the well.
   */
  private class BusinessKey
  {

  /**
   * Get the screening room user.
   *
   * @return the screening room user
   */
  public ScreeningRoomUser getScreeningRoomUser()
  {
    return _screeningRoomUser;
  }

  /**
   * Get the affiliation name.
   *
   * @return the affiliation name
   */
  public String getAffiliationName()
  {
    return _affiliationName;
  }

    @Override
    public boolean equals(Object object)
    {
      if (! (object instanceof BusinessKey)) {
        return false;
      }
      BusinessKey that = (BusinessKey) object;
      return
        getScreeningRoomUser().equals(that.getScreeningRoomUser()) &&
        getAffiliationName().equals(that.getAffiliationName());
    }

    @Override
    public int hashCode()
    {
      return
        getScreeningRoomUser().hashCode() +
        getAffiliationName().hashCode();
    }

    @Override
    public String toString()
    {
      return getScreeningRoomUser() + ":" + getAffiliationName();
    }
  }

  @Override
  protected Object getBusinessKey()
  {
    // TODO: assure changes to business key update relationships whose other side is many
    return new BusinessKey();
  }


  // package methods

  /**
   * Set the screening room user.
   * Throw a NullPointerException when the screening room user is null.
   *
   * @param screeningRoomUser the new screening room user
   * @throws NullPointerException when the screening room user is null
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  void setHbnScreeningRoomUser(ScreeningRoomUser screeningRoomUser)
  {
    if (screeningRoomUser == null) {
      throw new NullPointerException();
    }
    _screeningRoomUser = screeningRoomUser;
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
  private void setLabAffiliationId(Integer labAffiliationId) {
    _labAffiliationId = labAffiliationId;
  }

  /**
   * Get the version for the lab affiliation.
   *
   * @return the version for the lab affiliation
   * @motivation for hibernate
   * @hibernate.version
   */
  private Integer getVersion() {
    return _version;
  }

  /**
   * Set the version for the lab affiliation.
   *
   * @param version the new version for the lab affiliation
   * @motivation for hibernate
   */
  private void setVersion(Integer version) {
    _version = version;
  }

  /**
   * Get the screening room user.
   *
   * @return the screening room user
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.model.users.ScreeningRoomUser"
   *   column="screening_room_user_id"
   *   not-null="true"
   *   foreign-key="fk_lab_affiliation_to_screening_room_user"
   *   cascade="save-update"
   * @motivation for hibernate
   */
  private ScreeningRoomUser getHbnScreeningRoomUser()
  {
    return _screeningRoomUser;
  }
}
