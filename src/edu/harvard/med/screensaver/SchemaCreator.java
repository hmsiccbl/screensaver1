// $HeadURL$
// $Id$
//
// Copyright © 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver;

import org.hibernate.cfg.Configuration;

/**
 * A Java program to create the schema in the database.
 * 
 * Note that due to the naming convention Hibernate uses for table constraints,
 * this program is not 100% compatible with the <tt>drop_schema.sql</tt>
 * script created by the <tt>ddl</tt> rule in the Ant <tt>build.xml</tt> file.
 * I've found that running the <tt>drop_schema.sql</tt> script twice in a
 * row will drop a database created by this program.
 * 
 * @author john sullivan
 */
public class SchemaCreator {
  
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
