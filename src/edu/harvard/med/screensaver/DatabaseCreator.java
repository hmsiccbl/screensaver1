// DatabaseCreator.java
// by john sullivan 2006.05

package edu.harvard.med.screensaver;

import org.hibernate.cfg.Configuration;

/**
 * A Java program to create the schema in the database.
 * 
 * Note that due to the naming convention Hibernate uses for table constraints,
 * this program is not 100% compatible with the <tt>drop_database.sql</tt>
 * script created by the <tt>ddl</tt> rule in the Ant <tt>build.xml</tt> file.
 * I've found that running the <tt>drop_database.sql</tt> script twice in a
 * row will drop a database created by this program.
 * 
 * @author john sullivan
 */
public class DatabaseCreator {
  
  /** 
   * Location of hibernate.cfg.xml file.
   */
  private static String CONFIG_FILE_LOCATION = "/hibernate.cfg.xml";

  /**
   * Create the schema in the database.
   * @param args Unused
   */
  public static void main(String[] args) {
    Configuration configuration = new Configuration();
    configuration.configure(CONFIG_FILE_LOCATION);
    configuration.setProperty("hibernate.hbm2ddl.auto", "create");
    configuration.buildSessionFactory();
  }
}
