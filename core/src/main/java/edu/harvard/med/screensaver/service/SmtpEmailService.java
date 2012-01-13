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
import java.util.Properties;
import java.util.Set;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.google.common.collect.Sets;
import org.apache.commons.cli.OptionBuilder;
import org.apache.log4j.Logger;

/**
 * SMTP EmailService implementation
 * TODO: rework this class using a builder patter for creation of the message (to better handle multipart messages).
 */
public class SmtpEmailService implements EmailService
{
  private static Logger log = Logger.getLogger(SmtpEmailService.class);

  // member variables used if instantiated as a service
  private String host;
  private String username;
  private String password;
  private boolean useSmtps;
  private InternetAddress [] replyTos;

  public SmtpEmailService(String host, String username, String replyTos) throws AddressException
  {
    this(host,username, replyTos, null,false);
  }  
  
  /**
   * @motivation for Spring instantiation
   * @param host mail server host i.e. smtp.cl.med.harvard.edu
   * @param username mail account name
   * @param password password, can be blank if unnecessary
   * @throws AddressException 
   */
  public SmtpEmailService(String host, String username, String replyTos, String password) throws AddressException
  {
    this(host,username, replyTos, password,false);
  }

  /**
   * @param useSmtps in order to use smtp.gmail.com (for testing)
   * @throws AddressException 
   */
  public SmtpEmailService(String host, String username, String replyTos, String password, boolean useSmtps) throws AddressException
  {
    this.host = host;
    this.username = username;
    this.password = password;
    this.useSmtps = useSmtps;
    if(replyTos != null)
    {
      String[] sreplyTos = replyTos.split(",");
      Set<InternetAddress> set = Sets.newHashSet();
      for(String s:sreplyTos) {
        set.add(new InternetAddress(s));
      }
      this.replyTos = set.toArray(new InternetAddress[] {} );
    }
  }
  
  public void send(String subject,
                       String message,
                       InternetAddress from,
                       InternetAddress[] recipients,
                       InternetAddress[] cclist) throws MessagingException
  {
    try {
      send(subject, message, from, recipients, cclist, null);
    }
    catch (IOException e) {
      throw new RuntimeException("should not happen as there is no file attachment", e);
    }
  }

  public void send(String subject,
                       String message,
                       InternetAddress from,
                       InternetAddress[] recipients,
                       InternetAddress[] cclist,
                       File attachedFile) throws MessagingException, IOException
  {
    sendMessage(subject, message, from, recipients, cclist, attachedFile);
  }

  public void send(String subject,
                   String message,
                   String from,
                   String[] recipients,
                   String[] cclist) throws MessagingException
  {
    try {
      send(subject, message, from, recipients, cclist, null);
    }
    catch (IOException e) {
      throw new RuntimeException("should not happen as there is no file attachment", e);
    }
  }
  
  public void send(String subject,
                   String message,
                   String from,
                   String[] recipients,
                   String[] cclist,
                   File attachedFile) throws MessagingException, IOException
  {
    sendMessage(subject, message, from, recipients, cclist, attachedFile);
  }

  /**
   * Convert string email addresses to proper InternetAddress
   * 
   * @throws IOException
   */
  private void sendMessage(
                     String subject,
                     String message,
                     String from,
                     String[] srecipients,
                           String[] scclist,
                           File attachedFile
                     )
                       throws MessagingException, IOException
  {
    InternetAddress[] recipients = new InternetAddress[srecipients.length];
    int i = 0;
    for(String r:srecipients)
    {
      recipients[i++] = new InternetAddress(r);

    }      
    InternetAddress[] cclist = null;
    i = 0;
    if(scclist != null)
    {
      cclist = new InternetAddress[scclist.length];
      for(String r:scclist)
      {
        cclist[i++]= new InternetAddress(r);
      }
    }
    sendMessage(subject, message, new InternetAddress(from), recipients, cclist, attachedFile);
  }
  
