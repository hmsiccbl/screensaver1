// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import java.util.Date;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;


/**
 * A Hibernate entity bean representing a visit.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @hibernate.class lazy="false"
 * @hibernate.discriminator
 *   column="is_cherry_pick_visit"
 *   type="boolean"
 *   not-null="true"
 */
abstract public class Visit extends AbstractEntity
{
  
  // static fields

  private static final Logger log = Logger.getLogger(Visit.class);
  private static final long serialVersionUID = 0L;


  // instance fields

  private Integer _visitId;
  private Integer _version;
  private Screen _screen;
  private ScreeningRoomUser _performedBy;
  private Date _dateCreated;
  private Date _visitDate;
  private VisitType _visitType;
  private String _abaseTestsetId;
  private String _comments;


  // public constructor

  /**
   * Constructs an initialized <code>Visit</code> object.
   *
   * @param screen the screen
   * @param performedBy the user that performed the visit
   * @param dateCreated the date created
   * @param visitDate the visit date
   * @param visitType the visit type
   */
  public Visit(
    Screen screen,
    ScreeningRoomUser performedBy,
    Date dateCreated,
    Date visitDate,
    VisitType visitType)
  {
    if (screen == null || performedBy == null) {
      throw new NullPointerException();
    }
    _screen = screen;
    _performedBy = performedBy;
    _dateCreated = truncateDate(dateCreated);
    _visitDate = truncateDate(visitDate);
    _visitType = visitType;
    _screen.getHbnVisits().add(this);
    _performedBy.getHbnVisitsPerformed().add(this);
  }


  // public methods

  @Override
  public Integer getEntityId()
  {
    return getVisitId();
  }

  /**
   * Get the id for the visit.
   *
   * @return the id for the visit
   * @hibernate.id generator-class="sequence"
   * @hibernate.generator-param name="sequence" value="visit_id_seq"
   */
  public Integer getVisitId()
  {
    return _visitId;
  }

  /**
   * Get the screen.
   *
   * @return the screen
   */
  public Screen getScreen()
  {
    return _screen;
  }

  /**
   * Set the screen.
   *
   * @param screen the new screen
   */
  public void setScreen(Screen screen)
  {
    if (screen == null) {
      throw new NullPointerException();
    }
    _screen.getHbnVisits().remove(this);
    _performedBy.getHbnVisitsPerformed().remove(this);
    _screen = screen;
    _screen.getHbnVisits().add(this);
    _performedBy.getHbnVisitsPerformed().add(this);
  }

  /**
   * Get the user that performed the visit.
   *
   * @return the user that performed the visit
   */
  public ScreeningRoomUser getPerformedBy()
  {
    return _performedBy;
  }

  /**
   * Set the user that performed the visit.
   *
   * @param performedBy the new user that performed the visit
   */
  public void setPerformedBy(ScreeningRoomUser performedBy)
  {
    if (performedBy == null) {
      throw new NullPointerException();
    }
    _screen.getHbnVisits().remove(this);
    _performedBy.getHbnVisitsPerformed().remove(this);
    _performedBy = performedBy;
    _screen.getHbnVisits().add(this);
    _performedBy.getHbnVisitsPerformed().add(this);
  }

  /**
   * Get the date created.
   *
   * @return the date created
   * @hibernate.property
   *   not-null="true"
   */
  public Date getDateCreated()
  {
    return _dateCreated;
  }

  /**
   * Set the date created.
   *
   * @param dateCreated the new date created
   */
  public void setDateCreated(Date dateCreated)
  {
    _dateCreated = truncateDate(dateCreated);
  }

  /**
   * Get the visit date.
   *
   * @return the visit date
   */
  public Date getVisitDate()
  {
    return _visitDate;
  }

  /**
   * Set the visit date.
   *
   * @param visitDate the new visit date
   */
  public void setVisitDate(Date visitDate)
  {
    _screen.getHbnVisits().remove(this);
    _performedBy.getHbnVisitsPerformed().remove(this);
    _visitDate = truncateDate(visitDate);
    _screen.getHbnVisits().add(this);
    _performedBy.getHbnVisitsPerformed().add(this);
  }

  /**
   * Get the visit type.
   *
   * @return the visit type
   * @hibernate.property
   *   type="edu.harvard.med.screensaver.model.screens.VisitType$UserType"
   *   not-null="true"
   */
  public VisitType getVisitType()
  {
    return _visitType;
  }

  /**
   * Set the visit type.
   *
   * @param visitType the new visit type
   */
  public void setVisitType(VisitType visitType)
  {
    _visitType = visitType;
  }

  /**
   * Get the abase testset id.
   *
   * @return the abase testset id
   * @hibernate.property
   *   type="text"
   */
  public String getAbaseTestsetId()
  {
    return _abaseTestsetId;
  }

