// $HeadURL:  $
// $Id: WellFinder.java 5158 2011-01-06 14:26:53Z atolopko $
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
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.ui.arch.view.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.arch.view.aspects.UICommand;

/**
 *
 */
public class WellByCompoundNameFinder extends AbstractBackingBean
{
  private static final Logger log = Logger.getLogger(WellByCompoundNameFinder.class);

  private GenericEntityDAO _dao;
  private LibrariesDAO _librariesDao;
  private WellSearchResults _wellsBrowser;

  private String _compoundSearchName;

  /**
   * @motivation for CGLIB2
   */
  protected WellByCompoundNameFinder()
  {
  }

  public WellByCompoundNameFinder(GenericEntityDAO dao,
                    LibrariesDAO librariesDao,
                                  WellSearchResults wellsBrowser)
  {
    _dao = dao;
    _librariesDao = librariesDao;
    _wellsBrowser = wellsBrowser;
  }

  @UICommand
  public String findWells()
  {
    Set<WellKey> wellKeys = _librariesDao.findWellKeysForCompoundName(_compoundSearchName);

    _wellsBrowser.searchWells(wellKeys);
    if (_wellsBrowser.getRowCount() == 1) {
      _wellsBrowser.getRowsPerPageSelector().setSelection(1);
    }
    _wellsBrowser.getColumnManager().setVisibilityOfColumnsInGroup("Compound Reagent Columns", true);
    resetSearchFields();
    return BROWSE_WELLS;
  }

  private void resetSearchFields()
  {
    _compoundSearchName = null;
  }

  public void setCompoundSearchName(String compoundSearchName)
  {
    _compoundSearchName = compoundSearchName;
  }

  public String getCompoundSearchName()
  {
    return _compoundSearchName;
  }
}
