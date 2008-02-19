// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.hibernate;

import java.util.LinkedHashSet;

import edu.harvard.med.screensaver.util.StringUtils;

public class Disjunction extends CompositeClause
{
  public String toHql()
  {
    LinkedHashSet<Clause> uniqueOrderedClauses = getUniqueOrderedClauses();
    if (uniqueOrderedClauses.size() == 0) {
      return "";
    }
    if (uniqueOrderedClauses.size() == 1) {
      return uniqueOrderedClauses.iterator().next().toHql();
    }
    return "("+ StringUtils.makeListString(StringUtils.wrapStrings(getUniqueOrderedClauses(), "(", ")"), " or ") + ")";
  }
}