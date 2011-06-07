// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.view;

import java.awt.Color;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.ScreensaverProperties;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.CurrentScreensaverUser;
import edu.harvard.med.screensaver.ui.arch.util.Messages;
import edu.harvard.med.screensaver.ui.arch.util.servlet.ScreensaverServletFilter;

/**
 * A base class for JSF backing beans. A backing bean is responsible for
 * managing UI state via bean properties, providing methods to access the
 * underlying domain model entities, and providing methods to handle JSF actions
 * (command invocation) and events. This base class provides a grab bag of
 * convenience methods for
 * <ul>
 * <li>application information (name,version)</li>
 * <li>accessing servlet session and request state</li>
 * <li>accessing JSF state (FacesContext) and current view's UI components</li>
 * <li>reporting application and system errors back to the user</li>
 * <li>obtaining internationalized, parameterized message strings</li>
 * <li>accessing the current user and his/her roles and privileges</li>
 * <li>closing the current HTTP session</li>
 * </ul>
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public abstract class AbstractBackingBean implements ScreensaverConstants
{
  private static Logger log = Logger.getLogger(AbstractBackingBean.class);

  private ScreensaverProperties _applicationProperties;
  private Messages _messages;
  private CurrentScreensaverUser _currentScreensaverUser;

  private Map<String,Boolean> _isPanelCollapsedMap = Maps.newHashMap();

  /**
   * Get the application name (without version number).
   */
  public String getApplicationName()
  {
    return _applicationProperties.getProperty(APPLICATION_NAME_PROPERTY);
  }

  /**
   * Get the application version number, as a string.
   */
  public String getApplicationVersion()
  {
    return _applicationProperties.getVersion();
  }

  /**
   * Get the application title as "[Application Name] [Version]".
   */
  public String getApplicationTitle()
  {
    return getApplicationName() + " " + getApplicationVersion();
  }

  public Color getScreensaverThemeColor() 
  { 
    return SCREENSAVER_THEME_COLOR;
    //return _applicationProperties.getProperty(SCREENSAVER_THEME_COLOR_PROPERTY);
  } 

  public Color getScreensaverThemeHeaderColor() 
  { 
    return HEADER_COLOR;
    //return _applicationProperties.getProperty(HEADER_COLOR_PROPERTY); 
  } 
  
  public ScreensaverProperties getApplicationProperties()
  {
    return _applicationProperties;
  }

  public void setApplicationProperties(ScreensaverProperties applicationProperties)
  {
    _applicationProperties = applicationProperties;
  }

  /**
   * Get the group of messages that was injected into this backing bean.
   *
   * @return messages the Messages
   */
  public Messages getMessages()
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

  /**
   * Get whether user can view any data in the current view.
   *
   * @return <code>true</code> iff user can view any data in the current view
   */
  public boolean isReadAdmin()
  {
    return isUserInRole(ScreensaverUserRole.READ_EVERYTHING_ADMIN);
  }

  public boolean isScreener()
  {
    return getScreensaverUser() instanceof ScreeningRoomUser;
  }

  // TODO: consider moving to the Login Bean
  public boolean isAuthenticatedUser()
  {
    Boolean pendingSessionCloseRequest = (Boolean)
      getHttpSession().getAttribute(ScreensaverServletFilter.CLOSE_HTTP_SESSION);
    return getExternalContext().getUserPrincipal() != null && (pendingSessionCloseRequest == null || pendingSessionCloseRequest.equals(Boolean.FALSE));
  }

  /**
   * Returns the ScreensaverUser entity representing the user that is logged in
   * to the current HTTP session.
   *
   * @return the ScreensaverUser that is logged in to the current HTTP session
   */
  public ScreensaverUser getScreensaverUser()
  {
    return _currentScreensaverUser.getScreensaverUser();
  }

  /**
   * Set the CurrentScreensaverUser.
   *
   * @param currentScreensaverUser
   */
  public void setCurrentScreensaverUser(CurrentScreensaverUser currentScreensaverUser)
  {
    _currentScreensaverUser = currentScreensaverUser;
  }

  /**
   * Get the CurrentScreensaverUser.
   *
   * @return a CurrentScreensaverUser
   */
  public CurrentScreensaverUser getCurrentScreensaverUser()
  {
    return _currentScreensaverUser;
  }

  protected FacesContext getFacesContext()
  {
    return FacesContext.getCurrentInstance();
  }

  protected Application getApplicationContext()
  {
    return getFacesContext() == null ? null : getFacesContext().getApplication();
  }

  protected ExternalContext getExternalContext()
  {
    return getFacesContext() == null ? null : getFacesContext().getExternalContext();
  }

  protected Map getRequestMap()
  {
    return getExternalContext().getRequestMap();
  }

  protected Map getRequestParameterMap()
  {
    return getExternalContext().getRequestParameterMap();
  }

  protected Object getRequestParameter(String parameterName)
  {
    return getRequestParameterMap().get(parameterName);
  }

  protected HttpSession getHttpSession()
  {
    Object httpSession = getExternalContext() == null ? null : getExternalContext().getSession(false);
    if (httpSession == null) {
      log.warn("using MockHttpSession");
      httpSession = new MockHttpSession();
    }
    assert httpSession instanceof HttpSession : "not running in an HTTP-based application server";
    return (HttpSession) httpSession;
  }

  protected HttpServletRequest getHttpServletRequest()
  {
    Object request = getExternalContext().getRequest();
    if (request == null) {
      log.warn("using MockHttpServletRequest");
      request = new MockHttpServletRequest();
    }
    assert request instanceof HttpServletRequest : "not running in an Servlet-based application server";
    return (HttpServletRequest) request;
  }

  protected HttpServletResponse getHttpServletResponse()
  {
    Object response = getExternalContext().getResponse();
    if (response == null) {
      log.warn("using MockHttpServletResponset");
      response = new MockHttpServletResponse();
    }
    assert response instanceof HttpServletResponse : "not running in an Servlet-based application server";
    return (HttpServletResponse) response;
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

  public String getMessage(String messageKey, Object... messageArgs)
  {
    return _messages.getMessage(messageKey, messageArgs);
  }


  /**
   * Adds the message of the specified key to the component specified by the
   * localId. (requires a <pre> &lt;h:message for=&quot;localId&quot;/&gt;</pre> 
   * JSF element in the view)
   * 
   * @param facesContext
   * @param localId the "simple" component ID, as specified in the "id"
   *          attribute of its defining JSF tag (not the fully-qualified client
   *          ID expression required by UIComponent.findComponent(), such as
   *          ":formId:subviewId:fieldId"). If the specified component cannot be
   *          found then the messages will be sent to the generalized message
   *          list, see {@link #showMessage(String, Object...)}
   * @param msgKey the key to the message resource in the
   *          <code>messages</code> bean.
   * @param msgArgs arguments for the message resource
   * @return null if the component can not be found
   * @see Messages
   */
  protected FacesMessage showMessageForLocalComponentId(FacesContext facesContext,
                                                        String localId,
                                                        String msgKey,
                                                        Object... msgArgs)
  {
    // UIComponent c = getFacesContext().getViewRoot().findComponent(localID); this didn't work
    UIComponent component = null;
    UIViewRoot viewRoot = facesContext.getViewRoot();
    component = org.apache.myfaces.custom.util.ComponentUtils.findComponentById(getFacesContext(),
                                                                                viewRoot,
                                                                                localId);
    if (component != null) {
      String clientId = component.getClientId(getFacesContext());
      return showMessageForComponent(msgKey, clientId, msgArgs);
    }
    else {
      log.warn("Error creating messages: " + msgKey + ": " +
               Arrays.asList(msgArgs) + ", could not find the component: " +
               localId);
      showMessage(msgKey, msgArgs);
      return null;
    }
  }


  /**
   * Adds the message of the specified key to the view (requires an h:messages
   * JSF element in the view).
   * 
   * @param messageKey the key of the message to be shown
   * @return the FacesMessage that was set
   */
  protected FacesMessage showMessage(String messageKey)
  {
    return showMessage(messageKey, new Object[] {});
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
  protected FacesMessage showMessage(String messageKey, Object... messageArgs)
  {
    return _messages.setFacesMessageForComponent(messageKey, null, messageArgs);
  }

  /**
   * Adds the message of the specified key to the specified component. Any
   * request parameters that have a name of the form
   * "<componentId>MessageParam*" will be used to parameterize the messsage.
   * 
   * @param messageKey the key of the message to be shown
   * @param messageArgs the args that will be used to parameterize this message
   *          (replacing the "{0}", ..., "{n}" placeholders in the message
   *          string)
   * @param componentId the fully-qualified client ID expression required by
   *          UIComponent.findComponent(), such as ":formId:subviewId:fieldId".
   *          To give only the local name, i.e."fieldId", see
   *          {@link #showMessageForLocalComponentId(FacesContext, String, String, Object...)}
   * @return the FacesMessage that was set
   */
  protected FacesMessage showMessageForComponent(String messageKey,
                                                 String componentId,
                                                 Object... messageArgs)
  {
    return _messages.setFacesMessageForComponent(messageKey,
                                                 componentId,
                                                 messageArgs);
  }

  /**
   * Report the provided application error message to the user. An application
   * error is one that a developer would not be concerned about, and that
   * occurred due to so-called "user error".
   * 
   * @deprecated use {@link #showMessage(String)} instead
   */
  @Deprecated
  protected void reportApplicationError(String errorMessage)
  {
    showMessage("applicationError", errorMessage);
  }

  protected void reportApplicationError(String errorMessage, Throwable t)
  {
    log.error(errorMessage, t);
    showMessage("applicationError", errorMessage);
  }

  protected void closeHttpSession()
  {
    log.debug("requesting close of HTTP session");
    getHttpSession().setAttribute(ScreensaverServletFilter.CLOSE_HTTP_SESSION,
                                  Boolean.TRUE);
  }

  /**
   * A convenience method for determining if the current user is in a particular
   * role (with role type safety!)
   *
   * @param role
   * @return true iff the user is in the specified role
   */
  protected boolean isUserInRole(ScreensaverUserRole role)
  {
    return getCurrentScreensaverUser().getScreensaverUser().isUserInRole(role);
  }

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

  public Map<String,Boolean> getIsPanelCollapsedMap()
  {
    return _isPanelCollapsedMap;
  }
}
