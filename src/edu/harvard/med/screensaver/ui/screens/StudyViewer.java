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
import java.util.HashMap;
import java.util.Map;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.annotations.AnnotationTypesTable;
import edu.harvard.med.screensaver.ui.searchresults.ReagentSearchResults;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;


public class StudyViewer extends AbstractBackingBean
{
  // static members

  private static Logger log = Logger.getLogger(StudyViewer.class);


  // instance data members

  private GenericEntityDAO _dao;
  private StudyDetailViewer _studyDetailViewer;
  private AnnotationTypesTable _annotationTypesTable;
  private ReagentSearchResults _reagentSearchResults;

  private Study _study;
  private Map<String,Boolean> _isPanelCollapsedMap;

  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected StudyViewer()
  {
  }

  public StudyViewer(GenericEntityDAO dao,
                     StudyDetailViewer studyDetailViewer,
                     AnnotationTypesTable annotationTypesTable,
                     ReagentSearchResults reagentSearchResults)
  {
    _dao = dao;
    _annotationTypesTable = annotationTypesTable;
    _studyDetailViewer = studyDetailViewer;
    _reagentSearchResults = reagentSearchResults;

    _isPanelCollapsedMap = new HashMap<String,Boolean>();
    _isPanelCollapsedMap.put("reagentsData", false);
  }


  // public methods

  public Study getStudy()
  {
    return _study;
  }

  public ReagentSearchResults getReagentSearchResults()
  {
    return _reagentSearchResults;
  }

  public AnnotationTypesTable getAnnotationTypesTable()
  {
    return _annotationTypesTable;
  }

  public Map<?,?> getIsPanelCollapsedMap()
  {
    return _isPanelCollapsedMap;
  }

  /* JSF Application methods */

  @UIControllerMethod
  public String viewStudy(final Study studyIn)
  {
    // TODO: implement as aspect
    if (studyIn.isRestricted()) {
      showMessage("restrictedEntity", "Study " + ((Screen) studyIn).getScreenNumber());
      log.warn("user unauthorized to view " + studyIn);
      return REDISPLAY_PAGE_ACTION_RESULT;
    }

    try {
      _dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          Study study = _dao.reloadEntity(studyIn,
                                          true,
                                          "labHead.labMembers",
                                          "leadScreener");
          _dao.needReadOnly((Screen) study, "collaborators");
          _dao.needReadOnly((Screen) study, "publications");
          _dao.needReadOnly((Screen) study, "annotationTypes");
          setStudy(study);
        }
      });
    }
    catch (DataAccessException e) {
      showMessage("databaseOperationFailed", e.getMessage());
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    return VIEW_STUDY;
  }


  // protected methods

  protected void setStudy(Study study)
  {
    _study = study;
    _studyDetailViewer.setStudy(study);
    if (_study.isStudyOnly()) {
      _annotationTypesTable.initialize(new ArrayList<AnnotationType>(study.getAnnotationTypes()));
      _reagentSearchResults.searchReagentsForStudy(study);
    }
  }
}

