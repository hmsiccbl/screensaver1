// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries;

import java.io.InputStream;

import edu.harvard.med.screensaver.model.libraries.Library;

/**
 * Loads the contents (either partial or complete) of an RNAi library
 * from an Excel spreadsheet.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class RNAiLibraryContentsLoader implements LibraryContentsLoader
{

  /**
   * Load library contents (either partial or complete) from an input
   * stream of an Excel spreadsheet into a library.
   * @param library the library to load contents of
   * @param stream the input stream to load library contents from
   * @return the library with the contents loaded
   */
  public Library loadLibraryContents(Library library, InputStream stream)
  {
    return library;
  }
}
