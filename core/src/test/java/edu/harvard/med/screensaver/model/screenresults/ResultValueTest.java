// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screenresults;

import junit.framework.TestSuite;

import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.test.TestDataFactory.Builder;
import edu.harvard.med.screensaver.test.TestDataFactory.ParentedEntityBuilder;
import edu.harvard.med.screensaver.test.TestDataFactory.PreCreateHook;

public class ResultValueTest extends AbstractEntityInstanceTest<ResultValue>
{
  public static TestSuite suite()
  {
    return buildTestSuite(ResultValueTest.class, ResultValue.class);
  }

  public ResultValueTest()
  {
    super(ResultValue.class);
  }

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();

    Builder<ResultValue> excludeResultValueBuilder =
      new ParentedEntityBuilder<ResultValue,DataColumn>(ResultValue.class,
                                                        DataColumn.class.getMethod("createResultValue",
                                                                                   new Class[] { AssayWell.class, Double.class,
                                                                                     Boolean.TYPE }),
                                                        "testEntityProperty:exclude",
                                                        genericEntityDao, dataFactory) {
        protected DataColumn newParent(String callStack)
      {
        // unfortunately, we can't just call super.newParent(), as this causes the new DataColumn to be persisted before its data type is set, which in turn causes Hibernate to set the (immutable) data type to an arbitrary value
        return dataFactory.newInstance(ScreenResult.class, callStack).createDataColumn(dataFactory.newInstance(String.class, callStack)).makeNumeric(3);
      }
      }.addPreCreateHook(new PreCreateHook<ResultValue>() {

        @Override
        public void preCreate(String callStack, Object[] args)
        {
          args[2] = Boolean.TRUE;
        }
      });
    dataFactory.addBuilder(excludeResultValueBuilder);

    Builder<ResultValue> booleanPositiveResultValueBuilder =
      new ParentedEntityBuilder<ResultValue,DataColumn>(ResultValue.class,
                                                        DataColumn.class.getMethod("createBooleanPositiveResultValue",
                                                                                   new Class[] { AssayWell.class,
                                                                                     Boolean.class, Boolean.TYPE }),
                                                        "testEntityProperty:(booleanPositiveValue|positive)",
                                                        genericEntityDao, dataFactory) {
        protected DataColumn newParent(String callStack)
      {
        // unfortunately, we can't just call super.newParent(), as this causes the new DataColumn to be persisted before its data type is set, which in turn causes Hibernate to set the (immutable) data type to an arbitrary value
        return dataFactory.newInstance(ScreenResult.class, callStack).createDataColumn(dataFactory.newInstance(String.class, callStack)).makeBooleanPositiveIndicator();
      }
      };
    dataFactory.addBuilder(booleanPositiveResultValueBuilder);

    Builder<ResultValue> confirmedPositiveResultValueBuilder =
      new ParentedEntityBuilder<ResultValue,DataColumn>(ResultValue.class,
                                                        DataColumn.class.getMethod("createConfirmedPositiveResultValue",
                                                                                   new Class[] {
                                                                                     AssayWell.class,
                                                                                     ConfirmedPositiveValue.class,
                                                                                     Boolean.TYPE }),
                                                        "testEntityProperty:confirmedPositiveValue",
                                                        genericEntityDao, dataFactory) {
        protected DataColumn newParent(String callStack)
      {
        // unfortunately, we can't just call super.newParent(), as this causes the new DataColumn to be persisted before its data type is set, which in turn causes Hibernate to set the (immutable) data type to an arbitrary value
        return dataFactory.newInstance(ScreenResult.class, callStack).createDataColumn(dataFactory.newInstance(String.class, callStack)).makeConfirmedPositiveIndicator();
      }
      };
    dataFactory.addBuilder(confirmedPositiveResultValueBuilder);

    Builder<ResultValue> partitionedPositiveResultValueBuilder =
      new ParentedEntityBuilder<ResultValue,DataColumn>(ResultValue.class,
                                                        DataColumn.class.getMethod("createPartitionedPositiveResultValue",
                                                                                   new Class[] {
                                                                                     AssayWell.class,
                                                                                     PartitionedValue.class,
                                                                                     Boolean.TYPE }),
                                                        "testEntityProperty:partitionedPositiveValue",
                                                        genericEntityDao, dataFactory) {
        protected DataColumn newParent(String callStack)
      {
        // unfortunately, we can't just call super.newParent(), as this causes the new DataColumn to be persisted before its data type is set, which in turn causes Hibernate to set the (immutable) data type to an arbitrary value
        return dataFactory.newInstance(ScreenResult.class, callStack).createDataColumn(dataFactory.newInstance(String.class, callStack)).makePartitionPositiveIndicator();
      }
      };
    dataFactory.addBuilder(partitionedPositiveResultValueBuilder);
  }
}

