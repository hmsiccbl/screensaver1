// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.DerivedEntityProperty;
import edu.harvard.med.screensaver.model.ImmutableProperty;
import edu.harvard.med.screensaver.model.ToManyRelationship;
import edu.harvard.med.screensaver.model.ToOneRelationship;
import edu.harvard.med.screensaver.model.screens.LabCherryPick;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.log4j.Logger;


/**
 * A Hibernate entity bean representing a copy, which is a set of plates
 * representing a copy of a library's contents. The lab works from library
 * copies, rather than directly from master library plates, in order to reduce
 * freeze/thaw cycles, minimize the impact of loss due to a physical loss, etc.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @hibernate.class lazy="false"
 */
public class Copy extends AbstractEntity
{
  
  // static fields

  private static final Logger log = Logger.getLogger(Copy.class);
  private static final long serialVersionUID = 0L;
  

  // instance fields

  private Integer _version;
  private Library _library;
  private Set<CopyInfo> _copyInfos = new HashSet<CopyInfo>();
  private String _name;
  private CopyUsageType _usageType;
  private Set<LabCherryPick> _labCherryPicks = new HashSet<LabCherryPick>();
  

  // public constructor

  /**
   * Constructs an initialized <code>Copy</code> object.
   *
   * @param library the library
   * @param name the name
   */
  public Copy(
    Library library,
    CopyUsageType usageType,
    String name)
  {
    _library = library;
    _name = name;
    _usageType = usageType;
    _library.getHbnCopies().add(this);
  }


  // public methods

  @Override
  public String getEntityId()
  {
    return getCopyId();
  }

  /**
   * Get the id for the copy.
   *
   * @return the id for the copy
   * @hibernate.id generator-class="assigned"
   */
  public String getCopyId()
  {
    return getBusinessKey().toString();
  }

  /**
   * Get the library.
   *
   * @return the library
   */
  @ToOneRelationship(nullable=false, inverseProperty="copies")
  public Library getLibrary()
  {
    return _library;
  }

  /**
   * Set the library.
   *
   * @param library the new library
   */
  public void setLibrary(Library library)
  {
    _library.getHbnCopies().remove(this);
    _library = library;
    library.getHbnCopies().add(this);
  }

  /**
   * Get this copy's usage type.
   * @return the copy's usage type.
   * @hibernate.property type="edu.harvard.med.screensaver.model.libraries.CopyUsageType$UserType"
   */
  @ImmutableProperty
  public CopyUsageType getUsageType()
  {
    return _usageType;
  }

  /**
   * Get an unmodifiable copy of the set of copy infos.
   *
   * @return the copy infos
   */
  public Set<CopyInfo> getCopyInfos()
  {
    return Collections.unmodifiableSet(_copyInfos);
  }

  public CopyInfo getCopyInfo(final Integer plateNumber)
  {
    return (CopyInfo) CollectionUtils.find(_copyInfos, new Predicate() 
    {
      public boolean evaluate(Object e) { return ((CopyInfo) e).getPlateNumber().equals(plateNumber); };
    });
//    return CollectionUtils.indexCollection(_copyInfos,
//                                           new Transformer() 
//    {
//      public Object transform(Object e)
//      {
//        return ((CopyInfo) e).getPlateNumber();
//      }
//    },
//    Integer.class,
//    CopyInfo.class).get(plate);
  }

  /**
   * Add the copy info.
   *
   * @param copyInfo the copy info to add
   * @return true iff the copy did not already have the copy info
   */
  public boolean addCopyInfo(CopyInfo copyInfo)
  {
    if (getHbnCopyInfos().add(copyInfo)) {
      copyInfo.setHbnCopy(this);
      return true;
    }
    return false;
  }

  /**
   * Get the name.
   *
   * @return the name
   */
  public String getName()
  {
    return _name;
  }

  /**
   * Set the name.
   *
   * @param name the new name
   */
  public void setName(String name)
  {
    _library.getHbnCopies().remove(this);
    _name = name;
    _library.getHbnCopies().add(this);
  }

  // HACK: This property is marked as derived for unit testing purposes
  // only! It is in fact a real hibernate relationship, though it is unique in that it can only be
  // modified from the other side (via CherryPick.setAllocated()). Our unit tests do
  // not yet handle this case.
  /**
   * Get an unmodifiable copy of the set of cherry picks.
   *
   * @return the cherry picks
   */
  @ToManyRelationship(inverseProperty="sourceCopy")
  @DerivedEntityProperty
  public Set<LabCherryPick> getLabCherryPicks()
  {
    return Collections.unmodifiableSet(_labCherryPicks);
  }

  
  // protected methods

