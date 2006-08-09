// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import java.util.Iterator;
import java.util.Map;

import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.ui.util.Messages;

import org.apache.log4j.Logger;

/**
 * A base Controller class for JSF backing beans (beans that handle JSF actions
 * and events).
 * 
 * @author ant
 */
public abstract class AbstractController implements ScreensaverConstants
{
  
  // static data members
  
  private static Logger log = Logger.getLogger(AbstractController.class);

  
  // private data members
  
  private Messages _messages;


  // bean property methods
  
  /**
   * Get the application name (without version number).
   */
  public String getApplicationName()
  {
    return APPLICATION_NAME;
  }
  
  /**
   * Get the application version number, as a string.
   */
  public String getApplicationVersion()
  {
    return APPLICATION_VERSION;
  }
  
  /**
   * Get the application title as "[Application Name] [Version]".
   */
  public String getApplicationTitle()
  {
    return APPLICATION_TITLE;
  }
  
  /**
   * Get the group of messages that was injected into this backing bean.
   * 
   * @return messages the Messages
   */
  protected Messages getMessages() 
  {
    return _messages;
  }

  /**
   * Set the group of messages that can be accessed by this backing bean.
   * 
   * @param messages the Messages
   */
  public void setMessages(Messages messages) 
  {
    _messages = messages;
  }
  
  
  // public JSF convenience methods
  
  public FacesContext getFacesContext()
  {
    return FacesContext.getCurrentInstance();
  }
  
  public Application getApplicationContext()
  {
    return getFacesContext().getApplication();
  }
  
  public ExternalContext getExternalContext()
  {
    return getFacesContext().getExternalContext();
  }
  
  public Map getRequestMap()
  {
    return getExternalContext().getRequestMap();
  }
  
  public HttpSession getHttpSession()
  {
    Object httpSession = getExternalContext().getSession(false);
    if (httpSession == null) {
      return null;
    }
    assert httpSession instanceof HttpSession : "not running in an HTTP-based application server";
    return (HttpSession) httpSession;
  }
  
  public HttpServletRequest getHttpServletRequest()
  {
    Object request = getExternalContext().getRequest();
    if (request == null) {
      return null;
    }
    assert request instanceof HttpServletRequest : "not running in an Servlet-based application server";
    return (HttpServletRequest) request;
  }
  
  public HttpServletResponse getHttpServletResponse()
  {
    Object response = getExternalContext().getResponse();
    if (response == null) {
      return null;
    }
    assert response instanceof HttpServletResponse : "not running in an Servlet-based application server";
    return (HttpServletResponse) response;
  }
  
  public String getSessionDebugInfoString()
  {
    return "ID: " + getHttpSession().getId() + "\n" +
    "last accessed time: " + getHttpSession().getLastAccessedTime();
  }

//  // The "JSF way" to get a message (ignores Spring)
//  public String getMessage(String messageKey)
//  {
//    String text = null;
//    try {
//      ResourceBundle bundle =
//        // TODO: parameterize bundle name
//        ResourceBundle.getBundle("messages",
//                                 getFacesContext().getViewRoot().getLocale());
//      text = bundle.getString(messageKey);
//    } catch (Exception e) {
//      log.error("message key '" + messageKey + "' not found");
//      text = "???" + messageKey + "???";
//    }
//    return text;
//  } 


  /**
   * Adds the message of the specified key to the specified component. Any
   * request parameters that have a name of the form "<componentId>MessageParam*"
   * will be used to parameterize the messsage.
   * 
   * @param messageKey the key of the message to be shown
   * @param componentId the "simple" component ID, as specified in the "id"
   *          attribute of its defining JSF tag (not the fully-qualified client
   *          ID expression required by UIComponent.findComponent(), such as
   *          ":formId:subviewId:fieldId").
   * @return the FacesMessage that was set
   */
  public FacesMessage showMessage(String messageKey, String componentId)
  {
    return showMessage(messageKey, null, componentId);
  }
  
  /**
   * Adds the message of the specified key to the view (requires an h:messages
   * JSF element in the view).
   * 
   * @param messageKey the key of the message to be shown
   * @return the FacesMessage that was set
   */
  public FacesMessage showMessage(String messageKey)
  {
    return showMessage(messageKey, null, null);
  }
  
