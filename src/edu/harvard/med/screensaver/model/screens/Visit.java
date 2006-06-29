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


/**
 * A Hibernate entity bean representing a visit.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @hibernate.class lazy="false"
 */
public class Visit extends AbstractEntity
{
  
  // static fields

  private static final Logger log = Logger.getLogger(Visit.class);
  private static final long serialVersionUID = 0L;


  // instance fields

  private Integer _visitId;
  private Integer _version;
  private boolean _isCherryPickVisit;
  private Date _dateCreated;
  private Date _visitDate;
  private VisitType _visitType;
  private String _abaseTestsetId;
  private String _comments;


  // public constructor

  /**
   * Constructs an initialized <code>Visit</code> object.
   *
   * @param isCherryPickVisit the is cherry pick visit
   * @param dateCreated the date created
   * @param visitDate the visit date
   * @param visitType the visit type
   */
  public Visit(
    boolean isCherryPickVisit,
    Date dateCreated,
    Date visitDate,
    VisitType visitType)
  {
    // TODO: verify the order of assignments here is okay
    _isCherryPickVisit = isCherryPickVisit;
    _dateCreated = truncateDate(dateCreated);
    _visitDate = truncateDate(visitDate);
    _visitType = visitType;
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
   * Get the is cherry pick visit.
   *
   * @return the is cherry pick visit
   * @hibernate.property
   *   not-null="true"
   */
  public boolean getIsCherryPickVisit()
  {
    return _isCherryPickVisit;
  }

  /**
   * Set the is cherry pick visit.
   *
   * @param isCherryPickVisit the new is cherry pick visit
   */
  public void setIsCherryPickVisit(boolean isCherryPickVisit)
  {
    _isCherryPickVisit = isCherryPickVisit;
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
   * @hibernate.property
   *   not-null="true"
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
    _visitDate = truncateDate(visitDate);
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


  // protected methods

  @Override
  protected Object getBusinessKey()
  {
    // TODO: assure changes to business key update relationships whose other side is many
    return getComments();
  }


  // package methods


  // private constructor

  /**
   * Construct an uninitialized <code>Visit</code> object.
   *
   * @motivation for hibernate
   */
  protected Visit() {}


  // private methods

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
}
