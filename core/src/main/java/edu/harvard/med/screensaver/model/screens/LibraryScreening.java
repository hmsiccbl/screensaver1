// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Transient;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.joda.time.LocalDate;

import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.annotations.Derived;
import edu.harvard.med.screensaver.model.libraries.LibraryPlate;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.screenresults.AssayPlate;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;


/**
 * Records the activity of screening a {@link #getAssayPlatesScreened() set} of
 * {@link Plate}s. The screening of a plate implies it was removed from freezer
 * storage and reagent volume was withdrawn and transferred to assay plates.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
@Entity
@PrimaryKeyJoinColumn(name="activityId")
@org.hibernate.annotations.ForeignKey(name="fk_library_screening_to_activity")
@org.hibernate.annotations.Proxy
@edu.harvard.med.screensaver.model.annotations.ContainedEntity(containingEntityClass=Screen.class)
public class LibraryScreening extends Screening
{
  private static final long serialVersionUID = 0L;
  private static final Logger log = Logger.getLogger(LibraryScreening.class);
  
  public static final RelationshipPath<LibraryScreening> assayPlatesScreened = RelationshipPath.from(LibraryScreening.class).to("assayPlatesScreened");

  public static final String ACTIVITY_TYPE_NAME = "Library Screening";
  public static final String EXTERNAL_LIBRARY_SCREENING_ACTIVITY_TYPE_NAME = "External " + ACTIVITY_TYPE_NAME;


  private SortedSet<AssayPlate> _assayPlatesScreened = Sets.newTreeSet();
  private String _abaseTestsetId;
  private boolean _isForExternalLibraryPlates;
  private int _librariesScreenedCount;
  private int _libraryPlatesScreenedCount;
  private int _screenedExperimentalWellCount;


  /**
   * Construct an initialized <code>LibraryScreening</code>. Intended only for use by {@link Screen}.
   * @param screen the screen
   * @param performedBy the user that performed the library assay
   * @param dateOfActivity
   */
  LibraryScreening(Screen screen,
                   AdministratorUser recordedBy,
                   ScreeningRoomUser performedBy,
                   LocalDate dateOfActivity)
  {
    super(screen, recordedBy, performedBy, dateOfActivity);
  }

  /**
   * Construct an uninitialized <code>LibraryScreening</code> object.
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected LibraryScreening() {}

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  @Override
  @Transient
  public String getActivityTypeName()
  {
    if (isForExternalLibraryPlates()) {
      return EXTERNAL_LIBRARY_SCREENING_ACTIVITY_TYPE_NAME;
    }
    return ACTIVITY_TYPE_NAME;
  }

  /**
   * Get the plates screened during this library screening. Note that it is not
   * possible for the same plate (plate number and copy) to be screened multiple times within the same
   * visit.
   * 
   * @return the plates screened
   */
  @OneToMany(mappedBy = "libraryScreening", cascade = { CascadeType.MERGE })
  @edu.harvard.med.screensaver.model.annotations.ToMany(singularPropertyName="assayPlateScreened", hasNonconventionalMutation=true /*add/remove methods operator on replicate groups, not individual assay plates*/)
  @Sort(type=SortType.NATURAL)
  public SortedSet<AssayPlate> getAssayPlatesScreened()
  {
    return _assayPlatesScreened;
  }
  
  /**
   * @motivation for hibernate
   */
  private void setAssayPlatesScreened(SortedSet<AssayPlate> assayPlatesScreened)
  {
    _assayPlatesScreened = assayPlatesScreened;
  }

  @Transient
  public int getAssayPlatesScreenedCount()
  {
    return _assayPlatesScreened.size();
  }
  
  public void setLibrariesScreenedCount(int librariesScreenedCount)
  {
    _librariesScreenedCount = librariesScreenedCount;
  }

  @Derived
  public int getLibrariesScreenedCount()
  {
    return _librariesScreenedCount;
  }

  @Derived
  public int getLibraryPlatesScreenedCount()
  {
    return _libraryPlatesScreenedCount;
  }

  public void setLibraryPlatesScreenedCount(int libraryPlatesScreenedCount)
  {
    _libraryPlatesScreenedCount = libraryPlatesScreenedCount;
  }

  @Derived
  public int getScreenedExperimentalWellCount()
  {
    return _screenedExperimentalWellCount;
  }
  
  public void setScreenedExperimentalWellCount(int n)
  {
    _screenedExperimentalWellCount = n;
  }

  /**
   * Get the abase testset id
   * @return the abase testset id
   */
  @org.hibernate.annotations.Type(type="text")
  public String getAbaseTestsetId()
  {
    return _abaseTestsetId;
  }

  /**
   * Set the abase testset id
   * @param abaseTestsetId the new abase testset id
   */
  public void setAbaseTestsetId(String abaseTestsetId)
  {
    _abaseTestsetId = abaseTestsetId;
  }

  /**
   * Get whether the plates being screened are not based upon libraries
   * maintained at the facility, in which case there will be no library plates
   * associated with this activity.
   */
  @Column(name="isForExternalLibraryPlates", nullable=false)
  public boolean isForExternalLibraryPlates()
  {
    return _isForExternalLibraryPlates;
  }

  public void setForExternalLibraryPlates(boolean isForExternalLibraryPlates)
  {
    _isForExternalLibraryPlates = isForExternalLibraryPlates;
  }

  public boolean addAssayPlatesScreened(Plate plate)
  {
    if (Sets.newHashSet(Iterables.transform(getAssayPlatesScreened(), AssayPlate.ToPlateNumber)).contains(plate.getPlateNumber())) {  
      throw new DuplicateEntityException(this, "plateNumber", plate.getPlateNumber());
    }
    if (getNumberOfReplicates() == null || getNumberOfReplicates() < 1) {
      throw new DataModelViolationException("replicates must specified as a positive number");
    }
      
    for (int r = 0; r < getNumberOfReplicates(); ++r) {
      AssayPlate assayPlate = getScreen().createAssayPlate(plate, r, this);
      _assayPlatesScreened.add(assayPlate);
    }

    return true;
  }
  
  public boolean removeAssayPlatesScreened(Plate plate)
  {
    boolean changed = false;
    Iterator<AssayPlate> iter = _assayPlatesScreened.iterator();
    while (iter.hasNext()) {
      AssayPlate ap = iter.next();
      if (ap.getPlateScreened().equals(plate)) {
        if (ap.getStatus().compareTo(AssayPlateStatus.SCREENED) > 0) {
          throw new DataModelViolationException("assay plate has data and cannot be removed");
        }
        // TODO: currently, the act of removing an assay plate from a library screening is the same as deleting it altogether
        // (if Screensaver later tracks the creation and screening of assay plates independently, then we need only mark
        // the assay plate as unscreened here, rather than deleting it.
        getScreen().getAssayPlates().remove(ap);
        ap.setLibraryScreening(null);
        changed = true;
        iter.remove();
      }
    }
    return changed;
  }

  @Transient
  public SortedSet<LibraryPlate> getLibraryPlatesScreened()
  {
    Multimap<Integer,AssayPlate> index = Multimaps.index(getAssayPlatesScreened(),
                                                         new Function<AssayPlate,Integer>() {
                                                           @Override
                                                           public Integer apply(AssayPlate p)
                                                           {
                                                             return p.getPlateNumber();
                                                           }
                                                         });
    SortedSet<LibraryPlate> libraryPlates = Sets.newTreeSet();
    for (Integer plateNumber : index.keySet()) {
      Set<AssayPlate> assayPlates = Sets.newHashSet(index.get(plateNumber));
      libraryPlates.add(new LibraryPlate(plateNumber, assayPlates.iterator().next().getPlateScreened().getCopy().getLibrary(), assayPlates));
    }
    return libraryPlates;
  }
}
