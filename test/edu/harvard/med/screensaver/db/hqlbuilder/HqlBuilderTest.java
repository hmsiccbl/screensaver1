// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.hqlbuilder;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import edu.harvard.med.screensaver.db.hqlbuilder.Conjunction;
import edu.harvard.med.screensaver.db.hqlbuilder.Disjunction;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.db.hqlbuilder.JoinType;
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
    from("sr", "dataColumns", "col", JoinType.INNER).
    where("col", "numeric", Operator.EQUAL, true).
    where("col", "name", Operator.EQUAL, "Positive").
    orderBy("s", "screenNumber");
    assertEquals("hql text", "select s.screenNumber from Screen s join s.screenResult sr join sr.dataColumns col where col.numeric=:arg0 and col.name=:arg1 order by s.screenNumber",
                 hb.toHql());
    Map<String,Object> expectedArgs = new HashMap<String,Object>();
    expectedArgs.put("arg0", Boolean.TRUE);
    expectedArgs.put("arg1", "Positive");
    assertEquals("args", expectedArgs, hb.args());
  }

  public void testWhereRestrictionBuilder()
  {
    HqlBuilder hb = new HqlBuilder();
    hb.select("w", "id").select("col", "id").select("rv", "value");
    hb.from(Well.class, "w");
    hb.from("w", "resultValues", "rv", JoinType.LEFT_FETCH);
    hb.from("rv", "dataColumn", "col");
    Disjunction orClause = hb.disjunction();
    Conjunction and1 = hb.conjunction();
    Conjunction and2 = hb.conjunction();
    and1.add(hb.simplePredicate("col", Operator.EQUAL, null)).
         add(hb.simplePredicate("rv.value", Operator.GREATER_THAN, new Double(1.0)));
    and2.add(hb.simplePredicate("col", Operator.EQUAL, null)).
         add(hb.simplePredicate("rv.value", Operator.LESS_THAN, new Double(-1.0)));
    orClause.add(and1).add(and2);
    hb.where(orClause);
    assertEquals("select w.id, col.id, rv.value " +
    		"from Well w left join fetch w.resultValues rv left join rv.dataColumn col " +
    		"where ((col=:arg0 and rv.value>:arg1) or (col=:arg2 and rv.value<:arg3))",
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
    hb.restrictFrom("rv", "dataColumn.id", Operator.EQUAL, 1);
    hb.where(hb.simplePredicate("rv.value", Operator.GREATER_THAN, 5.0));
    assertEquals("from Well w left join w.resultValues rv with rv.dataColumn.id=:arg0 where rv.value>:arg1",
                 hb.toHql());
  }
  
  public void testGroupBy()
  {
    HqlBuilder hb = new HqlBuilder();
    hb.from(Well.class, "w").select("w", "id").select("w", "wellName").select("w", "plateNumber").groupBy("w", "id").orderBy("w", "plateNumber");
    assertEquals("select w.id, w.wellName, w.plateNumber from Well w group by w.plateNumber, w.id order by w.plateNumber",
                 hb.toHql());
  }
  
  public void testHaving()
  {
    HqlBuilder hb = new HqlBuilder();
    hb.from(Well.class, "w").select("w", "plateNumber").selectExpression("count(*)").groupBy("w", "plateNumber").having(hb.simplePredicate("count(*)", Operator.EQUAL, 384));
    assertEquals("select w.plateNumber, count(*) from Well w group by w.plateNumber having count(*)=:arg0",
                 hb.toHql());
  }
}
