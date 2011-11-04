package edu.harvard.med.lincs.screensaver.ui.libraries;

import java.net.URL;
import java.util.SortedSet;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.io.libraries.WellsSdfDataExporter;
import edu.harvard.med.screensaver.io.libraries.smallmolecule.LibraryContentsVersionReference;
import edu.harvard.med.screensaver.io.libraries.smallmolecule.StructureImageProvider;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultReporter;
import edu.harvard.med.screensaver.io.screens.StudyImageProvider;
import edu.harvard.med.screensaver.model.AttachedFile;
import edu.harvard.med.screensaver.model.AttachedFileType;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.ReagentAttachedFileType;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.policy.EntityViewPolicy;
import edu.harvard.med.screensaver.ui.arch.util.AttachedFiles;
import edu.harvard.med.screensaver.ui.libraries.AnnotationSearchResults;
import edu.harvard.med.screensaver.ui.libraries.LibraryViewer;
import edu.harvard.med.screensaver.ui.libraries.WellSearchResults;
import edu.harvard.med.screensaver.ui.screens.StudyViewer;

public class WellViewer extends edu.harvard.med.screensaver.ui.libraries.WellViewer
{
  private static final Logger log = Logger.getLogger(WellViewer.class);

  private AttachedFiles _attachedFiles;
  private StudyImageProvider _studyImageProvider; // for [#2417] NOTE: this is a LINCS-only feature
  /**
   * @motivation for CGLIB2
   */
  protected WellViewer() {}
  
  public WellViewer(WellViewer thisProxy,
                    WellSearchResults wellSearchResults,
                    GenericEntityDAO dao,
                    LibrariesDAO librariesDAO,
                    EntityViewPolicy entityViewPolicy,
                    LibraryViewer libraryViewer,
                    StructureImageProvider structureImageProvider,
                    StudyViewer studyViewer,
                    WellsSdfDataExporter wellsSdfDataExporter,
                    LibraryContentsVersionReference libraryContentsVersionRef,
                    AnnotationSearchResults annotationSearchResults,
                    ScreenResultReporter screenResultReporter,
                    StudyImageProvider studyImageProvider,
                    AttachedFiles attachedFiles)
  {
    super(thisProxy,
          wellSearchResults,
          dao,
          librariesDAO,
          entityViewPolicy,
          libraryViewer,
          structureImageProvider,
          studyViewer,
          wellsSdfDataExporter,
          libraryContentsVersionRef,
          annotationSearchResults,
          screenResultReporter);
    _attachedFiles = attachedFiles;
    _studyImageProvider = studyImageProvider;
    getIsPanelCollapsedMap().put("annotations", Boolean.FALSE);
  }

  @Override
  protected void initializeViewer(Well well)
  {
    super.initializeViewer(well);
    initalizeAttachedFiles(well.getLatestReleasedReagent());
  }

  public AttachedFiles getAttachedFiles()
  {
    return _attachedFiles;
  }

  private void initalizeAttachedFiles(Reagent reagent)
  {
    if (reagent != null) {
      getDao().needReadOnly(reagent, Reagent.attachedFiles);
      getDao().needReadOnly(reagent, Reagent.attachedFiles.to(AttachedFile.fileType));
      SortedSet<AttachedFileType> attachedFileTypes =
        Sets.<AttachedFileType>newTreeSet(getDao().findAllEntitiesOfType(ReagentAttachedFileType.class, true));
      _attachedFiles.initialize((Reagent) reagent.restrict(), attachedFileTypes,
                                new Predicate<AttachedFile>() {
        @Override
        public boolean apply(AttachedFile input)
        {
          return !!! input.getFileType().getValue().equals(ScreensaverConstants.STUDY_FILE_TYPE);
        }
      });
    }
    else {
      _attachedFiles.initialize();
    }
  }

  /**
   * NOTE: this is a LINCS-only feature
   * 
   * @return formatted (to 3 digits) Facility Batch Identifier from the SmallMoleculeReagent
   */
  public String getSmallMoleculeFacilityBatchId()
  {
    return getRestrictedReagent() == null ? null
      : String.format("%03d", ((SmallMoleculeReagent) getRestrictedReagent()).getFacilityBatchId());
  }

  /**
   * NOTE: this is a LINCS-only feature
   * 
   * @return formatted (to 3 digits) Facility BgetRestrictedReagent() == null ? null : atch Identifier from the
   *         SmallMoleculeReagent
   */
  public String getSmallMoleculeSaltFormId()
  {

    return getRestrictedReagent() == null ? null
      : String.format("%03d", ((SmallMoleculeReagent) getRestrictedReagent()).getSaltFormId());
  }

  /**
   * NOTE: this is a LINCS-only feature
   * 
   * @return formatted (to 3 digits) Facility Batch Identifier from the SmallMoleculeReagent
   */
  public String getSmallMoleculeVendorBatchId()
  {
    return getRestrictedReagent() == null ? null : ((SmallMoleculeReagent) getRestrictedReagent()).getVendorBatchId();
  }

  public String getStudyImageUrl()
  {
    if (_studyImageProvider == null) return null;

    URL url = _studyImageProvider.getImageUrl((SmallMoleculeReagent) getRestrictedReagent());
    return url == null ? null : url.toString();
  }

}
