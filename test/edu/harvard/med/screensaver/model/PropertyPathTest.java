// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.io.Serializable;

import junit.framework.TestCase;

import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.meta.RelationshipPath.RelationshipPathIterator;

public class PropertyPathTest extends TestCase
{
  private static class DummyEntity extends AbstractEntity<Integer> {

    private static final long serialVersionUID = 1L;

    public DummyEntity(int id)
    {
      setEntityId(id);
    }

    @Override
    public Object acceptVisitor(AbstractEntityVisitor visitor)
    {
      return null;
    }
  }

  public void testZeroElementPath()
  {
    PropertyPath propertyPath = new PropertyPath<AbstractEntity>(AbstractEntity.class, "name");
    assertEquals(propertyPath.getRootEntityClass(), AbstractEntity.class);
    assertEquals("size", 0, propertyPath.getPathLength());
    assertEquals("<AbstractEntity>.name", propertyPath.toString());
    assertEquals("name", propertyPath.getPath());
    assertEquals("name", propertyPath.getPropertyName());
    assertEquals("<AbstractEntity>", propertyPath.getAncestryPath().toString());
    assertEquals("", propertyPath.getAncestryPath().getPath());
    assertEquals("", propertyPath.getLeaf());
  }

  public void testOneElementPath()
  {
    PropertyPath<AbstractEntity> propertyPath = new RelationshipPath<AbstractEntity>(AbstractEntity.class, "child").toProperty("name");
    assertEquals(propertyPath.getRootEntityClass(), AbstractEntity.class);
    assertEquals("size", 1, propertyPath.getPathLength());
    assertEquals("<AbstractEntity>.child.name", propertyPath.toString());
    assertEquals("child.name", propertyPath.getPath());
    assertEquals("name", propertyPath.getPropertyName());
    assertEquals("<AbstractEntity>", propertyPath.getAncestryPath().toString());
    assertEquals("", propertyPath.getAncestryPath().getPath());
    assertEquals("child", propertyPath.getLeaf());
  }

  public void testMultipleElementsPath()
  {
    PropertyPath propertyPath = new RelationshipPath<AbstractEntity>(AbstractEntity.class, "child").to("sibling").toProperty("name");
    assertEquals("size", 2, propertyPath.getPathLength());
    assertEquals("<AbstractEntity>.child.sibling.name", propertyPath.toString());
    assertEquals("child", propertyPath.getAncestryPath().getPath());
    assertEquals("<AbstractEntity>.child", propertyPath.getAncestryPath().toString());
    assertEquals("sibling", propertyPath.getLeaf());

    RelationshipPathIterator iter = propertyPath.iterator();
    assertTrue(iter.hasNext());
    iter.next();
    assertEquals("child", iter.getPathElement());
    assertNull(iter.getRestrictionPropertyNameAndValue());
    assertTrue(iter.hasNext());
    iter.next();
    assertEquals("sibling", iter.getPathElement());
    assertNull(iter.getRestrictionPropertyNameAndValue());
    assertFalse(iter.hasNext());
    assertEquals("name", propertyPath.getPropertyName());
  }

  public void testMultipleElementsWithEntityIdPath()
  {
    AbstractEntity sibling = new DummyEntity(1);
    AbstractEntity toy = new DummyEntity(2);
    PropertyPath propertyPath = 
      new RelationshipPath<AbstractEntity>(AbstractEntity.class, "child").to("siblings").restrict("id", sibling.getEntityId()).
      to("toys").restrict("id", toy.getEntityId()).toProperty("name");
    assertEquals("size", 3, propertyPath.getPathLength());
    assertEquals("<AbstractEntity>.child.siblings[id=1].toys[id=2].name", propertyPath.toString());
    assertEquals("child.siblings.toys.name", propertyPath.getPath());
    assertEquals("<AbstractEntity>.child.siblings[id=1]", propertyPath.getAncestryPath().toString());
    assertEquals("child.siblings", propertyPath.getAncestryPath().getPath());
    assertEquals("toys", propertyPath.getLeaf());
    RelationshipPathIterator iter = propertyPath.iterator();


    assertTrue(iter.hasNext());
    iter.next();
    assertEquals("child", iter.getPathElement());
    assertNull(iter.getRestrictionPropertyNameAndValue());

    assertTrue(iter.hasNext());
    iter.next();
    assertEquals("siblings", iter.getPathElement());
    assertEquals("id", iter.getRestrictionPropertyNameAndValue().getName());
    assertEquals(sibling.getEntityId(), iter.getRestrictionPropertyNameAndValue().getValue());

    assertTrue(iter.hasNext());
    iter.next();
    assertEquals("toys", iter.getPathElement());
    assertEquals("id", iter.getRestrictionPropertyNameAndValue().getName());
    assertEquals(toy.getEntityId(), iter.getRestrictionPropertyNameAndValue().getValue());

    assertFalse(iter.hasNext());
    assertEquals("name", propertyPath.getPropertyName());
  }

  public void testIntermediatePaths()
  {
    AbstractEntity sibling = new DummyEntity(1);
    AbstractEntity toy = new DummyEntity(2);
    PropertyPath propertyPath = new RelationshipPath<AbstractEntity>(AbstractEntity.class, "child").to("siblings").restrict("id", sibling.getEntityId()).to("toys").restrict("id", toy.getEntityId()).toProperty("name");
    RelationshipPathIterator iterator = propertyPath.iterator();
    iterator.next();
    RelationshipPath intermediatePropertyPath = iterator.getIntermediatePath();
    assertEquals("<AbstractEntity>.child", intermediatePropertyPath.toString());
    assertEquals("child", intermediatePropertyPath.getPath());
    iterator.next();
    intermediatePropertyPath = iterator.getIntermediatePath();
    assertEquals("<AbstractEntity>.child.siblings[id=1]", intermediatePropertyPath.toString());
    assertEquals("child.siblings", intermediatePropertyPath.getPath());
    iterator.next();
    intermediatePropertyPath = iterator.getIntermediatePath();
    assertEquals("<AbstractEntity>.child.siblings[id=1].toys[id=2]", intermediatePropertyPath.toString());
    assertEquals("child.siblings.toys", intermediatePropertyPath.getPath());
    assertFalse(iterator.hasNext());
  }

