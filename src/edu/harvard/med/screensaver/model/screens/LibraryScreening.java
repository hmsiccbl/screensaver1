// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Transient;

import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.joda.time.LocalDate;


/**
 * A Hibernate entity bean representing a library screening. This is screening
 * that is performed against <i>full copies</i> of the plates of one or more
 * libraries. (Consider that a screening could also be performed against a
 * selected subset of the wells from a library, as is the case with
 * {@link CherryPickScreening}.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@PrimaryKeyJoinColumn(name="activityId")
@org.hibernate.annotations.ForeignKey(name="fk_library_screening_to_activity")
@org.hibernate.annotations.Proxy
@edu.harvard.med.screensaver.model.annotations.ContainedEntity(containingEntityClass=Screen.class)
public class LibraryScreening extends Screening
{

  // private static data

  private static final long serialVersionUID = 0L;
  private static final Logger log = Logger.getLogger(LibraryScreening.class);

  public static final String ACTIVITY_TYPE_NAME = "Library Screening";
  private static final String SPECIAL_LIBRARY_SCREENING_ACTIVITY_TYPE_NAME = ACTIVITY_TYPE_NAME + " (screener-provided plates)";

  
  // private instance data

  private SortedSet<PlatesUsed> _platesUsed = new TreeSet<PlatesUsed>();
  private String _abaseTestsetId;
  private boolean _isSpecial;


  // public instance methods

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  @Override
  @Transient
  public String getActivityTypeName()
  {
    if (isSpecial()) {
      return SPECIAL_LIBRARY_SCREENING_ACTIVITY_TYPE_NAME;
    }
    return ACTIVITY_TYPE_NAME;
  }

  /**
   * Get the plates used.
   * @return the plates used
   */
  @OneToMany(
    mappedBy="libraryScreening",
    cascade={ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE },
    fetch=FetchType.LAZY
  )
  @Sort(type=SortType.NATURAL)
  @org.hibernate.annotations.Cascade(value={
    org.hibernate.annotations.CascadeType.SAVE_UPDATE,
    org.hibernate.annotations.CascadeType.DELETE_ORPHAN
  })
  @edu.harvard.med.screensaver.model.annotations.ToMany(singularPropertyName="platesUsed")
  public SortedSet<PlatesUsed> getPlatesUsed()
  {
    return _platesUsed;
  }

  /**
   * Create and return a new plates used for the library screening.
   * @param startPlate the start plate
   * @param endPlate the end plate
   * @param copy the copy
   * @return a new plates used for the library screening
   */
  public PlatesUsed createPlatesUsed(Integer startPlate, Integer endPlate, Copy copy)
  {
    PlatesUsed platesUsed = new PlatesUsed(this, startPlate, endPlate, copy);
    _platesUsed.add(platesUsed);
    return platesUsed;
  }

  /**
   * Get the abase testset id
   * @return the abase testset id
   */
  @org.hibernate.annotations.Type(type="text")
  public String getAbaseTestsetId()
  {
    return _abaseTestsetId;
  }

  /**
   * Set the abase testset id
   * @param abaseTestsetId the new abase testset id
   */
  public void setAbaseTestsetId(String abaseTestsetId)
  {
    _abaseTestsetId = abaseTestsetId;
  }

  /**
   * Get the is special boolean flag.
   * @return the is special boolean flag
   */
  @Column(name="isSpecial", nullable=false)
  // TODO: rename property to isScreenerProvidedPlates
  public boolean isSpecial()
  {
    return _isSpecial;
  }

  /**
   * Set the is special boolean flag.
   * @param isSpecial the new value for the is special boolean flag
   */
  public void setSpecial(boolean isSpecial)
  {
    _isSpecial = isSpecial;
  }


  // package constructor

  /**
   * Construct an initialized <code>LibraryScreening</code>. Intended only for use by {@link Screen}.
   * @param screen the screen
   * @param performedBy the user that performed the library assay
   * @param dateOfActivity
   */
  LibraryScreening(Screen screen,
                   AdministratorUser recordedBy,
                   ScreeningRoomUser performedBy,
                   LocalDate dateOfActivity)
  {
    super(screen, recordedBy, performedBy, dateOfActivity);
  }


  // protected constructor

  /**
   * Construct an uninitialized <code>LibraryScreening</code> object.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected LibraryScreening() {}


  // private instance methods

  /**
   * Set the plates used.
   * @param platesUsed the new plates used
   * @motivation for hibernate
   */
  private void setPlatesUsed(SortedSet<PlatesUsed> platesUsed)
  {
    _platesUsed = platesUsed;
  }
}
