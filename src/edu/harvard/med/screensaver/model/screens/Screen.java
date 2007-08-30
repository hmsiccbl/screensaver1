// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.DerivedEntityProperty;
import edu.harvard.med.screensaver.model.ImmutableProperty;
import edu.harvard.med.screensaver.model.ToManyRelationship;
import edu.harvard.med.screensaver.model.ToOneRelationship;
import edu.harvard.med.screensaver.model.screenresults.Annotation;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.ui.util.ScreensaverUserComparator;
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.log4j.Logger;


/**
 * A Hibernate entity bean representing a screen.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @hibernate.class lazy="false"
 */
public class Screen extends AbstractEntity
{

  // static fields

  private static final Logger log = Logger.getLogger(Screen.class);
  private static final long serialVersionUID = 0L;


  // instance fields

  // study (provides annotation of library contents)

  //private Integer _studyId;
  private Integer _screenId;
  private Integer _version;
  private String _title;
  private Date _dateCreated;
  private ScreeningRoomUser _leadScreener; // should rename
  private ScreeningRoomUser _labHead;
  private Set<ScreeningRoomUser> _collaborators = new HashSet<ScreeningRoomUser>();
  private Set<Publication> _publications = new HashSet<Publication>();
  private String _summary;
  private String _comments;
  private Set<Annotation> _annotations = new HashSet<Annotation>();
  private StudyType _studyType;

  // generic screen

  private Integer _screenNumber;
  private ScreenType _screenType;
  private Set<AttachedFile> _attachedFiles = new HashSet<AttachedFile>();
  private Set<String> _keywords = new HashSet<String>();
  private String _publishableProtocol;
  private String _publishableProtocolComments;
  private ScreenResult _screenResult;

  // iccb screen

  private Set<StatusItem> _statusItems = new HashSet<StatusItem>();
  private transient SortedSet<StatusItem> _sortedStatusItems;
  private int _allTimeScreeningRoomActivityCount = 0;
  private Set<ScreeningRoomActivity> _screeningRoomActivities = new HashSet<ScreeningRoomActivity>();
  private Date _dataMeetingScheduled;
  private Date _dataMeetingComplete;
  private BillingInformation _billingInformation;
  private Set<FundingSupport> _fundingSupports = new HashSet<FundingSupport>();
  private Date _dateOfApplication;
  private Set<AbaseTestset> _abaseTestsets = new HashSet<AbaseTestset>();
  private String _abaseStudyId;
  private String _abaseProtocolId;
  private Set<LetterOfSupport> _lettersOfSupport = new HashSet<LetterOfSupport>();
  private Date _publishableProtocolDateEntered;
  private String _publishableProtocolEnteredBy;
  private Set<CherryPickRequest> _cherryPickRequests = new HashSet<CherryPickRequest>();
  private int _allTimeCherryPickRequestCount = 0;

  // public constructor

  /**
   * Constructs an initialized <code>Screen</code> object.
   *
   * @param leadScreener the lead screener
   * @param labHead the lab head
   * @param screenNumber the screen number
   * @param dateCreated the date created
   * @param screenType the screen type
   * @param title the title
   */
  public Screen(
    ScreeningRoomUser leadScreener,
    ScreeningRoomUser labHead,
    Integer screenNumber,
    Date dateCreated,
    ScreenType screenType,
    String title,
    Date dataMeetingScheduled,
    Date dataMeetingComplete,
    String summary,
    String comments,
    String abaseStudyId,
    String abaseProtocolId)
  {
    this(
      leadScreener,
      labHead,
      screenNumber,
      dateCreated,
      screenType,
      title);
    _dataMeetingScheduled = truncateDate(dataMeetingScheduled);
    _dataMeetingComplete = truncateDate(dataMeetingComplete);
    _summary = summary;
    _comments = comments;
    _abaseStudyId = abaseStudyId;
    _abaseProtocolId = abaseProtocolId;
    _studyType = StudyType.IN_VITRO;
  }

  /**
   * Constructs an initialized <code>Screen</code> object.
   *
   * @param leadScreener the lead screener
   * @param labHead the lab head
   * @param screenNumber the screen number
   * @param dateCreated the date created
   * @param screenType the screen type
   * @param title the title
   */
  public Screen(
    ScreeningRoomUser leadScreener,
    ScreeningRoomUser labHead,
    Integer screenNumber,
    Date dateCreated,
    ScreenType screenType,
    String title)
  {
    this(leadScreener,
         labHead,
         screenNumber,
         dateCreated,
         screenType,
         StudyType.IN_VITRO,
         title);
  }

