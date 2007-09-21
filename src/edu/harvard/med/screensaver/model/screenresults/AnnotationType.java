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
import java.util.HashSet;
import java.util.Set;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.ImmutableProperty;
import edu.harvard.med.screensaver.model.ToManyRelationship;
import edu.harvard.med.screensaver.model.ToOneRelationship;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.ui.screenresults.MetaDataType;

import org.apache.log4j.Logger;

/**
 * Annotation type on a library member (e.g. a compound or silencing reagent).
 *
 * @hibernate.class
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class AnnotationType extends AbstractEntity implements MetaDataType, Comparable<AnnotationType>
{
  // static members

  private static final long serialVersionUID = 1L;
  private static Logger log = Logger.getLogger(AnnotationType.class);


  // instance data members

  private Integer _annotationTypeId;
  private Integer _version;
  private Screen _study;
  private String _name;
  private String _description;
  private Integer _ordinal;
  private boolean _isNumeric;
  private Set<AnnotationValue> _values = new HashSet<AnnotationValue>();


  // constructors

  protected AnnotationType()
  {
  }

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
    _study.getAnnotationTypes().add(this);
  }


  // Comparable interface methods

  /**
   * Defines natural ordering of <code>AnnotationType</code> objects, based
   * upon their ordinal field value. Note that natural ordering is only defined
   * between <code>AnnotationType</code> objects that share the same parent
   * {@link Screen}.
   */
  public int compareTo(AnnotationType that) {
    return getOrdinal().compareTo(that.getOrdinal());
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
    return getAnnotationTypeId();
  }

  /**
   * Get the id for the annotation.
   *
   * @return the id for the annotation type
   * @hibernate.id generator-class="sequence"
   * @hibernate.generator-param name="sequence" value="annotation_type_id_seq"
   */
  public Integer getAnnotationTypeId()
  {
    return _annotationTypeId;
  }

  /**
   * Get the study.
   *
   * @return the screen
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.model.screens.Screen"
   *   column="study_id"
   *   not-null="true"
   *   foreign-key="fk_annotation_type_to_screen"
   *   cascade="save-update"
   */
  @ToOneRelationship(nullable=false, inverseProperty="annotationTypes")
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
   * Get the ordinal position of this <code>AnnotationType</code> within its
   * parent {@link Screen}.
   *
   * @return an <code>Integer</code>
   * @hibernate.property type="integer" column="ordinal"
   */
  public Integer getOrdinal()
  {
    return _ordinal;
  }

  /**
   * Set the ordinal position of this <code>AnnotationType</code> within its
   * parent {@link Screen}.
   *
   * @param ordinal the ordinal position of this <code>AnnotationType</code>
   *          within its parent {@link ScreenResult}
   * @motivation for Hibernate
   */
  @ImmutableProperty
  private void setOrdinal(Integer ordinal)
  {
    _ordinal = ordinal;
  }

  /**
   * @hibernate.property not-null="true"
   * @return
   */
  public boolean isNumeric()
  {
    return _isNumeric;
  }

  public AnnotationValue addAnnotationValue(ReagentVendorIdentifier reagentVendorIdentifier,
                                            String textValue,
                                            boolean isNumeric)
  {
    AnnotationValue annotationValue = new AnnotationValue(this,
                                                          reagentVendorIdentifier,
                                                          textValue,
                                                          isNumeric && textValue != null ?  new Double(textValue) : null);
    boolean result = _values.add(annotationValue);
    if (result) {
      return annotationValue;
    }
    return null;
  }

  /**
   * Get the set of annotation values for this annotation type
   *
   * @return the set of annotation values for this annotation type
   * @hibernate.set lazy="true"
   *                cascade="all-delete-orphan"
   *                inverse="true"
   * @hibernate.collection-key column="annotation_type_id"
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
   * Set the id for the annotation type.
   *
   * @param annotationTypeId the new id for the annotation type
   * @motivation for hibernate
   */
  private void setAnnotationTypeId(Integer annotationTypeId) {
    _annotationTypeId = annotationTypeId;
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