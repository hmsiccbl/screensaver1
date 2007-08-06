// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.control;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.springframework.dao.DataAccessException;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.io.DataExporter;
import edu.harvard.med.screensaver.io.libraries.PlateWellListParser;
import edu.harvard.med.screensaver.io.libraries.PlateWellListParserResult;
import edu.harvard.med.screensaver.io.libraries.WellsDataExporter;
import edu.harvard.med.screensaver.io.libraries.WellsDataExporterFormat;
import edu.harvard.med.screensaver.io.libraries.compound.NaturalProductsLibraryContentsParser;
import edu.harvard.med.screensaver.io.libraries.compound.SDFileCompoundLibraryContentsParser;
import edu.harvard.med.screensaver.io.libraries.rnai.RNAiLibraryContentsParser;
import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.ui.libraries.CompoundLibraryContentsImporter;
import edu.harvard.med.screensaver.ui.libraries.CompoundViewer;
import edu.harvard.med.screensaver.ui.libraries.GeneViewer;
import edu.harvard.med.screensaver.ui.libraries.LibrariesBrowser;
import edu.harvard.med.screensaver.ui.libraries.LibraryViewer;
import edu.harvard.med.screensaver.ui.libraries.NaturalProductsLibraryContentsImporter;
import edu.harvard.med.screensaver.ui.libraries.RNAiLibraryContentsImporter;
import edu.harvard.med.screensaver.ui.libraries.WellCopyVolume;
import edu.harvard.med.screensaver.ui.libraries.WellCopyVolumeSearchResults;
import edu.harvard.med.screensaver.ui.libraries.WellCopyVolumeSearchResultsViewer;
import edu.harvard.med.screensaver.ui.libraries.WellFinder;
import edu.harvard.med.screensaver.ui.libraries.WellSearchResultsViewer;
import edu.harvard.med.screensaver.ui.libraries.WellViewer;
import edu.harvard.med.screensaver.ui.namevaluetable.CompoundNameValueTable;
import edu.harvard.med.screensaver.ui.namevaluetable.GeneNameValueTable;
import edu.harvard.med.screensaver.ui.namevaluetable.LibraryNameValueTable;
import edu.harvard.med.screensaver.ui.namevaluetable.WellNameValueTable;
import edu.harvard.med.screensaver.ui.searchresults.LibrarySearchResults;
import edu.harvard.med.screensaver.ui.searchresults.SearchResults;
import edu.harvard.med.screensaver.ui.searchresults.WellSearchResults;
import edu.harvard.med.screensaver.ui.util.JSFUtils;
import edu.harvard.med.screensaver.util.Pair;