  public Screen(ScreeningRoomUser leadScreener,
                ScreeningRoomUser labHead,
                Integer screenNumber,
                Date dateCreated,
                ScreenType screenType,
                StudyType studyType,
                String title)

  {
    if (leadScreener == null || labHead == null) {
      throw new NullPointerException();
    }
    _leadScreener = leadScreener;
    _labHead = labHead;
    _screenNumber = screenNumber;
    _dateCreated = truncateDate(dateCreated);
    _screenType = screenType;
    _title = title;
    _leadScreener.getHbnScreensLed().add(this);
    _labHead.getHbnScreensHeaded().add(this);
    _studyType = studyType;
  }


  // public methods

  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  @Override
  public Integer getEntityId()
  {
    return getScreenId();
  }

  /**
   * Get the id for the screen.
   *
   * @return the id for the screen
   * @hibernate.id generator-class="sequence"
   * @hibernate.generator-param name="sequence" value="screen_id_seq"
   */
  public Integer getScreenId()
  {
    return _screenId;
  }

  /**
   * Get the lead screener.
   *
   * @return the lead screener
   */
  @ToOneRelationship(nullable=false, inverseProperty="screensLed")
  public ScreeningRoomUser getLeadScreener()
  {
    return _leadScreener;
  }

  /**
   * Set the lead screener.
   *
   * @param leadScreener the new lead screener
   */
  public void setLeadScreener(ScreeningRoomUser leadScreener)
  {
    if (leadScreener == null) {
      throw new NullPointerException();
    }
    _leadScreener.getHbnScreensLed().remove(this);
    _leadScreener = leadScreener;
    _leadScreener.getHbnScreensLed().add(this);
  }

  /**
   * Get the lab head.
   *
   * @return the lab head
   */
  @ToOneRelationship(nullable=false, inverseProperty="screensHeaded")
  public ScreeningRoomUser getLabHead()
  {
    return _labHead;
  }

  /**
   * Set the lab head.
   *
   * @param labHead the new lab head
   */
  public void setLabHead(ScreeningRoomUser labHead)
  {
    if (labHead == null) {
      throw new NullPointerException();
    }
    _labHead.getHbnScreensHeaded().remove(this);
    _labHead = labHead;
    _labHead.getHbnScreensHeaded().add(this);
  }

  /**
   * Get an unmodifiable copy of the set of collaborators.
   *
   * @return the collaborators
   */
  @ToManyRelationship(inverseProperty="screensCollaborated")
  public Set<ScreeningRoomUser> getCollaborators()
  {
    return Collections.unmodifiableSet(_collaborators);
  }

  /**
   * @motivation JSF EL binding
   */
  @SuppressWarnings("unchecked")
  @DerivedEntityProperty
  public List<ScreeningRoomUser> getCollaboratorsList()
  {
    List collaboratorsList = new ArrayList<ScreeningRoomUser>(_collaborators);
    Collections.sort(collaboratorsList, ScreensaverUserComparator.getInstance());
    return Collections.unmodifiableList(collaboratorsList);
  }

  /**
   * Get a comma-delimited list of the Screen's collaborators, identified by
   * their full name ("first, last" format).
   */
  @DerivedEntityProperty
  public String getCollaboratorsString()
  {
    StringBuilder collaborators = new StringBuilder();
    boolean first = true;
    for (ScreeningRoomUser collaborator : getCollaboratorsList()) {
      if (first) {
        first = false;
      }
      else {
        collaborators.append(", ");
      }
      collaborators.append(collaborator.getFullNameFirstLast());
    }
    return collaborators.toString();
  }

  /**
   * @motivation JSF EL binding
   * @param collaborators
   */
  public void setCollaboratorsList(List<ScreeningRoomUser> collaborators)
  {
    List<ScreeningRoomUser> collaboratorsToRemove = new ArrayList<ScreeningRoomUser>(getHbnCollaborators());
    for (ScreeningRoomUser collaborator : collaboratorsToRemove) {
      removeCollaborator(collaborator);
    }
    for (ScreeningRoomUser collaborator : collaborators) {
      addCollaborator(collaborator);
    }
  }

