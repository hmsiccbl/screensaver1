//$HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/ui/screenresults/ScreenResultViewer.java $
//$Id: ScreenResultViewer.java 1806 2007-09-10 13:55:05Z ant4 $

//Copyright 2006 by the President and Fellows of Harvard College.

//Screensaver is an open-source project developed by the ICCB-L and NSRB labs
//at Harvard Medical School. This software is distributed under the terms of
//the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.util.UISelectManyBean;

import org.apache.log4j.Logger;


/**
 * JSF backing bean for Annotation Viewer web page (annotationViewer.jspf).
 * <p>
 * The <code>study</code> property should be set to the
 * {@link Study} whose annotations are to be viewed.<br>

 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
@SuppressWarnings("unchecked")
public class AnnotationViewer extends AbstractBackingBean implements Observer
{

  // static data members

  private static Logger log = Logger.getLogger(ScreenResultViewer.class);

  // instance data members

  private GenericEntityDAO _dao;
  //private ScreenAnnotationExporter _screenAnnotationExporter;

  private Study _study;
  private AnnotationTypesTable _annotationTypesTable;
  private AnnotationValuesTable _annotationValuesTable;
  private Map<String,Boolean> _isPanelCollapsedMap;


  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected AnnotationViewer()
  {
  }

  public AnnotationViewer(GenericEntityDAO dao,
                          AnnotationTypesTable annotationTypesTable,
                          AnnotationValuesTable annotationValuesTable)

  {
    _dao = dao;
    _annotationTypesTable = annotationTypesTable;
    _annotationValuesTable = annotationValuesTable;
    _isPanelCollapsedMap = new HashMap<String,Boolean>();
    _isPanelCollapsedMap.put("annotations", true);
    _isPanelCollapsedMap.put("annotationTypes", true);
    _isPanelCollapsedMap.put("annotationValues", true);
  }


  // public methods

  public void setStudy(Study study)
  {
    _study = study;
    getAnnotationTypesTable().initialize(new ArrayList(_study.getAnnotationTypes()), this);
    updateAnnotationValuesTable();
  }

  public Study getStudy()
  {
    return _study;
  }

  public Map<?,?> getIsPanelCollapsedMap()
  {
    return _isPanelCollapsedMap;
  }

  public UISelectManyBean<AnnotationType> getAnnotationTypeSelections()
  {
    return getAnnotationTypesTable().getSelections();
  }

  public AnnotationTypesTable getAnnotationTypesTable()
  {
    return _annotationTypesTable;
  }

  public AnnotationValuesTable getAnnotationValuesTable()
  {
    return _annotationValuesTable;
  }

  public void update(Observable observable, Object o)
  {
    // annotation type selections changed
    // TODO: make use of TableSortManager.getColumnModel().updateVisibleColumns(), instead of rebuilding data table backing bean wholesale
    updateAnnotationValuesTable();
  }


  // JSF application methods

  @UIControllerMethod
  public String download()
  {
//    try {
//      _dao.doInTransaction(new DAOTransaction()
//      {
//        public void runTransaction()
//        {
//          ScreenResult screenResult = _dao.reloadEntity(_screen,
//                                                        true,
//                                                        "annotationTypes");
//          // note: we eager fetch the annotation values for each annotation type
//          // individually, since fetching all with a single needReadOnly() call
//          // would generate an SQL result cross-product for all types+values that
//          // would include a considerable amount of redundant data
//          // for the (denormalized) RVT fields
//          for (ResultValueType rvt : screenResult.getResultValueTypes()) {
//            // note: requesting the iterator generates an SQL statement that
//            // only includes the result_value_type_result_values table, whereas
//            // the needReadOnly() call's SQL statement joins to the
//            // result_value_type table as well, which is slower
//            rvt.getResultValues().keySet().iterator();
//            //_dao.needReadOnly(rvt, "resultValues");
//          }
//          File exportedWorkbookFile = null;
//          FileOutputStream out = null;
//          try {
//            if (screenResult != null) {
//              HSSFWorkbook workbook = _screenResultExporter.build(screenResult);
//              exportedWorkbookFile = File.createTempFile("screenResult" + screenResult.getScreen().getScreenNumber() + ".",
//              ".xls");
//              out = new FileOutputStream(exportedWorkbookFile);
//              workbook.write(out);
//              out.close();
//              JSFUtils.handleUserFileDownloadRequest(getFacesContext(),
//                                                     exportedWorkbookFile,
//                                                     Workbook.MIME_TYPE);
//            }
//          }
//          catch (IOException e)
//          {
//            reportApplicationError(e);
//          }
//          finally {
//            IOUtils.closeQuietly(out);
//            if (exportedWorkbookFile != null && exportedWorkbookFile.exists()) {
//              exportedWorkbookFile.delete();
//            }
//          }
//        }
//      });
//    }
//    catch (DataAccessException e) {
//      showMessage("databaseOperationFailed", e.getMessage());
//    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }


  // protected methods

  protected ScreensaverUserRole getEditableAdminRole()
  {
    return ScreensaverUserRole.SCREEN_RESULTS_ADMIN;
  }


  // private methods

  /**
   * Get AnnotationTypes set, safely, handling case that no screen result
   * exists.
   */
  private List<AnnotationType> getAnnotationTypes()
  {
    List<AnnotationType> rvts = new ArrayList<AnnotationType>();
    rvts.addAll(_study.getAnnotationTypes());
    return rvts;
  }

  private void updateAnnotationValuesTable()
  {
    log.debug("updating annotation values table content");
    _annotationValuesTable.setAnnotationTypes(getAnnotationTypeSelections().getSelections());
  }

}
