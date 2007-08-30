// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screenresults;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.ToManyRelationship;
import edu.harvard.med.screensaver.model.ToOneRelationship;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screens.Screen;

import org.apache.log4j.Logger;

/**
 * Annotation on a library member (e.g. a compound or silencing reagent). A
 * single annotation can be associated with multiple wells, since it is possible
 * for different wells to contains the same library member (since Screensaver
 * does not (currently) represent library members as first-class entities, we
 * use this many-to-many relationship instead). In general, all wells that share
 * the same annotation will also share the same vendor identifier (though this
 * is not enforced by the model).
 *
 * @hibernate.class
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class Annotation extends AbstractEntity
{
  // static members

  private static final long serialVersionUID = 1L;
  private static Logger log = Logger.getLogger(Annotation.class);


  // instance data members

  private Integer _annotationId;
  private Integer _version;
  private Screen _study;
  private String _name;
  private String _description;
  private boolean _isNumeric;
  private Set<AnnotationValue> _values = new HashSet<AnnotationValue>();

  // constructors

  private Annotation()
  {
  }

  public Annotation(Screen study,
                    String name,
                    String description,
                    boolean isNumeric)
  {
    _study = study;
    _name = name;
    _description = description;
    _isNumeric = isNumeric;
    _study.getAnnotations().add(this);
  }

  // public methods


  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  @Override
  public Serializable getEntityId()
  {
    return getAnnotationId();
  }

  /**
   * Get the id for the annotation.
   *
   * @return the id for the annotation
   * @hibernate.id generator-class="sequence"
   * @hibernate.generator-param name="sequence" value="annotation_id_seq"
   */
  public Integer getAnnotationId()
  {
    return _annotationId;
  }

  /**
   * Get the study.
   *
   * @return the screen
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.model.screens.Screen"
   *   column="study_id"
   *   not-null="true"
   *   foreign-key="fk_annotation_to_screen"
   *   cascade="save-update"
   */
  @ToOneRelationship(nullable=false, inverseProperty="annotations")
  public Screen getStudy()
  {
    return _study;
  }

  /**
   * @hibernate.property
   */
  public String getName()
  {
    return _name;
  }

  public void setName(String name)
  {
    _name = name;
  }

  /**
   * @hibernate.property type="text"
   */
  public String getDescription()
  {
    return _description;
  }

  public void setDescription(String description)
  {
    _description = description;
  }

  /**
   * @hibernate.property not-null="true"
   * @return
   */
  public boolean isNumeric()
  {
    return _isNumeric;
  }

  public boolean addAnnotationValue(AnnotationValue value)
  {
    return _values.add(value);
  }

  public boolean addAnnotationValue(String value, Well well)
  {
    return addAnnotationValue(value, new HashSet<Well>(Arrays.asList(well)));
  }

  public boolean addAnnotationValue(BigDecimal value, Well well)
  {
    return addAnnotationValue(value, new HashSet<Well>(Arrays.asList(well)));
  }

  public boolean addAnnotationValue(String value, Set<Well> wells)
  {
    AnnotationValue annotationValue = new AnnotationValue(this, wells, value, null);
    return addAnnotationValue(annotationValue);
  }

  public boolean addAnnotationValue(BigDecimal value, Set<Well> wells)
  {
    AnnotationValue annotationValue = new AnnotationValue(this, wells, null, value);
    return _values.add(annotationValue);
  }

  /**
   * Get the set of annotation values for this Annotation.
   *
   * @return the set of annotation values for this Annotation
   * @hibernate.set lazy="true"
   *                cascade="all-delete-orphan"
   *                inverse="true"
   * @hibernate.collection-key column="annotation_id"
   * @hibernate.collection-one-to-many class="edu.harvard.med.screensaver.model.screenresults.AnnotationValue"
   */
  @ToManyRelationship
  public Set<AnnotationValue> getAnnotationValues()
  {
    return _values;
  }

  @Override
  public int hashCode()
  {
    return getBusinessKey().hashCode();
  }

  public boolean equals(Object o)
  {
    return o == this;
  }

  public String toString()
  {
    return _study.getScreenNumber() + ":" + _name;
  }


  // private methods

  private void setAnnotationValues(Set<AnnotationValue> values)
  {
    _values = values;
  }

  /**
   * Set the id for the annotation.
   *
   * @param annotationId the new id for the annotation
   * @motivation for hibernate
   */
  private void setAnnotationId(Integer annotationId) {
    _annotationId = annotationId;
  }

  /**
   * Get the version for the annotation.
   *
   * @return the version for the annotation
   * @motivation for hibernate
   * @hibernate.version
   */
  private Integer getVersion() {
    return _version;
  }

  /**
   * @motivation for hibernate
   */
  private void setVersion(Integer version) {
    _version = version;
  }

  /**
   * @motivation for hibernate
   */
  private void setStudy(Screen study)
  {
    _study = study;
  }

  private void setNumeric(boolean isNumeric)
  {
    _isNumeric = isNumeric;
  }
  @Override
  protected Object getBusinessKey()
  {
    return _study + ":" + _name;
  }
}