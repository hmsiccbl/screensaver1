// $HeadURL: $
// $Id: $
//

// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.util;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import com.google.common.collect.Lists;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.AuditedAbstractEntity;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.ui.arch.view.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.arch.view.aspects.UICommand;
import edu.harvard.med.screensaver.util.StringUtils;

public class EntityComments<E extends AuditedAbstractEntity> extends AbstractBackingBean
{
  private GenericEntityDAO _dao;

  private E _entity;
  private String _newComment;
  private DataModel _model;
  private AdministratorUser _admin;

  public EntityComments(GenericEntityDAO dao)
  {
    _dao = dao;
  }
   
  protected EntityComments()
  {}
  
  public void setEntity(E entity)
  {
    _entity = entity;
    reset();
    // load current admin instance while we're in session, to avoid NonUniqueObjectException on save
    if (getScreensaverUser() instanceof AdministratorUser) {
      _admin = (AdministratorUser) _dao.reloadEntity(getScreensaverUser());
    }
  }

  public DataModel getCommentsDataModel()
  {
    if (_model == null) {
      _model = new ListDataModel(Lists.newArrayList(_entity.getUpdateActivitiesOfType(AdministrativeActivityType.COMMENT)));
    }
    return _model;
  }

  @UICommand
  public String addNewComment()
  {
    if (StringUtils.isEmpty(_newComment)) {
      showMessage("requiredValue", "Comment");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }

    _entity.createComment(_admin, _newComment);
    reset();
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  public String getNewComment()
  {
    return _newComment;
  }

  public void setNewComment(String newComment)
  {
    _newComment = newComment;
  }

  private void reset()
  {
    _newComment = null;
    _model = null;
  }
}
