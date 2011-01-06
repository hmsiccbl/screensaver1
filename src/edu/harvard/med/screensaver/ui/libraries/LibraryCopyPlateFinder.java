// $HeadURL: http://forge.abcd.harvard.edu/svn/screensaver/branches/iccbl/2.2.2-dev/src/edu/harvard/med/screensaver/ui/users/ScreenerFinder.java $
// $Id: ScreenerFinder.java 4689 2010-09-24 18:40:57Z atolopko $
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.util.Set;

import org.apache.log4j.Logger;

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
  {
  }

  public LibraryCopyPlateFinder(GenericEntityDAO dao,
                                LibrariesDAO librariesDao,
                                LibraryCopyPlateSearchResults libraryCopyPlatesBrowser)
  {
    _libraryCopyPlatesBrowser = libraryCopyPlatesBrowser;
    _dao = dao;
    _librariesDao = librariesDao;
  }


  @SuppressWarnings("unchecked")
  @UICommand
  public String findPlates()
  {
    LibraryCopyPlateListParserResult result = LibraryCopyPlateListParser.parsePlateCopies(_plateCopyInput);
    if (result.hasErrors()) {
      for (String error : result.getErrors()) {
        showMessage("libraries.invalidCopyPlateInput", error);
      }
      return REDISPLAY_PAGE_ACTION_RESULT;
    }

    Set<Integer> plateIds = _librariesDao.queryForPlateIds(result);
    if (plateIds.isEmpty()) {
      showMessage("libraries.noCopyPlatesFoundForSearch", result.print());
    }

    if (log.isDebugEnabled()) {
      log.debug("plate ids found: " + plateIds);
    }

    _libraryCopyPlatesBrowser.searchForPlates(result.print(), plateIds);

    resetSearchFields();
    return BROWSE_LIBRARY_COPY_PLATES;
  }

  //  @SuppressWarnings("unchecked")
  //  @UICommand
  //  public String findPlate()
  //  {
  //    Matcher matcher = PLATE_COPY_INPUT_PATTERN.matcher(_plateCopyInput);
  //    if (!!!matcher.matches()) {
  //      showMessage("libraries.invalidCopyPlateInput", _plateCopyInput);
  //      return REDISPLAY_PAGE_ACTION_RESULT;
  //    }
  //
  //    _libraryCopyPlatesBrowser.searchAll();
  //
  //    Integer plateNumber = Integer.valueOf(matcher.group(1));
  //    ((TableColumn<Plate,Integer>) _libraryCopyPlatesBrowser.getColumnManager().getColumn("Plate")).getCriterion().setOperatorAndValue(Operator.EQUAL, plateNumber);
  //
  //    String copyName = matcher.group(2);
  //    if (!copyName.isEmpty()) {
  //      ((TableColumn<Plate,String>) _libraryCopyPlatesBrowser.getColumnManager().getColumn("Copy")).getCriterion().setOperatorAndValue(Operator.EQUAL, copyName);
  //    }
  //
  //    resetSearchFields();
  //    return BROWSE_LIBRARY_COPY_PLATES;
  //  }

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

