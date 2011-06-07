// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.io.IOException;
import java.io.StringReader;

import junit.framework.TestSuite;
import org.hibernate.lob.ReaderInputStream;
import org.joda.time.LocalDate;

import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.ReagentAttachedFileType;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenAttachedFileType;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.UserAttachedFileType;

public class AttachedFileTest extends AbstractEntityInstanceTest<AttachedFile>
{
  public static TestSuite suite()
  {
    return buildTestSuite(AttachedFileTest.class, AttachedFile.class);
  }

  public AttachedFileTest()
  {
    super(AttachedFile.class);
  }
  
  public void testScreenRelationship() throws IOException
  {
    schemaUtil.truncateTables();
    Screen screen = dataFactory.newInstance(Screen.class);
    ScreenAttachedFileType attachedFileType = new ScreenAttachedFileType("Application");
    genericEntityDao.persistEntity(attachedFileType);
    screen.createAttachedFile("filename1", attachedFileType, new LocalDate(), new ReaderInputStream(new StringReader("file contents 1")));
    screen.createAttachedFile("filename2", attachedFileType, new LocalDate(), "file contents 2");
    screen = genericEntityDao.mergeEntity(screen);

    screen = genericEntityDao.reloadEntity(screen, true, Screen.attachedFiles.to(AttachedFile.screen));
    for (AttachedFile attachedFile : screen.getAttachedFiles()) {
      assertEquals(screen, attachedFile.getScreen());
    }
  }
  
  public void testScreeningUserRelationship() throws IOException
  {
    schemaUtil.truncateTables();
    ScreeningRoomUser user = dataFactory.newInstance(ScreeningRoomUser.class);
    UserAttachedFileType attachedFileType = new UserAttachedFileType("Application");
    genericEntityDao.persistEntity(attachedFileType);
    user.createAttachedFile("filename1", attachedFileType, new LocalDate(), new ReaderInputStream(new StringReader("file contents 1")));
    user.createAttachedFile("filename2", attachedFileType, new LocalDate(), "file contents 2");
    user = genericEntityDao.mergeEntity(user);

    user = genericEntityDao.reloadEntity(user, true, ScreeningRoomUser.attachedFiles.to(AttachedFile.screeningRoomUser));
    for (AttachedFile attachedFile : user.getAttachedFiles()) {
      assertEquals(user, attachedFile.getScreeningRoomUser());
    }
  }
  
  public void testReagentRelationship() throws IOException
  {
    schemaUtil.truncateTables();
    Reagent reagent = dataFactory.newInstance(SmallMoleculeReagent.class);
    ReagentAttachedFileType attachedFileType = new ReagentAttachedFileType("Application");
    genericEntityDao.saveOrUpdateEntity(attachedFileType);
    AttachedFile attachedFile1 = reagent.createAttachedFile("filename1", attachedFileType, new LocalDate(), new ReaderInputStream(new StringReader("file contents 1")));
    AttachedFile attachedFile2 = reagent.createAttachedFile("filename2", attachedFileType, new LocalDate(), "file contents 2");
    genericEntityDao.saveOrUpdateEntity(reagent.getLibraryContentsVersion().getLibrary());

    reagent = genericEntityDao.reloadEntity(reagent);
    AttachedFile attachedFile1b = genericEntityDao.findEntityById(AttachedFile.class, attachedFile1.getEntityId(), true, AttachedFile.reagent);
    AttachedFile attachedFile2b = genericEntityDao.findEntityById(AttachedFile.class, attachedFile2.getEntityId(), true, AttachedFile.reagent);
    assertEquals(reagent, attachedFile1b.getReagent());
    assertEquals(reagent, attachedFile2b.getReagent());
  }
}