// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.persistence.Version;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.hibernate.annotations.Type;
import org.joda.time.LocalDate;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.db.ScreenResultsDAO;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.AttachedFile;
import edu.harvard.med.screensaver.model.AttachedFilesEntity;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.MolarConcentration;
import edu.harvard.med.screensaver.model.RequiredPropertyException;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivity;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.annotations.Derived;
import edu.harvard.med.screensaver.model.annotations.ToMany;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickLiquidTransfer;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickLiquidTransferStatus;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.SmallMoleculeCherryPickRequest;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryPlate;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.meta.Cardinality;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.AssayPlate;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.util.NullSafeUtils;

/**
 * A screen tracks the progress of performing a screening assay, including a description of its biological significance,
 * its experimental protocol, and additional data to support the administrative needs of the facility. After screening
 * data is generated, a screen will contain a {@link ScreenResult}.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@org.hibernate.annotations.Proxy
public class Screen extends Study implements AttachedFilesEntity<ScreenAttachedFileType,Integer>
{

  // private static data

  private static final Logger log = Logger.getLogger(Screen.class);
  private static final long serialVersionUID = 0L;

  public static final RelationshipPath<Screen> thisEntity = RelationshipPath.from(Screen.class);
  public static final PropertyPath<Screen> facilityId = thisEntity.toProperty("facilityId");
  public static final RelationshipPath<Screen> screenResult = thisEntity.to("screenResult", Cardinality.TO_ONE);
  public static final RelationshipPath<Screen> labHead = thisEntity.to("labHead", Cardinality.TO_ONE);
  public static final RelationshipPath<Screen> leadScreener = thisEntity.to("leadScreener", Cardinality.TO_ONE);
  public static final RelationshipPath<Screen> collaborators = thisEntity.to("collaborators");
  public static final RelationshipPath<Screen> annotationTypes = thisEntity.to("annotationTypes");
  public static final RelationshipPath<Screen> cherryPickRequests = thisEntity.to("cherryPickRequests");
  public static final RelationshipPath<Screen> labActivities = thisEntity.to("labActivities");
  public static final RelationshipPath<Screen> statusItems = thisEntity.to("statusItems");
  public static final RelationshipPath<Screen> fundingSupports = thisEntity.to("fundingSupports");
  public static final PropertyPath<Screen> billingItems = thisEntity.toCollectionOfValues("billingItems");
  public static final RelationshipPath<Screen> attachedFiles = thisEntity.to("attachedFiles");
  public static final RelationshipPath<Screen> publications = thisEntity.to("publications");
  public static final PropertyPath<Screen> keywords = thisEntity.toCollectionOfValues("keywords");
  public static final RelationshipPath<Screen> pinTransferApprovalActivity = thisEntity.to("pinTransferApprovalActivity", Cardinality.TO_ONE);
  public static final RelationshipPath<Screen> reagents = thisEntity.to("reagents");
  public static final RelationshipPath<Screen> assayPlates = thisEntity.to("assayPlates");

  public static final Function<Screen,String> ToFacilityId = new Function<Screen,String>() {
    public String apply(Screen screen)
    {
      return screen.getFacilityId();
    }
  };

  public static final Function<Screen,String> ToNameFunction = new Function<Screen,String>() {
    public String apply(Screen s)
    {
      return s.getFacilityId();
    }
  };
  public static final Function<Screen,ScreenDataSharingLevel> ToDataSharingLevel = new Function<Screen,ScreenDataSharingLevel>() {
    public ScreenDataSharingLevel apply(Screen s)
    {
      return s.getDataSharingLevel();
    }
  };

  // private instance data

  // study (provides annotation of library contents)

  private Integer _version;
  private String _title;
  private ScreeningRoomUser _leadScreener; // should rename
  private LabHead _labHead;
  private SortedSet<ScreeningRoomUser> _collaborators = new TreeSet<ScreeningRoomUser>();
  private Set<Publication> _publications = new HashSet<Publication>();
  private String _url;
  private String _summary;
  private String _comments;
  private SortedSet<AnnotationType> _annotationTypes = new TreeSet<AnnotationType>();
  private Set<Reagent> _reagents = new HashSet<Reagent>();
  private StudyType _studyType;
  private boolean _isDownloadable = true;
  private Well wellStudied;

  // generic screen

  private String _facilityId;
  private ScreenType _screenType;
  private Set<AttachedFile> _attachedFiles = new HashSet<AttachedFile>();
  private SortedSet<String> _keywords = new TreeSet<String>();
  private String _publishableProtocol;
  private ScreenResult _screenResult;
  private SortedSet<AssayPlate> _assayPlates = Sets.newTreeSet();
  private ProjectPhase _projectPhase;
  private String _projectId;

  // iccb screen

  private SortedSet<StatusItem> _statusItems = new TreeSet<StatusItem>();
  private SortedSet<LabActivity> _labActivities = new TreeSet<LabActivity>();
  private LocalDate _dataMeetingScheduled;
  private LocalDate _dataMeetingComplete;
  private BillingInformation _billingInformation = new BillingInformation(this, false);
  private List<BillingItem> _billingItems = new ArrayList<BillingItem>();
  private Set<FundingSupport> _fundingSupports = new HashSet<FundingSupport>();
  private LocalDate _dateOfApplication;
  private Set<AbaseTestset> _abaseTestsets = new HashSet<AbaseTestset>();
  private String _abaseStudyId;
  private String _abaseProtocolId;
  private String _comsRegistrationNumber;
  private LocalDate _comsApprovalDate;
  private String _publishableProtocolComments;
  private LocalDate _publishableProtocolDateEntered;
  private String _publishableProtocolEnteredBy;
  private AdministrativeActivity _pinTransferApprovalActivity;
  private Set<CherryPickRequest> _cherryPickRequests = Sets.newHashSet();
  private ScreenDataSharingLevel _dataSharingLevel;
  private LocalDate _minAllowedDataPrivacyExpirationDate;
  private LocalDate _maxAllowedDataPrivacyExpirationDate;
  private LocalDate _dataPrivacyExpirationDate;
  private LocalDate _dataPrivacyExpirationNotifiedDate;
  
  private LocalDate _pubchemDepositedDate;
  private Integer _pubchemAssayId;

  private int _assayPlatesScreenedCount;
  private int _librariesScreenedCount;
  private int _libraryPlatesScreenedCount;
  private int _libraryPlatesDataLoadedCount;
  private int _libraryPlatesDataAnalyzedCount;
  private int _screenedExperimentalWellCount;
  private int _uniqueScreenedExperimentalWellCount;
  private int _totalPlatedLabCherryPicks;
  private Integer _minScreenedReplicateCount;
  private Integer _maxScreenedReplicateCount;
  private Integer _minDataLoadedReplicateCount;
  private Integer _maxDataLoadedReplicateCount;
  
  private Species _species;
  private CellLine _cellLine;
  private TransfectionAgent _transfectionAgent;
  private MolarConcentration perturbagenMolarConcentration;
  private BigDecimal perturbagenUgMlConcentration;
  
  // public constructors


  /**
   * Construct an uninitialized <code>Screen</code>.
   *
   * @motivation for new Screen creation via user interface, where even required
   *             fields are allowed to be uninitialized, initially
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected Screen() {}
  
  public Screen(AdministratorUser createdBy) 
  {
    super(createdBy);
    _dataSharingLevel = ScreenDataSharingLevel.PRIVATE;
  }

  /**
   * Construct an initialized <code>Screen</code>.
   * @param facilityId the screen facility ID
   * @param leadScreener the lead screener
   * @param labHead the lab head
   * @param screenType the screen type
   * @param studyType the study type
   * @param projectPhase TODO
   * @param title the title
   */
  public Screen(AdministratorUser createdBy,
                String facilityId,
                ScreeningRoomUser leadScreener,
                LabHead labHead,
                ScreenType screenType,
                StudyType studyType,
                ProjectPhase projectPhase,
                String title)
  {
    this(createdBy);
    _facilityId = facilityId;
    _screenType = screenType; // note: must be set before updateFacilityUsageRoleForAssociatedScreens() is called
    _studyType = studyType;
    _projectPhase = projectPhase;
    setLeadScreener(leadScreener);
    setLabHead(labHead);
    _title = title;
  }


  // public instance methods

  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    // TODO: HACK, until we have the real Study->Screen->IccbScreen hierarchy
    if (isStudyOnly()) {
      return visitor.visit((Study) this);
    }
    return visitor.visit(this);
  }

  /**
   * Get the id for the screen.
   * @return the id for the screen
   */
  @Id
  @org.hibernate.annotations.GenericGenerator(
    name="screen_id_seq",
    strategy="sequence",
    parameters = { @org.hibernate.annotations.Parameter(name="sequence", value="screen_id_seq") }
  )
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="screen_id_seq")
  public Integer getScreenId()
  {
    return getEntityId();
  }

  @ManyToMany(fetch = FetchType.LAZY, cascade={ CascadeType.PERSIST, CascadeType.MERGE })
  @JoinTable(name="screenUpdateActivity", 
             joinColumns=@JoinColumn(name="screenId", nullable=false, updatable=false),
             inverseJoinColumns=@JoinColumn(name="updateActivityId", nullable=false, updatable=false, unique=true))
  @org.hibernate.annotations.Cascade(value={org.hibernate.annotations.CascadeType.SAVE_UPDATE})
  @Sort(type=SortType.NATURAL)            
  @ToMany(singularPropertyName="updateActivity", hasNonconventionalMutation=true /* model testing framework doesn't understand this is a containment relationship, and so requires addUpdateActivity() method*/)
  @Override
  public SortedSet<AdministrativeActivity> getUpdateActivities()
  {
    return _updateActivities;
  }

  /**
   * Get the lead screener.
   * @return the lead screener
   */
  @ManyToOne(fetch=FetchType.LAZY, cascade={ CascadeType.PERSIST, CascadeType.MERGE })
  @JoinColumn(name="leadScreenerId"/*, nullable=false*/)
  @org.hibernate.annotations.ForeignKey(name="fk_screen_to_lead_screener")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @org.hibernate.annotations.Cascade(value={
    org.hibernate.annotations.CascadeType.SAVE_UPDATE
  })
  @edu.harvard.med.screensaver.model.annotations.ToOne(inverseProperty="screensLed")
  public ScreeningRoomUser getLeadScreener()
  {
    return _leadScreener;
  }

  /**
   * Set the lead screener.
   * @param leadScreener the new lead screener
   */
  public void setLeadScreener(ScreeningRoomUser leadScreener)
  {
    if (isHibernateCaller()) {
      _leadScreener = leadScreener;
      return;
    }
    if (leadScreener == null) {
      throw new NullPointerException();
    }
    if (_leadScreener != null) {
      _leadScreener.getScreensLed().remove(this);
      _leadScreener.updateFacilityUsageRoleForAssociatedScreens();
    }
    _leadScreener = leadScreener;
    _leadScreener.getScreensLed().add(this);
    _leadScreener.updateFacilityUsageRoleForAssociatedScreens();
  }

  /**
   * Get the lab head.
   * @return the lab head
   */
  @ManyToOne(fetch=FetchType.LAZY, cascade={ CascadeType.PERSIST, CascadeType.MERGE })
  @JoinColumn(name="labHeadId")
  @org.hibernate.annotations.ForeignKey(name="fk_screen_to_lab_head")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @org.hibernate.annotations.Cascade(value={ org.hibernate.annotations.CascadeType.SAVE_UPDATE })
  @edu.harvard.med.screensaver.model.annotations.ToOne(inverseProperty="screensHeaded")
  public LabHead getLabHead()
  {
    return _labHead;
  }

  /**
   * Set the lab head.
   * @param labHead the new lab head
   */
  public void setLabHead(LabHead labHead)
  {
    if (isHibernateCaller()) {
      _labHead = labHead;
      return;
    }
    if (NullSafeUtils.nullSafeEquals(labHead, _labHead)) {
      return;
    }
    if (_labHead != null) {
      _labHead.getScreensHeaded().remove(this);
      _labHead.updateFacilityUsageRoleForAssociatedScreens();
    }
    _labHead = labHead;
    if (_labHead != null) {
      _labHead.getScreensHeaded().add(this);
      _labHead.updateFacilityUsageRoleForAssociatedScreens();
    }
  }

  /**
   * Get the set of collaborators.
   * @return the set of collaborators
   */
  @ManyToMany(fetch=FetchType.LAZY)
  @JoinTable(
    name="collaboratorLink",
    joinColumns=@JoinColumn(name="screenId"),
    inverseJoinColumns=@JoinColumn(name="collaboratorId")
  )
  @org.hibernate.annotations.ForeignKey(name="fk_collaborator_link_to_screen")
  @org.hibernate.annotations.LazyCollection(value=org.hibernate.annotations.LazyCollectionOption.TRUE)
  @edu.harvard.med.screensaver.model.annotations.ToMany(inverseProperty="screensCollaborated")
  @Sort(type=SortType.NATURAL)
  public SortedSet<ScreeningRoomUser> getCollaborators()
  {
    return _collaborators;
  }

  /**
   * Add the collaborator.
   * @param collaborator the collaborator to add
   * @return true iff the screen did not already have the collaborator
   */
  public boolean addCollaborator(ScreeningRoomUser collaborator)
  {
    if (_collaborators.add(collaborator)) {
      collaborator.getScreensCollaborated().add(this);
      collaborator.updateFacilityUsageRoleForAssociatedScreens();
      return true;
    }
    return false;
  }

  /**
   * Remove the collaborator.
   * @param collaborator the collaborator to remove
   * @return true iff the screen previously had the collaborator
   */
  public boolean removeCollaborator(ScreeningRoomUser collaborator)
  {
    if (_collaborators.remove(collaborator)) {
      collaborator.getScreensCollaborated().remove(this);
      collaborator.updateFacilityUsageRoleForAssociatedScreens();
      return true;
    }
    return false;
  }

  @Transient
  public Set<ScreeningRoomUser> getAssociatedScreeningRoomUsers()
  {
    Set<ScreeningRoomUser> users = new HashSet<ScreeningRoomUser>();
    if (getLabHead() != null) {
      users.add(getLabHead());
    }
    users.add(getLeadScreener());
    users.addAll(getCollaborators());
    return users;
  }

  /**
   * Get the screen result.
   * @return the screen result
   */
  @OneToOne(
    mappedBy="screen",
    cascade={ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE },
    fetch=FetchType.LAZY
  )
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  @org.hibernate.annotations.Cascade(value={
    org.hibernate.annotations.CascadeType.SAVE_UPDATE,
    org.hibernate.annotations.CascadeType.DELETE
  })
  public ScreenResult getScreenResult()
  {
    return _screenResult;
  }
  
  AssayPlate createAssayPlate(Plate plateScreened, int replicateOrdinal, LibraryScreening libraryScreening)
  {
    AssayPlate assayPlate = new AssayPlate(this, plateScreened, replicateOrdinal);
    assayPlate.setLibraryScreening(libraryScreening);
    if (!_assayPlates.add(assayPlate)) {
      throw new DuplicateEntityException(this, assayPlate);
    }
    return assayPlate;
  }
  
  /**
   * @motivation this alternate factory method is necessary for cases where
   *             Screensaver has not been used to track library copies or
   *             library screenings, so that the necessary Plate and/or
   *             AssayPlate entities do not exist.
   */
  public AssayPlate createAssayPlate(int plateNumber, int replicateOrdinal) 
  {
    AssayPlate assayPlate = new AssayPlate(this, plateNumber, replicateOrdinal);
    if (!_assayPlates.add(assayPlate)) {
      throw new DuplicateEntityException(this, assayPlate);
    }
    return assayPlate;
  }
  
  public SortedSet<AssayPlate> findAssayPlates(final int plateNumber)
  {
    return ImmutableSortedSet.copyOf(Iterables.filter(getAssayPlates(), new Predicate<AssayPlate>() {
      public boolean apply(AssayPlate ap)
      {
        return ap.getPlateNumber() == plateNumber;
      }
    }));
  }

  /**
   * The collection of {@link AssayPlate}s that have been created for this
   * screen, which may contain multiple instances for a given library
   * {@link Plate} and replicate (if it required re-screening).
   * 
   * @return Collection of {@link AssayPlate}s
   */
  @OneToMany(mappedBy = "screen", cascade = { CascadeType.ALL }, orphanRemoval = true)
  @Sort(type=SortType.NATURAL)
  public SortedSet<AssayPlate> getAssayPlates()
  {
    return _assayPlates;
  }
  
  private void setAssayPlates(SortedSet<AssayPlate> assayPlates)
  {
    _assayPlates = assayPlates;
  }

  public int getLibrariesScreenedCount()
  {
    return _librariesScreenedCount;
  }

  public void setLibrariesScreenedCount(int librariesScreenedCount)
  {
    _librariesScreenedCount = librariesScreenedCount;
  }

  /**
   * @return a SortedSet of LibraryPlates. The library property will be null if the AssayPlate was not screened, but has
   *         had data loaded.
   */
  @Transient
  public SortedSet<LibraryPlate> getLibraryPlatesScreened()
  {
    Multimap<Integer,AssayPlate> index = Multimaps.index(Iterables.filter(getAssayPlates(), AssayPlate.IsScreened),
                                                         new Function<AssayPlate,Integer>() {
                                                           @Override
                                                           public Integer apply(AssayPlate p)
                                                           {
                                                             return p.getPlateNumber();
                                                           }
                                                         });
    SortedSet<LibraryPlate> libraryPlates = Sets.newTreeSet();
    for (Integer plateNumber : index.keySet()) {
      AssayPlate firstAssayPlate = Iterables.get(index.get(plateNumber), 0);
      Library library = null;
      if (firstAssayPlate.getPlateScreened() != null) {
        library = firstAssayPlate.getPlateScreened().getCopy().getLibrary();
      }
      libraryPlates.add(new LibraryPlate(plateNumber, 
                                         library,
                                         Sets.newHashSet(index.get(plateNumber))));
    }
    return libraryPlates;
  }

  @Transient
  public SortedSet<AssayPlate> getAssayPlatesDataLoaded()
  {
    return Sets.newTreeSet(Iterables.filter(getAssayPlates(), AssayPlate.IsDataLoaded));
  }

  @Transient
  public SortedSet<AssayPlate> getAssayPlatesScreened()
  {
    return Sets.newTreeSet(Iterables.filter(getAssayPlates(), AssayPlate.HasLibraryScreening));
  }

  @Derived
  public int getAssayPlatesScreenedCount()
  {
    return _assayPlatesScreenedCount;
  }

  public void setAssayPlatesScreenedCount(int assayPlatesScreenedCount)
  {
    _assayPlatesScreenedCount = assayPlatesScreenedCount;
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
  public int getLibraryPlatesDataLoadedCount()
  {
    return _libraryPlatesDataLoadedCount;
  }

  public void setLibraryPlatesDataLoadedCount(int libraryPlatesDataLoadedCount)
  {
    _libraryPlatesDataLoadedCount = libraryPlatesDataLoadedCount;
  }

  @Derived
  public int getLibraryPlatesDataAnalyzedCount()
  {
    return _libraryPlatesDataAnalyzedCount;
  }

  public void setLibraryPlatesDataAnalyzedCount(int libraryPlatesDataAnalyzedCount)
  {
    _libraryPlatesDataAnalyzedCount = libraryPlatesDataAnalyzedCount;
  }

  @Derived
  public Integer getMinScreenedReplicateCount()
  {
    return _minScreenedReplicateCount;
  }

  public void setMinScreenedReplicateCount(Integer minScreenedReplicateCount)
  {
    _minScreenedReplicateCount = minScreenedReplicateCount;
  }

  @Derived
  public Integer getMaxScreenedReplicateCount()
  {
    return _maxScreenedReplicateCount;
  }

  public void setMaxScreenedReplicateCount(Integer maxScreenedReplicateCount)
  {
    _maxScreenedReplicateCount = maxScreenedReplicateCount;
  }

  @Derived
  public Integer getMinDataLoadedReplicateCount()
  {
    return _minDataLoadedReplicateCount;
  }

  public void setMinDataLoadedReplicateCount(Integer minDataLoadedReplicateCount)
  {
    _minDataLoadedReplicateCount = minDataLoadedReplicateCount;
  }

  @Derived
  public Integer getMaxDataLoadedReplicateCount()
  {
    return _maxDataLoadedReplicateCount;
  }

  public void setMaxDataLoadedReplicateCount(Integer maxDataLoadedReplicateCount)
  {
    _maxDataLoadedReplicateCount = maxDataLoadedReplicateCount;
  }

  @Derived
  public int getScreenedExperimentalWellCount()
  {
    return _screenedExperimentalWellCount;
  }

  public void setScreenedExperimentalWellCount(int screenedExperimentalWellCount)
  {
    _screenedExperimentalWellCount = screenedExperimentalWellCount;
  }

  @Derived
  public int getUniqueScreenedExperimentalWellCount()
  {
    return _uniqueScreenedExperimentalWellCount;
  }

  public void setUniqueScreenedExperimentalWellCount(int uniqueScreenedExperimentalWellCount)
  {
    _uniqueScreenedExperimentalWellCount = uniqueScreenedExperimentalWellCount;
  }

  @Derived
  public int getTotalPlatedLabCherryPicks()
  {
    return _totalPlatedLabCherryPicks;
  }

  public void setTotalPlatedLabCherryPicks(int totalPlatedLabCherryPicks)
  {
    _totalPlatedLabCherryPicks = totalPlatedLabCherryPicks;
  }

  /**
   * Create and return a new screen result for the screen.
   * 
   * @return the new screen result
   */
  public ScreenResult createScreenResult()
  {
    _screenResult = new ScreenResult(this, null);
    return _screenResult;
  }

  /**
   * Clear the screen result (in memory only). Use {@link ScreenResultsDAO#deleteScreenResult(ScreenResult)} to delete
   * from persistent storage.
   */
  public void clearScreenResult()
  {
    _screenResult = null;
    for (AssayPlate assayPlate : getAssayPlates()) {
      assayPlate.setScreenResultDataLoading(null);
    }
  }

  /**
   * Get the status items. A Screen may only contain one status with a given
   * {@link ScreenStatus#getRank() rank} value (StatusItems with the same rank are mutually
   * exclusive). Ordering of StatusItems must be equivalent whether by
   * {@link ScreenStatus#getRank() rank} or {@link StatusItem#getStatusDate() date}.
   *
   * @return the status items
   */
  @ElementCollection
  @JoinTable(name = "screen_status_item",
             joinColumns = @JoinColumn(name = "screen_id"))
  @Sort(type=SortType.NATURAL)
  public SortedSet<StatusItem> getStatusItems()
  {
    return _statusItems;
  }

  @Transient
  public StatusItem getCurrentStatusItem()
  {
    if (_statusItems.isEmpty()) {
      return null;
    }
    return _statusItems.last();
  }

  /**
   * Create and return a new <code>StatusItem</code> for this screen.
   * @param statusDate the status date
   * @param screenStatus the status value
   * @return the new status item
   */
  public StatusItem createStatusItem(LocalDate statusDate, ScreenStatus screenStatus)
  {
    for (StatusItem statusItem : _statusItems) {
      if (statusItem.getStatus().getRank() == screenStatus.getRank()) {
        throw new BusinessRuleViolationException("screen status " + screenStatus +
                                                 " is mutually exclusive with existing screen status " + statusItem.getStatus());
      }
      if (screenStatus.getRank() < statusItem.getStatus().getRank()) {
        if (statusDate.compareTo(statusItem.getStatusDate()) > 0) {
          throw new BusinessRuleViolationException("date of new screen status must not be after date of subsequent screen status");
        }
      }
      else {
        if (statusDate.compareTo(statusItem.getStatusDate()) < 0) {
          throw new BusinessRuleViolationException("date of new screen status must not be before the date of the previous screen status");
        }
      }
    }
    StatusItem newStatusItem = new StatusItem(statusDate, screenStatus);
    _statusItems.add(newStatusItem);
    return newStatusItem;
  }

  /**
   * Get the lab activities.
   * @return the lab activities
   */
  @OneToMany(mappedBy = "screen", cascade = { CascadeType.ALL })
  @Sort(type=SortType.NATURAL)
  @edu.harvard.med.screensaver.model.annotations.ToMany(singularPropertyName="labActivity", 
                                                        hasNonconventionalMutation=true /* uses createLibraryScreening() and createRNAiCherryPickScreening */)
  public SortedSet<LabActivity> getLabActivities()
  {
    return _labActivities;
  }

  /**
   * Get all the lab activities for this screen of a particular type.
   * @param <E> the type of the lab activities to get
   * @param clazz the type of the lab activities to get
   * @return all the lab activities for this screen of a particular type.
   */
  @SuppressWarnings("unchecked")
  @Transient
  public <E extends LabActivity> SortedSet<E> getLabActivitiesOfType(Class<E> clazz)
  {
    SortedSet<E> result = new TreeSet<E>();
    for (LabActivity labActivity : _labActivities) {
      if (clazz.isAssignableFrom(labActivity.getClass())) {
        result.add((E) labActivity);
      }
    }
    return result;
  }

  /**
   * Create and return a new library screening for the screen.
   * @param performedBy the user that performed the screening
   * @param dateOfActivity the date the lab activity took place
   * @return the new library screening
   */
  public LibraryScreening createLibraryScreening(AdministratorUser recordedBy,
                                                 ScreeningRoomUser performedBy,
                                                 LocalDate dateOfActivity)
  {
    LibraryScreening libraryScreening =
      new LibraryScreening(this, recordedBy, performedBy, dateOfActivity);
    _labActivities.add(libraryScreening);
    return libraryScreening;
  }

  /**
   * Create and return a new cherry pick liquid transfer for the screen.
   * @param performedBy the user that performed the activity
   * @param dateOfActivity the date the lab activity took place
   * @param status the status of the cherry pick liquid transfer
   * @return the new cherry pick liquid transfer
   */
  public CherryPickLiquidTransfer createCherryPickLiquidTransfer(AdministratorUser recordedBy,
                                                                 ScreensaverUser performedBy,
                                                                 LocalDate dateOfActivity,
                                                                 CherryPickLiquidTransferStatus status)
  {
    CherryPickLiquidTransfer cherryPickLiquidTransfer = new CherryPickLiquidTransfer(this,
                                                                                     recordedBy,
                                                                                     performedBy,
                                                                                     dateOfActivity,
                                                                                     status);
    _labActivities.add(cherryPickLiquidTransfer);
    return cherryPickLiquidTransfer;
  }

  /**
   * Create and return a new cherry pick screening for the screen.
   * @param performedBy the user that performed the screening
   * @param dateOfActivity the date the screening took place
   * @param cherryPickRequest the cherry pick request
   * @return the newly created cherry pick screening
   */
  public CherryPickScreening createCherryPickScreening(AdministratorUser recordedBy,
                                                       ScreeningRoomUser performedBy,
                                                       LocalDate dateOfActivity,
                                                       CherryPickRequest cherryPickRequest)
  {
    CherryPickScreening screening = new CherryPickScreening(
      this,
      recordedBy,
      performedBy,
      dateOfActivity,
      cherryPickRequest);
    _labActivities.add(screening);
    return screening;
  }

  /**
   * Get the cherry pick requests.
   * @return the cherry pick requests
   */
  @OneToMany(mappedBy = "screen", cascade = { CascadeType.ALL })
  public Set<CherryPickRequest> getCherryPickRequests()
  {
    return _cherryPickRequests;
  }

  /**
   * Create and return a new cherry pick request for the screen of the appropriate type ({@link
   * SmallMoleculeCherryPickRequest} or {@link RNAiCherryPickRequest}. The cherry pick request will
   * have the {@link #getLeadScreener() lead screener} as the {@link
   * CherryPickRequest#getRequestedBy() requestor}, and the current date as the {@link
   * CherryPickRequest#getDateRequested() date requested}. It will not be a legacy ScreenDB
   * cherry pick.
   * @return the new cherry pick request
   */
  public CherryPickRequest createCherryPickRequest(AdministratorUser createdBy)
  {
    return createCherryPickRequest(createdBy, getLeadScreener(), new LocalDate());
  }

  /**
   * Create and return a new cherry pick request for the screen of the appropriate type ({@link
   * SmallMoleculeCherryPickRequest} or {@link RNAiCherryPickRequest}. The cherry pick request will
   * not be a legacy ScreenDB cherry pick.
   * @param requestedBy the requestor
   * @param dateRequested the date requested
   * @return the new cherry pick request
   */
  public CherryPickRequest createCherryPickRequest(AdministratorUser createdBy,
                                                   ScreeningRoomUser requestedBy,
                                                   LocalDate dateRequested)
  {
    CherryPickRequest cherryPickRequest;
    if (getScreenType().equals(ScreenType.RNAI)) {
      cherryPickRequest = new RNAiCherryPickRequest(createdBy, this, requestedBy, dateRequested);
    }
    else if(getScreenType().equals(ScreenType.SMALL_MOLECULE)) {
      cherryPickRequest = new SmallMoleculeCherryPickRequest(createdBy, this, requestedBy, dateRequested);
    }
    else {
      throw new UnsupportedOperationException(
        "screen of type " + getScreenType() + " does not support cherry pick requests");
    }
    _cherryPickRequests.add(cherryPickRequest);
    return cherryPickRequest;
  }

  /**
   * Get the set of abase testsets.
   * @return the abase testsets
   */
  @OneToMany(mappedBy = "screen", cascade = { CascadeType.ALL }, orphanRemoval = true)
  public Set<AbaseTestset> getAbaseTestsets()
  {
    return _abaseTestsets;
  }

  /**
   * Create and return an <code>AbaseTestset</code> for the screen.
   * @param testsetDate the testset date
   * @param testsetName the testset name
   * @param comments the comments
   * @return the new abase testset
   */
  public AbaseTestset createAbaseTestset(LocalDate testsetDate, String testsetName, String comments)
  {
    AbaseTestset abaseTestset = new AbaseTestset(this, testsetDate, testsetName, comments);
    _abaseTestsets.add(abaseTestset);
    return abaseTestset;
  }

  /**
   * Get the publications.
   * @return the publications
   */
  @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE }, fetch = FetchType.LAZY)
  @JoinTable(name = "screenPublicationLink", joinColumns = @JoinColumn(name = "screenId"), inverseJoinColumns = @JoinColumn(name = "publicationId"))
  @org.hibernate.annotations.ForeignKey(name = "fk_screen_publication_link_to_screen")
  public Set<Publication> getPublications()
  {
    return _publications;
  }

  // note: for automated model unit tests, we can't name this createPublication or addPublication
  public Publication addCopyOfPublication(Publication publicationDTO)
  {
    Publication publication = new Publication();
    publication.setPubmedId(publicationDTO.getPubmedId());
    publication.setPubmedCentralId(publicationDTO.getPubmedCentralId());
    publication.setTitle(publicationDTO.getTitle());
    publication.setYearPublished(publicationDTO.getYearPublished());
    publication.setAuthors(publicationDTO.getAuthors());
    publication.setJournal(publicationDTO.getJournal());
    publication.setVolume(publicationDTO.getVolume());
    publication.setPages(publicationDTO.getPages());
    addPublication(publication);
    return publication;
  }

  /**
   * Get the attached files.
   * @return the attached files
   */
  @OneToMany(mappedBy = "screen", cascade = { CascadeType.ALL }, orphanRemoval=true)
  @ToMany(hasNonconventionalMutation=true)
  public Set<AttachedFile> getAttachedFiles()
  {
    return _attachedFiles;
  }

  /**
   * Create and return a new attached file for the screen.
   * @param filename the filename
   * @param fileType the file type
   * @param fileContents the file contents
   * @throws IOException
   */
  public AttachedFile createAttachedFile(String filename,
                                         ScreenAttachedFileType fileType,
                                         LocalDate fileDate,
                                         String fileContents) throws IOException
  {
    return createAttachedFile(filename, fileType, fileDate, new ByteArrayInputStream(fileContents.getBytes()));
  }

  /**
   * Create and return a new attached file for the screen. Use
   * {@link Publication#createAttachedFile} to create an attached file that is
   * associated with a Publication.
   * 
   * @param filename the filename
   * @param fileType the file type
   * @param fileContents the file contents
   * @throws IOException
   */
  public AttachedFile createAttachedFile(String filename,
                                         ScreenAttachedFileType fileType,
                                         LocalDate fileDate,
                                         InputStream fileContents) throws IOException
  {
    AttachedFile attachedFile = new AttachedFile(this, filename, fileType, fileDate, fileContents);
    _attachedFiles.add(attachedFile);
    return attachedFile;
  }

  public void removeAttachedFile(AttachedFile attachedFile)
  {
    _attachedFiles.remove(attachedFile);
  }

  /**
   * Get the billing information.
   * @return the billing information
   */
  @edu.harvard.med.screensaver.model.annotations.Column(hasNonconventionalSetterMethod=true)
  public BillingInformation getBillingInformation()
  {
    return _billingInformation;
  }
  
  /**
   * Get the set of billing items.
   * @return the billing items
   */
  @ElementCollection
  @edu.harvard.med.screensaver.model.annotations.ElementCollection(hasNonconventionalMutation = true)
  @JoinTable(name = "screen_billing_item",
             joinColumns = @JoinColumn(name = "screen_id"))
  @org.hibernate.annotations.IndexColumn(name="ordinal")
  public List<BillingItem> getBillingItems()
  {
    return _billingItems;
  }
  
  /**
   * Set the set of billing items.
   * @param billingItems the new set of billing items
   * @motivation for hibernate
   */
  private void setBillingItems(List<BillingItem> billingItems)
  {
    _billingItems = billingItems;
  }

  /**
   * Create and return a new billing item for this billing information.
   * @param itemToBeCharged the item to be charged
   * @param amount the amount
   * @param dateSentForBilling the date sent for billing
   * @return the new billing item for this billing information
   */
  public BillingItem createBillingItem(String itemToBeCharged, BigDecimal amount, LocalDate dateSentForBilling)
  {
    if (itemToBeCharged == null) {
      throw new RequiredPropertyException(this, "billing item name");
    }
    if (amount == null) {
      throw new RequiredPropertyException(this, "billing item amount");
    }
    // allowed, as per [#1607]
    //if (dateSentForBilling == null) {
    //  throw new RequiredPropertyException(this, "billing item date faxed");
    //}
    BillingItem billingItem = new BillingItem(itemToBeCharged, amount, dateSentForBilling);
    _billingItems.add(billingItem);
    return billingItem;
  }

  public BillingItem addCopyOfBillingItem(BillingItem dtoBillingItem)
  {
    return createBillingItem(dtoBillingItem.getItemToBeCharged(),
                             dtoBillingItem.getAmount(),
                             dtoBillingItem.getDateSentForBilling());
  }

  @Column(unique=true, nullable=false)
  @org.hibernate.annotations.Type(type="text")
  public String getFacilityId()
  {
    return _facilityId;
  }

  public void setFacilityId(String name)
  {
    _facilityId = name;
  }

  @Column
  @org.hibernate.annotations.Type(type="edu.harvard.med.screensaver.model.screens.Species$UserType")
  public Species getSpecies()
  {
    return _species;
  }

  public void setSpecies(Species value)
  {
    _species = value;
  }  

  /**
   * Get the study type.
   * @return the study type
   */
  @Column(nullable = false)
  @org.hibernate.annotations.Type(type="edu.harvard.med.screensaver.model.screens.StudyType$UserType")
  public StudyType getStudyType()
  {
    return _studyType;
  }

  /**
   * Set the study type.
   *
   * @param studyType the new studyType
   */
  public void setStudyType(StudyType studyType)
  {
    // commenting this until comprehensive testing is performed, since it requires many relationships to be eager fetched, which may cause problems
//  if (isDataLoaded()) {
//    throw new BusinessRuleViolationException("screen type is immutable after screen contains data");
//  }
    _studyType = studyType;
  }

  /**
   * Get the screen type.
   * @return the screen type
   */
  @Column(nullable=false)
  @org.hibernate.annotations.Type(type="edu.harvard.med.screensaver.model.screens.ScreenType$UserType")
  public ScreenType getScreenType()
  {
    return _screenType;
  }

  /**
   * Set the screen type.
   * @param screenType the new screen type
   * @motivation for hibernate
   */
  public void setScreenType(ScreenType screenType)
  {
    // commenting this until comprehensive testing is performed, since it requires many relationships to be eager fetched, which may cause problems
//    if (isDataLoaded()) {
//      throw new BusinessRuleViolationException("screen type is immutable after screen contains data");
//    }
    _screenType = screenType;
  }

  public void setProjectPhase(ProjectPhase projectPhase)
  {
    _projectPhase = projectPhase;
  }

  @Column(nullable = false)
  @org.hibernate.annotations.Type(type = "edu.harvard.med.screensaver.model.screens.ProjectPhase$UserType")
  public ProjectPhase getProjectPhase()
  {
    return _projectPhase;
  }

  @Transient
  public boolean isDataLoaded()
  {
    return getScreenResult() != null || !getCherryPickRequests().isEmpty() || !getAnnotationTypes().isEmpty() || !getLabActivities().isEmpty();
  }

  /**
   * Get the title.
   * @return the title
   */
  @Column(nullable=false)
  @org.hibernate.annotations.Type(type="text")
  public String getTitle()
  {
    return _title;
  }

  /**
   * Set the title.
   * @param title the new title
   */
  public void setTitle(String title)
  {
    _title = title;
  }

  public void setProjectId(String projectId)
  {
    _projectId = projectId;
  }

  @Column(nullable = true)
  @org.hibernate.annotations.Type(type = "text")
  public String getProjectId()
  {
    return _projectId;
  }

  /**
   * Get the study url.
   * @return the study url
   */
  @Column(nullable=true)
  @org.hibernate.annotations.Type(type="text")
  public String getUrl()
  {
    return _url;
  }

  /**
   * Set the study url.
   * @param url the new study url
   */
  public void setUrl(String url)
  {
    _url = url;
  }

  /**
   * Get the data meeting scheduled.
   * @return the data meeting scheduled
   */
  @Column
  @Type(type="edu.harvard.med.screensaver.db.usertypes.LocalDateType")
  public LocalDate getDataMeetingScheduled()
  {
    return _dataMeetingScheduled;
  }

  /**
   * Set the data meeting scheduled date.
   * @param dataMeetingScheduled the new data meeting scheduled date
   */
  public void setDataMeetingScheduled(LocalDate dataMeetingScheduled)
  {
    _dataMeetingScheduled = dataMeetingScheduled;
  }

  /**
   * Get the data meeting completed date.
   * @return the data meeting completed date
   */
  @Column
  @Type(type="edu.harvard.med.screensaver.db.usertypes.LocalDateType")
  public LocalDate getDataMeetingComplete()
  {
    return _dataMeetingComplete;
  }

  /**
   * Set the data meeting complete.
   * @param dataMeetingComplete the new data meeting complete
   */
  public void setDataMeetingComplete(LocalDate dataMeetingComplete)
  {
    _dataMeetingComplete = dataMeetingComplete;
  }

  /**
   * Get the keywords.
   * @return the keywords
   */
  @ElementCollection
  @Column(name="keyword", nullable=false)
  @JoinTable(
    name="screenKeyword",
    joinColumns=@JoinColumn(name="screenId")
  )
  @Sort(type=SortType.NATURAL)
  @org.hibernate.annotations.Type(type="text")
  @org.hibernate.annotations.ForeignKey(name="fk_screen_keyword_to_screen")
  public SortedSet<String> getKeywords()
  {
    return _keywords;
  }

  /**
   * Set the keywords.
   * @param keywords the new keywords
   */
  public void setKeywords(SortedSet<String> keywords)
  {
    _keywords = keywords;
  }

  /**
   * Add the keyword.
   * @param keyword the keyword to add
   * @return true iff the screen did not already have the keyword
   */
  public boolean addKeyword(String keyword)
  {
    return _keywords.add(keyword);
  }

  /**
   * Remove the keyword.
   * @param keyword the keyword to remove
   * @return true iff the screen previously had the keyword
   */
  public boolean removeKeyword(String keyword)
  {
    return _keywords.remove(keyword);
  }

  /**
   * Get the set of funding supports.
   * @return the set of funding supports
   */
  @ManyToMany(fetch=FetchType.LAZY)
  @JoinTable(
    name="screenFundingSupportLink",
    joinColumns=@JoinColumn(name="screenId"),
    inverseJoinColumns=@JoinColumn(name="fundingSupportId"))
  @org.hibernate.annotations.LazyCollection(value=org.hibernate.annotations.LazyCollectionOption.TRUE)
  @edu.harvard.med.screensaver.model.annotations.ToMany(unidirectional=true)
  public Set<FundingSupport> getFundingSupports()
  {
    return _fundingSupports;
  }

  /**
   * Add the funding support.
   * @param fundingSupport the funding support to add
   * @return true iff the screen did not already have the funding support
   */
  public boolean addFundingSupport(FundingSupport fundingSupport)
  {
    return _fundingSupports.add(fundingSupport);
  }

  /**
   * Remove the funding support.
   * @param fundingSupport the funding support to remove
   * @return true iff the screen previously had the funding support
   */
  public boolean removeFundingSupport(FundingSupport fundingSupport)
  {
    return _fundingSupports.remove(fundingSupport);
  }

  /**
   * Get the summary.
   * @return the summary
   */
  @org.hibernate.annotations.Type(type="text")
  public String getSummary()
  {
    return _summary;
  }

  /**
   * Set the summary.
   * @param summary the new summary
   */
  public void setSummary(String summary)
  {
    _summary = summary;
  }

  /**
   * Get the comments.
   * @return the comments
   */
  @org.hibernate.annotations.Type(type="text")
  public String getComments()
  {
    return _comments;
  }

  /**
   * Set the comments.
   * @param comments the new comments
   */
  public void setComments(String comments)
  {
    _comments = comments;
  }

  /**
   * Get the abase study id.
   * @return the abase study id
   */
  @org.hibernate.annotations.Type(type="text")
  public String getAbaseStudyId()
  {
    return _abaseStudyId;
  }

  /**
   * Set the abase study id.
   * @param abaseStudyId the new abase study id
   */
  public void setAbaseStudyId(String abaseStudyId)
  {
    _abaseStudyId = abaseStudyId;
  }

  /**
   * Get the abase protocol id.
   * @return the abase protocol id
   */
  @org.hibernate.annotations.Type(type="text")
  public String getAbaseProtocolId()
  {
    return _abaseProtocolId;
  }

  /**
   * Set the abase protocol id.
   * @param abaseProtocolId the new abase protocol id
   */
  public void setAbaseProtocolId(String abaseProtocolId)
  {
    _abaseProtocolId = abaseProtocolId;
  }

  @org.hibernate.annotations.Type(type="text")
  public String getComsRegistrationNumber()
  {
    return _comsRegistrationNumber;
  }

  public void setComsRegistrationNumber(String comsRegistrationNumber)
  {
    _comsRegistrationNumber = comsRegistrationNumber;
  }

  @Type(type="edu.harvard.med.screensaver.db.usertypes.LocalDateType")
  public LocalDate getComsApprovalDate()
  {
    return _comsApprovalDate;
  }

  public void setComsApprovalDate(LocalDate comsApprovalDate)
  {
    _comsApprovalDate = comsApprovalDate;
  }

  // TODO: extract PublishableProtocol value-typed collection

  /**
   * Get the date the publishable protocol was entered.
   * @return the date the publishable protocol was entered
   */
  @Type(type="edu.harvard.med.screensaver.db.usertypes.LocalDateType")
  public LocalDate getPublishableProtocolDateEntered()
  {
    return _publishableProtocolDateEntered;
  }

  /**
   * Set the date the publishable protocol was entered.
   * @param publishableProtocolDateEntered the new date the publishable protocol was entered
   */
  public void setPublishableProtocolDateEntered(LocalDate publishableProtocolDateEntered)
  {
    _publishableProtocolDateEntered = publishableProtocolDateEntered;
  }

  /**
   * Get the initials of the administrator who entered the publishable protocol.
   * @return the initials of the administrator who entered the publishable protocol
   */
  @org.hibernate.annotations.Type(type="text")
  public String getPublishableProtocolEnteredBy()
  {
    return _publishableProtocolEnteredBy;
  }

  /**
   * Set the initials of the administrator who entered the publishable protocol.
   * @param publishableProtocolEnteredBy the new initials of the administrator who
   * entered the publishable protocol
   */
  public void setPublishableProtocolEnteredBy(String publishableProtocolEnteredBy)
  {
    _publishableProtocolEnteredBy = publishableProtocolEnteredBy;
  }

  /**
   * Get the publishable protocol.
   * @return the publishable protocol
   */
  @org.hibernate.annotations.Type(type="text")
  public String getPublishableProtocol()
  {
    return _publishableProtocol;
  }

  /**
   * Set the publishable protocol.
   * @param publishableProtocol the new publishable protocol
   */
  public void setPublishableProtocol(String publishableProtocol)
  {
    _publishableProtocol = publishableProtocol;
  }

  /**
   * Get the publishable protocol comments.
   * @return the publishable protocol comments
   */
  @org.hibernate.annotations.Type(type="text")
  public String getPublishableProtocolComments()
  {
    return _publishableProtocolComments;
  }

  /**
   * Set the publishable protocol comments.
   * @param publishableProtocolComments the new publishable protocol comments
   */
  public void setPublishableProtocolComments(String publishableProtocolComments)
  {
    _publishableProtocolComments = publishableProtocolComments;
  }

  @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY)
  @JoinColumn(name = "pin_transfer_admin_activity_id")
  @org.hibernate.annotations.ForeignKey(name = "fk_screen_to_pin_transfer_admin_activity")
  @org.hibernate.annotations.LazyToOne(value = org.hibernate.annotations.LazyToOneOption.PROXY)
  @org.hibernate.annotations.Cascade(value = { org.hibernate.annotations.CascadeType.SAVE_UPDATE })
  @edu.harvard.med.screensaver.model.annotations.ToOne(unidirectional=true, hasNonconventionalSetterMethod=true)
  public AdministrativeActivity getPinTransferApprovalActivity()
  {
    return _pinTransferApprovalActivity;
  }

  private void setPinTransferApprovalActivity(AdministrativeActivity pinTransferApprovalActivity)
  {
    _pinTransferApprovalActivity = pinTransferApprovalActivity;
  }
  
  public void setPinTransferApproved(AdministratorUser recordedBy,
                                     AdministratorUser approvedBy,
                                     LocalDate dateApproved,
                                     String comments)
  {
    if (_pinTransferApprovalActivity != null) {
      throw new BusinessRuleViolationException("pin transfer approval already recorded");
    }
    _pinTransferApprovalActivity = new AdministrativeActivity(recordedBy,
                                                              approvedBy,
                                                              dateApproved,
                                                              AdministrativeActivityType.PIN_TRANSFER_APPROVAL);
    _pinTransferApprovalActivity.setComments(comments);
  }

  /**
   * Get the date of application.
   * @return the date of application
   */
  @Column
  @Type(type="edu.harvard.med.screensaver.db.usertypes.LocalDateType")
  public LocalDate getDateOfApplication()
  {
    return _dateOfApplication;
  }

  /**
   * Set the date of application.
   * @param dateOfApplication the new date of application
   */
  public void setDateOfApplication(LocalDate dateOfApplication)
  {
    _dateOfApplication = dateOfApplication;
  }

  /**
   * Get the annotation types provided by this study.
   * @return the annotation types
   */
  @OneToMany(mappedBy = "study", cascade = { CascadeType.ALL }, orphanRemoval = true)
  @Sort(type = SortType.NATURAL)
  public SortedSet<AnnotationType> getAnnotationTypes()
  {
    return _annotationTypes;
  }

  /**
   * Create and return a new annotation type for the study.
   * @param name the name of the annotation type
   * @param description the description for the annotation type
   * @param isNumeric true iff this annotation type contains numeric result values
   * @return the new annotation type
   */
  public AnnotationType createAnnotationType(
      String name,
      String description,
      boolean isNumeric)
  {
    verifyNameIsUnique(name);
    AnnotationType annotationType = new AnnotationType(
      this,
      name,
      description,
      _annotationTypes.size(),
      isNumeric);
    _annotationTypes.add(annotationType);
    return annotationType;
  }

  /**
   * Get the set of reagents associated with this screen result. <i>Do not modify
   * the returned collection.</i> To add a reagent, call {@link #addReagent}.
   * @motivation efficiently find all reagent-related data for a study (w/o reading annotationTypes.annotationValues.reagents)
   * @return the set of reagents associated with this screen result
   */
  @ManyToMany(fetch=FetchType.LAZY)
  @JoinTable(name = "studyReagentLink", joinColumns = @JoinColumn(name = "studyId"), inverseJoinColumns = @JoinColumn(name = "reagentId"))
  @org.hibernate.annotations.ForeignKey(name="fk_reagent_link_to_study")
  @org.hibernate.annotations.LazyCollection(value=org.hibernate.annotations.LazyCollectionOption.TRUE)
  @edu.harvard.med.screensaver.model.annotations.ToMany(inverseProperty="studies")
  public Set<Reagent> getReagents()
  {
    return _reagents;
  }

  /**
   * Add the reagent.
   * @param reagent the reagent to add
   * @return true iff the screen did not already have the reagent
   */
  public boolean addReagent(Reagent reagent)
  {
    return addReagent(reagent, true);
  }
  
  public boolean addReagent(Reagent reagent, boolean createStudiesLink)
  {
    if (_reagents.add(reagent)) {
      if (createStudiesLink) {
        reagent.addStudy(this);
      }
      return true;
    }
    return false;
  }

  /**
   * Remove the reagent.
   * @param reagent the reagent to remove
   * @return true iff the screen previously had the reagent
   */
  public boolean removeReagent(Reagent reagent)
  {
    if (_reagents.remove(reagent)) {
      reagent.removeStudy(this);
      return true;
    }
    return false;
  }

  public boolean addPublication(Publication p)
  {
    return _publications.add(p);
  }

  @Transient
  public List<ScreenStatus> getCandidateStatuses()
  {
    List<ScreenStatus> candidateStatuses = new ArrayList<ScreenStatus>(Arrays.asList(ScreenStatus.values()));
    Set<Integer> illegalStatusRanks = new HashSet<Integer>();
    for (StatusItem statusItem : getStatusItems()) {
      illegalStatusRanks.add(statusItem.getStatus().getRank());
    }
    Iterator<ScreenStatus> iter = candidateStatuses.iterator();
    while (iter.hasNext()) {
      if (illegalStatusRanks.contains(iter.next().getRank())) {
        iter.remove();
      }
    }
    return candidateStatuses;
  }


  // private instance methods

  /**
   * Set the id for the screen.
   * @param screenId the new id for the screen
   * @motivation for hibernate
   */
  private void setScreenId(Integer screenId)
  {
    setEntityId(screenId);
  }

  /**
   * Get the version for the screen.
   * @return the version for the screen
   * @motivation for hibernate
   */
  @Version
  @Column(nullable=false)
  private Integer getVersion()
  {
    return _version;
  }

  /**
   * Set the version for the screen.
   * @param version the new version for the screen
   * @motivation for hibernate
   */
  private void setVersion(Integer version)
  {
    _version = version;
  }

  /**
   * Set the set of collaborators.
   * @param collaborators the new set of collaborators
   * @motivation for hibernate
   */
  private void setCollaborators(SortedSet<ScreeningRoomUser> collaborators)
  {
    _collaborators = collaborators;
  }

  /**
   * Set the screen result.
   * @param screenResult the new screen result
   * @motivation for hibernate
   */
  private void setScreenResult(ScreenResult screenResult)
  {
    _screenResult = screenResult;
  }

  /**
   * Set the status items.
   * @param statusItems the new status items
   * @motivation for hibernate
   */
  private void setStatusItems(SortedSet<StatusItem> statusItems)
  {
    _statusItems = statusItems;
  }

  /**
   * @motivation for hibernate
   */
  private void setLabActivities(SortedSet<LabActivity> labActivities)
  {
    _labActivities = labActivities;
  }

  /**
   * Set the cherry pick requests.
   * @param cherryPickRequests the new cherry pick requests
   * @motivation for hibernate
   */
  private void setCherryPickRequests(Set<CherryPickRequest> cherryPickRequests)
  {
    _cherryPickRequests = cherryPickRequests;
  }

  /**
   * Set the abase testsets.
   * @param abaseTestsets the new abase testsets
   * @motivation for hibernate
   */
  private void setAbaseTestsets(Set<AbaseTestset> abaseTestsets)
  {
    _abaseTestsets = abaseTestsets;
  }

  /**
   * Set the publications.
   * @param publications the new publications
   * @motivation for hibernate
   */
  private void setPublications(Set<Publication> publications)
  {
    _publications = publications;
  }

  /**
   * Set the attached files.
   * @param attachedFiles the new attached files
   * @motivation for hibernate
   */
  private void setAttachedFiles(Set<AttachedFile> attachedFiles)
  {
    _attachedFiles = attachedFiles;
  }

  /**
   * Set the billing information.
   * @param billingInformation the new billing information
   * @motivation for hibernate
   */
  private void setBillingInformation(BillingInformation billingInformation)
  {
    _billingInformation = billingInformation;
  }

  /**
   * Set the funding supports.
   * @param fundingSupports the new funding supports
   * @motivation for hibernate
   */
  private void setFundingSupports(Set<FundingSupport> fundingSupports)
  {
    _fundingSupports = fundingSupports;
  }

  /**
   * Set the annotation types.
   * @param annotationTypes the new annotation types
   * @motivation for hibernate
   */
  private void setAnnotationTypes(SortedSet<AnnotationType> annotationTypes)
  {
    _annotationTypes = annotationTypes;
  }

  /**
   * Set the reagents.
   * @param reagents the new reagents
   * @motivation for hibernate
   */
  private void setReagents(Set<Reagent> reagents)
  {
    _reagents = reagents;
  }

  private void verifyNameIsUnique(String name)
  {
    for (AnnotationType at : getAnnotationTypes()) {
      if (at.getName().equals(name)) {
        throw new DuplicateEntityException(this, at);
      }
    }
  }

  /**
   * Get the set of libraries that this screen has been authorized to screen,
   * even if the library's screening status would otherwise not permit it.
   */
  @Transient
  public Set<Library> getLibrariesPermitted()
  {
    return Collections.emptySet();
  }

  public void setCellLine(CellLine _cellLine)
  {
    this._cellLine = _cellLine;
  }

  @ManyToOne
  @JoinColumn(name="cellLineId", nullable=true)
  @org.hibernate.annotations.ForeignKey(name="fk_screen_to_cell_line")
  @org.hibernate.annotations.Cascade(value={ org.hibernate.annotations.CascadeType.SAVE_UPDATE })
  @edu.harvard.med.screensaver.model.annotations.ToOne(unidirectional=true)  
  public CellLine getCellLine()
  {
    return _cellLine;
  }
  
  public void setTransfectionAgent(TransfectionAgent _transfectionAgent)
  {
    this._transfectionAgent = _transfectionAgent;
  }

  @ManyToOne
  @JoinColumn(name="transfectionAgentId", nullable=true)
  @org.hibernate.annotations.ForeignKey(name="fk_screen_to_transfection_agent")
  @org.hibernate.annotations.Cascade(value={ org.hibernate.annotations.CascadeType.SAVE_UPDATE })
  @edu.harvard.med.screensaver.model.annotations.ToOne(unidirectional=true)  
  public TransfectionAgent getTransfectionAgent()
  {
    return _transfectionAgent;
  }

  @Column(nullable=false)
  //@org.hibernate.annotations.Type(type="edu.harvard.med.screensaver.model.screens.ScreenDataSharingLevel$UserType")
  public ScreenDataSharingLevel getDataSharingLevel()
  {
    return _dataSharingLevel;
  }

  public void setDataSharingLevel(ScreenDataSharingLevel dataSharingLevel)
  {
    _dataSharingLevel = dataSharingLevel;
  }

  /**
   * The date on which a level 2 or 3 screen is to become level 1.
   */
  @Column
  @Type(type="edu.harvard.med.screensaver.db.usertypes.LocalDateType")
  public LocalDate getDataPrivacyExpirationDate()
  {
    return _dataPrivacyExpirationDate;
  }

  public void setDataPrivacyExpirationDate(LocalDate dataPrivacyExpirationDate)
  {
    if(dataPrivacyExpirationDate == null) {
      if(_maxAllowedDataPrivacyExpirationDate != null ||
        _minAllowedDataPrivacyExpirationDate != null) {
        throw new DataModelViolationException("null value not allowed for condition if(maxAllowedDataPrivacyExpirationDate != null || minAllowedDataPrivacyExpirationDate != null)");
      }
    }
  
    LocalDate min = _minAllowedDataPrivacyExpirationDate;
    LocalDate max = _maxAllowedDataPrivacyExpirationDate;
    if( max != null 
      && dataPrivacyExpirationDate.compareTo(max) > 0 )
    {
      dataPrivacyExpirationDate = max;
    }
    else if( min != null
      && dataPrivacyExpirationDate.compareTo(min) < 0 )
    {
      dataPrivacyExpirationDate = min;
    } 
    _dataPrivacyExpirationDate = dataPrivacyExpirationDate;
  }
  
  public void setMinAllowedDataPrivacyExpirationDate(LocalDate minAllowedDataPrivacyExpirationDate)
  {
    _minAllowedDataPrivacyExpirationDate = minAllowedDataPrivacyExpirationDate;
    if(minAllowedDataPrivacyExpirationDate == null) return;
    if( _dataPrivacyExpirationDate  == null 
      || _dataPrivacyExpirationDate.compareTo(minAllowedDataPrivacyExpirationDate) < 0 )
    {
      _dataPrivacyExpirationDate = minAllowedDataPrivacyExpirationDate;
    }
  }

  @Column
  @Type(type="edu.harvard.med.screensaver.db.usertypes.LocalDateType")
  public LocalDate getMinAllowedDataPrivacyExpirationDate()
  {
    return _minAllowedDataPrivacyExpirationDate;
  }

  public void setMaxAllowedDataPrivacyExpirationDate(LocalDate maxAllowedDataPrivacyExpirationDate)
  {
    _maxAllowedDataPrivacyExpirationDate = maxAllowedDataPrivacyExpirationDate;
    if(maxAllowedDataPrivacyExpirationDate == null) return;
    if( _dataPrivacyExpirationDate  == null 
      || _dataPrivacyExpirationDate.compareTo(maxAllowedDataPrivacyExpirationDate) > 0 )
    {
      _dataPrivacyExpirationDate = maxAllowedDataPrivacyExpirationDate;
    }
  }

  @Column
  @Type(type="edu.harvard.med.screensaver.db.usertypes.LocalDateType")
  public LocalDate getMaxAllowedDataPrivacyExpirationDate()
  {
    return _maxAllowedDataPrivacyExpirationDate;
  }
  
  /**
   * Call this to set the {@link Screen#getDataPrivacyExpirationDate()} after the 
   * {@link Screen#getMinAllowedDataPrivacyExpirationDate()} and the {@link Screen#getMaxAllowedDataPrivacyExpirationDate()}
   * have been set.<br>
   */
  private void updateDataPrivacyExpirationDate()
  {
    LocalDate requestedDate = getDataPrivacyExpirationDate();
    LocalDate max = getMaxAllowedDataPrivacyExpirationDate();
    LocalDate min = getMinAllowedDataPrivacyExpirationDate();
        
    if( requestedDate.compareTo(max) > 0 & max != null )
    {
      _dataPrivacyExpirationDate = max;
    }
    else if(min != null
      && requestedDate.compareTo(min) < 0 )
    {
      _dataPrivacyExpirationDate = min;
    }else {
      _dataPrivacyExpirationDate = requestedDate;
    }
  }

  public void setDataPrivacyExpirationNotifiedDate(LocalDate dataPrivacyExpirationNotifiedDate)
  {
    _dataPrivacyExpirationNotifiedDate = dataPrivacyExpirationNotifiedDate;
  }

  /**
   * The date at which a dataPrivacyExpiration email was sent to Screensaver
   * Users associated with this Screen.
   */
  @Column
  @Type(type="edu.harvard.med.screensaver.db.usertypes.LocalDateType")
  public LocalDate getDataPrivacyExpirationNotifiedDate()
  {
    return _dataPrivacyExpirationNotifiedDate;
  }

  @Column(nullable = true)
  @Type(type = "edu.harvard.med.screensaver.db.usertypes.LocalDateType")
  public LocalDate getPubchemDepositedDate()
  {
    return _pubchemDepositedDate;
  }

  public void setPubchemDepositedDate(LocalDate pubchemDepositedDate)
  {
    _pubchemDepositedDate = pubchemDepositedDate;
  }

  @Column(nullable = true)
  public Integer getPubchemAssayId()
  {
    return _pubchemAssayId;
  }

  public void setPubchemAssayId(Integer pubchemAssayId)
  {
    _pubchemAssayId = pubchemAssayId;
  }

  /**
   * Specify the specific compound studied, if applicable
   * for [#3155] Create a linkable field for "compound studied" on the study
   */
  public void setWellStudied(Well wellStudied)
  {
    this.wellStudied = wellStudied;
  }

  @ManyToOne(fetch=FetchType.LAZY)
  @JoinColumn(name="wellStudiedId", nullable=true, updatable=true)
  @org.hibernate.annotations.ForeignKey(name="fk_screen_to_well_studied")
  @org.hibernate.annotations.LazyToOne(value=org.hibernate.annotations.LazyToOneOption.PROXY)
  public Well getWellStudied()
  {
    return wellStudied;
  }

  @Column(precision=ScreensaverConstants.MOLAR_CONCENTRATION_PRECISION, scale=ScreensaverConstants.MOLAR_CONCENTRATION_SCALE)
  @org.hibernate.annotations.Type(type = "edu.harvard.med.screensaver.db.usertypes.MolarConcentrationType")
  public MolarConcentration getPerturbagenMolarConcentration()
  {
    return perturbagenMolarConcentration;
  }
  
  public void setPerturbagenMolarConcentration(MolarConcentration value)
  {
    this.perturbagenMolarConcentration = value;
  }
  
    public void setPerturbagenUgMlConcentration(BigDecimal ugMlConcentration)
  {
    this.perturbagenUgMlConcentration = ugMlConcentration;
  }

  @Column(precision = ScreensaverConstants.UG_ML_CONCENTRATION_PRECISION, scale = ScreensaverConstants.UG_ML_CONCENTRATION_SCALE)
  public BigDecimal getPerturbagenUgMlConcentration()
  {
    return this.perturbagenUgMlConcentration;
  }
}