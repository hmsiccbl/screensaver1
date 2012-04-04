// $HeadURL:
// http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/lincs/ui-cleanup/core/src/main/java/edu/harvard/med/screensaver/ui/screens/StudyViewer.java
// $
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screens;

import java.net.URL;
import java.util.ArrayList;
import java.util.SortedSet;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;

import edu.harvard.med.lincs.screensaver.LincsScreensaverConstants;
import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.io.image.ImageLocatorUtil;
import edu.harvard.med.screensaver.io.screens.StudyImageLocator;
import edu.harvard.med.screensaver.model.AttachedFile;
import edu.harvard.med.screensaver.model.AttachedFileType;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenAttachedFileType;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.ui.arch.searchresults.EntitySearchResults;
import edu.harvard.med.screensaver.ui.arch.util.AttachedFiles;
import edu.harvard.med.screensaver.ui.arch.view.SearchResultContextEntityViewerBackingBean;
import edu.harvard.med.screensaver.ui.libraries.AnnotationSearchResults;
import edu.harvard.med.screensaver.ui.libraries.WellSearchResults;
import edu.harvard.med.screensaver.util.NullSafeUtils;

public class StudyViewer<E extends Study> extends SearchResultContextEntityViewerBackingBean<E,E>
{
  private static Logger log = Logger.getLogger(StudyViewer.class);

  private StudyDetailViewer _studyDetailViewer;
  private AnnotationTypesTable _annotationTypesTable;
  private WellSearchResults _wellSearchResults;
  private WellSearchResults _reagentsBrowser;
  private StudyImageLocator _studyImageLocator; // LINCS-only feature
  private AnnotationSearchResults _annotationSearchResults;
  private AttachedFiles _attachedFiles;

  /**
   * @motivation for CGLIB2
   */
  protected StudyViewer()
  {}

  public StudyViewer(StudyViewer thisProxy,
                     StudyDetailViewer studyDetailViewer,
                     StudySearchResults studiesBrowser,
                     GenericEntityDAO dao,
                     AnnotationTypesTable annotationTypesTable,
                     WellSearchResults wellSearchResults,
                     AnnotationSearchResults annotationSearchResults,
                     StudyImageLocator studyImageLocator,
                     AttachedFiles attachedFiles)
  {
    super(thisProxy,
          (Class<E>) Screen.class,
          ScreensaverConstants.BROWSE_STUDIES,
          ScreensaverConstants.VIEW_STUDY,
          dao,
          (EntitySearchResults<E,E,?>) studiesBrowser);
    _studyDetailViewer = studyDetailViewer;
    _annotationTypesTable = annotationTypesTable;
    _wellSearchResults = wellSearchResults;
    _studyImageLocator = studyImageLocator;
    _annotationSearchResults = annotationSearchResults;
    _attachedFiles = attachedFiles;

    getIsPanelCollapsedMap().put("reagentsData", false);
  }

  protected StudyViewer(Class<E> entityClass,
                        StudyViewer thisProxy,
                        EntitySearchResults<E,E,?> studiesBrowser,
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
          (EntitySearchResults<E,E,?>) studiesBrowser);
    _annotationTypesTable = annotationTypesTable;
    _wellSearchResults = wellSearchResults;

    getIsPanelCollapsedMap().put("reagentsData", false);
  }

  public WellSearchResults getWellSearchResults()
  {
    return _wellSearchResults;
  }

  public AnnotationSearchResults getAnnotationSearchResults()
  {
    return _annotationSearchResults;
  }

  public AnnotationTypesTable getAnnotationTypesTable()
  {
    return _annotationTypesTable;
  }

  public String getStudyImageUrl()
  {
    if (_studyImageLocator == null) return null;

    URL url = _studyImageLocator.getImageUrl((Screen) getEntity());
    return NullSafeUtils.toString(ImageLocatorUtil.toExtantContentUrl(url), "");
  }

  @Override
  protected void initializeEntity(E study)
  {
    getDao().needReadOnly((Screen) study, Screen.labHead.to(LabHead.labMembers));
    getDao().needReadOnly((Screen) study, Screen.leadScreener);
    getDao().needReadOnly((Screen) study, Screen.collaborators);
    getDao().needReadOnly((Screen) study, Screen.publications);
    getDao().needReadOnly((Screen) study, Screen.annotationTypes);
  }

  @Override
  protected void initializeViewer(E study)
  {
    if (study.isStudyOnly()) {
      _annotationTypesTable.initialize(new ArrayList<AnnotationType>(study.getAnnotationTypes()));
      if (getApplicationProperties().isFacility(LincsScreensaverConstants.FACILITY_KEY)) {
        _annotationSearchResults.setStudyViewerMode();
        _annotationSearchResults.searchForCanonicalAnnotations((Screen) study);
      }
      else {
        _wellSearchResults.searchReagentsForStudy(study);
      }
      _studyDetailViewer.setEntity(study);

      if (((Screen) study).getWellStudied() != null) {
        initalizeAttachedFiles((Screen) study);
      }
      else {
        initalizeAttachedFiles(null);
      }
    }
  }

  private void initalizeAttachedFiles(Screen screen)
  {
    getDao().needReadOnly(screen, Screen.attachedFiles);
    getDao().needReadOnly(screen, Screen.attachedFiles.to(AttachedFile.fileType));
    SortedSet<AttachedFileType> attachedFileTypes =
        Sets.<AttachedFileType>newTreeSet(getDao().findAllEntitiesOfType(ScreenAttachedFileType.class, true));
    _attachedFiles.initialize(screen, attachedFileTypes, new Predicate<AttachedFile>() {
      @Override
      public boolean apply(AttachedFile input)
      {
        return input.getFileType().getValue().equals(ScreensaverConstants.STUDY_FILE_TYPE);
      }
    });
  }

  public AttachedFiles getAttachedFiles()
  {
    return _attachedFiles;
  }
  
  public String getWellStudiedLabel()
  {
    Well well = ((Screen) getEntity()).getWellStudied();
    if (well == null) return null;
    if (!!!getApplicationProperties().isFacility(LincsScreensaverConstants.FACILITY_KEY)) {
      return well.getWellKey().toString();
    }
    else {
      if (!(well.getLatestReleasedReagent() instanceof SmallMoleculeReagent)) return null;
      return well.getFacilityId() + "-" + ((SmallMoleculeReagent) well.getLatestReleasedReagent()).getSaltFormId() + "-" +
        well.getLatestReleasedReagent().getFacilityBatchId();
    }
  }

}
