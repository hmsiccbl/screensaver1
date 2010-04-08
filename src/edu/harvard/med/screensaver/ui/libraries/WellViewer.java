// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.io.libraries.WellsSdfDataExporter;
import edu.harvard.med.screensaver.io.libraries.smallmolecule.StructureImageProvider;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryContentsVersion;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.policy.EntityViewPolicy;
import edu.harvard.med.screensaver.ui.SearchResultContextEntityViewerBackingBean;
import edu.harvard.med.screensaver.ui.UICommand;
import edu.harvard.med.screensaver.ui.namevaluetable.NameValueTable;
import edu.harvard.med.screensaver.ui.namevaluetable.WellNameValueTable;
import edu.harvard.med.screensaver.ui.screens.StudyViewer;
import edu.harvard.med.screensaver.ui.searchresults.AnnotationHeaderColumn;
import edu.harvard.med.screensaver.ui.searchresults.WellSearchResults;
import edu.harvard.med.screensaver.ui.table.SimpleCell;
import edu.harvard.med.screensaver.ui.util.JSFUtils;
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class WellViewer extends SearchResultContextEntityViewerBackingBean<Well> 
{
  
  private static final Logger log = Logger.getLogger(WellViewer.class);

  // HACK: for special case message
  private static final String SPECIAL_CHEMDIV6_LIBRARY_NAME = "ChemDiv6";

  private LibraryViewer _libraryViewer;
  private LibrariesDAO _librariesDao;
  private EntityViewPolicy _entityViewPolicy;
  private StructureImageProvider _structureImageProvider;
  private NameValueTable _nameValueTable;
  private NameValueTable _annotationNameValueTable;
  private StudyViewer _studyViewer;
  private WellsSdfDataExporter _wellsSdfDataExporter;

  private DataModel _otherWellsDataModel;
  private DataModel _duplexWellsDataModel;
  private Reagent _versionedReagent;

  
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
                    WellsSdfDataExporter wellsSdfDataExporter)
  {
    super(thisProxy,
          Well.class,
          ScreensaverConstants.BROWSE_WELLS,
          ScreensaverConstants.VIEW_WELL,
          dao,
          wellSearchResults);
    _librariesDao = librariesDAO;
    _entityViewPolicy = entityViewPolicy;
    _libraryViewer = libraryViewer;
    _structureImageProvider = structureImageProvider;
    _studyViewer = studyViewer;
    _wellsSdfDataExporter = wellsSdfDataExporter;
    getIsPanelCollapsedMap().put("otherWells", Boolean.TRUE);
    getIsPanelCollapsedMap().put("duplexWells", Boolean.TRUE);
    getIsPanelCollapsedMap().put("annotations", Boolean.TRUE);
  }

  public Reagent getVersionedReagent()
  {
    return _versionedReagent;
  }

  /**
   * Compounds in certain libraries are to be treated specially - we need to display a special message to give some
   * idea to the user why there are no structures for these compounds. Returns a non-null, non-empty message
   * explaining why there is no structure, when such a message is applicable to the library that contains this well.
   */
  public String getSpecialMessage()
  {
    if (getEntity() == null) {
      return null;
    }
    if (! getEntity().getLibraryWellType().equals(LibraryWellType.EXPERIMENTAL)) {
      return null;
    }
    Library library = getEntity().getLibrary();
    // HACK: special case messages
    if (library.getLibraryType().equals(LibraryType.NATURAL_PRODUCTS)) {
      return "Structure information is unavailable for compounds in natural products libraries.";
    }
    if (library.getLibraryName().equals(SPECIAL_CHEMDIV6_LIBRARY_NAME)) {
      return "Structure information for compounds in the " + SPECIAL_CHEMDIV6_LIBRARY_NAME +
        " library are available via ICCB-L staff.";
    }
    return null;
  }

  public String viewLibrary()
  {
    return _libraryViewer.viewEntity(getEntity().getLibrary());
  }

  @UICommand
  public String downloadSDFile()
  {
    try {
      _wellsSdfDataExporter.setLibraryContentsVersion(_versionedReagent == null ? null : _versionedReagent.getLibraryContentsVersion());
      InputStream inputStream = _wellsSdfDataExporter.export(Sets.newHashSet(getEntity().getWellKey().toString()));
      JSFUtils.handleUserDownloadRequest(getFacesContext(),
                                         inputStream,
                                         _wellsSdfDataExporter.getFileName(),
                                         _wellsSdfDataExporter.getMimeType());
    }
    catch (IOException e) {
      reportApplicationError(e.toString());
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public DataModel getOtherWellsDataModel()
  {
    if (_otherWellsDataModel == null) {
      if (_versionedReagent != null) {
        Set<Reagent> reagents = _librariesDao.findReagents(_versionedReagent.getVendorId(), true /*Reagent.well.to(Well.library).getPath()*/);
        reagents.remove(_versionedReagent);
        _otherWellsDataModel = new ListDataModel(Lists.newArrayList(reagents));
      }
      else {
        _otherWellsDataModel = new ListDataModel(Lists.newArrayList());
      }
    }
    return _otherWellsDataModel;
  }
  
//  @UICommand
//  public String browseOtherWells()
//  {
//    Iterable<ReagentVendorIdentifier> rvis = 
//      Iterables.transform((List<Reagent>) getOtherWellsDataModel().getWrappedData(),
//                          new Function<Reagent,ReagentVendorIdentifier>() { public ReagentVendorIdentifier apply(Reagent r) { return r.getVendorId(); } });
//    _wellSearchResults.searchReagents(Sets.newHashSet(rvis));
//    return BROWSE_WELLS;
//  }

  public DataModel getDuplexWellsDataModel()
  {
    if (_duplexWellsDataModel == null) {
      if (_versionedReagent != null && _versionedReagent instanceof SilencingReagent) {
        Set<Well> well = ((SilencingReagent) _versionedReagent).getDuplexWells();
        _duplexWellsDataModel = new ListDataModel(Lists.newArrayList(well));
      }
      else {
        _duplexWellsDataModel = new ListDataModel(Lists.newArrayList());
      }
    }
    return _duplexWellsDataModel;
  }
  
//  @UICommand
//  public String browseDuplexWells()
//  {
//    Iterable<WellKey> wellKeys =
//      Iterables.transform((List<Well>) getDuplexWellsDataModel().getWrappedData(),
//                          new Function<Well,WellKey>() { public WellKey apply(Well w) { return w.getWellKey(); } });
//    _wellSearchResults.searchWells(Sets.newHashSet(wellKeys));
//    return BROWSE_WELLS;
//  }

  public NameValueTable getNameValueTable()
  {
    return _nameValueTable;
  }

  public void setNameValueTable(NameValueTable nameValueTable)
  {
    _nameValueTable = nameValueTable;
  }

  public NameValueTable getAnnotationNameValueTable()
  {
    return _annotationNameValueTable;
  }

  private void setAnnotationNameValueTable(NameValueTable annotationNameValueTable)
  {
    _annotationNameValueTable = annotationNameValueTable;
  }

  private void initializeAnnotationValuesTable(Well well)
  {
    List<AnnotationValue> annotationValues = new ArrayList<AnnotationValue>();
    Map<Integer,List<SimpleCell>> studyNumberToStudyInfoMap = Maps.newHashMap();
    if (_versionedReagent != null) {
      getDao().needReadOnly(_versionedReagent, Reagent.annotationValues.getPath());
      annotationValues.addAll(_versionedReagent.getAnnotationValues().values());
      for (Iterator iterator = annotationValues.iterator(); iterator.hasNext(); ) {
        AnnotationValue annotationValue = (AnnotationValue) iterator.next();
        if (annotationValue.isRestricted()) {
          iterator.remove();
        }
      }
      //TODO: remove annotations that the user has not selected to view, 
      // also use user settings to see which annotations to view

      // Optional header information
      // Note, rather than Lazy load the table, (i.e. extend DataTableModelLazyUpdateDecorator)
      // Just fill the whole table now, since if this is being created, then
      // we will need the data anyway.
      // group by study
      studyNumberToStudyInfoMap = Maps.newHashMapWithExpectedSize(annotationValues.size());

      for (AnnotationValue value: annotationValues)
      {
        final AnnotationType type = value.getAnnotationType();
        // once per study
        Integer studyNumber = value.getAnnotationType().getStudy().getStudyNumber();
        if(! studyNumberToStudyInfoMap.containsKey(studyNumber))
        {
          // create empty list either way
          List<SimpleCell> headerInfo = new ArrayList<SimpleCell>();

          // Now build a 2xN array of header values mapped to the study number
          for(AnnotationHeaderColumn headerColumn : EnumSet.allOf(AnnotationHeaderColumn.class))
          {
            String headerValue = headerColumn.getValue(_versionedReagent, type);
            if (!StringUtils.isEmpty(headerValue)) {
              final Study study = type.getStudy();
              if (headerColumn == AnnotationHeaderColumn.STUDY_NAME) {
                headerInfo.add(
                               new SimpleCell(headerColumn.getColName(), headerValue, headerColumn.getDescription()) 
                               {
                                 @Override
                                 public boolean isCommandLink() { return true; }

                                 @Override
                                 public Object cellAction() 
                                 { 
                                   return _studyViewer.viewEntity(study); 
                                 }
                               });
              } 
              else {
                headerInfo.add(new SimpleCell(headerColumn.getColName(), headerValue, headerColumn.getDescription()));
              }
            }
          }
          // add even if empty - will just show the number
          studyNumberToStudyInfoMap.put(studyNumber, headerInfo);
        }
      }
    }
    
    setAnnotationNameValueTable(new AnnotationNameValueTable(annotationValues, studyNumberToStudyInfoMap, null));
  }

  @Override
  protected void initializeEntity(Well well)
  {
    getDao().needReadOnly(well, Well.library.getPath());
    LibraryContentsVersion lcv = ((WellSearchResults) getContextualSearchResults()).getLibraryContentsVersion();
    _versionedReagent = lcv == null ? well.getLatestReleasedReagent() : well.getReagents().get(lcv);
    if (well.getLibrary().getReagentType().equals(SilencingReagent.class)) {
      getDao().needReadOnly(_versionedReagent, SilencingReagent.vendorGene.to(Gene.genbankAccessionNumbers).getPath());
      getDao().needReadOnly(_versionedReagent, SilencingReagent.vendorGene.to(Gene.entrezgeneSymbols).getPath());
      getDao().needReadOnly(_versionedReagent, SilencingReagent.facilityGene.to(Gene.genbankAccessionNumbers).getPath());
      getDao().needReadOnly(_versionedReagent, SilencingReagent.facilityGene.to(Gene.entrezgeneSymbols).getPath());
      getDao().needReadOnly(_versionedReagent, SilencingReagent.duplexWells.to(Well.library).getPath());
    }
    if (well.getLibrary().getReagentType().equals(SmallMoleculeReagent.class)) {
      getDao().needReadOnly(_versionedReagent, SmallMoleculeReagent.compoundNames.getPath());
      getDao().needReadOnly(_versionedReagent, SmallMoleculeReagent.pubchemCids.getPath());
      getDao().needReadOnly(_versionedReagent, SmallMoleculeReagent.chembankIds.getPath());
      getDao().needReadOnly(_versionedReagent, SmallMoleculeReagent.molfileList.getPath());
    }
  }

  @Override
  protected void initializeViewer(Well well)
  {
    initializeAnnotationValuesTable(well);
    setNameValueTable(new WellNameValueTable(well,
                                             _versionedReagent,
                                             this,
                                             _libraryViewer,
                                             _structureImageProvider));
    _otherWellsDataModel = null;
    _duplexWellsDataModel = null;
  }

  public boolean isAllowedAccessToSilencingReagentSequence() 
  {
    return isAllowedAccessToSilencingReagentSequence(getEntity());
  }

  public boolean isAllowedAccessToSilencingReagentSequence(Well well)
  {
    if (well.getLatestReleasedReagent() != null && well.<Reagent>getLatestReleasedReagent() instanceof SilencingReagent) {
      return _entityViewPolicy.isAllowedAccessToSilencingReagentSequence((SilencingReagent) well.getLatestReleasedReagent());
    }
    return false;
  }
  
  @Override
  protected Serializable convertEntityId(String entityIdAsString)
  {
    return entityIdAsString;
  }
}
