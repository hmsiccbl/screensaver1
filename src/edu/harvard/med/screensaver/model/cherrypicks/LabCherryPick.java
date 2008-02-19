// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.cherrypicks;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Parameter;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellName;
import edu.harvard.med.screensaver.model.libraries.WellType;
import edu.harvard.med.screensaver.model.libraries.WellVolumeAdjustment;
import edu.harvard.med.screensaver.model.screens.ScreenType;


/**
 * A Hibernate entity bean representing a lab cherry pick. See
 * {@link #CherryPickRequest} for explanation.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@org.hibernate.annotations.Proxy
@edu.harvard.med.screensaver.model.annotations.ContainedEntity(containingEntityClass=CherryPickRequest.class)
public class LabCherryPick extends AbstractEntity
{

  // private static data

  private static final Logger log = Logger.getLogger(LabCherryPick.class);
  private static final long serialVersionUID = 0L;


  // private instance data

  private Integer _labCherryPickId;
  private Integer _version;
  private CherryPickRequest _cherryPickRequest;
  private ScreenerCherryPick _screenerCherryPick;
  private Well _sourceWell;
  private Set<WellVolumeAdjustment> _wellVolumeAdjustments = new HashSet<WellVolumeAdjustment>();
  private CherryPickAssayPlate _assayPlate;
  private Integer _assayPlateRow;
  private Integer _assayPlateColumn;

  public enum LabCherryPickStatus {
    Unfulfilled,
    Reserved,
    Mapped,
    Canceled,
    Failed,
    Plated
  };




  // public instance methods

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  @Override
  @Transient
  public Integer getEntityId()
  {
    return getLabCherryPickId();
  }

  /**
   * Get the id for the lab cherry pick.
   * @return the id for the lab cherry pick
   */
  @Id
  @org.hibernate.annotations.GenericGenerator(
    name="lab_cherry_pick_id_seq",
    strategy="sequence",
    parameters = { @Parameter(name="sequence", value="lab_cherry_pick_id_seq") }
  )
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="lab_cherry_pick_id_seq")
  public Integer getLabCherryPickId()
  {
    return _labCherryPickId;
  }

  @Transient
  public LabCherryPickStatus getStatus()
  {
    return isPlated() ? LabCherryPickStatus.Plated :
      isFailed() ? LabCherryPickStatus.Failed :
        isCancelled() ? LabCherryPickStatus.Canceled :
          isMapped() ? LabCherryPickStatus.Mapped :
            isAllocated() ? LabCherryPickStatus.Reserved :
              LabCherryPickStatus.Unfulfilled;
  }

  /**
   * Get the cherry pick request.
   * @return the cherry pick request
   */
  @ManyToOne
  @JoinColumn(name="cherryPickRequestId", nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_lab_cherry_pick_to_cherry_pick_request")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  public CherryPickRequest getCherryPickRequest()
  {
    return _cherryPickRequest;
  }

  /**
   * Get the screener cherry pick for this lab cherry pick.
   * @return the screener cherry pick for this lab cherry pick
   */
  @ManyToOne(cascade={ CascadeType.PERSIST, CascadeType.MERGE })
  @JoinColumn(name="screenerCherryPickId", nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_lab_cherry_pick_to_screener_cherry_pick")
  @org.hibernate.annotations.Cascade(value={ org.hibernate.annotations.CascadeType.SAVE_UPDATE })
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
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
   */
  @ManyToOne
  @JoinColumn(name="sourceWellId", nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_lab_cherry_pick_to_source_well")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @edu.harvard.med.screensaver.model.annotations.ManyToOne(unidirectional=true)
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
   */
  @OneToMany(
    cascade={ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE },
    fetch=FetchType.LAZY
  )
  @JoinColumn(name="labCherryPickId")
  @org.hibernate.annotations.ForeignKey(name="fk_well_volume_adjustment_to_lab_cherry_pick")
  @org.hibernate.annotations.Cascade(value={
    org.hibernate.annotations.CascadeType.SAVE_UPDATE,
    org.hibernate.annotations.CascadeType.DELETE,
    org.hibernate.annotations.CascadeType.DELETE_ORPHAN
  })
  public Set<WellVolumeAdjustment> getWellVolumeAdjustments()
  {
    return _wellVolumeAdjustments;
  }

  /**
   * Create and return a new well volume adjustment for the lab cherry pick.
   * @param copy the copy
   * @param well the well
   * @param microliterVolume the volume in microliters
   * @return true the new well volume adjustment
   */
  public WellVolumeAdjustment createWellVolumeAdjustment(
      Copy copy,
      Well well,
      BigDecimal microliterVolume)
  {
    WellVolumeAdjustment wellVolumeAdjustment = new WellVolumeAdjustment(
      copy,
      well,
      microliterVolume,
      this);
    _wellVolumeAdjustments.add(wellVolumeAdjustment);
    return wellVolumeAdjustment;
  }

  /**
   * Get the source copy.
   * @return the source copy
   */
  @Transient
  public Copy getSourceCopy()
  {
    if (_wellVolumeAdjustments.size() == 0) {
      return null;
    }
    return _wellVolumeAdjustments.iterator().next().getCopy();
  }

  /**
   * Mark the cherry pick as having well volume allocated from a particular source library plate
   * copy.
   * @param sourceCopy the source copy from which the well volume was allocated
   */
  public void setAllocated(Copy sourceCopy)
  {
    if (sourceCopy != null && isAllocated()) {
      throw new BusinessRuleViolationException("cannot (re)allocate a cherry pick that has already been allocated");
    }
    if (sourceCopy != null && isCancelled()) {
      throw new BusinessRuleViolationException("cannot (re)allocate a cherry pick that has been canceled");
    }
    if (sourceCopy == null && !isAllocated()) {
      throw new BusinessRuleViolationException("cannot deallocate a cherry pick that has not been allocated");
    }
    if (isPlated()) {
      throw new BusinessRuleViolationException("cannot allocate or deallocate a cherry pick after it has been plated");
    }

    boolean wasUnfulfilled = isUnfulfilled();
    _wellVolumeAdjustments.clear();
    if (sourceCopy != null) {
      createWellVolumeAdjustment(
          sourceCopy,
          getSourceWell(),
          getCherryPickRequest().getMicroliterTransferVolumePerWellApproved().negate());
    }

    boolean nowUnfulfilled = isUnfulfilled();
    if (!wasUnfulfilled && nowUnfulfilled) {
      _cherryPickRequest.incUnfulfilledLabCherryPicks();
    }
    else if (wasUnfulfilled && !nowUnfulfilled) {
      _cherryPickRequest.decUnfulfilledLabCherryPicks();
    }
  }

  /**
   * Mark the cherry pick as having well volume allocated for a particular assay plate, specifying
   * the assay plate and well that the liquid volume has been allocated to.
   * @param assayPlate the cherry pick assay plate
   * @param assayPlateRow the assay plate row
   * @param assayPlateColumn the assay plate column
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
    _assayPlate.getLabCherryPicks().add(this);
    _assayPlateRow = assayPlateRow;
    _assayPlateColumn = assayPlateColumn;
  }

  /**
   * Get the volume.
   * @return the volume
   */
  @Transient
  public BigDecimal getVolume()
  {
    if (!isPlated()) {
      throw new IllegalStateException("a cherry pick does not have a transferred volume before it has been transfered");
    }
    return _cherryPickRequest.getMicroliterTransferVolumePerWellApproved();
  }

  /**
   * Get the cherry pick assay plate. Can be null, if the lab cherry pick has
   * not been mapped to an assay plate. This value should only be updated via
   * {@link #setMapped(CherryPickAssayPlate, int, int)}.
   * @return the cherry pick assay plate
   */
  @ManyToOne
  @JoinColumn(name="cherryPickAssayPlateId", nullable=true)
  @org.hibernate.annotations.ForeignKey(name="fk_lab_cherry_pick_to_cherry_pick_assay_plate")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @edu.harvard.med.screensaver.model.annotations.Column(hasNonconventionalSetterMethod=true)
  public CherryPickAssayPlate getAssayPlate()
  {
    return _assayPlate;
  }

  /**
   * Get the 0-based indexed assay plate row.
   * @return the assay plate row
   */
  @org.hibernate.annotations.Type(type="integer")
  @edu.harvard.med.screensaver.model.annotations.Column(hasNonconventionalSetterMethod=true)
  public Integer getAssayPlateRow()
  {
    return _assayPlateRow;
  }

  /**
   * Get the 0-based indexed assay plate column.
   * @return the assay plate column
   */
  @org.hibernate.annotations.Type(type="integer")
  @edu.harvard.med.screensaver.model.annotations.Column(hasNonconventionalSetterMethod=true)
  public Integer getAssayPlateColumn()
  {
    return _assayPlateColumn;
  }

  @Transient
  public WellName getAssayPlateWellName()
  {
    if (_assayPlateRow != null &&
      _assayPlateColumn != null) {
      return new WellName(_assayPlateRow, _assayPlateColumn);
    }
    return null;
  }

  /**
   * Return true iff the lab cherry pick is unfulfilled. A lab cherry pick is unfulfilled
   * whenever if is neither allocated nor cancelled.
   * @return true iff the lab cherry pick is unfulfilled
   */
  @Transient
  public boolean isUnfulfilled()
  {
    // note: a failed labCherryPick will be unallocated, so an isFailed() check would be redundant
    return !isAllocated() && !isCancelled() /*&& !isFailed()*/;
  }

  /**
   * Get whether liquid volume for this cherry pick has been allocated from a
   * source plate well.
   * @return true iff source plate well liquid volume has been allocated
   */
  @Transient
  public boolean isAllocated()
  {
    return _wellVolumeAdjustments.size() > 0;
  }

  /**
   * Get whether this cherry pick has been mapped to an assay plate well. A
   * mapped lab cherry pick can be either allocated or unallocated.
   * @return true iff this cherry pick has been mapped to an assay plate well
   */
  @Transient
  public boolean isMapped()
  {
    return _assayPlate != null;
  }

  /**
   * Get whether this cherry pick has been cancelled. A cancelled lab cherry pick
   * is one that was allocated, then mapped, and later deallocated (due to its
   * entire cherry pick assay plate having been canceled).
   * @return true iff this cherry pick has been cancelled
   */
  @Transient
  public boolean isCancelled()
  {
    return isMapped() && _assayPlate.isCancelled();
  }

  /**
   * Get whether liquid volume for this cherry pick has been transferred from a
   * source copy plate to a cherry pick assay plate.
   * @return true iff source plate well liquid volume has been transfered
   */
  @Transient
  public boolean isPlated()
  {
    return isAllocated() && isMapped() && _assayPlate.isPlated();
  }

  /**
   * Get whether this cherry pick is located on a failed assay plate.
   * @return true iff this cherry pick is located on a failed assay plate
   */
  @Transient
  public boolean isFailed()
  {
    return isAllocated() && isMapped() && _assayPlate.isFailed();
  }


  // package constructor

  /**
   * Construct an initialized <code>LabCherryPick</code> with an association to the
   * <code>ScreenerCherryPick</code>. Intended only for use by {@link
   * CherryPickRequest#createLabCherryPick(ScreenerCherryPick, Well)}.
   * @param sourceWell the source well
   * @param screenerCherryPick the screener cherry pick
   */
  LabCherryPick(ScreenerCherryPick screenerCherryPick, Well sourceWell)
  {
    if (screenerCherryPick == null || sourceWell == null) {
      throw new NullPointerException();
    }
    _cherryPickRequest = screenerCherryPick.getCherryPickRequest();

    // TODO: reinstate the following BusinessRuleViolationExceptions once we have some means of setting the well
    // type for experimental wells in natural products libraries. (see rt#72830)
    if (!sourceWell.getWellType().equals(WellType.EXPERIMENTAL)) {
      log.warn(sourceWell + " is not a valid source well (not experimental)");
      //throw new BusinessRuleViolationException(sourceWell + " is not a valid source well (not experimental)");
    }
    if (_cherryPickRequest.getScreen().getScreenType().equals(ScreenType.SMALL_MOLECULE) &&
      sourceWell.getCompounds().size() == 0) {
      log.warn(sourceWell + " is not a valid source well (does not contain a compound)");
      //throw new BusinessRuleViolationException(sourceWell + " is not a valid source well (does not contain a compound)");
    }

    if (_cherryPickRequest.getScreen().getScreenType().equals(ScreenType.RNAI) &&
      sourceWell.getSilencingReagents().size() == 0) {
      throw new InvalidCherryPickWellException(sourceWell.getWellKey(), "does not contain any reagents");
    }

    _sourceWell = sourceWell;
    _screenerCherryPick = screenerCherryPick;
  }


  // protected constructor

  /**
   * Construct an uninitialized <code>LabCherryPick</code>.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected LabCherryPick() {}


  // private instance methods

  /**
   * Set the id for the lab cherry pick.
   * @param labCherryPickId the new id for the lab cherry pick
   * @motivation for hibernate
   */
  private void setLabCherryPickId(Integer labCherryPickId)
  {
    _labCherryPickId = labCherryPickId;
  }

  /**
   * Set the screener cherry pick.
   * @param screenerCherryPick the new screener cherry pick
   * @motivation for hibernate
   */
  private void setScreenerCherryPick(ScreenerCherryPick screenerCherryPick)
  {
    _screenerCherryPick = screenerCherryPick;
  }

  /**
   * Get the version for the lab cherry pick.
   * @return the version for the lab cherry pick
   * @motivation for hibernate
   */
  @Column(nullable=false)
  @Version
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * Set the version for the lab cherry pick.
   * @param version the new version for the lab cherry pick
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }

  /**
   * Set the cherry pick request.
   * @param cherryPickRequest the new cherry pick request
   * @motivation for hibernate
   */
  private void setCherryPickRequest(CherryPickRequest cherryPickRequest)
  {
    _cherryPickRequest = cherryPickRequest;
  }

  /**
   * Set the source well.
   * @param well the new source well
   * @motivation for hibernate
   */
  private void setSourceWell(Well sourceWell)
  {
    _sourceWell = sourceWell;
  }

  /**
   * Set the well volume adjustments.
   * @param wellVolumeAdjustments the well volume adjustments
   * @motivation for hibernate
   */
  private void setWellVolumeAdjustments(Set<WellVolumeAdjustment> wellVolumeAdjustments)
  {
    _wellVolumeAdjustments = wellVolumeAdjustments;
  }

  /**
   * Set the assay plate.
   * @param assayPlate the new assay plate
   * @motivation for hibernate
   */
  private void setAssayPlate(CherryPickAssayPlate assayPlate)
  {
    _assayPlate = assayPlate;
  }

  /**
   * Set the assay plate row.
   * @param assayPlateRow the new assay plate row
   * @motivation for hibernate
   */
  private void setAssayPlateRow(Integer assayPlateRow)
  {
    _assayPlateRow = assayPlateRow;
  }

  /**
   * Set the assay plate column.
   * @param assayPlateColumn the new assay plate column
   * @motivation for hibernate
   */
  private void setAssayPlateColumn(Integer assayPlateColumn)
  {
    _assayPlateColumn = assayPlateColumn;
  }
}
