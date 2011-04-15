// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import com.google.common.collect.Lists;
import junit.framework.TestCase;

import edu.harvard.med.screensaver.model.meta.Cardinality;
import edu.harvard.med.screensaver.model.meta.PropertyNameAndValue;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;

public class PropertyPathTest extends TestCase
{
  public void testZeroElements()
  {
    PropertyPath<Parent> propertyPath = RelationshipPath.from(Parent.class).toProperty("name");
    assertEquals(Parent.class, propertyPath.getRootEntityClass());
    assertEquals("size", 0, propertyPath.getPathLength());
    assertEquals("<Parent>.name", propertyPath.toString());
    assertEquals("name", propertyPath.getPath());
    assertEquals("name", propertyPath.getPropertyName());
    assertEquals("<Parent>", propertyPath.getAncestryPath().toString());
    assertEquals("", propertyPath.getAncestryPath().getPath());
    assertEquals("", propertyPath.getLeaf());
    assertFalse(propertyPath.pathIterator().hasNext());
  }

  public void testOneElement()
  {
    PropertyPath<Parent> propertyPath = RelationshipPath.from(Parent.class).to("child").toProperty("name");
    assertEquals(Parent.class, propertyPath.getRootEntityClass());
    assertEquals("size", 1, propertyPath.getPathLength());
    assertEquals("<Parent>.child.name", propertyPath.toString());
    assertEquals("child.name", propertyPath.getPath());
    assertEquals("name", propertyPath.getPropertyName());
    assertEquals("<Parent>.child", propertyPath.getAncestryPath().toString());
    assertEquals("child", propertyPath.getAncestryPath().getPath());
    assertEquals("<Parent>", propertyPath.getAncestryPath().getAncestryPath().toString());
    assertEquals("", propertyPath.getAncestryPath().getAncestryPath().getPath());
    assertNull(propertyPath.getAncestryPath().getAncestryPath().getAncestryPath());
    assertEquals("child", propertyPath.getLeaf());
    assertEquals(Lists.newArrayList("child"), Lists.newArrayList(propertyPath.pathIterator()));
  }

  public void testMultipleElements()
  {
    PropertyPath propertyPath = RelationshipPath.from(Parent.class).to("child").to("sibling").toProperty("name");
    assertEquals("size", 2, propertyPath.getPathLength());
    assertEquals("<Parent>.child.sibling.name", propertyPath.toString());
    assertEquals("child.sibling", propertyPath.getAncestryPath().getPath());
    assertEquals("<Parent>.child.sibling", propertyPath.getAncestryPath().toString());
    assertEquals("child", propertyPath.getAncestryPath().getAncestryPath().getPath());
    assertEquals("<Parent>.child", propertyPath.getAncestryPath().getAncestryPath().toString());
    assertEquals("sibling", propertyPath.getLeaf());
    assertEquals(Lists.newArrayList("child", "sibling"),
                 Lists.newArrayList(propertyPath.pathIterator()));
  }

  public void testMultipleElementsWithRestrictions()
  {
    Child sibling = new Child(1);
    Toy toy = new Toy(2);
    PropertyPath propertyPath = 
      RelationshipPath.from(Parent.class).to("child").to("siblings").restrict("id", sibling.getEntityId()).
      to("toys").restrict("id", toy.getEntityId()).toProperty("name");
    assertEquals("size", 3, propertyPath.getPathLength());
    assertEquals("<Parent>.child.siblings[id=1].toys[id=2].name", propertyPath.toString());
    assertEquals(new PropertyNameAndValue("id", Integer.valueOf(2)), propertyPath.getLeafRestriction());
    assertEquals("child.siblings.toys.name", propertyPath.getPath());
    assertEquals("<Parent>.child.siblings[id=1].toys[id=2]", propertyPath.getAncestryPath().toString());
    assertEquals(new PropertyNameAndValue("id", Integer.valueOf(2)), propertyPath.getLeafRestriction());
    assertEquals("child.siblings.toys", propertyPath.getAncestryPath().getPath());
    assertEquals("<Parent>.child.siblings[id=1]", propertyPath.getAncestryPath().getAncestryPath().toString());
    assertEquals(new PropertyNameAndValue("id", Integer.valueOf(1)), propertyPath.getAncestryPath().getAncestryPath().getLeafRestriction());
    assertEquals("child.siblings", propertyPath.getAncestryPath().getAncestryPath().getPath());
    assertEquals("toys", propertyPath.getLeaf());
    assertEquals(Lists.newArrayList("child", "siblings", "toys"),
                 Lists.newArrayList(propertyPath.pathIterator()));
    assertEquals("name", propertyPath.getPropertyName());
  }

