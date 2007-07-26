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
import java.util.HashSet;
import java.util.Set;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.DerivedEntityProperty;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.ToManyRelationship;
import edu.harvard.med.screensaver.model.ToOneRelationship;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellName;
import edu.harvard.med.screensaver.model.libraries.WellVolumeAdjustment;

import org.apache.log4j.Logger;


/**
 * A Hibernate entity bean representing a lab cherry pick. See
 * {@link CherryPickRequest} for explanation.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @hibernate.class lazy="false"
 */
public class LabCherryPick extends AbstractEntity
{
  
  // static fields

  private static final Logger log = Logger.getLogger(LabCherryPick.class);
  private static final long serialVersionUID = 0L;
  
  // instance fields

  private Integer _labCherryPickId;
  private Integer _version;
  
  private CherryPickRequest _cherryPickRequest;
  private ScreenerCherryPick _screenerCherryPick;
  private Well _sourceWell;
  private Set<WellVolumeAdjustment> _wellVolumeAdjustments = new HashSet<WellVolumeAdjustment>();
  
  private CherryPickAssayPlate _assayPlate;
  private Integer _assayPlateRow;
  private Integer _assayPlateColumn;


  // public constructor

  /**
   * Constructs an initialized <code>LabCherryPick</code> object.
   * 
   * @param cherryPickRequest the cherry pick request
   * @param sourceWell the well
   */
  private LabCherryPick(CherryPickRequest cherryPickRequest,
                        Well sourceWell)
  {
  }

