// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.StringReader;

import junit.framework.TestSuite;

import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenAttachedFileType;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.UserAttachedFileType;

import org.hibernate.lob.ReaderInputStream;

public class AttachedFileTest extends AbstractEntityInstanceTest<AttachedFile>
{
  public static TestSuite suite()
  {
    return buildTestSuite(AttachedFileTest.class, AttachedFile.class);
  }

  public AttachedFileTest() throws IntrospectionException
  {
    super(AttachedFile.class);
  }
  
  public void testScreenRelationship() throws IOException
  {
    schemaUtil.truncateTablesOrCreateSchema();
    Screen screen = dataFactory.newInstance(Screen.class);
    AttachedFileType attachedFileType = new ScreenAttachedFileType("Application");
    genericEntityDao.saveOrUpdateEntity(attachedFileType);
    AttachedFile attachedFile1 = screen.createAttachedFile("filename1", attachedFileType, new ReaderInputStream(new StringReader("file contents 1")));
    AttachedFile attachedFile2 = screen.createAttachedFile("filename2", attachedFileType, "file contents 2");
    genericEntityDao.saveOrUpdateEntity(screen);

    screen = genericEntityDao.reloadEntity(screen);
    AttachedFile attachedFile1b = genericEntityDao.findEntityById(AttachedFile.class, attachedFile1.getEntityId(), true, "screen");
    AttachedFile attachedFile2b = genericEntityDao.findEntityById(AttachedFile.class, attachedFile2.getEntityId(), true, "screen");
    assertEquals(screen, attachedFile1b.getScreen());
    assertEquals(screen, attachedFile2b.getScreen());
  }
  
  public void testScreeningUserRelationship() throws IOException
  {
    schemaUtil.truncateTablesOrCreateSchema();
    ScreeningRoomUser user = dataFactory.newInstance(ScreeningRoomUser.class);
    AttachedFileType attachedFileType = new UserAttachedFileType("Application");
    genericEntityDao.saveOrUpdateEntity(attachedFileType);
    AttachedFile attachedFile1 = user.createAttachedFile("filename1", attachedFileType, new ReaderInputStream(new StringReader("file contents 1")));
    AttachedFile attachedFile2 = user.createAttachedFile("filename2", attachedFileType, "file contents 2");
    genericEntityDao.persistEntity(user);

    user = genericEntityDao.reloadEntity(user);
    AttachedFile attachedFile1b = genericEntityDao.findEntityById(AttachedFile.class, attachedFile1.getEntityId(), true, "screeningRoomUser");
    AttachedFile attachedFile2b = genericEntityDao.findEntityById(AttachedFile.class, attachedFile2.getEntityId(), true, "screeningRoomUser");
    assertEquals(user, attachedFile1b.getScreeningRoomUser());
    assertEquals(user, attachedFile2b.getScreeningRoomUser());
  }
}