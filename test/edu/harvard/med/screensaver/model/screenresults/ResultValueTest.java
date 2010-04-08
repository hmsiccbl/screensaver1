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
import edu.harvard.med.screensaver.model.libraries.Well;

public class ResultValueTest extends AbstractEntityInstanceTest<ResultValue>
{
  public static TestSuite suite()
  {
    return buildTestSuite(ResultValueTest.class, ResultValue.class);
  }

  public ResultValueTest() throws IntrospectionException
  {
    super(ResultValue.class);
  }
  
  /**
   * @motivation automated model tests can only test one type of result value,
   *             numeric or non-numeric, and since numeric is the more common
   *             choice, we must explicitly test the less common type here
   */
  public void testValueOfNonNumericResultValue()
  {
    schemaUtil.truncateTablesOrCreateSchema();
    DataColumn col = dataFactory.newInstance(ScreenResult.class).createDataColumn("col").makeTextual();
    AssayWell assayWell = col.getScreenResult().createAssayWell(dataFactory.newInstance(Well.class));
    ResultValue resultValue = col.createResultValue(assayWell, "text value");
    persistEntityNetwork(resultValue);
    
    ResultValue resultValue2 = genericEntityDao.findEntityById(ResultValue.class, resultValue.getEntityId());
    assertEquals("text value", resultValue2.getValue()); 
  }
}

