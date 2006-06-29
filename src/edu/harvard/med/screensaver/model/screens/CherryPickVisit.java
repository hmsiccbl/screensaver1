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


/**
 * A Hibernate entity bean representing a cherry pick visit.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @hibernate.subclass
 *   lazy="false"
 *   descriminator-value="true"
 */
public class CherryPickVisit extends Visit
{
  
  // static fields

  private static final Logger log = Logger.getLogger(CherryPickVisit.class);
  private static final long serialVersionUID = 0L;


  // instance fields

  private Integer _cherryPickVisitId;
  private Integer _version;
  private String _volume;


  // public constructor

  /**
   * Constructs an initialized <code>CherryPickVisit</code> object.
   *
   * 
   */
  public CherryPickVisit(
    Date dateCreated,
    Date visitDate,
    VisitType visitType)
  {
    super(true, dateCreated, visitDate, visitType);
  }


  // public methods

  @Override
  public Integer getEntityId()
  {
    return getCherryPickVisitId();
  }

  /**
   * Get the id for the cherry pick visit.
   *
   * @return the id for the cherry pick visit
   * @hibernate.id generator-class="sequence"
   * @hibernate.generator-param name="sequence" value="cherry_pick_visit_id_seq"
   */
  public Integer getCherryPickVisitId()
  {
    return _cherryPickVisitId;
  }

  /**
   * Get the volume.
   *
   * @return the volume
   * @hibernate.property
   *   column="cpv_volume"
   *   type="text"
   */
  public String getVolume()
  {
    return _volume;
  }

  /**
   * Set the volume.
   *
   * @param volume the new volume
   */
  public void setVolume(String volume)
  {
    _volume = volume;
  }


  // package methods


  // private constructor

  /**
   * Construct an uninitialized <code>CherryPickVisit</code> object.
   *
   * @motivation for hibernate
   */
  private CherryPickVisit() {}


  // private methods

  /**
   * Set the id for the cherry pick visit.
   *
   * @param cherryPickVisitId the new id for the cherry pick visit
   * @motivation for hibernate
   */
  private void setCherryPickVisitId(Integer cherryPickVisitId) {
    _cherryPickVisitId = cherryPickVisitId;
  }

  /**
   * Get the version for the cherry pick visit.
   *
   * @return the version for the cherry pick visit
   * @motivation for hibernate
   * @hibernate.version
   */
  private Integer getVersion() {
    return _version;
  }

  /**
   * Set the version for the cherry pick visit.
   *
   * @param version the new version for the cherry pick visit
   * @motivation for hibernate
   */
  private void setVersion(Integer version) {
    _version = version;
  }
}