  /**
   * Add the collaborator.
   *
   * @param collaborator the collaborator to add
   * @return true iff the screen did not already have the collaborator
   */
  public boolean addCollaborator(ScreeningRoomUser collaborator)
  {
    if (getHbnCollaborators().add(collaborator)) {
      return collaborator.getHbnScreensCollaborated().add(this);
    }
    return false;
  }

  /**
   * Remove the collaborator.
   *
   * @param collaborator the collaborator to remove
   * @return true iff the screen previously had the collaborator
   */
  public boolean removeCollaborator(ScreeningRoomUser collaborator)
  {
    if (getHbnCollaborators().remove(collaborator)) {
      return collaborator.getHbnScreensCollaborated().remove(this);
    }
    return false;
  }

  /**
   * Get the status items.
   *
   * @return the status items
   * @hibernate.set
   *   cascade="all-delete-orphan"
   *   lazy="true"
   *   inverse="true"
   * @hibernate.collection-key
   *   column="screen_id"
   * @hibernate.collection-one-to-many
   *   class="edu.harvard.med.screensaver.model.screens.StatusItem"
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  public Set<StatusItem> getStatusItems()
  {
    return _statusItems;
  }

  /**
   * Get the status items, sorted by their natural ordering.
   *
   * @return the status items, sorted by their natural ordering; null if there
   *         are no status items
   */
  @DerivedEntityProperty
  public SortedSet<StatusItem> getSortedStatusItems()
  {
    assert _sortedStatusItems != null;
    if (_sortedStatusItems == null) {
      _sortedStatusItems = new TreeSet<StatusItem>(getStatusItems());
    }
    return _sortedStatusItems;
  }

  /**
   * Get the screening room activities.
   *
   * @return the screening room activities
   * @hibernate.set
   *   cascade="all-delete-orphan"
   *   lazy="true"
   *   inverse="true"
   * @hibernate.collection-key
   *   column="screen_id"
   * @hibernate.collection-one-to-many
   *   class="edu.harvard.med.screensaver.model.screens.ScreeningRoomActivity"
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  @ToManyRelationship(inverseProperty="screen")
  public Set<ScreeningRoomActivity> getScreeningRoomActivities()
  {
    return _screeningRoomActivities;
  }

  @SuppressWarnings("unchecked")
  @DerivedEntityProperty
  public <E extends ScreeningRoomActivity> Set<E> getScreeningRoomActivitiesOfType(Class<E> clazz)
  {
    Set<E> result = new TreeSet<E>();
    for (ScreeningRoomActivity screeningRoomActivity : _screeningRoomActivities) {
      if (clazz.isAssignableFrom(screeningRoomActivity.getClass())) {
        result.add((E) screeningRoomActivity);
      }
    }
    return result;
  }

  /**
   * Get the allTimeScreeningRoomActivityCount.
   * @return the allTimeScreeningRoomActivityCount
   * @hibernate.property not-null="true"
   */
  public int getAllTimeScreeningRoomActivityCount()
  {
    return _allTimeScreeningRoomActivityCount;
  }

  /**
   * Set the allTimeScreeningRoomActivityCount.
   * @param allTimeScreeningRoomActivityCount the allTimeScreeningRoomActivityCount
   */
  public void setAllTimeScreeningRoomActivityCount(int allTimeScreeningRoomActivityCount)
  {
    _allTimeScreeningRoomActivityCount = allTimeScreeningRoomActivityCount;
  }

  /**
   * Get the cherry pick requests.
   *
   * @return the cherry pick requests
   * @hibernate.set
   *   cascade="all-delete-orphan"
   *   lazy="true"
   *   inverse="true"
   * @hibernate.collection-key
   *   column="screen_id"
   * @hibernate.collection-one-to-many
   *   class="edu.harvard.med.screensaver.model.screens.CherryPickRequest"
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  @ToManyRelationship(inverseProperty="screen")
  public Set<CherryPickRequest> getCherryPickRequests()
  {
    return _cherryPickRequests;
  }

  /**
   * Get the allTimeCherryPickRequestCount.
   * @return the allTimeCherryPickRequestCount
   * @hibernate.property not-null="true"
   */
  public int getAllTimeCherryPickRequestCount()
  {
    return _allTimeCherryPickRequestCount;
  }

