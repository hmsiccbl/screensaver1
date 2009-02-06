// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/ui/screens/ScreenViewer.java $
// $Id: ScreenViewer.java 2304 2008-04-14 13:31:27Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.activities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.Activity;
import edu.harvard.med.screensaver.model.Concentration;
import edu.harvard.med.screensaver.model.ConcentrationUnit;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.VolumeUnit;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.screens.AssayProtocolType;
import edu.harvard.med.screensaver.model.screens.LabActivity;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.PlatesUsed;
import edu.harvard.med.screensaver.model.screens.RNAiCherryPickScreening;
import edu.harvard.med.screensaver.model.screens.Screening;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.AbstractEditableBackingBean;
import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.searchresults.LabActivitySearchResults;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;
import edu.harvard.med.screensaver.ui.util.UISelectOneEntityBean;
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;

public class ActivityViewer extends AbstractEditableBackingBean
{
  // static members

  private static Logger log = Logger.getLogger(ActivityViewer.class);

  // instance data members

  private ActivityViewer _thisProxy;
  private GenericEntityDAO _dao;
  private LibrariesDAO _librariesDao;

  private Activity _activity;
  private boolean _isEditMode;
  private UISelectOneEntityBean<ScreensaverUser> _performedBy;
  private UISelectOneBean<AssayProtocolType> _assayProtocolType;
  private UISelectOneBean<ConcentrationUnit> _concentrationType;
  private UISelectOneBean<VolumeUnit> _volumeType;
  private String _concentrationValue;
  private String _volumeValue;
  private DataModel _libraryAndPlatesScreenedDataModel;
  private PlatesUsed _newPlatesScreened;
  private AbstractBackingBean _returnToViewAfterEdit;
  private LabActivitySearchResults _labActivitiesBrowser;


  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected ActivityViewer()
  {
  }

  public ActivityViewer(ActivityViewer _activityViewerProxy,
                        GenericEntityDAO dao,
                        LibrariesDAO librariesDao,
                        LabActivitySearchResults labActivitiesBrowser)
  {
    super(ScreensaverUserRole.SCREENS_ADMIN);
    _thisProxy = _activityViewerProxy;
    _returnToViewAfterEdit = _thisProxy;
    _dao = dao;
    _librariesDao = librariesDao;
    _labActivitiesBrowser = labActivitiesBrowser;
  }


  // public methods

  public AbstractEntity getEntity()
  {
    return getActivity();
  }

  public Activity getActivity()
  {
    return _activity;
  }

  @Transactional
  public void setActivity(Activity activity)
  {
    if (activity.getEntityId() != null) {
      activity = _dao.reloadEntity(activity,
                                   true,
                                   "screen.labHead",
                                   "screen.leadScreener",
                                   "screen.collaborators",
                                   "performedBy");
      // HACK: performedBy is not being eager fetched when this method is called
      // from viewActivity(); chalking this up to a Hibernate bug for now, since
      // the relationships *are* being eager fetched; problem manifests in
      // ActivityViewer.getPerformedBy() method
      activity.getPerformedBy().getEntityId();

      if (activity instanceof LibraryScreening) {
        _dao.need(activity, "platesUsed");
      }
      if (activity instanceof RNAiCherryPickScreening) {
        _dao.need(activity, "rnaiCherryPickRequest");
      }
    }
    _activity = activity;

    setConcentrationType( _activity instanceof LabActivity ? ((LabActivity) _activity).getConcentrationUnits() : null);
    setConcentrationValue( _activity instanceof LabActivity ? ((LabActivity) _activity).getConcentrationValue() : null );
    resetView();
  }

  public UISelectOneBean<ScreensaverUser> getPerformedBy()
  {
    if (_performedBy == null) {
      Set<ScreensaverUser> performedByCandidates = _activity.getPerformedByCandidates();
      if (performedByCandidates == null) {
        performedByCandidates = Sets.newTreeSet();
        performedByCandidates.addAll(_dao.findAllEntitiesOfType(ScreensaverUser.class));
      }
      _performedBy = new UISelectOneEntityBean<ScreensaverUser>(
        performedByCandidates,
        _activity.getPerformedBy(),
        _dao) {
        @Override
        protected String makeLabel(ScreensaverUser t) { return t.getFullNameLastFirst(); }
      };
    }
    return _performedBy;
  }

