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
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.log4j.Logger;
import org.hibernate.annotations.MapKeyManyToMany;

import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.SemanticIDAbstractEntity;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;
import edu.harvard.med.screensaver.model.screens.Screen;


/**
 * A substance, such as a {@link Compound} or {@link SilencingReagent}, used to
 * test the response of a biological system to a specific perturbation. Reagents
 * are contained in {@link #Library Library} {@link Well wells}.
 * <p>
 * <i>Note: The Reagent entity has been recently added to the data model, and
 * will ultimately be renamed to LibraryReagent and will become the parent of
 * {@link Compound} and {@link SilencingReagent}. This entity currently only
 * maintains a {@link ReagentVendorIdentifier} property, and all other useful
 * properties about a Well's reagent(s) can be found in the aforementioned
 * entity types.</i>
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
@Entity
@Table(uniqueConstraints = {})
@org.hibernate.annotations.Proxy
public class Reagent extends SemanticIDAbstractEntity implements Comparable<Reagent>
{

  // static fields

  private static final Logger log = Logger.getLogger(Well.class);
  private static final long serialVersionUID = 1;

  // instance fields

  private ReagentVendorIdentifier _reagentId;
  private Integer _version;
  private Set<Well> _wells = new HashSet<Well>();
  private Map<AnnotationType,AnnotationValue> _annotationValues = new HashMap<AnnotationType,AnnotationValue>();
  private Set<Screen> _studies = new HashSet<Screen>();


  // public instance methods

  /**
   * Construct an initialized <code>Reagent</code> object.
   * 
   * @param reagentVendorIdentifier
   * @motivation for {@link Library#createWell}
   */
  public Reagent(ReagentVendorIdentifier reagentVendorIdentifier)
  {
    _reagentId = reagentVendorIdentifier;
  }

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  @Override
  @Transient
  public ReagentVendorIdentifier getEntityId()
  {
    return getReagentId();
  }

  public int compareTo(Reagent o)
  {
    return _reagentId.compareTo(o._reagentId);
  }

  /**
   * Get the well id for the well.
   * 
   * @return the well id for the well
   */
  @Id
  public ReagentVendorIdentifier getReagentId()
  {
    return _reagentId;
  }

  /**
   * Get the reagent ID as a string. Note that this Hibernate property mapping
   * is an alias for the reagentId property (they map to the same schema field).
   * 
   * @motivation We cannot directly use the reagentId property (a
   *             ReagentVendorIdentifier type) in a PropertyPath, since if such
   *             as PropertyPath is passed to
   *             EntityDataFetcher.buildFetchKeysQuery() to create a Hibernate
   *             filter query, it needs a String type argument for the query
   *             parameter, not a ReagentVendorIdentifier object (and not sure
   *             if we can make Hibernate perform the necessary implicit cast
   *             when setting this query argument). See
   *             ReagentSearchResults.buildReagentPropertyColumns(), "Reagent
   *             Source ID" column.
   * @return reagent ID as a String
   */
  @Column(name = "reagentId", updatable = false, insertable = false)
  public String getReagentIdString()
  {
    return _reagentId.getReagentId();
  }

  public void setReagentIdString(String reagentIdString)
  {
  // do nothing; set by setReagentId()
  }

  /**
   * Get the set of wells.
   * 
   * @return the set of wells
   */
  @OneToMany(mappedBy = "reagent", fetch = FetchType.LAZY)
  public Set<Well> getWells()
  {
    return _wells;
  }

  /**
   * Add the well.
   * 
   * @param well the well to add
   * @return true iff the reagent did not already have the well
   */
  public boolean addWell(Well well)
  {
    if (!_wells.contains(well)) {
      well.setReagent(this);
      return _wells.contains(well);
    }
    return false;
  }

  /**
   * Remove the well.
   * 
   * @param well the well to remove
   * @return true iff the reagent previously had the well
   */
  public boolean removeWell(Well well)
  {
    if (_wells.contains(well)) {
      well.setReagent(null);
      return !_wells.contains(well);
    }
    return false;
  }

  /**
   * Get the set of annotation values.
   * 
   * @return the set of annotation values
   */
  @OneToMany(fetch = FetchType.LAZY, mappedBy = "reagent")
  /* @LazyCollection(LazyCollectionOption.EXTRA) */
  @MapKeyManyToMany(joinColumns = { @JoinColumn(name = "annotationTypeId") }, targetEntity = AnnotationType.class)
  public Map<AnnotationType,AnnotationValue> getAnnotationValues()
  {
    return _annotationValues;
  }

  @ManyToMany(targetEntity = Screen.class, mappedBy = "reagents", fetch = FetchType.LAZY)
  @JoinColumn(name = "studyId", nullable = false, updatable = false)
  @org.hibernate.annotations.ForeignKey(name = "fk_reagent_to_study")
  @org.hibernate.annotations.LazyCollection(value = org.hibernate.annotations.LazyCollectionOption.TRUE)
  @edu.harvard.med.screensaver.model.annotations.ManyToMany(singularPropertyName = "study")
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


  // protected constructor

  /**
   * Construct an uninitialized <code>Reagent</code> object.
   * 
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected Reagent()
  {}


  // private methods

  /**
   * Set the reagent id for the reagent.
   * 
   * @param reagentId the new reagent id for the reagent
   * @motivation for hibernate
   */
  private void setReagentId(ReagentVendorIdentifier reagentId)
  {
    _reagentId = reagentId;
  }

  /**
   * Get the version of the reagent.
   * 
   * @return the version of the reagent
   * @motivation for hibernate
   */
  @Version
  @Column(nullable = false)
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * Set the version of the reagent.
   * 
   * @param version the new version of the reagent
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }

  /**
   * Set the set of wells
   * 
   * @param wells the new set of wells
   * @motivation for hibernate
   */
  private void setWells(Set<Well> wells)
  {
    _wells = wells;
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