  public void testEquality()
  {
    Child sibling = new Child(1);
    Toy toy1 = new Toy(2);
    Toy toy2 = new Toy(3);
    PropertyPath propertyPath1a = RelationshipPath.from(Parent.class).to("child").to("siblings").restrict("id", sibling.getEntityId()).to("toys").restrict("id", toy1.getEntityId()).toProperty("name");
    PropertyPath propertyPath1b = RelationshipPath.from(Parent.class).to("child").to("siblings").restrict("id", sibling.getEntityId()).to("toys").restrict("id", toy1.getEntityId()).toProperty("name");
    PropertyPath propertyPath1c = RelationshipPath.from(Parent.class).to("child").to("siblings").restrict("id", sibling.getEntityId()).to("toys").restrict("id", toy1.getEntityId()).toProperty("size");
    PropertyPath propertyPath2 = RelationshipPath.from(Parent.class).to("child").to("siblings").restrict("id", sibling.getEntityId()).to("toys").restrict("id", toy2.getEntityId()).toProperty("name");
    RelationshipPath relPath3 = RelationshipPath.from(Parent.class).to("child").to("siblings").restrict("id", sibling.getEntityId()).to("toys").restrict("id", toy1.getEntityId());

    assertEquals(propertyPath1a, propertyPath1b);
    assertEquals(propertyPath1a.hashCode(), propertyPath1b.hashCode());

    assertFalse(propertyPath1a.equals(propertyPath1c));
    assertFalse(propertyPath1a.hashCode() == propertyPath1c.hashCode());

    assertFalse(propertyPath1a.equals(propertyPath2));
    assertFalse(propertyPath1a.hashCode() == propertyPath2.hashCode());

    assertEquals(propertyPath1a.getAncestryPath(), relPath3);
  }
  
  public void testHasRestrictions()
  {
    assertTrue("has restrictions", RelationshipPath.from(Parent.class).to("child").to("siblings").restrict("id", 1).hasRestrictions());
    assertTrue("has restrictions", RelationshipPath.from(Parent.class).to("child").restrict("id", 1).to("siblings").restrict("id", 1).hasRestrictions());
    assertTrue("has restrictions", RelationshipPath.from(Parent.class).to("child").restrict("id", 1).to("siblings").hasRestrictions());
    assertFalse("has restrictions", RelationshipPath.from(Parent.class).to("child").to("siblings").hasRestrictions());
  }
  
  public void testUnrestrictedPathConversion()
  {
    assertFalse(RelationshipPath.from(Parent.class).to("child").restrict("id", 1).to("siblings").restrict("id", 1).getUnrestrictedPath().hasRestrictions());
    assertEquals(RelationshipPath.from(Parent.class).to("child").to("siblings"),
                 RelationshipPath.from(Parent.class).to("child").restrict("id", 1).to("siblings").restrict("id", 1).getUnrestrictedPath());

    assertFalse(RelationshipPath.from(Parent.class).to("child").restrict("id", 1).to("siblings").restrict("id", 1).toProperty("property").getUnrestrictedPath().hasRestrictions());
    assertEquals(RelationshipPath.from(Parent.class).to("child").to("siblings").toProperty("property"),
                 RelationshipPath.from(Parent.class).to("child").restrict("id", 1).to("siblings").restrict("id", 1).toProperty("property").getUnrestrictedPath());
  }