  /**
   * Set the abase testset id.
   *
   * @param abaseTestsetId the new abase testset id
   */
  public void setAbaseTestsetId(String abaseTestsetId)
  {
    _abaseTestsetId = abaseTestsetId;
  }

  /**
   * Get the comments.
   *
   * @return the comments
   * @hibernate.property
   *   type="text"
   */
  public String getComments()
  {
    return _comments;
  }

  /**
   * Set the comments.
   *
   * @param comments the new comments
   */
  public void setComments(String comments)
  {
    _comments = comments;
  }

  /**
   * Set the user that performed the visit.
   * Throw a NullPointerException when the user that performed the visit is null.
   *
   * @param performedBy the new user that performed the visit
   * @throws NullPointerException when the user that performed the visit is null
   * @motivation for hibernate and maintenance of bi-directional relationships
   * this method is public only because the bi-directional relationship
   * is cross-package.
   */
  public void setHbnPerformedBy(ScreeningRoomUser performedBy)
  {
    if (performedBy == null) {
      throw new NullPointerException();
    }
    _performedBy = performedBy;
  }

  
  // protected constructor
  
  /**
   * Construct an uninitialized <code>Visit</code> object.
   *
   * @motivation for hibernate
   */
  protected Visit() {}


  // protected methods
  
  /**
   * A business key class for the well.
   */
  private class BusinessKey
  {
    
    /**
     * Get the screen.
     *
     * @return the screen
     */
    public Screen getScreen()
    {
      return _screen;
    }
    
    /**
     * Get the user that performed the visit.
     *
     * @return the user that performed the visit
     */
    public ScreeningRoomUser getPerformedBy()
    {
      return _performedBy;
    }
    
    /**
     * Get the visit date.
     *
     * @return the visit date
     */
    public Date getVisitDate()
    {
      return _visitDate;
    }
    
    @Override
    public boolean equals(Object object)
    {
      if (! (object instanceof BusinessKey)) {
        return false;
      }
      BusinessKey that = (BusinessKey) object;
      return
        getScreen().equals(that.getScreen()) &&
        getPerformedBy().equals(that.getPerformedBy()) &&
        getVisitDate().equals(that.getVisitDate());
    }

    @Override
    public int hashCode()
    {
      return
        getScreen().hashCode() +
        getPerformedBy().hashCode() +
        getVisitDate().hashCode();
    }

    @Override
    public String toString()
    {
      return getScreen() + ":" + getPerformedBy() + ":" + getVisitDate();
    }
  }

  @Override
  protected Object getBusinessKey()
  {
    return new BusinessKey();
  }


  // package methods

  /**
   * Set the screen.
   * Throw a NullPointerException when the screen is null.
   *
   * @param screen the new screen
   * @throws NullPointerException when the screen is null
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  void setHbnScreen(Screen screen)
  {
    if (screen == null) {
      throw new NullPointerException();
    }
    _screen = screen;
  }

  /**
   * Set the id for the visit.
   *
   * @param visitId the new id for the visit
   * @motivation for hibernate
   */
  private void setVisitId(Integer visitId) {
    _visitId = visitId;
  }

  /**
   * Get the version for the visit.
   *
   * @return the version for the visit
   * @motivation for hibernate
   * @hibernate.version
   */
  private Integer getVersion() {
    return _version;
  }

  /**
   * Set the version for the visit.
   *
   * @param version the new version for the visit
   * @motivation for hibernate
   */
  private void setVersion(Integer version) {
    _version = version;
  }

  /**
   * Get the screen.
   *
   * @return the screen
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.model.screens.Screen"
   *   column="screen_id"
   *   not-null="true"
   *   foreign-key="fk_visit_to_screen"
   *   cascade="save-update"
   * @motivation for hibernate
   */
  private Screen getHbnScreen()
  {
    return _screen;
  }

  /**
   * Get the user that performed the visit.
   *
   * @return the user that performed the visit
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.model.users.ScreeningRoomUser"
   *   column="performed_by_id"
   *   not-null="true"
   *   foreign-key="fk_visit_to_performed_by"
   *   cascade="save-update"
   * @motivation for hibernate
   */
  private ScreeningRoomUser getHbnPerformedBy()
  {
    return _performedBy;
  }

  /**
   * Get the visit date.
   *
   * @return the visit date
   * @hibernate.property
   *   column="visit_date"
   *   not-null="true"
   * @motivation for hibernate
   */
  private Date getHbnVisitDate()
  {
    return _visitDate;
  }

  /**
   * Set the visit date.
   *
   * @param visitDate the new visit date
   * @motivation for hibernate
   */
  private void setHbnVisitDate(Date visitDate)
  {
    _visitDate = truncateDate(visitDate);
  }
}
