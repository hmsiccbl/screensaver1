// $HeadURL$
// $Id$
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

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
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
import edu.harvard.med.screensaver.ui.libraries.LibraryViewer;
import edu.harvard.med.screensaver.ui.libraries.NaturalProductsLibraryContentsImporter;
import edu.harvard.med.screensaver.ui.libraries.RNAiLibraryContentsImporter;
import edu.harvard.med.screensaver.ui.libraries.WellCopyVolume;
import edu.harvard.med.screensaver.ui.libraries.WellCopyVolumeSearchResults;
import edu.harvard.med.screensaver.ui.libraries.WellFinder;
import edu.harvard.med.screensaver.ui.libraries.WellViewer;
import edu.harvard.med.screensaver.ui.namevaluetable.CompoundNameValueTable;
import edu.harvard.med.screensaver.ui.namevaluetable.GeneNameValueTable;
import edu.harvard.med.screensaver.ui.namevaluetable.LibraryNameValueTable;
import edu.harvard.med.screensaver.ui.namevaluetable.WellNameValueTable;
import edu.harvard.med.screensaver.ui.searchresults.LibrarySearchResults;
import edu.harvard.med.screensaver.ui.searchresults.WellSearchResults;
import edu.harvard.med.screensaver.ui.util.JSFUtils;
import edu.harvard.med.screensaver.util.Pair;

import org.apache.log4j.Logger;
import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.springframework.dao.DataAccessException;