  /**
   * Set the allTimeCherryPickRequestCount.
   * @param allTimeCherryPickRequestCount the allTimeCherryPickRequestCount
   */
  public void setAllTimeCherryPickRequestCount(int allTimeCherryPickRequestCount)
  {
    _allTimeCherryPickRequestCount = allTimeCherryPickRequestCount;
  }

  /**
   * Get the set of abase testsets.
   *
   * @return the abase testsets
   * @hibernate.set
   *   cascade="all-delete-orphan"
   *   lazy="true"
   *   inverse="true"
   * @hibernate.collection-key
   *   column="screen_id"
   * @hibernate.collection-one-to-many
   *   class="edu.harvard.med.screensaver.model.screens.AbaseTestset"
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  public Set<AbaseTestset> getAbaseTestsets()
  {
    return _abaseTestsets;
  }

  /**
   * Get the publications.
   *
   * @return the publications
   * @hibernate.set
   *   cascade="all-delete-orphan"
   *   lazy="true"
   *   inverse="true"
   * @hibernate.collection-key
   *   column="screen_id"
   * @hibernate.collection-one-to-many
   *   class="edu.harvard.med.screensaver.model.screens.Publication"
   */
  public Set<Publication> getPublications()
  {
    return _publications;
  }

  /**
   * Get the letters of support.
   *
   * @return the letters of support
   * @hibernate.set
   *   cascade="all-delete-orphan"
   *   lazy="true"
   *   inverse="true"
   * @hibernate.collection-key
   *   column="screen_id"
   * @hibernate.collection-one-to-many
   *   class="edu.harvard.med.screensaver.model.screens.LetterOfSupport"
   */
  @ToManyRelationship(inverseProperty="screen")
  public Set<LetterOfSupport> getLettersOfSupport()
  {
    return _lettersOfSupport;
  }

  /**
   * Get the attached files.
   *
   * @return the attached files
   * @hibernate.set
   *   cascade="all-delete-orphan"
   *   lazy="true"
   *   inverse="true"
   * @hibernate.collection-key
   *   column="screen_id"
   * @hibernate.collection-one-to-many
   *   class="edu.harvard.med.screensaver.model.screens.AttachedFile"
   */
  public Set<AttachedFile> getAttachedFiles()
  {
    return _attachedFiles;
  }

  /**
   * Get the billing information.
   *
   * @return the billing information
   * @hibernate.one-to-one
   *   class="edu.harvard.med.screensaver.model.screens.BillingInformation"
   *   property-ref="screen"
   *   cascade="save-update"
   */
  public BillingInformation getBillingInformation()
  {
    return _billingInformation;
  }

  /**
   * Get the screen number.
   *
   * @return the screen number
   */
  public Integer getScreenNumber()
  {
    return _screenNumber;
  }

  /**
   * Set the screen number.
   *
   * @param screenNumber the new screen number
   */
  public void setScreenNumber(Integer screenNumber)
  {
    _leadScreener.getHbnScreensLed().remove(this);
    _labHead.getHbnScreensHeaded().remove(this);
    for (ScreeningRoomUser collaborator : _collaborators) {
      collaborator.getHbnScreensCollaborated().remove(this);
    }
    _screenNumber = screenNumber;
    _leadScreener.getHbnScreensLed().add(this);
    _labHead.getHbnScreensHeaded().add(this);
    for (ScreeningRoomUser collaborator : _collaborators) {
      collaborator.getHbnScreensCollaborated().add(this);
    }
  }

  /**
   * Get the date created.
   *
   * @return the date created
   * @hibernate.property
   *   not-null="true"
   */
  public Date getDateCreated()
  {
    return _dateCreated;
  }

  /**
   * Set the date created.
   *
   * @param dateCreated the new date created
   */
  public void setDateCreated(Date dateCreated)
  {
    _dateCreated = truncateDate(dateCreated);
  }

  /**
   * Get the study type.
   *
   * @return the study type
   * @hibernate.property
   *   type="edu.harvard.med.screensaver.model.screens.StudyType$UserType"
   *   not-null="true"
   */
  @ImmutableProperty
  public StudyType getStudyType()
  {
    return _studyType;
  }

