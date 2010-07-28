// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.SemanticIDAbstractEntity;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.annotations.ContainedEntity;
import edu.harvard.med.screensaver.model.annotations.ToMany;
import edu.harvard.med.screensaver.model.meta.Cardinality;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;


/**
 * A set of plates representing a copy of a {@link Library}'s contents. A facility creates and uses
 * multiple copies of a library, rather than working with a single master copy, in
 * order to reduce reagent freeze/thaw cycles, minimize the impact of loss due to a
 * physical loss, etc.  Note that in the Screensaver domain model, a library Copy represents
 * the physical instances of library plates that exist in reality.  A copy is a physical manifestation 
 * of a {@link Library}, which only specifies the layout of reagents across a 
 * set plates.  Therefore, even if a facility decided to work with a single, master set of library 
 * plates, in Screensaver one would still have to define a single Copy for this master set of plates. 
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
@Entity
@Table(uniqueConstraints={ @UniqueConstraint(columnNames={ "libraryId", "name" }) })
@org.hibernate.annotations.Proxy
@ContainedEntity(containingEntityClass=Library.class)
public class Copy extends SemanticIDAbstractEntity<String> implements Comparable<Copy>
{
  private static final long serialVersionUID = 0L;
  
  public static final RelationshipPath<Copy> plates = RelationshipPath.from(Copy.class).to("plates");
  public static final RelationshipPath<Copy> library = RelationshipPath.from(Copy.class).to("library", Cardinality.TO_ONE);

  public static final Function<Copy,String> ToName = new Function<Copy,String>() { public String apply(Copy c) { return c.getName(); } };
  public static final Function<Copy,Library> ToLibrary = new Function<Copy,Library>() { public Library apply(Copy c) { return c.getLibrary(); } };


  private Integer _version;
  private Library _library;
  private String _name;
  private CopyUsageType _usageType;
  private Map<Integer,Plate> _plates = Maps.newHashMap();


  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  public int compareTo(Copy other) 
  { 
    return this.getCopyId().compareTo(other.getCopyId()); 
  }
  
  /**
   * Get the id for the copy.
   * @return the id for the copy
   */
  @Id
  @org.hibernate.annotations.Type(type="text")
  public String getCopyId()
  {
    return getEntityId();
  }

  /**
   * Get the library.
   * @return the library
   */
  @ManyToOne
  @JoinColumn(name="libraryId", nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_copy_to_library")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @edu.harvard.med.screensaver.model.annotations.ToOne(inverseProperty="copies")
  public Library getLibrary()
  {
    return _library;
  }


  /**
   * @motivation for hibernate
   */
  private void setLibrary(Library library)
  {
    _library = library;
  }

  @OneToMany(
    mappedBy="copy",
    cascade={ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE },
    fetch=FetchType.LAZY
  )
  @MapKey(name="plateNumber")
  @ToMany(hasNonconventionalMutation=true /*Maps not yet supported by automated model testing framework*/)
  @org.hibernate.annotations.Cascade(value={org.hibernate.annotations.CascadeType.SAVE_UPDATE, 
                                            org.hibernate.annotations.CascadeType.DELETE})
  public Map<Integer,Plate> getPlates()
  {
    return _plates;
  }

  private void setPlates(Map<Integer,Plate> plates)
  {
    _plates = plates;
  }

  public Plate createPlate(
    Integer plateNumber,
    String location,
    PlateType plateType,
    Volume volume)
  {
    Plate plate = new Plate(this, plateNumber, location, plateType, volume);
    if (getLibrary().getStartPlate() > plateNumber || getLibrary().getEndPlate() < plateNumber) {
      throw new DataModelViolationException("plate number " + plateNumber + 
                                            " is outside of library plate range (" + 
                                            getLibrary().getStartPlate() + ".." + getLibrary().getEndPlate() + ")");
    }
    if (_plates.containsKey(plateNumber)) {
      throw new DuplicateEntityException(this, plate);
    }
    _plates.put(plateNumber, plate);
    return plate;
  }

  @Column(nullable=false)
  @org.hibernate.annotations.Type(type="text")
  @org.hibernate.annotations.Immutable
  public String getName()
  {
    return _name;
  }

  private void setName(String name)
  {
    _name = name;
  }

  @Column(nullable=false)
  @org.hibernate.annotations.Type(
    type="edu.harvard.med.screensaver.model.libraries.CopyUsageType$UserType"
  )
  @org.hibernate.annotations.Immutable
  public CopyUsageType getUsageType()
  {
    return _usageType;
  }

  private void setUsageType(CopyUsageType copyUsageType)
  {
    _usageType = copyUsageType;
  }

  /**
   * @motivation intended for use by {@link Library#createCopy} only.
   */
  Copy(Library library, CopyUsageType usageType, String name)
  {
    setEntityId(library.getShortName() + ":" + name);
    _library = library;
    _name = name;
    _usageType = usageType;
  }


  /**
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected Copy() {}

  /**
   * @motivation for hibernate
   */
  private void setCopyId(String copyId)
  {
    setEntityId(copyId);
  }

  /**
   * Get the version for the copy.
   * @return the version for the copy
   * @motivation for hibernate
   */
  @Version
  @Column(nullable=false)
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }
}
