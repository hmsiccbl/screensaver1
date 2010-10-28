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
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import org.apache.log4j.Logger;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.db.datafetcher.Tuple;
import edu.harvard.med.screensaver.io.libraries.WellsSdfDataExporter;
import edu.harvard.med.screensaver.io.libraries.smallmolecule.LibraryContentsVersionReference;
import edu.harvard.med.screensaver.io.libraries.smallmolecule.StructureImageProvider;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;
import edu.harvard.med.screensaver.model.screenresults.ConfirmedPositiveValue;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.policy.EntityViewPolicy;
import edu.harvard.med.screensaver.service.screenresult.ScreenResultReporter;
import edu.harvard.med.screensaver.service.screenresult.ScreenResultReporter.ConfirmationReport;
import edu.harvard.med.screensaver.ui.SearchResultContextEntityViewerBackingBean;
import edu.harvard.med.screensaver.ui.UICommand;
import edu.harvard.med.screensaver.ui.screens.StudyViewer;
import edu.harvard.med.screensaver.ui.searchresults.AnnotationHeaderColumn;
import edu.harvard.med.screensaver.ui.searchresults.WellSearchResults;
import edu.harvard.med.screensaver.ui.table.SimpleCell;
import edu.harvard.med.screensaver.ui.util.JSFUtils;
import edu.harvard.med.screensaver.util.StringUtils;

public class WellViewer extends SearchResultContextEntityViewerBackingBean<Well,Tuple<String>>
{
  
  private static final Logger log = Logger.getLogger(WellViewer.class);

  public static final String GENBANK_ACCESSION_NUMBER_LOOKUP_URL_PREFIX =
    "http://www.ncbi.nlm.nih.gov/entrez/viewer.fcgi?val=";
  public static final String ENTREZGENE_ID_LOOKUP_URL_PREFIX =
    "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?db=gene&cmd=Retrieve&dopt=full_report&list_uids=";
  public static final String PUBCHEM_CID_LOOKUP_URL_PREFIX =
    "http://pubchem.ncbi.nlm.nih.gov/summary/summary.cgi?cid=";
  public static final String CHEMBANK_ID_LOOKUP_URL_PREFIX =
    "http://chembank.broad.harvard.edu/chemistry/viewMolecule.htm?cbid=";

  private LibraryViewer _libraryViewer;
  private LibrariesDAO _librariesDao;
  private EntityViewPolicy _entityViewPolicy;
  private StructureImageProvider _structureImageProvider;
  //  private NameValueTable _nameValueTable;
  private List<SimpleCell> _annotationNameValueTable;
  private StudyViewer _studyViewer;
  private WellsSdfDataExporter _wellsSdfDataExporter;
  private LibraryContentsVersionReference _libraryContentsVersionRef;

  private DataModel _otherWellsDataModel;
  private DataModel _duplexWellsDataModel;
  private Reagent _versionedReagent;

