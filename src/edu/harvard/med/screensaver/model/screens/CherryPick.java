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
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.Well;


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
  private CherryPickVisit _cherryPickVisit;
  private Well _well;
  private Copy _copy;
  private RNAiKnockdownConfirmation _rnaiKnockdownConfirmation;
  private String _volume;
  private String _plateMap;
  private IsHitConfirmedViaExperimentation _isHitConfirmedViaExperimentation;
  private String _notesOnHitConfirmation;


  // public constructor

  /**
   * Constructs an initialized <code>CherryPick</code> object.
   *
   * @param cherryPickVisit the cherry pick visit
   * @param well the well
   * @param copy the copy
   * @param volume the volume
   */
  public CherryPick(
    CherryPickVisit cherryPickVisit,
    Well well,
    Copy copy,
    String volume)
  {
    // TODO: verify the order of assignments here is okay
    _cherryPickVisit = cherryPickVisit;
    _well = well;
    _copy = copy;
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
   * Get the cherry pick visit.
   *
   * @return the cherry pick visit
   */
  public CherryPickVisit getCherryPickVisit()
  {
    return _cherryPickVisit;
  }

  /**
   * Set the cherry pick visit.
   *
   * @param cherryPickVisit the new cherry pick visit
   */
  public void setCherryPickVisit(CherryPickVisit cherryPickVisit)
  {
    _cherryPickVisit = cherryPickVisit;
    cherryPickVisit.getHbnCherryPicks().add(this);
  }

  /**
   * Get the well.
   *
   * @return the well
   */
  public Well getWell()
  {
    return _well;
  }

  /**
   * Set the well.
   *
   * @param well the new well
   */
  public void setWell(Well well)
  {
    _well = well;
    well.getHbnCherryPicks().add(this);
  }

  /**
   * Get the copy.
   *
   * @return the copy
   */
  public Copy getCopy()
  {
    return _copy;
  }

  /**
   * Set the copy.
   *
   * @param copy the new copy
   */
  public void setCopy(Copy copy)
  {
    _copy = copy;
    copy.getHbnCherryPicks().add(this);
  }

  /**
   * Get the RNAi knockdown confirmation.
   *
   * @return the RNAi knockdown confirmation
   */
  public RNAiKnockdownConfirmation getRNAiKnockdownConfirmation()
  {
    return _rnaiKnockdownConfirmation;
  }

  /**
   * Set the RNAi knockdown confirmation.
   *
   * @param rNAiKnockdownConfirmation the new RNAi knockdown confirmation
   */
  public void setRNAiKnockdownConfirmation(RNAiKnockdownConfirmation rNAiKnockdownConfirmation)
  {
    _rnaiKnockdownConfirmation = rNAiKnockdownConfirmation;
    rNAiKnockdownConfirmation.setHbnCherryPick(this);
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


  /**
   * Set the well.
   * Throw a NullPointerException when the well is null.
   *
   * @param well the new well
   * @throws NullPointerException when the well is null
   * @motivation for hibernate and maintenance of bi-directional relationships
   * this method is public only because the bi-directional relationship
   * is cross-package.
   */
  public void setHbnWell(Well well)
  {
    if (well == null) {
      throw new NullPointerException();
    }
    _well = well;
  }


  /**
   * Set the copy.
   * Throw a NullPointerException when the copy is null.
   *
   * @param copy the new copy
   * @throws NullPointerException when the copy is null
   * @motivation for hibernate and maintenance of bi-directional relationships.
   * this method is public only because the bi-directional relationship
   * is cross-package.
   */
  public void setHbnCopy(Copy copy)
  {
    if (copy == null) {
      throw new NullPointerException();
    }
    _copy = copy;
  }


  /**
   * A business key class for the well.
   */
  private class BusinessKey
  {
    
    /**
     * Get the cherry pick visit.
     *
     * @return the cherry pick visit
     */
    public CherryPickVisit getCherryPickVisit()
    {
      return _cherryPickVisit;
    }
    
    /**
     * Get the well.
     *
     * @return the well
     */
    public Well getWell()
    {
      return _well;
    }

    @Override
    public boolean equals(Object object)
    {
      if (! (object instanceof BusinessKey)) {
        return false;
      }
      BusinessKey that = (BusinessKey) object;
      return
        getCherryPickVisit().equals(that.getCherryPickVisit()) &&
        getWell().equals(that.getWell());
    }

    @Override
    public int hashCode()
    {
      return
        getCherryPickVisit().hashCode() +
        getWell().hashCode();
    }

    @Override
    public String toString()
    {
      return getCherryPickVisit() + ":" + getWell();
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
   * Set the cherry pick visit.
   * Throw a NullPointerException when the cherry pick visit is null.
   *
   * @param cherryPickVisit the new cherry pick visit
   * @throws NullPointerException when the cherry pick visit is null
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  void setHbnCherryPickVisit(CherryPickVisit cherryPickVisit)
  {
    if (cherryPickVisit == null) {
      throw new NullPointerException();
    }
    _cherryPickVisit = cherryPickVisit;
  }

  /**
   * Set the RNAi knockdown confirmation.
   *
   * @param rNAiKnockdownConfirmation the new RNAi knockdown confirmation
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  void setHbnRNAiKnockdownConfirmation(RNAiKnockdownConfirmation rNAiKnockdownConfirmation)
  {
    _rnaiKnockdownConfirmation = rNAiKnockdownConfirmation;
  }


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

  /**
   * Get the cherry pick visit.
   *
   * @return the cherry pick visit
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.model.screens.CherryPickVisit"
   *   column="cherry_pick_visit_id"
   *   not-null="true"
   *   foreign-key="fk_cherry_pick_to_cherry_pick_visit"
   *   cascade="save-update"
   * @motivation for hibernate
   */
  private CherryPickVisit getHbnCherryPickVisit()
  {
    return _cherryPickVisit;
  }

  /**
   * Get the well.
   *
   * @return the well
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.model.libraries.Well"
   *   column="well_id"
   *   not-null="true"
   *   foreign-key="fk_cherry_pick_to_well"
   *   cascade="save-update"
   * @motivation for hibernate
   */
  private Well getHbnWell()
  {
    return _well;
  }

  /**
   * Get the copy.
   *
   * @return the copy
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.model.libraries.Copy"
   *   column="copy_id"
   *   not-null="true"
   *   foreign-key="fk_cherry_pick_to_copy"
   *   cascade="save-update"
   * @motivation for hibernate
   */
  private Copy getHbnCopy()
  {
    return _copy;
  }

  /**
   * Get the RNAi knockdown confirmation.
   *
   * @return the RNAi knockdown confirmation
   * @hibernate.one-to-one
   *   class="edu.harvard.med.screensaver.model.screens.RNAiKnockdownConfirmation"
   *   property-ref="hbnCherryPick"
   *   cascade="save-update"
   * @motivation for hibernate
   */
  private RNAiKnockdownConfirmation getHbnRNAiKnockdownConfirmation()
  {
    return _rnaiKnockdownConfirmation;
  }
}
