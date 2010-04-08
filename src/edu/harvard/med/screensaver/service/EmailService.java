// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

public interface EmailService
{
  public static final String DELIMITER = ",";

  //  public void send(String subject,
  //                   String message,
  //                   String from,
  //                   String[] recipients,
  //                   String[] cclist) throws MessagingException;
  public void send(String subject,
                   String message,
                   InternetAddress from,
                   InternetAddress[] recipients,
                   InternetAddress[] cclist) throws MessagingException;


}
