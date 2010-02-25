// $HeadURL:
// svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/src/edu/harvard/med/screensaver/model/screenresults/ResultValue.java
// $
// $Id: ResultValue.java 2679 2008-08-05 13:34:13Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screenresults;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.persistence.Version;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.annotations.ContainedEntity;
import edu.harvard.med.screensaver.model.annotations.ToOne;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Index;


/**
 * AssayWell maintains information common to each ResultValue of a given library Well.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@org.hibernate.annotations.Entity
@org.hibernate.annotations.Proxy
// TODO @edu.harvard.med.screensaver.model.annotations.ContainedEntity(containingEntityClass=ScreenResult.class)
// TODO: create index with "where is_positive", for performance
@org.hibernate.annotations.Table(appliesTo = "assay_well", 
                                 indexes={ @Index(name = "assay_well_unique_index", columnNames={ "well_id", "screenResultId" })})
@ContainedEntity(containingEntityClass=ScreenResult.class)                                 
public class AssayWell extends AbstractEntity<Integer> implements Comparable<AssayWell>
{

  private static final long serialVersionUID = 1L;
  private static final Logger log = Logger.getLogger(AssayWell.class);
  
  public static final RelationshipPath<AssayWell> screenResult = new RelationshipPath<AssayWell>(AssayWell.class, "screenResult");
  public static final RelationshipPath<AssayWell> libraryWell = new RelationshipPath<AssayWell>(AssayWell.class, "libraryWell");

  private Integer _version;
  private ScreenResult _screenResult;
  private Well _libraryWell;
  private AssayWellType _assayWellType;
  private boolean _isPositive;
  private Map<ResultValueType,ResultValue> _resultValues = new HashMap<ResultValueType,ResultValue>();


  /*public*/ AssayWell(ScreenResult screenResult, Well libraryWell, AssayWellType assayWellType)
  {
    if (screenResult == null) {
      throw new DataModelViolationException("screenResult is required");
    }
    if (libraryWell == null) {
      throw new DataModelViolationException("screenResult is required");
    }
    _screenResult = screenResult;
    _libraryWell = libraryWell;
//    // TODO: remove, for performance of screen result import
//    _libraryWell.getAssayWells().put(_screenResult, this);
    _assayWellType = assayWellType;
    validateAssayWellType(assayWellType);
  }

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  @Id
  @org.hibernate.annotations.GenericGenerator(
    name="assay_well_id_seq",
    strategy="seqhilo",
    parameters = {
      @org.hibernate.annotations.Parameter(name="sequence", value="assay_well_id_seq"),
      @org.hibernate.annotations.Parameter(name="max_lo", value="384")
    }
  )
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="assay_well_id_seq")
  public Integer getAssayWellId()
  {
    return getEntityId();
  }

  /**
   * Get the version number of the screen result.
   * @return the version number of the screen result
   * @motivation for hibernate
   */
  @Column(nullable=false)
  @Version
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * Set the version number of the screen result.
   * @param version the new version number of the screen result
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }

  public int compareTo(AssayWell other)
  {
    return getLibraryWell().getWellKey().compareTo(((AssayWell) other).getLibraryWell().getWellKey());
  }

  @ManyToOne(fetch=FetchType.LAZY, cascade={})
  @JoinColumn(name="screenResultId", nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_assay_well_to_screen_result")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  public ScreenResult getScreenResult()
  {
    return _screenResult;
  }

  private void setScreenResult(ScreenResult screenResult)
  {
    _screenResult = screenResult;
  }

  /**
   * Get the well.
   * @return the well
   */
  @ManyToOne(fetch=FetchType.LAZY, cascade={} /*Well is owned by Library.wells*/)
  @JoinColumn(name="well_id", nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_assay_well_to_well")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @ToOne(unidirectional=true)
  public Well getLibraryWell()
  {
    return _libraryWell;
  }

  /**
   * @motivation for hibernate
   */
  private void setLibraryWell(Well libraryWell)
  {
    _libraryWell = libraryWell;
  }

  /**
   * Get whether this result value indicates a positive. Returns false if the
   * {@link #getResultValueType() ResultValueType} is not a positive indicator.
   *
   * @return true whenever this result value is a positive indicator
   */
  @Column(nullable=false, name="isPositive")
  @org.hibernate.annotations.Index(name="assay_well_well_positives_only_index")
  public boolean isPositive()
  {
    return _isPositive;
  }

  // TODO
//  private void setResultValues(Map<ResultValueType,ResultValue> resultValues)
//  {
//    _resultValues = resultValues;
//  }
//
//  /**
//   * Get the set of result values.
//   * @return the set of result values
//   */
//  @OneToMany(fetch=FetchType.LAZY, mappedBy="assayWell")
//  @MapKeyManyToMany(joinColumns={ @JoinColumn(name="resultValueTypeId") }, targetEntity=ResultValueType.class)
//  public Map<ResultValueType,ResultValue> getResultValues()
//  {
//    return _resultValues;
//  }

  public void setPositive(boolean isPositive)
  {
    _isPositive = isPositive;
  }

  /**
   * Get the assay well's type.
   *
   * @return the assay well's type
   */
  @Column(nullable=false)
  @org.hibernate.annotations.Type(type="edu.harvard.med.screensaver.model.screenresults.AssayWellType$UserType")
  public AssayWellType getAssayWellType()
  {
    return _assayWellType;
  }

  /**
   * Return true iff the assay well type is
   * {@link AssayWellType#EXPERIMENTAL experimental}.
   *
   * @return true iff the assay well type is experimental
   * @see AssayWellType#EXPERIMENTAL
   */
  @Transient
  public boolean isExperimentalWell()
  {
    return getAssayWellType().equals(AssayWellType.EXPERIMENTAL);
  }

  /**
   * Return true iff the assay well type is a control.
   *
   * @return true iff the assay well type is a control
   * @see AssayWellType#isControl()
   */
  @Transient
  public boolean isControlWell()
  {
    return getAssayWellType().isControl();
  }

  /**
   * Return true iff the assay well type is data producing.
   *
   * @return true iff the assay well type is data producing
   * @see AssayWellType#isDataProducing()
   */
  @Transient
  public boolean isDataProducerWell()
  {
    return getAssayWellType().isDataProducing();
  }

  /**
   * Return true iff the assay well type is {@link AssayWellType#OTHER other}.
   *
   * @return true iff the assay well type is other
   * @see AssayWellType#OTHER
   */
  @Transient
  public boolean isOtherWell()
  {
    return getAssayWellType().equals(AssayWellType.OTHER);
  }

  /**
   * Return true iff the assay well type is {@link AssayWellType#EMPTY empty}.
   *
   * @return true iff the assay well type is empty
   * @see AssayWellType#EMPTY
   */
  @Transient
  public boolean isEmptyWell()
  {
    return getAssayWellType().equals(AssayWellType.EMPTY);
  }

  /**
   * @motivation for hibernate
   */
  protected AssayWell() {}

  /**
   * Set the well id for the well.
   * @param wellId the new well id for the well
   * @motivation for hibernate
   */
  private void setAssayWellId(Integer assayWellId)
  {
    setEntityId(assayWellId);
  }

  /**
   * @motivation for hibernate
   */
  private void setAssayWellType(AssayWellType assayWellType)
  {
    _assayWellType = assayWellType;
  }

  private void validateAssayWellType(AssayWellType assayWellType)
  {
    if (assayWellType == AssayWellType.ASSAY_CONTROL ||
      assayWellType == AssayWellType.ASSAY_POSITIVE_CONTROL ||
      assayWellType == AssayWellType.OTHER) {
      if (_libraryWell.getLibraryWellType() != LibraryWellType.EMPTY) {
        log.warn(/*(throw new DataModelViolationException(*/"result value assay well type can only be 'assay control', 'assay positive control', or 'other' if the library well type is 'empty'");
      }
    }
    else if (!_libraryWell.getLibraryWellType().getValue().equals(assayWellType.getValue())) {
      log.warn(/*throw new DataModelViolationException(*/"result value assay well type does not match library well type of associated well");
    }
  }
}
