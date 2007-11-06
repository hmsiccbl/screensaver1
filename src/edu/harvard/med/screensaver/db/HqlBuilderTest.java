// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import edu.harvard.med.screensaver.model.screens.Screen;

public class HqlBuilderTest extends TestCase
{
  public void testHqlBuilder()
  {
    HqlBuilder hb = new HqlBuilder();
    hb.select("s", "screenNumber").from(Screen.class, "s").from("s", "screenResult", "sr").from("sr", "resultValueTypes", "rvt").where("sr", "shareable", true).where("rvt", "name", "Positive").orderBy("s", "screenNumber");
    assertEquals("hql text", "select s.screenNumber from Screen s join s.screenResult sr join sr.resultValueTypes rvt where sr.shareable=? and rvt.name=? order by s.screenNumber",
                 hb.hql());
    List<Object> expectedArgs = new ArrayList<Object>();
    expectedArgs.add(Boolean.TRUE);
    expectedArgs.add("Positive");
    assertEquals("args", expectedArgs, hb.args());
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
      new HqlBuilder().select("t", "screenNumber").from(Screen.class, "s").hql();
      fail("expected exception for undefined select alias");
    }
    catch (Exception e) {}
  }

}
