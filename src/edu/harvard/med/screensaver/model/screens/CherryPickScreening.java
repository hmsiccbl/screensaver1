// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import java.util.SortedSet;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Transient;

import com.google.common.collect.Sets;
import org.joda.time.LocalDate;

import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.annotations.ToMany;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickAssayPlate;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.meta.Cardinality;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

/**
 * A library screening performed on set of cherry picks, as requested by a
 * screener after an initial screening has been performed.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@PrimaryKeyJoinColumn(name="activityId")
@org.hibernate.annotations.ForeignKey(name="fk_cherry_pick_screening_to_activity")
@org.hibernate.annotations.Proxy
@edu.harvard.med.screensaver.model.annotations.ContainedEntity(containingEntityClass=Screen.class)
public class CherryPickScreening extends Screening
{
  private static final long serialVersionUID = 1L;

  public static final String ACTIVITY_TYPE_NAME = "Cherry Pick Screening";

  public static final RelationshipPath<CherryPickScreening> cherryPickRequest = new RelationshipPath<CherryPickScreening>(CherryPickScreening.class, "cherryPickRequest", Cardinality.TO_ONE);
  public static final RelationshipPath<CherryPickScreening> assayPlatesScreened = new RelationshipPath<CherryPickScreening>(CherryPickScreening.class, "assayPlatesScreened");

  private CherryPickRequest _cherryPickRequest;
  private SortedSet<CherryPickAssayPlate> _assayPlatesScreened = Sets.newTreeSet();

  
  /**
   * Construct an initialized <code>CherryPickScreening</code>.
   * @param screen the screen
   * @param performedBy the user that performed the screening
   * @param dateOfActivity the date the screening took place
   * @param cherryPickRequest the cherry pick request
   */
  CherryPickScreening(Screen screen,
                      AdministratorUser recordedBy,
                      ScreeningRoomUser performedBy,
                      LocalDate dateOfActivity,
                      CherryPickRequest cherryPickRequest)
  {
    super(screen, recordedBy, performedBy, dateOfActivity);
    _cherryPickRequest = cherryPickRequest;
    _cherryPickRequest.getCherryPickScreenings().add(this);
  }


  /**
   * Construct an uninitialized <code>CherryPickScreening</code>.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected CherryPickScreening() {}

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  @Override
  @Transient
  public String getActivityTypeName()
  {
    return ACTIVITY_TYPE_NAME;
  }

  /**
   * Get the cherry pick request.
   * @return the cherry pick request
   */
  @ManyToOne(fetch=FetchType.LAZY)
  @JoinColumn(name="cherryPickRequestId", nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_cherry_pick_screening_to_cherry_pick_request")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @edu.harvard.med.screensaver.model.annotations.ToOne(inverseProperty="CherryPickScreenings")
  public CherryPickRequest getCherryPickRequest()
  {
    return _cherryPickRequest;
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
   * Get the plates used.
   * @return the plates used
   */
  @OneToMany(mappedBy="cherryPickScreening", cascade={}, fetch=FetchType.LAZY) /* note: no cascades, since CherryPickAssayPlate is managed by CherryPickRequest, not CherryPickScreeing */
  @org.hibernate.annotations.Sort(type=org.hibernate.annotations.SortType.NATURAL)
  @ToMany(singularPropertyName="assayPlateScreened", hasNonconventionalMutation=true /* has constraint that CPAP.isPlated()==true */ )
  public SortedSet<CherryPickAssayPlate> getAssayPlatesScreened()
  {
    return _assayPlatesScreened;
  }

  private void setAssayPlatesScreened(SortedSet<CherryPickAssayPlate> assayPlatesScreened)
  {
    _assayPlatesScreened = assayPlatesScreened;
  }
  
  public boolean addAssayPlateScreened(CherryPickAssayPlate assayPlateScreened)
  {
    if (assayPlateScreened.getCherryPickScreening() != null && assayPlateScreened.getCherryPickScreening() != this) {
      throw new DataModelViolationException(assayPlateScreened + " has already been assigned to " + assayPlateScreened.getCherryPickScreening());
    }
    if (_assayPlatesScreened.add(assayPlateScreened)) {
      assayPlateScreened.setCherryPickScreening(this);
      return true;
    }
    return false;
  }

  public boolean removeAssayPlateScreened(CherryPickAssayPlate assayPlateScreened)
  {
    if (_assayPlatesScreened.remove(assayPlateScreened)) {
      assayPlateScreened.setCherryPickScreening(null);
      return true;
    }
    return false;
  }
}