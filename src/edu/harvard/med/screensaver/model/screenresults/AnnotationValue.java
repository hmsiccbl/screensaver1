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

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;

/**
 * Annotation value on a particular library member (e.g. a compound or silencing
 * reagent). A single annotation value can be associated with multiple wells,
 * since it is possible for different wells to contains the same library member
 * (since Screensaver does not (currently) represent library members as
 * first-class entities, we use this many-to-many relationship instead). In
 * general, all wells that share the same annotation will also share the same
 * vendor identifier (though this is not enforced by the model).
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
@Entity
@org.hibernate.annotations.Proxy
@edu.harvard.med.screensaver.model.annotations.ContainedEntity(containingEntityClass=AnnotationType.class)
public class AnnotationValue extends AbstractEntity
{

  // private static data

  private static final long serialVersionUID = 1L;
  private static Logger log = Logger.getLogger(AnnotationType.class);


  // private instance data

  private Integer _annotationValueId;
  private Integer _version;
  private AnnotationType _annotationType;
  private ReagentVendorIdentifier _reagentVendorIdentifier;
  private String _value;
  private Double _numericValue;


  // private instance methods

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  @Override
  @Transient
  public Serializable getEntityId()
  {
    return getAnnotationValueId();
  }

  /**
   * Get the id for the annotation value.
   * @return the id for the annotation value
   */
  @Id
  @org.hibernate.annotations.GenericGenerator(
    name="annotation_value_id_seq",
    strategy="sequence",
    parameters = {
      @org.hibernate.annotations.Parameter(name="sequence", value="annotation_value_id_seq")
    }
  )
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="annotation_value_id_seq")
  public Integer getAnnotationValueId()
  {
    return _annotationValueId;
  }

  /**
   * Get the annotation type.
   * @return the annotation type
  */
  @ManyToOne
  @JoinColumn(name="annotationTypeId", nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_annotation_value_to_annotation_type")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.NO_PROXY)
  public AnnotationType getAnnotationType()
  {
    return _annotationType;
  }

  /**
   * Get the reagent vendor identifier.
   * @return the reagent vendor identifier
   */
  @Column(nullable=false)
  @Embedded
  public ReagentVendorIdentifier getReagentVendorIdentifier()
  {
    return _reagentVendorIdentifier;
  }

  /**
   * Set the reagent vendor identifier.
   * @param reagentVendorIdentifier the new reagent vendor identifier
   */
  public void setReagentVendorIdentifier(ReagentVendorIdentifier reagentVendorIdentifier)
  {
    _reagentVendorIdentifier = reagentVendorIdentifier;
  }

  /**
   * Get the value.
   * @return the value
   */
  @Column(updatable=false)
  @org.hibernate.annotations.Type(type="text")
  @org.hibernate.annotations.Immutable
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
  @org.hibernate.annotations.Immutable
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
    return _value;
  }


  // protected constructor

  /**
   * Construct an initialized <code>AnnotationValue</code>. Intended only for use by
   * {@link AnnotationType#createAnnotationValue(ReagentVendorIdentifier, String, boolean)}.
   * @param annotationType the annotation type
   * @param reagentVendorIdentifier the reagent vendor identifier
   * @param value the value
   * @param numericValue the numerical value
   */
  AnnotationValue(
    AnnotationType annotationType,
    ReagentVendorIdentifier reagentVendorIdentifier,
    String value,
    Double numericValue)
  {
    if (annotationType.isNumeric() && value != null && numericValue == null) {
      throw new IllegalArgumentException("'numericValue' must be specified (in addition to 'value') for numeric annotation types");
    }
    _annotationType = annotationType;
    _reagentVendorIdentifier = reagentVendorIdentifier;
    _value = value;
    _numericValue = numericValue;
  }


  // protected constructor

  /**
   * Construct an uninitialized <code>AnnotationValue</code>.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected AnnotationValue() {}


  // private constructor and instance methods

  /**
   * Set the id for the annotation value.
   * @param annotationValueId the new id for the annotation value
   * @motivation for hibernate
   */
  private void setAnnotationValueId(Integer annotationValueId)
  {
    _annotationValueId = annotationValueId;
  }

  /**
   * Get the version for the annotation value.
   * @return the version for the annotation value
   * @motivation for hibernate
   */
  @Column(nullable=false)
  @Version
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * Set the version for the annotation value.
   * @param version the new version for the annotation value
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
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