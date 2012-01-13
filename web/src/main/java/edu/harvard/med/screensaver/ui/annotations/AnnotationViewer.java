// $HeadURL: http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/go/trunk/src/edu/harvard/med/screensaver/ui/libraries/WellViewer.java $
// $Id: WellViewer.java 4446 2010-07-27 18:36:17Z seanderickson1 $
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.annotations;

import java.io.Serializable;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;
import edu.harvard.med.screensaver.policy.EntityViewPolicy;
import edu.harvard.med.screensaver.ui.arch.view.SearchResultContextEntityViewerBackingBean;
import edu.harvard.med.screensaver.ui.libraries.AnnotationSearchResults;
import edu.harvard.med.screensaver.ui.screens.StudyViewer;

public class AnnotationViewer extends SearchResultContextEntityViewerBackingBean<AnnotationValue,AnnotationValue>
{
  
  private static final Logger log = Logger.getLogger(AnnotationViewer.class);

  private EntityViewPolicy _entityViewPolicy;
  private StudyViewer _studyViewer;

  
  /**
   * @motivation for CGLIB2
   */
  protected AnnotationViewer() {}

  public AnnotationViewer(AnnotationViewer thisProxy,
                          AnnotationSearchResults annotationSearchResults,
                          GenericEntityDAO dao,
                          EntityViewPolicy entityViewPolicy,
                          StudyViewer studyViewer)
  {
    super(thisProxy,
          AnnotationValue.class,
          ScreensaverConstants.BROWSE_ANNOTATIONS,
          ScreensaverConstants.VIEW_ANNOTATION,
          dao,
          annotationSearchResults);

    _entityViewPolicy = entityViewPolicy;
    _studyViewer = studyViewer;
    //getIsPanelCollapsedMap().put("studyHeaders", Boolean.TRUE);
  }

  @Override
  protected void initializeEntity(AnnotationValue av)
  {
    getDao().needReadOnly(av, AnnotationValue.annotationType);
  }

  @Override
  protected void initializeViewer(AnnotationValue av)
  {
  }

  @Override
  protected Serializable convertEntityId(String entityIdAsString)
  {
    return entityIdAsString;
  }

}
