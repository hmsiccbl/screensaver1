// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.db.datafetcher.Tuple;
import edu.harvard.med.screensaver.io.image.ImageLocatorUtil;
import edu.harvard.med.screensaver.io.libraries.WellsSdfDataExporter;
import edu.harvard.med.screensaver.io.libraries.smallmolecule.LibraryContentsVersionReference;
import edu.harvard.med.screensaver.io.libraries.smallmolecule.StructureImageLocator;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultReporter;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultReporter.ConfirmationReport;
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
import edu.harvard.med.screensaver.model.screens.Publication;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.policy.EntityViewPolicy;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;
import edu.harvard.med.screensaver.ui.arch.util.JSFUtils;
import edu.harvard.med.screensaver.ui.arch.util.SimpleCell;
import edu.harvard.med.screensaver.ui.arch.view.SearchResultContextEntityViewerBackingBean;
import edu.harvard.med.screensaver.ui.arch.view.aspects.UICommand;
import edu.harvard.med.screensaver.ui.screens.AnnotationHeaderColumn;
import edu.harvard.med.screensaver.ui.screens.StudyViewer;
import edu.harvard.med.screensaver.util.NullSafeUtils;
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
  public static final String CHEMBL_ID_LOOKUP_URL_PREFIX =
    "http://www.ebi.ac.uk/chembldb/compound/inspect/CHEMBL";

  private LibraryViewer _libraryViewer;
  private LibrariesDAO _librariesDao;
  private EntityViewPolicy _entityViewPolicy;
  private StructureImageLocator _structureImageLocator;

  private StudyViewer _studyViewer;
  private WellsSdfDataExporter _wellsSdfDataExporter;
  private LibraryContentsVersionReference _libraryContentsVersionRef;
 
  // Note: the annotation search results is used by LINCS, it will display all annotations from all studies in one table.  TODO: rework this (by nesting two levels?) so that annotions are visually grouped by study
  private AnnotationSearchResults _annotationSearchResults;

  // Note: the annotation name value table is used to display a generic table listing annotations from multiple studies, grouped in the well viewer
  private List<SimpleCell> _annotationNameValueTable;

  private DataModel _otherWellsDataModel;
  private DataModel _duplexWellsDataModel;
  private Reagent _versionedRestrictedReagent;

  private boolean _isAnnotationSearchResultsInitialized = false;
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
                    StructureImageLocator structureImageLocator,
                    StudyViewer studyViewer,
                    WellsSdfDataExporter wellsSdfDataExporter,
                    LibraryContentsVersionReference libraryContentsVersionRef,
                    AnnotationSearchResults annotationSearchResults,
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
    _structureImageLocator = structureImageLocator;
    _studyViewer = studyViewer;
    _wellsSdfDataExporter = wellsSdfDataExporter;
    _libraryContentsVersionRef = libraryContentsVersionRef == null ? new LibraryContentsVersionReference()
      : libraryContentsVersionRef;
    _annotationSearchResults = annotationSearchResults;
    _screenResultReporter = screenResultReporter;
    getIsPanelCollapsedMap().put("otherWells", Boolean.TRUE);
    getIsPanelCollapsedMap().put("duplexWells", Boolean.TRUE);
    getIsPanelCollapsedMap().put("annotations", Boolean.TRUE);
    getIsPanelCollapsedMap().put("studyHeaders", Boolean.TRUE);
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
      if (_versionedRestrictedReagent != null) {
        Set<Reagent> reagents = _librariesDao.findReagents(_versionedRestrictedReagent.getVendorId(), true /*
                                                                                                  * Reagent.well.to(Well.
                                                                                                  * library)
                                                                                                  */);
        reagents.remove(_versionedRestrictedReagent);
        List<Reagent> list = Lists.newArrayList(reagents);
        Collections.sort(list, new Comparator<Reagent>() {

					@Override
					public int compare(Reagent o1, Reagent o2) {
						if(o1==o2) return 0;
						return o1.getWell().getWellId().compareTo(o2.getWell().getWellId());
					}} );
        _otherWellsDataModel = new ListDataModel(list);
      }
      else {
        _otherWellsDataModel = new ListDataModel(Lists.newArrayList());
      }
    }
    return _otherWellsDataModel;
  }

  // Annotation search results - used by LINCS - all annotations (for all studies) in one table
  public AnnotationSearchResults getAnnotationSearchResults()
  {
    // lazy initialization of _annotationSearchResults, for performance (avoid expense of determining columns, if not being viewed)
    if (!_isAnnotationSearchResultsInitialized && !getIsPanelCollapsedMap().get("annotations")) {
      _annotationSearchResults.searchForAnnotations(getEntity());
      _isAnnotationSearchResultsInitialized = true;
    }
    return _annotationSearchResults;
  }

  // Annotation name value table: (generic build) - annotations are grouped by study, shown as separate "name/value" tables (name/value table are simple property name/property value)
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
    if (_versionedRestrictedReagent != null) {
      Reagent reagent = (Reagent) getDao().reloadEntity(_versionedRestrictedReagent, true, Reagent.annotationValues).restrict();
      annotationValues.addAll(reagent.getAnnotationValues().values());
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
						
						// Fix for bug [#3220] Study link in Well Viewer Annotation table is broken
						// Note: cannot use an anonymous inner class with er-ri-1.0.jar 
						// FYI: http://stackoverflow.com/questions/2998745/how-to-invoke-jsf-action-on-an-anonymous-class-el-cannot-access-it
						// TODO: why is this working for TableColumn?
						//						SimpleCell cell = new SimpleCell(headerColumn.getColName(), headerValue, headerColumn.getDescription()) {
						//							@Override
						//							public boolean isCommandLink() {
						//								return true;
						//							}
						//
						//							@Override
						//							public Object cellAction() {
						//								return _studyViewer.viewEntity(study);
						//							}
						//						};
						StudyCell cell = new StudyCell(headerColumn.getColName(), headerValue, headerColumn.getDescription(), study);
						metaInformation.add(cell);
					} else {
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
  
	// Fix for bug [#3220] Study link in Well Viewer Annotation table is broken
	// Note: cannot use an anonymous inner class with er-ri-1.0.jar 
	// FYI: http://stackoverflow.com/questions/2998745/how-to-invoke-jsf-action-on-an-anonymous-class-el-cannot-access-it
  public class StudyCell extends SimpleCell
  {
  	Study study;
		public StudyCell(String title, Object value, String description, Study study) {
			super(title, value, description);
			this.study = study;
		}
  	@Override
  	public Object cellAction() {
			return _studyViewer.viewEntity(study);
  	}
  	@Override
  	public boolean isCommandLink() {
  		return true;
  	}
  }

  @Override
  protected void initializeEntity(Well well)
  {
    getDao().needReadOnly(well, Well.library);
    getDao().needReadOnly(well, Well.deprecationActivity);
    Reagent versionedReagent = _libraryContentsVersionRef.value() == null ? well.getLatestReleasedReagent()
      : well.getReagents().get(_libraryContentsVersionRef.value());
    if (versionedReagent != null) {
      if (well.getLibrary().getReagentType().equals(SilencingReagent.class)) {
        getDao().needReadOnly((SilencingReagent) versionedReagent, SilencingReagent.vendorGene.to(Gene.genbankAccessionNumbers));
        getDao().needReadOnly((SilencingReagent) versionedReagent, SilencingReagent.vendorGene.to(Gene.entrezgeneSymbols));
        getDao().needReadOnly((SilencingReagent) versionedReagent, SilencingReagent.facilityGene.to(Gene.genbankAccessionNumbers));
        getDao().needReadOnly((SilencingReagent) versionedReagent, SilencingReagent.facilityGene.to(Gene.entrezgeneSymbols));
        getDao().needReadOnly((SilencingReagent) versionedReagent, SilencingReagent.duplexWells.to(Well.library));
      }
      if (well.getLibrary().getReagentType().equals(SmallMoleculeReagent.class)) {
        getDao().needReadOnly((SmallMoleculeReagent) versionedReagent, SmallMoleculeReagent.compoundNames);
        getDao().needReadOnly((SmallMoleculeReagent) versionedReagent, SmallMoleculeReagent.pubchemCids);
        getDao().needReadOnly((SmallMoleculeReagent) versionedReagent, SmallMoleculeReagent.chembankIds);
        getDao().needReadOnly((SmallMoleculeReagent) versionedReagent, SmallMoleculeReagent.chemblIds);
        getDao().needReadOnly((SmallMoleculeReagent) versionedReagent, SmallMoleculeReagent.molfileList);
      }
      getDao().needReadOnly(versionedReagent, Reagent.libraryContentsVersion);
      getDao().needReadOnly(versionedReagent, Reagent.publications);
      _versionedRestrictedReagent = (Reagent) versionedReagent.restrict();
    }
    else {
      _versionedRestrictedReagent = null;
    }
  }

  @Override
  protected void initializeViewer(Well well)
  {
    initializeAnnotationValuesTable(well);
    _otherWellsDataModel = null;
    _duplexWellsDataModel = null;
    _isAnnotationSearchResultsInitialized = false;
    _confirmationReportTableModel = null;
  }

  public Reagent getRestrictedReagent()
  {
    return _versionedRestrictedReagent;
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

  public String getChemblIdUrlPrefix()
  {
    return CHEMBL_ID_LOOKUP_URL_PREFIX;
  }

  public String getCompoundImageUrl()
  {
    URL url = _structureImageLocator.getImageUrl((SmallMoleculeReagent) getRestrictedReagent());
    // TODO: consider using: return NullSafeUtils.toString(ImageLocatorUtil.toExtantContentUrl(url), "");
    return url == null ? null : url.toString();
  }

  public String getCompoundMolecularFormula()
  {
    return ((SmallMoleculeReagent) getRestrictedReagent()).getMolecularFormula() == null ?
      "" : ((SmallMoleculeReagent) getRestrictedReagent()).getMolecularFormula().toHtml();
  }

  public DataModel getPublicationsDataModel()
  {
    if (_versionedRestrictedReagent == null) return null;

    ArrayList<Publication> publications = new ArrayList<Publication>(getRestrictedReagent().getPublications());
    Collections.sort(publications,
                     new Comparator<Publication>() {
                       public int compare(Publication p1, Publication p2)
      {
        return p1.getAuthors().compareTo(p2.getAuthors());
      }
                     });
    return new ListDataModel(publications);
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
        duplexReagents.add((SilencingReagent) getDao().reloadEntity(dr, true, SilencingReagent.well.castToSubtype(SilencingReagent.class)).restrict());
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
      String style = "confirmationReportInconclusiveOrNoData";
      if (value != null) {
        switch (value) {
          case CONFIRMED_POSITIVE:
            style = "confirmationReportConfirmedPositive";
            break;
          case FALSE_POSITIVE:
            style = "confirmationReportFalsePositive";
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
    Reagent reagent = getRestrictedReagent();
    if (_confirmationReportTableModel == null) {
      if (reagent == null || !(reagent instanceof SilencingReagent)
        || !reagent.getWell().getLibrary().isPool()) {
        _confirmationReportTableModel = new ConfirmationReportTableModel();
      }
      if (_confirmationReportTableModel == null) { //TODO: will this report become too stale if a user session lasts too long?
        ConfirmationReport report = _screenResultReporter.getDuplexReconfirmationReport((SilencingReagent) reagent);
        if (report.getDuplexReagents().isEmpty()) { // this will occur if there are no confirmation results.  the UI should still show the duplex wells
          report.setDuplexReagents(Lists.newArrayList(Iterables.transform(((SilencingReagent) reagent).getDuplexSilencingReagents(),
                                                                          SilencingReagent.<SilencingReagent>ToRestricted())));
        }

        _confirmationReportTableModel = new ConfirmationReportTableModel(report);
      }
    }
    return _confirmationReportTableModel;
  }
}