  /**
   * Adds the message of the specified key to the view (requires an h:messages
   * JSF element in the view).
   * 
   * @param messageKey the key of the message to be shown
   * @param messageArgs the args that will be used to parameterize this message
   *          (replacing the "{0}", ..., "{n}" placeholders in the message
   *          string)
   * @return the FacesMessage that was set
   */
  public FacesMessage showMessage(String messageKey, Object[] messageArgs)
  {
    return showMessage(messageKey, messageArgs, null);
  }
  
  /**
   * Adds the message of the specified key to the specified component. Any
   * request parameters that have a name of the form "<componentId>MessageParam*"
   * will be used to parameterize the messsage.
   * 
   * @param messageKey the key of the message to be shown
   * @param messageArgs the args that will be used to parameterize this message
   *          (replacing the "{0}", ..., "{n}" placeholders in the message string)
   * @param componentId the "simple" component ID, as specified in the "id"
   *          attribute of its defining JSF tag (not the fully-qualified client
   *          ID expression required by UIComponent.findComponent(), such as
   *          ":formId:subviewId:fieldId").
   * @return the FacesMessage that was set
   */
  public FacesMessage showMessage(
    String messageKey,
    Object[] messageArgs,
    String componentId)
  {
    // TODO: this parameterized message strategy isn't working
//  List<Object> messageParams = new ArrayList<Object>();
//  Map requestMap = getFacesContext().getExternalContext().getRequestMap();
//  for (Iterator iter = requestMap.keySet().iterator(); iter.hasNext();) {
//    String paramName = (String) iter.next();
//    log.debug("inspecting param " + paramName);
//    if (paramName.startsWith(componentId + "MessageParam")) {
//      log.debug("found param " + paramName);
//      Object paramValue = (Object) requestMap.get(paramName);
//      messageParams.add(paramValue);
//    }
//  }
    String clientId = getClientId(findComponent(componentId));
    FacesMessage msg = 
      _messages.setFacesMessageForComponent(messageKey, 
                                            messageArgs,
                                            clientId);
    if (msg == null) {
      log.error("no message exists for key '" + messageKey + "'");
    } 
    else {
      log.debug(msg.getDetail());
    }
    return msg;
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

  /**
   * Finds a JSF component within the current view, given only the "base" ID of
   * the component. Ambiguity is possible, so the caller should know whether the
   * component's base ID is unique within the view.
   * 
   * @param componentId the "base" ID of the component, which means it is the
   *          value as specified in the component's ID attribute of a JSP file;
   *          it does include the fully-qualified hierarchical path of parent
   *          components.
   * @return the (first) component found with the given ID
   */
  public UIComponent findComponent(String componentId)
  {
    return doFindComponent(getFacesContext().getViewRoot(), componentId);
  }
  
  /**
   * Finds a JSF component within the current view, rooted under the specified
   * parent, given only the "base" ID of the component. Ambiguity is possible,
   * so the caller should know whether the component's base ID is unique within
   * the view.
   * 
   * @param componentId the "base" ID of the component, which means it is the
   *          value as specified in the component's ID attribute of a JSP file;
   *          it does include the fully-qualified hierarchical path of parent
   *          components.
   * @param parentId the "base" ID of the parent (and not the fully-qualified ID
   *          of the parent)
   * @return the (first) component found with the given ID
   */
  public UIComponent findComponent(String componentId, String parentId)
  {
    UIComponent container = findComponent(parentId);
    return doFindComponent(container,
                           componentId);
  }
  
  /**
   * Report the provided error message via the appropriate communication
   * channels, which are currently: stacktrace to standard error, error-level
   * log message, and JSF view message (assumes an <code>h:messages</code>
   * component exists).
   * 
   * @param errorMesssage the error message to report
   */
  public void reportSystemError(String errorMessage)
  {
    showMessage("systemError",
                new Object[] {errorMessage});
    log.error(errorMessage);
  }

  /**
   * Report the provided Throwable via the appropriate communication channels,
   * which are currently: stacktrace to standard error, error-level log message,
   * and JSF view message (assumes an <code>h:messages</code> component
   * exists).
   * 
   * @param throwable the Throwable to report
   */
  public void reportSystemError(Throwable throwable)
  {
    throwable.printStackTrace();
    reportSystemError(throwable.getMessage());
  }
  
  
  // private methods
  
  private UIComponent doFindComponent(UIComponent container, String componentId)
  {
    if (componentId == null) {
      return null;
    }
       
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
