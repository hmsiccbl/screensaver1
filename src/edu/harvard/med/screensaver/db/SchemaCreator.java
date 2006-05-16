// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.ImprovedNamingStrategy;

/**
 * A Java program to create the schema in the database.
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
    configuration.setNamingStrategy(ImprovedNamingStrategy.INSTANCE);
    configuration.configure(CONFIG_FILE_LOCATION);
    configuration.setProperty("hibernate.hbm2ddl.auto", "create");
    configuration.buildSessionFactory();
  }
}
