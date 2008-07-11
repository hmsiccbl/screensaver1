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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.SchemaUtil;
import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.DataModelViolationException;

import org.apache.log4j.Logger;

public class ScreeningRoomUserTest extends AbstractEntityInstanceTest<ScreeningRoomUser>
{
  // static members

  private static Logger log = Logger.getLogger(ScreeningRoomUserTest.class);


  // instance data members


  // public constructors and methods

  public ScreeningRoomUserTest() throws IntrospectionException
  {
    super(ScreeningRoomUser.class);
  }

  public void testLabName()
  {
    initLab();

    ScreeningRoomUser labMember = genericEntityDao.findEntityByProperty(ScreeningRoomUser.class,
                                                                        "lastName",
                                                                        "Member",
                                                                        true,
                                                                        "labHead.labAffiliation");
    assertEquals("lab member with lab head", "Head, Lab - LabAffiliation", labMember.getLab().getLabName());
    ScreeningRoomUser labHead = labMember.getLab().getLabHead();
    assertEquals("lab head", "Head, Lab - LabAffiliation", labHead.getLab().getLabName());
  }

  public void testAdministrativeRoleNotAllowed() {
    final ScreeningRoomUser user = new ScreeningRoomUser("first",
                                                         "last",
                                                         "first_last@hms.harvard.edu");
    user.setECommonsId("ec1");

    user.addScreensaverUserRole(ScreensaverUserRole.RNAI_SCREENER);
    genericEntityDao.saveOrUpdateEntity(user);

    ScreeningRoomUser user2 = genericEntityDao.findEntityById(ScreeningRoomUser.class, user.getEntityId(), false, "screensaverUserRoles");
    assertEquals(new HashSet<ScreensaverUserRole>(Arrays.asList(ScreensaverUserRole.SCREENSAVER_USER,
                                                                ScreensaverUserRole.RNAI_SCREENER, 
                                                                ScreensaverUserRole.SCREENER, 
                                                                ScreensaverUserRole.SCREENSAVER_USER)),
                 user2.getScreensaverUserRoles());

    try {
      genericEntityDao.doInTransaction(new DAOTransaction() {
        public void runTransaction()
        {
          ScreeningRoomUser user3 = genericEntityDao.findEntityById(ScreeningRoomUser.class, user.getEntityId(), false, "screensaverUserRoles");
          user3.addScreensaverUserRole(ScreensaverUserRole.READ_EVERYTHING_ADMIN);
        };
      });
      fail("expected DataModelViolationException after adding administrative role to screening room user");
    }
    catch (Exception e) {
      assertTrue(e instanceof DataModelViolationException);
    }
  }

  public void testOnlyLabHeadHasLabAffiliation()
  {
    initLab();
    ScreeningRoomUser labMember =
      genericEntityDao.findEntityByProperty(ScreeningRoomUser.class,
                                            "lastName",
                                            "Member");
    ScreeningRoomUser labHead =
      genericEntityDao.findEntityByProperty(ScreeningRoomUser.class,
                                            "lastName",
                                            "Head");
    List<ScreeningRoomUser> screeningRoomUsers =
      genericEntityDao.findEntitiesByHql(ScreeningRoomUser.class,
                                         "from ScreeningRoomUser sru where sru.labAffiliation is not null");
    assertTrue(screeningRoomUsers.contains(labHead));
    assertFalse(screeningRoomUsers.contains(labMember));
  }

  public void testOnlyLabMembersHaveLabHead()
  {
    initLab();
    List<ScreeningRoomUser> labMembers =
      genericEntityDao.findEntitiesByHql(ScreeningRoomUser.class,
                                         "from ScreeningRoomUser sru where sru.labHead is not null");
    ScreeningRoomUser labHead =
      genericEntityDao.findEntityByProperty(ScreeningRoomUser.class,
                                            "lastName",
                                            "Head");
    ScreeningRoomUser labMember =
      genericEntityDao.findEntityByProperty(ScreeningRoomUser.class,
                                            "lastName",
                                            "Member");
    assertTrue(labMembers.contains(labMember));
    assertFalse(labMembers.contains(labHead));
  }

