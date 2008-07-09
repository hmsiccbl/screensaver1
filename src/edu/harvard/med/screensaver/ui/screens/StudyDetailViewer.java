// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screens;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.UsersDAO;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.EntityViewer;
import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.util.JSFUtils;
import edu.harvard.med.screensaver.ui.util.ScreensaverUserComparator;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;
import edu.harvard.med.screensaver.ui.util.UISelectOneEntityBean;

import org.apache.log4j.Logger;

public class StudyDetailViewer extends AbstractBackingBean implements EntityViewer
{
  // static members

  private static Logger log = Logger.getLogger(StudyDetailViewer.class);


  // instance data members

  private GenericEntityDAO _dao;
  private UsersDAO _usersDao;

  private Study _study;
  private UISelectOneEntityBean<LabHead> _labName;
  private UISelectOneEntityBean<ScreeningRoomUser> _leadScreener;
  private UISelectOneBean<ScreeningRoomUser> _newCollaborator;
  private boolean _isPanelCollapsed;


  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected StudyDetailViewer()
  {
  }

  public StudyDetailViewer(GenericEntityDAO dao,
                           UsersDAO usersDao)
  {
    _dao = dao;
    _usersDao = usersDao;
  }


  // public methods

  public AbstractEntity getEntity()
  {
    return getStudy();
  }

  public void setStudy(Study study)
  {
    _study = study;
    resetView();
  }

  public Study getStudy()
  {
    return _study;
  }

  public boolean isPanelCollapsed()
  {
    return _isPanelCollapsed;
  }

  public void setPanelCollapsed(boolean isPanelCollapsed)
  {
    _isPanelCollapsed = isPanelCollapsed;
  }

  public DataModel getCollaboratorsDataModel()
  {
    return new ListDataModel(new ArrayList<ScreeningRoomUser>(_study.getCollaborators()));
  }

  public List<SelectItem> getScreenTypeSelectItems()
  {
    return JSFUtils.createUISelectItems(Arrays.asList(ScreenType.values()),
                                        _study.getScreenType() == null);
  }

  public UISelectOneBean<LabHead> getLabName()
  {
    if (_labName == null) {
      SortedSet<LabHead> labHeads = _usersDao.findAllLabHeads();
      labHeads.add(null);
      _labName = new UISelectOneEntityBean<LabHead>(labHeads, _study.getLabHead(), _dao) {
        protected String getLabel(LabHead t) { return t == null ? "" : t.getLab().getLabName(); }
      };
    }
    return _labName;
  }

  public UISelectOneBean<ScreeningRoomUser> getNewCollaborator()
  {
    if (_newCollaborator == null) {
      _newCollaborator = new UISelectOneBean<ScreeningRoomUser>(getCandidateCollaborators()) {
        @Override
        protected String getLabel(ScreeningRoomUser t) { return t.getFullNameLastFirst(); }
      };
    }
    return _newCollaborator;
  }

  private Collection<ScreeningRoomUser> getCandidateCollaborators()
  {
    List<ScreeningRoomUser> candidateCollaborators =
      _dao.findAllEntitiesOfType(ScreeningRoomUser.class,
                                 true,
                                 // TODO: this is very inefficient!
                                 "screensCollaborated");
    Collections.sort(candidateCollaborators, ScreensaverUserComparator.getInstance());
    Screen screen = (Screen) getStudy(); // HACK
    candidateCollaborators.removeAll(screen.getCollaborators());
    candidateCollaborators.remove(screen.getLeadScreener());
    candidateCollaborators.remove(screen.getLabHead());
    return candidateCollaborators;
  }

  @UIControllerMethod
  public String addCollaborator()
  {
    if (getNewCollaborator().getSelection() != null) {
      try {
        ScreeningRoomUser collaborator = getNewCollaborator().getSelection();
        ( /*HACK*/(Screen) getStudy()).addCollaborator(collaborator);
      }
      catch (BusinessRuleViolationException e) {
        showMessage("businessError", e.getMessage());
      }
      _newCollaborator = null;
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String deleteCollaborator()
  {
    ScreeningRoomUser collaborator = (ScreeningRoomUser) getRequestMap().get("element");
    getStudy().getCollaborators().remove(collaborator);
    _newCollaborator = null;
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  public UISelectOneBean<ScreeningRoomUser> getLeadScreener()
  {
    if (_leadScreener == null) {
      updateLeadScreenerSelectItems();
    }
    return _leadScreener;
  }


  // JSF event listeners

  public void update(ValueChangeEvent event)
  {
    // despite the Tomahawk taglib docs, this event listener is called *before*
    // the *end* of the apply request values phase, preventing the
    // _labName.value property from being updated already
    getLabName().setValue((String) event.getNewValue());
    updateLeadScreenerSelectItems();
    getFacesContext().renderResponse();
  }


  // private methods

  protected void resetView()
  {
    _labName = null;
    _leadScreener = null;
    _newCollaborator = null;
  }

  /**
   * Updates the set of lead screeners that can be selected for this screen.
   * Depends upon the lab head.
   *
   * @motivation to update the list of lead screeners in the UI, in response to
   *             a new lab head selection, but without updating the entity
   *             before the user saves his edits
   */
  private void updateLeadScreenerSelectItems() {
    _dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        ArrayList<ScreeningRoomUser> leadScreenerCandidates = new ArrayList<ScreeningRoomUser>();
        leadScreenerCandidates.addAll(_dao.findAllEntitiesOfType(ScreeningRoomUser.class));
        if (_study == null || _study.getLeadScreener() == null) {
          leadScreenerCandidates.add(null);
        }
        Collections.sort(leadScreenerCandidates, ScreensaverUserComparator.getInstance());
        _leadScreener = new UISelectOneEntityBean<ScreeningRoomUser>(leadScreenerCandidates,
                                                                     _study.getLeadScreener(),
                                                                     _dao) {
          protected String getLabel(ScreeningRoomUser t)
          {
            return t == null ? "" : t.getFullNameLastFirst();
          }
        };
      }
    });
  }
}