  /**
   * Get the screen type.
   *
   * @return the screen type
   * @hibernate.property
   *   type="edu.harvard.med.screensaver.model.screens.ScreenType$UserType"
   *   not-null="true"
   */
  @ImmutableProperty
  public ScreenType getScreenType()
  {
    return _screenType;
  }

  /**
   * Get the title.
   *
   * @return the title
   * @hibernate.property
   *   type="text"
   *   not-null="true"
   */
  public String getTitle()
  {
    return _title;
  }

  /**
   * Set the title.
   *
   * @param title the new title
   */
  public void setTitle(String title)
  {
    _title = title;
  }

  /**
   * Get the data meeting scheduled.
   *
   * @return the data meeting scheduled
   * @hibernate.property
   */
  public Date getDataMeetingScheduled()
  {
    return _dataMeetingScheduled;
  }

  /**
   * Set the data meeting scheduled.
   *
   * @param dataMeetingScheduled the new data meeting scheduled
   */
  public void setDataMeetingScheduled(Date dataMeetingScheduled)
  {
    _dataMeetingScheduled = truncateDate(dataMeetingScheduled);
  }

  /**
   * Get the data meeting complete.
   *
   * @return the data meeting complete
   * @hibernate.property
   */
  public Date getDataMeetingComplete()
  {
    return _dataMeetingComplete;
  }

  /**
   * Set the data meeting complete.
   *
   * @param dataMeetingComplete the new data meeting complete
   */
  public void setDataMeetingComplete(Date dataMeetingComplete)
  {
    _dataMeetingComplete = truncateDate(dataMeetingComplete);
  }

  /**
   * Get the keywords.
   *
   * @return the keywords
   * @hibernate.set
   *   order-by="keyword"
   *   table="screen_keyword"
   *   cascade="delete"
   *   lazy="true"
   * @hibernate.collection-key
   *   column="screen_id"
   *   foreign-key="fk_screen_keyword_to_screen"
   * @hibernate.collection-element
   *   type="text"
   *   column="keyword"
   *   not-null="true"
   */
  public Set<String> getKeywords()
  {
    return _keywords;
  }

  /**
   * Add the keyword.
   *
   * @param keyword the keyword to add
   * @return true iff the screen did not already have the keyword
   */
  public boolean addKeyword(String keyword)
  {
    return _keywords.add(keyword);
  }

  /**
   * Remove the keyword.
   *
   * @param keyword the keyword to remove
   * @return true iff the screen previously had the keyword
   */
  public boolean removeKeyword(String keyword)
  {
    return _keywords.remove(keyword);
  }

  /**
   * Get the funding supports.
   *
   * @return the funding supports
   * @hibernate.set
   *   order-by="funding_support"
   *   table="screen_funding_support"
   *   cascade="delete-orphan"
   *   lazy="true"
   * @hibernate.collection-key
   *   column="screen_id"
   *   foreign-key="fk_screen_funding_support_to_screen"
   * @hibernate.collection-element
   *   type="edu.harvard.med.screensaver.model.screens.FundingSupport$UserType"
   *   column="funding_support"
   *   not-null="true"
   */
  public Set<FundingSupport> getFundingSupports()
  {
    return _fundingSupports;
  }

  /**
   * Add the funding support.
   *
   * @param fundingSupport the funding suppor to add
   * @return true iff the screen did not already have the funding support
   */
  public boolean addFundingSupport(FundingSupport fundingSupport)
  {
    return _fundingSupports.add(fundingSupport);
  }

  /**
   * Remove the funding support.
   *
   * @param fundingSupport the funding support to remove
   * @return true iff the screen previously had the funding support
   */
  public boolean removeFundingSupport(FundingSupport fundingSupport)
  {
    // note: related entity will be deleted by Hibernate on flush
    return _fundingSupports.remove(fundingSupport);
  }

  /**
   * Get the summary.
   *
   * @return the summary
   * @hibernate.property
   *   type="text"
   */
  public String getSummary()
  {
    return _summary;
  }

  /**
   * Set the summary.
   *
   * @param summary the new summary
   */
  public void setSummary(String summary)
  {
    _summary = summary;
  }

  /**
   * Get the comments.
   *
   * @return the comments
   * @hibernate.property
   *   type="text"
   */
  public String getComments()
  {
    return _comments;
  }

  /**
   * Set the comments.
   *
   * @param comments the new comments
   */
  public void setComments(String comments)
  {
    _comments = comments;
  }

