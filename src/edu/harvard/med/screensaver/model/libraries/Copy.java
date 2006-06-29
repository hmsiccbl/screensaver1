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

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.AbstractEntity;


/**
 * A Hibernate entity bean representing a copy.
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

  private Integer _copyId;
  private Integer _version;
  private Library _library;
  private Set<CopyInfo> _copyInfos = new HashSet<CopyInfo>();
  private String _name;


  // public constructor

  /**
   * Constructs an initialized <code>Copy</code> object.
   *
   * @param library the library
   * @param name the name
   */
  public Copy(
    Library library,
    String name)
  {
    _library = library;
    _name = name;
    _library.getHbnCopies().add(this);
  }


  // public methods

  @Override
  public Integer getEntityId()
  {
    return getCopyId();
  }

  /**
   * Get the id for the copy.
   *
   * @return the id for the copy
   * @hibernate.id generator-class="sequence"
   * @hibernate.generator-param name="sequence" value="copy_id_seq"
   */
  public Integer getCopyId()
  {
    return _copyId;
  }

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
   * Get an unmodifiable copy of the set of copy infos.
   *
   * @return the copy infos
   */
  public Set<CopyInfo> getCopyInfos()
  {
    return Collections.unmodifiableSet(_copyInfos);
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
        getLibrary().equals(that.getLibrary()) &&
        getName().equals(that.getName());
    }

    @Override
    public int hashCode()
    {
      return
        getLibrary().hashCode() +
        getName().hashCode();
    }

    @Override
    public String toString()
    {
      return getLibrary() + ":" + getName();
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
  private void setCopyId(Integer copyId) {
    _copyId = copyId;
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
}