  private ConfirmationReportTableModel _confirmationReportTableModel;
  private ScreenResultReporter _screenResultReporter;
  
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
                    ScreenResultReporter screenResultReporter)
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
    _libraryContentsVersionRef = libraryContentsVersionRef == null ? new LibraryContentsVersionReference()
      : libraryContentsVersionRef;
    _screenResultReporter = screenResultReporter;
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
      InputStream inputStream = _wellsSdfDataExporter.export(ImmutableSet.of(getEntity().getWellKey().toString()).iterator());
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

  public DataModel getAnnotationNameValueTable()
  {
    return new ListDataModel(_annotationNameValueTable);
  }

  public boolean isAnnotationListAvailable()
  {
    return !_annotationNameValueTable.isEmpty();
  }

  private void initializeAnnotationValuesTable(Well well)
  {
    List<AnnotationValue> annotationValues = new ArrayList<AnnotationValue>();
    if (_versionedReagent != null) {
      getDao().needReadOnly(_versionedReagent, Reagent.annotationValues.getPath());
      annotationValues.addAll(_versionedReagent.getAnnotationValues().values());
      for (Iterator iterator = annotationValues.iterator(); iterator.hasNext();) {
        AnnotationValue annotationValue = (AnnotationValue) iterator.next();
        if (annotationValue.isRestricted()) {
          iterator.remove();
        }
      }
    }
    Collections.sort(annotationValues, new Comparator<AnnotationValue>() {
      public int compare(AnnotationValue o1, AnnotationValue o2)
      {
        return o1.getAnnotationType().getStudy().getFacilityId()
          .compareTo(o2.getAnnotationType().getStudy().getFacilityId());
      }
    });

    _annotationNameValueTable = new ArrayList<SimpleCell>(annotationValues.size());

    // Create the top level list of AV's
    for (AnnotationValue value : annotationValues) {
      // for each AV, create Meta "grouping" information 
      // - this is all of the study information
      List<SimpleCell> metaInformation = new ArrayList<SimpleCell>();
      for (AnnotationHeaderColumn headerColumn : EnumSet.allOf(AnnotationHeaderColumn.class)) {
        String headerValue = headerColumn.getValue(value.getReagent(), value.getAnnotationType());
        if (!StringUtils.isEmpty(headerValue)) {
          final Study study = value.getAnnotationType().getStudy();
          if (headerColumn == AnnotationHeaderColumn.STUDY_NAME) {
            SimpleCell cell =
              new SimpleCell(headerColumn.getColName(), headerValue, headerColumn.getDescription())
              {
              @Override
              public boolean isCommandLink()
                            {
                return true;
              }

              @Override
              public Object cellAction()
                            {
                return _studyViewer.viewEntity(study);
              }
            };
            metaInformation.add(cell);
          }
          else {
            metaInformation.add(new SimpleCell(headerColumn.getColName(), headerValue, headerColumn.getDescription()));
          }
        }
      }
      String textValue = value.getAnnotationType().isNumeric() ? "" + value.getNumericValue() : value.getValue();
      _annotationNameValueTable.add(
        new SimpleCell(
                       value.getAnnotationType().getName(),
                       textValue,
                       value.getAnnotationType().getDescription(),
                       metaInformation)
        .setGroupId("" + value.getAnnotationType().getStudy().getFacilityId()));

    }
  }

  @Override
  protected void initializeEntity(Well well)
  {
    getDao().needReadOnly(well, Well.library.getPath());
    getDao().needReadOnly(well, Well.deprecationActivity.getPath());
    _versionedReagent = _libraryContentsVersionRef.value() == null ? well.getLatestReleasedReagent()
      : well.getReagents().get(_libraryContentsVersionRef.value());
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
    getDao().needReadOnly(_versionedReagent, Reagent.libraryContentsVersion.getPath());
  }

  @Override
  protected void initializeViewer(Well well)
  {
    initializeAnnotationValuesTable(well);
    _otherWellsDataModel = null;
    _duplexWellsDataModel = null;
    _confirmationReportTableModel = null;
  }

  public boolean isAllowedAccessToSilencingReagentSequence() 
  {
    if (isAllowedAccessToSilencingReagent()) {
      return _entityViewPolicy.
        isAllowedAccessToSilencingReagentSequence((SilencingReagent) getEntity().getLatestReleasedReagent());
    }
    return false;
  }

  public boolean isAllowedAccessToSilencingReagent()
  {
    if (isUnrestrictedReagent()
      && getEntity().<Reagent>getLatestReleasedReagent() instanceof SilencingReagent) {
      return true;
    }
    return false;
  }

  public boolean isAllowedAccessToSmallMoleculeReagent()
  {
    if (isUnrestrictedReagent()
      && getEntity().<Reagent>getLatestReleasedReagent() instanceof SmallMoleculeReagent) {
      return true;
    }
    return false;
  }
  
  public boolean isUnrestrictedReagent()
  {
    Well well = getEntity();
    return well != null && well.getLatestReleasedReagent() != null
      && !well.getLatestReleasedReagent().isRestricted();
  }

  @Override
  protected Serializable convertEntityId(String entityIdAsString)
  {
    return entityIdAsString;
  }

  public String getGenbankAccessionNumberUrlPrefix()
  {
    return GENBANK_ACCESSION_NUMBER_LOOKUP_URL_PREFIX;
  }

  public String getEntrezgeneIdUrlPrefix()
  {
    return ENTREZGENE_ID_LOOKUP_URL_PREFIX;
  }

  public String getPubchemCidUrlPrefix()
  {
    return PUBCHEM_CID_LOOKUP_URL_PREFIX;
  }

  public String getChembankIdUrlPrefix()
  {
    return CHEMBANK_ID_LOOKUP_URL_PREFIX;
  }

  public String getCompoundImageUrl()
  {
    if (!isAllowedAccessToSmallMoleculeReagent()) return null; //TODO: is there a RTE to throw? - sde4
    // TODO: should we be using well.getLatestReleasedReagent()? - sde4
    return "" + _structureImageProvider.getImageUrl((SmallMoleculeReagent) getVersionedReagent());
  }

  public String getCompoundMolecularFormula()
  {
    if (!isAllowedAccessToSmallMoleculeReagent()) return null; //TODO: is there a RTE to throw? - sde4
    // TODO: should we be using well.getLatestReleasedReagent()? - sde4
    return ((SmallMoleculeReagent) getVersionedReagent()).getMolecularFormula() == null ?
      "" : ((SmallMoleculeReagent) getVersionedReagent()).getMolecularFormula().toHtml();
  }

  //// confirmation report table model
  public class ConfirmationReportTableModel
  {
    public DataModel _columnDataModel = new ListDataModel(); //List<SilencingReagent>
    public DataModel _dataModel = new ListDataModel(); // List<Map<SilencingReagent,SimpleCell>>
    private ConfirmationReport _report;

    public ConfirmationReportTableModel()
    {};

    public ConfirmationReportTableModel(ConfirmationReport report)
    {
      _report = report;
      _dataModel = new ListDataModel(_report.getScreens());

      List<SilencingReagent> duplexReagents = Lists.newArrayList();
      for (SilencingReagent dr : _report.getDuplexReagents()) {
        duplexReagents.add(getDao().reloadEntity(dr, true, SilencingReagent.well.getPath()));
      }
      _columnDataModel = new ListDataModel(duplexReagents);
    }

    public DataModel getDataModel()
    {
      return _dataModel;
    }

    public DataModel getColumnDataModel()
    {
      return _columnDataModel;
    }

    public SimpleCell getCell()
    {
      Screen s = getScreen();
      SilencingReagent r = _report.getDuplexReagents().get(_columnDataModel.getRowIndex());
      ConfirmedPositiveValue value = _report.getResults().get(s).get(r);
      String style = getStyleClass(value);

      return new SimpleCell(r.getSequence(), value, "Value for " + r.getSequence())
        .withStyleClass(style)
        .withLinkValue(r.getWell());
    }

    private String getStyleClass(ConfirmedPositiveValue value)
    {
      String style = "";
      if (value != null) {
        switch (value) {
          case CONFIRMED_POSITIVE:
            style = "confirmationReportConfirmedPositive";
            break;
          case FALSE_POSITIVE:
            style = "confirmationReportFalsePositive";
            break;
          case INCONCLUSIVE:
            style = "confirmationReportInconclusive";
            break;
          default:
            style = "confirmationReportNoData";
            break;
        }
      }
      return style;
    }

    public Screen getScreen()
    {
      return _report.getScreens().get(_dataModel.getRowIndex());
    }
  }

  public ConfirmationReportTableModel getConfirmationReport()
  {
    if (_versionedReagent == null || !(_versionedReagent instanceof SilencingReagent)
      || !_versionedReagent.getWell().getLibrary().isPool()) {
      return new ConfirmationReportTableModel();
    }
    if (_confirmationReportTableModel == null) { //TODO: will this report become too stale if a user session lasts too long?
      ConfirmationReport report = _screenResultReporter.getDuplexReconfirmationReport((SilencingReagent) _versionedReagent);
      if (report.getDuplexReagents().isEmpty()) { // this will occur if there are no confirmation results.  the UI should still show the duplex wells
        report.setDuplexReagents(((SilencingReagent) _versionedReagent).getDuplexSilencingReagents());
      }

      _confirmationReportTableModel = new ConfirmationReportTableModel(report);
    }
    return _confirmationReportTableModel;
  }



}
