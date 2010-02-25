// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screens;

import java.util.ArrayList;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.ui.SearchResultContextEntityViewerBackingBean;
import edu.harvard.med.screensaver.ui.annotations.AnnotationTypesTable;
import edu.harvard.med.screensaver.ui.searchresults.EntitySearchResults;
import edu.harvard.med.screensaver.ui.searchresults.StudySearchResults;
import edu.harvard.med.screensaver.ui.searchresults.WellSearchResults;

import org.apache.log4j.Logger;


public class StudyViewer<E extends Study> extends SearchResultContextEntityViewerBackingBean<E>
{
  private static Logger log = Logger.getLogger(StudyViewer.class);

  private StudyDetailViewer _studyDetailViewer;
  private AnnotationTypesTable _annotationTypesTable;
  private WellSearchResults _wellsBrowser;


  /**
   * @motivation for CGLIB2
   */
  protected StudyViewer()
  {
  }

  public StudyViewer(StudyViewer thisProxy,
                     StudyDetailViewer studyDetailViewer,
                     StudySearchResults studiesBrowser,
                     GenericEntityDAO dao,
                     AnnotationTypesTable annotationTypesTable,
                     WellSearchResults wellsBrowser)
  {
    super(thisProxy,
          (Class<E>) Study.class,
          ScreensaverConstants.BROWSE_STUDIES,
          ScreensaverConstants.VIEW_STUDY,
          dao,
          (EntitySearchResults<E,?>) studiesBrowser);
    _studyDetailViewer = studyDetailViewer;
    _annotationTypesTable = annotationTypesTable;
    _wellsBrowser = wellsBrowser;

    getIsPanelCollapsedMap().put("reagentsData", false);
  }

  protected StudyViewer(Class<E> entityClass,
                        StudyViewer thisProxy,
                        EntitySearchResults<E,?> studiesBrowser,
                        String browserActionResult,
                        String viewerActionResult,
                        GenericEntityDAO dao,
                        AnnotationTypesTable annotationTypesTable,
                        WellSearchResults wellSearchResults)
  {
    super(thisProxy,
          entityClass,
          browserActionResult,
          viewerActionResult,
          dao,
          (EntitySearchResults<E,?>) studiesBrowser);
    _annotationTypesTable = annotationTypesTable;
    _wellsBrowser = wellSearchResults;

    getIsPanelCollapsedMap().put("reagentsData", false);
  }

  public WellSearchResults getWellsBrowser()
  {
    return _wellsBrowser;
  }

  public AnnotationTypesTable getAnnotationTypesTable()
  {
    return _annotationTypesTable;
  }

  @Override
  protected void initializeEntity(E study)
  {
    getDao().needReadOnly(study, "labHead.labMembers", "leadScreener");
    getDao().needReadOnly((Screen) study, "collaborators"); 
    getDao().needReadOnly((Screen) study, "publications");
    getDao().needReadOnly((Screen) study, "annotationTypes");
  }

  @Override
  protected void initializeViewer(E study)
  {
    if (study.isStudyOnly()) {
      _annotationTypesTable.initialize(new ArrayList<AnnotationType>(study.getAnnotationTypes()));
      _wellsBrowser.searchReagentsForStudy(study);
      _studyDetailViewer.setEntity(study);
    }
  }
}

