// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.harvard.med.screensaver.BuildNumber;
import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.aspects.UIControllerMethodExceptionHandlerAspect;
import edu.harvard.med.screensaver.ui.util.Messages;
import edu.harvard.med.screensaver.ui.util.ScreensaverServletFilter;

import org.apache.log4j.Logger;

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

  // static data members

  private static Logger log = Logger.getLogger(AbstractBackingBean.class);


  // private data members

  private Properties _applicationProperties;
  private Messages _messages;
  private CurrentScreensaverUser _currentScreensaverUser;

  // bean property methods

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
      return _applicationProperties.getProperty(APPLICATION_VERSION_PROPERTY);
  }

  /**
   * Get the application version number, as a string.
   */
  public String getApplicationBuildNumber()
  {
    return BuildNumber.getBuildNumber();
  }

  /**
   * Get the application title as "[Application Name] [Version]".
   */
  public String getApplicationTitle()
  {
    return getApplicationName() + " " + getApplicationVersion();
  }

  /**
   * Get the URL to which user feedback can be submitted.
   */
  public String getFeedbackUrl()
  {
    return _applicationProperties.getProperty(FEEDBACK_URL_PROPERTY);
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
  
  public Properties getApplicationProperties()
  {
    return _applicationProperties;
  }

  public void setApplicationProperties(Properties applicationProperties)
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
   * Override this method to indicate whether the current user is allowed to
   * perform editing operations on the data in the view. Components in the JSF
   * view can call this method/property via the JSF EL, to set component
   * attributes, such as 'displayValueOnly', 'rendered', 'readonly', 'disables',
   * etc.
   *
   * @motivation the Tomahawk components have 'enabledOnUserRole' and
   *             'visibleOnUserRole' attributes, which are convenient, but 1)
   *             'enabledOnUserRole' shows a grayed-out component, rather than a
   *             nice plain text value, as does 'displayValueOnly'; 2) cannot
   *             handle the case where the visibility/enabling of a component is
   *             controlled by the user *not* being in a particular role (e.g.
   *             user is not an admin of any type)
   * @return true iff the view is read-only for the current user, based upon the
   *         user's roles. Defaults to false, unless a subclass overrides this
   *         method.
   * @see #isUserInRole(ScreensaverUserRole)
   */