  /**
   * Test that PropertyPath allows an empty string for the property name value,
   * since this usage is required for collections of elements, which have no
   * properties other than their immediate value (as opposed to collections of
   * entities, which are comprised of one or more properties).
   */
  public void testCollectionOfValuesUsage()
  {
    PropertyPath propertyPath1 = RelationshipPath.from(Parent.class).to("child").to("siblings").toCollectionOfValues("nicknames");
    assertEquals("child.siblings.nicknames", propertyPath1.getPath());
    assertEquals("nicknames", propertyPath1.getPropertyName());
    
    PropertyPath propertyPath2 = RelationshipPath.from(Parent.class).to("child").to("siblings").toCollectionOfValues("nicknames");
    assertEquals(propertyPath1, propertyPath2);
  }
  
  public void testAppend()
  {
    PropertyPath<Parent> relationship2 = RelationshipPath.from(Parent.class).to("grandchild").restrict("age", Integer.valueOf(1)).to("greatgrandchild").toProperty("name");
    PropertyPath<Parent> relationship = RelationshipPath.from(Parent.class).to("child").to(relationship2);
    assertEquals(3, relationship.getPathLength());
    assertEquals("child.grandchild.greatgrandchild.name", relationship.getPath());
    assertEquals("<Parent>.child.grandchild[age=1].greatgrandchild.name", relationship.toString());
  }

  public void testCardinality()
  {
    assertEquals(Cardinality.TO_ONE, RelationshipPath.from(Parent.class).to("parent", Cardinality.TO_ONE).getCardinality());
    assertEquals(Cardinality.TO_ONE, RelationshipPath.from(Parent.class).to("parent", Cardinality.TO_ONE).to(RelationshipPath.from(Parent.class).to("parent", Cardinality.TO_ONE)).getCardinality());
    assertEquals(Cardinality.TO_ONE, RelationshipPath.from(Parent.class).to("parent", Cardinality.TO_ONE).toProperty("value").getCardinality());
    assertEquals(Cardinality.TO_MANY, RelationshipPath.from(Parent.class).to("parent", Cardinality.TO_ONE).to(RelationshipPath.from(Parent.class).toCollectionOfValues("items")).getCardinality());
    assertEquals(Cardinality.TO_MANY, RelationshipPath.from(Parent.class).to("children", Cardinality.TO_MANY).getCardinality());
    assertEquals(Cardinality.TO_MANY, RelationshipPath.from(Parent.class).to("children", Cardinality.TO_MANY).to(RelationshipPath.from(Child.class).to("children", Cardinality.TO_MANY)).getCardinality());
    assertEquals(Cardinality.TO_MANY, RelationshipPath.from(Parent.class).to("children", Cardinality.TO_MANY).to(RelationshipPath.from(Child.class).to("children", Cardinality.TO_MANY)).to(RelationshipPath.from(Child.class).to("parent", Cardinality.TO_ONE)).getCardinality());
    assertEquals(Cardinality.TO_MANY, RelationshipPath.from(Parent.class).to("children", Cardinality.TO_MANY).toProperty("value").getCardinality());
    assertEquals(Cardinality.TO_ONE, RelationshipPath.from(Parent.class).to("children", Cardinality.TO_MANY).restrict("name", "Joe").getCardinality());
    assertEquals(Cardinality.TO_MANY, RelationshipPath.from(Parent.class).to("children", Cardinality.TO_MANY).restrict("name", "Sam").to(RelationshipPath.from(Child.class).to("children", Cardinality.TO_MANY)).getCardinality());
  }

  public void testInversePath()
  {
    PropertyPath<Parent> propertyPath = RelationshipPath.from(Parent.class).to("child", Child.class, "parent", Cardinality.TO_MANY).toProperty("name");
    assertEquals(Lists.newArrayList("parent"),
                 Lists.newArrayList(propertyPath.inversePathIterator()));
    assertEquals(Lists.newArrayList(Child.class),
                 Lists.newArrayList(propertyPath.entityClassIterator()));
  }

}

class Parent extends AbstractEntity<Integer>
{
  private static final long serialVersionUID = 1L;

  public Parent(int id)
  {
    setEntityId(id);
  }

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return null;
  }
}

class Child extends AbstractEntity<Integer>
{
  private static final long serialVersionUID = 1L;

  public Child(int id)
  {
    setEntityId(id);
  }

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return null;
  }
}

class Toy extends AbstractEntity<Integer>
{
  private static final long serialVersionUID = 1L;

  public Toy(int id)
  {
    setEntityId(id);
  }

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return null;
  }
}
