// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.hqlbuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 * A command-line application for issuing HQL queries to a Screensaver database.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class HibernateConsole 
{
  // static members

  private static Logger log = Logger.getLogger(HibernateConsole.class);

  /**
   * @param args
   */
  @SuppressWarnings("unchecked")
  public static void main(String[] args)
  {
    BufferedReader br = null;
    try {
      CommandLineApplication app = new CommandLineApplication(args);
      if (!app.processOptions(true, true)) {
        System.exit(1);
      }
      br = new BufferedReader(new InputStreamReader(System.in));
      HibernateTemplate hib = (HibernateTemplate) app.getSpringBean("hibernateTemplate");
      do {
        System.out.println("Enter HQL query (blank to quit): ");
        String input = br.readLine();
        if (input.length() == 0) {
          System.out.println("Goodbye!");
          System.exit(0);
        }
        try {
          List list = hib.find(input);
          System.out.println("Result:");
          for (Iterator iter = list.iterator(); iter.hasNext();) {
            Object item = iter.next();
            // format output from multi-item selects ("select a, b, c, ... from ...")
            if (item instanceof Object[]) {
              List<Object> fields = Arrays.asList((Object[]) item);
              System.out.println(StringUtils.makeListString(fields, ", "));
            }
            // format output from single-item selected ("select a from ..." or "from ...")
            else {
              System.out.println("[" + item.getClass().getName() + "]: " + item);
            }
          }
          System.out.println("(" + list.size() + " rows)\n");
        }
        catch (Exception e) {
          System.out.println("Hibernate Error: " + e.getMessage());
        }
        System.out.println();
      } while (true);
    }
    catch (Exception e) {
      System.err.println("Fatal Error: " + e.getMessage());
      e.printStackTrace();
    }
    finally {
      IOUtils.closeQuietly(br);
    }      
  }

}