//  public boolean isReadOnly()
//  {
//    ScreensaverUserRole editableAdminRole = getEditableAdminRole();
//    if (editableAdminRole == null) {
//      return true;
//    }
//    return !isUserInRole(editableAdminRole);
//  }
//
//  public boolean isEditable()
//  {
//    return !isReadOnly();
//  }
//
//  /**
//   * Controller subclasses for viewers should override this method and return
//   * the ScreensaverUserRole that is allowed to edit the data contents of the
//   * viewer.
//   */
//  protected ScreensaverUserRole getEditableAdminRole()
//  {
//    return null;
//  }

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
    return isUserInRole(ScreensaverUserRole.SCREENER);
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

  /**
   * Override this method to allow the backing bean to recover from an operation
   * that threw an exception that requires reloading of the data. The
   * implementation must guarantee (directly or indirectly) that a new
   * transaction will be started in which to perform the reload, even if an
   * existing "outer" transaction is in effect (which may be in rollback-only
   * mode); otherwise infinite recursion may occur, if the reload attempt is
   * being called by {@link UIControllerMethodExceptionHandlerAspect} in
   * response to a persistence-related exception.
   */
  public String reload()
  {
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  public Map<String,String> getEscapeBackslashes()
  {
    return _backslashEscaper;
  }

  /**
   * Workaround for JSF suckiness. Two things: first, I need to use the
   * returning a Map trick to get around the problem that JSF EL doesn't allow
   * parameterized methods. Second, I gotta escape the backslashes in the
   * f:param, since the JSF EL is evaluating that backslash as an escape
   * character somewhere.
   */
  private final Map<String,String> _backslashEscaper = new HashMap<String,String>() {
    private static final long serialVersionUID = 1L;

    public String get(Object key)
    {
      if (key instanceof String) {
        return ((String) key).replace("\\", "\\\\");
      }
      else {
        return key.toString();
      }
    }
  };


  // protected methods


  protected FacesContext getFacesContext()
  {
    return FacesContext.getCurrentInstance();
  }

  protected Application getApplicationContext()
  {
    return getFacesContext().getApplication();
  }

  protected ExternalContext getExternalContext()
  {
    return getFacesContext().getExternalContext();
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
    Object httpSession = getExternalContext().getSession(false);
    if (httpSession == null) {
      return null;
    }
    assert httpSession instanceof HttpSession : "not running in an HTTP-based application server";
    return (HttpSession) httpSession;
  }

  protected HttpServletRequest getHttpServletRequest()
  {
    Object request = getExternalContext().getRequest();
    if (request == null) {
      return null;
    }
    assert request instanceof HttpServletRequest : "not running in an Servlet-based application server";
    return (HttpServletRequest) request;
  }

  protected HttpServletResponse getHttpServletResponse()
  {
    Object response = getExternalContext().getResponse();
    if (response == null) {
      return null;
    }
    assert response instanceof HttpServletResponse : "not running in an Servlet-based application server";
    return (HttpServletResponse) response;
  }

  protected String getSessionDebugInfoString()
  {
    return "ID: " + getHttpSession().getId() + "\n" +
    "last accessed time: " + getHttpSession().getLastAccessedTime();
  }

  /**
   * Acquire a JSF bean (e.g. a controller) or a Spring bean by name. Spring
   * beans are accessible thanks to
   * org.springframework.web.jsf.DelegatingVariableResolver, which must be
   * configured for use in faces-config.xml. <i>In general, you should <b>not</b> need
   * to call this method. Use JSF or Spring injection mechanisms to make the
   * required beans available to your class!</i>
   *
   * @return the bean of the given name, or <code>null</code> if no such named
   *         bean exists
   */
  protected Object getBean(String beanName)
  {
    log.warn("you are using AbstractBackingBean.getBean() to acquire bean " + beanName + "; please reconsider your use of this method!  Use injection!");
    return getFacesContext().getApplication().getVariableResolver().resolveVariable(getFacesContext(),
                                                                                    beanName);
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
   * @param messageKey the key to the message resource in the
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
   * Returns the fully-qualified "client" ID of the component, which can be used to
   * @param component
   */
  protected String getClientId(UIComponent component)
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
  protected UIComponent findComponent(String componentId)
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
  protected UIComponent findComponent(String componentId, String parentId)
  {
    UIComponent container = findComponent(parentId);
    return doFindComponent(container, componentId);
  }

  /**
   * Report the provided system error message via the appropriate communication
   * channels, which are currently: error-level log message, and JSF view
   * message (assumes an <code>h:messages</code> component exists). System
   * errors are errors that developers should be concerned about.
   *
   * @param errorMessage the error message to report
   * @see #reportApplicationError(String)
   */
  protected void reportSystemError(String errorMessage)
  {
    showMessage("systemError", errorMessage);
    log.error(errorMessage);
  }

  /**
   * Report the provided Throwable via the appropriate communication channels,
   * which are currently: stacktrace to standard error, error-level log message,
   * and JSF view message (assumes an <code>h:messages</code> component
   * exists). System errors are errors that developers should be concerned
   * about.
   *
   * @param throwable the Throwable to report
   * @see #reportApplicationError(String)
   */
  protected void reportSystemError(Throwable throwable)
  {
    throwable.printStackTrace();
    showMessage("systemError", throwable.toString());
    log.error(throwable.toString());
  }

  /**
   * Report the provided application error message to the user. An application
   * error is one that a developer would not be concerned about, and that
   * occurred due to so-called "user error".
   *
   * @param errorMessage the error message to report
   * @see #reportSystemError(String)
   * @see #reportSystemError(Throwable)
   */
  protected void reportApplicationError(String errorMessage)
  {
    showMessage("applicationError", errorMessage);
  }

  protected void reportApplicationError(String errorMessage, Throwable t)
  {
    log.error(errorMessage, t);
    showMessage("applicationError", errorMessage);
  }

  /**
   * @deprecated prefer the method {@link AbstractBackingBean#reportApplicationError(String, Throwable)}
   */
  protected void reportApplicationError(Throwable throwable)
  {
    throwable.printStackTrace();
    showMessage("applicationError", throwable.getMessage());
    log.error(throwable.toString());
  }

  protected void closeHttpSession()
  {
    log.debug("requesting close of HTTP session");
    getHttpSession().setAttribute(ScreensaverServletFilter.CLOSE_HTTP_SESSION,
                                  Boolean.TRUE);
  }

  /**
   * Each JSF component maintains "local" state, which is in addition to the
   * state that is maintained by the application's model. This local state is
   * used during the JSF validation phase, and eventually is copied to the
   * application's model state later during the JSF Update Model phase. This
   * local state is used to repopulate the controls when a view is re-rendered
   * (after an initial visit). I believe this local state also corresponds to
   * more than just the value, and, where applicable, includes other rendering
   * state such as item selections, scroll bar state, etc. Sometimes a view will
   * want to redisplay itself in a completely re-initialized state, where the
   * values of the components are freshly read from the model, and all other
   * rendering state. Calling this method will cause the view to be recreated;
   * the existing view's component tree, and thus any state stored in that tree,
   * will be thrown away.
   *
   * @param renderResponseImmediately true, if you want
   *          FacesContext.renderResponse() to be called after the view has been
   *          recreated. Set to false if you intend to modify the new view after
   *          this method call (e.g., setting a message, or updating some of the
   *          component's values manually).
   * @motivation Components that hang onto Hibernate proxy objects (e.g. lazy
   *             instantiation collections), may become invalid after their
   *             parent entity is reloaded in response to a concurrent
   *             modification exception. We must recreate the view's component
   *             tree to avoid accessing these now-invalid HIbernate proxy
   *             objects.
   */
  protected void recreateView(boolean renderResponseImmediately)
  {
    FacesContext facesCtx = getFacesContext();
    log.debug("recreating JSF view ID " + facesCtx.getViewRoot().getViewId());
    ViewHandler viewHandler = getApplicationContext().getViewHandler();
    UIViewRoot recreatedViewRoot = viewHandler.createView(facesCtx,
                                                          facesCtx.getViewRoot().getViewId());
    facesCtx.setViewRoot(recreatedViewRoot);
    if (renderResponseImmediately) {
      facesCtx.renderResponse();
    }
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
