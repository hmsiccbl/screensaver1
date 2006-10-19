// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;


/**
 * A Hibernate entity bean representing a cherry pick visit.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @hibernate.subclass
 *   discriminator-value="true"
 *   lazy="false"
 */
public class CherryPickVisit extends Visit
{
  
  // static fields

  private static final Logger log = Logger.getLogger(CherryPickVisit.class);
  private static final long serialVersionUID = 0L;


  // instance fields

  private Set<CherryPick> _cherryPicks = new HashSet<CherryPick>();


  // public constructor

  /**
   * Constructs an initialized <code>CherryPickVisit</code> object.
   *
   * @param screen the screen
   * @param performedBy the user that performed the visit
   * @param dateCreated the date created
   * @param visitDate the visit date
   * @param visitType the visit type
   * @throws DuplicateEntityException 
   */
  public CherryPickVisit(
    Screen screen,
    ScreeningRoomUser performedBy,
    Date dateCreated,
    Date visitDate,
    VisitType visitType) throws DuplicateEntityException
  {
    super(screen, performedBy, dateCreated, visitDate, visitType);
  }


  // public methods

  /**
   * Get an unmodifiable copy of the set of cherry picks.
   *
   * @return the cherry picks
   */
  public Set<CherryPick> getCherryPicks()
  {
    return Collections.unmodifiableSet(_cherryPicks);
  }

  /**
   * Add the cherry pick.
   *
   * @param cherryPick the cherry pick to add
   * @return true iff the cherry pick visit did not already have the cherry pick
   */
  public boolean addCherryPick(CherryPick cherryPick)
  {
    if (getHbnCherryPicks().add(cherryPick)) {
      cherryPick.setHbnCherryPickVisit(this);
      return true;
    }
    return false;
  }


  // package methods

  /**
   * Get the cherry picks.
   *
   * @return the cherry picks
   * @hibernate.set
   *   cascade="save-update"
   *   inverse="true"
   * @hibernate.collection-key
   *   column="cherry_pick_visit_id"
   * @hibernate.collection-one-to-many
   *   class="edu.harvard.med.screensaver.model.screens.CherryPick"
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  Set<CherryPick> getHbnCherryPicks()
  {
    return _cherryPicks;
  }


  // private constructor

  /**
   * Construct an uninitialized <code>CherryPickVisit</code> object.
   *
   * @motivation for hibernate
   */
  private CherryPickVisit() {}


  // private methods

  /**
   * Set the cherry picks.
   *
   * @param cherryPicks the new cherry picks
   * @motivation for hibernate
   */
  private void setHbnCherryPicks(Set<CherryPick> cherryPicks)
  {
    _cherryPicks = cherryPicks;
  }
}
