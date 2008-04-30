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
import java.util.List;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.Activity;
import edu.harvard.med.screensaver.model.screens.AssayProtocolType;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.PlatesUsed;
import edu.harvard.med.screensaver.model.screens.RNAiCherryPickScreening;
import edu.harvard.med.screensaver.model.screens.Screen;
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

  private GenericEntityDAO _dao;
  
  private Activity _activity;
  private boolean _isEditMode = false;
  private UISelectOneEntityBean<ScreensaverUser> _performedBy;
  private UISelectOneBean<AssayProtocolType> _assayProtocolType;
  private DataModel _platesScreenedDataModel;
  private PlatesUsed _newPlatesScreened;


  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected ActivityViewer()
  {
  }

  public ActivityViewer(GenericEntityDAO dao)
  {
    _dao = dao;
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
      _performedBy = new UISelectOneEntityBean<ScreensaverUser>(
        _dao.findAllEntitiesOfType(ScreensaverUser.class), 
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
      if (_activity instanceof LibraryScreening) {
       LibraryScreening libraryScreening = (LibraryScreening) _activity;
      }
    }
    return _newPlatesScreened;
  }
  
  @Override
  public String reload()
  {
    return viewActivity(_activity, REDISPLAY_PAGE_ACTION_RESULT);
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
  @Transactional
  public String edit()
  {
    _isEditMode = true;
    _dao.reattachEntity(getActivity()); // check for updated data 
    //_dao.need();
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  @Transactional // necessary, due to Spring AOP not triggered on self-invocations of methods 
  public String cancel()
  {
    _isEditMode = false;
    return reload();
  }

  @UIControllerMethod
  @Transactional
  public String save()
  {
    _isEditMode = false;
    _dao.reattachEntity(getActivity());
    getActivity().setPerformedBy(getPerformedBy().getSelection());
    // TODO _labActivitiesBrowser.refetch();
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public DataModel getPlatesScreenedDataModel()
  {
    if (_platesScreenedDataModel == null) {
      List<PlatesUsed> platesUsed = new ArrayList<PlatesUsed>();
      if (_activity instanceof LibraryScreening) {
        platesUsed.addAll(((LibraryScreening) _activity).getPlatesUsed());
      }
      _platesScreenedDataModel = new ListDataModel(platesUsed);
    }
    return _platesScreenedDataModel;
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
    return viewActivity(activity, VIEW_ACTIVITY);
  }

  @UIControllerMethod
  @Transactional
  public String viewActivity(Activity activity, String actionResult)
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
                                 "screen.leadScreener");
    if (activity instanceof LibraryScreening) {
      _dao.need(activity, "platesUsed");
    }
    if (activity instanceof RNAiCherryPickScreening) {
      _dao.need(activity, "rnaiCherryPickRequest");
    }
    setActivity(activity);
    return actionResult;
  }
  
  public String addPlatesScreened()
  {
    if (_activity instanceof LibraryScreening) {
      LibraryScreening libraryScreening = (LibraryScreening) _activity;
      if (getNewPlatesScreened().getStartPlate() != null &&
        getNewPlatesScreened().getEndPlate() != null &&
        getNewPlatesScreened().getCopy() != null &&
        getNewPlatesScreened().getCopy().length() != 0) {
        libraryScreening.createPlatesUsed(getNewPlatesScreened().getStartPlate(),
                                          getNewPlatesScreened().getEndPlate(),
                                          getNewPlatesScreened().getCopy());
        _platesScreenedDataModel = null;
        _newPlatesScreened = null;
      }
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  public String deletePlatesScreened()
  {
    if (_activity instanceof LibraryScreening) {
      LibraryScreening libraryScreening = (LibraryScreening) _activity;
      PlatesUsed platesUsed = (PlatesUsed) getPlatesScreenedDataModel().getRowData();
      libraryScreening.getPlatesUsed().remove(platesUsed);
      _platesScreenedDataModel = null;
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
    _performedBy = null;
    _platesScreenedDataModel = null;
    _newPlatesScreened = null;
  }
}

