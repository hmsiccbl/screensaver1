// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.collections.Transformer;
import org.apache.log4j.Logger;

public class CollectionUtilsTest extends TestCase
{
  // static members

  private static Logger log = Logger.getLogger(CollectionUtilsTest.class);


  // instance data members

  // public constructors and methods
  
  private static class Struct
  {
    Integer key;
    String value;

    Struct(Integer key, String value) 
    {
      this.key = key; 
      this.value = value;
    }
  }
  
  public void testIndexCollection()
  {
    Transformer getKey = new Transformer() {
      @SuppressWarnings("unchecked")
      public Object transform(Object e)
      {
        return ((Struct) e).key;
      }
    };
    List<Struct> c = new ArrayList<Struct>();
    c.add(new Struct(1, "A"));
    c.add(new Struct(2, "B"));
    c.add(new Struct(3, "C"));

    Map<Integer,Struct> index = CollectionUtils.indexCollection(c,
                                                                getKey,
                                                                Integer.class,
                                                                Struct.class);
    assertEquals(c.get(0), index.get(1));
    assertEquals(c.get(1), index.get(2));
    assertEquals(c.get(2), index.get(3));
  }

  // private methods

}

