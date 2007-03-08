// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;


import java.math.BigDecimal;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.DerivedEntityProperty;
import edu.harvard.med.screensaver.model.ToOneRelationship;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.model.libraries.Well;

import org.apache.log4j.Logger;


// TODO: for performance, we may have to make CherryPick into a Hibernate value
// type, rather than an entity type, as we did with ResultValue (this would mean
// eliminating the navigability to CherryPickRequest)
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

  private CherryPickRequest _cherryPickRequest;
  private Well _sourceWell;
  private Copy _sourceCopy;
  
  // Note: dest plate type must the same for all cherry picks from the same *library*; we shall enforce at the business tier, rather than the schema-level (this design is denormalized)
  private PlateType _destinationPlateType;   
  // TODO: perhaps plate name should be an index into a list of plate names maintained by CherryPickRequest (this is a more space-efficient and normalized designed)
  private String _destinationPlateName;
  private int _destinationPlateRow;
  private int _destinationPlateColumn;

  private CherryPickLiquidTransfer _cherryPickLiquidTransfer;
  
  /* follow-up data from screener, after cherry pick screening is completed */
  private RNAiKnockdownConfirmation _rnaiKnockdownConfirmation;
  private IsHitConfirmedViaExperimentation _isHitConfirmedViaExperimentation;
  private String _notesOnHitConfirmation;


  // public constructor

  /**
   * Constructs an initialized <code>CherryPick</code> object.
   *
   * @param cherryPickRequest the cherry pick request
   * @param well the well
   * @param copy the copy
   */
  public CherryPick(CherryPickRequest cherryPickRequest,
                    Well well,
                    Copy copy)
  {
    if (cherryPickRequest == null || well == null || copy == null) {
        throw new NullPointerException();
    }
    _cherryPickRequest = cherryPickRequest;
    _sourceWell = well;
    _sourceCopy = copy;
    _cherryPickRequest.getCherryPicks().add(this);
    _sourceWell.getHbnCherryPicks().add(this);
    _sourceCopy.getHbnCherryPicks().add(this);
  }

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
   * Get the cherry pick request.
   *
   * @return the cherry pick request
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.model.screens.CherryPickRequest"
   *   column="cherry_pick_request_id"
   *   not-null="true"
   *   foreign-key="fk_cherry_pick_to_cherry_pick_request"
   *   cascade="save-update"
   * @motivation for hibernate
   */
  @ToOneRelationship(nullable=false)
  public CherryPickRequest getCherryPickRequest()
  {
    return _cherryPickRequest;
  }

  /**
   * Get the source library well that is the target of this cherry pick.
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
  @ToOneRelationship(nullable=false)
  public Well getSourceWell()
  {
    return _sourceWell;
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
  @ToOneRelationship(nullable=false)
  public Copy getSourceCopy()
  {
    return _sourceCopy;
  }

  // HACK: This property is marked as derived for unit testing purposes
  // only! It is in fact a real hibernate relationshp, though it is unique in that it can only be
  // modified from the other side (CherryPickLiquidTransfer). Our unit tests do
  // not yet handle this case.
  /**
   * Get the cherry pick liquid transfer that marks the plating of this cherry
   * pick. 
   * 
   * @param cherryPickLiquidTransfer
   * @see #setCherryPickLiquidTransfer(CherryPickLiquidTransfer) to set this
   *      cherry pick's relationship to a chery pick liquid transfer
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.model.screens.CherryPickLiquidTransfer"
   *   column="cherry_pick_liquid_transfer_id"
   *   not-null="false"
   *   foreign-key="fk_cherry_pick_to_cherry_pick_liquid_transfer"
   *   cascade="save-update"
   */
  @ToOneRelationship(inverseProperty="platedCherryPicks")
  @DerivedEntityProperty(isPersistent=true)
  public CherryPickLiquidTransfer getCherryPickLiquidTransfer()
  {
    return _cherryPickLiquidTransfer;
  }

  /**
   * Marks the cherry pick as has having source plate well volume allocated for
   * it, and specifies the destination cherry pick assay plate and well that the
   * liquid volume has been allocated to.
   * 
   * @param destinationPlateType
   * @param destinationPlateName
   * @param destinationPlateRow
   * @param destinationPlateColumn
   */
  public void setAllocated(PlateType destinationPlateType,
                           String destinationPlateName,
                           int destinationPlateRow,
                           int destinationPlateColumn)
  {
    if (destinationPlateType == null ||
      destinationPlateName == null) {
      throw new IllegalArgumentException("null argument values not allowed");
    }
      
    _destinationPlateType = destinationPlateType;
    _destinationPlateName = destinationPlateName;
    _destinationPlateRow = destinationPlateRow;
    _destinationPlateColumn = destinationPlateColumn;
  }
  
  /**
   * Get the volume.
   *
   * @return the volume
   */
  @DerivedEntityProperty
  public BigDecimal getVolume()
  {
    if (!isPlated()) {
      throw new IllegalStateException("a cherry pick does not have a transferred volume before it has been transfered");
    }
    return _cherryPickLiquidTransfer.getActualMicroliterTransferVolumePerWell();
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
  @ToOneRelationship(inverseProperty="cherryPick")
  public RNAiKnockdownConfirmation getRNAiKnockdownConfirmation()
  {
    return _rnaiKnockdownConfirmation;
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

  // HACK: annotating as DerivedEntityProperty to avoid unit tests expecting a
  // setter method (setAllocated() updates this property's value)
  /**
   * Get the destination plate type
   *
   * @return the destination plate type
   * @hibernate.property type="edu.harvard.med.screensaver.model.libraries.PlateType$UserType"
   */
  @DerivedEntityProperty
  public PlateType getDestinationPlateType()
  {
    return _destinationPlateType;
  }

  // HACK: annotating as DerivedEntityProperty to avoid unit tests expecting a
  // setter method (setAllocated() updates this property's value)
  /**
   * The name of the cherry pick assay plate.
   * @hibernate.property type="text"
   * @return
   */
  @DerivedEntityProperty
  public String getDestinationPlateName()
  {
    return _destinationPlateName;
  }

  // HACK: annotating as DerivedEntityProperty to avoid unit tests expecting a
  // setter method (setAllocated() updates this property's value)
  /**
   * @hiberate.property type="int"
   * @return
   */
  @DerivedEntityProperty
  public int getDestinationPlateRow()
  {
    return _destinationPlateRow;
  }
  
  // HACK: annotating as DerivedEntityProperty to avoid unit tests expecting a
  // setter method (setAllocated() updates this property's value)
  /**
   * @hiberate.property type="int"
   * @return
   */
  @DerivedEntityProperty
  public int getDestinationPlateColumn()
  {
    return _destinationPlateColumn;
  }
  
  // TODO: unit test this property
  /**
   * Get whether liquid volume for this cherry pick has been allocated from a
   * source plate well.
   * 
   * @return true, if source plate well liquid volume has been allocated
   */
  @DerivedEntityProperty
  public boolean isAllocated()
  {
    return _destinationPlateName != null;
  }

  /**
   * Get whether liquid volume for this cherry pick has been transferred from a
   * source copy plate to a cherry pick assay plate.
   * 
   * @return true, if source plate well liquid volume has been transfered
   */
  @DerivedEntityProperty
  public boolean isPlated()
  {
    return _cherryPickLiquidTransfer != null;
  }

  /**
   * A business key class for the cherry pick
   */
  private class BusinessKey
  {
    
    /**
     * Get the cherry pick request.
     *
     * @return the cherry pick request
     */
    public CherryPickRequest getCherryPickRequest()
    {
      return _cherryPickRequest;
    }
    
    /**
     * Get the well.
     *
     * @return the well
     */
    public Well getWell()
    {
      return _sourceWell;
    }

    @Override
    public boolean equals(Object object)
    {
      if (! (object instanceof BusinessKey)) {
        return false;
      }
      BusinessKey that = (BusinessKey) object;
      return
        this.getCherryPickRequest().equals(that.getCherryPickRequest()) &&
        this.getWell().equals(that.getWell());
    }

    @Override
    public int hashCode()
    {
      return
        this.getCherryPickRequest().hashCode() +
        this.getWell().hashCode();
    }

    @Override
    public String toString()
    {
      return this.getCherryPickRequest() + ":" + this.getWell();
    }
  }

  @Override
  protected Object getBusinessKey()
  {
    return new BusinessKey();
  }


  // package methods

  /**
   * Set the RNAi knockdown confirmation.
   *
   * @param rNAiKnockdownConfirmation the new RNAi knockdown confirmation
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  void setRNAiKnockdownConfirmation(RNAiKnockdownConfirmation rNAiKnockdownConfirmation)
  {
    _rnaiKnockdownConfirmation = rNAiKnockdownConfirmation;
  }

  /**
   * Set the cherry pick liquid transfer.
   * 
   * @see #setCherryPickLiquidTransfer(CherryPickLiquidTransfer) for application
   *      use
   * @motivation for hibernate and maintenance of bi-directional relationships;
   *             the only reason we need Hibernate get/set methods for this
   *             property is to allow validation logic to be invoked in the
   *             public setter.
   */
  void setCherryPickLiquidTransfer(CherryPickLiquidTransfer cherryPickLiquidTransfer)
  {
    _cherryPickLiquidTransfer = cherryPickLiquidTransfer;
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
   * Set the cherry pick request.
   *
   * @param cherryPickRequest the new cherry pick request
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  private void setCherryPickRequest(CherryPickRequest cherryPickRequest)
  {
    _cherryPickRequest = cherryPickRequest;
  }

  /**
   * Set the source well.
   *
   * @param well the new well
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  private void setSourceWell(Well well)
  {
    _sourceWell = well;
  }

  /**
   * Set the source copy.
   *
   * @param copy the new copy
   * @motivation for hibernate and maintenance of bi-directional relationships.
   */
  private void setSourceCopy(Copy copy)
  {
    _sourceCopy = copy;
  }

  private void setDestinationPlateType(PlateType destinationPlateType)
  {
    _destinationPlateType = destinationPlateType;
  }

  private void setDestinationPlateName(String destinationPlateName)
  {
    _destinationPlateName = destinationPlateName;
  }
  
  private void setDestinationPlateRow(int row)
  {
    _destinationPlateRow = row;
  }
  
  private void setDestinationPlateColumn(int column)
  {
    _destinationPlateColumn = column;
  }
  
  CherryPickLiquidTransfer getHbnCherryPickLiquidTransfer()
  {
    return _cherryPickLiquidTransfer;
  }

}
