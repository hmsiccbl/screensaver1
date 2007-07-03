// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

import org.apache.log4j.Logger;

/**
 * TODO: javadoc
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * 
 * @hibernate.joined-subclass table="screening" lazy="true"
 * @hibernate.joined-subclass-key column="activity_id"
 */
public abstract class Screening extends ScreeningRoomActivity
{
  // static members

  private static final long serialVersionUID = 1L;
  
  private static Logger log = Logger.getLogger(Screening.class);
  

  // instance data members

  private String _assayProtocol;
  private Date _assayProtocolLastModifiedDate;
  private AssayProtocolType _assayProtocolType;
  private Integer _numberOfReplicates;
  private BigDecimal _estimatedFinalScreenConcentrationInMoles;

  
  // public constructors and methods

  public Screening(Screen screen,
                   ScreeningRoomUser performedBy,
                   Date dateCreated,
                   Date dateOfActivity) throws DuplicateEntityException
  {
    super(screen, performedBy, dateCreated, dateOfActivity);
  }

  /**
   * Get the assay protocol.
   * 
   * @return the assay protocol
   * @hibernate.property
   *   type="text"
   */
  public String getAssayProtocol()
  {
    return _assayProtocol;
  }
  
  /**
   * Set the assay protocol.
   * 
   * @param assayProtocol the new assay protocol
   */
  public void setAssayProtocol(String assayProtocol)
  {
    _assayProtocol = assayProtocol;
  }

  /**
   * Get the date the assay protocol was last modified.
   * @return the date the assay protocol was last modified
   * @hibernate.property
   */
  public Date getAssayProtocolLastModifiedDate()
  {
    return _assayProtocolLastModifiedDate;
  }

  /**
   * Set the date the assay protocol was last modified
   * @param assayProtocolLastModifiedDate the new date the assay protocol was last modified
   */
  public void setAssayProtocolLastModifiedDate(Date assayProtocolLastModifiedDate)
  {
    _assayProtocolLastModifiedDate = truncateDate(assayProtocolLastModifiedDate);
  }

  /**
   * Get the assay protocol type
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
   * @param assayProtocolType the new assay protocol type
   */
  public void setAssayProtocolType(AssayProtocolType assayProtocolType)
  {
    _assayProtocolType = assayProtocolType;
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
   * Get the estimated final screen concentration, in Moles.
   * @return the estimated final screen concentration, in Moles
   * @hibernate.property type="big_decimal"
   */
  public BigDecimal getEstimatedFinalScreenConcentrationInMoles()
  {
    return _estimatedFinalScreenConcentrationInMoles;
  }

  /**
   * Set the estimated final screen concentration, in Moles.
   * @param estimatedFinalScreenConcentrationInMoles the new estimated final screen concentration,
   * in Moles.
   */
  public void setEstimatedFinalScreenConcentrationInMoles(
    BigDecimal estimatedFinalScreenConcentrationInMoles)
  {
    if (estimatedFinalScreenConcentrationInMoles == null) {
      _estimatedFinalScreenConcentrationInMoles = null;
    }
    else {
      _estimatedFinalScreenConcentrationInMoles = estimatedFinalScreenConcentrationInMoles.setScale(Well.VOLUME_SCALE, RoundingMode.HALF_UP);
    }
  }


  // private methods

  /**
   * @motivation for Hibernate and subclasses
   */
  protected Screening()
  {
  }
}

