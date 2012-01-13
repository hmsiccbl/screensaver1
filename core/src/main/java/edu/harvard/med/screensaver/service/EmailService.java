// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service;

import java.io.File;
import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

public interface EmailService
{
  public static final String DELIMITER = ",";

  public void send(String subject,
                   String message,
                   InternetAddress from,
                   InternetAddress[] recipients,
                   InternetAddress[] cclist) throws MessagingException;

  public void send(String subject,
                   String message,
                   InternetAddress from,
                   InternetAddress[] recipients,
                   InternetAddress[] cclist,
                   File attachedFile) throws MessagingException, IOException;

}
