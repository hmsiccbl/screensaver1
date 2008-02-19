// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.hibernate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class HqlUtils
{
  // static members

  private static Logger log = Logger.getLogger(HqlUtils.class);


  public static <T> Map<T,String> makeAliases(List<T> relationships)
  {
    int nextAlias = 1;
    Map<T,String> path2Alias = new HashMap<T,String>();
    for (T relationship : relationships) {
      if (!path2Alias.containsKey(relationship)) {
        path2Alias.put(relationship, "x" + nextAlias++);
      }
    }
    return path2Alias;
  }

  /**
   * Returns an ordered set of the relationships, expanded to include all
   * implicit, intermediate relationships. For example, if input is { "w", "x.y.z", },
   * output will be { "w", "x", "x.y", "x.y.z" }.
   */
  public static List<String> expandRelationships(String... relationships)
  {
    LinkedHashSet<String> expandedRelationships = new LinkedHashSet<String>();
    for (String relationship : relationships) {
      int pos = -1;
      do {
        pos = relationship.indexOf('.', pos + 1);
        if (pos < 0) {
          expandedRelationships.add(relationship);
        }
        else if (pos > 0 && pos < relationship.length()) {
          expandedRelationships.add(relationship.substring(0, pos));
        }
      } while (pos >= 0);
    }
     return new ArrayList<String>(expandedRelationships);
  }

  // instance data members

  // public constructors and methods

  // private methods

}