  /**
   * Get the abase study id.
   *
   * @return the abase study id
   * @hibernate.property
   *   type="text"
   */
  public String getAbaseStudyId()
  {
    return _abaseStudyId;
  }

  /**
   * Set the abase study id.
   *
   * @param abaseStudyId the new abase study id
   */
  public void setAbaseStudyId(String abaseStudyId)
  {
    _abaseStudyId = abaseStudyId;
  }

  /**
   * Get the abase protocol id.
   *
   * @return the abase protocol id
   * @hibernate.property
   *   type="text"
   */
  public String getAbaseProtocolId()
  {
    return _abaseProtocolId;
  }

  /**
   * Set the abase protocol id.
   *
   * @param abaseProtocolId the new abase protocol id
   */
  public void setAbaseProtocolId(String abaseProtocolId)
  {
    _abaseProtocolId = abaseProtocolId;
  }

  /**
   * Get the assay readout types.
   *
   * @return the assay readout types
   */
  @DerivedEntityProperty
  public Set<AssayReadoutType> getAssayReadoutTypes()
  {
    Set<AssayReadoutType> assayReadoutTypes = new HashSet<AssayReadoutType>();
    if (getScreenResult() != null) {
      for (ResultValueType rvt : getScreenResult().getResultValueTypes()) {
        if (rvt.getAssayReadoutType() != null) {
          assayReadoutTypes.add(rvt.getAssayReadoutType());
        }
      }
    }
    return assayReadoutTypes;
  }

  /**
   * Get the assay readout types, as a formatted, comma-delimited string
   *
   * @return the assay readout types, as a formatted, comma-delimited string
   */
  @DerivedEntityProperty
  public String getAssayReadoutTypesString()
  {
    if (getScreenResult() != null) {
      return StringUtils.makeListString(getAssayReadoutTypes(), ", ");
    }
    return "";
  }

  // TODO: extract PublishableProtocol value-typed collection

  /**
   * Get the date the publishable protocol was entered.
   * @return the date the publishable protocol was entered
   * @hibernate.property
   */
  public Date getPublishableProtocolDateEntered()
  {
    return _publishableProtocolDateEntered;
  }

  /**
   * Set the date the publishable protocol was entered.
   * @param publishableProtocolDateEntered the new date the publishable protocol was entered
   */
  public void setPublishableProtocolDateEntered(
    Date publishableProtocolDateEntered)
  {
    _publishableProtocolDateEntered = publishableProtocolDateEntered;
  }

  /**
   * Get the initials of the administrator who entered the publishable protocol.
   * @return the initials of the administrator who entered the publishable protocol
   * @hibernate.property type="text"
   */
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
   *
   * @return the publishable protocol
   * @hibernate.property
   *   type="text"
   */
  public String getPublishableProtocol()
  {
    return _publishableProtocol;
  }

  /**
   * Set the publishable protocol.
   *
   * @param publishableProtocol the new publishable protocol
   */
  public void setPublishableProtocol(String publishableProtocol)
  {
    _publishableProtocol = publishableProtocol;
  }

  /**
   * Get the publishable protocol comments.
   *
   * @return the publishable protocol comments
   * @hibernate.property
   *   type="text"
   */
  public String getPublishableProtocolComments()
  {
    return _publishableProtocolComments;
  }

  /**
   * Set the publishable protocol comments.
   *
   * @param publishableProtocolComments the new publishable protocol comments
   */
  public void setPublishableProtocolComments(String publishableProtocolComments)
  {
    _publishableProtocolComments = publishableProtocolComments;
  }

  /**
   * Get the date of application.
   *
   * @return the date of application
   * @hibernate.property
   */
  public Date getDateOfApplication()
  {
    return _dateOfApplication;
  }

  /**
   * Set the date of application.
   *
   * @param dateOfApplication the new date of application
   */
  public void setDateOfApplication(Date dateOfApplication)
  {
    _dateOfApplication = truncateDate(dateOfApplication);
  }

  /**
   * Get the annotations generated by this screen.
   *
   * @return the annotations
   * @hibernate.set
   *   cascade="all-delete-orphan"
   *   lazy="true"
   *   inverse="true"
   * @hibernate.collection-key
   *   column="study_id"
   * @hibernate.collection-one-to-many
   *   class="edu.harvard.med.screensaver.model.screenresults.Annotation"
   */
  public Set<Annotation> getAnnotations()
  {
    return _annotations;
  }

