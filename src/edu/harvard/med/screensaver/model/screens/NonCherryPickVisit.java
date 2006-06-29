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
 * A Hibernate entity bean representing a non-cherry pick visit.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @hibernate.subclass
 *   lazy="false"
 *   descriminator-value="true"
 */
public class NonCherryPickVisit extends Visit
{
  
  // static fields

  private static final Logger log = Logger.getLogger(NonCherryPickVisit.class);
  private static final long serialVersionUID = 0L;


  // instance fields

  private Integer _nonCherryPickVisitId;
  private Integer _version;
  private Integer _numberOfReplicates;
  private String _volumeOfCompoundTransferred;
  private AssayProtocolType _assayProtocolType;


  // public constructor

  /**
   * Constructs an initialized <code>NonCherryPickVisit</code> object.
   *
   * @param assayProtocolType the assay protocol type
   */
  public NonCherryPickVisit(
    Date dateCreated,
    Date visitDate,
    VisitType visitType)
  {
    super(false, dateCreated, visitDate, visitType);
  }


  // public methods

  @Override
  public Integer getEntityId()
  {
    return getNonCherryPickVisitId();
  }

  /**
   * Get the id for the non-cherry pick visit.
   *
   * @return the id for the non-cherry pick visit
   * @hibernate.id generator-class="sequence"
   * @hibernate.generator-param name="sequence" value="non_cherry_pick_visit_id_seq"
   */
  public Integer getNonCherryPickVisitId()
  {
    return _nonCherryPickVisitId;
  }

  /**
   * Get the number of replicates.
   *
   * @return the number of replicates
   * @hibernate.property
   */
  public Integer getNumberOfReplicates()
  {
    return _numberOfReplicates;
  }

  /**
   * Set the number of replicates.
   *
   * @param numberOfReplicates the new number of replicates
   */
  public void setNumberOfReplicates(Integer numberOfReplicates)
  {
    _numberOfReplicates = numberOfReplicates;
  }

  /**
   * Get the volume of compound transferred.
   *
   * @return the volume of compound transferred
   * @hibernate.property
   *   type="text"
   */
  public String getVolumeOfCompoundTransferred()
  {
    return _volumeOfCompoundTransferred;
  }

  /**
   * Set the volume of compound transferred.
   *
   * @param volumeOfCompoundTransferred the new volume of compound transferred
   */
  public void setVolumeOfCompoundTransferred(String volumeOfCompoundTransferred)
  {
    _volumeOfCompoundTransferred = volumeOfCompoundTransferred;
  }

  /**
   * Get the assay protocol type.
   *
   * @return the assay protocol type
   * @hibernate.property
   *   type="edu.harvard.med.screensaver.model.screens.AssayProtocolType$UserType"
   */
  public AssayProtocolType getAssayProtocolType()
  {
    return _assayProtocolType;
  }

  /**
   * Set the assay protocol type.
   *
   * @param assayProtocolType the new assay protocol type
   */
  public void setAssayProtocolType(AssayProtocolType assayProtocolType)
  {
    _assayProtocolType = assayProtocolType;
  }


  // package methods


  // private constructor

  /**
   * Construct an uninitialized <code>NonCherryPickVisit</code> object.
   *
   * @motivation for hibernate
   */
  private NonCherryPickVisit() {}


  // private methods

  /**
   * Set the id for the non-cherry pick visit.
   *
   * @param nonCherryPickVisitId the new id for the non-cherry pick visit
   * @motivation for hibernate
   */
  private void setNonCherryPickVisitId(Integer nonCherryPickVisitId) {
    _nonCherryPickVisitId = nonCherryPickVisitId;
  }

  /**
   * Get the version for the non-cherry pick visit.
   *
   * @return the version for the non-cherry pick visit
   * @motivation for hibernate
   * @hibernate.version
   */
  private Integer getVersion() {
    return _version;
  }

  /**
   * Set the version for the non-cherry pick visit.
   *
   * @param version the new version for the non-cherry pick visit
   * @motivation for hibernate
   */
  private void setVersion(Integer version) {
    _version = version;
  }
}
