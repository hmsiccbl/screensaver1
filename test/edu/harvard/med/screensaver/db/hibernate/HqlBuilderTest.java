// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.hibernate;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.ui.table.Criterion.Operator;

public class HqlBuilderTest extends TestCase
{
  public void testBasicUsage()
  {
    HqlBuilder hb = new HqlBuilder();
    hb.select("s", "screenNumber").
    from(Screen.class, "s").
    from("s", "screenResult", "sr", JoinType.INNER).
    from("sr", "resultValueTypes", "rvt", JoinType.INNER).
    where("sr", "shareable", Operator.EQUAL, true).
    where("rvt", "name", Operator.EQUAL, "Positive").
    orderBy("s", "screenNumber");
    assertEquals("hql text", "select s.screenNumber from Screen s join s.screenResult sr join sr.resultValueTypes rvt where sr.shareable=:arg0 and rvt.name=:arg1 order by s.screenNumber",
                 hb.toHql());
    Map<String,Object> expectedArgs = new HashMap<String,Object>();
    expectedArgs.put("arg0", Boolean.TRUE);
    expectedArgs.put("arg1", "Positive");
    assertEquals("args", expectedArgs, hb.args());
  }

  public void testWhereRestrictionBuilder()
  {
    HqlBuilder hb = new HqlBuilder();
    hb.select("w", "id").select("rvt", "id").select("rv", "value");
    hb.from(Well.class, "w");
    hb.from("w", "resultValues", "rv", JoinType.LEFT_FETCH);
    hb.from("rv", "resultValueType", "rvt");
    Disjunction orClause = hb.disjunction();
    Conjunction and1 = hb.conjunction();
    Conjunction and2 = hb.conjunction();
    and1.add(hb.predicate("rvt", Operator.EQUAL, null)).
         add(hb.predicate("rv.value", Operator.GREATER_THAN, new Double(1.0)));
    and2.add(hb.predicate("rvt", Operator.EQUAL, null)).
         add(hb.predicate("rv.value", Operator.LESS_THAN, new Double(-1.0)));
    orClause.add(and1).add(and2);
    hb.where(orClause);
    assertEquals("select w.id, rvt.id, rv.value " +
    		"from Well w left join fetch w.resultValues rv left join rv.resultValueType rvt " +
    		"where ((rvt=:arg0 and rv.value>:arg1) or (rvt=:arg2 and rv.value<:arg3))",
    		hb.toHql());
  }

  public void testAliasReuse()
  {
    try {
      new HqlBuilder().from(Screen.class, "s").from("s", "screenResult", "s");
      fail("expected exception for alias reuse");
    }
    catch (Exception e) {}
  }

  public void testUndefinedSelectAlias()
  {

    try {
      new HqlBuilder().select("t", "screenNumber").from(Screen.class, "s").toHql();
      fail("expected exception for undefined select alias");
    }
    catch (Exception e) {}
  }

  public void testFromWith()
  {
    HqlBuilder hb = new HqlBuilder();
    hb.from(Well.class, "w");
    hb.from("w", "resultValues", "rv", JoinType.LEFT);
    hb.restrictFrom("rv", "resultValueType.id", Operator.EQUAL, 1);
    hb.where(hb.predicate("rv.value", Operator.GREATER_THAN, 5.0));
    assertEquals("from Well w left join w.resultValues rv with rv.resultValueType.id=:arg0 where rv.value>:arg1",
                 hb.toHql());
  }
}