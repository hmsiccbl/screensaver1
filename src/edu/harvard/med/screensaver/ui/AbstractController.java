// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.application.Application;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import edu.harvard.med.screensaver.ui.util.Messages;

/**
 * A base Controller class for JSF backing beans (beans that handle JSF actions
 * and events).
 * 
 * @author ant
 */
public abstract class AbstractController
{
  private Messages _messages;
  private FacesContext _cachedFacesContext;

  protected Messages getMessages() 
  {
    return _messages;
  }

  public void setMessages(Messages messages) 
  {
    _messages = messages;
  }
  
  
  // JSF convenience methods
  
  public FacesContext getFacesContext()
  {
    if (_cachedFacesContext == null) {
      _cachedFacesContext = FacesContext.getCurrentInstance();
    }
    return _cachedFacesContext;
  }
  
  public Application getApplicationContext()
  {
    return getFacesContext().getApplication();
  }
  
  /**
   * Adds the message of the specified key to the specified component. Any
   * request parameters that have a name of the form "<componentId>MessageParam*" will
   * be used to parameterize the messsage.
   * 
   * @param messageKey
   * @param componentId
   */
  public void setMessage(String messageKey, String componentId)
  {
    List<Object> messageParams = new ArrayList<Object>();
    Map requestMap = getFacesContext().getExternalContext().getRequestMap();
    for (Iterator iter = requestMap.keySet().iterator(); iter.hasNext();) {
      String paramName = (String) iter.next();
      if (paramName.startsWith(componentId + "MessageParam")) {
        Object paramValue = (Object) requestMap.get(paramName);
        messageParams.add(paramValue);
      }
    }  
    _messages.setFacesMessageForComponent(messageKey, messageParams.toArray(), componentId);
  }
  
  public UIComponent findComponent(String componentId)
  {
    return getFacesContext().getViewRoot().findComponent(componentId);
  }
  
  public UIComponent findComponent(String componentId, String parentId)
  {
    return getFacesContext().getViewRoot().findComponent(parentId + NamingContainer.SEPARATOR_CHAR + componentId);
  }
}
