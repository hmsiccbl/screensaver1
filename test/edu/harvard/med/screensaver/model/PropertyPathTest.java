// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.io.Serializable;

import junit.framework.TestCase;

import edu.harvard.med.screensaver.model.RelationshipPath.RelationshipPathIterator;

public class PropertyPathTest extends TestCase
{
  private static class DummyEntity extends AbstractEntity {

    private static final long serialVersionUID = 1L;
    private Integer _id;

    public DummyEntity(int id)
    {
      _id = id;
    }

    @Override
    public Object acceptVisitor(AbstractEntityVisitor visitor)
    {
      return null;
    }

    @Override
    public Serializable getEntityId()
    {
      return _id;
    }
  }

  public void testZeroElementPath()
  {
    PropertyPath propertyPath = new PropertyPath<AbstractEntity>(AbstractEntity.class, "name");
    assertEquals(propertyPath.getRootEntityClass(), AbstractEntity.class);
    assertEquals("size", 0, propertyPath.getPathLength());
    assertEquals("<AbstractEntity>.name", propertyPath.toString());
    assertEquals("name", propertyPath.getFormattedPath());
    assertEquals("name", propertyPath.getPropertyName());
    assertEquals("<AbstractEntity>", propertyPath.getAncestryPath().toString());
    assertEquals("", propertyPath.getAncestryPath().getFormattedPath());
    assertEquals("", propertyPath.getLeaf());
  }

  public void testOneElementPath()
  {
    PropertyPath propertyPath = new PropertyPath<AbstractEntity>(AbstractEntity.class, "child", "name");
    assertEquals(propertyPath.getRootEntityClass(), AbstractEntity.class);
    assertEquals("size", 1, propertyPath.getPathLength());
    assertEquals("<AbstractEntity>.child.name", propertyPath.toString());
    assertEquals("child.name", propertyPath.getFormattedPath());
    assertEquals("name", propertyPath.getPropertyName());
    assertEquals("<AbstractEntity>", propertyPath.getAncestryPath().toString());
    assertEquals("", propertyPath.getAncestryPath().getFormattedPath());
    assertEquals("child", propertyPath.getLeaf());
  }

  public void testMultipleElementsPath()
  {
    PropertyPath propertyPath = new PropertyPath<AbstractEntity>(AbstractEntity.class, "child.sibling" , "name");
    assertEquals("size", 2, propertyPath.getPathLength());
    assertEquals("<AbstractEntity>.child.sibling.name", propertyPath.toString());
    assertEquals("child", propertyPath.getAncestryPath().getFormattedPath());
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
    PropertyPath propertyPath = new PropertyPath<AbstractEntity>(AbstractEntity.class,
                                                 "child.siblings[id].toys[id]",
                                                 "name",
                                                 sibling.getEntityId(),
                                                 toy.getEntityId());
    assertEquals("size", 3, propertyPath.getPathLength());
    assertEquals("<AbstractEntity>.child.siblings[id=1].toys[id=2].name", propertyPath.toString());
    assertEquals("child.siblings.toys.name", propertyPath.getFormattedPath());
    assertEquals("<AbstractEntity>.child.siblings[id=1]", propertyPath.getAncestryPath().toString());
    assertEquals("child.siblings", propertyPath.getAncestryPath().getFormattedPath());
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
    PropertyPath propertyPath = new PropertyPath<AbstractEntity>(AbstractEntity.class,
                                                 "child.siblings[id].toys[id]",
                                                 "name",
                                                 sibling.getEntityId(),
                                                 toy.getEntityId());
    RelationshipPathIterator iterator = propertyPath.iterator();
    iterator.next();
    RelationshipPath intermediatePropertyPath = iterator.getIntermediatePath();
    assertEquals("<AbstractEntity>.child", intermediatePropertyPath.toString());
    assertEquals("child", intermediatePropertyPath.getFormattedPath());
    iterator.next();
    intermediatePropertyPath = iterator.getIntermediatePath();
    assertEquals("<AbstractEntity>.child.siblings[id=1]", intermediatePropertyPath.toString());
    assertEquals("child.siblings", intermediatePropertyPath.getFormattedPath());
    iterator.next();
    intermediatePropertyPath = iterator.getIntermediatePath();
    assertEquals("<AbstractEntity>.child.siblings[id=1].toys[id=2]", intermediatePropertyPath.toString());
    assertEquals("child.siblings.toys", intermediatePropertyPath.getFormattedPath());
    assertFalse(iterator.hasNext());
  }

  public void testEquality()
  {
    AbstractEntity sibling = new DummyEntity(1);
    AbstractEntity toy1 = new DummyEntity(2);
    AbstractEntity toy2 = new DummyEntity(3);
    PropertyPath propertyPath1a = new PropertyPath<AbstractEntity>(AbstractEntity.class, "child.siblings[id].toys[id]", "name", sibling.getEntityId(), toy1.getEntityId());
    PropertyPath propertyPath1b = new PropertyPath<AbstractEntity>(AbstractEntity.class, "child.siblings[id].toys[id]", "name", sibling.getEntityId(), toy1.getEntityId());
    PropertyPath propertyPath1c = new PropertyPath<AbstractEntity>(AbstractEntity.class, "child.siblings[id].toys[id]", "size", sibling.getEntityId(), toy1.getEntityId());
    PropertyPath propertyPath2 = new PropertyPath<AbstractEntity>(AbstractEntity.class, "child.siblings[id].toys[id]", "name", sibling.getEntityId(), toy2.getEntityId());
    PropertyPath propertyPath3 = new PropertyPath<AbstractEntity>(AbstractEntity.class, "child.siblings[id]", sibling.getEntityId(), toy1.getEntityId());

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
    assertTrue("has restrictions", new RelationshipPath<AbstractEntity>(AbstractEntity.class, "child.siblings[id]", 1).hasRestrictions());
    assertTrue("has restrictions", new RelationshipPath<AbstractEntity>(AbstractEntity.class, "child[id].siblings[id]", 1, 1).hasRestrictions());
    assertTrue("has restrictions", new RelationshipPath<AbstractEntity>(AbstractEntity.class, "child[id].siblings", 1).hasRestrictions());
    assertFalse("has restrictions", new RelationshipPath<AbstractEntity>(AbstractEntity.class, "child.siblings").hasRestrictions());
  }
  
  /**
   * Test that PropertyPath allows an empty string the property name value,
   * since this usage is required for collections of elements, which have no
   * properties other than their immediate value (as opposed to collections of
   * entities, which are comprised of one or more properties).
   */
  public void testCollectionOfElementsUsage()
  {
    PropertyPath propertyPath1 = new PropertyPath<AbstractEntity>(AbstractEntity.class, "child.siblings.nicknames", "");
    assertEquals("", propertyPath1.getPropertyName());
    
    PropertyPath propertyPath2 = new PropertyPath<AbstractEntity>(AbstractEntity.class, "child.siblings.nicknames", "");
    assertEquals(propertyPath1, propertyPath2);
    
  }

}