  public void testSameLabForAllLabMembersAndLabHead()
  {
    initLab();
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        ScreeningRoomUser labMember =
          genericEntityDao.findEntityByProperty(ScreeningRoomUser.class,
                                                "lastName",
                                                "Member");
        ScreeningRoomUser labHead =
          genericEntityDao.findEntityByProperty(ScreeningRoomUser.class,
                                                "lastName",
                                                "Head");
        assertSame(labHead.getLab(), labMember.getLab());
      }
    });
  }

  public void testUserWithoutLab()
  {
    schemaUtil.truncateTablesOrCreateSchema();
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        ScreeningRoomUser labMember = new ScreeningRoomUser("Independent", "User", "not_in_a_lab@hms.harvard.edu");
        genericEntityDao.saveOrUpdateEntity(labMember);
        assertEquals("", labMember.getLab().getLabName());
        assertEquals("", labMember.getLab().getLabAffiliationName());
        assertNull(labMember.getLab().getLabAffiliation());
        assertNull(labMember.getLab().getLabHead());
      }
    });
  }

  public void testLabMemberChangesLab()
  {
    initLab();
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        ScreeningRoomUser labMember =
          genericEntityDao.findEntityByProperty(ScreeningRoomUser.class,
                                                "lastName",
                                                "Member");
        assertEquals("Head, Lab - LabAffiliation", labMember.getLab().getLabName());

        LabHead labHead2 = new LabHead("Lab", "Head2", "lab_head2@hms.harvard.edu", new LabAffiliation("LabAffiliation2", AffiliationCategory.HMS));
        genericEntityDao.persistEntity(labHead2);

        labMember.setLab(labHead2.getLab());
      }
    });

    ScreeningRoomUser labMember =
      genericEntityDao.findEntityByProperty(ScreeningRoomUser.class,
                                            "lastName",
                                            "Member",
                                            true,
                                            "labHead.labAffiliation");
    assertEquals("Head2, Lab - LabAffiliation2", labMember.getLab().getLabName());

  }

  public void testLabMemberBecomesLabIndependent()
  {
    initLab();
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        ScreeningRoomUser labMember =
          genericEntityDao.findEntityByProperty(ScreeningRoomUser.class,
                                                "lastName",
                                                "Member",
                                                false,
                                                "labHead.labAffiliation");
        assertEquals("Head, Lab - LabAffiliation", labMember.getLab().getLabName());
        labMember.setLab(null);
      }
    });

    ScreeningRoomUser labMember =
      genericEntityDao.findEntityByProperty(ScreeningRoomUser.class,
                                            "lastName",
                                            "Member",
                                            true,
                                            "labHead.labAffiliation");
    assertEquals("", labMember.getLab().getLabName());
    assertEquals("", labMember.getLab().getLabAffiliationName());
    assertNull(labMember.getLab().getLabAffiliation());
    assertNull(labMember.getLab().getLabHead());
  }

  public void testLabMemberClassificationCannotBecomePrincipalInvestigator()
  {
    initLab();
    ScreeningRoomUser labMember =
      genericEntityDao.findEntityByProperty(ScreeningRoomUser.class,
                                            "lastName",
                                            "Member",
                                            false,
                                            "labHead.labAffiliation");
    try {
      assertFalse(labMember.isHeadOfLab());
      labMember.setUserClassification(ScreeningRoomUserClassification.GRADUATE_STUDENT); // allowed
      labMember.setUserClassification(ScreeningRoomUserClassification.PRINCIPAL_INVESTIGATOR); // not allowed
      fail("expected BusinessRuleViolationException when attempting to change classification of a lab head");
    }
    catch (BusinessRuleViolationException e) {}
  }

  private void initLab()
  {
    initLab(genericEntityDao, schemaUtil);
  }

  static void initLab(final GenericEntityDAO dao,
                      SchemaUtil schemaUtil)
  {
    schemaUtil.truncateTablesOrCreateSchema();
    dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        LabHead labHead = new LabHead("Lab", "Head", "lab_head@hms.harvard.edu", new LabAffiliation("LabAffiliation", AffiliationCategory.HMS));
        ScreeningRoomUser labMember = new ScreeningRoomUser("Lab", "Member", "lab_member@hms.harvard.edu");
        labMember.setLab(labHead.getLab());

        dao.saveOrUpdateEntity(labMember);
        dao.saveOrUpdateEntity(labHead);
      }
    });
  }
}

