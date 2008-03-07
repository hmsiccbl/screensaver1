// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.persistence.Version;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;

import org.apache.log4j.Logger;

/**
 * A Hibernate entity bean representing a plates used.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@org.hibernate.annotations.Proxy
@edu.harvard.med.screensaver.model.annotations.ContainedEntity(containingEntityClass=LibraryScreening.class)
public class PlatesUsed extends AbstractEntity
{

  // private static data

  private static final Logger log = Logger.getLogger(PlatesUsed.class);
  private static final long serialVersionUID = 0L;


  // private instance data

  private Integer _platesUsedId;
  private Integer _version;
  private LibraryScreening _libraryScreening;
  private Integer _startPlate;
  private Integer _endPlate;
  private String _copy;


  // public instance methods

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  @Override
  @Transient
  public Integer getEntityId()
  {
    return getPlatesUsedId();
  }

  /**
   * Get the id for the plates used.
   * @return the id for the plates used
   */
  @Id
  @org.hibernate.annotations.GenericGenerator(
    name="plates_used_id_seq",
    strategy="sequence",
    parameters = { @org.hibernate.annotations.Parameter(name="sequence", value="plates_used_id_seq") }
  )
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="plates_used_id_seq")
  public Integer getPlatesUsedId()
  {
    return _platesUsedId;
  }

  /**
   * Get the library screening for which these plates were used.
   * @return the library screening
   */
  @ManyToOne(fetch=FetchType.LAZY)
  @JoinColumn(name="libraryScreeningId", nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_plates_used_to_library_screening")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @edu.harvard.med.screensaver.model.annotations.ManyToOne(inverseProperty="platesUsed")
  public LibraryScreening getLibraryScreening()
  {
    return _libraryScreening;
  }

  /**
   * Get the start plate.
   * @return the start plate
   */
  @Column(nullable=false)
  public Integer getStartPlate()
  {
    return _startPlate;
  }

  /**
   * Set the start plate.
   * @param startPlate the new start plate
   */
  public void setStartPlate(Integer startPlate)
  {
    _startPlate = startPlate;
  }

  /**
   * Get the end plate.
   * @return the end plate
   */
  @Column(nullable=false)
  public Integer getEndPlate()
  {
    return _endPlate;
  }

  /**
   * Set the end plate.
   * @param endPlate the new end plate
   */
  public void setEndPlate(Integer endPlate)
  {
    _endPlate = endPlate;
  }

  /**
   * Get the copy.
   * @return the copy
   */
  @Column(nullable=false)
  @org.hibernate.annotations.Type(type="text")
  public String getCopy()
  {
    return _copy;
  }

  /**
   * Set the copy.
   * @param copy the new copy
   */
  public void setCopy(String copy)
  {
    _copy = copy;
  }


  // package constructor

  /**
   * Construct an initialized <code>PlatesUsed</code>. Intended only for use with {@link
   * LibraryScreening#createPlatesUsed(Integer, Integer, String)}.
   * @param libraryScreening the library screening
   * @param startPlate the start plate
   * @param endPlate the end plate
   * @param copy the copy
   */
  PlatesUsed(
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
  }


  // protected constructor

  /**
   * Construct an uninitialized <code>PlatesUsed</code>.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected PlatesUsed() {}


  // private constructor and instance methods

  /**
   * Set the id for the plates used.
   * @param platesUsedId the new id for the plates used
   * @motivation for hibernate
   */
  private void setPlatesUsedId(Integer platesUsedId)
  {
    _platesUsedId = platesUsedId;
  }

  /**
   * Set the library screening.
   * @param libraryScreening the library screening.
   * @motivation for hibernate
   */
  private void setLibraryScreening(LibraryScreening libraryScreening)
  {
    _libraryScreening = libraryScreening;
  }

  /**
   * Get the version for the plates used.
   * @return the version for the plates used
   * @motivation for hibernate
   */
  @Column(nullable=false)
  @Version
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * Set the version for the plates used.
   * @param version the new version for the plates used
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }
}
