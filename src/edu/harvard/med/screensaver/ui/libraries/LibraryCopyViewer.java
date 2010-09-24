// $HeadURL: http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/iccbl/2.1.0-dev/src/edu/harvard/med/screensaver/ui/users/UserViewer.java $
// $Id: UserViewer.java 4484 2010-08-04 19:20:14Z atolopko $
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.service.OperationRestrictedException;
import edu.harvard.med.screensaver.ui.SearchResultContextEntityViewerBackingBean;
import edu.harvard.med.screensaver.ui.searchresults.LibraryCopyPlateSearchResults;
import edu.harvard.med.screensaver.ui.searchresults.LibraryCopySearchResults;

/**
 */
public class LibraryCopyViewer extends SearchResultContextEntityViewerBackingBean<Copy,Copy>
{
  private static Logger log = Logger.getLogger(LibraryCopyViewer.class);

  private LibraryCopyDetail _libraryCopyDetail;
  private LibraryCopyPlateSearchResults _libraryCopyPlateSearchResults;

  /**
   * @motivation for CGLIB2
   */
  protected LibraryCopyViewer()
  {
  }

  public LibraryCopyViewer(LibraryCopyViewer libraryCopyViewerProxy,
                           GenericEntityDAO dao,
                           LibraryCopySearchResults libraryCopySearchResults,
                           LibraryCopyDetail libraryCopyDetail,
                           LibraryCopyPlateSearchResults libraryCopyPlateSearchResults)
  {
    super(libraryCopyViewerProxy,
          Copy.class,
          BROWSE_LIBRARY_COPIES,
          VIEW_LIBRARY_COPY,
          dao,
          libraryCopySearchResults);
    _libraryCopyDetail = libraryCopyDetail;
    _libraryCopyPlateSearchResults = libraryCopyPlateSearchResults;
  }

  public LibraryCopyPlateSearchResults getLibraryCopyPlateSearchResults()
  {
    return _libraryCopyPlateSearchResults;
  }

  @Override
  protected void initializeViewer(Copy copy)
  {
    if (!!!getScreensaverUser().isUserInRole(ScreensaverUserRole.READ_EVERYTHING_ADMIN)) {
      throw new OperationRestrictedException("view Library Copy");
    }
    _libraryCopyDetail.setEntity(copy);
    getLibraryCopyPlateSearchResults().searchPlatesForCopy(copy);
  }

  @Override
  protected void initializeEntity(Copy copy)
  {
    getDao().needReadOnly(copy, Copy.library.getPath());
  }
  
  /**
   * Get whether user can view any data in the current view.
   * 
   * @return <code>true</code> iff user can view any data in the current view
   */
  public boolean isLibraryCopiesAdmin()
  {
    return isUserInRole(ScreensaverUserRole.LIBRARY_COPIES_ADMIN);
  }
}

