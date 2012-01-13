// $HeadURL$
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.datafetcher;

import junit.framework.TestCase;

import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.model.NonPersistentEntity;
import edu.harvard.med.screensaver.model.meta.Cardinality;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;

public class DataFetcherUtilTest extends TestCase
{
  private static class Grandchild extends NonPersistentEntity<Integer>
  {
    public static final RelationshipPath<Grandchild> child = RelationshipPath.from(Grandchild.class).to("child", Cardinality.TO_ONE);

    public Grandchild(Integer id)
    {
      super(id);
    }

    @Override
    public boolean isRestricted()
    {
      return false;
    }
  };

  private static class Child extends NonPersistentEntity<Integer>
  {
    public static final RelationshipPath<Child> parent = RelationshipPath.from(Child.class).to("parent", Cardinality.TO_ONE);

    public Child(Integer id)
    {
      super(id);
    }

    @Override
    public boolean isRestricted()
    {
      return false;
    }
  };

  private static class Parent extends NonPersistentEntity<Integer>
  {
    public Parent(Integer id)
    {
      super(id);
    }

    @Override
    public boolean isRestricted()
    {
      return false;
    }
  };

  public void testAddDomainRestrictionsForParent()
  {
    Parent parent = new Parent(1);
    HqlBuilder hql = new HqlBuilder();
    hql.from(Grandchild.class, "x");
    DataFetcherUtil.addDomainRestrictions(hql, RelationshipPath.from(Grandchild.class).to(Grandchild.child).to(Child.parent), parent, "x");
    assertEquals("from Grandchild x join x.child p1 join p1.parent p2 where p2=:arg0", hql.toHql());
  }
}