  public UISelectOneBean<VolumeUnit> getVolumeTransferredPerWellType()
  {
    try {
      if (_volumeType == null) {
        Volume v = (_activity instanceof LabActivity ?
                                    ((LabActivity) _activity).getVolumeTransferredPerWell() :
                                    null );
        VolumeUnit unit = ( v == null ? null: v.getUnits());

        _volumeType = new UISelectOneBean<VolumeUnit>( Arrays.asList(VolumeUnit.DISPLAY_VALUES), unit )
          {
            @Override
            protected String makeLabel(VolumeUnit t)
            {
              return t.getValue();
            }
          };
      }
      return _volumeType;
    } catch (Exception e) {
      log.error("err: " + e);
      return null;
    }
  }

  /**
   * This method exists to grab the value portion of the Quantity stored
  */
  public String getVolumeTransferredPerWellValue()
  {
    if( _volumeValue == null )
    {
      _volumeValue =
        _activity instanceof LabActivity ? ((LabActivity) _activity).getVolumeTransferredPerWellValue(): null;
    }
    return _volumeValue;
  }
  /**
   * This method exists to set the value portion of the Quantity stored
   * @see #save()
  */
  public void setVolumeTransferredPerWellValue( String value )
  {
    _volumeValue = value;
  }

  public Concentration getConcentration()
  {
    return _activity instanceof LabActivity ? ((LabActivity) _activity).getConcentration(): null;
  }


  public UISelectOneBean<ConcentrationUnit> getConcentrationType()
  {
    try {
      if (_concentrationType == null) {
        setConcentrationType( _activity instanceof LabActivity ? ((LabActivity) _activity).getConcentrationUnits() : null);
      }
      return _concentrationType;
    } catch (Exception e) {
      log.error("err: " + e);
      return null;
    }
  }

  private void setConcentrationType( ConcentrationUnit unit )
  {
    _concentrationType =
      new UISelectOneBean<ConcentrationUnit>(Arrays.asList(ConcentrationUnit.DISPLAY_VALUES),unit)
      {
        @Override
        protected String makeLabel(ConcentrationUnit t) { return t.getValue(); }
      };
  }

  /**
   * This method exists to grab the value portion of the Quantity stored
  */
  public String getConcentrationValue()
  {
    if( _concentrationValue == null )
    {
      _concentrationValue =
        _activity instanceof LabActivity ? ((LabActivity) _activity).getConcentrationValue(): null;
    }
    return _concentrationValue;
  }

  /**
   * This method exists to set the value portion of the Quantity stored
   * @see #save()
  */
  public void setConcentrationValue( String value )
  {
    _concentrationValue = value;
  }

  public UISelectOneBean<AssayProtocolType> getAssayProtocolType()
  {
    if (_assayProtocolType == null) {
      _assayProtocolType = new UISelectOneBean<AssayProtocolType>(Arrays.asList(AssayProtocolType.values()),
        _activity instanceof Screening ? ((Screening) _activity).getAssayProtocolType() : null) {
        @Override
        protected String makeLabel(AssayProtocolType t) { return t.getValue(); }
      };
    }
    return _assayProtocolType;
  }

  public PlatesUsed getNewPlatesScreened()
  {
    if (_newPlatesScreened == null) {
      _newPlatesScreened = new PlatesUsed();
    }
    return _newPlatesScreened;
  }

