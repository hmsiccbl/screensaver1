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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.io.libraries.PlateWellListParser;
import edu.harvard.med.screensaver.io.libraries.PlateWellListParserResult;
import edu.harvard.med.screensaver.io.libraries.compound.SDFileCompoundLibraryContentsParser;
import edu.harvard.med.screensaver.io.libraries.rnai.RNAiLibraryContentsParser;
import edu.harvard.med.screensaver.io.workbook.Workbook;
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
import edu.harvard.med.screensaver.ui.libraries.RNAiLibraryContentsImporter;
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

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.dao.DataAccessException;

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
  
  private DAO _dao;
  private WellFinder _wellFinder;
  private LibrariesBrowser _librariesBrowser;
  private LibraryViewer _libraryViewer;
  private WellSearchResultsViewer _wellSearchResultsViewer;
  private WellViewer _wellViewer;
  private GeneViewer _geneViewer;
  private CompoundViewer _compoundViewer;
  private CompoundLibraryContentsImporter _compoundLibraryContentsImporter;
  private SDFileCompoundLibraryContentsParser _compoundLibraryContentsParser;
  private RNAiLibraryContentsImporter _rnaiLibraryContentsImporter;
  private RNAiLibraryContentsParser _rnaiLibraryContentsParser;
  private PlateWellListParser _plateWellListParser;
  
  // public getters and setters
  
  public DAO getDao()
  {
    return _dao;
  }
  
  public void setDao(DAO dao)
  {
    _dao = dao;
  }
  
  public WellFinder getWellFinder()
  {
    return _wellFinder;
  }
  
  public void setWellFinder(WellFinder wellFinder)
  {
    _wellFinder = wellFinder;
  }
  
  public LibrariesBrowser getLibrariesBrowser()
  {
    return _librariesBrowser;
  }
  
  public void setLibrariesBrowser(LibrariesBrowser librariesBrowser)
  {
    _librariesBrowser = librariesBrowser;
  }
  
  public LibraryViewer getLibraryViewer()
  {
    return _libraryViewer;
  }
  
  public void setLibraryViewer(LibraryViewer libraryViewer)
  {
    _libraryViewer = libraryViewer;
  }

  public WellSearchResultsViewer getWellSearchResultsViewer()
  {
    return _wellSearchResultsViewer;
  }
  
  public void setWellSearchResultsViewer(WellSearchResultsViewer wellSearchResultsViewer)
  {
    _wellSearchResultsViewer = wellSearchResultsViewer;
  }
  
  public WellViewer getWellViewer()
  {
    return _wellViewer;
  }
  
  public void setWellViewer(WellViewer wellViewer)
  {
    _wellViewer = wellViewer;
  }
  
  public GeneViewer getGeneViewer()
  {
    return _geneViewer;
  }
  
  public void setGeneViewer(GeneViewer geneViewer)
  {
    _geneViewer = geneViewer;
  }
  
  public CompoundViewer getCompoundViewer()
  {
    return _compoundViewer;
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

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.ui.control.LibrariesController#findWells()
   */
  @UIControllerMethod
  public String findWells()
  {
    logUserActivity("open findWells");
    return "findWells";
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.ui.control.LibrariesController#findWell(java.lang.String, java.lang.String)
   */
  @UIControllerMethod
  public String findWell(String plateNumber, String wellName)
  {
    logUserActivity("findWell " + plateNumber + ":" + wellName);
    Well well = _plateWellListParser.lookupWell(plateNumber, wellName);
    if (well == null) {
      showMessage("libraries.noSuchWell", plateNumber, wellName);
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    return viewWell(well, null);
  }
  
  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.ui.control.LibrariesController#findWells(java.lang.String)
   */
  @UIControllerMethod
  public String findWells(String plateWellList)
  {
    logUserActivity("findWells");
    PlateWellListParserResult result = _plateWellListParser.lookupWellsFromPlateWellList(plateWellList);
    if (result.getFatalErrors().size() > 0) {
      showMessage("libraries.unexpectedErrorReadingPlateWellList", "searchResults");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    
    // display errors before proceeding with successfully parsed wells
    for (Pair<Integer,String> error : result.getSyntaxErrors()) {
      showMessage("libraries.plateWellListParseError", error.getSecond());
    }
    for (WellKey wellKey : result.getWellsNotFound()) {
      showMessage("libraries.noSuchWell", wellKey.getPlateNumber(), wellKey.getWellName());
    }
    
    if (result.getWells().size() == 1) {
      return viewWell(result.getWells().first(), null);
    }
    else {
      WellSearchResults searchResults =
        new WellSearchResults(new ArrayList<Well>(result.getWells()),
                              this);
      return viewWellSearchResults(searchResults);
    }
  }
  
  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.ui.control.LibrariesController#browseLibraries()
   */
  @UIControllerMethod
  public String browseLibraries()
  {
    logUserActivity("browseLibraries");
    if (getLibrariesBrowser().getLibrarySearchResults() == null) {
      //List<Library> libraries = _dao.findAllEntitiesWithType(Library.class);
      List<Library> libraries = _dao.findLibrariesDisplayedInLibrariesBrowser();
      _librariesBrowser.setLibrarySearchResults(new LibrarySearchResults(libraries, this));
    }
    return "browseLibraries";
  }
  
  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.ui.control.LibrariesController#viewLibrary()
   */
  @UIControllerMethod
  public String viewLibrary()
  {
    String libraryIdAsString = (String) getRequestParameter("libraryId");
    Integer libraryId = Integer.parseInt(libraryIdAsString);
    Library library = _dao.findEntityById(Library.class, libraryId);
    return viewLibrary(library, null);
  }
  
  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.ui.control.LibrariesController#viewLibrary(edu.harvard.med.screensaver.model.libraries.Library, edu.harvard.med.screensaver.ui.searchresults.LibrarySearchResults)
   */
  @UIControllerMethod
  public String viewLibrary(final Library libraryIn, LibrarySearchResults librarySearchResults)
  {
    logUserActivity("viewLibrary " + libraryIn);
    _libraryViewer.setLibrarySearchResults(librarySearchResults);

    _dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Library library = (Library) _dao.reloadEntity(libraryIn);
        _libraryViewer.setLibrary(library);
        _libraryViewer.setLibrarySize(
          _dao.relationshipSize(library, "hbnWells", "wellType", "experimental"));
        _libraryViewer.setLibraryNameValueTable(
          new LibraryNameValueTable(library, _libraryViewer.getLibrarySize()));
      }
    });

    return "viewLibrary";
  }
  
  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.ui.control.LibrariesController#viewLibraryContents(edu.harvard.med.screensaver.model.libraries.Library)
   */
  @UIControllerMethod
  public String viewLibraryContents(final Library libraryIn)
  {
    logUserActivity("viewLibraryContents " + libraryIn);
    final Library[] libraryOut = new Library[1];
    _dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        Library library = (Library) _dao.reloadEntity(libraryIn);
        _dao.need(library,
                  "hbnWells",
                  "hbnWells.hbnSilencingReagents",
                  "hbnWells.hbnCompounds");
        WellSearchResults wellSearchResults = 
          new WellSearchResults(new ArrayList<Well>(library.getWells()),
                                LibrariesControllerImpl.this);
        _wellSearchResultsViewer.setWellSearchResults(wellSearchResults);
        libraryOut[0] = library;
      }
    });

    return "viewWellSearchResults";
  }
  
  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.ui.control.LibrariesController#viewWellSearchResults(edu.harvard.med.screensaver.ui.searchresults.WellSearchResults)
   */
  @UIControllerMethod
  public String viewWellSearchResults(WellSearchResults wellSearchResults)
  {
    logUserActivity("viewWellSearchResults");
    _wellSearchResultsViewer.setWellSearchResults(wellSearchResults);
    return "viewWellSearchResults";
  }
  
  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.ui.control.LibrariesController#viewWell()
   */
  @UIControllerMethod
  public String viewWell()
  {
    String wellId = (String) getRequestParameter("wellId");
    Well well = _dao.findEntityById(Well.class, wellId);
    return viewWell(well, null);
  }
  
  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.ui.control.LibrariesController#viewWell(edu.harvard.med.screensaver.model.libraries.Well, edu.harvard.med.screensaver.ui.searchresults.WellSearchResults)
   */
  @UIControllerMethod
  /**
   * @param wellSearchResults <code>null</code> if well was not found within
   *          the context of a search result
   */
  public String viewWell(final Well wellIn, WellSearchResults wellSearchResults)
  {
    logUserActivity("viewWell " + wellIn);
    // TODO: we should consider replicating this null-condition handling in our
    // other view*() methods (and in all controllers)
    if (wellIn == null) {
      showMessage("libraries.noSuchWell", wellIn.getPlateNumber(), wellIn.getWellName());
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    else {
      _wellViewer.setWellSearchResults(wellSearchResults);
      
      _dao.doInTransaction(new DAOTransaction() {
        public void runTransaction()
        {
          // TODO: try outer join HQL query instead of iteration, for performance improvement
          Well well = (Well) _dao.reloadEntity(wellIn);
          _dao.need(well,
                    "hbnSilencingReagents",
                    "hbnSilencingReagents.gene",
                    "hbnSilencingReagents.gene.genbankAccessionNumbers",
                    "hbnCompounds",
                    "hbnCompounds.compoundNames",
                    "hbnCompounds.pubchemCids",
                    "hbnCompounds.nscNumbers",
                    "hbnCompounds.casNumbers");
          _wellViewer.setWell(well);
          _wellViewer.setWellNameValueTable(new WellNameValueTable(LibrariesControllerImpl.this, well));
        }
      });

      return "viewWell";
    }
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.ui.control.LibrariesController#viewGene(edu.harvard.med.screensaver.model.libraries.Gene, edu.harvard.med.screensaver.ui.searchresults.WellSearchResults)
   */
  @UIControllerMethod
  public String viewGene(final Gene geneIn, WellSearchResults wellSearchResults)
  {
    logUserActivity("viewGene " + geneIn);
    _dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        if (geneIn != null) {
          Gene gene = (Gene) _dao.reloadEntity(geneIn);
          _dao.need(gene,
                    "genbankAccessionNumbers",
                    "hbnSilencingReagents",
                    "hbnSilencingReagents.hbnWells");
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

    return "viewGene";
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.ui.control.LibrariesController#viewCompound(edu.harvard.med.screensaver.model.libraries.Compound, edu.harvard.med.screensaver.ui.searchresults.WellSearchResults)
   */
  @UIControllerMethod
  public String viewCompound(final Compound compoundIn,
                             final WellSearchResults wellSearchResults)
  {
    logUserActivity("viewCompound " + compoundIn);
    _dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        if (compoundIn != null) {
          Compound compound = (Compound) _dao.reloadEntity(compoundIn);
          _dao.need(compound,
                    "compoundNames",
                    "pubchemCids",
                    "casNumbers",
                    "nscNumbers",
          "hbnWells");
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
    
    return "viewCompound";
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.ui.control.LibrariesController#importCompoundLibraryContents(edu.harvard.med.screensaver.model.libraries.Library)
   */
  @UIControllerMethod
  public String importCompoundLibraryContents(Library library)
  {
    logUserActivity("importCompoundLibraryContents " + library);
    _compoundLibraryContentsImporter.setLibrary(library);
    _compoundLibraryContentsImporter.setUploadedFile(null);
    _compoundLibraryContentsImporter.setCompoundLibraryContentsParser(_compoundLibraryContentsParser);
    _compoundLibraryContentsParser.clearErrors();
    return "importCompoundLibraryContents";
  }
  
  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.ui.control.LibrariesController#importCompoundLibraryContents(edu.harvard.med.screensaver.model.libraries.Library, org.apache.myfaces.custom.fileupload.UploadedFile)
   */
  @UIControllerMethod
  public String importCompoundLibraryContents(final Library libraryIn, final UploadedFile uploadedFile)
  {
    logUserActivity("importCompoundLibraryContents " + libraryIn + " " + uploadedFile.getName() + " " + uploadedFile.getSize());
    try {
      if (uploadedFile == null || uploadedFile.getInputStream().available() == 0) {
        showMessage("badUploadedFile", uploadedFile.getName());
        return "importCompoundLibraryContents";
      }
      _dao.doInTransaction(new DAOTransaction() 
      {
        public void runTransaction()
        {
          try {
            Library library = (Library) _dao.reloadEntity(libraryIn);
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
        return "importCompoundLibraryContents";
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
      return "importCompoundLibraryContents";
    }
    catch (Exception e) {
      reportSystemError(e);
      return "importCompoundLibraryContents";
    }
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.ui.control.LibrariesController#importRNAiLibraryContents(edu.harvard.med.screensaver.model.libraries.Library)
   */
  @UIControllerMethod
  public String importRNAiLibraryContents(Library library)
  {
    logUserActivity("importRNAiLibraryContents " + library);
    _rnaiLibraryContentsImporter.setLibrary(library);
    _rnaiLibraryContentsImporter.setUploadedFile(null);
    _rnaiLibraryContentsParser.clearErrors();
    return "importRNAiLibraryContents";
  }
  
  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.ui.control.LibrariesController#importRNAiLibraryContents(edu.harvard.med.screensaver.model.libraries.Library, org.apache.myfaces.custom.fileupload.UploadedFile, edu.harvard.med.screensaver.model.libraries.SilencingReagentType)
   */
  @UIControllerMethod
  public String importRNAiLibraryContents(
    final Library libraryIn,
    final UploadedFile uploadedFile,
    final SilencingReagentType silencingReagentType)
  {
    logUserActivity("importRNAiLibraryContents " + libraryIn + " " + uploadedFile.getName() + " " + uploadedFile.getSize());
    try {
      if (uploadedFile == null || uploadedFile.getInputStream().available() == 0) {
        showMessage("badUploadedFile", uploadedFile.getName());
        return "importRNAiLibraryContents";
      }
      _dao.doInTransaction(new DAOTransaction() 
      {
        public void runTransaction()
        {
          try {
            Library library = (Library) _dao.reloadEntity(libraryIn);
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
        return "importRNAiLibraryContents";
      }
      showMessage("libraries.importedLibraryContents", "libraryViewer");
      // TODO: to be correct, we should regen the search results, though I don't think anything in the results would actually be different after this import
      return viewLibrary(libraryIn, _libraryViewer.getLibrarySearchResults());
    }
    catch (DataAccessException e) {
      // TODO: should reload library and goto library viewer
      reportSystemError(e);
      return "importRNAiLibraryContents";
    }
    catch (Exception e) {
      reportApplicationError(e);
      return "importRNAiLibraryContents";
    }
  }

  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.ui.control.LibrariesController#unloadLibraryContents(edu.harvard.med.screensaver.model.libraries.Library)
   */
  @UIControllerMethod
  public String unloadLibraryContents(final Library libraryIn)
  {
    logUserActivity("unloadLibraryContents " + libraryIn);
    _dao.doInTransaction(new DAOTransaction() 
    {
      public void runTransaction() 
      {
        Library library = (Library) _dao.reloadEntity(libraryIn);
        _dao.deleteLibraryContents(library);
      }
    });
    showMessage("libraries.unloadedLibraryContents", "libraryViewer");
    return "viewLibrary";
  }
  
  // TODO: refactor code in WellSearchResults that exports well search results to our io.libraries.{compound,rnai} packages, and call directly
  /* (non-Javadoc)
   * @see edu.harvard.med.screensaver.ui.control.LibrariesController#downloadWellSearchResults(edu.harvard.med.screensaver.ui.searchresults.WellSearchResults)
   */
  public String downloadWellSearchResults(final WellSearchResults searchResultsIn)
  {
    logUserActivity("downloadWellSearchResults");
    _dao.doInTransaction(new DAOTransaction() 
    {
      @SuppressWarnings("unchecked")
      public void runTransaction() 
      {
        // reload the search result wells into the current hibernate session
        List<Well> reloadedWells = new ArrayList<Well>(searchResultsIn.getResultsSize());
        // TODO: optimize, as reattachment is slow, for large sets of wells
        for (Iterator iter = ((List<Well>) searchResultsIn.getDataModel().getWrappedData()).iterator(); iter.hasNext();) {
          Well well = (Well) iter.next();
          reloadedWells.add((Well) _dao.reattachEntity(well));
        }
        WellSearchResults searchResults = new WellSearchResults(reloadedWells, LibrariesControllerImpl.this);

        
        File searchResultsFile = null;
        PrintWriter searchResultsPrintWriter = null;
        FileOutputStream searchResultsFileOutputStream = null;
        try {
          searchResultsFile = File.createTempFile(
            "searchResults.",
            searchResultsIn.getDownloadFormat().equals(SearchResults.SD_FILE) ? ".sdf" : ".xls");
          if (searchResultsIn.getDownloadFormat().equals(SearchResults.SD_FILE)) {
            searchResultsPrintWriter = new PrintWriter(searchResultsFile);
            searchResults.writeSDFileSearchResults(searchResultsPrintWriter);
            searchResultsPrintWriter.close();
          }
          else {
            HSSFWorkbook searchResultsWorkbook = new HSSFWorkbook();
            searchResults.writeExcelFileSearchResults(searchResultsWorkbook);
            searchResultsFileOutputStream = new FileOutputStream(searchResultsFile);
            searchResultsWorkbook.write(searchResultsFileOutputStream);
            searchResultsFileOutputStream.close();
          }
          JSFUtils.handleUserFileDownloadRequest(
            getFacesContext(),
            searchResultsFile,
            searchResultsIn.getDownloadFormat().equals(SearchResults.SD_FILE) ? "chemical/x-mdl-sdfile" : Workbook.MIME_TYPE);
        }
        catch (IOException e)
        {
          showMessage("systemError");
          log.error(e.getMessage());
          throw new DAOTransactionRollbackException("could not create export file", e);
        }
        finally {
          IOUtils.closeQuietly(searchResultsPrintWriter);
          IOUtils.closeQuietly(searchResultsFileOutputStream);
          if (searchResultsFile != null && searchResultsFile.exists()) {
            searchResultsFile.delete();
          }
        }
      }
    });
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  
  // private instance methods
    
}