  /**
   * Get the set of screen result.
   *
   * @return the screen result
   * @hibernate.one-to-one
   *   class="edu.harvard.med.screensaver.model.screenresults.ScreenResult"
   *   property-ref="screen"
   *   cascade="save-update"
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  public ScreenResult getScreenResult()
  {
    return _screenResult;
  }

  public void setScreenResult(ScreenResult screenResult)
  {
    _screenResult = screenResult;
  }

  /**
   * Set the annotations.
   *
   * @param annotations the new annotations
   * @motivation for hibernate
   */
  private void setAnnotations(Set<Annotation> annotations)
  {
    _annotations = annotations;
  }

  /**
   * A factory method for creating an appropriately typed cherry pick request
   * for this screen.
   *
   * @return
   */
  public CherryPickRequest createCherryPickRequest()
  {
    if (getScreenType().equals(ScreenType.RNAI)) {
      return new RNAiCherryPickRequest(this, getLeadScreener(), new Date());
    }
    else if(getScreenType().equals(ScreenType.SMALL_MOLECULE)) {
      return new CompoundCherryPickRequest(this, getLeadScreener(), new Date());
    }
    log.error("screen of type " + getScreenType() + " does not support cherry pick requests");
    return null;
  }


  // protected methods

  /**
   * Set the lead screener.
   * Throw a NullPointerException when the lead screener is null.
   *
   * @param leadScreener the new lead screener
   * @throws NullPointerException when the lead screener is null
   * @motivation for hibernate and maintenance of bi-directional relationships
   * this method is public only because the bi-directional relationship
   * is cross-package.
   */
  public void setHbnLeadScreener(ScreeningRoomUser leadScreener)
  {
    if (leadScreener == null) {
      throw new NullPointerException();
    }
    _leadScreener = leadScreener;
  }

  /**
   * Set the lab head.
   * Throw a NullPointerException when the lab head is null.
   *
   * @param labHead the new lab head
   * @throws NullPointerException when the lab head is null
   * @motivation for hibernate and maintenance of bi-directional relationships
   * this method is public only because the bi-directional relationship
   * is cross-package.
   */
  public void setHbnLabHead(ScreeningRoomUser labHead)
  {
    if (labHead == null) {
      throw new NullPointerException();
    }
    _labHead = labHead;
  }

  /**
   * Get the collaborators.
   *
   * @return the collaborators
   * @hibernate.set
   *   table="collaborator_link"
   *   lazy="true"
   *   cascade="all"
   * @hibernate.collection-key
   *   column="screen_id"
   * @hibernate.collection-many-to-many
   *   column="collaborator_id"
   *   class="edu.harvard.med.screensaver.model.users.ScreeningRoomUser"
   *   foreign-key="fk_collaborator_link_to_screen"
   * @motivation for hibernate and maintenance of bi-directional relationships
   * this method is public only because the bi-directional relationship
   * is cross-package.
   */
  public Set<ScreeningRoomUser> getHbnCollaborators()
  {
    return _collaborators;
  }

  // protected methods

  @Override
  protected Object getBusinessKey()
  {
    return getScreenNumber();
  }

  /**
   * Set the billing information.
   *
   * @param billingInformation the new billing information
   * @motivation for hibernate
   */
  void setBillingInformation(BillingInformation billingInformation)
  {
    _billingInformation = billingInformation;
  }


  // private constructor

  /**
   * Construct an uninitialized <code>Screen</code> object.
   *
   * @motivation for hibernate
   */
  private Screen() {}


  // private methods

  /**
   * Set the id for the screen.
   *
   * @param screenId the new id for the screen
   * @motivation for hibernate
   */
  private void setScreenId(Integer screenId) {
    _screenId = screenId;
  }

  /**
   * Get the version for the screen.
   *
   * @return the version for the screen
   * @motivation for hibernate
   * @hibernate.version
   */
  private Integer getVersion() {
    return _version;
  }

  /**
   * Set the version for the screen.
   *
   * @param version the new version for the screen
   * @motivation for hibernate
   */
  private void setVersion(Integer version) {
    _version = version;
  }