  public void testEquality()
  {
    AbstractEntity sibling = new DummyEntity(1);
    AbstractEntity toy1 = new DummyEntity(2);
    AbstractEntity toy2 = new DummyEntity(3);
    PropertyPath propertyPath1a = new RelationshipPath<AbstractEntity>(AbstractEntity.class, "child").to("siblings").restrict("id", sibling.getEntityId()).to("toys").restrict("id", toy1.getEntityId()).toProperty("name");
    PropertyPath propertyPath1b = new RelationshipPath<AbstractEntity>(AbstractEntity.class, "child").to("siblings").restrict("id", sibling.getEntityId()).to("toys").restrict("id", toy1.getEntityId()).toProperty("name");
    PropertyPath propertyPath1c = new RelationshipPath<AbstractEntity>(AbstractEntity.class, "child").to("siblings").restrict("id", sibling.getEntityId()).to("toys").restrict("id", toy1.getEntityId()).toProperty("size");
    PropertyPath propertyPath2 =  new RelationshipPath<AbstractEntity>(AbstractEntity.class, "child").to("siblings").restrict("id", sibling.getEntityId()).to("toys").restrict("id", toy2.getEntityId()).toProperty("name");
    RelationshipPath propertyPath3 = new RelationshipPath<AbstractEntity>(AbstractEntity.class, "child").to("siblings").restrict("id", sibling.getEntityId()).to("toys").restrict("id", toy1.getEntityId());

    assertEquals(propertyPath1a, propertyPath1b);
    assertEquals(propertyPath1a.hashCode(), propertyPath1b.hashCode());
    assertEquals(propertyPath1a.iterator().getIntermediatePath(), propertyPath1b.iterator().getIntermediatePath());
    assertEquals(propertyPath1a.iterator().getIntermediatePath().hashCode(), propertyPath1b.iterator().getIntermediatePath().hashCode());

    assertFalse(propertyPath1a.equals(propertyPath1c));
    assertFalse(propertyPath1a.hashCode() == propertyPath1c.hashCode());
    assertEquals(propertyPath1a.iterator().getIntermediatePath(), propertyPath1c.iterator().getIntermediatePath());
    assertEquals(propertyPath1a.iterator().getIntermediatePath().hashCode(), propertyPath1c.iterator().getIntermediatePath().hashCode());

    assertEquals(propertyPath3.iterator().getIntermediatePath(), propertyPath1a.iterator().getIntermediatePath());
    assertEquals(propertyPath3.iterator().getIntermediatePath().hashCode(), propertyPath1a.iterator().getIntermediatePath().hashCode());
    assertFalse(propertyPath1a.equals(propertyPath2));
    assertFalse(propertyPath1a.hashCode() == propertyPath2.hashCode());
  }
  
  public void testHasRestrictions()
  {
    assertTrue("has restrictions", new RelationshipPath<AbstractEntity>(AbstractEntity.class, "child").to("siblings").restrict("id", 1).hasRestrictions());
    assertTrue("has restrictions", new RelationshipPath<AbstractEntity>(AbstractEntity.class, "child").restrict("id", 1).to("siblings").restrict("id", 1).hasRestrictions());
    assertTrue("has restrictions", new RelationshipPath<AbstractEntity>(AbstractEntity.class, "child").restrict("id", 1).to("siblings").hasRestrictions());
    assertFalse("has restrictions", new RelationshipPath<AbstractEntity>(AbstractEntity.class, "child").to("siblings").hasRestrictions());
  }
  
  /**
   * Test that PropertyPath allows an empty string for the property name value,
   * since this usage is required for collections of elements, which have no
   * properties other than their immediate value (as opposed to collections of
   * entities, which are comprised of one or more properties).
   */
  public void testCollectionOfElementsUsage()
  {
    PropertyPath propertyPath1 = new RelationshipPath<AbstractEntity>(AbstractEntity.class, "child").to("siblings").to("nicknames").toCollectionOfValues();
    assertEquals("child.siblings.nicknames", propertyPath1.getPath());
    assertEquals("", propertyPath1.getPropertyName());
    
    PropertyPath propertyPath2 = new RelationshipPath<AbstractEntity>(AbstractEntity.class, "child").to("siblings").to("nicknames").toCollectionOfValues();
    assertEquals(propertyPath1, propertyPath2);
  }
  
  public void testAppend()
  {
    PropertyPath<AbstractEntity> relationship2 = new RelationshipPath<AbstractEntity>(AbstractEntity.class, "grandchild").restrict("age", new Integer(1)).to("greatgrandchild").toProperty("name");
    PropertyPath<AbstractEntity> relationship = new RelationshipPath<AbstractEntity>(AbstractEntity.class, "child").to(relationship2);
    assertEquals(3, relationship.getPathLength());
    assertEquals("child.grandchild.greatgrandchild.name", relationship.getPath());
    assertEquals("<AbstractEntity>.child.grandchild[age=1].greatgrandchild.name", relationship.toString());
  }

}
