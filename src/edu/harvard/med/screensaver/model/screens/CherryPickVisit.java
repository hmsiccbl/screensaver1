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

import org.apache.log4j.Logger;


/**
 * A Hibernate entity bean representing a cherry pick visit.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @hibernate.subclass
 *   lazy="false"
 *   discriminator-value="true"
 */
public class CherryPickVisit extends Visit
{
  
  // static fields

  private static final Logger log = Logger.getLogger(CherryPickVisit.class);
  private static final long serialVersionUID = 0L;


  // instance fields

  private String _volume;


  // public constructor

  /**
   * Constructs an initialized <code>CherryPickVisit</code> object.
   *
   * 
   */
  public CherryPickVisit(
    Date dateCreated,
    Date visitDate,
    VisitType visitType)
  {
    super(dateCreated, visitDate, visitType);
  }


  // public methods

  /**
   * Get the volume.
   *
   * @return the volume
   * @hibernate.property
   *   column="cpv_volume"
   *   type="text"
   */
  public String getVolume()
  {
    return _volume;
  }

  /**
   * Set the volume.
   *
   * @param volume the new volume
   */
  public void setVolume(String volume)
  {
    _volume = volume;
  }


  // package methods


  // private constructor

  /**
   * Construct an uninitialized <code>CherryPickVisit</code> object.
   *
   * @motivation for hibernate
   */
  private CherryPickVisit() {}
}
