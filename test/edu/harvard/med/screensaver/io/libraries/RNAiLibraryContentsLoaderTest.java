// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries;

import java.io.File;

import edu.harvard.med.screensaver.AbstractSpringTest;


public class RNAiLibraryContentsLoaderTest extends AbstractSpringTest
{
  public static final File TEST_INPUT_FILE_DIR =
    new File("test/edu/harvard/med/screensaver/io/libraries");
  
  protected RNAiLibraryContentsLoader rnaiLibraryContentsLoader;

  public void testSanity()
  {
    assertNotNull("loader is not null", rnaiLibraryContentsLoader);
  }
}