/**
 * 
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class LibrariesControllerImpl extends AbstractUIController implements LibrariesController
{

  // private static final fields
  
  private static final Logger log = Logger.getLogger(LibrariesController.class);

  
  // instance variables
  
  private GenericEntityDAO _dao;
  private LibrariesDAO _librariesDao;
  private ScreensController _screensController;
  private WellFinder _wellFinder;
  private LibrariesBrowser _librariesBrowser;
  private LibraryViewer _libraryViewer;
  private WellSearchResultsViewer _wellSearchResultsViewer;
  private WellCopyVolumeSearchResultsViewer _wellCopyVolumeSearchResultsViewer;
  private WellViewer _wellViewer;
  private GeneViewer _geneViewer;
  private CompoundViewer _compoundViewer;
  private CompoundLibraryContentsImporter _compoundLibraryContentsImporter;
  private SDFileCompoundLibraryContentsParser _compoundLibraryContentsParser;
  private NaturalProductsLibraryContentsImporter _naturalProductsLibraryContentsImporter;
  private NaturalProductsLibraryContentsParser _naturalProductsLibraryContentsParser;
  private RNAiLibraryContentsImporter _rnaiLibraryContentsImporter;
  private RNAiLibraryContentsParser _rnaiLibraryContentsParser;
  private PlateWellListParser _plateWellListParser;
  private List<DataExporter<Well>> _wellDataExporters;

  
  // public getters and setters
  
  public void setGenericEntityDao(GenericEntityDAO dao)
  {
    _dao = dao;
  }
  
  public void setLibrariesDao(LibrariesDAO librariesDao)
  {
    _librariesDao = librariesDao;
  }
  
  public void setScreensController(ScreensController screensController)
  {
    _screensController = screensController;
  }

  public void setWellFinder(WellFinder wellFinder)
  {
    _wellFinder = wellFinder;
  }
  
  public void setLibrariesBrowser(LibrariesBrowser librariesBrowser)
  {
    _librariesBrowser = librariesBrowser;
  }
  
  public void setLibraryViewer(LibraryViewer libraryViewer)
  {
    _libraryViewer = libraryViewer;
  }

  public void setWellSearchResultsViewer(WellSearchResultsViewer wellSearchResultsViewer)
  {
    _wellSearchResultsViewer = wellSearchResultsViewer;
  }
  
  public void setWellCopyVolumeSearchResultsViewer(WellCopyVolumeSearchResultsViewer wellCopyVolumeSearchResultsViewer)
  {
    _wellCopyVolumeSearchResultsViewer = wellCopyVolumeSearchResultsViewer;
  }
  
  public void setWellViewer(WellViewer wellViewer)
  {
    _wellViewer = wellViewer;
  }
  
  public void setGeneViewer(GeneViewer geneViewer)
  {
    _geneViewer = geneViewer;
  }
  
  public void setCompoundViewer(CompoundViewer compoundViewer)
  {
    _compoundViewer = compoundViewer;
  }
  
  public CompoundLibraryContentsImporter getCompoundLibraryContentsImporter()
  {
    return _compoundLibraryContentsImporter;
  }
  
  public void setCompoundLibraryContentsImporter(
    CompoundLibraryContentsImporter compoundLibraryContentsImporter)
  {
    _compoundLibraryContentsImporter = compoundLibraryContentsImporter;
  }

  public SDFileCompoundLibraryContentsParser getCompoundLibraryContentsParser()
  {
    return _compoundLibraryContentsParser;
  }

  public void setCompoundLibraryContentsParser(
    SDFileCompoundLibraryContentsParser compoundLibraryContentsParser)
  {
    _compoundLibraryContentsParser = compoundLibraryContentsParser;
  }
  
  public NaturalProductsLibraryContentsImporter getNaturalProductsLibraryContentsImporter()
  {
    return _naturalProductsLibraryContentsImporter;
  }

  public void setNaturalProductsLibraryContentsImporter(
    NaturalProductsLibraryContentsImporter naturalProductsLibraryContentsImporter)
  {
    _naturalProductsLibraryContentsImporter = naturalProductsLibraryContentsImporter;
  }

  public NaturalProductsLibraryContentsParser getNaturalProductsLibraryContentsParser()
  {
    return _naturalProductsLibraryContentsParser;
  }

  public void setNaturalProductsLibraryContentsParser(
    NaturalProductsLibraryContentsParser naturalProductsLibraryContentsParser)
  {
    _naturalProductsLibraryContentsParser = naturalProductsLibraryContentsParser;
  }

  public RNAiLibraryContentsImporter getRnaiLibraryContentsImporter()
  {
    return _rnaiLibraryContentsImporter;
  }
  
  public void setRnaiLibraryContentsImporter(
    RNAiLibraryContentsImporter rnaiLibraryContentsImporter)
  {
    _rnaiLibraryContentsImporter = rnaiLibraryContentsImporter;
  }
  
  public RNAiLibraryContentsParser getRnaiLibraryContentsParser()
  {
    return _rnaiLibraryContentsParser;
  }
  
  public void setRnaiLibraryContentsParser(RNAiLibraryContentsParser rnaiLibraryContentsParser)
  {
    _rnaiLibraryContentsParser = rnaiLibraryContentsParser;
  }
  

  // controller methods
  
  public PlateWellListParser getPlateWellListParser()
  {
    return _plateWellListParser;
  }

  public void setPlateWellListParser(PlateWellListParser plateWellLristParser)
  {
    _plateWellListParser = plateWellLristParser;
  }
  
  public void setWellDataExporters(List<DataExporter<Well>> wellDataExporters)
  {
    _wellDataExporters = wellDataExporters;
  }

  @UIControllerMethod
  public String findWells()
  {
    logUserActivity("open findWells");
    return FIND_WELLS;
  }

  @UIControllerMethod
  public String findWell(final String plateNumber, final String wellName)
  {
    logUserActivity("findWell " + plateNumber + ":" + wellName);
    return viewWell(_plateWellListParser.lookupWell(plateNumber, wellName), null);
  }
  
  @UIControllerMethod
  public String findWells(final String plateWellList)
  {
    logUserActivity(FIND_WELLS);
    final String[] result = new String[1];
    _dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        PlateWellListParserResult parseResult = _plateWellListParser.parseWellsFromPlateWellList(plateWellList);
        if (parseResult.getParsedWellKeys().size() == 1) {
          result[0] = viewWell(parseResult.getParsedWellKeys().first(), null);
          return;
        }

        // display parse errors before proceeding with successfully parsed wells
        for (Pair<Integer,String> error : parseResult.getErrors()) {
          showMessage("libraries.plateWellListParseError", error.getSecond());
        }

        List<Well> foundWells = new ArrayList<Well>();
        for (WellKey wellKey : parseResult.getParsedWellKeys()) {
          Well well = _dao.findEntityById(Well.class,
                                          wellKey.toString(),
                                          true,
                                          "hbnLibrary",
                                          "hbnSilencingReagents.gene",
                                          "hbnCompounds");
          if (well == null) {
            showMessage("libraries.noSuchWell", wellKey.getPlateNumber(), wellKey.getWellName());
          }
          else {
            foundWells.add(well);
          }
        }
        WellSearchResults searchResults =
          new WellSearchResults(foundWells,
                                LibrariesControllerImpl.this,
                                _wellDataExporters);
        result[0] = viewWellSearchResults(searchResults);
      }
    });
    return result[0];
  }

  @UIControllerMethod
  public String downloadWellSDFile(final Well well)
  {
    WellsDataExporter dataExporter = new WellsDataExporter(_dao, WellsDataExporterFormat.SDF);
    Set<Well> wells = new HashSet<Well>(1, 2.0f);
    wells.add(well);
    InputStream inputStream = dataExporter.export(wells);
    try {
      JSFUtils.handleUserDownloadRequest(getFacesContext(),
                                         inputStream,
                                         dataExporter.getFileName(),
                                         dataExporter.getMimeType());
    }
    catch (IOException e) {
      reportApplicationError(e.toString());
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  @UIControllerMethod
  public String findWellVolumes(final String plateWellList)
  {
    logUserActivity(FIND_WELL_VOLUMES);
    final String[] result = new String[1];
    _dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        PlateWellListParserResult parseResult = _plateWellListParser.parseWellsFromPlateWellList(plateWellList);

        // display parse errors before proceeding with successfully parsed wells
        for (Pair<Integer,String> error : parseResult.getErrors()) {
          showMessage("libraries.plateWellListParseError", error.getSecond());
        }

        List<WellCopyVolume> foundWellCopyVolumes = new ArrayList<WellCopyVolume>();
        for (WellKey wellKey : parseResult.getParsedWellKeys()) {
          Collection<WellCopyVolume> wellCopyVolumes = _librariesDao.findWellCopyVolumes(wellKey);
          if (wellCopyVolumes.size() == 0) {
            showMessage("libraries.noSuchWell", wellKey.getPlateNumber(), wellKey.getWellName());
          }
          else {
            foundWellCopyVolumes.addAll(wellCopyVolumes);
          }
        }
        WellCopyVolumeSearchResults searchResults =
          new WellCopyVolumeSearchResults(foundWellCopyVolumes,
                                          LibrariesControllerImpl.this,
                                          _screensController,
                                          _dao,
                                          getMessages());
        result[0] = viewWellCopyVolumeSearchResults(searchResults);
      }
    });
    return result[0];
  }
  
  @UIControllerMethod
  public String browseLibraries()
  {
    logUserActivity(BROWSE_LIBRARIES);
    if (_librariesBrowser.getSearchResults() == null) {
      List<Library> libraries = _librariesDao.findLibrariesDisplayedInLibrariesBrowser();
      _librariesBrowser.setSearchResults(new LibrarySearchResults(libraries, this));
    }
    return BROWSE_LIBRARIES;
  }
  
  @UIControllerMethod
  public String viewLibrary()
  {
    String libraryIdAsString = (String) getRequestParameter("libraryId");
    Integer libraryId = Integer.parseInt(libraryIdAsString);
    Library library = _dao.findEntityById(Library.class, libraryId);
    return viewLibrary(library, null);
  }
  
  @UIControllerMethod
  public String viewLibrary(final Library libraryIn, SearchResults<Library> librarySearchResults)
  {
    logUserActivity(VIEW_LIBRARY + " " + libraryIn);
    _libraryViewer.setLibrarySearchResults(librarySearchResults);

    _dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Library library = _dao.reloadEntity(libraryIn, true);
        _libraryViewer.setLibrary(library);
        _libraryViewer.setLibrarySize(
          _dao.relationshipSize(library, "hbnWells", "wellType", "experimental"));
        _libraryViewer.setLibraryNameValueTable(
          new LibraryNameValueTable(library, _libraryViewer.getLibrarySize()));
      }
    });

    return VIEW_LIBRARY;
  }
  
  @UIControllerMethod
  public String viewLibraryContents(final Library libraryIn)
  {
    logUserActivity(VIEW_WELL_SEARCH_RESULTS + " for libraryContents " + libraryIn);
    _dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Library library = _dao.reloadEntity(libraryIn, true);
        _dao.needReadOnly(library,
                          "hbnWells.hbnSilencingReagents.gene.genbankAccessionNumbers",
                          "hbnWells.hbnCompounds");
        WellSearchResults wellSearchResults = 
          new WellSearchResults(new ArrayList<Well>(library.getWells()),
                                LibrariesControllerImpl.this,
                                _wellDataExporters);
        _wellSearchResultsViewer.setSearchResults(wellSearchResults);
      }
    });

    return VIEW_WELL_SEARCH_RESULTS;
  }
  
  @UIControllerMethod
  public String viewLibraryWellCopyVolumes(final Library libraryIn)
  {
    logUserActivity(VIEW_WELL_VOLUME_SEARCH_RESULTS + " for library " + libraryIn);
    final String[] result = new String[1];
    _dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Collection<WellCopyVolume> wellCopyVolumes = _librariesDao.findWellCopyVolumes(libraryIn);
        WellCopyVolumeSearchResults wellCopyVolumeSearchResults = 
          new WellCopyVolumeSearchResults(wellCopyVolumes,
                                          LibrariesControllerImpl.this,
                                          _screensController,
                                          _dao,
                                          getMessages());
        result[0] = viewWellCopyVolumeSearchResults(wellCopyVolumeSearchResults);
      }
    });

    return result[0];
  }

  @UIControllerMethod
  public String viewWellSearchResults(WellSearchResults wellSearchResults)
  {
    logUserActivity(VIEW_WELL_SEARCH_RESULTS);
    _wellSearchResultsViewer.setSearchResults(wellSearchResults);
    return VIEW_WELL_SEARCH_RESULTS;
  }
  
  @UIControllerMethod
  public String viewWell()
  {
    WellKey wellKey = new WellKey((String) getRequestParameter("wellId"));
    return viewWell(wellKey, null);
  }
  
  public String viewWell(Well well, WellSearchResults wellSearchResults)
  {
    if (well == null) {
      reportApplicationError("attempted to view an unknown well (not in database)");
      return REDISPLAY_PAGE_ACTION_RESULT;
      
    }
    return viewWell(well.getWellKey(), wellSearchResults);
  }

  /**
   * @param wellSearchResults <code>null</code> if well was not found within
   *          the context of a search result
   */
  @UIControllerMethod
  public String viewWell(final WellKey wellKey, WellSearchResults wellSearchResults)
  {
    logUserActivity(VIEW_WELL + " " + wellKey);
   
    try {
      _dao.doInTransaction(new DAOTransaction() {
        public void runTransaction()
        {
          Well well = _dao.findEntityById(Well.class,
                                          wellKey.toString(),
                                          true,
                                          "hbnLibrary",
                                          "hbnSilencingReagents.gene.genbankAccessionNumbers",
                                          "hbnCompounds.compoundNames",
                                          "hbnCompounds.pubchemCids",
                                          "hbnCompounds.nscNumbers",
                                          "hbnCompounds.casNumbers");
          if (well == null) {
            throw new IllegalArgumentException("no such well");
          }
          _wellViewer.setWell(well);
          _wellViewer.setWellNameValueTable(new WellNameValueTable(LibrariesControllerImpl.this, well));
        }
      });
      _wellViewer.setWellSearchResults(wellSearchResults);
    }
    catch (IllegalArgumentException e) {
      showMessage("libraries.noSuchWell", wellKey.getPlateNumber(), wellKey.getWellName());
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    return VIEW_WELL;
  }

  @UIControllerMethod
  public String viewGene(final Gene geneIn, WellSearchResults wellSearchResults)
  {
    logUserActivity(VIEW_GENE + " " + geneIn);
    _dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        if (geneIn != null) {
          Gene gene = _dao.reloadEntity(geneIn, false);
          _dao.needReadOnly(gene,
                            "genbankAccessionNumbers",
                            "hbnSilencingReagents.hbnWells.hbnLibrary");
          _geneViewer.setGene(gene);
          _geneViewer.setGeneNameValueTable(new GeneNameValueTable(LibrariesControllerImpl.this, gene));
        }
        else {
          _geneViewer.setGene(null);
          _geneViewer.setGeneNameValueTable(null);
        }
      }
    });
      
    _geneViewer.setWellSearchResults(wellSearchResults);
    Well parentWellOfInterest = wellSearchResults == null ? null : wellSearchResults.getCurrentRowDataObject();
    _geneViewer.setParentWellOfInterest(parentWellOfInterest);

    return VIEW_GENE;
  }

  @UIControllerMethod
  public String viewCompound(final Compound compoundIn,
                             final WellSearchResults wellSearchResults)
  {
    logUserActivity(VIEW_COMPOUND + " " + compoundIn);
    _dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        if (compoundIn != null) {
          Compound compound = _dao.reloadEntity(compoundIn, true);
          _dao.needReadOnly(compound,
                            "compoundNames",
                            "pubchemCids",
                            "casNumbers",
                            "nscNumbers",
                            "hbnWells.hbnLibrary");
          _compoundViewer.setCompound(compound);
          _compoundViewer.setCompoundNameValueTable(
            new CompoundNameValueTable(LibrariesControllerImpl.this, compound));
        }
        else {
          _compoundViewer.setCompound(null);
          _compoundViewer.setCompoundNameValueTable(null);
        }
      }
    });
    
    _compoundViewer.setWellSearchResults(wellSearchResults);
    Well parentWellOfInterest = wellSearchResults == null ? null : wellSearchResults.getCurrentRowDataObject();
    _compoundViewer.setParentWellOfInterest(parentWellOfInterest);
    
    return VIEW_COMPOUND;
  }

  public String viewWellCopyVolumeSearchResults(WellCopyVolumeSearchResults wellCopyVolumeSearchResults)
  {
    logUserActivity(VIEW_WELL_VOLUME_SEARCH_RESULTS);
    _wellCopyVolumeSearchResultsViewer.setSearchResults(wellCopyVolumeSearchResults);
    return VIEW_WELL_VOLUME_SEARCH_RESULTS;
  }

  @UIControllerMethod
  public String importCompoundLibraryContents(Library library)
  {
    logUserActivity(IMPORT_COMPOUND_LIBRARY_CONTENTS + " " + library);
    _compoundLibraryContentsImporter.setLibrary(library);
    _compoundLibraryContentsImporter.setUploadedFile(null);
    _compoundLibraryContentsImporter.setCompoundLibraryContentsParser(_compoundLibraryContentsParser);
    _compoundLibraryContentsParser.clearErrors();
    return IMPORT_COMPOUND_LIBRARY_CONTENTS;
  }
  
  @UIControllerMethod
  public String importCompoundLibraryContents(final Library libraryIn, final UploadedFile uploadedFile)
  {
    logUserActivity(IMPORT_COMPOUND_LIBRARY_CONTENTS + " " + libraryIn + " " + uploadedFile.getName() + " " + uploadedFile.getSize());
    try {
      if (uploadedFile == null || uploadedFile.getInputStream().available() == 0) {
        showMessage("badUploadedFile", uploadedFile.getName());
        return IMPORT_COMPOUND_LIBRARY_CONTENTS;
      }
      _dao.doInTransaction(new DAOTransaction() 
      {
        public void runTransaction()
        {
          try {
            Library library = _dao.reloadEntity(libraryIn);
            _compoundLibraryContentsParser.parseLibraryContents(library,
                                                                new File(uploadedFile.getName()),
                                                                uploadedFile.getInputStream());
            _dao.persistEntity(library);
          }
          catch (IOException e) {
            throw new DAOTransactionRollbackException("could not access uploaded file", e);
          }
        }
      });
      if (_compoundLibraryContentsParser.getHasErrors()) {
        return IMPORT_COMPOUND_LIBRARY_CONTENTS;
      }
      else {
        showMessage("libraries.importedLibraryContents", "libraryViewer");
        // TODO: to be correct, we should regen the search results, though I don't think anything in the results would actually be different after this import
        return viewLibrary(libraryIn, _libraryViewer.getLibrarySearchResults());
      }
    }
    catch (DataAccessException e) {
      // TODO: should reload library and goto library viewer
      reportSystemError(e);
      return IMPORT_COMPOUND_LIBRARY_CONTENTS;
    }
    catch (Exception e) {
      reportSystemError(e);
      return IMPORT_COMPOUND_LIBRARY_CONTENTS;
    }
  }

  @UIControllerMethod
  public String importNaturalProductsLibraryContents(Library library)
  {
    logUserActivity(IMPORT_NATURAL_PRODUCTS_LIBRARY_CONTENTS+ " " + library);
    _naturalProductsLibraryContentsImporter.setLibrary(library);
    _naturalProductsLibraryContentsImporter.setUploadedFile(null);
    _naturalProductsLibraryContentsImporter.setNaturalProductsLibraryContentsParser(_naturalProductsLibraryContentsParser);
    _naturalProductsLibraryContentsParser.clearErrors();
    return IMPORT_NATURAL_PRODUCTS_LIBRARY_CONTENTS;
  }
  
  @UIControllerMethod
  public String importNaturalProductsLibraryContents(final Library libraryIn, final UploadedFile uploadedFile)
  {
    logUserActivity(IMPORT_NATURAL_PRODUCTS_LIBRARY_CONTENTS + " " + libraryIn + " " + uploadedFile.getName() + " " + uploadedFile.getSize());
    try {
      if (uploadedFile == null || uploadedFile.getInputStream().available() == 0) {
        showMessage("badUploadedFile", uploadedFile.getName());
        return IMPORT_NATURAL_PRODUCTS_LIBRARY_CONTENTS;
      }
      _dao.doInTransaction(new DAOTransaction() 
      {
        public void runTransaction()
        {
          try {
            Library library = _dao.reloadEntity(libraryIn);
            _naturalProductsLibraryContentsParser.parseLibraryContents(
              library,
              new File(uploadedFile.getName()),
              uploadedFile.getInputStream());
            _dao.persistEntity(library);
          }
          catch (IOException e) {
            throw new DAOTransactionRollbackException("could not access uploaded file", e);
          }
        }
      });
      if (_naturalProductsLibraryContentsParser.getHasErrors()) {
        return IMPORT_NATURAL_PRODUCTS_LIBRARY_CONTENTS;
      }
      else {
        showMessage("libraries.importedLibraryContents", "libraryViewer");
        // TODO: to be correct, we should regen the search results, though I don't think anything in the results would actually be different after this import
        return viewLibrary(libraryIn, _libraryViewer.getLibrarySearchResults());
      }
    }
    catch (DataAccessException e) {
      // TODO: should reload library and goto library viewer
      reportSystemError(e);
      return IMPORT_NATURAL_PRODUCTS_LIBRARY_CONTENTS;
    }
    catch (IOException e) {
      reportSystemError(e);
      return IMPORT_NATURAL_PRODUCTS_LIBRARY_CONTENTS;
    }
  }
  
  @UIControllerMethod
  public String importRNAiLibraryContents(Library library)
  {
    logUserActivity(IMPORT_RNAI_LIBRARY_CONTENTS + " " + library);
    _rnaiLibraryContentsImporter.setLibrary(library);
    _rnaiLibraryContentsImporter.setUploadedFile(null);
    _rnaiLibraryContentsParser.clearErrors();
    return IMPORT_RNAI_LIBRARY_CONTENTS;
  }
  
  @UIControllerMethod
  public String importRNAiLibraryContents(
    final Library libraryIn,
    final UploadedFile uploadedFile,
    final SilencingReagentType silencingReagentType)
  {
    logUserActivity(IMPORT_RNAI_LIBRARY_CONTENTS + " " + libraryIn + " " + uploadedFile.getName() + " " + uploadedFile.getSize());
    try {
      if (uploadedFile == null || uploadedFile.getInputStream().available() == 0) {
        showMessage("badUploadedFile", uploadedFile.getName());
        return IMPORT_RNAI_LIBRARY_CONTENTS;
      }
      _dao.doInTransaction(new DAOTransaction() 
      {
        public void runTransaction()
        {
          try {
            Library library = _dao.reloadEntity(libraryIn);
            _rnaiLibraryContentsParser.setSilencingReagentType(silencingReagentType);
            _rnaiLibraryContentsParser.parseLibraryContents(library,
                                                            new File(uploadedFile.getName()), 
                                                            uploadedFile.getInputStream());
            _dao.persistEntity(library);
          }
          catch (IOException e) {
            throw new DAOTransactionRollbackException("could not access uploaded file", e);
          }
        }
      });
      if (_rnaiLibraryContentsParser.getHasErrors()) {
        return IMPORT_RNAI_LIBRARY_CONTENTS;
      }
      showMessage("libraries.importedLibraryContents", "libraryViewer");
      // TODO: to be correct, we should regen the search results, though I don't think anything in the results would actually be different after this import
      return viewLibrary(libraryIn, _libraryViewer.getLibrarySearchResults());
    }
    catch (DataAccessException e) {
      // TODO: should reload library and goto library viewer
      reportSystemError(e);
      return IMPORT_RNAI_LIBRARY_CONTENTS;
    }
    catch (Exception e) {
      reportApplicationError(e);
      return IMPORT_RNAI_LIBRARY_CONTENTS;
    }
  }

  @UIControllerMethod
  public String unloadLibraryContents(final Library libraryIn, final SearchResults<Library> results)
  {
    logUserActivity("unloadLibraryContents " + libraryIn);
    _dao.doInTransaction(new DAOTransaction() 
    {
      public void runTransaction() 
      {
        Library library = _dao.reloadEntity(libraryIn);
        _librariesDao.deleteLibraryContents(library);
      }
    });
    showMessage("libraries.unloadedLibraryContents", "libraryViewer");
    return viewLibrary(libraryIn, results);
  }
}
