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
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestSuite;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.SchemaUtil;
import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.AttachedFile;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.model.DomainModelDefinitionException;
import edu.harvard.med.screensaver.model.TestDataFactory;
import edu.harvard.med.screensaver.model.screens.AttachedFileType;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.util.CryptoUtils;

import org.apache.commons.io.IOUtils;
import org.joda.time.LocalDate;

import com.google.common.collect.Sets;

public class ScreeningRoomUserTest extends AbstractEntityInstanceTest<ScreeningRoomUser>
{
  public static TestSuite suite()
  {
    return buildTestSuite(ScreeningRoomUserTest.class, ScreeningRoomUser.class);
  }

  public ScreeningRoomUserTest() throws IntrospectionException
  {
    super(ScreeningRoomUser.class);
    dataFactory = new TestDataFactory() {
      @Override
      public <T> T getTestValueForType(Class<T> type) throws DomainModelDefinitionException
      {
        if (getName().endsWith("screensaverUserRoles") &&
          ScreensaverUserRole.class.isAssignableFrom(type)) {
           return (T) ScreensaverUserRole.SCREENSAVER_USER;
        }
        return super.getTestValueForType(type);
      }
    };
  }

  public void testUserDigestedPassword()
  {
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        ScreensaverUser user = new ScreeningRoomUser("First", "Last", "first_last@hms.harvard.edu");
        user.setLoginId("myLoginId");
        user.updateScreensaverPassword("myPassword");
        genericEntityDao.saveOrUpdateEntity(user);
      }
    });
    
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        ScreensaverUser user = genericEntityDao.findEntityByProperty(ScreensaverUser.class, "loginId", "myLoginId");
        assertNotNull(user);
        assertEquals(CryptoUtils.digest("myPassword"),
                     user.getDigestedPassword());
      }
    });
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

  public void testRoles() {
    final ScreeningRoomUser user = new ScreeningRoomUser("first",
                                                         "last",
                                                         "first_last@hms.harvard.edu");

    user.addScreensaverUserRole(ScreensaverUserRole.RNAI_SCREENER);
    genericEntityDao.saveOrUpdateEntity(user);

    ScreeningRoomUser user2 = genericEntityDao.findEntityById(ScreeningRoomUser.class, user.getEntityId(), false, "screensaverUserRoles");
    assertEquals(Sets.newHashSet(ScreensaverUserRole.RNAI_SCREENER, ScreensaverUserRole.SCREENER),
                 user2.getScreensaverUserRoles());

    try {
      ScreeningRoomUser user3 = genericEntityDao.findEntityById(ScreeningRoomUser.class, user.getEntityId(), false, "screensaverUserRoles");
      user3.addScreensaverUserRole(ScreensaverUserRole.READ_EVERYTHING_ADMIN);
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
  
  public void testAllAssociatedScreens()
  {
    LabHead labHead = new LabHead("Lab", "Head", "lab_head@hms.harvard.edu", new LabAffiliation("LabAffiliation", AffiliationCategory.HMS));
    ScreeningRoomUser labMember1 = new ScreeningRoomUser("Lab", "Member1", "lab_member@hms.harvard.edu");
    ScreeningRoomUser labMember2 = new ScreeningRoomUser("Lab", "Member2", "lab_member@hms.harvard.edu");
    labMember1.setLab(labHead.getLab());
    
    Screen screen1 = new Screen(labMember1, labHead, 1, ScreenType.RNAI, "1");
    Screen screen2 = new Screen(labHead, labHead, 1, ScreenType.RNAI, "2");
    Screen screen3 = new Screen(labMember1, null, 1, ScreenType.RNAI, "3");
    Screen screen4 = new Screen(labMember1, null, 1, ScreenType.RNAI, "4");
    screen4.addCollaborator(labMember2);
    Screen screen5 = new Screen(labMember2, labHead, 1, ScreenType.RNAI, "5");
    screen5.addCollaborator(labMember1);
    
    assertEquals(new HashSet<Screen>(Arrays.asList(screen1, screen2, screen5)), labHead.getAllAssociatedScreens());
    assertEquals(new HashSet<Screen>(Arrays.asList(screen1, screen3, screen4, screen5)), labMember1.getAllAssociatedScreens());
    assertEquals(new HashSet<Screen>(Arrays.asList(screen4, screen5)), labMember2.getAllAssociatedScreens());
  }
  
  public void testChecklistItemEvents()
  {
    schemaUtil.truncateTablesOrCreateSchema();
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        genericEntityDao.persistEntity(new ChecklistItem("trained", false, ChecklistItemGroup.NON_HARVARD_SCREENERS, 1));
        genericEntityDao.persistEntity(new ChecklistItem("lab access", true, ChecklistItemGroup.NON_HARVARD_SCREENERS, 2));
        AdministratorUser admin = new AdministratorUser("Admin", "User", "admin_user@hms.harvard.edu", "", "", "", "", "");
        ScreeningRoomUser user = new ScreeningRoomUser("Lab", "User", "lab_user@hms.harvard.edu");
        genericEntityDao.persistEntity(admin);
        genericEntityDao.persistEntity(user);
      }
    });
    ScreeningRoomUser user = genericEntityDao.findEntityByProperty(ScreeningRoomUser.class, "firstName", "Lab", false, "checklistItemEvents");
    AdministratorUser admin = genericEntityDao.findEntityByProperty(AdministratorUser.class, "firstName", "Admin", false, "activitiesPerformed");
    ChecklistItem checklistItem = genericEntityDao.findEntityByProperty(ChecklistItem.class, "itemName", "lab access");
    LocalDate today = new LocalDate();
    ChecklistItemEvent activationEvent = 
      user.createChecklistItemActivationEvent(checklistItem,
                                              today,
                                              new AdministrativeActivity(admin, 
                                                                         today, 
                                                                         AdministrativeActivityType.CHECKLIST_ITEM_EVENT));
    
    try {
      activationEvent.createChecklistItemExpirationEvent(today.minusDays(1), 
                                                         new AdministrativeActivity(admin, 
                                                                                    today, 
                                                                                    AdministrativeActivityType.CHECKLIST_ITEM_EVENT));
      fail("expected DataModelViolationException");
    }
    catch (DataModelViolationException e) {}
    ChecklistItemEvent expirationEvent = 
      activationEvent.createChecklistItemExpirationEvent(today.plusDays(1), 
                                                         new AdministrativeActivity(admin, 
                                                                                    today, 
                                                                                    AdministrativeActivityType.CHECKLIST_ITEM_EVENT));
    try {
      expirationEvent.createChecklistItemExpirationEvent(today,
                                                         new AdministrativeActivity(admin, 
                                                                                    today, 
                                                                                    AdministrativeActivityType.CHECKLIST_ITEM_EVENT));
      fail("expected DataModelViolationException");
    }
    catch (DataModelViolationException e) {}
    assertEquals(2, user.getChecklistItemEvents(checklistItem).size());
    assertFalse(user.getChecklistItemEvents(checklistItem).first().isExpiration());
    assertTrue(user.getChecklistItemEvents(checklistItem).last().isExpiration());
    try {
      expirationEvent.createChecklistItemExpirationEvent(today.minusDays(1),
                                                         new AdministrativeActivity(admin, 
                                                                                    today, 
                                                                                    AdministrativeActivityType.CHECKLIST_ITEM_EVENT));
      fail("expected DataModelViolationException");
    }
    catch (DataModelViolationException e) {}
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
  
  public void testAddAndDeleteAttachedFiles() throws IOException
  {
    initLab();
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        ScreeningRoomUser user = genericEntityDao.findEntityByProperty(ScreeningRoomUser.class, "lastName", "Head", false);
        try {
          user.createAttachedFile("file1.txt", AttachedFileType.SCREENER_CORRESPONDENCE, "file1 contents");
        }
        catch (IOException e) {
          throw new DAOTransactionRollbackException(e);
        }
        genericEntityDao.saveOrUpdateEntity(user);
      }
    });
    
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        ScreeningRoomUser user = (ScreeningRoomUser) genericEntityDao.findEntityByProperty(ScreeningRoomUser.class, "lastName", "Head", true, "attachedFiles");
        assertEquals("add attached file to user", 1, user.getAttachedFiles().size());
        try {
          assertEquals("attached file contents accessible",
                       "file1 contents",
                       IOUtils.toString(user.getAttachedFiles().iterator().next().getFileContents().getBinaryStream()));
        }
        catch (Exception e) {
          throw new DAOTransactionRollbackException(e);
        }
      }
    });

    ScreeningRoomUser user = genericEntityDao.findEntityByProperty(ScreeningRoomUser.class, "lastName", "Head", false, "attachedFiles");
    Iterator<AttachedFile> iter = user.getAttachedFiles().iterator();
    AttachedFile attachedFile = iter.next();
    user.getAttachedFiles().remove(attachedFile);
    genericEntityDao.saveOrUpdateEntity(user);
    user = genericEntityDao.findEntityByProperty(ScreeningRoomUser.class, "lastName", "Head", true, "attachedFiles");
    assertEquals("delete attached file from detached user", 0, user.getAttachedFiles().size());
  }
  
}

