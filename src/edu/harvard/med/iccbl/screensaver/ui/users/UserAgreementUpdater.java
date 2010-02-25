// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.ui.users;


import java.io.IOException;

import edu.harvard.med.iccbl.screensaver.policy.DataSharingLevelMapper;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.UICommand;
import edu.harvard.med.screensaver.ui.users.UserViewer;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;

import org.apache.log4j.Logger;
import org.apache.myfaces.custom.fileupload.UploadedFile;


public class UserAgreementUpdater extends AbstractBackingBean
{
  private static Logger log = Logger.getLogger(UserAgreementUpdater.class);

  private UploadedFile _userAgreementUploadedFile;
  private UISelectOneBean<ScreensaverUserRole> _dataSharingLevel;

  private UserViewer _userViewer;
  private edu.harvard.med.iccbl.screensaver.service.users.UserAgreementUpdater _userAgreementUpdater;

  protected UserAgreementUpdater() {}

  public UserAgreementUpdater(UserViewer userViewer,
                              edu.harvard.med.iccbl.screensaver.service.users.UserAgreementUpdater userAgreementUpdater)
  {
    _userViewer = userViewer;
    _userAgreementUpdater = userAgreementUpdater;
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
    return edu.harvard.med.iccbl.screensaver.service.users.UserAgreementUpdater.getCurrentDataSharingLevelRoleName(_userViewer.getScreeningRoomUser());
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
    return edu.harvard.med.iccbl.screensaver.service.users.UserAgreementUpdater.getCurrentDataSharingLevelRoleName(labHead);
  }
  
  public boolean isScreensaverUser()
  {
    return _userViewer.getEntity().getScreensaverUserRoles().contains(ScreensaverUserRole.SCREENSAVER_USER);
  }

  public UISelectOneBean<ScreensaverUserRole> getNewDataSharingLevel()
  {
    if (_dataSharingLevel == null) {
      _dataSharingLevel = new UISelectOneBean<ScreensaverUserRole>(DataSharingLevelMapper.UserSmDslRoles,
        edu.harvard.med.iccbl.screensaver.service.users.UserAgreementUpdater.getCurrentDataSharingLevelRole(_userViewer.getScreeningRoomUser()),
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

  public void setDataSharingLevel(UISelectOneBean<ScreensaverUserRole> dataSharingLevel)
  {
    _dataSharingLevel = dataSharingLevel;
  }

}