  /**
   * Set the study type.
   *
   * @param studyType the new studyType
   */
  private void setStudyType(StudyType studyType)
  {
    _studyType = studyType;
  }

  /**
   * Set the screen type.
   *
   * @param screenType the new screen type
   */
  private void setScreenType(ScreenType screenType)
  {
    _screenType = screenType;
  }

  /**
   * Get the lead screener.
   *
   * @return the lead screener
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.model.users.ScreeningRoomUser"
   *   column="lead_screener_id"
   *   not-null="true"
   *   foreign-key="fk_screen_to_lead_screener"
   *   cascade="save-update"
   *   lazy="no-proxy"
   * @motivation for hibernate
   */
  private ScreeningRoomUser getHbnLeadScreener()
  {
    return _leadScreener;
  }

  /**
   * Get the lab head.
   *
   * @return the lab head
   * @hibernate.many-to-one
   *   class="edu.harvard.med.screensaver.model.users.ScreeningRoomUser"
   *   column="lab_head_id"
   *   not-null="true"
   *   foreign-key="fk_screen_to_lab_head"
   *   cascade="save-update"
   *   lazy="no-proxy"
   * @motivation for hibernate
   */
  private ScreeningRoomUser getHbnLabHead()
  {
    return _labHead;
  }

  /**
   * Set the collaborators.
   *
   * @param collaborators the new collaborators
   * @motivation for hibernate
   */
  private void setHbnCollaborators(Set<ScreeningRoomUser> collaborators)
  {
    _collaborators = collaborators;
  }

  /**
   * Set the status items.
   *
   * @param statusItems the new status items
   * @motivation for hibernate
   */
  private void setStatusItems(Set<StatusItem> statusItems)
  {
    _statusItems = statusItems;
  }

  /**
   * Set the screening room activities.
   *
   * @param screeningRoomActivities the new screening room activities
   * @motivation for hibernate
   */
  private void setScreeningRoomActivities(Set<ScreeningRoomActivity> screeningRoomActivities)
  {
    _screeningRoomActivities = screeningRoomActivities;
  }

  /**
   * Set the cherry pick requests.
   *
   * @param cherryPickRequests the new cherry pick requests
   * @motivation for hibernate
   */
  private void setCherryPickRequests(Set<CherryPickRequest> cherryPickRequests)
  {
    _cherryPickRequests = cherryPickRequests;
  }

  /**
   * Set the abase testsets.
   *
   * @param abaseTestsets the new abase testsets
   * @motivation for hibernate
   */
  private void setAbaseTestsets(Set<AbaseTestset> abaseTestsets)
  {
    _abaseTestsets = abaseTestsets;
  }

  /**
   * Set the publications.
   *
   * @param publications the new publications
   * @motivation for hibernate
   */
  private void setPublications(Set<Publication> publications)
  {
    _publications = publications;
  }

  /**
   * Set the letters of support.
   *
   * @param lettersOfSupport the new letters of support
   * @motivation for hibernate
   */
  private void setLettersOfSupport(Set<LetterOfSupport> lettersOfSupport)
  {
    _lettersOfSupport = lettersOfSupport;
  }

  /**
   * Set the attached files.
   *
   * @param attachedFiles the new attached files
   * @motivation for hibernate
   */
  private void setAttachedFiles(Set<AttachedFile> attachedFiles)
  {
    _attachedFiles = attachedFiles;
  }

  /**
   * Set the keywords.
   *
   * @param keywords the new keywords
   * @motivation for hibernate
   */
  private void setKeywords(Set<String> keywords)
  {
    _keywords = keywords;
  }

  /**
   * Set the funding supports.
   *
   * @param fundingSupports the new funding supports
   * @motivation for hibernate
   */
  private void setFundingSupports(Set<FundingSupport> fundingSupports)
  {
    _fundingSupports = fundingSupports;
  }

  /**
   * Get the screen number.
   *
   * @return the screen number
   * @hibernate.property
   *   column="screen_number"
   *   not-null="true"
   * @motivation for hibernate
   */
  private Integer getHbnScreenNumber()
  {
    return _screenNumber;
  }

  /**
   * Set the screen number.
   *
   * @param screenNumber the new screen number
   * @motivation for hibernate
   */
  private void setHbnScreenNumber(Integer screenNumber)
  {
    _screenNumber = screenNumber;
  }
}
