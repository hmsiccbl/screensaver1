// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screens;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.UsersDAO;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.EditableEntityViewerBackingBean;
import edu.harvard.med.screensaver.ui.UICommand;
import edu.harvard.med.screensaver.ui.util.JSFUtils;
import edu.harvard.med.screensaver.ui.util.ScreensaverUserComparator;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;
import edu.harvard.med.screensaver.ui.util.UISelectOneEntityBean;

import org.apache.log4j.Logger;

import com.google.common.collect.Sets;

public abstract class AbstractStudyDetailViewer<E extends Study> extends EditableEntityViewerBackingBean<E>
{
  private static Logger log = Logger.getLogger(AbstractStudyDetailViewer.class);

  private UsersDAO _usersDao;

  private UISelectOneEntityBean<LabHead> _labName;
  private UISelectOneEntityBean<ScreeningRoomUser> _leadScreener;
  protected UISelectOneEntityBean<ScreeningRoomUser> _newCollaborator;
  protected SortedSet<ScreeningRoomUser> _collaborators;

  
  /**
   * @motivation for CGLIB2
   */
  protected AbstractStudyDetailViewer()
  {
  }

  protected AbstractStudyDetailViewer(AbstractStudyDetailViewer thisProxy,
                                      GenericEntityDAO dao,
                                      String viewerActionResult,
                                      UsersDAO usersDao)
  {
    super(thisProxy, (Class<E>) Screen.class, viewerActionResult, dao);
    _usersDao = usersDao;
    getIsPanelCollapsedMap().put("studyDetail", false);
  }
  
  public DataModel getCollaboratorsDataModel()
  {
    return new ListDataModel(new ArrayList<ScreeningRoomUser>(_collaborators));
  }

  public List<SelectItem> getScreenTypeSelectItems()
  {
    List<ScreenType> items = Arrays.asList(ScreenType.values());
    if (getEntity().getScreenType() == null) {
      return JSFUtils.createUISelectItemsWithEmptySelection(items, "<select>");
    }
    else {
      return JSFUtils.createUISelectItems(items);
    }
  }

  public UISelectOneBean<LabHead> getLabName()
  {
    if (_labName == null) {
      SortedSet<LabHead> labHeads = _usersDao.findAllLabHeads();
      _labName = new UISelectOneEntityBean<LabHead>(labHeads, getEntity().getLabHead(), true, getDao()) {
        @Override
        protected String makeLabel(LabHead t) { return t.getLab().getLabName(); }
      };
    }
    return _labName;
  }

  public UISelectOneBean<ScreeningRoomUser> getNewCollaborator()
  {
    if (_newCollaborator == null) {
      SortedSet<ScreeningRoomUser> collaborators = getCandidateCollaborators(getEntity());
      _newCollaborator = new UISelectOneEntityBean<ScreeningRoomUser>(collaborators, null, true, getDao()) {
        @Override
        protected String makeLabel(ScreeningRoomUser t) { return t.getFullNameLastFirst(); }
        @Override 
        protected String getEmptyLabel() { return "<select>"; }
      };
    }
    return _newCollaborator;
  }

  public UISelectOneBean<ScreeningRoomUser> getLeadScreener()
  {
    if (_leadScreener == null) {
      ArrayList<ScreeningRoomUser> leadScreenerCandidates = new ArrayList<ScreeningRoomUser>();
      leadScreenerCandidates.addAll(getDao().findAllEntitiesOfType(ScreeningRoomUser.class));
      Collections.sort(leadScreenerCandidates, ScreensaverUserComparator.getInstance());
      _leadScreener = new UISelectOneEntityBean<ScreeningRoomUser>(leadScreenerCandidates,
        getEntity().getLeadScreener(),
        getEntity().getLeadScreener() == null,                                                                     
        getDao()) {
        @Override
        protected String makeLabel(ScreeningRoomUser t) { return t.getFullNameLastFirst(); }
        @Override
        protected String getEmptyLabel() { return "<select>"; }
      };
    }
    return _leadScreener;
  }

  @Override
  protected void initializeEntity(E entity) {};
  
  @Override
  protected void initializeViewer(E entity)
  {
    _labName = null;
    _leadScreener = null;
    _newCollaborator = null; 
    _collaborators = Sets.newTreeSet(entity.getCollaborators());
  }
  
  private SortedSet<ScreeningRoomUser> getCandidateCollaborators(E entity)
  {
    SortedSet<ScreeningRoomUser> candidateCollaborators = new TreeSet<ScreeningRoomUser>(ScreensaverUserComparator.getInstance());
    candidateCollaborators.addAll(getDao().findAllEntitiesOfType(ScreeningRoomUser.class));
    candidateCollaborators.removeAll(_collaborators);
    return candidateCollaborators;
  }

  @UICommand
  public String addCollaborator()
  {
    if (getNewCollaborator().getSelection() != null) {
      ScreeningRoomUser collaborator = getNewCollaborator().getSelection();
      _collaborators.add(collaborator);
      _newCollaborator = null;
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UICommand
  public String deleteCollaborator()
  {
    ScreeningRoomUser collaborator = (ScreeningRoomUser) getRequestMap().get("element");
    _collaborators.remove(collaborator);
    _newCollaborator = null;
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  public SortedSet<ScreeningRoomUser> getCollaborators()
  {
    return _collaborators;
  }
}

