// $HeadURL:
// svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/test/edu/harvard/med/screensaver/TestHibernate.java
// $
// $Id: ComplexDAOTest.java 322 2006-07-12 15:04:37Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.model.Child;
import edu.harvard.med.screensaver.model.Parent;
import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screenresults.ActivityIndicatorType;
import edu.harvard.med.screensaver.model.screenresults.IndicatorDirection;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.AssayReadoutType;


/**
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class HibernateParentChildTest extends AbstractSpringTest
{
  public void testParentChildRelationship()
  {
    Parent parent = new Parent("parent1");
    new Child("a", parent);
    new Child("b", parent);
    dao.persistEntity(parent);
    
    Parent loadedParent = dao.findEntityById(Parent.class, parent.getParentId());
    assertNotSame("distinct parent objects for save and load operations", parent, loadedParent);
    Set<Child> loadedChildren = loadedParent.getChildren();
    assertNotSame("distinct children set objects for save and load operations", parent.getChildren(), loadedChildren);
    assertEquals(parent, loadedParent);
    assertEquals(parent.getChildren(), loadedChildren);
    
    // now test whether we can add another child to our Parent that was loaded from the database
    Child childC = new Child("c", loadedParent);
    assertTrue("child added to loaded parent", loadedParent.getChildren().contains(childC));
    dao.persistEntity(loadedParent);
    
    Parent loadedParent2 = dao.findEntityById(Parent.class, parent.getParentId());
    assertTrue("child added to re-loaded parent", loadedParent2.getChildren().contains(childC));
  }
}  
