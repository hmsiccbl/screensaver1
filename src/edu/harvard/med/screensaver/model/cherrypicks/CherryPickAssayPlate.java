// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.cherrypicks;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.model.meta.Cardinality;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.screens.CherryPickScreening;

/**
 * A CherryPickAssayPlate represents an assay plate that is created as the
 * result of {@link CherryPickRequest}. It parents the set of
 * {@link LabCherryPick LabCherryPicks} that define the "plate mapping" from
 * library copy source plates to the wells on the assay plate. A
 * CherryPickAssayPlate progresses through a range of states:
 * <p>
 * <table border="1">
 * <tr>
 * <td>State</td>
 * <td>Description</td>
 * <td>State Type</td>
 * <td>Valid Transition(s)</td>
 * <td>Affected Properties/Relationships</td>
 * </tr>
 * <tr>
 * <td>Not Plated</td>
 * <td>The assay plate has not yet been physically created in the lab.</td>
 * <td>Initial</td>
 * <td>Plated, Failed, Canceled</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>Plated</td>
 * <td>The assay plate has been physically created in the lab.</td>
 * <td>Final</td>
 * <td></td>
 * <td>cherryPickAssayPlate</td>
 * </tr>
 * <tr>
 * <td>Failed</td>
 * <td>The physical creation of the assay plate in the lab was attempted, but
 * failed. Liquid for each of its LabCPs was consumed in the failed attempt.</td>
 * <td>Final</td>
 * <td></td>
 * <td>cherryPickAssayPlate</td>
 * </tr>
 * <tr>
 * <td>Canceled</td>
 * <td>The assay plate has not yet been physically created in the lab.</td>
 * <td>Final</td>
 * <td></td>
 * <td>isCanceled</td>
 * </tr>
 * </table>
 * <p>
 * Note that business rules dictate that an assay plate is always created with
 * the set of lab cherry picks that are assigned to it, so there is no need for,
 * say, a "New" status.
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
@Table(uniqueConstraints={ @UniqueConstraint(columnNames={
  "cherryPickRequestId",
  "plateOrdinal",
  "attemptOrdinal"
}) })
@org.hibernate.annotations.Proxy
@edu.harvard.med.screensaver.model.annotations.ContainedEntity(containingEntityClass=CherryPickRequest.class)
public class CherryPickAssayPlate extends AbstractEntity<Integer> implements Comparable<CherryPickAssayPlate>
{

  // private static data

  private static final long serialVersionUID = 1L;
  private static Logger log = Logger.getLogger(CherryPickAssayPlate.class);

  public static final RelationshipPath<CherryPickAssayPlate> thisEntity = RelationshipPath.from(CherryPickAssayPlate.class);
  public static final RelationshipPath<CherryPickAssayPlate> cherryPickRequest = thisEntity.to("cherryPickRequest", Cardinality.TO_ONE);
  public static final RelationshipPath<CherryPickAssayPlate> cherryPickLiquidTransfer = thisEntity.to("cherryPickLiquidTransfer", Cardinality.TO_ONE);
  public static final RelationshipPath<CherryPickAssayPlate> cherryPickScreenings = thisEntity.to("cherryPickScreenings", Cardinality.TO_MANY);
  public static final RelationshipPath<CherryPickAssayPlate> labCherryPicks = thisEntity.to("labCherryPicks");

  // private instance data

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
  private SortedSet<CherryPickScreening> _cherryPickScreenings = Sets.newTreeSet();
  private transient int _logicalAssayPlateCount;
  private transient String _name;


  // public instance methods

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
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
    int plateOrdinalDifference = getPlateOrdinal() - that.getPlateOrdinal();
    if (plateOrdinalDifference != 0) {
      return plateOrdinalDifference;
    }
    return getAttemptOrdinal() - that.getAttemptOrdinal();
  }

  public String toString() { return super.toString() + ":" + _plateOrdinal + ":" + _attemptOrdinal; }

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
    return getEntityId();
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
      append(" (").append(_cherryPickRequest.getScreen().getFacilityId()).append(") ").
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
  @edu.harvard.med.screensaver.model.annotations.ToMany(hasNonconventionalMutation=true)
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
    return isPlatedAndScreened() ? "Screened" : isPlated() ? "Plated" : isFailed() ? "Failed" : isCancelled() ? "Canceled" : "Not Plated";
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
  
  @Transient
  public boolean isPlatedAndScreened()
  {
    return !_cherryPickScreenings.isEmpty();
  }

  /**
   * Get the cherry pick liquid transfer that marks that this plate has been created.
   * @return a CherryPickLiquidTransfer
   */
  @ManyToOne
  @JoinColumn(name="cherryPickLiquidTransferId", nullable=true)
  @org.hibernate.annotations.ForeignKey(name="fk_cherry_pick_assay_plate_to_cherry_pick_liquid_transfer")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
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

    _cherryPickLiquidTransfer = cherryPickLiquidTransfer;
    if (_cherryPickLiquidTransfer != null) {
      _cherryPickLiquidTransfer.getCherryPickAssayPlates().add(this);
    }
    if (cherryPickLiquidTransfer.getStatus() == CherryPickLiquidTransferStatus.SUCCESSFUL) {
      cherryPickLiquidTransfer.getScreen().invalidate();
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


  /**
   * Get the cherry pick screening activity that recorded the screening of this assay plate.
   */
  @ManyToMany
  @JoinTable(name = "cherryPickAssayPlateScreeningLink",
             joinColumns = @JoinColumn(name = "cherryPickAssayPlateId"),
             inverseJoinColumns = @JoinColumn(name = "cherryPickScreeningId"))
  /* has constraint that isPlated() == true */
  @edu.harvard.med.screensaver.model.annotations.ToMany(inverseProperty = "screensCollaborated", hasNonconventionalMutation = true)
  @Sort(type = SortType.NATURAL)
  public SortedSet<CherryPickScreening> getCherryPickScreenings()
  {
    return _cherryPickScreenings;
  }

  @Transient
  public CherryPickScreening getMostRecentCherryPickScreening()
  {
    if (_cherryPickScreenings.isEmpty()) {
      return null;
    }
    return _cherryPickScreenings.last();
  }

  private void setCherryPickScreenings(SortedSet<CherryPickScreening> cherryPickScreenings)
  {
    _cherryPickScreenings = cherryPickScreenings;
  }

  public boolean addCherryPickScreening(CherryPickScreening cherryPickScreening)
  {
    if (!_cherryPickScreenings.isEmpty() && !isPlated()) {
      throw new DataModelViolationException("cannot mark assay plate as \"screened\" unless it has been \"plated\"");
    }
    if (cherryPickScreening.getCherryPickAssayPlatesScreened().add(this)) {
      _cherryPickScreenings.add(cherryPickScreening);
      return true;
    }
    return false;
  }

  public boolean removeCherryPickScreening(CherryPickScreening cherryPickScreening)
  {
    if (cherryPickScreening.getCherryPickAssayPlatesScreened().remove(this)) {
      _cherryPickScreenings.remove(cherryPickScreening);
      return true;
    }
    return false;
  }

  /**
   * Construct an initialized <code>CherryPickAssayPlate</code>. Intended only for use by {@link CherryPickRequest}.
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
    setEntityId(cherryPickAssayPlateId);
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

