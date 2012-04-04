// $HeadURL: http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/serickson/3200/web/src/main/java/edu/harvard/med/screensaver/ui/libraries/LibraryViewer.java $
// $Id: LibraryViewer.java 6946 2012-01-13 18:24:30Z seanderickson1 $
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.cells;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.CellsDAO;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.cells.ExperimentalCellInformation;
import edu.harvard.med.screensaver.ui.arch.view.SearchResultContextEntityViewerBackingBean;
import edu.harvard.med.screensaver.ui.screens.ScreenSearchResults;

/**
 * This class will not be used often, it is the link, required by the SCEVBC hierarchy,  in order to link Cells-ExperimentalCellInformation-Screen
 * - it will only be used if viewing a single ExperimentalCellInformation in the UI, ordinarily user will click through to Screen instead
 */
public class ScreenCellViewer extends SearchResultContextEntityViewerBackingBean<ExperimentalCellInformation,ExperimentalCellInformation>
{
  private static Logger log = Logger.getLogger(ScreenCellViewer.class);

	private ScreenSearchResults _screenSearchResults;


  /**
   * @motivation for CGLIB2
   */
  protected ScreenCellViewer()
  {
  }

  public ScreenCellViewer(ScreenCellViewer thisProxy,
                       ScreenCellSearchResults screenCellSearchResults,
                       GenericEntityDAO dao)
  {
    super(thisProxy,
          ExperimentalCellInformation.class,
          BROWSE_CELLS,
          VIEW_CELL,
          dao,
          screenCellSearchResults);
  }


  @Override
  protected void initializeEntity(ExperimentalCellInformation cell)
  {
    // getDao().needReadOnly(cell, ExperimentalCellInformation.screens);
  }
  
  @Override
  protected void initializeViewer(ExperimentalCellInformation library)
  {
  }
}
