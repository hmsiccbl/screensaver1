// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.DerivedEntityProperty;
import edu.harvard.med.screensaver.model.EntityIdProperty;
import edu.harvard.med.screensaver.model.ImmutableProperty;
import edu.harvard.med.screensaver.model.ToManyRelationship;
import edu.harvard.med.screensaver.model.ToOneRelationship;
import edu.harvard.med.screensaver.model.libraries.PlateType;

import org.apache.log4j.Logger;

/**
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @hibernate.class lazy="false" discriminator-value="false"
 * @hibernate.discriminator column="is_legacy" type="boolean" not-null="true"
 */
public class CherryPickAssayPlate extends AbstractEntity implements Comparable<CherryPickAssayPlate>
{
  // static members

  private static final long serialVersionUID = 1L;
  private static Logger log = Logger.getLogger(CherryPickAssayPlate.class);


  // instance data members

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

  
  // public constructors and methods

  public CherryPickAssayPlate(CherryPickRequest cherryPickRequest,
                              Integer plateOrdinal,
                              Integer attemptOrdinal,
                              PlateType plateType)
  {
    _cherryPickRequest = cherryPickRequest;
    _plateOrdinal = plateOrdinal;
    _attemptOrdinal = attemptOrdinal;
    _plateType = plateType;
    _cherryPickRequest.addCherryPickAssayPlate(this);
  }

  @Override
  public Serializable getEntityId()
  {
    return _cherryPickAssayPlateId;
  }

  /**
   * Get the id for the cherry pick assay plate.
   * 
   * @return the id for the cherry pick assay plate
   * @hibernate.id generator-class="sequence"
   * @hibernate.generator-param name="sequence"
   *                            value="cherry_pick_assay_plate_id_seq"
   */
  public Integer getCherryPickAssayPlateId()
  {
    return _cherryPickAssayPlateId;
  }

  /**
   * @hibernate.many-to-one class="edu.harvard.med.screensaver.model.screens.CherryPickRequest"
   *                        column="cherry_pick_request_id" not-null="true"
   *                        foreign-key="fk_cherry_pick_assay_plate_to_cherry_pick_request"
   *                        cascade="none"
   * @return
   */
  @ToOneRelationship(nullable = false)
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
   * 
   * @hibernate.property type="integer" not-null="true"
   */
  @EntityIdProperty
  public Integer getPlateOrdinal()
  {
    return _plateOrdinal;
  }

  /**
   * Get the attempt ordinal, which signifies that this is the n'th attempt (by
   * the lab) at creating a given logical cherry pick assay plate (i.e., for the
   * same plate ordinal). This value is a zero-based index.
   * 
   * @hibernate.property type="integer" not-null="true"
   */
  @EntityIdProperty
  public Integer getAttemptOrdinal()
  {
    return _attemptOrdinal;
  }
  
  @DerivedEntityProperty
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
   * @hibernate.set cascade="none" inverse="true"
   * @hibernate.collection-key column="cherry_pick_assay_plate_id"
   * @hibernate.collection-one-to-many class="edu.harvard.med.screensaver.model.screens.LabCherryPick"
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  @ToManyRelationship(inverseProperty = "assayPlate")
  public Set<LabCherryPick> getLabCherryPicks()
  {
    return _labCherryPicks;
  }

  public boolean addLabCherryPick(LabCherryPick labCherryPick)
  {
    if (labCherryPick.getAssayPlate() != null && !labCherryPick.getAssayPlate()
                                                               .equals(this)) {
      throw new DataModelViolationException("lab cherry pick is already mapped to another cherry pick assay plate");
    }
    labCherryPick.setAssayPlate(this);
    return _labCherryPicks.add(labCherryPick);
  }
  
  @DerivedEntityProperty
  public String getStatusLabel() 
  {
    return isPlated() ? "Plated" : isFailed() ? "Failed" : isCanceled() ? "Canceled" : "Not Plated";
  }

  @DerivedEntityProperty
  public boolean isCanceled()
  {
    return _cherryPickLiquidTransfer != null && _cherryPickLiquidTransfer.isCanceled();
  }
  
  @DerivedEntityProperty
  public boolean isPlated()
  {
    return _cherryPickLiquidTransfer != null && _cherryPickLiquidTransfer.isSuccessful();
  }

  @DerivedEntityProperty
  public boolean isFailed()
  {
    return _cherryPickLiquidTransfer != null && _cherryPickLiquidTransfer.isFailed();
  }

  /**
   * Get the cherry pick liquid transfer that marks that this plate has been
   * created.
   * 
   * @return a CherryPickLiquidTransfer
   * @hibernate.many-to-one class="edu.harvard.med.screensaver.model.screens.CherryPickLiquidTransfer"
   *                        column="cherry_pick_liquid_transfer_id"
   *                        not-null="false"
   *                        foreign-key="fk_cherry_pick_assay_plate_to_cherry_pick_liquid_transfer"
   *                        cascade="all"
   */
  @ToOneRelationship(nullable = true)
  private CherryPickLiquidTransfer getHbnCherryPickLiquidTransfer()
  {
    return _cherryPickLiquidTransfer;
  }

