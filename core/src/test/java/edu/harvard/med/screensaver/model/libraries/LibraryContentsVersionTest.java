// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import junit.framework.TestSuite;

import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;

public class LibraryContentsVersionTest extends AbstractEntityInstanceTest<LibraryContentsVersion>
{
  public static TestSuite suite()
  {
    return buildTestSuite(LibraryContentsVersionTest.class, LibraryContentsVersion.class);
  }

  public LibraryContentsVersionTest()
  {
    super(LibraryContentsVersion.class);
  }
}

