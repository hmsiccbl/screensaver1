//$HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/ui/screenresults/ScreenResultViewer.java $
//$Id: ScreenResultViewer.java 1806 2007-09-10 13:55:05Z ant4 $

//Copyright 2006 by the President and Fellows of Harvard College.

//Screensaver is an open-source project developed by the ICCB-L and NSRB labs
//at Harvard Medical School. This software is distributed under the terms of
//the GNU General Public License.

package edu.harvard.med.screensaver.ui.annotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.screenresults.ScreenResultViewer;
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
    // HACK: the "annotationTypes" panel must be expanded initially, if all metadata type selections are to be selected on initialization (MetaDataTable.initialize())
    _isPanelCollapsedMap.put("annotationTypes", false);
    _isPanelCollapsedMap.put("annotationValues", true);
  }


  // public methods

  public void setStudy(Study study)
  {
    _study = study;
    ArrayList annotationTypes = new ArrayList(_study.getAnnotationTypes());
    getAnnotationTypesTable().initialize(annotationTypes, this);
    updateAnnotationValuesTable(annotationTypes);
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
    return getAnnotationTypesTable().getSelector();
  }

  public AnnotationTypesTable getAnnotationTypesTable()
  {
    return _annotationTypesTable;
  }

  public AnnotationValuesTable getAnnotationValuesTable()
  {
    return _annotationValuesTable;
  }

  public void update(Observable observable, Object selections)
  {
    // annotation type selections changed
    // TODO: make use of TableSortManager.getColumnModel().updateVisibleColumns(), instead of rebuilding data table backing bean wholesale
    updateAnnotationValuesTable((List<AnnotationType>) selections);
  }


  // JSF application methods


  // protected methods

  protected ScreensaverUserRole getEditableAdminRole()
  {
    return ScreensaverUserRole.SCREEN_RESULTS_ADMIN;
  }


  // private methods

  private void updateAnnotationValuesTable(List<AnnotationType> annotationTypes)
  {
    log.debug("updating annotation values table content");
    _annotationValuesTable.setAnnotationTypes(annotationTypes);
  }

}