/**
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class LibrariesController extends AbstractUIController
{

  // private static final fields

  private static final Logger log = Logger.getLogger(LibrariesController.class);


  // instance variables

  private GenericEntityDAO _dao;
  private LibrariesDAO _librariesDao;
  private WellFinder _wellFinder;
  private LibrarySearchResults _librariesBrowser;
  private LibraryViewer _libraryViewer;
  private WellSearchResults _wellsBrowser;
  private WellViewer _wellViewer;
  private WellCopyVolumeSearchResults _wellCopyVolumesBrowser;
  private GeneViewer _geneViewer;
  private CompoundViewer _compoundViewer;
  private CompoundLibraryContentsImporter _compoundLibraryContentsImporter;
  private SDFileCompoundLibraryContentsParser _compoundLibraryContentsParser;
  private NaturalProductsLibraryContentsImporter _naturalProductsLibraryContentsImporter;
  private NaturalProductsLibraryContentsParser _naturalProductsLibraryContentsParser;
  private RNAiLibraryContentsImporter _rnaiLibraryContentsImporter;
  private RNAiLibraryContentsParser _rnaiLibraryContentsParser;
  private PlateWellListParser _plateWellListParser;


  // constructors




  // controller methods

  /**
   * @motivation for CGLIB2
   */
  protected LibrariesController()
  {
  }

  public LibrariesController(GenericEntityDAO dao,
                             LibrariesDAO librariesDao,
                             WellFinder wellFinder,
                             LibrarySearchResults librariesBrowser,
                             LibraryViewer libraryViewer,
                             WellSearchResults wellsBrowser,
                             WellViewer wellViewer,
                             WellCopyVolumeSearchResults wellCopyVolumesBrowser,
                             GeneViewer geneViewer,
                             CompoundViewer compoundViewer,
                             CompoundLibraryContentsImporter compoundLibraryContentsImporter,
                             SDFileCompoundLibraryContentsParser compoundLibraryContentsParser,
                             NaturalProductsLibraryContentsImporter naturalProductsLibraryContentsImporter,
                             NaturalProductsLibraryContentsParser naturalProductsLibraryContentsParser,
                             RNAiLibraryContentsImporter rnaiLibraryContentsImporter,
                             RNAiLibraryContentsParser rnaiLibraryContentsParser,
                             PlateWellListParser plateWellListParser)
  {
    _dao = dao;
    _librariesDao = librariesDao;
    _wellFinder = wellFinder;
    _librariesBrowser = librariesBrowser;
    _libraryViewer = libraryViewer;
    _wellsBrowser = wellsBrowser;
    _wellViewer = wellViewer;
    _wellCopyVolumesBrowser = wellCopyVolumesBrowser;
    _geneViewer = geneViewer;
    _compoundViewer = compoundViewer;
    _compoundLibraryContentsImporter = compoundLibraryContentsImporter;
    _compoundLibraryContentsParser = compoundLibraryContentsParser;
    _naturalProductsLibraryContentsImporter = naturalProductsLibraryContentsImporter;
    _naturalProductsLibraryContentsParser = naturalProductsLibraryContentsParser;
    _rnaiLibraryContentsImporter = rnaiLibraryContentsImporter;
    _rnaiLibraryContentsParser = rnaiLibraryContentsParser;
    _plateWellListParser = plateWellListParser;
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
    return viewWell(_plateWellListParser.lookupWell(plateNumber, wellName));
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
          result[0] = viewWell(parseResult.getParsedWellKeys().first());
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
        _wellsBrowser.setContents(foundWells);
        result[0] = VIEW_WELL_SEARCH_RESULTS;
      }
    });
    return result[0];
  }

  @UIControllerMethod
  public String downloadWellSDFile(final Well well)
  {
    try {
      WellsDataExporter dataExporter = new WellsDataExporter(_dao, WellsDataExporterFormat.SDF);
      Set<Well> wells = new HashSet<Well>(1, 2.0f);
      wells.add(well);
      InputStream inputStream = dataExporter.export(wells);
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
        _wellCopyVolumesBrowser.setContents(foundWellCopyVolumes);
        result[0] = VIEW_WELL_VOLUME_SEARCH_RESULTS;
      }
    });
    return result[0];
  }

  @UIControllerMethod
  public String browseLibraries()
  {
    List<Library> libraries = _librariesDao.findLibrariesDisplayedInLibrariesBrowser();
    _librariesBrowser.setContents(libraries);
    return BROWSE_LIBRARIES;
  }

  @UIControllerMethod
  public String viewLibrary()
  {
    String libraryIdAsString = (String) getRequestParameter("libraryId");
    Integer libraryId = Integer.parseInt(libraryIdAsString);
    Library library = _dao.findEntityById(Library.class, libraryId);
    return viewLibrary(library);
  }

  @UIControllerMethod
  public String viewLibrary(final Library libraryIn)
  {
    logUserActivity(VIEW_LIBRARY + " " + libraryIn);

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
        _wellsBrowser.setContents(new ArrayList<Well>(library.getWells()));
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
        _wellCopyVolumesBrowser.setContents(wellCopyVolumes);
        result[0] = VIEW_WELL_VOLUME_SEARCH_RESULTS;
      }
    });

    return result[0];
  }

  @UIControllerMethod
  public String viewWell()
  {
    WellKey wellKey = new WellKey((String) getRequestParameter("wellId"));
    return viewWell(wellKey);
  }

  public String viewWell(Well well)
  {
    if (well == null) {
      reportApplicationError("attempted to view an unknown well (not in database)");
      return REDISPLAY_PAGE_ACTION_RESULT;

    }
    return viewWell(well.getWellKey());
  }

  /**
   * @param wellSearchResults <code>null</code> if well was not found within
   *          the context of a search result
   */
  @UIControllerMethod
  public String viewWell(final WellKey wellKey)
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
          _wellViewer.setWellNameValueTable(new WellNameValueTable(LibrariesController.this, well));
        }
      });
    }
    catch (IllegalArgumentException e) {
      showMessage("libraries.noSuchWell", wellKey.getPlateNumber(), wellKey.getWellName());
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    return VIEW_WELL;
  }

  @UIControllerMethod
  public String viewGene(final Gene geneIn)
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
          _geneViewer.setGeneNameValueTable(new GeneNameValueTable(LibrariesController.this, gene));
        }
        else {
          _geneViewer.setGene(null);
          _geneViewer.setGeneNameValueTable(null);
        }
      }
    });

    Well parentWellOfInterest = _wellsBrowser == null ? null : _wellsBrowser.getCurrentRowDataObject();
    _geneViewer.setParentWellOfInterest(parentWellOfInterest);

    return VIEW_GENE;
  }

  @UIControllerMethod
  public String viewCompound(final Compound compoundIn)
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
            new CompoundNameValueTable(LibrariesController.this, compound));
        }
        else {
          _compoundViewer.setCompound(null);
          _compoundViewer.setCompoundNameValueTable(null);
        }
      }
    });

    Well parentWellOfInterest = _wellsBrowser == null ? null : _wellsBrowser.getCurrentRowDataObject();
    _compoundViewer.setParentWellOfInterest(parentWellOfInterest);

    return VIEW_COMPOUND;
  }

  @UIControllerMethod
  public String importCompoundLibraryContents(Library library)
  {
    logUserActivity(IMPORT_COMPOUND_LIBRARY_CONTENTS + " " + library);
    _compoundLibraryContentsImporter.setLibrary(library);
    _compoundLibraryContentsImporter.setUploadedFile(null);
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
        return viewLibrary(libraryIn);
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
        return viewLibrary(libraryIn);
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
      return viewLibrary(libraryIn);
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
  public String unloadLibraryContents(final Library libraryIn)
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
    return viewLibrary(libraryIn);
  }
}
