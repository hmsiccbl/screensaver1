// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screenresults;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.apache.log4j.Logger;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.OptimisticLock;

import com.google.common.base.Function;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.annotations.ToMany;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.meta.Cardinality;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.ui.screenresults.MetaDataType;

/**
 * Annotation type on a reagent.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
@Entity
@Table(uniqueConstraints={ @UniqueConstraint(columnNames={ "studyId", "name" }) })
@org.hibernate.annotations.Proxy
@edu.harvard.med.screensaver.model.annotations.ContainedEntity(containingEntityClass=Screen.class)
public class AnnotationType extends AbstractEntity<Integer> implements MetaDataType, Comparable<AnnotationType>
{

  // private static data

  private static final long serialVersionUID = 1L;
  private static Logger log = Logger.getLogger(AnnotationType.class);
  
  public static final RelationshipPath<AnnotationType> study = RelationshipPath.from(AnnotationType.class).to("study", Cardinality.TO_ONE);
  public static final RelationshipPath<AnnotationType> annotationValues = RelationshipPath.from(AnnotationType.class).to("annotationValues");

  public static final Function<AnnotationType,String> ToName = new Function<AnnotationType,String>() {
    public String apply(AnnotationType entity)
    {
      return entity.getName();
    }
  };
  // private instance data

  private Integer _version;
  private Screen _study;
  private String _name;
  private String _description;
  private Integer _ordinal;
  private boolean _isNumeric;
  private Map<Reagent,AnnotationValue> _values = new HashMap<Reagent,AnnotationValue>();


  // constructors

  /**
   * Construct an initialized <code>AnnotationType</code>. Intended only for use by {@link
   * Screen}.
   * @param study the study
   * @param name the name of the annotation type
   * @param description the description for the annotation type
   * @param ordinal the ordinal position of this <code>AnnotationType</code> within its
   * parent {@link Screen}.
   * @param isNumeric true iff this annotation type contains numeric result values
   */
  public AnnotationType(Screen study,
                        String name,
                        String description,
                        Integer ordinal,
                        boolean isNumeric)
  {
    _study = study;
    _name = name;
    _description = description;
    _ordinal = ordinal;
    _isNumeric = isNumeric;
    //TODO: may have to lazily make this connection, re: large studies, [#2268] -sde4  
    _study.getAnnotationTypes().add(this);
  }


  // public instance methods

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  /**
   * Defines natural ordering of <code>AnnotationType</code> objects, based
   * upon their ordinal field value. Note that natural ordering is only defined
   * between <code>AnnotationType</code> objects that share the same parent
   * {@link Screen}.
   */
  public int compareTo(AnnotationType that)
  {
    return getOrdinal().compareTo(that.getOrdinal());
  }

  /**
   * Get the id for the annotation.
   * @return the id for the annotation type
   */
  @Id
  @org.hibernate.annotations.GenericGenerator(
    name="annotation_type_id_seq",
    strategy="sequence",
    parameters = {
      @org.hibernate.annotations.Parameter(name="sequence", value="annotation_type_id_seq")
    }
  )
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="annotation_type_id_seq")
  public Integer getAnnotationTypeId()
  {
    return getEntityId();
  }

  /**
   * Get the study.
   * @return the study
   */
  @ManyToOne(fetch=FetchType.LAZY)
  @JoinColumn(name="studyId", nullable=false, updatable=false)
  @org.hibernate.annotations.ForeignKey(name="fk_annotation_type_to_screen")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  public Screen getStudy()
  {
    return _study;
  }

  /**
   * Get the set of annotation values for this annotation type
   * @return the set of annotation values for this annotation type
   */
  @OneToMany(fetch=FetchType.LAZY,
             cascade={ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE },
             mappedBy="annotationType")
  @ToMany(hasNonconventionalMutation=true /* model unit tests don't handle Maps yet, tested in AnnotationTypeTest#testAnnotationValues */)             
  @MapKey(name="reagent")
  @OptimisticLock(excluded=true)
  @org.hibernate.annotations.Cascade(value={org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.DELETE})
  // removing, as this inconveniently forces us to access all reagents before looking for them in the map collection  @org.hibernate.annotations.LazyCollection(LazyCollectionOption.EXTRA)
  public Map<Reagent,AnnotationValue> getAnnotationValues()
  {
    return _values;
  }

  /**
   * Create and return an annotation value for the annotation type.
   * 
   * @param reagent the reagent
   * @param value the value
   * @return the new annotation value
   */
  public AnnotationValue createAnnotationValue(
    Reagent reagent,
    String value)
  {
    if (_values.containsKey(reagent)) {
      AnnotationValue extantAnnotValue = _values.get(reagent);
      if (extantAnnotValue.getValue().equals(value)) {
        log.warn("duplicate annotation for " + reagent + " (ignoring duplicate)");
      }
      else {
        log.error("conflicting annotations exist for " + reagent + " (fix this!)");
      }
      return null;
    }
    
    AnnotationValue annotationValue = new AnnotationValue(
      this,
      reagent,
      value,
      _isNumeric && value != null ? new Double(value) : null);

    //TODO: may have to lazily make this connection, re: large studies, [#2268] -sde4  
    reagent.getAnnotationValues().put(this, annotationValue);
    
    //TODO: may have to lazily make this connection, re: large studies, [#2268] -sde4  
    getStudy().addReagent(reagent);
    _values.put(reagent, annotationValue);
    return annotationValue;
  }

  /**
   * Get the name of the annotation type.
   * @return the name of the annotation type
   */
  @org.hibernate.annotations.Type(type="text")
  public String getName()
  {
    return _name;
  }

  /**
   * Set the name of the annotation type.
   * @param name the new name of the annotation type
   */
  public void setName(String name)
  {
    _name = name;
  }

  /**
   * Get the description
   * @return the description
   */
  @org.hibernate.annotations.Type(type="text")
  public String getDescription()
  {
    return _description;
  }

  /**
   * Set the description
   * @param description the new description
   */
  public void setDescription(String description)
  {
    _description = description;
  }

  /**
   * Get the 0-based ordinal position of this <code>AnnotationType</code> within its
   * parent {@link Screen}.
   * @return the 0-based ordinal position
   */
  @Column(nullable=false, updatable=false)
  public Integer getOrdinal()
  {
    return _ordinal;
  }

  /**
   * Return true iff this annotation type contains numeric result values.
   * @return true iff this annotation type contains numeric result values
   */
  @Column(nullable=false, updatable=false, name="isNumeric")
  public boolean isNumeric()
  {
    return _isNumeric;
  }


  // protected constructor

  /**
   * Construct an uninitialized <code>AnnotationType</code>.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected AnnotationType() {}


  // private instance methods

  /**
   * Set the id for the annotation type.
   * @param annotationTypeId the new id for the annotation type
   * @motivation for hibernate
   */
  private void setAnnotationTypeId(Integer annotationTypeId)
  {
    setEntityId(annotationTypeId);
  }

  /**
   * Get the version for the annotation type.
   * @return the version for the annotation type
   * @motivation for hibernate
   */
  @Column(nullable=false)
  @Version
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * Set the version for the annotation type.
   * @param version the new version for the annotation type
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }

  /**
   * Set the study.
   * @param study the new study
   * @motivation for hibernate
   */
  private void setStudy(Screen study)
  {
    _study = study;
  }

  /**
   * Set the annotation values.
   * @param values the new set of annotation values
   */
  private void setAnnotationValues(Map<Reagent,AnnotationValue> values)
  {
    _values = values;
  }

  /**
   * Set the ordinal position of this <code>AnnotationType</code> within its
   * parent {@link Screen}.
   * @param ordinal the ordinal position of this <code>AnnotationType</code>
   *          within its parent {@link ScreenResult}
   * @motivation for Hibernate
   */
  private void setOrdinal(Integer ordinal)
  {
    _ordinal = ordinal;
  }

  /**
   * Set the numericalness of this annotation type.
   * @param isNumeric the new numericalness of this annotation type
   */
  private void setNumeric(boolean isNumeric)
  {
    _isNumeric = isNumeric;
  }


  public void clearAnnotationValues()
  {
    _values.clear();
    
  }
}