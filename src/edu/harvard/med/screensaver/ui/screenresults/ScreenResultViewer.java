//$HeadURL$
// $Id$
//
//Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
//Screensaver is an open-source project developed by the ICCB-L and NSRB labs
//at Harvard Medical School. This software is distributed under the terms of
//the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults;

import java.io.File;
import java.util.Collections;

import edu.harvard.med.screensaver.ScreensaverProperties;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.ScreenResultsDAO;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.service.cellhts2.CellHts2Annotator;
import edu.harvard.med.screensaver.ui.EditResult;
import edu.harvard.med.screensaver.ui.EditableEntityViewerBackingBean;
import edu.harvard.med.screensaver.ui.UICommand;
import edu.harvard.med.screensaver.ui.screenresults.cellhts2.CellHTS2Runner;
import edu.harvard.med.screensaver.ui.screens.ScreenViewer;
import edu.harvard.med.screensaver.ui.searchresults.WellSearchResults;
import edu.harvard.med.screensaver.util.DeleteDir;

import org.apache.log4j.Logger;


/**
 * JSF backing bean for Screen Result Viewer web page (screenresultviewer.jsp).
 * <p>
 * The <code>screenResult</code> property should be set to the
 * {@link ScreenResult} that is to be viewed.<br>

 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
@SuppressWarnings("unchecked")
public class ScreenResultViewer extends EditableEntityViewerBackingBean<ScreenResult>
{
  private static Logger log = Logger.getLogger(ScreenResultViewer.class);

  private ScreenResultsDAO _screenResultsDao;
  private ScreenResultDataColumnsTables _screenResultDataColumnsTables;
  private WellSearchResults _wellSearchResults;
  private CellHts2Annotator _cellHts2Annotator;

  private boolean _isWellSearchResultsInitialized;

  private String _cellHTS2ReportFilePath;
  private CellHTS2Runner _cellHTS2Runner;

  private ScreenViewer _screenViewer;


  /**
   * @motivation for CGLIB2
   */
  protected ScreenResultViewer()
  {
  }

  public ScreenResultViewer(ScreenResultViewer thisProxy,
                            GenericEntityDAO dao,
                            ScreenResultsDAO screenResultsDao,
                            ScreenViewer screenViewer,
                            ScreenResultDataColumnsTables screenResultDataColumnsTables,
                            WellSearchResults wellSearchResults,
                            CellHts2Annotator cellHts2Annotator,
                            CellHTS2Runner cellHTS2Runner)

  {
    super(thisProxy,
          ScreenResult.class,
          EDIT_SCREEN_RESULT,
          dao);
    _screenResultsDao = screenResultsDao;
    _screenViewer = screenViewer;
    _screenResultDataColumnsTables = screenResultDataColumnsTables;
    _wellSearchResults = wellSearchResults;
    _cellHts2Annotator = cellHts2Annotator;
    _cellHTS2Runner = cellHTS2Runner;

    getIsPanelCollapsedMap().put("screenResultSummary", false);
    getIsPanelCollapsedMap().put("dataColumnsTable", true);
    getIsPanelCollapsedMap().put("dataTable", true);
    getIsPanelCollapsedMap().put("heatMaps", true);
  }
  
  public String postEditAction(ScreenResult entity)
  {
    if (entity == null) {
      return VIEW_MAIN;
    }
    return _screenViewer.viewEntity(entity.getScreen());
  }
  
  @Override
  protected void initializeEntity(ScreenResult entity)
  {
  }

  /**
   * @param screenResult can be null
   */
  @Override
  protected void initializeViewer(ScreenResult screenResult)
  {
    // lazy initialization of _wellSearchResults, for performance (avoid expense of determining columns, if not being viewed)
    _wellSearchResults.searchWellsForScreenResult(null);
    _isWellSearchResultsInitialized = false;

    // open viewer with dataTable panel closed, to avoid expense of initializing unless user explicitly requests to view it, while scrolling through multiple screen results
    getIsPanelCollapsedMap().put("dataTable", true);
    getIsPanelCollapsedMap().put("heatMaps", true);

    if (screenResult == null) {
      _screenResultDataColumnsTables.initialize(Collections.<DataColumn>emptyList());
    }
    else {
      _screenResultDataColumnsTables.initialize(screenResult.getDataColumnsList());
      _cellHTS2ReportFilePath = ScreensaverProperties.getProperty("cellHTS2report.filepath.base") + 
         ScreensaverProperties.getProperty("cellHTS2report.filepath.prefix") + 
         screenResult.getScreenResultId();
    }
  }

  public ScreenResultDataColumnsTables getDataColumnsTable()
  {
    return _screenResultDataColumnsTables;
  }

  public WellSearchResults getResultValueTable()
  {
      // lazy initialization of _wellSearchResults, for performance (avoid expense of determining columns, if not being viewed)
    if (!_isWellSearchResultsInitialized && !getIsPanelCollapsedMap().get("dataTable")) {
      _wellSearchResults.searchWellsForScreenResult(getEntity());
      _isWellSearchResultsInitialized = true;
    }
    return _wellSearchResults;
  }


  // JSF application methods

  @UICommand
  public String delete()
  {
    if (getEntity() != null) {
      _screenResultsDao.deleteScreenResult(getEntity());
      
      // Delete directory and all files under it
      DeleteDir.deleteDirectory(new File(_cellHTS2ReportFilePath));
      log.info("Screen result file deleted");
      
      return cancel();
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  public boolean isCellHTS2ReportFileExists() 
  {
	  File file = new File(_cellHTS2ReportFilePath);
	  return file.exists();
  }
  
  public String getReportURL() {
	  String contextRoot = WEBAPP_ROOT.substring(WEBAPP_ROOT.lastIndexOf("/", WEBAPP_ROOT.length()-2), WEBAPP_ROOT.length());
	  return contextRoot + ScreensaverProperties.getProperty("cellHTS2report.filepath.prefix") + getEntity().getScreenResultId()+"/index.html";
  }
  
  @UICommand
  public String viewCellHTS2Runner()
  {
	  return _cellHTS2Runner.viewCellHTS2Runner(getEntity());
  }

  @Override
  protected String postEditAction(EditResult editResult)
  {
    switch (editResult) {
    case CANCEL_EDIT: return _screenViewer.reload();
    case SAVE_EDIT: return _screenViewer.reload();
    default: return null;
    }
  }
}