  @Override
  public String reload()
  {
    if (_activity == null || _activity.getEntityId() == null) {
      _activity = null;
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    return _thisProxy.viewActivity(_activity);
  }

  public boolean isEditMode()
  {
    return _isEditMode;
  }

  @UIControllerMethod
  public String edit()
  {
    _isEditMode = true;
    _returnToViewAfterEdit = _thisProxy;
    return VIEW_ACTIVITY;
  }

  @UIControllerMethod
  public String cancel()
  {
    _isEditMode = false;
    return _returnToViewAfterEdit.reload();
  }

  @UIControllerMethod
  @Transactional
  public String save()
  {
    boolean valid = true;
    if( !saveConcentration() ) valid = false;
    if( !saveVolumeTransferredPerWell()) valid = false;
    if(! valid )
    {
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    _isEditMode = false;

    _dao.saveOrUpdateEntity(getActivity());
    _dao.flush();
    getActivity().setPerformedBy(getPerformedBy().getSelection());
    // TODO _labActivitiesBrowser.refetch();
    return _returnToViewAfterEdit.reload();
  }

  private boolean saveConcentration()
  {
    try {
      Concentration c = null;
      if (!StringUtils.isEmpty(_concentrationValue)) {
        ConcentrationUnit units = _concentrationType.getSelection();
        c = new Concentration(_concentrationValue, units).convertToReasonableUnits();
      }
      ((LabActivity) _activity).setConcentration(c);
      _concentrationType = null;
      _concentrationValue = null;
    }
    catch (Exception e) {
      // herein lies the weakness of the application level validation:
      // you have to know the field name here.
      // alternative would be to write a validator/UIComponent that could take
      // both values at once.
      String fieldName = "concentrationtypedValuetypedValue";
      String msgKey = "invalidUserInput";

      String msg = "Concentration value is incorrect";
      if (e.getLocalizedMessage() != null)
        msg += ": " + e.getLocalizedMessage();

      FacesContext facesContext = getFacesContext();
      log.warn("validation on concentration field: " + _concentrationValue, e);

      showMessageForLocalComponentId(facesContext, fieldName, msgKey, msg);
      return false;
    }
    return true;
  }

  private boolean saveVolumeTransferredPerWell()
  {
    try {
      Volume v = null;
      if (!StringUtils.isEmpty(_volumeValue)) {
        VolumeUnit units = _volumeType.getSelection();
        v = new Volume(_volumeValue, units).convertToReasonableUnits();
      }
      ((LabActivity) _activity).setVolumeTransferredPerWell(v);
      _volumeType = null;
      _volumeValue = null;
    }
    catch (Exception e) {
      // herein lies the weakness of the application level validation:
      // you have to know the field name here.
      // alternative would be to write a validator/UIComponent that could take
      // both values at once.
      String fieldName = "volumeTransferredPerWelltypedValuetypedValue";
      String msgKey = "invalidUserInput";

      String msg = "Volume value is incorrect";
      if (e.getLocalizedMessage() != null)
        msg += ": " + e.getLocalizedMessage();

      FacesContext facesContext = getFacesContext();
      log.warn("validation on: " + fieldName + ": " + _volumeValue, e);

      showMessageForLocalComponentId(facesContext, fieldName, msgKey, msg);
      return false;
    }
    return true;
  }

  public DataModel getLibraryAndPlatesScreenedDataModel()
  {
    if (_libraryAndPlatesScreenedDataModel == null) {
      List<LibraryAndPlatesUsed> libraryAndPlatesUsed = new ArrayList<LibraryAndPlatesUsed>();
      if (_activity instanceof LibraryScreening) {
        for (PlatesUsed platesUsed : ((LibraryScreening) _activity).getPlatesUsed()) {
          libraryAndPlatesUsed.add(new LibraryAndPlatesUsed(_librariesDao, platesUsed));
        }
      }
      _libraryAndPlatesScreenedDataModel = new ListDataModel(libraryAndPlatesUsed);
    }
    return _libraryAndPlatesScreenedDataModel;
  }


  /* JSF Application methods */

  @UIControllerMethod
  public String viewActivity()
  {
    Integer entityId = Integer.parseInt(getRequestParameter("entityId").toString());
    if (entityId == null) {
      throw new IllegalArgumentException("missing 'entityId' request parameter");
    }
    Activity activity = _dao.findEntityById(Activity.class, entityId);
    if (activity == null) {
      throw new IllegalArgumentException(Activity.class.getSimpleName() + " " + entityId + " does not exist");
    }
    return _thisProxy.viewActivity(activity);
  }

  @UIControllerMethod
  @Transactional
  public String viewActivity(Activity activity)
  {
    // TODO: implement as aspect
    if (activity.isRestricted()) {
      showMessage("restrictedEntity", "Activity " + activity.getActivityId());
      log.warn("user unauthorized to view " + activity);
      return REDISPLAY_PAGE_ACTION_RESULT;
    }

    setActivity(activity);

    // calling viewActivity() is a request to view the most up-to-date, persistent
    // version of the activity, which means the labActivitesBrowser must also be
    // updated to reflect the persistent version of the user
    _labActivitiesBrowser.refetch();

    // all activities are viewed within the context of a search results, providing
    // the user with activity search options at all times
    // ActivitySearchResults will call our setActivity() method
    if (!_labActivitiesBrowser.viewEntity((LabActivity) activity)) {
      _labActivitiesBrowser.searchAllActivities();
      // note: calling viewEntity(user) will only work as long as
      // ActivitySearchResults continues to use InMemoryDataTableModel
      _labActivitiesBrowser.viewEntity((LabActivity) activity);
    }
    return BROWSE_ACTIVITIES;
  }

  @UIControllerMethod
  public String editNewActivity(Activity activity, AbstractBackingBean returnToViewerAfterEdit)
  {
    setActivity(activity);
    // TODO: this model shouldn't allow this null value, and we should really set to null at the UI component level only
    activity.setDateOfActivity(null);
    _isEditMode = true;
    _returnToViewAfterEdit = returnToViewerAfterEdit;
    return VIEW_ACTIVITY;
  }

  @UIControllerMethod
  public String addPlatesScreened()
  {
    if (_activity instanceof LibraryScreening) {
      LibraryScreening libraryScreening = (LibraryScreening) _activity;
      if (getNewPlatesScreened().getStartPlate() != null &&
        getNewPlatesScreened().getCopyName() != null &&
        getNewPlatesScreened().getCopyName().length() != 0) {
        if (getNewPlatesScreened().getEndPlate() == null) {
          getNewPlatesScreened().setEndPlate(getNewPlatesScreened().getStartPlate());
        }
        for (PlatesUsed platesUsed : splitPlateRangeByLibrary(getNewPlatesScreened())) {
          libraryScreening.createPlatesUsed(platesUsed.getStartPlate(),
                                            platesUsed.getEndPlate(),
                                            platesUsed.getCopy());
        }
      }
        _libraryAndPlatesScreenedDataModel = null;
        _newPlatesScreened = null;
      }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  private Set<PlatesUsed> splitPlateRangeByLibrary(PlatesUsed platesUsed)
  {
    int startPlate = platesUsed.getStartPlate();
    int endPlate = platesUsed.getEndPlate();
    Library libraryWithEndPlate = _librariesDao.findLibraryWithPlate(endPlate);
    if (libraryWithEndPlate == null) {
      reportApplicationError("plate " + endPlate + " is not contained in any library");
      return Collections.emptySet();
    }
    Set<PlatesUsed> result = new HashSet<PlatesUsed>();
    do {
      Library libraryWithStartPlate = _librariesDao.findLibraryWithPlate(startPlate);
      if (libraryWithStartPlate == null) {
        reportApplicationError("plate " + endPlate + " is not contained in any library");
        return Collections.emptySet();
      }
      PlatesUsed platesUsed2 = new PlatesUsed();
      platesUsed2.setStartPlate(startPlate);
      platesUsed2.setEndPlate(Math.min(libraryWithStartPlate.getEndPlate(), endPlate));
      platesUsed2.setCopy(new Copy(libraryWithStartPlate,
                                   CopyUsageType.FOR_LIBRARY_SCREENING,
                                   platesUsed.getCopyName()));
      result.add(platesUsed2);
      startPlate = libraryWithStartPlate.getEndPlate() + 1;
    } while (startPlate <= endPlate);
    return result;
  }

  public String deletePlatesScreened()
  {
    if (_activity instanceof LibraryScreening) {
      LibraryScreening libraryScreening = (LibraryScreening) _activity;
      LibraryAndPlatesUsed libraryAndPlatesUsed = (LibraryAndPlatesUsed) getLibraryAndPlatesScreenedDataModel().getRowData();
      libraryScreening.getPlatesUsed().remove(libraryAndPlatesUsed.getPlatesUsed());
      _libraryAndPlatesScreenedDataModel = null;
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  public List<String> getAllCopies()
  {
    // TODO: master copies list to be acquired from database
    return Arrays.asList("", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "HA", "HB", "HC", "HD", "HE", "HF", "HG", "HH", "HI", "HJ", "H Stock", "MA", "MB", "MC", "MD", "ME", "MF", "MG", "MH", "MI", "MJ", "M Stock", "St A", "St B", "St C", "St D", "St E");
  }


  // private methods

  private void resetView()
  {
    _isEditMode = false;
    _returnToViewAfterEdit = _thisProxy;
    _performedBy = null;
    _libraryAndPlatesScreenedDataModel = null;
    _newPlatesScreened = null;
    _concentrationType = null;
    _concentrationValue = null;
    _volumeType = null;
    _volumeValue = null;
  }
}

