// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.SemanticIDAbstractEntity;
import edu.harvard.med.screensaver.model.annotations.ContainedEntity;


/**
 * A Hibernate entity bean representing a copy, which is a set of plates
 * representing a copy of a library's contents. The lab works from library
 * copies, rather than directly from master library plates, in order to reduce
 * freeze/thaw cycles, minimize the impact of loss due to a physical loss, etc.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@org.hibernate.annotations.Proxy
@ContainedEntity(containingEntityClass=Library.class)
public class Copy extends SemanticIDAbstractEntity
{

  // static fields

  private static final Logger log = Logger.getLogger(Copy.class);
  private static final long serialVersionUID = 0L;


  // instance fields

  private String _copyId;
  private Integer _version;
  private Library _library;
  private Set<CopyInfo> _copyInfos = new HashSet<CopyInfo>();
  private String _name;
  private CopyUsageType _usageType;


  // public instance methods

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  @Override
  @Transient
  public String getEntityId()
  {
    return getCopyId();
  }

  /**
   * Get the id for the copy.
   * @return the id for the copy
   */
  @Id
  @org.hibernate.annotations.Type(type="text")
  public String getCopyId()
  {
    return _copyId;
  }

  /**
   * Get the library.
   * @return the library
   */
  @ManyToOne(cascade={ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE })
  @JoinColumn(name="libraryId", nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_copy_to_library")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @org.hibernate.annotations.Cascade(value={
    org.hibernate.annotations.CascadeType.SAVE_UPDATE,
    org.hibernate.annotations.CascadeType.DELETE
  })
  @edu.harvard.med.screensaver.model.annotations.ManyToOne(inverseProperty="copies")
  public Library getLibrary()
  {
    return _library;
  }

  /**
   * Get the set of copy infos.
   * @return the set of copy infos
   */
  @OneToMany(
    mappedBy="copy",
    cascade={ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE },
    fetch=FetchType.LAZY
  )
  @OrderBy("copyInfoId")
  @org.hibernate.annotations.Cascade(value={
    org.hibernate.annotations.CascadeType.SAVE_UPDATE,
    org.hibernate.annotations.CascadeType.DELETE
  })
  public Set<CopyInfo> getCopyInfos()
  {
    return _copyInfos;
  }

  /**
   * Get the copy info with the given plate number
   * @param plateNumber the plate number to get the copy info for
   * @return the copy info with the given plate number
   */
  @Transient
  public CopyInfo getCopyInfo(final Integer plateNumber)
  {
    // TODO: use a map instead of a set for Copy=>CopyInfo
    return (CopyInfo) CollectionUtils.find(_copyInfos, new Predicate()
    {
      public boolean evaluate(Object e) { return ((CopyInfo) e).getPlateNumber().equals(plateNumber); };
    });
  }

  /**
   * Create a new copy info for the copy.
   * @param plateNumber the plate number
   * @param location the location
   * @param plateType the plate type
   * @param volume the volume
   * @return the new copy info for the copy
   */
  public CopyInfo createCopyInfo(
    Integer plateNumber,
    String location,
    PlateType plateType,
    BigDecimal volume)
  {
    CopyInfo copyInfo = new CopyInfo(this, plateNumber, location, plateType, volume);
    if (! _copyInfos.add(copyInfo)) {
      throw new DuplicateEntityException(this, copyInfo);
    }
    return copyInfo;
  }

  /**
   * Get the name.
   * @return the name
   */
  @Column(nullable=false)
  @org.hibernate.annotations.Type(type="text")
  @org.hibernate.annotations.Immutable
  public String getName()
  {
    return _name;
  }

  /**
   * Get this copy's usage type.
   * @return the copy's usage type.
   */
  @Column(nullable=false)
  @org.hibernate.annotations.Type(
    type="edu.harvard.med.screensaver.model.libraries.CopyUsageType$UserType"
  )
  @org.hibernate.annotations.Immutable
  public CopyUsageType getUsageType()
  {
    return _usageType;
  }

  /**
   * Construct an initialized <code>Copy</code>.
   * @param library the library
   * @param usageType the copy usage type
   * @param name the name
   * @motivation intended for use by {@link Library#createCopy} only.
   */
  Copy(Library library, CopyUsageType usageType, String name)
  {
    _copyId = library.getShortName() + ":" + name;
    _library = library;
    _name = name;
    _usageType = usageType;
  }


  // protected constructor

  /**
   * Construct an uninitialized <code>Copy</code>.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected Copy() {}


  // private instance methods

  /**
   * Set the id for the copy.
   * @param copyId the new id for the copy
   * @motivation for hibernate
   */
  private void setCopyId(String copyId)
  {
    _copyId = copyId;
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
   * Set the version for the copy.
   * @param version the new version for the copy
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }

  /**
   * Set the library the well is in.
   * @param library the new library for the well
   * @motivation for hibernate
   */
  private void setLibrary(Library library)
  {
    _library = library;
  }

  /**
   * Set the copy infos.
   * @param copyInfos the new copy infos
   * @motivation for hibernate
   */
  private void setCopyInfos(Set<CopyInfo> copyInfos)
  {
    _copyInfos = copyInfos;
  }

  /**
   * Set this copy's usage type.
   * @param copyUsageType
   */
  private void setUsageType(CopyUsageType copyUsageType)
  {
    _usageType = copyUsageType;
  }

  /**
   * Set the name.
   * @param name the new name
   * @motivation for hibernate
   */
  private void setName(String name)
  {
    _name = name;
  }
}
