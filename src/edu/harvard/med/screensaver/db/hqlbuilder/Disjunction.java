// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.hqlbuilder;

import java.util.LinkedHashSet;

import edu.harvard.med.screensaver.util.StringUtils;

public class Disjunction extends CompositePredicate
{
  public String toHql()
  {
    LinkedHashSet<Predicate> uniqueOrderedClauses = getUniqueOrderedClauses();
    if (uniqueOrderedClauses.size() == 0) {
      return "";
    }
    if (uniqueOrderedClauses.size() == 1) {
      return uniqueOrderedClauses.iterator().next().toHql();
    }
    return "("+ StringUtils.makeListString(StringUtils.wrapStrings(getUniqueOrderedClauses(), "(", ")"), " or ") + ")";
  }
}