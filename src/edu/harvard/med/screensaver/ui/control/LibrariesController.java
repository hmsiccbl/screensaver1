// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.control;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
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
import edu.harvard.med.screensaver.util.StringUtils;

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
public class LibrariesController extends AbstractUIController
{

  // private static final fields
  
  private static final Logger log = Logger.getLogger(LibrariesController.class);
  
  // TODO: consider moving these to WellKey
  private static final Pattern _plateWellHeaderLinePattern = Pattern.compile(
    "^\\s*plate\\s+well\\s*$",
    Pattern.CASE_INSENSITIVE);
  private static final Pattern _plateWellPattern = Pattern.compile(
    "^\\s*((PL[-_]?)?(\\d+))[A-P]([0-9]|[01][0-9]|2[0-4]).*",
    Pattern.CASE_INSENSITIVE);
  private static final Pattern _plateNumberPattern = Pattern.compile(
    "^\\s*(PL[-_]?)?(\\d+)\\s*$",
    Pattern.CASE_INSENSITIVE);
  private static final Pattern _wellNamePattern = Pattern.compile(
    "^\\s*([A-P]([0-9]|[01][0-9]|2[0-4]))\\s*$",
    Pattern.CASE_INSENSITIVE);


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
    _wellFinder.setLibrariesController(this);
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
    _libraryViewer.setLibrariesController(this);
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
    _wellViewer.setLibrariesController(this);
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
    _compoundViewer.setLibrariesController(this);
  }
  
  public CompoundLibraryContentsImporter getCompoundLibraryContentsImporter()
  {
    return _compoundLibraryContentsImporter;
  }
  
  public void setCompoundLibraryContentsImporter(
    CompoundLibraryContentsImporter compoundLibraryContentsImporter)
  {
    _compoundLibraryContentsImporter = compoundLibraryContentsImporter;
    _compoundLibraryContentsImporter.setLibrariesController(this);
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
    _rnaiLibraryContentsImporter.setLibrariesController(this);
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
  
  /**
   * Go to the {@link WellFinder} page.
   * @return the control code "findWells"
   */
  @UIControllerMethod
  public String findWells()
  {
    return "findWells";
  }

  /**
   * Find the well with the specified plate number and well name, and go to the appropriate next
   * page depending on the result.
   * @return the control code for the appropriate next page
   */
  @UIControllerMethod
  public String findWell(String plateNumber, String wellName)
  {
    Well well = lookupWell(plateNumber, wellName);
    if (well == null) {
      return "findWells";
    }
    return viewWell(well, null);
  }
  
  /**
   * Find the wells specified in the plate-well list, and go to the {@link WellSearchResultsViewer}
   * page.
   * @return the controler code for the next appropriate page
   */
  @UIControllerMethod
  public String findWells(String plateWellList)
  {
    List<Well> wells = lookupWellsFromPlateWellList(plateWellList);
    if (wells.size() == 1) {
      return viewWell(wells.get(0), null);
    }
    WellSearchResults searchResults =
      new edu.harvard.med.screensaver.ui.searchresults.WellSearchResults(wells, this);
    return viewWellSearchResults(searchResults);
  }
  
  @UIControllerMethod
  public String browseLibraries()
  {
    if (getLibrariesBrowser().getLibrarySearchResults() == null) {
      //List<Library> libraries = _dao.findAllEntitiesWithType(Library.class);
      List<Library> libraries = _dao.findLibrariesDisplayedInLibrariesBrowser();
      _librariesBrowser.setLibrarySearchResults(new LibrarySearchResults(libraries, this));
    }
    return "browseLibraries";
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
  public String viewLibrary(final Library libraryIn, LibrarySearchResults librarySearchResults)
  {
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
  
  @UIControllerMethod
  public String viewLibraryContents(final Library libraryIn)
  {
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
                                LibrariesController.this);
        _wellSearchResultsViewer.setWellSearchResults(wellSearchResults);
        libraryOut[0] = library;
      }
    });

    return "viewWellSearchResults";
  }
  
  @UIControllerMethod
  public String viewWellSearchResults(WellSearchResults wellSearchResults)
  {
    _wellSearchResultsViewer.setWellSearchResults(wellSearchResults);
    return "viewWellSearchResults";
  }
  
  @UIControllerMethod
  public String viewWell()
  {
    String wellId = (String) getRequestParameter("wellId");
    Well well = _dao.findEntityById(Well.class, wellId);
    return viewWell(well, null);
  }
  
  @UIControllerMethod
  /**
   * @param wellSearchResults <code>null</code> if well was not found within
   *          the context of a search result
   */
  public String viewWell(final Well wellIn, WellSearchResults wellSearchResults)
  {
    // TODO: we should consider replicating this null-condition handling in our
    // other view*() methods (and in all controllers)
    if (wellIn == null) {
      this.showMessage("libraries.noSuchWell", wellIn.getPlateNumber(), wellIn.getWellName());
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
          _wellViewer.setWellNameValueTable(new WellNameValueTable(LibrariesController.this, well));
        }
      });

      return "viewWell";
    }
  }

  @UIControllerMethod
  public String viewGene(final Gene geneIn, WellSearchResults wellSearchResults)
  {
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
          _geneViewer.setGeneNameValueTable(new GeneNameValueTable(LibrariesController.this, gene));
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

  @UIControllerMethod
  public String viewCompound(final Compound compoundIn,
                             final WellSearchResults wellSearchResults)
  {
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
            new CompoundNameValueTable(LibrariesController.this, compound));
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

  /**
   * Go to the {@link CompoundLibraryContentsImporter} page.
   * @param library the library to import
   * @return the control code "importCompoundLibraryContents"
   */
  @UIControllerMethod
  public String importCompoundLibraryContents(Library library)
  {
    _compoundLibraryContentsImporter.setLibrary(library);
    _compoundLibraryContentsImporter.setUploadedFile(null);
    _compoundLibraryContentsParser.clearErrors();
    return "importCompoundLibraryContents";
  }
  
  /**
   * Load the compound library contents from the file, and go to the appropriate next page
   * depending on the result.
   * @return the control code for the appropriate next page
   */
  @UIControllerMethod
  public String importCompoundLibraryContents(final Library libraryIn, final UploadedFile uploadedFile)
  {
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

  /**
   * Go to the {@link RNAiLibraryContentsImporter} page.
   * @param library the library to import
   * @return the control code "importRNAiLibraryContents"
   */
  @UIControllerMethod
  public String importRNAiLibraryContents(Library library)
  {
    _rnaiLibraryContentsImporter.setLibrary(library);
    _rnaiLibraryContentsImporter.setUploadedFile(null);
    _rnaiLibraryContentsParser.clearErrors();
    return "importRNAiLibraryContents";
  }
  
  /**
   * Load the RNAi library contents from the file, and go to the appropriate next page
   * depending on the result.
   * @return the control code for the appropriate next page
   */
  @UIControllerMethod
  public String importRNAiLibraryContents(
    final Library libraryIn,
    final UploadedFile uploadedFile,
    final SilencingReagentType silencingReagentType)
  {
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

  @UIControllerMethod
  public String unloadLibraryContents(final Library libraryIn)
  {
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
  public String downloadWellSearchResults(final WellSearchResults searchResultsIn)
  {
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
        WellSearchResults searchResults = new WellSearchResults(reloadedWells, LibrariesController.this);

        
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
    
  /**
   * Parse and return the well for the plate number and well name.
   * Helper method for {@link #findWell(Integer, String)}.
   * @param plateNumberString the unparsed plate number
   * @param wellName the unparsed well name
   * @return the well
   */
  private Well lookupWell(String plateNumberString, String wellName)
  {
    Integer plateNumber = parsePlateNumber(plateNumberString);
    wellName = parseWellName(wellName);
    if (plateNumber == null || wellName == null) {
      return null;
    }
    return lookupWell(plateNumber, wellName);
  }

  /**
   * Parse and return the list of wells from the plate-well list.
   * Helper method for {@link #findWells(String)}.
   * @param plateWellList the plate-well list
   * @return the list of wells
   */
  private List<Well> lookupWellsFromPlateWellList(final String plateWellList)
  {
    final List<Well> wells = new ArrayList<Well>();
    _dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        BufferedReader plateWellListReader = new BufferedReader(new StringReader(plateWellList));
        try {
          for (
            String line = plateWellListReader.readLine();
            line != null;
            line = plateWellListReader.readLine()) {

            // skip lines that say "Plate Well"
            Matcher matcher = _plateWellHeaderLinePattern.matcher(line);
            if (matcher.matches()) {
              continue;
            }

            // separate initial plate and well with a space if necessary
            line = splitInitialPlateWell(line);

            // split the line into tokens; should be one plate, then one or more wells
            String [] tokens = line.split("[\\s;,]+");
            if (tokens.length == 0) {
              continue;
            }

            Integer plateNumber = parsePlateNumber(tokens[0]);
            for (int i = 1; i < tokens.length; i ++) {
              String wellName = parseWellName(tokens[i]);
              if (plateNumber == null || wellName == null) {
                continue;
              }
              Well well = lookupWell(plateNumber, wellName);
              if (well != null) {
                wells.add(well);
              }
            }
          }
        }
        catch (IOException e) {
          showMessage("libraries.unexpectedErrorReadingPlateWellList", "searchResults");
        }
      }
    });
    return wells;
  }
  
  /**
   * Lookup the well from the dao by plate number and well name.
   * Helper method for {@link #lookupWell(String, String)} and {@link
   * #lookupWellsFromPlateWellList(String)}.
   * @param plateNumber the parsed plate number
   * @param wellName the parse well name
   * @return
   */
  private Well lookupWell(final Integer plateNumber, final String wellName) {
    final Well[] result = new Well[1];
    _dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Well well = _dao.findWell(new WellKey(plateNumber, wellName)); 
        if (well == null) {
          showMessage("libraries.noSuchWell", "searchResults", plateNumber.toString(), wellName);
        }
        else {
          // force initialization of persistent collections needed by search result viewer
          well.getGenes().size();
          well.getCompounds().size();
        }
        result[0] = well;
      }
    });
    return result[0];
  }
  
  /**
   * Insert a space between the first plate number and well name if there is no
   * space there already.
   * @param line the line to patch up
   * @return the patched up line
   */
  private String splitInitialPlateWell(String line)
  {
    Matcher matcher = _plateWellPattern.matcher(line);
    if (matcher.matches()) {
      int spliceIndex = matcher.group(1).length();
      line = line.substring(0, spliceIndex) + " " + line.substring(spliceIndex);
    }
    return line;
  }
  
  /**
   * Parse the plate number.
   * Helper method for {@link #lookupWell(String, String)} and {@link
   * #lookupWellsFromPlateWellList(String)}.
   */
  private Integer parsePlateNumber(String plateNumber)
  {
    Matcher matcher = _plateNumberPattern.matcher(plateNumber);
    if (matcher.matches()) {
      plateNumber = matcher.group(2);
      try {
        return Integer.parseInt(plateNumber);
      }
      catch (NumberFormatException e) {
        // this seems unlikely given the _plateNumberPattern match, but it's actually possible
        // to match that pattern and still get a NFE, if the number is larger than MAXINT
      }
    }
    showMessage("libraries.invalidPlateNumber", plateNumber.toString());
    return null;
  }
  
  /**
   * Parse the well name.
   * Helper method for {@link #lookupWell(String, String)} and {@link
   * #lookupWellsFromPlateWellList(String)}.
   */
  private String parseWellName(String wellName)
  {
    Matcher matcher = _wellNamePattern.matcher(wellName);
    if (matcher.matches()) {
      wellName = matcher.group(1);
      if (wellName.length() == 2) {
        wellName = wellName.charAt(0) + "0" + wellName.charAt(1);
      }
      wellName = StringUtils.capitalize(wellName);
      return wellName;
    }
    else {
      showMessage("libraries.invalidWellName", wellName);
      return null;
    }
  }
}
