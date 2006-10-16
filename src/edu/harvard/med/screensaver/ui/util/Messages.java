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

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.springframework.context.MessageSource;

import edu.harvard.med.screensaver.util.Pair;

/**
 * Spring-related utility methods for use by the web user interface. For one,
 * this class provides access to localized messages.
 * 
 * @author ant, s
 */
public class Messages
{
  
  // private static things
  
  private static final Object[] EMPTY_ARGS = new Object[] {};
  private static Logger log = Logger.getLogger(Messages.class);
  private static String DEFAULT_MESSAGE = "<UNKNOWN MESSAGE>";
  
  
  // private instance fields
  
  private MessageSource _messageSource;
  // TODO: parameterize via Spring bean definition
  private Locale _locale = Locale.ENGLISH;
  private List<Pair<String,FacesMessage>> _queuedFacesMessages =
    new ArrayList<Pair<String,FacesMessage>>();
  
  
  // public application methods
  
  // this is a bit of a hack: i return String but i dont really return anything. i am just here
  // to unqueue the queued messages. i currently need to be loaded in a t:outputText, or something
  // similar, to work
  public String getQueuedMessages()
  {
    for (Pair<String,FacesMessage> queuedFacesMessage : _queuedFacesMessages) {
      String clientId = queuedFacesMessage.getFirst();
      FacesMessage message = queuedFacesMessage.getSecond();
      FacesContext.getCurrentInstance().addMessage(clientId, message);
    }
    _queuedFacesMessages = new ArrayList<Pair<String,FacesMessage>>();
    return null;
  }

  public FacesMessage setFacesMessageForComponent(
    String messageKey,
    String clientId)
  {
    return setFacesMessageForComponent(messageKey, EMPTY_ARGS, clientId);
  }
  
  public FacesMessage setFacesMessageForComponent(
    String messageKey,
    Object[] args,
    String clientId)
  {
    FacesMessage message = getFacesMessage(messageKey, args);
    assert message != null : "expected non-null FacesMessage";
    _queuedFacesMessages.add(new Pair<String,FacesMessage>(clientId, message));
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

  
  // private instance methods
  
  FacesMessage getFacesMessage(String messageKey, Object[] args)
  {
    return getFacesMessage(FacesMessage.SEVERITY_ERROR, messageKey, args);
  }

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
