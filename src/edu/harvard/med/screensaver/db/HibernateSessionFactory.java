// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.ImprovedNamingStrategy;


/**
 * Configures and provides access to Hibernate sessions, tied to the
 * current thread of execution.  Follows the Thread Local Session
 * pattern, see {@link http://hibernate.org/42.html}.
 */
public class HibernateSessionFactory
{
  
  // static fields
  
  /** Location of hibernate.cfg.xml file. */
  private static String CONFIG_FILE_LOCATION = "/hibernate.cfg.xml";

  /** Holds a single instance of Session */
  private static final ThreadLocal<Session> threadLocal =
    new ThreadLocal<Session>();

  /** The single instance of hibernate configuration */
  private static final Configuration configuration = buildConfiguration();

  /** The single instance of hibernate SessionFactory */
  private static org.hibernate.SessionFactory sessionFactory;

  
  // static methods
  
  /**
   * Returns the ThreadLocal Session instance.  Lazy initialize
   * the <code>SessionFactory</code> if needed.
   *
   *  @return Session
   *  @throws HibernateException
   */
  public static Session currentSession() throws HibernateException {
    Session session = threadLocal.get();
    if (session == null || ! session.isOpen()) {
      if (sessionFactory == null) {
        try {
          configuration.configure(CONFIG_FILE_LOCATION);
          sessionFactory = configuration.buildSessionFactory();
        }
        catch (Exception e) {
          System.err.println("%%%% Error Creating SessionFactory %%%%");
          e.printStackTrace();
        }
      }
      session = (sessionFactory != null) ? sessionFactory.openSession() : null;
      threadLocal.set(session);
    }
    return session;
  }

  /**
   *  Close the single hibernate session instance.
   *  @throws HibernateException
   */
  public static void closeSession() throws HibernateException {
    Session session = (Session) threadLocal.get();
    threadLocal.set(null);
    if (session != null) {
      session.close();
    }
  }

  private static Configuration buildConfiguration() {
    Configuration configuration = new Configuration();
    configuration.setNamingStrategy(ImprovedNamingStrategy.INSTANCE);
    return configuration;
  }
  
  
  // private constructor
  
  /**
   * Default constructor.
   * @motivation The default no-arg constructor is private to maintain the
   *             singleton pattern by preventing others from instantiating
   *             objects of this type.
   */
  private HibernateSessionFactory() {
  }
}
