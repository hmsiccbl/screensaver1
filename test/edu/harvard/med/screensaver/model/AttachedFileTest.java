// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.StringReader;

import junit.framework.TestSuite;
import org.hibernate.lob.ReaderInputStream;

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
    AttachedFileType attachedFileType = new ScreenAttachedFileType("Application");
    genericEntityDao.persistEntity(attachedFileType);
    screen.createAttachedFile("filename1", attachedFileType, new ReaderInputStream(new StringReader("file contents 1")));
    screen.createAttachedFile("filename2", attachedFileType, "file contents 2");
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
    AttachedFileType attachedFileType = new UserAttachedFileType("Application");
    genericEntityDao.persistEntity(attachedFileType);
    user.createAttachedFile("filename1", attachedFileType, new ReaderInputStream(new StringReader("file contents 1")));
    user.createAttachedFile("filename2", attachedFileType, "file contents 2");
    user = genericEntityDao.mergeEntity(user);

    user = genericEntityDao.reloadEntity(user, true, ScreeningRoomUser.attachedFiles.to(AttachedFile.screeningRoomUser));
    for (AttachedFile attachedFile : user.getAttachedFiles()) {
      assertEquals(user, attachedFile.getScreeningRoomUser());
    }
  }
}