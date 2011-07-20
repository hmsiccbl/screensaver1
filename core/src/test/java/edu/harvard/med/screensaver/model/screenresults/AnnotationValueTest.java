// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screenresults;

import java.beans.IntrospectionException;

import junit.framework.TestSuite;

import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.test.TestDataFactory;

public class AnnotationValueTest extends AbstractEntityInstanceTest<AnnotationValue>
{
  public static TestSuite suite()
  {
    return buildTestSuite(AnnotationValueTest.class, AnnotationValue.class);
  }

  public AnnotationValueTest()
  {
    super(AnnotationValue.class);
  }

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();

    dataFactory.addPreCreateHook(AnnotationType.class, new TestDataFactory.PreCreateHook<AnnotationType>() {
      @Override
      public void preCreate(String callStack, Object[] instantiationArgs)
      {
        if (callStack.matches(".*AnnotationType\\|.*AnnotationValue.*\\|testEntityProperty:numericValue")) {
          instantiationArgs[2] = Boolean.TRUE;
        }
      }
    });
    dataFactory.addPreCreateHook(AnnotationValue.class, new TestDataFactory.PreCreateHook<AnnotationValue>() {
      @Override
      public void preCreate(String callStack, Object[] instantiationArgs)
      {
        if (callStack.matches(AnnotationValue.class.getName() + "\\|testEntityProperty:numericValue")) {
          instantiationArgs[1] = dataFactory.newInstance(Double.class).toString();
        }
      }
    });
  }
}

