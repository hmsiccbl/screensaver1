// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.cherrypicks;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
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
import edu.harvard.med.screensaver.model.libraries.PlateType;

/**
 * A Hibernate entity bean representing a cherry pick assay plate.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @see CherryPickRequest
 */
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
  name="cherryPickAssayPlateType",
  discriminatorType=DiscriminatorType.STRING
)
@DiscriminatorValue("CherryPickAssayPlate")
@org.hibernate.annotations.Proxy
@edu.harvard.med.screensaver.model.annotations.ContainedEntity(containingEntityClass=CherryPickRequest.class)
public class CherryPickAssayPlate extends AbstractEntity implements Comparable<CherryPickAssayPlate>
{

  // private static data

  private static final long serialVersionUID = 1L;
  private static Logger log = Logger.getLogger(CherryPickAssayPlate.class);


  // private instance data

  private Integer _cherryPickAssayPlateId;
  private Integer _version;
  private CherryPickRequest _cherryPickRequest;
  private Set<LabCherryPick> _labCherryPicks = new HashSet<LabCherryPick>();
  private Integer _plateOrdinal;
  private Integer _attemptOrdinal;
  // Note: assay plate type must the same for all cherry picks from the same
  // *library*; we shall enforce at the business tier, rather than the
  // schema-level (this design is denormalized)
  private PlateType _plateType;
  private CherryPickLiquidTransfer _cherryPickLiquidTransfer;
  private transient int _logicalAssayPlateCount;
  private transient String _name;


  // public instance methods

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  @Override
  @Transient
  public Serializable getEntityId()
  {
    return _cherryPickAssayPlateId;
  }

  @Override
  public Object clone()
  {
    return getCherryPickRequest().createCherryPickAssayPlate(
      getPlateOrdinal(),
      getAttemptOrdinal() + 1,
      getAssayPlateType());
  }

  public int compareTo(CherryPickAssayPlate that)
  {
    return this._plateOrdinal < that._plateOrdinal ? -1 :
      this._plateOrdinal > that._plateOrdinal ? 1 :
        this._attemptOrdinal < that._attemptOrdinal ? -1 :
          this._attemptOrdinal > that._attemptOrdinal ? 1 : 0;
  }

