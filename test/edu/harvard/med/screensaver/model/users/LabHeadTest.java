// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.users;

import java.beans.IntrospectionException;

import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;

import org.apache.log4j.Logger;

public class LabHeadTest extends AbstractEntityInstanceTest<LabHead>
{
  // static members

  private static Logger log = Logger.getLogger(LabHeadTest.class);


  // instance data members


  // public constructors and methods

  protected void onSetUp() {
    schemaUtil.truncateTablesOrCreateSchema();
  }

  public LabHeadTest() throws IntrospectionException
  {
    super(LabHead.class);
  }

  public void testLabHeadClassificationImmutable()
  {
    ScreeningRoomUserTest.initLab(genericEntityDao);
    ScreeningRoomUser labHead =
      genericEntityDao.findEntityByProperty(ScreeningRoomUser.class,
                                            "lastName",
                                            "Head");
    try {
      assertTrue(labHead.isHeadOfLab());
      labHead.setUserClassification(ScreeningRoomUserClassification.PRINCIPAL_INVESTIGATOR); // allowed
      labHead.setUserClassification(ScreeningRoomUserClassification.GRADUATE_STUDENT); // not allowed
      fail("expected BusinessRuleViolationException when attempting to change classification of a lab head");
    }
    catch (BusinessRuleViolationException e) {}
  }


}