  private void setHbnCherryPickLiquidTransfer(CherryPickLiquidTransfer cherryPickLiquidTransfer)
  {
    _cherryPickLiquidTransfer = cherryPickLiquidTransfer;
  }

  /**
   * Get the cherry pick liquid transfer that marks that this plate has been
   * created.
   * 
   * @return a CherryPickLiquidTransfer
   */
  public CherryPickLiquidTransfer getCherryPickLiquidTransfer()
  {
    return _cherryPickLiquidTransfer;
  }

  public void setCherryPickLiquidTransfer(CherryPickLiquidTransfer cherryPickLiquidTransfer)
  {
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
      _cherryPickLiquidTransfer.getHbnCherryPickAssayPlates()
                               .remove(this);
    }
    _cherryPickLiquidTransfer = cherryPickLiquidTransfer;
    if (_cherryPickLiquidTransfer != null) {
      _cherryPickLiquidTransfer.getHbnCherryPickAssayPlates()
                               .add(this);
    }
  }

  // HACK: annotating as DerivedEntityProperty to prevent unit tests from
  // expecting a setter method (setAllocated() updates this property's value)
  /**
   * Get the assay plate type
   * 
   * @return the assay plate type
   * @hibernate.property type="edu.harvard.med.screensaver.model.libraries.PlateType$UserType"
   */
  @ImmutableProperty
  public PlateType getAssayPlateType()
  {
    return _plateType;
  }
  
  @DerivedEntityProperty
  public Set<Integer> getSourcePlates()
  {
    Set<Integer> sourcePlates = new HashSet<Integer>();
    for (LabCherryPick labCherryPick : _labCherryPicks) {
      sourcePlates.add(labCherryPick.getSourceWell().getPlateNumber());
    }
    return sourcePlates;
  }

  public int compareTo(CherryPickAssayPlate that)
  {
    return this._plateOrdinal < that._plateOrdinal ? -1 : 
      this._plateOrdinal > that._plateOrdinal ? 1 : 
        this._attemptOrdinal < that._attemptOrdinal ? -1 :
          this._attemptOrdinal > that._attemptOrdinal ? 1 : 0;
  }

  @Override
  public Object clone() 
  {
    return 
    new CherryPickAssayPlate(getCherryPickRequest(),
                             getPlateOrdinal(), 
                             getAttemptOrdinal() + 1,
                             getAssayPlateType());
  }
  
  
  // protected methods

  @Override
  protected Object getBusinessKey()
  {
    return new BusinessKey();
  }

  /**
   * A business key class for the CherryPickRequest.
   */
  private class BusinessKey
  {
    public CherryPickRequest getCherryPickRequest()
    {
      return _cherryPickRequest;
    }

    public Integer getPlateOrdinal()
    {
      return _plateOrdinal;
    }

    public Integer getAttemptOrdinal()
    {
      return _attemptOrdinal;
    }

    @Override
    public boolean equals(Object object)
    {
      if (!(object instanceof BusinessKey)) {
        return false;
      }
      BusinessKey that = (BusinessKey) object;
      return this.getCherryPickRequest().equals(that.getCherryPickRequest()) &&
             this.getPlateOrdinal().equals(that.getPlateOrdinal()) &&
             this.getAttemptOrdinal().equals(that.getAttemptOrdinal());
    }

    @Override
    public int hashCode()
    {
      return this.getCherryPickRequest().hashCode() * 117 +
      this.getPlateOrdinal().hashCode() * 61 +
      this.getAttemptOrdinal().hashCode() * 17;
    }

    @Override
    public String toString()
    {
      return this.getCherryPickRequest() + ":" + this.getPlateOrdinal() + "." + this.getAttemptOrdinal();
    }
  }


  // private methods

  /**
   * @motivation for hibernate
   */
  protected CherryPickAssayPlate()
  {}

  /**
   * Set the unique identifier for the <code>CherryPickAssayPlate</code>.
   * 
   * @param cherryPickAssayPlateId a unique identifier for the
   *          <code>CherryPickAssayPlate</code>
   */
  private void setCherryPickAssayPlateId(Integer cherryPickAssayPlateId)
  {
    _cherryPickAssayPlateId = cherryPickAssayPlateId;
  }

  /**
   * Get the version for the cherry pick request.
   * 
   * @return the version for the cherry pick request
   * @motivation for hibernate
   * @hibernate.version
   */
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * Set the version for the cherry pick request.
   * 
   * @param version the new version for the cherry pick request
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }

  private void setCherryPickRequest(CherryPickRequest cherryPickRequest)
  {
    _cherryPickRequest = cherryPickRequest;
  }

  private void setPlateOrdinal(Integer plateOrdinal)
  {
    _plateOrdinal = plateOrdinal;
  }

  private void setAttemptOrdinal(Integer attemptOrdinal)
  {
    _attemptOrdinal = attemptOrdinal;
  }

  private void setLabCherryPicks(Set<LabCherryPick> labCherryPicks)
  {
    _labCherryPicks = labCherryPicks;
  }

  private void setAssayPlateType(PlateType plateType)
  {
    _plateType = plateType;
  }

  
}