  /**
   * Constructs an initialized <code>LabCherryPick</code> object with an
   * association to the ScreenerCherryPick.
   * 
   * @param cherryPickRequest
   * @param sourceWell
   * @param screenerCherryPick
   */
  public LabCherryPick(ScreenerCherryPick screenerCherryPick,
                       Well sourceWell)
  {
    if (screenerCherryPick == null || sourceWell == null) {
      throw new NullPointerException();
    }
    _cherryPickRequest = screenerCherryPick.getCherryPickRequest();

    // TODO: turn the following business rules back on once we have some means of setting the well
    // type for experimental wells in natural products libraries. (see rt#72830)
    //
    //  if (!sourceWell.getWellType().equals(WellType.EXPERIMENTAL)) {
    //    throw new BusinessRuleViolationException(sourceWell + " is not a valid source well (not experimental)");
    //  }
    //  if (_cherryPickRequest.getScreen().getScreenType().equals(ScreenType.SMALL_MOLECULE) && 
    //      sourceWell.getCompounds().size() == 0) {
    //    throw new BusinessRuleViolationException(sourceWell + " is not a valid source well (does not contain a compound)");
    //  }

    if (_cherryPickRequest.getScreen().getScreenType().equals(ScreenType.RNAI) && 
      sourceWell.getSilencingReagents().size() == 0) {
      throw new InvalidCherryPickWellException(sourceWell.getWellKey(), "does not contain any reagents");
    }

    _sourceWell = sourceWell;
    _screenerCherryPick = screenerCherryPick;
    if (!_screenerCherryPick.getLabCherryPicks().add(this)) {
      throw new DuplicateEntityException(_screenerCherryPick, this);
    }
    if (!_cherryPickRequest.getLabCherryPicks().add(this)) {
      throw new DuplicateEntityException(_cherryPickRequest, this);
    }
  }

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }
  
  @Override
  public Integer getEntityId()
  {
    return getLabCherryPickId();
  }

  /**
   * Get the id for the lab cherry pick.
   *
   * @return the id for the lab cherry pick
   * @hibernate.id generator-class="sequence"
   * @hibernate.generator-param name="sequence" value="lab_cherry_pick_id_seq"
   */
  public Integer getLabCherryPickId()
  {
    return _labCherryPickId;
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
   *   cascade="none"
   * @motivation for hibernate
   */
  @ToOneRelationship(nullable=false)
  public CherryPickRequest getCherryPickRequest()
  {
    return _cherryPickRequest;
  }

  /**
   * @hibernate.many-to-one class="edu.harvard.med.screensaver.model.screens.ScreenerCherryPick"
   *                        column="screener_cherry_pick_id" not-null="true"
   *                        foreign-key="fk_lab_cherry_pick_to_screener_cherry_pick"
   *                        cascade="none"
   */
  @ToOneRelationship(nullable=false)
  public ScreenerCherryPick getScreenerCherryPick()
  {
    return _screenerCherryPick;
  }

  /**
   * Get the source well for this cherry pick. The source well corresponds to
   * the well that will provide the liquid (compound or reagent) used to produce
   * the cherry pick assay plate. For compound screens, the screened well will
   * be the same as the source well. For RNAi screens, the screened well will
   * map to a set of source wells (to accommodate pool-to-duplex mapping).
   * <p>
   * Note: Since we must allow a LabCherryPick to be plate mapped after
   * instantation time, we instantiate it with only a sourceWell, but not with a
   * sourceCopy. This means we cannot create an assocation with a
   * WellVolumeAdjustment until the sourceCopy is specified via
   * {@link #setAllocated}. So we must redundantly store the sourceWell in both
   * the LabCherryPick and, later on, in the related wellVolumeAdjustment
   * entity.
   * 
   * @return the source well
   * @see ScreenerCherryPick#getScreenedWell()
   * @hibernate.many-to-one class="edu.harvard.med.screensaver.model.libraries.Well"
   *                        column="source_well_id" not-null="true"
   *                        foreign-key="fk_lab_cherry_pick_to_source_well"
   *                        cascade="none"
   * @motivation for hibernate
   */
  @ToOneRelationship(nullable=false, unidirectional=true)
  public Well getSourceWell()
  {
    return _sourceWell;
  }

  /**
   * Get the well volume adjustments associated with this lab cherry pick.
   * <p>
   * Note: Currently, we only allow for 1 WellVolumeAdjustment per
   * LabCherryPick. However, declaring this relationship as a one-to-many set
   * allows for:
   * <ul>
   * <li>automatic deletion of the associated WellVolumeAdjustment, if removed
   * from this set</li>
   * <li>future possibility of allowing multiple WellVolumeAdjustment per
   * LabCherryPick; e.g., now that we have WellVolumeAdjustment in our data
   * model, it may be possible to get rid of CherryPickAssayPlate "attempts", so
   * that if a CherryPickAssayPlate attempt fails, we do not create a new
   * CherryPickAssayPlate entity with a duplicate set of LabCherryPick; instead
   * we just add more WellVolumeAdjustments to the plate's set of
   * LabCherryPicks, as necessary.</li>
   * <li>it's possible that the lab might (manually) perform a secondary
   * reagent transfer for a given LabCherryPick, say, if they encountered a
   * problem; this model would accommodate such an activity</li>
   * </ul>
   * 
   * @hibernate.set cascade="all-delete-orphan" inverse="false" lazy="true"
   * @hibernate.collection-key column="lab_cherry_pick_id"
   * @hibernate.collection-one-to-many class="edu.harvard.med.screensaver.model.libraries.WellVolumeAdjustment"
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  @ToManyRelationship(inverseProperty="wellVolumeAdjustments")
  public Set<WellVolumeAdjustment> getWellVolumeAdjustments()
  {
    return _wellVolumeAdjustments;
  }

  /**
   * Get the copy.
   *
   * @return the copy
   */
  @DerivedEntityProperty
  public Copy getSourceCopy()
  {
    if (_wellVolumeAdjustments.size() == 0) {
      return null;
    }
    return _wellVolumeAdjustments.iterator().next().getCopy();
  }

  /**
   * Marks the cherry pick as has having source library plate copy well volume
   * allocated for it
   * 
   * @param sourceCopy
   */
  public void setAllocated(Copy sourceCopy)
  {
    if (sourceCopy != null && isAllocated()) {
      throw new BusinessRuleViolationException("cannot (re)allocate a cherry pick that has already been allocated");
    }
    if (sourceCopy != null && isCanceled()) {
      throw new BusinessRuleViolationException("cannot (re)allocate a cherry pick that has been canceled");
    }
    if (sourceCopy == null && !isAllocated()) {
      throw new BusinessRuleViolationException("cannot deallocate a cherry pick that has not been allocated");
    }
    if (isPlated()) {
      throw new BusinessRuleViolationException("cannot allocate or deallocate a cherry pick after it has been plated");
    }
    
    _wellVolumeAdjustments.clear();
    if (sourceCopy != null) {
      WellVolumeAdjustment wellVolumeAdjustment = 
        new WellVolumeAdjustment(sourceCopy,
                                 getSourceWell(),
                                 getCherryPickRequest().getMicroliterTransferVolumePerWellApproved().negate(),
                                 this);
      _wellVolumeAdjustments.add(wellVolumeAdjustment);
    }
  }
  
  /**
   * Marks the cherry pick as has having source library plate copy well volume
   * allocated for it, and specifies the assay plate and well that the liquid
   * volume has been allocated to
   * 
   * @param assayPlateRow
   * @param assayPlateColumn
   */
  public void setMapped(CherryPickAssayPlate assayPlate,
                        int assayPlateRow,
                        int assayPlateColumn)
  {
//    if (!isAllocated()) {
//      throw new BusinessRuleViolationException("cannot map a cherry pick to an assay plate before it has been allocated");
//    }
    if (isMapped() || isPlated()) {
      throw new BusinessRuleViolationException("cannot map a cherry pick to an assay plate if it has already been mapped or plated");
    }
    _assayPlate = assayPlate;
    _assayPlate.addLabCherryPick(this);
    _assayPlateRow = assayPlateRow;
    _assayPlateColumn = assayPlateColumn;
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
    return _cherryPickRequest.getMicroliterTransferVolumePerWellApproved();
  }

  // HACK: annotating as DerivedEntityProperty to prevent unit tests from
  // expecting a setter method (setAllocated() updates this property's value)
  /**
   * Get the cherry pick assay plate. Can be null, if the lab cherry pick has
   * not been mapped to an assay plate.
   * 
   * @return
   * @hibernate.many-to-one class="edu.harvard.med.screensaver.model.screens.CherryPickAssayPlate"
   *                        column="cherry_pick_assay_plate_id" not-null="false"
   *                        foreign-key="fk_lab_cherry_pick_to_cherry_pick_assay_plate"
   *                        cascade="none"
   */
  @ToOneRelationship(nullable=true)
  @DerivedEntityProperty
  public CherryPickAssayPlate getAssayPlate()
  {
    return _assayPlate;
  }

  // HACK: annotating as DerivedEntityProperty to prevent unit tests from
  // expecting a setter method (setAllocated() updates this property's value)
  /**
   * @return
   * @hibernate.property type="integer"
   */
  @DerivedEntityProperty
  public Integer getAssayPlateRow()
  {
    return _assayPlateRow;
  }
  
  // HACK: annotating as DerivedEntityProperty to prevent unit tests from
  // expecting a setter method (setAllocated() updates this property's value)
  /**
   * @return
   * @hibernate.property type="integer"
   */
  @DerivedEntityProperty
  public Integer getAssayPlateColumn()
  {
    return _assayPlateColumn;
  }
  
  @DerivedEntityProperty
  public WellName getAssayPlateWellName()
  {
    if (_assayPlateRow != null && 
      _assayPlateColumn != null) {
      return new WellName(_assayPlateRow, _assayPlateColumn);
    }
    return null;
  }
  
  public boolean isUnfulfilled()
  {
    return !isAllocated() && !isCanceled();
  }

  /**
   * Get whether liquid volume for this cherry pick has been allocated from a
   * source plate well.
   * 
   * @return true, if source plate well liquid volume has been allocated
   */
  @DerivedEntityProperty
  public boolean isAllocated()
  {
    return _wellVolumeAdjustments.size() > 0;
  }
  
  /**
   * Get whether this cherry pick has been mapped to an assay plate well. A
   * mapped lab cherry pick can be either allocated or unallocated.
   * 
   * @return true, if this cherry pick has been mapped to an assay plate well
   */
  @DerivedEntityProperty
  public boolean isMapped()
  {
    return _assayPlate != null;
  }
  
  /**
   * Get whether this cherry pick has been canceled. A canceled lab cherry pick
   * is one that was allocated, then mapped, and later deallocated (due to its
   * entire cherry pick assay plate having been canceled).
   */
  @DerivedEntityProperty
  public boolean isCanceled()
  {
    return isMapped() && _assayPlate.isCanceled();
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
    return isAllocated() && isMapped() && _assayPlate.isPlated();
  }

  @DerivedEntityProperty
  public boolean isFailed()
  {
    return isAllocated() && isMapped() && _assayPlate.isFailed();
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
    public Well getSourceWell()
    {
      return _sourceWell;
    }
    
    public CherryPickAssayPlate getAssayPlate()
    {
      return _assayPlate;
    }
    
    @Override
    public boolean equals(Object object)
    {
      if (! (object instanceof BusinessKey)) {
        return false;
      }
      BusinessKey that = (BusinessKey) object;
      if ((this.getAssayPlate() == null) != (that.getAssayPlate() == null)) {
        return false;
      }
      return
        this.getCherryPickRequest().equals(that.getCherryPickRequest()) &&
        this.getSourceWell().equals(that.getSourceWell()) &&
        ((getAssayPlate() == null && that.getAssayPlate() == null) ||
          this.getAssayPlate().equals(that.getAssayPlate()));
    }

    @Override
    public int hashCode()
    {
      return
        this.getCherryPickRequest().hashCode() +
        this.getSourceWell().hashCode() + 
        (this.getAssayPlate() == null ? 0 : this.getAssayPlate().hashCode());
    }

    @Override
    public String toString()
    {
      return this.getCherryPickRequest() + ":" + 
      this.getSourceWell() + ":" + 
      (this.getAssayPlate() == null ? "<unmapped>" : this.getAssayPlate());
    }
  }

  @Override
  protected Object getBusinessKey()
  {
    return new BusinessKey();
  }


  // private constructor

  /**
   * Construct an uninitialized <code>LabCherryPick</code> object.
   *
   * @motivation for hibernate
   */
  private LabCherryPick() {}


  // private methods

  /**
   * Set the id for the lab cherry pick.
   *
   * @param labCherryPickId the new id for the lab cherry pick
   * @motivation for hibernate
   */
  private void setLabCherryPickId(Integer labCherryPickId) {
    _labCherryPickId = labCherryPickId;
  }

  private void setScreenerCherryPick(ScreenerCherryPick screenerCherryPick)
  {
    _screenerCherryPick = screenerCherryPick;
  }

  /**
   * Get the version for the lab cherry pick.
   *
   * @return the version for the lab cherry pick
   * @motivation for hibernate
   * @hibernate.version
   */
  private Integer getVersion() {
    return _version;
  }

  /**
   * Set the version for the lab cherry pick.
   *
   * @param version the new version for the lab cherry pick
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
  private void setSourceWell(Well sourceWell)
  {
    _sourceWell = sourceWell;
  }

  /**
   * Set the well volume adjustments.
   *
   * @param wellVolumeAdjustments the well volume adjustments
   * @motivation for hibernate and maintenance of bi-directional relationships.
   */
  private void setWellVolumeAdjustments(Set<WellVolumeAdjustment> wellVolumeAdjustments)
  {
    _wellVolumeAdjustments = wellVolumeAdjustments;
  }

  void setAssayPlate(CherryPickAssayPlate assayPlate)
  {
    _assayPlate = assayPlate;
  }

  private void setAssayPlateRow(Integer row)
  {
    _assayPlateRow = row;
  }
  
  private void setAssayPlateColumn(Integer column)
  {
    _assayPlateColumn = column;
  }
}
