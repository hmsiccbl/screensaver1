// $HeadURL:
// svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/model/libraries/Well.java
// $
// $Id: Well.java 1985 2007-10-19 18:27:49Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.SemanticIDAbstractEntity;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.util.CollectionUtils;

import org.apache.commons.collections.Transformer;
import org.apache.log4j.Logger;


/**
 * A Hibernate entity bean representing a reagent.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
@Entity
@Table(uniqueConstraints={  })
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
  private Set<AnnotationValue> _annotationValues = new HashSet<AnnotationValue>();
  private Set<Screen> _studies = new HashSet<Screen>();
  private transient Map<AnnotationType,AnnotationValue> _annotationTypeIdToAnnotationValue;
// TODO: implement
  //private Set<Reagent> _reagents = new HashSet<Reagent>();


  // public instance methods

  /**
   * Construct an initialized <code>Reagent</code> object.
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
   * @return the well id for the well
   */
  @Id
  public ReagentVendorIdentifier getReagentId()
  {
    return _reagentId;
  }

  /**
   * Get the set of wells.
   * @return the set of wells
   */
  @OneToMany(
    mappedBy="reagent",
    cascade={ CascadeType.PERSIST, CascadeType.MERGE },
    fetch=FetchType.LAZY
  )
  @OrderBy("wellId")
  @org.hibernate.annotations.Cascade(value=org.hibernate.annotations.CascadeType.SAVE_UPDATE)
  public Set<Well> getWells()
  {
    return _wells;
  }

  /**
   * Add the well.
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
   * @return the set of annotation values
   */
  @OneToMany(cascade={ CascadeType.PERSIST, CascadeType.MERGE },
             mappedBy="reagent",
             fetch=FetchType.LAZY)
  @org.hibernate.annotations.Cascade(value=org.hibernate.annotations.CascadeType.SAVE_UPDATE)
  public Set<AnnotationValue> getAnnotationValues()
  {
    return _annotationValues;
  }

  @Transient
  public AnnotationValue getAnnotationValue(AnnotationType annotationType)
  {
    if (_annotationTypeIdToAnnotationValue == null) {
      _annotationTypeIdToAnnotationValue =
        CollectionUtils.indexCollection(_annotationValues,
                                        new Transformer() {
          public Object transform(Object annotationValue) {
            Serializable entityId = ((AnnotationValue) annotationValue).getAnnotationType().getEntityId();
            if (entityId == null) {
              throw new IllegalStateException("cannot call getAnnotationValue() unless annotationTypes are persisted");
            }
            return entityId;
          }
        },
        AnnotationType.class,
        AnnotationValue.class);
    }
    return _annotationTypeIdToAnnotationValue.get(annotationType.getAnnotationTypeId());
  }

  @ManyToMany(cascade={ CascadeType.PERSIST, CascadeType.MERGE },
              targetEntity=Screen.class,
              mappedBy="reagents",
              fetch=FetchType.LAZY)
  @org.hibernate.annotations.Cascade(value=org.hibernate.annotations.CascadeType.SAVE_UPDATE)
  @JoinColumn(name="studyId", nullable=false, updatable=false)
  @org.hibernate.annotations.ForeignKey(name="fk_reagent_to_study")
  @org.hibernate.annotations.LazyCollection(value=org.hibernate.annotations.LazyCollectionOption.TRUE)
  @edu.harvard.med.screensaver.model.annotations.ManyToMany(singularPropertyName="study")
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
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected Reagent() {}


  // private methods

  /**
   * Set the reagent id for the reagent.
   * @param reagentId the new reagent id for the reagent
   * @motivation for hibernate
   */
  private void setReagentId(ReagentVendorIdentifier reagentId)
  {
    _reagentId = reagentId;
  }

  /**
   * Get the version of the reagent.
   * @return the version of the reagent
   * @motivation for hibernate
   */
  @Version
  @Column(nullable=false)
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * Set the version of the reagent.
   * @param version the new version of the reagent
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }

  /**
   * Set the set of wells
   * @param wells the new set of wells
   * @motivation for hibernate
   */
  private void setWells(Set<Well> wells)
  {
    _wells = wells;
  }

  /**
   * Set the set of annotation values
   * @param annotationValues the new set of annotation values
   * @motivation for hibernate
   */
  private void setAnnotationValues(Set<AnnotationValue> annotationValues)
  {
    _annotationValues = annotationValues;
  }

  private void setStudies(Set<Screen> studies)
  {
    _studies = studies;
  }
}
