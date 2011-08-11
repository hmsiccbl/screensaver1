// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.hibernate.annotations.Parameter;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.AuditedAbstractEntity;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivity;
import edu.harvard.med.screensaver.model.annotations.ContainedEntity;
import edu.harvard.med.screensaver.model.annotations.ToOne;
import edu.harvard.med.screensaver.model.meta.Cardinality;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;

/**
 * A specific version of the reagent information for a library. Over time, the
 * information about a library's reagents may change due to 1) corrections of
 * identified errors in metadata provided by vendors (which has proven to not be
 * as rare as one would hope!), 2) updated information that becomes available as
 * scientific knowledge progresses (e.g. gene information can change
 * frequently). A LibraryContentsVersion thus maintains a snapshot of the
 * information that describes the reagents at a given point in time. The most
 * current version of a well's reagent is obtained via {@link Well#getLatestReleasedReagent()},
 * which defaults to that latest "released" library contents version (see
 * {@link Library#setLatestReleasedContentsVersion(LibraryContentsVersion)}). A
 * given, historical version of a well's reagent is obtained via
 * {@link Well#getReagents()}, using a {@link LibraryContentsVersion} as the
 * lookup key into the returned map.
 * <p/>
 * Note that this entity is NOT an {@link AuditedAbstractEntity}, since the
 * entity creation information is duplicated by {@link #getLoadingActivity()}
 * (and the loading activity additionally provides comments).
 * 
 * @author atolopko
 */
@Entity
@Table(uniqueConstraints={@UniqueConstraint(columnNames={"libraryId", "versionNumber"})})
@ContainedEntity(containingEntityClass = Library.class)
public class LibraryContentsVersion extends AbstractEntity<Integer> implements Comparable<LibraryContentsVersion>
{
  private static final long serialVersionUID = 1;

  public static final int FIRST_VERSION_NUMBER = 1;
  
  public static final RelationshipPath<LibraryContentsVersion> library = RelationshipPath.from(LibraryContentsVersion.class).to("library", Cardinality.TO_ONE);
  public static final RelationshipPath<LibraryContentsVersion> loadingActivity = RelationshipPath.from(LibraryContentsVersion.class).to("loadingActivity", Cardinality.TO_ONE);
  public static final RelationshipPath<LibraryContentsVersion> releaseActivity = RelationshipPath.from(LibraryContentsVersion.class).to("releaseActivity", Cardinality.TO_ONE);

  private Integer _version; /* for Hibernate optimistic locking */ 
  private Integer _versionNumber; /* for domain model */
  private Library _library;
  private AdministrativeActivity _loadingActivity;
  private AdministrativeActivity _releaseActivity;

  /** @motivation for CGLIB2 */
  protected LibraryContentsVersion() {}

  LibraryContentsVersion(Library library,
                         Integer versionNumber,
                         AdministrativeActivity loadingActivity)
  {
    _library = library;
    _versionNumber = versionNumber;
    _loadingActivity = loadingActivity;
  }

  @Id
  @org.hibernate.annotations.GenericGenerator(name = "library_contents_version_id_seq", 
                                              strategy = "sequence", 
                                              parameters = { @Parameter(name = "sequence", 
                                                                        value = "library_contents_version_id_seq") })
  @GeneratedValue(strategy = GenerationType.SEQUENCE, 
                  generator = "library_contents_version_id_seq")
  public Integer getLibraryContentsVersionId()
  {
    return getEntityId();
  }

  private void setLibraryContentsVersionId(Integer id)
  {
    setEntityId(id);
  }

  /**
   * @motivation for hibernate
   */
  @Version
  @Column(nullable = false)
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


  @Column(nullable = false, updatable=false)
  public Integer getVersionNumber()
  {
    return _versionNumber;
  }

  private void setVersionNumber(Integer versionNumber)
  {
    _versionNumber = versionNumber;
  }
  
  public void release(AdministrativeActivity releaseAdminActivity)
  {
    release(releaseAdminActivity, true);
  }

  public void release(AdministrativeActivity releaseAdminActivity, boolean updateInMemory)
  {
    if (_library.getLatestReleasedContentsVersion() != null && 
      _library.getLatestReleasedContentsVersion().getVersionNumber() >= getVersionNumber()) {
      throw new DataModelViolationException("can only release library contents versions with a greater version number than the currently released contents version");
    }
    if (isReleased()) {
      throw new DataModelViolationException("cannot release a library contents version more than once");
    }
    _library.setLatestReleasedContentsVersion(this);
    _releaseActivity = releaseAdminActivity;
    if (updateInMemory) {
      for (Well well : _library.getWells()) {
        Reagent reagent = well.getReagents().get(this);
        well.setLatestReleasedReagent(reagent);
      }
    }
  }
  
  public int compareTo(LibraryContentsVersion other)
  {
    assert _library.equals(other.getLibrary()) : "invalid to compare library versions from different libraries";
    return _versionNumber.compareTo(other.getVersionNumber());
  }

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  @ManyToOne
  @JoinColumn(name = "libraryId", nullable = false, updatable = false)
  @org.hibernate.annotations.ForeignKey(name = "fk_library_contents_version_to_library")
  @org.hibernate.annotations.LazyToOne(value = org.hibernate.annotations.LazyToOneOption.PROXY)
  @edu.harvard.med.screensaver.model.annotations.ToOne(inverseProperty = "contentsVersions")
  public Library getLibrary()
  {
    return _library;
  }

  private void setLibrary(Library library)
  {
    _library = library;
  }

  @OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE }, fetch = FetchType.LAZY)
  @ToOne(unidirectional=true)
  @JoinColumn(name = "libraryContentsLoadingActivityId", nullable = false, updatable = false)
  @org.hibernate.annotations.LazyToOne(value = org.hibernate.annotations.LazyToOneOption.PROXY)
  @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.DELETE })
  public AdministrativeActivity getLoadingActivity()
  {
    return _loadingActivity;
  }

  private void setLoadingActivity(AdministrativeActivity libraryContentsLoadingActivity)
  {
    _loadingActivity = libraryContentsLoadingActivity;
  }

  @OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE }, fetch = FetchType.LAZY)
  @ToOne(unidirectional=true, hasNonconventionalSetterMethod=true /* updated via release() instead */)
  @JoinColumn(name = "libraryContentsReleaseActivityId", nullable = true, updatable = true)
  @org.hibernate.annotations.LazyToOne(value = org.hibernate.annotations.LazyToOneOption.PROXY)
  @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.DELETE })
  public AdministrativeActivity getReleaseActivity()
  {
    return _releaseActivity;
  }
  
  @Transient
  public boolean isReleased()
  {
    return _releaseActivity != null;
  }

  private void setReleaseActivity(AdministrativeActivity libraryContentsReleaseActivity)
  {
    _releaseActivity = libraryContentsReleaseActivity;
  }
}
