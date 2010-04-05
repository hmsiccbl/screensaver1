// $HeadURL: http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/iccbl/1.9.0-dev/src/edu/harvard/med/screensaver/ui/util/Messages.java $
// $Id: Messages.java 2962 2009-02-06 22:38:16Z seanderickson1 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service;

import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.context.MessageSource;

/**
 * Spring-related utility methods for use by the Services layer. Provides access to
 * localized messages. 
 */
public class ServiceMessages
{
  private static final long serialVersionUID = 7593034897039616028L;
  
  private MessageSource _messageSource;
  // TODO: parameterize via Spring bean definition
  private Locale _locale = Locale.ENGLISH;
  
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
