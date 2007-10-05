// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screens;

import java.util.ArrayList;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.UsersDAO;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.util.UISelectManyBean;
import edu.harvard.med.screensaver.ui.util.UISelectManyEntityBean;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;
import edu.harvard.med.screensaver.ui.util.UISelectOneEntityBean;

public class StudyDetailViewer extends AbstractBackingBean
{
  // static members

  private static Logger log = Logger.getLogger(StudyDetailViewer.class);


  // instance data members

  private GenericEntityDAO _dao;
  private UsersDAO _usersDao;

  private Study _study;
  private UISelectOneEntityBean<ScreeningRoomUser> _labName;
  private UISelectOneEntityBean<ScreeningRoomUser> _leadScreener;
  private UISelectManyEntityBean<ScreeningRoomUser> _collaborators;
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

  public UISelectOneBean<ScreeningRoomUser> getLabName()
  {
    if (_labName == null) {
      _labName = new UISelectOneEntityBean<ScreeningRoomUser>(_usersDao.findAllLabHeads(), _study.getLabHead(), _dao) {
        protected String getLabel(ScreeningRoomUser t) { return t.getLabName(); }
      };
    }
    return _labName;
  }

  /**
   * Get a list of SelectItems for selecting the screen's collaborators.
   */
  public UISelectManyBean<ScreeningRoomUser> getCollaborators()
  {
    if (_collaborators == null) {
      _collaborators =
        new UISelectManyEntityBean<ScreeningRoomUser>(_usersDao.findCandidateCollaborators(),
                                                      _study.getCollaborators(),
                                                      _dao)
        {
          protected String getLabel(ScreeningRoomUser t)
          {
            return t.getFullNameLastFirst();
          }
        };
    }
    return _collaborators;
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

  private void resetView()
  {
    _labName = null;
    _leadScreener = null;
    _collaborators = null;
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
        ScreeningRoomUser labHead = _labName.getSelection();
        ArrayList<ScreeningRoomUser> leadScreenerCandidates = new ArrayList<ScreeningRoomUser>();
        if (labHead != null) {
          leadScreenerCandidates.add(labHead);
          leadScreenerCandidates.addAll(labHead.getLabMembers());
        }
        _leadScreener = new UISelectOneEntityBean<ScreeningRoomUser>(leadScreenerCandidates, _study.getLeadScreener(), _dao) {
          protected String getLabel(ScreeningRoomUser t) { return t.getFullNameLastFirst(); }
        };
      }
    });
  }
}

