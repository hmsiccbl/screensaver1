// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screenresults;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Index;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.RequiredPropertyException;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.meta.Cardinality;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;

/**
 * Annotation value on a library reagent.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
@Entity
@Immutable
@org.hibernate.annotations.Proxy
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "annotationTypeId", "reagentId" }) })
@org.hibernate.annotations.Table(appliesTo = "annotation_value", indexes = {
  @Index(name = "annot_value_annot_type_and_value_index", columnNames = { "annotationTypeId", "value" }),
  @Index(name = "annot_value_annot_type_and_numeric_value_index", columnNames = { "annotationTypeId", "numericValue" }) })
@edu.harvard.med.screensaver.model.annotations.ContainedEntity(containingEntityClass=AnnotationType.class)
public class AnnotationValue extends AbstractEntity<Integer>
{
  private static final long serialVersionUID = 1L;
  private static Logger log = Logger.getLogger(AnnotationValue.class);

  public static final RelationshipPath<AnnotationValue> annotationType = RelationshipPath.from(AnnotationValue.class).to("annotationType", Cardinality.TO_ONE);
  public static final RelationshipPath<AnnotationValue> reagent = RelationshipPath.from(AnnotationValue.class).to("reagent", Cardinality.TO_ONE);
  public static final RelationshipPath<AnnotationValue> study =
    RelationshipPath.from(AnnotationValue.class).to("annotationType", Cardinality.TO_ONE)
                                                .to("study", Cardinality.TO_ONE);

  private AnnotationType _annotationType;
  private Reagent _reagent;
  private String _value;
  private Double _numericValue;


  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  /**
   * Get the id for the annotation value.
   * @return the id for the annotation value
   */
  @Id
  @org.hibernate.annotations.GenericGenerator(name = "annotation_value_id_seq", strategy = "seqhilo",
    parameters = {
    @org.hibernate.annotations.Parameter(name = "sequence", value = "annotation_value_id_seq"),
    @org.hibernate.annotations.Parameter(name = "max_lo", value = "50") })
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="annotation_value_id_seq")
  public Integer getAnnotationValueId()
  {
    return getEntityId();
  }

  /**
   * Get the annotation type.
   * @return the annotation type
  */
  @ManyToOne(fetch=FetchType.LAZY)
  @JoinColumn(name="annotationTypeId", nullable=false, updatable=false)
  @org.hibernate.annotations.ForeignKey(name="fk_annotation_value_to_annotation_type")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  public AnnotationType getAnnotationType()
  {
    return _annotationType;
  }

  /**
   * Get the reagent being annotated.
   * 
   * @return the annotated reagent
   */
  @ManyToOne(fetch=FetchType.LAZY)
  @JoinColumn(name = "reagentId", nullable = false, updatable = false)
  @org.hibernate.annotations.ForeignKey(name="fk_annotation_value_to_reagent")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  public Reagent getReagent()
  {
    return _reagent;
  }

  /**
   * Get the annotation value.
   * 
   * @return the annotation value
   */
  @Column(updatable=false)
  @org.hibernate.annotations.Type(type="text")
  public String getValue()
  {
    return _value;
  }

  /**
   * Get the numerical value.
   * @return the numerical value
   * @motivation for sorting via SQL
   */
  @Column(updatable=false)
  public Double getNumericValue()
  {
    return _numericValue;
  }

  /**
   * Get the formatted value.
   * @return the formatted value
   */
  @Transient
  public String getFormattedValue()
  {
    return _value == null ? (_numericValue == null ? "null" : _numericValue.toString()) : _value;
  }


  /**
   * Construct an initialized <code>AnnotationValue</code>. 
   * TODO: change this comment now that this is public:  Intended only for use by
   * {@link AnnotationType}.
   */
  public AnnotationValue(
    AnnotationType annotationType,
    Reagent reagent,
    String value,
    Double numericValue )
  {
    if (reagent == null) {
      throw new RequiredPropertyException(this, "reagent"); 
    }
    if (annotationType.isNumeric() && value != null && numericValue == null) {
      throw new IllegalArgumentException("'numericValue' must be specified (in addition to 'value') for numeric annotation types");
    }
    _annotationType = annotationType;
    _reagent = reagent;
    _value = value;
    _numericValue = numericValue;

  }

  /**
   * Construct an uninitialized <code>AnnotationValue</code>.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected AnnotationValue() {}

  /**
   * Set the id for the annotation value.
   * @param annotationValueId the new id for the annotation value
   * @motivation for hibernate
   */
  private void setAnnotationValueId(Integer annotationValueId)
  {
    setEntityId(annotationValueId);
  }

  /**
   * Set the reagent.
   * @param Reagent the new reagent
   * @motivation for hibernate
   */
  private void setReagent(Reagent reagent)
  {
    _reagent = reagent;
  }

  /**
   * Set the annotation type.
   * @param annotationType the new annotation type
   * @motivation for hibernate
   */
  private void setAnnotationType(AnnotationType annotationType)
  {
    _annotationType = annotationType;
  }

  /**
   * Set the value.
   * @param value the new value
   * @motivation for hibernate
   */
  private void setValue(String value)
  {
    _value = value;
  }

  /**
   * Set the numerical Value
   * @param numericValue the new numerical value
   */
  private void setNumericValue(Double numericValue)
  {
    _numericValue = numericValue;
  }
}