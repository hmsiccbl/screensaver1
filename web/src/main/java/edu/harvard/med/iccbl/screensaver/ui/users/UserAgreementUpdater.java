// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.ui.users;

import java.io.IOException;
import java.util.SortedSet;

import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.apache.myfaces.custom.fileupload.UploadedFile;

import edu.harvard.med.iccbl.screensaver.IccblScreensaverConstants;
import edu.harvard.med.iccbl.screensaver.policy.DataSharingLevelMapper;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.arch.util.UISelectOneBean;
import edu.harvard.med.screensaver.ui.arch.view.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.arch.view.aspects.UICommand;
import edu.harvard.med.screensaver.ui.users.UserViewer;


public class UserAgreementUpdater extends AbstractBackingBean
{
  private static Logger log = Logger.getLogger(UserAgreementUpdater.class);

  private UploadedFile _userAgreementUploadedFile;
  private UISelectOneBean<ScreensaverUserRole> _dataSharingLevel;

  private UserViewer _userViewer;
  private edu.harvard.med.iccbl.screensaver.service.users.UserAgreementUpdater _userAgreementUpdater;

  private ScreenType _screenType;

  protected UserAgreementUpdater() {}

  public boolean isEnabled()
  {
    return getApplicationProperties().isFeatureEnabled("user_agreement_updater");
  }

  public UserAgreementUpdater(UserViewer userViewer,
                              ScreenType screenType,
                              edu.harvard.med.iccbl.screensaver.service.users.UserAgreementUpdater userAgreementUpdater)
  {
    _userViewer = userViewer;
    _screenType = screenType;
    _userAgreementUpdater = userAgreementUpdater;
  }

  public ScreenType getScreenType()
  {
    return _screenType;
  }

  public void setScreenType(ScreenType screenType)
  {
    _screenType = screenType;
  }

  public void setUserAgreementUploadedFile(UploadedFile uploadedFile)
  {
    _userAgreementUploadedFile = uploadedFile;
  }

  public UploadedFile getUserAgreementUploadedFile()
  {
    return _userAgreementUploadedFile;
  }
  
  public String getCurrentDataSharingLevelRoleName()
  {
    ScreensaverUserRole dslRole = DataSharingLevelMapper.getPrimaryDataSharingLevelRoleForUser(getScreenType(), _userViewer.getScreeningRoomUser());
    return dslRole == null ? "<none>" : dslRole.getDisplayableRoleName();
  }
  
  public String getLabHeadDataSharingLevelRoleName()
  {
    if (_userViewer.getScreeningRoomUser().isHeadOfLab()) {
      return "<user is lab head>";
    }
    LabHead labHead = _userViewer.getScreeningRoomUser().getLab().getLabHead();
    if (labHead == null) {
      return "<no lab head specified>";
    }
    ScreensaverUserRole dslRole = DataSharingLevelMapper.getPrimaryDataSharingLevelRoleForUser(getScreenType(), labHead);
    return dslRole == null ? "<none>" : dslRole.getDisplayableRoleName();
  }
  
  public boolean isScreensaverUser()
  {
    return _userViewer.getEntity().getScreensaverUserRoles().contains(ScreensaverUserRole.SCREENSAVER_USER);
  }

  public UISelectOneBean<ScreensaverUserRole> getNewDataSharingLevel()
  {
    if (_dataSharingLevel == null) {
      SortedSet<ScreensaverUserRole> candidateDslRoles = Sets.newTreeSet(DataSharingLevelMapper.UserDslRoles.get(getScreenType()));
      // At ICCB-L, the RNAi DSL 2 role is not an option, so we hide it at the UI level; we maintain it in our model for consistency with the SM DSL roles
      // TODO: refactor to share this logic with similar code in UserViewer.getNewUserRole()
      if (getApplicationProperties().isFacility(IccblScreensaverConstants.FACILITY_KEY)) {
        candidateDslRoles.remove(ScreensaverUserRole.RNAI_DSL_LEVEL2_MUTUAL_POSITIVES);
      }

      _dataSharingLevel = new UISelectOneBean<ScreensaverUserRole>(candidateDslRoles,
                                                                   DataSharingLevelMapper.getPrimaryDataSharingLevelRoleForUser(getScreenType(), _userViewer.getScreeningRoomUser()),
                                                                   false) {
        @Override
        protected String makeLabel(ScreensaverUserRole t)
        {
          return t.getDisplayableRoleName();
        }
      };
    }
    return _dataSharingLevel;
  }

  @UICommand
  public String updateUser() throws IOException
  {
    ScreeningRoomUser user = _userViewer.getScreeningRoomUser();
    _userAgreementUpdater.updateUser(user,
                                     getNewDataSharingLevel().getSelection(), 
                                     getScreenType(),
                                     getUserAgreementUploadedFile().getName(),
                                     getUserAgreementUploadedFile().getInputStream(),
                                     (AdministratorUser) getScreensaverUser());
    showMessage("users.updatedUserAgreement");
    return _userViewer.viewEntity(user);
  }
  
  @UICommand
  public String cancel()
  {
    return _userViewer.reload();
  }

  public edu.harvard.med.iccbl.screensaver.service.users.UserAgreementUpdater getUserAgreementUpdater()
  {
    return _userAgreementUpdater;
  }

  public void setUserAgreementUpdater(edu.harvard.med.iccbl.screensaver.service.users.UserAgreementUpdater userAgreementUpdater)
  {
    _userAgreementUpdater = userAgreementUpdater;
  }
}
