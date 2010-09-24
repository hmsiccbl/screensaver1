// $HeadURL$
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
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
import javax.persistence.Version;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Parameter;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.RequiredPropertyException;
import edu.harvard.med.screensaver.model.annotations.ContainedEntity;
import edu.harvard.med.screensaver.model.annotations.ToOne;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.meta.Cardinality;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.screens.AssayPlateStatus;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.Screen;

/**
 * A physical plate that is derived from a library {@link Plate} and that is used to perform the actual experimentation
 * in its wells by combining the {@link Reagent}s with the biological targets (e.g., cells). If screening multiple
 * replicates, one assay plate will exist for each replicate and will have an assigned {@link #getReplicateOrdinal()
 * replicate ordinal}. If an assay plate screening fails, it may be necessary to create new assay plates to re-screen
 * the library {@link plate}, but these repeated assay plates must belong to new {@link LibraryScreening}. It is valid
 * for an AssayPlate to have a missing {@link #getLibraryScreening() library screening}, while having a
 * {@link #getScreenResultDataLoading() data loading activity}. It is not valid for an AssayPlate to exist without
 * having an association to one of these activities.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@org.hibernate.annotations.Entity
@org.hibernate.annotations.Proxy
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "libraryScreeningId", "plateNumber", "replicateOrdinal" }) })
@ContainedEntity(containingEntityClass=Screen.class)                                 
public class AssayPlate extends AbstractEntity<Integer> implements Comparable<AssayPlate>
{
  private static final long serialVersionUID = 1L;
  private static final Logger log = Logger.getLogger(AssayWell.class);
  
  public static final RelationshipPath<AssayPlate> screen = RelationshipPath.from(AssayPlate.class).to("screen", Cardinality.TO_ONE);
  public static final RelationshipPath<AssayPlate> plateScreened = RelationshipPath.from(AssayPlate.class).to("plateScreened", Cardinality.TO_ONE);
  public static final RelationshipPath<AssayPlate> libraryScreening = RelationshipPath.from(AssayPlate.class).to("libraryScreening", Cardinality.TO_ONE);
  public static final RelationshipPath<AssayPlate> screenResultDataLoading = RelationshipPath.from(AssayPlate.class).to("screenResultDataLoading", Cardinality.TO_ONE);
  
  public static final Function<AssayPlate,Plate> ToPlate = new Function<AssayPlate,Plate>() { public Plate apply(AssayPlate ap) { return ap.getPlateScreened(); } };
  public static final Function<AssayPlate,Integer> ToPlateNumber = new Function<AssayPlate,Integer>() { public Integer apply(AssayPlate ap) { return ap.getPlateNumber(); } };
  public static final Function<AssayPlate,Integer> ToReplicateOrdinal = new Function<AssayPlate,Integer>() { public Integer apply(AssayPlate ap) { return ap.getReplicateOrdinal(); } };
  public static final Function<AssayPlate,LibraryScreening> ToLibraryScreening = new Function<AssayPlate,LibraryScreening>() { public LibraryScreening apply(AssayPlate from) { return from.getLibraryScreening(); } };
  public static final Function<AssayPlate,AdministrativeActivity> ToScreenResultDataLoading = new Function<AssayPlate,AdministrativeActivity>() { public AdministrativeActivity apply(AssayPlate from) { return from.getScreenResultDataLoading(); } };
  public static final Predicate<AssayPlate> IsScreened = new Predicate<AssayPlate>() { public boolean apply(AssayPlate ap) { return ap.getStatus().compareTo(AssayPlateStatus.SCREENED) >= 0; } };
  public static final Predicate<AssayPlate> IsDataLoaded = new Predicate<AssayPlate>() { public boolean apply(AssayPlate ap) { return ap.getStatus().compareTo(AssayPlateStatus.DATA_LOADED) >= 0; } };
  public static final Predicate<AssayPlate> IsFirstReplicate = new Predicate<AssayPlate>() { public boolean apply(AssayPlate ap) { return ap.getReplicateOrdinal() == 0; } };
  /** @motivation {@link #IsScreened} can return true if {@link AssayPlateStatus} > SCREENED even if there is no LibraryScreening */
  public static final Predicate<AssayPlate> HasLibraryScreening = new Predicate<AssayPlate>() { public boolean apply(AssayPlate ap) { return ap.getLibraryScreening() != null; } };
  
  private Integer _version;
  private Screen _screen;
  private Integer _plateNumber;
  private Plate _plateScreened;
  private int _replicateOrdinal;
  private LibraryScreening _libraryScreening;
  private AdministrativeActivity _screenResultDataLoading;
  private double _zScore;

  /** @motivation for hibernate */
  protected AssayPlate() {}

  /** should only be called by {@link Screen#createAssayPlate(Plate, int)} */
  public AssayPlate(Screen screen, Plate plateScreened, int replicateOrdinal)
  {
    this(screen, plateScreened.getPlateNumber(), replicateOrdinal);
    setPlateScreened(plateScreened);
  }


  /**
   * @motivation this alternate constructor is necessary for cases where
   *             Screensaver has not been used to track library copies or
   *             library screenings, so that the necessary Plate and/or
   *             AssayPlate entities do not exist.
   */
  public AssayPlate(Screen screen, int plateNumber, int replicateOrdinal)
  {
    if (screen == null) {
      throw new RequiredPropertyException(this, "screen");
    }
    if (plateNumber < 0) {
      throw new RequiredPropertyException(this, "plateScreened");
    }
    if (replicateOrdinal < 0) {
      throw new DataModelViolationException("replicateOrdinal must be non-negative");
    }
    _screen = screen;
    _plateNumber = plateNumber;
    _replicateOrdinal = replicateOrdinal;
  }

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  @Id
  @org.hibernate.annotations.GenericGenerator(name="assay_plate_id_seq",
                                              strategy="sequence",
                                              parameters = { @Parameter(name="sequence", value="assay_plate_id_seq") })
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="assay_plate_id_seq")
  public Integer getAssayPlateId()
  {
    return getEntityId();
  }
  
    private void setAssayPlateId(Integer assayPlateId)
  {
    setEntityId(assayPlateId);
  }  

  @ManyToOne(fetch=FetchType.LAZY, cascade={})
  @JoinColumn(name="plateId", nullable=true, updatable=false)
  @Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_assay_plate_to_plate")
  @ToOne(hasNonconventionalSetterMethod = true)
  public Plate getPlateScreened()
  {
    return _plateScreened;
  }

  private void setPlateScreened(Plate plateScreened)
  {
    _plateScreened = plateScreened;
  }

  @Column(nullable=false, updatable=false)
  @Immutable
  public int getPlateNumber()
  {
    return _plateNumber;
  }

  private void setPlateNumber(int plateNumber)
  {
    _plateNumber = plateNumber;
  }

  @ManyToOne(fetch=FetchType.LAZY, cascade={})
  @JoinColumn(name="libraryScreeningId")
  @org.hibernate.annotations.ForeignKey(name="fk_assay_plate_to_library_screening")
  public LibraryScreening getLibraryScreening()
  {
    return _libraryScreening;
  }

  public void setLibraryScreening(LibraryScreening libraryScreening)
  {
    _libraryScreening = libraryScreening;
  }

  @ManyToOne(fetch=FetchType.LAZY, cascade={})
  @JoinColumn(name="screenResultDataLoadingId")
  @org.hibernate.annotations.ForeignKey(name="fk_assay_plate_to_screen_result_data_loading")
  public AdministrativeActivity getScreenResultDataLoading()
  {
    return _screenResultDataLoading;
  }

  public void setScreenResultDataLoading(AdministrativeActivity screenResultDataLoading)
  {
    _screenResultDataLoading = screenResultDataLoading;
  }

  @Column(nullable=false, updatable=false)
  @Immutable
  public int getReplicateOrdinal()
  {
    return _replicateOrdinal;
  }

  private void setReplicateOrdinal(int replicateOrdinal)
  {
    _replicateOrdinal = replicateOrdinal;
  }

  /**
   * @motivation for hibernate
   */
  @Column(nullable=false)
  @Version
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }

  public int compareTo(AssayPlate other)
  {
    int result = Integer.valueOf(getPlateNumber()).compareTo(Integer.valueOf(other.getPlateNumber()));
    if (result == 0) {
      result = Integer.valueOf(getReplicateOrdinal()).compareTo(Integer.valueOf(other.getReplicateOrdinal()));
    }
    if (result == 0) {
      if (getLibraryScreening() == null && other.getLibraryScreening() == null) {
        return 0;
      }
      if (getLibraryScreening() != null && other.getLibraryScreening() != null) {
        return getLibraryScreening().compareTo(other.getLibraryScreening());
      }
      if (getLibraryScreening() == null) {
        if (other.getLibraryScreening() == null) {
          return 0;
        }
        return 1;
      }
      return -1;
    }
    return result;
  }

  @ManyToOne(fetch=FetchType.LAZY, cascade={})
  @JoinColumn(name="screenId", nullable=false, updatable=false)
  @org.hibernate.annotations.Immutable
  @org.hibernate.annotations.ForeignKey(name="fk_assay_plate_to_screen")
  public Screen getScreen()
  {
    return _screen;
  }

  private void setScreen(Screen screen)
  {
    _screen = screen;
  }

  @Transient
  public AssayPlateStatus getStatus()
  {
    if (getScreenResultDataLoading() != null) {
      // for now, if data is loaded, we also assume it has been analyzed
      return AssayPlateStatus.DATA_ANALYZED;
    }
    if (getLibraryScreening() != null) {
      return AssayPlateStatus.SCREENED;
    }
    // for now, if AssayPlate exists in the system, we assume it exists in reality and has been plated
    return AssayPlateStatus.PLATED;
  }

}
