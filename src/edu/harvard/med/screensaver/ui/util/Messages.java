// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.util;

import java.util.Locale;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.springframework.context.MessageSource;

/**
 * Spring-related utility methods for use by the web user interface. For one,
 * this class provides access to localized messages.
 * 
 * @author ant
 */
public class Messages
{
  private static final Object[] EMPTY_ARGS = new Object[] {};
  private static Logger log = Logger.getLogger(Messages.class);
  private static String DEFAULT_MESSAGE = "<UNKNOWN MESSAGE>";
  
  private MessageSource _messageSource;
  // TODO: parameterize via Spring bean definition
  private Locale _locale = Locale.ENGLISH;

  /**
   * TODO
   * @param messageKey
   * @param args
   * @return
   */
  public FacesMessage getFacesMessage(String messageKey,
                                      Object[] args) {
    String message = _messageSource.getMessage(messageKey,
                                               args,
                                               DEFAULT_MESSAGE,
                                               _locale);
    if (message.equals(DEFAULT_MESSAGE)) {
      log.error("message not found for key '" + messageKey + "'");
    }
    return new FacesMessage(message);
  }
  
  /**
   * @param messageKey
   * @param args
   * @param componentId
   */
  public FacesMessage setFacesMessageForComponent(String messageKey,
                                                  Object[] args,
                                                  String componentId)
  {
    FacesMessage msg = getFacesMessage(messageKey, args);
    FacesContext.getCurrentInstance().addMessage(componentId,
                                                 msg);
    return msg;
  }
  
  public FacesMessage setFacesMessageForComponent(String messageKey,
                                          String componentId) {
    return setFacesMessageForComponent(messageKey,
                                       EMPTY_ARGS,
                                       componentId);
  }

  
  /* Property methods */

  public Locale getLocale() {
    return _locale;
  }

  public void setLocale(Locale locale) {
    _locale = locale;
  }

  public MessageSource getMessageSource() {
    return _messageSource;
  }

  public void setMessageSource(MessageSource messageSource) {
    _messageSource = messageSource;
  }

}
