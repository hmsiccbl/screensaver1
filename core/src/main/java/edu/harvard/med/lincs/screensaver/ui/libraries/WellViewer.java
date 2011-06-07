package edu.harvard.med.lincs.screensaver.ui.libraries;

import java.net.URL;
import java.util.SortedSet;

import com.google.common.collect.Sets;
import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.io.libraries.WellsSdfDataExporter;
import edu.harvard.med.screensaver.io.libraries.smallmolecule.LibraryContentsVersionReference;
import edu.harvard.med.screensaver.io.libraries.smallmolecule.StructureImageProvider;
import edu.harvard.med.screensaver.io.screens.StudyImageProvider;
import edu.harvard.med.screensaver.model.AttachedFile;
import edu.harvard.med.screensaver.model.AttachedFileType;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.ReagentAttachedFileType;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.policy.EntityViewPolicy;
import edu.harvard.med.screensaver.service.screenresult.ScreenResultReporter;
import edu.harvard.med.screensaver.ui.arch.util.AttachedFiles;
import edu.harvard.med.screensaver.ui.arch.util.servlet.ImageProviderServlet;
import edu.harvard.med.screensaver.ui.libraries.AnnotationSearchResults;
import edu.harvard.med.screensaver.ui.libraries.LibraryViewer;
import edu.harvard.med.screensaver.ui.libraries.WellSearchResults;
import edu.harvard.med.screensaver.ui.screens.StudyViewer;

public class WellViewer extends edu.harvard.med.screensaver.ui.libraries.WellViewer
{
  private static final Logger log = Logger.getLogger(WellViewer.class);

  private AttachedFiles _attachedFiles;
  private StudyImageProvider _ambitImageProvider; // for [#2417] NOTE: this is a LINCS-only feature
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
                    StudyImageProvider ambitImageProvider,
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
    _ambitImageProvider = ambitImageProvider;
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
    getDao().needReadOnly(reagent, Reagent.attachedFiles);
    getDao().needReadOnly(reagent, Reagent.attachedFiles.to(AttachedFile.fileType));
    SortedSet<AttachedFileType> attachedFileTypes =
      Sets.<AttachedFileType>newTreeSet(getDao().findAllEntitiesOfType(ReagentAttachedFileType.class, true));
    _attachedFiles.reset();
    _attachedFiles.setAttachedFileTypes(attachedFileTypes);
    _attachedFiles.setAttachedFilesEntity(reagent);
  }

  /**
   * NOTE: this is a LINCS-only feature
   * 
   * @return formatted (to 3 digits) Facility Batch Identifier from the SmallMoleculeReagent
   */
  public String getSmallMoleculeFacilityBatchId()
  {
    if (isAllowedAccessToSmallMoleculeReagent()) {
      return String.format("%03d", getSmallMoleculeReagent().getFacilityBatchId());
    }
    return "";
  }

  /**
   * NOTE: this is a LINCS-only feature
   * 
   * @return formatted (to 3 digits) Facility Batch Identifier from the SmallMoleculeReagent
   */
  public String getSmallMoleculeSaltFormId()
  {
    if (isAllowedAccessToSmallMoleculeReagent()) {
      return String.format("%03d", getSmallMoleculeReagent().getSaltFormId());
    }
    return "";
  }

  /**
   * NOTE: this is a LINCS-only feature
   * 
   * @return formatted (to 3 digits) Facility Batch Identifier from the SmallMoleculeReagent
   */
  public String getSmallMoleculeVendorBatchId()
  {
    if (isAllowedAccessToSmallMoleculeReagent()) {
      return getSmallMoleculeReagent().getVendorBatchId();
    }
    return "";
  }

  public String getAmbitImageImageUrl()
  {
    if (!isAllowedAccessToSmallMoleculeReagent()) return null;
    if (_ambitImageProvider == null) return null;

    URL url = _ambitImageProvider.getImageUrl((SmallMoleculeReagent) getVersionedReagent());
    return url == null ? null : url.toString();
  }

}
