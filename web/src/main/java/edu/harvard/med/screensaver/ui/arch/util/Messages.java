// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.faces.application.FacesMessage;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.springframework.context.MessageSource;

import edu.harvard.med.screensaver.util.Pair;

/**
 * Spring-related utility methods for use by the web user interface. Provides access to
 * messages. Queues message requests for display by the next rendered view, using {@link #getMessagesAndDequeue()}.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class Messages
{
  private static final long serialVersionUID = 7593034897039616028L;
  private static final Logger log = Logger.getLogger(Messages.class);
  private static final Object [] EMPTY_ARGS = new Object[] {};
  private static final String DEFAULT_MESSAGE = "<UNKNOWN MESSAGE>";
  

  private MessageSource _messageSource;
  // TODO: parameterize via Spring bean definition
  private Locale _locale = Locale.ENGLISH;
  private List<Pair<String,FacesMessage>> queuedFacesMessages;
  

  /**
   * Attach a <code>FacesMessage</code> to a specific UI component, based on the
   * <code>message.properties</code> resource.
   *
   * @param messageKey the message key
   * @param clientId the client ID of the UI component
   * @param args arguments to the message
   * @return the <code>FacesMessage</code>
   */
  public FacesMessage setFacesMessageForComponent(String messageKey,
                                                  String clientId,
                                                  Object... args)
  {
    FacesMessage message = getFacesMessage(messageKey, args);
    assert message != null : "expected non-null FacesMessage";
    enqueueMessage(clientId, message);
    return message;
  }

  public void enqueueMessage(String clientId, FacesMessage message)
  {
    getQueuedMessages().add(new Pair<String,FacesMessage>(clientId, message));
    log.info("enqueued user message: " + message.getSummary());
  }

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

  public List<Pair<String,FacesMessage>> getQueuedMessages()
  {
    if (queuedFacesMessages == null) {
      queuedFacesMessages = Lists.newArrayList();
    }
    return queuedFacesMessages;
  }

  public List<FacesMessage> getMessagesAndDequeue()
  {
    ArrayList<FacesMessage> facesMessages = Lists.newArrayList(Iterables.transform(getQueuedMessages(), Pair.<String,FacesMessage>toSecond()));
    getQueuedMessages().clear();
    return facesMessages;
  }

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
      message = "???"+ messageKey +"???";
    }
    // if detail is null, FacesMessage repeats summary when asked for detail! :(
    return new FacesMessage(severity, message, "");
  }
  
  /**
   * Create and return a formatted message based on the <code>message.properties</code>
   * resource.
   *
   * @param messageKey the message key
   * @param args arguments to the message
   */
  public String getMessage(String messageKey, Object... args)
  {
    return _messageSource.getMessage(messageKey, args, "???" + messageKey +
                                                       "???", _locale);
  }
}
