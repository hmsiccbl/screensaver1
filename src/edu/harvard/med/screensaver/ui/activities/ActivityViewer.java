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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.Activity;
import edu.harvard.med.screensaver.model.DataModelViolationException;
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
import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.util.EditableViewer;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;
import edu.harvard.med.screensaver.ui.util.UISelectOneEntityBean;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

public class ActivityViewer extends AbstractBackingBean implements EditableViewer
{
  // static members

  private static Logger log = Logger.getLogger(ActivityViewer.class);
  private static final ScreensaverUserRole EDITING_ROLE = ScreensaverUserRole.SCREENS_ADMIN;


  // instance data members

  private ActivityViewer _thisProxy;
  private GenericEntityDAO _dao;
  private LibrariesDAO _librariesDao;

  private Activity _activity;
  private boolean _isEditMode;
  private UISelectOneEntityBean<ScreensaverUser> _performedBy;
  private UISelectOneBean<AssayProtocolType> _assayProtocolType;
  private DataModel _libraryAndPlatesScreenedDataModel;
  private PlatesUsed _newPlatesScreened;
  private AbstractBackingBean _returnToViewAfterEdit;


  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected ActivityViewer()
  {
  }

  public ActivityViewer(ActivityViewer _activityViewerProxy,
                        GenericEntityDAO dao,
                        LibrariesDAO librariesDao)
  {
    _thisProxy = _activityViewerProxy;
    _returnToViewAfterEdit = _thisProxy;
    _dao = dao;
    _librariesDao = librariesDao;
  }


  // public methods

  public Activity getActivity()
  {
    return _activity;
  }

  public void setActivity(Activity activity)
  {
    _activity = activity;
    resetView();
  }

  public UISelectOneBean<ScreensaverUser> getPerformedBy()
  {
    if (_performedBy == null) {
      Set<ScreensaverUser> performedByCandidates;
      if (_activity instanceof LabActivity) {
         performedByCandidates = new HashSet<ScreensaverUser>(((LabActivity) _activity).getScreen().getAssociatedScreeningRoomUsers());
      }
      else {
        performedByCandidates = new HashSet<ScreensaverUser>(_dao.findAllEntitiesOfType(ScreensaverUser.class));
      }
      _performedBy = new UISelectOneEntityBean<ScreensaverUser>(
        performedByCandidates,
        (ScreensaverUser) _activity.getPerformedBy(),
        _dao) {
        protected String getLabel(ScreensaverUser t) { return t.getFullNameLastFirst(); }
      };
    }
    return _performedBy;
  }

  public UISelectOneBean<AssayProtocolType> getAssayProtocolType()
  {
    if (_assayProtocolType == null) {
      _assayProtocolType = new UISelectOneBean<AssayProtocolType>(
        Arrays.asList(AssayProtocolType.values()),
        _activity instanceof Screening ? ((Screening) _activity).getAssayProtocolType() : null) {
        protected String getLabel(AssayProtocolType t) { return t.getValue(); }
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
    return _thisProxy.viewActivity(_activity);
  }

  @Override
  protected ScreensaverUserRole getEditableAdminRole()
  {
    return EDITING_ROLE;
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
    return REDISPLAY_PAGE_ACTION_RESULT;
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
    _isEditMode = false;
    _dao.saveOrUpdateEntity(getActivity());
    getActivity().setPerformedBy(getPerformedBy().getSelection());
//    if (_activity instanceof LibraryScreening) {
//      resplitExistingPlatesUsedByLibrary((LibraryScreening) _activity);
//    }
    // TODO _labActivitiesBrowser.refetch();
    return _returnToViewAfterEdit.reload();
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
  @Transactional
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
    return viewActivity(activity);
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

    activity = _dao.reloadEntity(activity,
                                 true,
                                 "performedBy",
                                 "screen.labHead",
                                 "screen.leadScreener",
                                 "screen.collaborators");
    if (activity instanceof LibraryScreening) {
      _dao.need(activity, "platesUsed");
    }
    if (activity instanceof RNAiCherryPickScreening) {
      _dao.need(activity, "rnaiCherryPickRequest");
    }
    setActivity(activity);
    return VIEW_ACTIVITY;
  }

  @UIControllerMethod
  @Transactional
  public String editNewActivity(Activity activity, AbstractBackingBean returnToViewerAfterEdit)
  {
    setActivity(activity);
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
        getNewPlatesScreened().getEndPlate() != null &&
        getNewPlatesScreened().getCopy() != null &&
        getNewPlatesScreened().getCopy().length() != 0) {
        for(PlatesUsed platesUsed : splitPlateRangeByLibrary(getNewPlatesScreened())) {
          libraryScreening.createPlatesUsed(platesUsed.getStartPlate(),
                                            platesUsed.getEndPlate(),
                                            platesUsed.getCopy());
        }
        _libraryAndPlatesScreenedDataModel = null;
        _newPlatesScreened = null;
      }
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  private Set<PlatesUsed> splitPlateRangeByLibrary(PlatesUsed platesUsed)
  {
    int startPlate = platesUsed.getStartPlate();
    int endPlate = platesUsed.getEndPlate();
    Library libraryWithEndPlate = _librariesDao.findLibraryWithPlate(endPlate);
    if (libraryWithEndPlate == null) {
      throw new DataModelViolationException("end plate " + endPlate + " is not contained in any library");
    }
    Set<PlatesUsed> result = new HashSet<PlatesUsed>();
    do {
      Library libraryWithStartPlate = _librariesDao.findLibraryWithPlate(startPlate);
      if (libraryWithStartPlate == null) {
        throw new DataModelViolationException("plate " + startPlate + " is not contained in any library");
      }
      PlatesUsed platesUsed2 = new PlatesUsed();
      platesUsed2.setStartPlate(startPlate);
      platesUsed2.setEndPlate(Math.min(libraryWithStartPlate.getEndPlate(), endPlate));
      platesUsed2.setCopy(platesUsed.getCopy());
      result.add(platesUsed2);
      startPlate = libraryWithStartPlate.getEndPlate() + 1;
    } while (startPlate <= endPlate);
    return result;
  }

  private void resplitExistingPlatesUsedByLibrary(LibraryScreening libraryScreening)
  {
    Set<PlatesUsed> newPlatesUsed = new HashSet<PlatesUsed>();
    for (PlatesUsed platesUsed : libraryScreening.getPlatesUsed()) {
      newPlatesUsed.addAll(splitPlateRangeByLibrary(platesUsed));
    }
    libraryScreening.getPlatesUsed().clear();
    libraryScreening.getPlatesUsed().addAll(newPlatesUsed);
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
  }
}