  private void sendMessage(
                     String subject,
                     String message,
                     InternetAddress from,
                     InternetAddress[] recipients,
                           InternetAddress[] cclist,
                           File attachedFile
                     )
                       throws MessagingException, IOException
  {
    log.info("try to send: " +
      printEmail(subject, message, from, this.replyTos, recipients, cclist, (attachedFile == null ? "" : "" + attachedFile.getCanonicalFile())));
    log.info("host: " + host + ", useSMTPS: " + useSmtps);
    Properties props = new Properties();
    String protocol = "smtp";
    if(useSmtps)  // need smtps to test with gmail
    {
      props.put("mail.smtps.auth", "true");
      protocol = "smtps";
    }
    Session session = Session.getDefaultInstance(props, null);
    Transport t = session.getTransport(protocol);

    try {
      MimeMessage msg = new MimeMessage(session);
      if(this.replyTos != null) msg.setReplyTo(replyTos);
      msg.setFrom(from);  
      msg.setSubject(subject);

      if (attachedFile != null) {
        setFileAsAttachment(msg, message, attachedFile);
      }
      else {
        msg.setContent(message, "text/plain");
      }

      msg.addRecipients(Message.RecipientType.TO, recipients );
      if (cclist != null) msg.addRecipients(Message.RecipientType.CC, cclist);

      t.connect(host, username, password);
      t.sendMessage(msg, msg.getAllRecipients());
    }
    finally {
      t.close();
    }
    log.info("sent: " +
      printEmailHeader(subject, from, this.replyTos, recipients, cclist, (attachedFile == null ? "" : "" + attachedFile.getCanonicalFile())));
  }

    // Set a file as an attachment.  Uses JAF FileDataSource.
    // from http://en.wikipedia.org/wiki/Javamail
  private static void setFileAsAttachment(Message msg, String message, File file)
    throws MessagingException
  {
    MimeBodyPart p1 = new MimeBodyPart();
    p1.setText(message);

    // Create second part
    MimeBodyPart p2 = new MimeBodyPart();

    // Put a file in the second part
    FileDataSource fds = new FileDataSource(file);
    p2.setDataHandler(new DataHandler(fds));
    p2.setFileName(fds.getName());

    // Create the Multipart.  Add BodyParts to it.
    Multipart mp = new MimeMultipart();
    mp.addBodyPart(p1);
    mp.addBodyPart(p2);

    // Set Multipart as the message's content
    msg.setContent(mp);
    }

  
  //    // Set a single part html content.
  //    // Sending data of any type is similar.
  //    // from http://en.wikipedia.org/wiki/Javamail
  //  private static void setHTMLContent(Message msg) throws MessagingException
  //  {
  //
  //        String html = "<html><head><title>" +
  //                        msg.getSubject() +
  //                        "</title></head><body><h1>" +
  //                        msg.getSubject() +
  //                        "</h1><p>This is a test of sending an HTML e-mail" +
  //                        " through Java.</body></html>";
  //
  //        // HTMLDataSource is an inner class
  //        msg.setDataHandler(new DataHandler(new HTMLDataSource(html)));
  //    }
  //
  //    /*
  //     * Inner class to act as a JAF datasource to send HTML e-mail content
  //     */
  //    static class HTMLDataSource implements DataSource {
  //        private String html;
  //
  //        public HTMLDataSource(String htmlString) {
  //            html = htmlString;
  //        }
  //
  //        // Return html string in an InputStream.
  //        // A new stream must be returned each time.
  //        public InputStream getInputStream() throws IOException {
  //            if (html == null) throw new IOException("Null HTML");
  //            return new ByteArrayInputStream(html.getBytes());
  //        }
  //
  //        public OutputStream getOutputStream() throws IOException {
  //            throw new IOException("This DataHandler cannot write HTML");
  //        }
  //
  //        public String getContentType() {
  //            return "text/html";
  //        }
  //
  //        public String getName() {
  //            return "JAF text/html dataSource to send e-mail only";
  //        }
  //    }

  /**
   * For logging
   */
  public static String printEmail(String subject,
                                  String message,
                                  InternetAddress from,
                                  InternetAddress[] replyTos,
                                  InternetAddress[] recipients,
                                  InternetAddress[] cclist, String attachedFilePath)
  {
    return printEmailHeader(subject, from, replyTos, recipients, cclist, attachedFilePath)
             + "\n========message========\n" + message;
  }

  public static String printEmailHeader(String subject,
                                        InternetAddress from,
                                        InternetAddress[] replyTos,
                                        InternetAddress[] recipients,
                                        InternetAddress[] cclist,
                                        String attachedFilePath)
  {
    return "\nSubject: " + subject
             + "\nFrom: " + from
             + "\nReplyTo's: " + (replyTos == null ? "" : com.google.common.base.Joiner.on(',').join(replyTos))
             + "\nTo: " + com.google.common.base.Joiner.on(',').join(recipients)
             + "\nCC: " + (cclist == null ? "" : com.google.common.base.Joiner.on(',').join(cclist))
               + "\nAttached File: " + attachedFilePath;
  }
}