  /**
   * Get the id for the cherry pick assay plate.
   * @return the id for the cherry pick assay plate
   */
  @Id
  @org.hibernate.annotations.GenericGenerator(
    name="cherry_pick_assay_plate_id_seq",
    strategy="sequence",
    parameters = { @Parameter(name="sequence", value="cherry_pick_assay_plate_id_seq") }
  )
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="cherry_pick_assay_plate_id_seq")
  public Integer getCherryPickAssayPlateId()
  {
    return _cherryPickAssayPlateId;
  }

  /**
   * Get the cherry pick request.
   * @return the cherry pick request
   */
  @ManyToOne
  @JoinColumn(name="cherryPickRequestId", nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_cherry_pick_assay_plate_to_cherry_pick_request")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  public CherryPickRequest getCherryPickRequest()
  {
    return _cherryPickRequest;
  }

  /**
   * Get the plate ordinal, which signifies that this is the n'th <i>logical</i>
   * cherry pick assay plate for a given cherry pick request. This value is a
   * zero-based index. There may be multiple {@link CherryPickAssayPlate}s for
   * a given logical plate, if it takes more than one attempt to physically
   * produce the plate in the lab. The multiple instances are differentiated by
   * their {@link #getAttemptOrdinal attemptOrdinal}.
   * @return the plate ordinal
   */
  @Column(nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.Type(type="integer")
  public Integer getPlateOrdinal()
  {
    return _plateOrdinal;
  }

  /**
   * Get the attempt ordinal, which signifies that this is the n'th attempt (by
   * the lab) at creating a given logical cherry pick assay plate (i.e., for the
   * same plate ordinal). This value is a zero-based index.
   * @return the attempt ordinal
   */
  @Column(nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.Type(type="integer")
  public Integer getAttemptOrdinal()
  {
    return _attemptOrdinal;
  }

  /**
   * Get the assay plate name. the plate name is computed according to ICCB-L lab policies
   * based on the name of the person who requested the cherry pick request, the ICCB-L screen
   * number, the cherry pick request id, the plate ordinal, and the total number of plates in
   * the cherry pick request.
   * @return the assay plate name
   */
  @Transient
  public String getName()
  {
    int logicalAssayPlates = _cherryPickRequest.getActiveCherryPickAssayPlates().size();
    if (_name == null || logicalAssayPlates != _logicalAssayPlateCount) {
      StringBuilder name = new StringBuilder();
      name.append(_cherryPickRequest.getRequestedBy().getFullNameFirstLast()).
      append(" (").append(_cherryPickRequest.getScreen().getScreenNumber()).append(") ").
      append("CP").append(_cherryPickRequest.getEntityId()).
      append("  Plate ").append(String.format("%02d", (_plateOrdinal + 1))).append(" of ").
      append(logicalAssayPlates);
      _name = name.toString();
      _logicalAssayPlateCount = logicalAssayPlates;
    }
    return _name;
  }

  /**
   * Get the lab cherry picks mapped onto this cherry pick assay plate. New lab cherry picks
   * should only be added to this set via {@link
   * LabCherryPick#setMapped(CherryPickAssayPlate, int, int)}.
   * @return the set of lab cherry picks mapped onto this cherry pick assay plate
   */
  @OneToMany(mappedBy="assayPlate", fetch=FetchType.LAZY)
  @edu.harvard.med.screensaver.model.annotations.Column(hasNonconventionalSetterMethod=true)
  public Set<LabCherryPick> getLabCherryPicks()
  {
    return _labCherryPicks;
  }

  /**
   * Get a string label describing the status of the assay plate.
   * @return a string label describing the status of the assay plate
   */
  @Transient
  public String getStatusLabel()
  {
    return isPlated() ? "Plated" : isFailed() ? "Failed" : isCancelled() ? "Canceled" : "Not Plated";
  }

  /**
   * Return true iff the cherry pick liquid transfer is {@link
   * CherryPickLiquidTransferStatus#CANCELED cancelled}.
   * @return true iff the cherry pick liquid transfer is cancelled
   */
  @Transient
  public boolean isCancelled()
  {
    return _cherryPickLiquidTransfer != null && _cherryPickLiquidTransfer.isCancelled();
  }

  /**
   * Return true iff the cherry pick assay plate has been plated.
   * @return true iff the cherry pick assay plate has been plated
   */
  @Transient
  public boolean isPlated()
  {
    return _cherryPickLiquidTransfer != null && _cherryPickLiquidTransfer.isSuccessful();
  }

  /**
   * Return true iff the cherry pick liquid transfer is {@link
   * CherryPickLiquidTransferStatus#FAILED failed}.
   * @return true iff the cherry pick liquid transfer is failed
   */
  @Transient
  public boolean isFailed()
  {
    return _cherryPickLiquidTransfer != null && _cherryPickLiquidTransfer.isFailed();
  }

  /**
   * Get the cherry pick liquid transfer that marks that this plate has been created.
   * @return a CherryPickLiquidTransfer
   */
  @ManyToOne(cascade={ CascadeType.PERSIST, CascadeType.MERGE })
  @JoinColumn(name="cherryPickLiquidTransferId", nullable=true)
  @org.hibernate.annotations.ForeignKey(name="fk_cherry_pick_assay_plate_to_cherry_pick_liquid_transfer")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @org.hibernate.annotations.Cascade(value={ org.hibernate.annotations.CascadeType.SAVE_UPDATE })
  public CherryPickLiquidTransfer getCherryPickLiquidTransfer()
  {
    return _cherryPickLiquidTransfer;
  }

  /**
   * Set the cherry pick liquid transfer.
   * @param cherryPickLiquidTransfer the new cherry pick liquid transfer
   */
  public void setCherryPickLiquidTransfer(CherryPickLiquidTransfer cherryPickLiquidTransfer)
  {
    if (isHibernateCaller()) {
      _cherryPickLiquidTransfer = cherryPickLiquidTransfer;
      return;
    }
    if (_cherryPickLiquidTransfer != null) {
      if (_cherryPickLiquidTransfer.equals(cherryPickLiquidTransfer)) {
        // already set to the specified cherryPickLiquidTransfer
        return;
      }
      String requestedStatus = cherryPickLiquidTransfer.getStatus().getValue().toLowerCase();
      throw new BusinessRuleViolationException("cannot mark assay plate as '" +
                                               requestedStatus + "' since it is already " +
                                               getStatusLabel());
    }
    if (_cherryPickLiquidTransfer != null) {
      _cherryPickLiquidTransfer.getCherryPickAssayPlates().remove(this);
    }
    _cherryPickLiquidTransfer = cherryPickLiquidTransfer;
    if (_cherryPickLiquidTransfer != null) {
      _cherryPickLiquidTransfer.getCherryPickAssayPlates().add(this);
    }
  }

  /**
   * Get the assay plate type.
   * @return the assay plate type
   */
  @Column(nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.Type(type="edu.harvard.med.screensaver.model.libraries.PlateType$UserType")
  public PlateType getAssayPlateType()
  {
    return _plateType;
  }

  /**
   * Get the source plates to be drawn from to plate this assay plate.
   * @return the source plates to be drawn from to plate this assay plate
   */
  @Transient
  public Set<Integer> getSourcePlates()
  {
    Set<Integer> sourcePlates = new HashSet<Integer>();
    for (LabCherryPick labCherryPick : _labCherryPicks) {
      sourcePlates.add(labCherryPick.getSourceWell().getPlateNumber());
    }
    return sourcePlates;
  }


  // package constructor

  /**
   * Construct an initialized <code>CherryPickAssayPlate</code>. Intended only for use by {@link
   * CherryPickRequest#createCherryPickAssayPlate(Integer, Integer, PlateType)}.
   * @param cherryPickRequest the cherry pick request
   * @param plateOrdinal the plate ordinal
   * @param attemptOrdinal the attempt ordinal
   * @param plateType the plate type
   */
  CherryPickAssayPlate(
    CherryPickRequest cherryPickRequest,
    Integer plateOrdinal,
    Integer attemptOrdinal,
    PlateType plateType)
  {
    _cherryPickRequest = cherryPickRequest;
    _plateOrdinal = plateOrdinal;
    _attemptOrdinal = attemptOrdinal;
    _plateType = plateType;
  }


  // protected constructor

  /**
   * Construct an uninitialized <code>CherryPickAssayPlate</code>.
   * @motivation for hibernate. protected and not private for subclasses.
   */
  protected CherryPickAssayPlate() {}


  // private methods

  /**
   * Get the id for the cherry pick assay plate.
   * @param cherryPickAssayPlateId the new id for the cherry pick assay plate
   */
  private void setCherryPickAssayPlateId(Integer cherryPickAssayPlateId)
  {
    _cherryPickAssayPlateId = cherryPickAssayPlateId;
  }

  /**
   * Get the version for the cherry pick request.
   * @return the version for the cherry pick request
   * @motivation for hibernate
   */
  @Column(nullable=false)
  @Version
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * Set the version for the cherry pick request.
   * @param version the new version for the cherry pick request
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }

  /**
   * Set the cherry pick request
   * @param cherryPickRequest the new cherry pickr request
   * @motivation for hibernate
   */
  private void setCherryPickRequest(CherryPickRequest cherryPickRequest)
  {
    _cherryPickRequest = cherryPickRequest;
  }

  /**
   * Set the plate ordinal
   * @param plateOrdinal the new plate ordinal
   * @motivation for hibernate
   */
  private void setPlateOrdinal(Integer plateOrdinal)
  {
    _plateOrdinal = plateOrdinal;
  }

  /**
   * Set the attempt ordinal.
   * @param attemptOrdinal the new attempt ordinal
   * @motivation for hibernate
   */
  private void setAttemptOrdinal(Integer attemptOrdinal)
  {
    _attemptOrdinal = attemptOrdinal;
  }

  /**
   * Set the lab cherry picks mapped onto this cherry pick assay plate.
   * @param labCherryPicks the new lab cherry picks mapped onto this cherry pick assay plate
   * @motivation for hibernate
   */
  private void setLabCherryPicks(Set<LabCherryPick> labCherryPicks)
  {
    _labCherryPicks = labCherryPicks;
  }

  /**
   * Set the assay plate type.
   * @param plateType the new assay plate type
   * @motivation for hibernate
   */
  private void setAssayPlateType(PlateType plateType)
  {
    _plateType = plateType;
  }
}

