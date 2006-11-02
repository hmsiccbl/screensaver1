// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/ui/LibraryViewer.java $
// $Id: LibraryViewer.java 443 2006-08-09 20:43:32Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.view.libraries;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.searchresults.LibrarySearchResults;

/**
 * TODO: add comments
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class LibrariesBrowser extends AbstractBackingBean
{
  
  // private static fields
  
  private static Logger log = Logger.getLogger(LibrariesBrowser.class);
  
  
  // private instance fields

  private LibrarySearchResults _librarySearchResults;
  
  
  // public instance methods

  public LibrarySearchResults getLibrarySearchResults()
  {
    return _librarySearchResults;
  }

  public void setLibrarySearchResults(LibrarySearchResults librarySearchResults)
  {
    _librarySearchResults = librarySearchResults;
  }
}
