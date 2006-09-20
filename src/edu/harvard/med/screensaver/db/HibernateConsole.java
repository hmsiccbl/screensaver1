// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.log4j.Logger;
import org.springframework.orm.hibernate3.HibernateTemplate;

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
    try {
      CommandLineApplication app = new CommandLineApplication(args);
      if (!app.processOptions(true, true)) {
        System.exit(1);
      }
      do {
        System.out.println("Enter HQL query (blank to quit): ");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        HibernateTemplate hib = (HibernateTemplate) app.getSpringBean("hibernateTemplate");
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
  }

}
