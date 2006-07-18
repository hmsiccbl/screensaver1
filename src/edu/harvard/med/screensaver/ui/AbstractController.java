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
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import edu.harvard.med.screensaver.ui.util.Messages;

import org.apache.log4j.Logger;

/**
 * A base Controller class for JSF backing beans (beans that handle JSF actions
 * and events).
 * 
 * @author ant
 */
public abstract class AbstractController
{

  // static data members
  
  private static Logger log = Logger.getLogger(AbstractController.class);

  
  // private data members
  
  private Messages _messages;
  private FacesContext _cachedFacesContext;


  // bean property methods
  
  protected Messages getMessages() 
  {
    return _messages;
  }

  public void setMessages(Messages messages) 
  {
    _messages = messages;
  }
  
  
  // public JSF convenience methods
  
  public FacesContext getFacesContext()
  {
    if (true/*_cachedFacesContext == null*/) {
      _cachedFacesContext = FacesContext.getCurrentInstance();
    }
    
    return _cachedFacesContext;
  }
  
  public Application getApplicationContext()
  {
    return getFacesContext().getApplication();
  }
  
  public HttpSession getHttpSession()
  {
    Object httpSession = getFacesContext().getExternalContext().getSession(false);
    if (httpSession == null) {
      return null;
    }
    assert httpSession instanceof HttpSession : "not running in an HTTP-based application server";
    return (HttpSession) httpSession;
  }
  
  public String getSessionDebugInfoString()
  {
    return "ID: " + getHttpSession().getId() + "\n" +
    "last accessed time: " + getHttpSession().getLastAccessedTime();
  }

  /**
   * Adds the message of the specified key to the specified component. Any
   * request parameters that have a name of the form "<componentId>MessageParam*"
   * will be used to parameterize the messsage.
   * 
   * @param messageKey
   * @param componentId the "simple" component ID, as specified in the "id"
   *          attribute of its defining JSF tag (not the fully-qualified client
   *          ID expression required by UIComponent.findComponent(), such as
   *          ":formId:subviewId:fieldId").
   */
  public void setMessage(String messageKey, String componentId)
  {
    List<Object> messageParams = new ArrayList<Object>();
    Map requestMap = getFacesContext().getExternalContext().getRequestMap();
    for (Iterator iter = requestMap.keySet().iterator(); iter.hasNext();) {
      String paramName = (String) iter.next();
      log.debug("inspecting param " + paramName);
      if (paramName.startsWith(componentId + "MessageParam")) {
        log.debug("found param " + paramName);
        Object paramValue = (Object) requestMap.get(paramName);
        messageParams.add(paramValue);
      }
    }
    String clientId = getClientId(findComponent(componentId));
    _messages.setFacesMessageForComponent(messageKey, 
                                          messageParams.toArray(), 
                                          clientId);
  }
  
  /**
   * Returns the fully-qualified "client" ID of the component, which can be used to 
   * @param component
   * @return
   */
  public String getClientId(UIComponent component)
  {
    if (component == null) {
      return null;
    }
    return component.getClientId(getFacesContext());
  }

  public UIComponent findComponent(String componentId)
  {
    return doFindComponent(getFacesContext().getViewRoot(), componentId);
  }
  
  public UIComponent findComponent(String componentId, String parentId)
  {
    UIComponent container = findComponent(parentId);
    return doFindComponent(container,
                           componentId);
  }
  
  
  // private methods
  
  private UIComponent doFindComponent(UIComponent container, String componentId)
  {
    if (componentId.equals(container.getId())) {
      return container;
    }
    
    for (Iterator iter = container.getChildren().iterator(); iter.hasNext();) {
      UIComponent child = (UIComponent) iter.next();
      UIComponent result = doFindComponent(child, componentId);
      if (result != null) {
        return result;
      }
    }
    return null;
  }
  
}
