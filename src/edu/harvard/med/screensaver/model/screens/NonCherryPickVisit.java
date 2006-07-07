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

import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;


/**
 * A Hibernate entity bean representing a non-cherry pick visit.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @hibernate.subclass
 *   discriminator-value="false"
 *   lazy="false"
 */
public class NonCherryPickVisit extends Visit
{
  
  // static fields

  private static final Logger log = Logger.getLogger(NonCherryPickVisit.class);
  private static final long serialVersionUID = 0L;


  // instance fields

  private Set<PlatesUsed> _platesUsed = new HashSet<PlatesUsed>();
  private Set<EquipmentUsed> _equipmentUsed = new HashSet<EquipmentUsed>();
  private Integer _numberOfReplicates;
  private String _volumeOfCompoundTransferred;
  private AssayProtocolType _assayProtocolType;


  // public constructor

  /**
   * Constructs an initialized <code>NonCherryPickVisit</code> object.
   *
   * @param screen the screen
   * @param performedBy the user that performed the visit
   * @param dateCreated the date created
   * @param visitDate the visit date
   * @param visitType the visit type
   * @param assayProtocolType the assay protocol type
   */
  public NonCherryPickVisit(
    Screen screen,
    ScreeningRoomUser performedBy,
    Date dateCreated,
    Date visitDate,
    VisitType visitType,
    AssayProtocolType assayProtocolType)
  {
    super(screen, performedBy, dateCreated, visitDate, visitType);
    _assayProtocolType = assayProtocolType;
  }


  // public methods

  /**
   * Get an unmodifiable copy of the set of plates used.
   *
   * @return the plates used
   */
  public Set<PlatesUsed> getPlatesUsed()
  {
    return Collections.unmodifiableSet(_platesUsed);
  }

  /**
   * Add the plates use.
   *
   * @param platesUsed the plates use to add
   * @return true iff the non-cherry pick visit did not already have the plates use
   */
  public boolean addPlatesUsed(PlatesUsed platesUsed)
  {
    if (getHbnPlatesUsed().add(platesUsed)) {
      platesUsed.setHbnVisit(this);
      return true;
    }
    return false;
  }

  /**
   * Get an unmodifiable copy of the set of equipment used.
   *
   * @return the equipment used
   */
  public Set<EquipmentUsed> getEquipmentUsed()
  {
    return Collections.unmodifiableSet(_equipmentUsed);
  }

  /**
   * Add the equipment use.
   *
   * @param equipmentUsed the equipment use to add
   * @return true iff the non-cherry pick visit did not already have the equipment use
   */
  public boolean addEquipmentUsed(EquipmentUsed equipmentUsed)
  {
    if (getHbnEquipmentUsed().add(equipmentUsed)) {
      equipmentUsed.setHbnVisit(this);
      return true;
    }
    return false;
  }

  /**
   * Get the number of replicates.
   *
   * @return the number of replicates
   * @hibernate.property
   */
  public Integer getNumberOfReplicates()
  {
    return _numberOfReplicates;
  }

  /**
   * Set the number of replicates.
   *
   * @param numberOfReplicates the new number of replicates
   */
  public void setNumberOfReplicates(Integer numberOfReplicates)
  {
    _numberOfReplicates = numberOfReplicates;
  }

  /**
   * Get the volume of compound transferred.
   *
   * @return the volume of compound transferred
   * @hibernate.property
   *   type="text"
   */
  public String getVolumeOfCompoundTransferred()
  {
    return _volumeOfCompoundTransferred;
  }

  /**
   * Set the volume of compound transferred.
   *
   * @param volumeOfCompoundTransferred the new volume of compound transferred
   */
  public void setVolumeOfCompoundTransferred(String volumeOfCompoundTransferred)
  {
    _volumeOfCompoundTransferred = volumeOfCompoundTransferred;
  }

  /**
   * Get the assay protocol type.
   *
   * @return the assay protocol type
   * @hibernate.property
   *   type="edu.harvard.med.screensaver.model.screens.AssayProtocolType$UserType"
   */
  public AssayProtocolType getAssayProtocolType()
  {
    return _assayProtocolType;
  }

  /**
   * Set the assay protocol type.
   *
   * @param assayProtocolType the new assay protocol type
   */
  public void setAssayProtocolType(AssayProtocolType assayProtocolType)
  {
    _assayProtocolType = assayProtocolType;
  }


  // package methods

  /**
   * Get the plates used.
   *
   * @return the plates used
   * @hibernate.set
   *   cascade="save-update"
   *   inverse="true"
   * @hibernate.collection-key
   *   column="visit_id"
   * @hibernate.collection-one-to-many
   *   class="edu.harvard.med.screensaver.model.screens.PlatesUsed"
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  Set<PlatesUsed> getHbnPlatesUsed()
  {
    return _platesUsed;
  }

  /**
   * Get the equipment used.
   *
   * @return the equipment used
   * @hibernate.set
   *   cascade="save-update"
   *   inverse="true"
   * @hibernate.collection-key
   *   column="visit_id"
   * @hibernate.collection-one-to-many
   *   class="edu.harvard.med.screensaver.model.screens.EquipmentUsed"
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  Set<EquipmentUsed> getHbnEquipmentUsed()
  {
    return _equipmentUsed;
  }


  // private constructor

  /**
   * Construct an uninitialized <code>NonCherryPickVisit</code> object.
   *
   * @motivation for hibernate
   */
  private NonCherryPickVisit() {}


  // private methods

  /**
   * Set the plates used.
   *
   * @param platesUsed the new plates used
   * @motivation for hibernate
   */
  private void setHbnPlatesUsed(Set<PlatesUsed> platesUsed)
  {
    _platesUsed = platesUsed;
  }

  /**
   * Set the equipment used.
   *
   * @param equipmentUsed the new equipment used
   * @motivation for hibernate
   */
  private void setHbnEquipmentUsed(Set<EquipmentUsed> equipmentUsed)
  {
    _equipmentUsed = equipmentUsed;
  }
}
