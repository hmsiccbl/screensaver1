// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.context.MessageSource;

import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.util.Pair;

/**
 * Spring-related utility methods for use by the web user interface. Provides access to
 * localized messages. Queues message requests, and adds messages to the <code>FacesContext</code>
 * during the response rendering phase.
 * 
 * <p>
 * 
 * The idea for queueing messages, and using a phase listener to add them during response
 * rendering, is due to public-domain code by <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 * found on the web, as well as to comments from other posters to the <a
 * href="http://forum.java.sun.com/thread.jspa?threadID=523001&messageID=2619083">same thread
 * where I found Jesse's code</a>. Thanks Jesse!
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class Messages extends AbstractBackingBean implements PhaseListener
{
  
  // private static fields
  
  private static final long serialVersionUID = 7593034897039616028L;
  private static final Logger log = Logger.getLogger(Messages.class);
  private static final Object [] EMPTY_ARGS = new Object[] {};
  private static final String DEFAULT_MESSAGE = "<UNKNOWN MESSAGE>";
  private static final String sessionToken = "QUEUED_FACES_MESSAGES";
  
  
  // private instance fields
  
  private MessageSource _messageSource;
  // TODO: parameterize via Spring bean definition
  private Locale _locale = Locale.ENGLISH;
  
  
  // public application methods

  /**
   * Attach a <code>FacesMessage</code> to a specific UI component, based on the
   * <code>message.properties</code> resource.
   *
   * @param messageKey the message key
   * @param clientId the client ID of the UI component
   * @param args arguments to the message
   * @return the <code>FacesMessage</code>
   */
  public FacesMessage setFacesMessageForComponent(
    String messageKey,
    String clientId,
    Object... args)
  {
    HttpSession session = (HttpSession) getFacesContext().getExternalContext().getSession(false);
    return setFacesMessageForComponent(session, messageKey, clientId, args);
  }


  /**
   * Attach a <code>FacesMessage</code> to a specific UI component, based on
   * the <code>message.properties</code> resource. This method can be called
   * outside of the JSF context, as long as the caller can provide an
   * HttpSession object (e.g. from a servlet filter).
   * 
   * @param session the HttpSession
   * @param messageKey the message key
   * @param clientId the client ID of the UI component
   * @param args arguments to the message
   * @return the <code>FacesMessage</code>
   */
  @SuppressWarnings("unchecked")
  public FacesMessage setFacesMessageForComponent(HttpSession session,
                                                  String messageKey,
                                                  String clientId,
                                                  Object... args)
  {

    List<Pair<String,FacesMessage>> queuedFacesMessages = (List<Pair<String,FacesMessage>>) session.getAttribute(sessionToken);
    if (queuedFacesMessages == null) {
      queuedFacesMessages = new ArrayList<Pair<String,FacesMessage>>();
      session.setAttribute(sessionToken, queuedFacesMessages);
    }
    FacesMessage message = getFacesMessage(messageKey, args);
    assert message != null : "expected non-null FacesMessage";
    queuedFacesMessages.add(new Pair<String,FacesMessage>(clientId, message));
    return message;
  }

  
  // public property methods

  public Locale getLocale()
  {
    return _locale;
  }

  public void setLocale(Locale locale)
  {
    _locale = locale;
  }

  public MessageSource getMessageSource()
  {
    return _messageSource;
  }

  public void setMessageSource(MessageSource messageSource) 
  {
    _messageSource = messageSource;
  }
  
  /**
   * @see FacesContext#renderResponse()
   */
  public void renderResponse() 
  {
    FacesContext.getCurrentInstance().renderResponse();
  }

  
  // public PhaseListener implementation methods
  
  /**
   * Return the identifier of the request processing phase during which this
   * listener is interested in processing PhaseEvent events.
   * <p>
   * In our case, we are only interested in the response rendering phase.
   */ 
  public PhaseId getPhaseId()
  {
    return PhaseId.RENDER_RESPONSE;
  }

  /**
   * Handle a notification that the processing for a particular phase of the
   * request processing lifecycle is about to begin.
   * <p>
   * In our case, we want to retrieve all the <code>FacesMessages</code> that have been stored
   * with the session, and add them to the current <code>FacesContext</code>.
   */ 
  @SuppressWarnings("unchecked")
  public void beforePhase(PhaseEvent event)
  {
    Map sessionMap = getFacesContext().getExternalContext().getSessionMap();
    List<Pair<String,FacesMessage>> queuedFacesMessages = 
      (List<Pair<String,FacesMessage>>) sessionMap.remove(sessionToken);
    if (queuedFacesMessages == null) {
      return;
    }
    FacesContext facesContext = getFacesContext();
    for (Pair<String,FacesMessage> queuedFacesMessage : queuedFacesMessages) {
      String clientId = queuedFacesMessage.getFirst();
      FacesMessage message = queuedFacesMessage.getSecond();
      facesContext.addMessage(clientId, message);
    }
  }

  /**
   * Handle a notification that the processing for a particular phase has just
   * been completed.
   * <p>
   * In our case, do nothing. We only need to do work <i>before</i> the response rendering
   * phase.
   */
  public void afterPhase(PhaseEvent event)
  {
  }
  
  
  // package-private and private instance methods
  
  /**
   * Create and return a <code>FacesMessage</code> based on the <code>message.properties</code>
   * resource.
   *  
   * @param messageKey the message key
   * @param args arguments to the message
   * @return the <code>FacesMessage</code>
   */
  FacesMessage getFacesMessage(String messageKey, Object[] args)
  {
    return getFacesMessage(FacesMessage.SEVERITY_ERROR, messageKey, args);
  }

  /**
   * Create and return a <code>FacesMessage</code> based on the <code>message.properties</code>
   * resource.
   *
   * @param severity the severity of the message
   * @param messageKey the message key
   * @param args arguments to the message
   * @return the <code>FacesMessage</code>
   */
  private FacesMessage getFacesMessage(
    FacesMessage.Severity severity,
    String messageKey,
    Object[] args)
  {
    String message = _messageSource.getMessage(messageKey, args, DEFAULT_MESSAGE, _locale);
    if (message.equals(DEFAULT_MESSAGE)) {
      log.error("message not found for key '" + messageKey + "'");
    }
    // if detail is null, FacesMessage repeats summary when asked for detail! :(
    return new FacesMessage(severity, message, "");
  }
}
