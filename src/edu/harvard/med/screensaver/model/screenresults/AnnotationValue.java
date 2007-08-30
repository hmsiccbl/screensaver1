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
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.ToManyRelationship;
import edu.harvard.med.screensaver.model.ToOneRelationship;
import edu.harvard.med.screensaver.model.libraries.Well;

import org.apache.log4j.Logger;

/**
 * Annotation value on a particular library member (e.g. a compound or silencing reagent). A
 * single annotation value can be associated with multiple wells, since it is possible
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
public class AnnotationValue extends AbstractEntity
{
  // static members

  private static final long serialVersionUID = 1L;
  private static Logger log = Logger.getLogger(Annotation.class);


  // instance data members

  private Integer _annotationValueId;
  private Integer _version;
  private Annotation _annotation;
  private SortedSet<Well> _wells = new TreeSet<Well>();
  private BigDecimal _numericValue;
  private String _value;


  // constructors

  private AnnotationValue()
  {
  }

  AnnotationValue(Annotation annotation,
                  Set<Well> wells,
                  String value,
                  BigDecimal numericValue)
  {
    _annotation = annotation;
    _wells = new TreeSet<Well>(wells);
    _value = value;
    _numericValue = numericValue;
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
    return getAnnotationValueId();
  }

  /**
   * Get the id for the annotation value.
   *
   * @return the id for the annotation value
   * @hibernate.id generator-class="sequence"
   * @hibernate.generator-param name="sequence" value="annotation_value_id_seq"
   */
  public Integer getAnnotationValueId()
  {
    return _annotationValueId;
  }

  /**
  * @hibernate.many-to-one
  *   class="edu.harvard.med.screensaver.model.screenresults.Annotation"
  *   column="annotation_id"
  *   not-null="true"
  *   foreign-key="fk_annotation_value_to_annotation"
  *   cascade="none"
  */
  @ToOneRelationship
  public Annotation getAnnotation()
  {
    return _annotation;
  }

  /**
   * @hibernate.property type="text"
   */
  public String getValue()
  {
    return _value;
  }

  /**
   * @hibernate.property
   */
  public BigDecimal getNumericValue()
  {
    return _numericValue;
  }

  /**
   * Add a well that is annotated by this annotation value
   *
   * @param well the well to add
   * @return true iff the well was added successfully
   */
  public boolean addWell(Well well)
  {
    return _wells.add(well);
  }

  /**
   * Get the set of wells associated with this Annotation.
   *
   * @return the set of wells associated with this Annotation
   * @hibernate.set table="annotation_value_well_link" lazy="true"
   *                cascade="save-update" sort="natural"
   * @hibernate.collection-key column="annotation_value_id"
   * @hibernate.collection-many-to-many class="edu.harvard.med.screensaver.model.libraries.Well"
   *                                    column="well_id"
   */
  @ToManyRelationship(unidirectional=true)
  public SortedSet<Well> getWells()
  {
    return _wells;
  }


  // private methods

  /**
   * Set the id for the annotation value.
   *
   * @param annotationValueId the new id for the annotation value
   * @motivation for hibernate
   */
  private void setAnnotationValueId(Integer annotationValueId) {
    _annotationValueId = annotationValueId;
  }

  /**
   * Get the version for the annotation value.
   *
   * @return the version for the annotation value
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
  private void setAnnotation(Annotation annotation)
  {
    _annotation = annotation;
  }

  /**
   * @motivation for Hibernate
   */
  private void setWells(SortedSet<Well> wells)
  {
    _wells = wells;
  }

  private void setValue(String value)
  {
    _value = value;
  }

  private void setNumericValue(BigDecimal value)
  {
    _numericValue = value;
  }

  @Override
  protected Object getBusinessKey()
  {
    return _annotation + ":" + _wells;
  }
}