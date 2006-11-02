// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.control;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.searchresults.LibrarySearchResults;
import edu.harvard.med.screensaver.ui.searchresults.WellSearchResults;
import edu.harvard.med.screensaver.ui.view.libraries.CompoundLibraryContentsImporter;
import edu.harvard.med.screensaver.ui.view.libraries.CompoundViewer;
import edu.harvard.med.screensaver.ui.view.libraries.GeneViewer;
import edu.harvard.med.screensaver.ui.view.libraries.LibrariesBrowser;
import edu.harvard.med.screensaver.ui.view.libraries.LibraryViewer;
import edu.harvard.med.screensaver.ui.view.libraries.RNAiLibraryContentsImporter;
import edu.harvard.med.screensaver.ui.view.libraries.WellSearchResultsViewer;
import edu.harvard.med.screensaver.ui.view.libraries.WellViewer;

/**
 * 
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class LibrariesController extends AbstractBackingBean
{
  private static Logger log = Logger.getLogger(LibrariesController.class);

  
  // instance variables
  
  private DAO _dao;
  private LibrariesBrowser _librariesBrowser;
  private LibraryViewer _libraryViewer;
  private WellSearchResultsViewer _wellSearchResultsViewer;
  private WellViewer _wellViewer;
  private GeneViewer _geneViewer;
  private CompoundViewer _compoundViewer;
  private CompoundLibraryContentsImporter _compoundLibraryContentsImporter;
  private RNAiLibraryContentsImporter _rnaiLibraryContentsImporter;
  
  
  // public getters and setters
  
  public DAO getDao()
  {
    return _dao;
  }
  
  public void setDao(DAO dao)
  {
    _dao = dao;
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
  

  // controller methods
  
  public String browseLibraries()
  {
    if (getLibrariesBrowser().getLibrarySearchResults() == null) {
        List<Library> libraries = _dao.findAllEntitiesWithType(Library.class);
        _librariesBrowser.setLibrarySearchResults(new LibrarySearchResults(libraries, this));
    }
    return "browseLibraries";
  }
  
  public String viewLibrary(Library library, LibrarySearchResults librarySearchResults)
  {
    _libraryViewer.setLibrarySearchResults(librarySearchResults);
    _libraryViewer.setLibrary(library);
    return "viewLibrary";
  }
  
  public String viewLibraryContents(Library library)
  {
    WellSearchResults wellSearchResults = new WellSearchResults(
        new ArrayList<Well>(library.getWells()),
        this);
    return viewWellSearchResults(wellSearchResults);
  }
  
  public String viewWellSearchResults(WellSearchResults wellSearchResults)
  {
    _wellSearchResultsViewer.setWellSearchResults(wellSearchResults);
    return "viewWellSearchResults";
  }
  
  public String viewWell(Well well, WellSearchResults wellSearchResults)
  {
    _wellViewer.setWellSearchResults(wellSearchResults);
    _wellViewer.setWell(well);
    return "viewWell";
  }

  public String viewGene(Gene gene, WellSearchResults wellSearchResults)
  {
    _geneViewer.setWellSearchResults(wellSearchResults);
    _geneViewer.setGene(gene);
    return "viewGene";
  }

  public String viewCompound(
    Compound compound,
    edu.harvard.med.screensaver.ui.searchresults.WellSearchResults wellSearchResults)
  {
    _compoundViewer.setWellSearchResults(wellSearchResults);
    _compoundViewer.setCompound(compound);
    return "viewCompound";
  }

  public String importCompoundLibraryContents(Library library)
  {
    _compoundLibraryContentsImporter.setLibrary(library);
    _compoundLibraryContentsImporter.setUploadedFile(null);
    _compoundLibraryContentsImporter.getCompoundLibraryContentsParser().clearErrors();
    return "importCompoundLibraryContents";
  }

  public String importRNAiLibraryContents(Library library)
  {
    _rnaiLibraryContentsImporter.setLibrary(library);
    _rnaiLibraryContentsImporter.setUploadedFile(null);
    _rnaiLibraryContentsImporter.getRnaiLibraryContentsParser().clearErrors();
    return "importRNAiLibraryContents";
  }
}
