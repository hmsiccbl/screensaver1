// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.ImmutableProperty;
import edu.harvard.med.screensaver.model.ToManyRelationship;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;


/**
 * A Hibernate entity bean representing a library screening. This is screening
 * that is performed against <i>full copies</i> of the plates of one or more
 * libraries. (Consider that a screening could also be performed against a
 * selected subset of the wells from a library, as is the case with
 * {@link RNAiCherryPickScreening}.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @hibernate.joined-subclass table="library_screening" lazy="false"
 * @hibernate.joined-subclass-key column="screening_room_activity_id"
 */
public class LibraryScreening extends Screening
{
  
  // static fields

  private static final long serialVersionUID = 0L;
  private static final Logger log = Logger.getLogger(LibraryScreening.class);


  // instance fields

  private Set<PlatesUsed> _platesUsed = new HashSet<PlatesUsed>();
  private String _abaseTestsetId;
  private boolean _isSpecial;


  // public constructor

  /**
   * Constructs an initialized <code>LibraryScreening</code> object.
   *
   * @param screen the screen
   * @param performedBy the user that performed the library assay
   * @param dateCreated the date created
   * @param assayProtocolType the assay protocol type
   * @throws DuplicateEntityException 
   */
  public LibraryScreening(
    Screen screen,
    ScreeningRoomUser performedBy,
    Date dateCreated,
    Date dateOfActivity) throws DuplicateEntityException
  {
    super(screen, performedBy, dateCreated, dateOfActivity);
  }


  // public methods


  @Override
  @ImmutableProperty
  public String getActivityTypeName()
  {
    return "Library Screening";
  }

  /**
   * Get the plates used.
   *
   * @return the plates used
   * @hibernate.set
   *   cascade="save-update"
   *   inverse="true"
   * @hibernate.collection-key
   *   column="library_screening_id"
   * @hibernate.collection-one-to-many
   *   class="edu.harvard.med.screensaver.model.screens.PlatesUsed"
   */
  @ToManyRelationship(inverseProperty="libraryScreening")
  public Set<PlatesUsed> getPlatesUsed()
  {
    return _platesUsed;
  }

  /**
   * Get the abase testset id
   * @return the abase testset id
   * @hibernate.property type="text"
   */
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
   * @hibernate.property not-null="true"
   */
  public boolean getIsSpecial()
  {
    return _isSpecial;
  }

  /**
   * Set the is special boolean flag.
   * @param isSpecial the new value for the is special boolean flag
   */
  public void setIsSpecial(boolean isSpecial)
  {
    _isSpecial = isSpecial;
  }

  
  // private constructor
  
  /**
   * Construct an uninitialized <code>LibraryScreening</code> object.
   *
   * @motivation for hibernate
   */
  private LibraryScreening() {}


  // private methods

  /**
   * Set the plates used.
   *
   * @param platesUsed the new plates used
   * @motivation for hibernate
   */
  private void setPlatesUsed(Set<PlatesUsed> platesUsed)
  {
    _platesUsed = platesUsed;
  }
}
