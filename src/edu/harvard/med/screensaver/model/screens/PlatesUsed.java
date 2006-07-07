// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;


import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.AbstractEntity;


/**
 * A Hibernate entity bean representing a plates used.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @hibernate.class lazy="false"
 */
public class PlatesUsed extends AbstractEntity
{
  
  // static fields

  private static final Logger log = Logger.getLogger(PlatesUsed.class);
  private static final long serialVersionUID = 0L;


  // instance fields

  private Integer _platesUsedId;
  private Integer _version;
  private NonCherryPickVisit _visit;
  private String _startPlate;
  private String _endPlate;
  private String _copy;


  // public constructor

  /**
   * Constructs an initialized <code>PlatesUsed</code> object.
   *
   * @param visit the visit
   * @param startPlate the start plate
   * @param endPlate the end plate
   * @param copy the copy
   */
  public PlatesUsed(
    NonCherryPickVisit visit,
    String startPlate,
    String endPlate,
    String copy)
  {
    // TODO: verify the order of assignments here is okay
    _visit = visit;
    _startPlate = startPlate;
    _endPlate = endPlate;
    _copy = copy;
  }


  // public methods

  @Override
  public Integer getEntityId()
  {
    return getPlatesUsedId();
  }

  /**
   * Get the id for the plates used.
   *
   * @return the id for the plates used
   * @hibernate.id generator-class="sequence"
   * @hibernate.generator-param name="sequence" value="plates_used_id_seq"
   */
  public Integer getPlatesUsedId()
  {
    return _platesUsedId;
  }

  /**
   * Get the visit.
   *
   * @return the visit
   */
  public NonCherryPickVisit getVisit()
  {
    return _visit;
  }

  /**
   * Set the visit.
   *
   * @param visit the new visit
   */
  public void setVisit(NonCherryPickVisit visit)
  {
    _visit = visit;
    visit.getHbnPlatesUsed().add(this);
  }

  /**
   * Get the start plate.
   *
   * @return the start plate
   * @hibernate.property
   *   type="text"
   *   not-null="true"
   */
  public String getStartPlate()
  {
    return _startPlate;
  }

  /**
   * Set the start plate.
   *
   * @param startPlate the new start plate
   */
  public void setStartPlate(String startPlate)
  {
    _startPlate = startPlate;
  }

  /**
   * Get the end plate.
   *
   * @return the end plate
   * @hibernate.property
   *   type="text"
   *   not-null="true"
   */
  public String getEndPlate()
  {
    return _endPlate;
  }

  /**
   * Set the end plate.
   *
   * @param endPlate the new end plate
   */
  public void setEndPlate(String endPlate)
  {
    _endPlate = endPlate;
  }

  /**
   * Get the copy.
   *
   * @return the copy
   * @hibernate.property
   *   type="text"
   *   not-null="true"
   */
  public String getCopy()
  {
    return _copy;
  }

  /**
   * Set the copy.
   *
   * @param copy the new copy
   */
  public void setCopy(String copy)
  {
    _copy = copy;
  }


  // protected methods

  /**
   * A business key class for the well.
   */
  private class BusinessKey
  {
    
    /**
     * Get the visit.
     *
     * @return the visit
     */
    public NonCherryPickVisit getVisit()
    {
      return _visit;
    }
    
    /**
     * Get the start plate.
     *
     * @return the start plate
     */
    public String getStartPlate()
    {
      return _startPlate;
    }

    @Override
    public boolean equals(Object object)
    {
      if (! (object instanceof BusinessKey)) {
        return false;
      }
      BusinessKey that = (BusinessKey) object;
      return
        getVisit().equals(that.getVisit()) &&
        getStartPlate().equals(that.getStartPlate());
    }

    @Override
    public int hashCode()
    {
      return
        getVisit().hashCode() +
        getStartPlate().hashCode();
    }

    @Override
    public String toString()
    {
      return getVisit() + ":" + getStartPlate();
    }
  }

  @Override
  protected Object getBusinessKey()
  {
    // TODO: assure changes to business key update relationships whose other side is many
    return new BusinessKey();
  }


  // package methods

  /**
   * Set the visit.
   * Throw a NullPointerException when the visit is null.
   *
   * @param visit the new visit
   * @throws NullPointerException when the visit is null
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  void setHbnVisit(NonCherryPickVisit visit)
  {
    if (visit == null) {
      throw new NullPointerException();
    }
    _visit = visit;
  }


  // private constructor

  /**
   * Construct an uninitialized <code>PlatesUsed</code> object.
   *
   * @motivation for hibernate
   */
  private PlatesUsed() {}


  // private methods

  /**
   * Set the id for the plates used.
   *
   * @param platesUsedId the new id for the plates used
   * @motivation for hibernate
   */
  private void setPlatesUsedId(Integer platesUsedId) {
    _platesUsedId = platesUsedId;
  }

  /**
   * Get the version for the plates used.
   *
   * @return the version for the plates used
   * @motivation for hibernate
   * @hibernate.version
   */
  private Integer getVersion() {
    return _version;
  }

  /**
   * Set the version for the plates used.
   *
   * @param version the new version for the plates used
   * @motivation for hibernate
   */
  private void setVersion(Integer version) {
    _version = version;
  }

  /**
   * Get the visit.
   *
   * @return the visit
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.model.screens.NonCherryPickVisit"
   *   column="visit_id"
   *   not-null="true"
   *   foreign-key="fk_plates_used_to_visit"
   *   cascade="save-update"
   * @motivation for hibernate
   */
  private NonCherryPickVisit getHbnVisit()
  {
    return _visit;
  }
}
