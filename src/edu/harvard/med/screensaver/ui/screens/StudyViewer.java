// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screens;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.screenresults.AnnotationViewer;

import org.apache.log4j.Logger;

public class StudyViewer extends AbstractBackingBean
{
  // static members

  private static Logger log = Logger.getLogger(StudyViewer.class);


  // instance data members

  private GenericEntityDAO _dao;
  private StudyDetailViewer _studyDetailViewer;
  private AnnotationViewer _annotationViewer;

  private Screen _study;
  private boolean _showNavigationBar;

  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected StudyViewer()
  {
  }

  public StudyViewer(GenericEntityDAO dao,
                     StudyDetailViewer studyDetailViewer,
                     AnnotationViewer annotationViewer)
  {
    _dao = dao;
    _studyDetailViewer = studyDetailViewer;
    _annotationViewer = annotationViewer;
  }


  // public methods

  public void setStudy(Screen study)
  {
    _study = study;
    _studyDetailViewer.setStudy(study);
    _annotationViewer.setStudy(study);
  }

  public Screen getStudy()
  {
    return _study;
  }

  /**
   * @motivation for JSF saveState component
   */
  public void setShowNavigationBar(boolean showNavigationBar)
  {
    _showNavigationBar = showNavigationBar;
  }

  public boolean isShowNavigationBar()
  {
    return _showNavigationBar;
  }


  // private methods

}

