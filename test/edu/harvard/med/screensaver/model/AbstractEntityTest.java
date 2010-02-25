// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

/**
 * Test methods implemented in class {@link AbstractEntity}.
 */
public class AbstractEntityTest extends TestCase
{
  private static Logger log = Logger.getLogger(AbstractEntityTest.class);

  private static class LessAbstractEntity extends AbstractEntity<Integer>
  {
    private static int nextUniqueRelative = 1;
    private static final long serialVersionUID = 1L;

    private String _name;
    private boolean _great;

    public LessAbstractEntity(String name,
                              boolean isGreat)
    {
      _name = name;
      _great = isGreat;
      setEntityId(-1);
    }

    public Object acceptVisitor(AbstractEntityVisitor visitor)
    {
      return null;
    }

    public String getName()
    {
      return _name;
    }

    public boolean isGreat()
    {
      return _great;
    }

    /**
     * @motivation to test that collection-returning methods are NOT considered by AbstractEntity.isEquivalent()
     */
    public List<Integer> getUniqueList()
    {
      ArrayList<Integer> relatives = new ArrayList<Integer>();
      relatives.add(nextUniqueRelative++);
      return relatives;
    }

    /**
     * @motivation to test that map-returning methods are NOT considered by AbstractEntity.isEquivalent()
     */
    public Map<String,Integer> getUniqueMap()
    {
      HashMap<String,Integer> map = new HashMap<String,Integer>();
      map.put(""+nextUniqueRelative,nextUniqueRelative);
      ++nextUniqueRelative;
      return map;
    }

    public int someBehavioralMethod()
    {
      return -1;
    }

    protected Object getBusinessKey()
    {
      return _name;
    }

  }

  /**
   * @motivation tests whether property getter methods from both inherited
   *             classes and concrete class are used by
   *             AbstractEntity.isEquivalent()
   */
  public static class ConcreteEntity extends LessAbstractEntity
  {
    private static final long serialVersionUID = 1L;

    private int _value;

    public ConcreteEntity(String name, boolean isGreat, int value) {
      super(name, isGreat);
      _value = value;
    }

    public int getValue()
    {
      return _value;
    }
  }

  public void testIsEquivalent()
  {
    ConcreteEntity entity1 = new ConcreteEntity("1",true,1);
    ConcreteEntity entity2 = new ConcreteEntity("1",true,1);
    assertTrue(entity1.isEquivalent(entity2));
    assertTrue(entity2.isEquivalent(entity1));

    List<ConcreteEntity> nonEquivalents = new ArrayList<ConcreteEntity>();
    //nonEquivalents.add(new ConcreteEntity("1",true,1));
    nonEquivalents.add(new ConcreteEntity("1",true,2));
    nonEquivalents.add(new ConcreteEntity("1",false,1));
    nonEquivalents.add(new ConcreteEntity("1",false,2));
    nonEquivalents.add(new ConcreteEntity("2",true,1));
    nonEquivalents.add(new ConcreteEntity("2",true,2));
    nonEquivalents.add(new ConcreteEntity("2",false,1));
    nonEquivalents.add(new ConcreteEntity("2",false,2));
    for (Iterator iter = nonEquivalents.iterator(); iter.hasNext();) {
      ConcreteEntity other = (ConcreteEntity) iter.next();
      assertFalse(entity1.isEquivalent(other));
    }
  }

  public void testGetProperty()
  {
    ConcreteEntity entity = new ConcreteEntity("3",true,3);
    assertEquals("'value' property", new Integer(3), entity.getPropertyValue("value", Integer.class));
    assertEquals("'name' property", "3", entity.getPropertyValue("name", String.class));
    assertEquals("'great' property", Boolean.TRUE, entity.getPropertyValue("great", Boolean.class));

  }
}
