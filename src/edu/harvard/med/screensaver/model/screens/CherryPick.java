// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;


import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.AbstractEntity;


/**
 * A Hibernate entity bean representing a cherry pick.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @hibernate.class lazy="false"
 */
public class CherryPick extends AbstractEntity
{
  
  // static fields

  private static final Logger log = Logger.getLogger(CherryPick.class);
  private static final long serialVersionUID = 0L;


  // instance fields

  private Integer _cherryPickId;
  private Integer _version;
  private String _volume;
  private String _plateMap;
  private IsHitConfirmedViaExperimentation _isHitConfirmedViaExperimentation;
  private String _notesOnHitConfirmation;


  // public constructor

  /**
   * Constructs an initialized <code>CherryPick</code> object.
   *
   * @param volume the volume
   */
  public CherryPick(
    String volume)
  {
    // TODO: verify the order of assignments here is okay
    _volume = volume;
  }


  // public methods

  @Override
  public Integer getEntityId()
  {
    return getCherryPickId();
  }

  /**
   * Get the id for the cherry pick.
   *
   * @return the id for the cherry pick
   * @hibernate.id generator-class="sequence"
   * @hibernate.generator-param name="sequence" value="cherry_pick_id_seq"
   */
  public Integer getCherryPickId()
  {
    return _cherryPickId;
  }

  /**
   * Get the volume.
   *
   * @return the volume
   * @hibernate.property
   *   type="text"
   *   not-null="true"
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

  /**
   * Get the plate map.
   *
   * @return the plate map
   * @hibernate.property
   *   type="text"
   */
  public String getPlateMap()
  {
    return _plateMap;
  }

  /**
   * Set the plate map.
   *
   * @param plateMap the new plate map
   */
  public void setPlateMap(String plateMap)
  {
    _plateMap = plateMap;
  }

  /**
   * Get the is hit confirmed via exmperimentation.
   *
   * @return the is hit confirmed via exmperimentation
   * @hibernate.property
   *   type="edu.harvard.med.screensaver.model.screens.IsHitConfirmedViaExperimentation$UserType"
   */
  public IsHitConfirmedViaExperimentation getIsHitConfirmedViaExperimentation()
  {
    return _isHitConfirmedViaExperimentation;
  }

  /**
   * Set the is hit confirmed via exmperimentation.
   *
   * @param isHitConfirmedViaExperimentation the new is hit confirmed via exmperimentation
   */
  public void setIsHitConfirmedViaExperimentation(IsHitConfirmedViaExperimentation isHitConfirmedViaExperimentation)
  {
    _isHitConfirmedViaExperimentation = isHitConfirmedViaExperimentation;
  }

  /**
   * Get the notes on hit confirmation.
   *
   * @return the notes on hit confirmation
   * @hibernate.property
   *   type="text"
   */
  public String getNotesOnHitConfirmation()
  {
    return _notesOnHitConfirmation;
  }

  /**
   * Set the notes on hit confirmation.
   *
   * @param notesOnHitConfirmation the new notes on hit confirmation
   */
  public void setNotesOnHitConfirmation(String notesOnHitConfirmation)
  {
    _notesOnHitConfirmation = notesOnHitConfirmation;
  }


  // protected methods

  @Override
  protected Object getBusinessKey()
  {
    // TODO: assure changes to business key update relationships whose other side is many
    return getVolume();
  }


  // package methods


  // private constructor

  /**
   * Construct an uninitialized <code>CherryPick</code> object.
   *
   * @motivation for hibernate
   */
  private CherryPick() {}


  // private methods

  /**
   * Set the id for the cherry pick.
   *
   * @param cherryPickId the new id for the cherry pick
   * @motivation for hibernate
   */
  private void setCherryPickId(Integer cherryPickId) {
    _cherryPickId = cherryPickId;
  }

  /**
   * Get the version for the cherry pick.
   *
   * @return the version for the cherry pick
   * @motivation for hibernate
   * @hibernate.version
   */
  private Integer getVersion() {
    return _version;
  }

  /**
   * Set the version for the cherry pick.
   *
   * @param version the new version for the cherry pick
   * @motivation for hibernate
   */
  private void setVersion(Integer version) {
    _version = version;
  }
}
