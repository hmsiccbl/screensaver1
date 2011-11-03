// $HeadURL:
// http://forge.abcd.harvard.edu/svn/screensaver/branches/iccbl/2.2.2-dev/src/edu/harvard/med/screensaver/ui/users/ScreenerFinder.java
// $
// $Id: ScreenerFinder.java 4689 2010-09-24 18:40:57Z atolopko $
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.io.libraries.LibraryCopyPlateListParser;
import edu.harvard.med.screensaver.io.libraries.LibraryCopyPlateListParserResult;
import edu.harvard.med.screensaver.ui.arch.view.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.arch.view.aspects.UICommand;

/**
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class LibraryCopyPlateFinder extends AbstractBackingBean
{
  private static final Logger log = Logger.getLogger(LibraryCopyPlateFinder.class);

  private LibraryCopyPlateSearchResults _libraryCopyPlatesBrowser;
  private GenericEntityDAO _dao;
  private LibrariesDAO _librariesDao;
  private String _plateCopyInput;

  /**
   * @motivation for CGLIB2
   */
  protected LibraryCopyPlateFinder()
  {}

  public LibraryCopyPlateFinder(GenericEntityDAO dao,
                                LibrariesDAO librariesDao,
                                LibraryCopyPlateSearchResults libraryCopyPlatesBrowser)
  {
    _libraryCopyPlatesBrowser = libraryCopyPlatesBrowser;
    _dao = dao;
    _librariesDao = librariesDao;
  }

  @UICommand
  public String findPlates()
  {
    List<LibraryCopyPlateListParserResult> results = LibraryCopyPlateListParser.parsePlateCopiesSublists(_plateCopyInput);
    resetSearchFields();
    for (LibraryCopyPlateListParserResult result : results) {
      if (result.hasErrors()) {
        for (String error : result.getErrors()) {
          showMessage("libraries.invalidCopyPlateInput", error);
        }
        return REDISPLAY_PAGE_ACTION_RESULT;
      }
    }

    Set<Integer> plateIds = Sets.newHashSet();

    for (LibraryCopyPlateListParserResult result : results) {
      plateIds.addAll(_librariesDao.queryForPlateIds(result));
    }
    if (plateIds.isEmpty()) {
      showMessage("libraries.noCopyPlatesFoundForSearch", results);
    }

    if (log.isDebugEnabled()) {
      log.debug("plate ids found: " + plateIds);
    }

    Joiner joiner = Joiner.on("; ");
    _libraryCopyPlatesBrowser.searchForPlates(joiner.join(results), plateIds);

    return BROWSE_LIBRARY_COPY_PLATES;
  }

  private void resetSearchFields()
  {
    _plateCopyInput = null;
  }

  public String getPlateCopyInput()
  {
    return _plateCopyInput;
  }

  public void setPlateCopyInput(String plateCopyInput)
  {
    _plateCopyInput = plateCopyInput;
  }
}
