// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.util;

import java.io.File;

import junit.framework.TestCase;

public class FileUtilsTest extends TestCase
{
  public void testModifyDirectoryAndExtension()
  {
    File file = new File("/tmp/file.txt");
    String newDirectory = "/usr/local";
    String newExtension = ".csv";
    File newFile = FileUtils.modifyFileDirectoryAndExtension(file,
                                                             newDirectory,
                                                             newExtension);
    assertEquals(new File("/usr/local/file.csv"), newFile);

    newExtension = "csv";
    newFile = FileUtils.modifyFileDirectoryAndExtension(file,
                                                        newDirectory,
                                                        newExtension);
    assertEquals(new File("/usr/local/file.csv"), newFile);

    newFile = FileUtils.modifyFileDirectoryAndExtension(file, (String) null, null);
    assertEquals(file, newFile);

  }
}
