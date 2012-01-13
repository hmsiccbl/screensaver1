// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.cherrypicks;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.libraries.PlateType;

@Entity
@DiscriminatorValue("LegacyCherryPickAssayPlate")
@org.hibernate.annotations.Proxy
@edu.harvard.med.screensaver.model.annotations.ContainedEntity(containingEntityClass=CherryPickRequest.class)
public class LegacyCherryPickAssayPlate extends CherryPickAssayPlate
{

  // private static data

  private static final long serialVersionUID = 1L;
  private static Logger log = Logger.getLogger(LegacyCherryPickAssayPlate.class);


  // private instance datum
  
  private String _legacyPlateName;


  // public instance methods

  @Override
  public Object clone() 
  {
    return getCherryPickRequest().createLegacyCherryPickAssayPlate(
      getPlateOrdinal(), 
      getAttemptOrdinal() + 1,
      getAssayPlateType(),
      getLegacyPlateName());
  }

  @Transient
  @Override
  public String getName()
  {
    return _legacyPlateName;
  }
  
  /**
   * Get the legacy plate name.
   * @return the legacy plate name
   */
  @org.hibernate.annotations.Type(type="text")
  public String getLegacyPlateName()
  {
    return _legacyPlateName;
  }

  /**
   * Set the legacy plate name.
   * @param legacyPlateName the new legacy plate name
   */
  public void setLegacyPlateName(String legacyPlateName)
  {
    _legacyPlateName = legacyPlateName;
  }

  
  // package constructor

  /**
   * Construct an initialized <code>LegacyCherryPickAssayPlate</code>. Intended only for use
   * by {@link CherryPickRequest}.
   * @param cherryPickRequest the cherry pick request
   * @param plateOrdinal the plate ordinal
   * @param attemptOrdinal the attempt ordinal
   * @param plateType the plate type
   * @param legacyPlateName the legacy plate name
   */
  LegacyCherryPickAssayPlate(
    CherryPickRequest cherryPickRequest,
    Integer plateOrdinal,
    Integer attemptOrdinal,
    PlateType plateType,
    String legacyPlateName)
  {
    super(cherryPickRequest, plateOrdinal, attemptOrdinal, plateType);
    _legacyPlateName = legacyPlateName;
  }

  
  // protected constructor

  /**
   * Construct an uninitialized <code>LegacyCherryPickAssayPlate</code>.
   * @motivation for hibernate and CGLIB
   */
  protected LegacyCherryPickAssayPlate() {}
}