  /**
   * A business key class for the well.
   */
  private class BusinessKey
  {
    
    /**
     * Get the library.
     *
     * @return the library
     */
    public Library getLibrary()
    {
      return _library;
    }
    
    /**
     * Get the name.
     *
     * @return the name
     */
    public String getName()
    {
      return _name;
    }

    @Override
    public boolean equals(Object object)
    {
      if (! (object instanceof BusinessKey)) {
        return false;
      }
      BusinessKey that = (BusinessKey) object;
      return
        this.getLibrary().equals(that.getLibrary()) &&
        this.getName().equals(that.getName());
    }

    @Override
    public int hashCode()
    {
      return
        this.getLibrary().hashCode() +
        this.getName().hashCode();
    }

    @Override
    public String toString()
    {
      return this.getLibrary().getShortName() + ":" + this.getName();
    }
  }

  @Override
  protected Object getBusinessKey()
  {
    return new BusinessKey();
  }


  // package methods

  /**
   * Set the library.
   * Throw a NullPointerException when the library is null.
   *
   * @param library the new library
   * @throws NullPointerException when the library is null
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  void setHbnLibrary(Library library)
  {
    if (library == null) {
      throw new NullPointerException();
    }
    _library = library;
  }

  /**
   * Get the copy infos.
   *
   * @return the copy infos
   * @hibernate.set
   *   cascade="save-update"
   *   inverse="true"
   *   lazy="true"
   * @hibernate.collection-key
   *   column="copy_id"
   * @hibernate.collection-one-to-many
   *   class="edu.harvard.med.screensaver.model.libraries.CopyInfo"
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  Set<CopyInfo> getHbnCopyInfos()
  {
    return _copyInfos;
  }


  // private constructor

  /**
   * Construct an uninitialized <code>Copy</code> object.
   *
   * @motivation for hibernate
   */
  private Copy() {}


  // private methods

  /**
   * Set the id for the copy.
   *
   * @param copyId the new id for the copy
   * @motivation for hibernate
   */
  private void setCopyId(String copyId)
  {
  }

  /**
   * Get the version for the copy.
   *
   * @return the version for the copy
   * @motivation for hibernate
   * @hibernate.version
   */
  private Integer getVersion() {
    return _version;
  }

  /**
   * Set the version for the copy.
   *
   * @param version the new version for the copy
   * @motivation for hibernate
   */
  private void setVersion(Integer version) {
    _version = version;
  }

  /**
   * Get the library.
   *
   * @return the library
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.model.libraries.Library"
   *   column="library_id"
   *   not-null="true"
   *   foreign-key="fk_copy_to_library"
   *   cascade="save-update"
   * @motivation for hibernate
   */
  private Library getHbnLibrary()
  {
    return _library;
  }

  /**
   * Set the copy infos.
   *
   * @param copyInfos the new copy infos
   * @motivation for hibernate
   */
  private void setHbnCopyInfos(Set<CopyInfo> copyInfos)
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
   * Get the name.
   *
   * @return the name
   * @hibernate.property
   *   column="name"
   *   type="text"
   *   not-null="true"
   * @motivation for hibernate
   */
  private String getHbnName()
  {
    return _name;
  }

  /**
   * Set the name.
   *
   * @param name the new name
   * @motivation for hibernate
   */
  private void setHbnName(String name)
  {
    _name = name;
  }
  

  /**
   * Get the cherry picks.
   * 
   * @return the cherry picks
   * @hibernate.set cascade="none" inverse="true" lazy="true"
   * @hibernate.collection-key column="copy_id"
   * @hibernate.collection-one-to-many class="edu.harvard.med.screensaver.model.screens.LabCherryPick"
   * @motivation for hibernate and maintenance of bi-directional relationships
   * public access for cross-package relationship
   */
  public Set<LabCherryPick> getHbnLabCherryPicks()
  {
    return _labCherryPicks;
  }

  /**
   * Set the cherry picks.
   *
   * @param labCherryPicks the new cherry picks
   * @motivation for hibernate
   */
  private void setHbnLabCherryPicks(Set<LabCherryPick> labCherryPicks)
  {
    _labCherryPicks = labCherryPicks;
  }
}
