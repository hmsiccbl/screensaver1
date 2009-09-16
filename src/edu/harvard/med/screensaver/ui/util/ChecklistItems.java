// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ChecklistItem;
import edu.harvard.med.screensaver.model.users.ChecklistItemEvent;
import edu.harvard.med.screensaver.model.users.ChecklistItemGroup;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.AbstractEditableBackingBean;
import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.users.ChecklistItemsEntity;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

/**
 * Checklist Items backing bean.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class ChecklistItems extends AbstractEditableBackingBean
{
  private static Logger log = Logger.getLogger(ChecklistItems.class);
  
  private GenericEntityDAO _dao;
  private AbstractBackingBean _parentViewer;

  private ChecklistItemsEntity _entity; 
  private boolean _isChecklistItemsCollapsed = true;
  private Map<ChecklistItemGroup,DataModel> _checklistItemDataModelMap;
  private AdministratorUser _checklistItemEventEnteredBy;
  private Map<ChecklistItem,LocalDate> _newChecklistItemDatePerformed;

  private List<ChecklistItemGroup> _checklistItemGroups;

  
  ChecklistItems()
  {
  }
  
  public ChecklistItems(GenericEntityDAO dao)
  {
    super(ScreensaverUserRole.USER_CHECKLIST_ITEMS_ADMIN);
    _dao = dao;
  }
  
  public void setParentViewer(AbstractBackingBean parentViewer)
  {
    _parentViewer = parentViewer;
  }

  public void setChecklistItemsEntity(ChecklistItemsEntity entity)
  {
    _entity = entity;

    // HACK: must find the logged in admin user now, so we can re-use same instance, if multiple activations/expirations are entered before save()
    if (getScreensaverUser() instanceof AdministratorUser) {
      _checklistItemEventEnteredBy = _dao.reloadEntity((AdministratorUser) getScreensaverUser(), false, "activitiesPerformed");
    }
  }
  
  public AbstractEntity getEntity()
  {
    return (AbstractEntity) _entity;
  }

  public void setChecklistItemGroups(List<ChecklistItemGroup> checklistItemGroups)
  {
    _checklistItemGroups = checklistItemGroups;
  }
  
  public void reset()
  {
    _entity = null;
    setEditMode(false);
    _checklistItemDataModelMap = null;
    _newChecklistItemDatePerformed = null;
  }

  public boolean isChecklistItemsCollapsed()
  {
    return _isChecklistItemsCollapsed;
  }

  public void setChecklistItemsCollapsed(boolean isChecklistItemsCollapsed)
  {
    _isChecklistItemsCollapsed = isChecklistItemsCollapsed;
  }

  @UIControllerMethod
  public String edit()
  {
    setEditMode(true);
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String cancel()
  {
    setEditMode(false);
    return _parentViewer.reload();
  }

  @UIControllerMethod
  @Transactional
  public String save()
  {
    setEditMode(false);

    assert getEntity().getEntityId() != null : "parent entity must be persisted";
    _dao.reattachEntity(getEntity());
    _dao.flush();
    return _parentViewer.reload();
  }

  public List<ChecklistItemGroup> getChecklistItemGroups()
  {
    return _checklistItemGroups;
  }

  public Map<ChecklistItemGroup,DataModel> getChecklistItemsDataModelMap()
  {
    if (_checklistItemDataModelMap == null) {
      _checklistItemDataModelMap = new HashMap<ChecklistItemGroup,DataModel>();
      for (ChecklistItemGroup group : getChecklistItemGroups()) {
        Map<ChecklistItem,ChecklistItemEvent> checklistItemsMap = new LinkedHashMap<ChecklistItem,ChecklistItemEvent>();
        List<ChecklistItem> checklistItems = _dao.findEntitiesByProperty(ChecklistItem.class, "checklistItemGroup", group);
        Collections.sort(checklistItems);
        for (ChecklistItem type : checklistItems) {
          checklistItemsMap.put(type, null);
        }
        for (ChecklistItemEvent checklistItemEvent : _entity.getChecklistItemEvents()) {
          if (checklistItemEvent.getChecklistItem().getChecklistItemGroup() == group) {
            checklistItemsMap.put(checklistItemEvent.getChecklistItem(), checklistItemEvent);
          }
        }
        DataModel checklistItemDataModel = new ListDataModel(Lists.newArrayList(checklistItemsMap.entrySet()));
        _checklistItemDataModelMap.put(group, checklistItemDataModel);
      }
    }
    return _checklistItemDataModelMap;
  }
  
  public Map<ChecklistItem,LocalDate> getNewChecklistItemDatePerformed()
  {
    if (_newChecklistItemDatePerformed == null) {
      LocalDate today = new LocalDate();
      _newChecklistItemDatePerformed = new HashMap<ChecklistItem,LocalDate>();
      for (DataModel groupDataModel : getChecklistItemsDataModelMap().values()) {
        for (Map.Entry<ChecklistItem,ChecklistItemEvent> entry : (List<Map.Entry<ChecklistItem,ChecklistItemEvent>>) groupDataModel.getWrappedData()) {
          _newChecklistItemDatePerformed.put(entry.getKey(), today);
        }
      }
    }
    return _newChecklistItemDatePerformed;
  }

  @UIControllerMethod
  public String checklistItemActivated()
  {
    Map.Entry<ChecklistItem,ChecklistItemEvent> entry = (Map.Entry<ChecklistItem,ChecklistItemEvent>) getRequestMap().get("element");
    assert entry.getKey().isExpirable() && (entry.getValue() == null || entry.getValue().isExpiration());
    _entity.createChecklistItemActivationEvent(entry.getKey(),
                                               getNewChecklistItemDatePerformed().get(entry.getKey()),
                                               new AdministrativeActivity(_checklistItemEventEnteredBy,
                                                                          new LocalDate(),
                                                                          AdministrativeActivityType.CHECKLIST_ITEM_EVENT));
    _checklistItemDataModelMap = null;
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String checklistItemDeactivated()
  {
    Map.Entry<ChecklistItem,ChecklistItemEvent> entry = (Map.Entry<ChecklistItem,ChecklistItemEvent>) getRequestMap().get("element");
    assert entry.getKey().isExpirable() && entry.getValue() != null && !entry.getValue().isExpiration();
    entry.getValue().createChecklistItemExpirationEvent(getNewChecklistItemDatePerformed().get(entry.getKey()),
                                                        new AdministrativeActivity(_checklistItemEventEnteredBy,
                                                                                   new LocalDate(),
                                                                                   AdministrativeActivityType.CHECKLIST_ITEM_EVENT));
    _checklistItemDataModelMap = null;
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  public String checklistItemCompleted()
  {
    Map.Entry<ChecklistItem,ChecklistItemEvent> entry = (Map.Entry<ChecklistItem,ChecklistItemEvent>) getRequestMap().get("element");
    assert !entry.getKey().isExpirable() && entry.getValue() == null;
    _entity.createChecklistItemActivationEvent(entry.getKey(),
                                               getNewChecklistItemDatePerformed().get(entry.getKey()),
                                               new AdministrativeActivity(_checklistItemEventEnteredBy,
                                                                          new LocalDate(),
                                                                          AdministrativeActivityType.CHECKLIST_ITEM_EVENT));
    _checklistItemDataModelMap = null;
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  @UIControllerMethod
  @SuppressWarnings("unchecked")
  public String checklistItemNotApplicable()
  {
    Map.Entry<ChecklistItem,ChecklistItemEvent> entry = 
      (Map.Entry<ChecklistItem,ChecklistItemEvent>) getRequestMap().get("element");

    assert entry.getValue() == null || entry.getKey().isExpirable() || entry.getValue().isExpiration();
    _entity.createChecklistItemNotApplicableEvent(entry.getKey(),
                                                  getNewChecklistItemDatePerformed().get(entry.getKey()),
                                                  new AdministrativeActivity(_checklistItemEventEnteredBy,
                                                                             new LocalDate(),
                                                                             AdministrativeActivityType.CHECKLIST_ITEM_EVENT));
    _checklistItemDataModelMap = null;
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

}
