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
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.myfaces.custom.fileupload.UploadedFile;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.io.libraries.compound.SDFileCompoundLibraryContentsParser;
import edu.harvard.med.screensaver.io.libraries.rnai.RNAiLibraryContentsParser;
import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.ui.libraries.CompoundLibraryContentsImporter;
import edu.harvard.med.screensaver.ui.libraries.CompoundViewer;
import edu.harvard.med.screensaver.ui.libraries.GeneViewer;
import edu.harvard.med.screensaver.ui.libraries.LibrariesBrowser;
import edu.harvard.med.screensaver.ui.libraries.LibraryViewer;
import edu.harvard.med.screensaver.ui.libraries.RNAiLibraryContentsImporter;
import edu.harvard.med.screensaver.ui.libraries.WellFinder;
import edu.harvard.med.screensaver.ui.libraries.WellSearchResultsViewer;
import edu.harvard.med.screensaver.ui.libraries.WellViewer;
import edu.harvard.med.screensaver.ui.searchresults.LibrarySearchResults;
import edu.harvard.med.screensaver.ui.searchresults.WellSearchResults;
import edu.harvard.med.screensaver.util.StringUtils;

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
  private static final Pattern _plateNumberPattern =
    Pattern.compile("^\\s*((PL)[-_]?)?(\\d+)\\s*$");
  private static final Pattern _wellNamePattern =
    Pattern.compile("^\\s*([A-Ha-h]([0-9]|[01][0-9]|2[0-4]))\\s*$");


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
    WellSearchResults searchResults =
      new edu.harvard.med.screensaver.ui.searchresults.WellSearchResults(wells, this);
    return viewWellSearchResults(searchResults);
  }
  
  @UIControllerMethod
  public String browseLibraries()
  {
    if (getLibrariesBrowser().getLibrarySearchResults() == null) {
        List<Library> libraries = _dao.findAllEntitiesWithType(Library.class);
        _librariesBrowser.setLibrarySearchResults(new LibrarySearchResults(libraries, this));
    }
    return "browseLibraries";
  }
  
  @UIControllerMethod
  public String viewLibrary(Library library, LibrarySearchResults librarySearchResults)
  {
    _libraryViewer.setLibrarySearchResults(librarySearchResults);
    _libraryViewer.setLibrary(library);
    return "viewLibrary";
  }
  
  @UIControllerMethod
  public String viewLibraryContents(Library library)
  {
    WellSearchResults wellSearchResults = new WellSearchResults(
        new ArrayList<Well>(library.getWells()),
        this);
    return viewWellSearchResults(wellSearchResults);
  }
  
  @UIControllerMethod
  public String viewWellSearchResults(WellSearchResults wellSearchResults)
  {
    _wellSearchResultsViewer.setWellSearchResults(wellSearchResults);
    return "viewWellSearchResults";
  }
  
  @UIControllerMethod
  public String viewWell(Well well, WellSearchResults wellSearchResults)
  {
    _wellViewer.setWellSearchResults(wellSearchResults);
    _wellViewer.setWell(well);
    return "viewWell";
  }

  @UIControllerMethod
  public String viewGene(Gene gene, WellSearchResults wellSearchResults)
  {
    _geneViewer.setWellSearchResults(wellSearchResults);
    _geneViewer.setGene(gene);
    return "viewGene";
  }

  @UIControllerMethod
  public String viewCompound(
    Compound compound,
    edu.harvard.med.screensaver.ui.searchresults.WellSearchResults wellSearchResults)
  {
    _compoundViewer.setWellSearchResults(wellSearchResults);
    _compoundViewer.setCompound(compound);
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
  public String importCompoundLibraryContents(final Library library, final UploadedFile uploadedFile)
  {
    try {
      if (uploadedFile != null && uploadedFile.getInputStream().available() > 0) {
        _dao.doInTransaction(new DAOTransaction() {
          public void runTransaction()
          {
            try {
              _compoundLibraryContentsParser.parseLibraryContents(
                library,
                new File(uploadedFile.getName()),
                uploadedFile.getInputStream());
              _dao.persistEntity(library);
            }
            catch (IOException e) {
              reportSystemError(e);
            }
          }
        });
      }
      else {
        showMessage("badUploadedFile", uploadedFile.getName());
        return "importCompoundLibraryContents";
      }

      if (_compoundLibraryContentsParser.getHasErrors()) {
        return "importCompoundLibraryContents";
      }
      else {
        showMessage("libraries.importedLibraryContents", "libraryViewer");
        return "viewLibrary";
      }
    }
    catch (IOException e) {
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
    final Library library,
    final UploadedFile uploadedFile,
    final SilencingReagentType silencingReagentType)
  {
    try {
      if (uploadedFile != null && uploadedFile.getInputStream().available() > 0) {
        _rnaiLibraryContentsParser.setSilencingReagentType(silencingReagentType);
        _rnaiLibraryContentsParser.parseLibraryContents(
          library,
          new File(uploadedFile.getName()), uploadedFile.getInputStream());
        _dao.persistEntity(library);
      }
      else {
        showMessage("badUploadedFile", uploadedFile.getName());
        return "importRNAiLibraryContents";
      }

      if (_rnaiLibraryContentsParser.getHasErrors()) {
        return "importRNAiLibraryContents";
      }
      else {
        showMessage("libraries.importedLibraryContents", "libraryViewer");
        return "viewLibrary";
      }
    }
    catch (IOException e) {
      reportSystemError(e);
      return "importRNAiLibraryContents";
    }
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
  private List<Well> lookupWellsFromPlateWellList(String plateWellList)
  {
    List<Well> wells = new ArrayList<Well>();
    BufferedReader plateWellListReader = new BufferedReader(new StringReader(plateWellList));
    try {
      for (
        String line = plateWellListReader.readLine();
        line != null;
        line = plateWellListReader.readLine()) {
        
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
  private Well lookupWell(Integer plateNumber, String wellName) {
    Well well = _dao.findWell(plateNumber, wellName);
    if (well == null) {
      showMessage("libraries.noSuchWell", "searchResults", plateNumber.toString(), wellName);
    }
    return well;
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
      plateNumber = matcher.group(3);
      return Integer.parseInt(plateNumber);
    }
    else {
      showMessage("libraries.invalidPlateNumber", plateNumber.toString());
      return null;
    }
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
