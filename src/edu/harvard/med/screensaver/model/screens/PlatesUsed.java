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
import edu.harvard.med.screensaver.model.ToOneRelationship;

//TODO: this doesn't need to be a first-class entity; could be just a value-type for a collection in LibraryScreening

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
  private LibraryScreening _libraryScreening;
  private Integer _startPlate;
  private Integer _endPlate;
  private String _copy;


  // public constructor

  /**
   * Constructs an initialized <code>PlatesUsed</code> object.
   *
   * @param libraryScreening the library screening
   * @param startPlate the start plate
   * @param endPlate the end plate
   * @param copy the copy
   */
  public PlatesUsed(
    LibraryScreening libraryScreening,
    Integer startPlate,
    Integer endPlate,
    String copy)
  {
    if (libraryScreening == null) {
      throw new NullPointerException();
    }
    _libraryScreening = libraryScreening;
    _startPlate = startPlate;
    _endPlate = endPlate;
    _copy = copy;
    _libraryScreening.getPlatesUsed().add(this);
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
   * Get the library screening for which these plates were used.
   *
   * @return the library screening
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.model.screens.LibraryScreening"
   *   column="library_screening_id"
   *   not-null="true"
   *   foreign-key="fk_plates_used_to_library_screening"
   *   cascade="save-update"
   * @motivation for hibernate
   */
  @ToOneRelationship(nullable=false, inverseProperty="platesUsed")
  public LibraryScreening getLibraryScreening()
  {
    return _libraryScreening;
  }

  /**
   * Get the start plate.
   *
   * @return the start plate
   */
  public Integer getStartPlate()
  {
    return _startPlate;
  }

  /**
   * Set the start plate.
   *
   * @param startPlate the new start plate
   */
  public void setStartPlate(Integer startPlate)
  {
    _libraryScreening.getPlatesUsed().remove(this);
    _startPlate = startPlate;
    _libraryScreening.getPlatesUsed().add(this);
  }

  /**
   * Get the end plate.
   *
   * @return the end plate
   * @hibernate.property
   *   type="integer"
   *   not-null="true"
   */
  public Integer getEndPlate()
  {
    return _endPlate;
  }

  /**
   * Set the end plate.
   *
   * @param endPlate the new end plate
   */
  public void setEndPlate(Integer endPlate)
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
     * Get the library screening.
     *
     * @return the library screening
     */
    public LibraryScreening getLibraryScreening()
    {
      return _libraryScreening;
    }
    
    /**
     * Get the start plate.
     *
     * @return the start plate
     */
    public Integer getStartPlate()
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
        this.getLibraryScreening().equals(that.getLibraryScreening()) &&
        this.getStartPlate().equals(that.getStartPlate());
    }

    @Override
    public int hashCode()
    {
      return
        this.getLibraryScreening().hashCode() +
        this.getStartPlate().hashCode();
    }

    @Override
    public String toString()
    {
      return this.getLibraryScreening() + ":" + this.getStartPlate();
    }
  }

  @Override
  protected Object getBusinessKey()
  {
    return new BusinessKey();
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
   * Set the library screening.
   *
   * @param libraryScreening the library screening.
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  private void setLibraryScreening(LibraryScreening libraryScreening)
  {
    _libraryScreening = libraryScreening;
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

  private LibraryScreening get()
  {
    return _libraryScreening;
  }

  /**
   * Get the start plate.
   *
   * @return the start plate
   * @hibernate.property
   * 
   *   column="start_plate"
   *   not-null="true"
   * @motivation for hibernate
   */
  private Integer getHbnStartPlate()
  {
    return _startPlate;
  }

  /**
   * Set the start plate.
   *
   * @param startPlate the new start plate
   * @motivation for hibernate
   */
  private void setHbnStartPlate(Integer startPlate)
  {
    _startPlate = startPlate;
  }
}
