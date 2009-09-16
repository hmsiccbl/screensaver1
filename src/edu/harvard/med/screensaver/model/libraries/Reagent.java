// $HeadURL:
// svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/model/libraries/Well.java
// $
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.annotations.ContainedEntity;
import edu.harvard.med.screensaver.model.annotations.ToMany;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;
import edu.harvard.med.screensaver.model.screens.Screen;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.MapKeyManyToMany;
import org.hibernate.annotations.Type;


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
@org.hibernate.annotations.Proxy(lazy=false) // proxying causes problems with casts of getLatestReleasedReagent() return value
@Inheritance(strategy=InheritanceType.JOINED)
@ContainedEntity(containingEntityClass=Well.class)
public abstract class Reagent extends AbstractEntity implements Comparable<Reagent>
{
  private static final long serialVersionUID = 1;

  public static final RelationshipPath<Reagent> libraryContentsVersion = new RelationshipPath<Reagent>(Reagent.class, "libraryContentsVersion");
  public static final RelationshipPath<Reagent> well = new RelationshipPath<Reagent>(Reagent.class, "well");
  public static final RelationshipPath<Reagent> annotationValues = new RelationshipPath<Reagent>(Reagent.class, "annotationValues");
  public static final RelationshipPath<Reagent> studies = new RelationshipPath<Reagent>(Reagent.class, "studies");
  public static final PropertyPath<Reagent> vendorName = new PropertyPath<Reagent>(Reagent.class, "vendorId.vendorName");
  public static final PropertyPath<Reagent> vendorIdentifier = new PropertyPath<Reagent>(Reagent.class, "vendorId.vendorIdentifier");

  private Integer _reagentId;
  private LibraryContentsVersion _libraryContentsVersion;
  private Well _well;
  private ReagentVendorIdentifier _vendorId;
  private Map<AnnotationType,AnnotationValue> _annotationValues = new HashMap<AnnotationType,AnnotationValue>();
  private Set<Screen> _studies = new HashSet<Screen>();


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

  @Override
  @Transient
  public Integer getEntityId()
  {
    return getReagentId();
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
    return _reagentId;
  }

  private void setReagentId(Integer reagentId)
  {
    _reagentId = reagentId;
  }

  @Column
  @Type(type="text")
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
  @ToMany(singularPropertyName = "study", hasNonconventionalMutation=true /* model unit tests don't handle immutable to-many relationships, tested in ReagentTest#testAnnotationValueMap */) 
  @JoinColumn(name = "studyId", nullable = false, updatable = false)
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
    if (_studies.remove(study)) {
      study.removeReagent(this);
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
}
