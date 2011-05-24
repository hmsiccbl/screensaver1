// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.google.common.collect.Sets;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.MapKeyManyToMany;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AttachedFile;
import edu.harvard.med.screensaver.model.AttachedFileType;
import edu.harvard.med.screensaver.model.AttachedFilesEntity;
import edu.harvard.med.screensaver.model.annotations.ContainedEntity;
import edu.harvard.med.screensaver.model.annotations.ToMany;
import edu.harvard.med.screensaver.model.meta.Cardinality;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;
import edu.harvard.med.screensaver.model.screens.Publication;
import edu.harvard.med.screensaver.model.screens.Screen;


/**
 * A substance, such as a {@link SmallMoleculeReagent} or {@link SilencingReagent}, used to
 * test the response of a biological system to a specific perturbation. Reagents
 * are contained in {@link Library} {@link Well wells}.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@Immutable
@Table(uniqueConstraints={@UniqueConstraint(columnNames={"wellId", "libraryContentsVersionId"})})
@org.hibernate.annotations.Table(appliesTo = "reagent", indexes = {
  @Index(name = "reagent_vendor_identifier_index", columnNames = { "vendorIdentifier" }) })
@org.hibernate.annotations.Proxy(lazy=false) // proxying causes problems with casts of getLatestReleasedReagent() return value
@Inheritance(strategy=InheritanceType.JOINED)
@ContainedEntity(containingEntityClass=Well.class)
public abstract class Reagent extends AbstractEntity<Integer> implements Comparable<Reagent>, AttachedFilesEntity<Integer>
{
  private static final long serialVersionUID = 1;


  public static final RelationshipPath<Reagent> libraryContentsVersion = RelationshipPath.from(Reagent.class).to("libraryContentsVersion", Cardinality.TO_ONE);
  public static final RelationshipPath<Reagent> well = RelationshipPath.from(Reagent.class).to("well", Cardinality.TO_ONE);
  public static final RelationshipPath<Reagent> annotationValues = RelationshipPath.from(Reagent.class).to("annotationValues");
  public static final RelationshipPath<Reagent> studies = RelationshipPath.from(Reagent.class).to("studies");
  public static final PropertyPath<Reagent> vendorName = RelationshipPath.from(Reagent.class).toProperty("vendorId.vendorName");
  public static final PropertyPath<Reagent> vendorIdentifier = RelationshipPath.from(Reagent.class).toProperty("vendorId.vendorIdentifier");
  public static final RelationshipPath<Reagent> publications = RelationshipPath.from(Reagent.class).to("publications");
  public static final RelationshipPath<Reagent> attachedFiles = RelationshipPath.from(Reagent.class).to("attachedFiles");

  private LibraryContentsVersion _libraryContentsVersion;
  private Well _well;
  private ReagentVendorIdentifier _vendorId;
  private Map<AnnotationType,AnnotationValue> _annotationValues = new HashMap<AnnotationType,AnnotationValue>();
  private Set<Screen> _studies = new HashSet<Screen>();
  private Set<Publication> _publications = Sets.newHashSet();
  private Set<AttachedFile> _attachedFiles = new HashSet<AttachedFile>();


  /**
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected Reagent() {}

  /**
   * @motivation for {@link Library#createWell}
   */
  protected Reagent(ReagentVendorIdentifier reagentVendorIdentifier, Well well, LibraryContentsVersion libraryContentsVersion)
  {
    _vendorId = reagentVendorIdentifier;
    _well = well;
    _libraryContentsVersion = libraryContentsVersion;
  }

  public int compareTo(Reagent o)
  {
    return _vendorId.compareTo(o._vendorId);
  }

  @Id
  @org.hibernate.annotations.GenericGenerator(
    name="reagent_id_seq",
    strategy="sequence",
    parameters = { @org.hibernate.annotations.Parameter(name="sequence", value="reagent_id_seq") }
  )
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="reagent_id_seq")
  public Integer getReagentId()
  {
    return getEntityId();
  }

  private void setReagentId(Integer reagentId)
  {
    setEntityId(reagentId);
  }

  @Column
  public ReagentVendorIdentifier getVendorId()
  {
    if (_vendorId == null) {
      return ReagentVendorIdentifier.NULL_VENDOR_ID;
    }
    return _vendorId;
  }

  private void setVendorId(ReagentVendorIdentifier vendorId)
  {
    _vendorId = vendorId;
  }

  @ManyToOne(fetch=FetchType.LAZY)
  @JoinColumn(name="wellId", nullable=false, updatable=false)
  @org.hibernate.annotations.ForeignKey(name="fk_reagent_to_well")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  public Well getWell()
  {
    return _well;
  }

  private void setWell(Well well)
  {
    _well = well;
  }

  @ManyToOne(fetch=FetchType.LAZY)
  @JoinColumn(name="libraryContentsVersionId", nullable=false, updatable=false)
  @org.hibernate.annotations.ForeignKey(name="fk_reagent_to_library_contents_version")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  public LibraryContentsVersion getLibraryContentsVersion()
  {
    return _libraryContentsVersion;
  }

  private void setLibraryContentsVersion(LibraryContentsVersion libraryContentsVersion)
  {
    _libraryContentsVersion = libraryContentsVersion;
  }

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "reagent")
  @ToMany(hasNonconventionalMutation=true /* model unit tests don't handle Maps yet, tested in ReagentTest#testAnnotationValueMap */)
  /* @LazyCollection(LazyCollectionOption.EXTRA) */
  @MapKeyManyToMany(joinColumns = { @JoinColumn(name = "annotationTypeId") }, targetEntity = AnnotationType.class)
  public Map<AnnotationType,AnnotationValue> getAnnotationValues()
  {
    return _annotationValues;
  }

  @ManyToMany(targetEntity = Screen.class, mappedBy = "reagents", fetch = FetchType.LAZY)
  @ToMany(singularPropertyName = "study", hasNonconventionalMutation = true /*
                                                                             * model unit tests don't handle immutable
                                                                             * to-many relationships, tested in
                                                                             * ReagentTest#testAnnotationValueMap
                                                                             */)
  //@JoinColumn(name = "studyId", nullable = false, updatable = false)
  @org.hibernate.annotations.ForeignKey(name = "fk_reagent_to_study")
  @org.hibernate.annotations.LazyCollection(value = org.hibernate.annotations.LazyCollectionOption.TRUE)
  public Set<Screen> getStudies()
  {
    return _studies;
  }

  public boolean addStudy(Screen study)
  {
    if (_studies.add(study)) {
      study.addReagent(this);
      return true;
    }
    return false;
  }

  public boolean removeStudy(Screen study)
  {
    return removeStudy(study, true);
  }

  public boolean removeStudy(Screen study, boolean removeReagentStudyLink)
  {
    if (_studies.remove(study)) {
      if (removeReagentStudyLink) study.removeReagent(this);
      return true;
    }
    return false;
  }

  /**
   * Set the set of annotation values
   *
   * @param annotationValues the new set of annotation values
   * @motivation for hibernate
   */
  private void setAnnotationValues(Map<AnnotationType,AnnotationValue> annotationValues)
  {
    _annotationValues = annotationValues;
  }

  private void setStudies(Set<Screen> studies)
  {
    _studies = studies;
  }

  /**
   * Get the publications.
   * 
   * @return the publications
   */
  @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE }, fetch = FetchType.LAZY)
  @ToMany(hasNonconventionalMutation = true)
  @JoinTable(name = "reagentPublicationLink", joinColumns = @JoinColumn(name = "reagentId"), inverseJoinColumns = @JoinColumn(name = "publicationId"))
  @org.hibernate.annotations.ForeignKey(name = "fk_reagent_publication_link_to_reagent")
  @org.hibernate.annotations.LazyCollection(value = org.hibernate.annotations.LazyCollectionOption.TRUE)
  public Set<Publication> getPublications()
  {
    return _publications;
  }

  private void setPublications(Set<Publication> publications)
  {
    _publications = publications;
  }

  public boolean addPublication(Publication p)
  {
    return _publications.add(p);
  }

  /**
   * Get the attached files.
   * 
   * @return the attached files
   */
  @OneToMany(mappedBy = "reagent",
             cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE },
             fetch = FetchType.LAZY,
             orphanRemoval = true)
  @ToMany(hasNonconventionalMutation = true)
  public Set<AttachedFile> getAttachedFiles()
  {
    return _attachedFiles;
  }

  /**
   * Create and return a new attached file for the screen.
   * 
   * @param filename the filename
   * @param fileType the file type
   * @param fileContents the file contents
   * @throws IOException
   */
  public AttachedFile createAttachedFile(String filename, AttachedFileType fileType, String fileContents) throws IOException
  {
    return createAttachedFile(filename, fileType, new ByteArrayInputStream(fileContents.getBytes()));
  }

  /**
   * Create and return a new attached file for the screen. Use {@link Publication#createAttachedFile} to create an
   * attached file that is
   * associated with a Publication.
   * 
   * @param filename the filename
   * @param fileType the file type
   * @param fileContents the file contents
   * @throws IOException
   */
  public AttachedFile createAttachedFile(String filename, AttachedFileType fileType, InputStream fileContents) throws IOException
  {
    AttachedFile attachedFile = new AttachedFile(this, filename, fileType, fileContents);
    _attachedFiles.add(attachedFile);
    return attachedFile;
  }

  public void removeAttachedFile(AttachedFile attachedFile)
  {
    _attachedFiles.remove(attachedFile);
  }

  /**
   * Set the attached files.
   * 
   * @param attachedFiles the new attached files
   * @motivation for hibernate
   */
  private void setAttachedFiles(Set<AttachedFile> attachedFiles)
  {
    _attachedFiles = attachedFiles;
  }

}
