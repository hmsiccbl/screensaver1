// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.util.ArrayList;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.ui.AbstractController;
import edu.harvard.med.screensaver.ui.SearchResults;

public class LibraryViewerController extends AbstractController
{
  private static Logger log = Logger.getLogger(LibraryViewerController.class);
  
  private DAO _dao;
  private Library _library;
  private SearchResults<Library> _searchResults;
  private RNAiLibraryContentsImporterController _rnaiLibraryContentsImporter;
  private CompoundLibraryContentsImporterController _compoundLibraryContentsImporter;
  private WellViewerController _wellViewerController;
  private GeneViewerController _geneViewerController;
  private CompoundViewerController _compoundViewerController;
  private WellSearchResultsController _wellSearchResultsController;
  private String _usageMode; // "create" or "edit"
  private boolean _advancedMode;
  
  /* Property getter/setter methods */
  
  public void setDao(DAO dao)
  {
    _dao = dao;
  }
  
  public void setLibrary(Library library)
  {
    _library = library;
  }

  public Library getLibrary()
  {
    return _library;
  }

  public SearchResults<Library> getSearchResults()
  {
    return _searchResults;
  }

  public void setSearchResults(SearchResults<Library> searchResults)
  {
    _searchResults = searchResults;
  }

  public RNAiLibraryContentsImporterController getRnaiLibraryContentsImporter()
  {
    return _rnaiLibraryContentsImporter;
  }

  public void setRnaiLibraryContentsImporter(
    RNAiLibraryContentsImporterController rnaiLibraryContentsImporter)
  {
    _rnaiLibraryContentsImporter = rnaiLibraryContentsImporter;
  }

  public CompoundLibraryContentsImporterController getCompoundLibraryContentsImporter()
  {
    return _compoundLibraryContentsImporter;
  }

  public void setCompoundLibraryContentsImporter(
    CompoundLibraryContentsImporterController compoundLibraryContentsImporter)
  {
    _compoundLibraryContentsImporter = compoundLibraryContentsImporter;
  }
  
  public WellViewerController getWellViewer()
  {
    return _wellViewerController;
  }

  public void setWellViewer(WellViewerController wellViewerController)
  {
    _wellViewerController = wellViewerController;
    _wellViewerController.setLibraryViewer(this);
  }

  public GeneViewerController getGeneViewer()
  {
    return _geneViewerController;
  }

  public void setGeneViewer(GeneViewerController geneViewerController)
  {
    _geneViewerController = geneViewerController;
  }

  public CompoundViewerController getCompoundViewer()
  {
    return _compoundViewerController;
  }

  public void setCompoundViewer(CompoundViewerController compoundViewerController)
  {
    _compoundViewerController = compoundViewerController;
  }

  public WellSearchResultsController getWellSearchResults()
  {
    return _wellSearchResultsController;
  }

  public void setWellSearchResults(WellSearchResultsController wellSearchResultsController)
  {
    _wellSearchResultsController = wellSearchResultsController;
  }

  public void setUsageMode(String usageMode)
  {
    _usageMode = usageMode;
  }
  
  public String getUsageMode()
  {
    return _usageMode;
  }
  
  public boolean isAdvancedMode()
  {
    return _advancedMode;
  }

  public void setAdvancedMode(boolean advancedMode) 
  {
    _advancedMode = advancedMode;
  }

  public boolean getIsRNAiLibrary()
  {
    return _library != null && _library.getLibraryType().equals(LibraryType.RNAI);
  }

  public boolean getIsCompoundLibrary()
  {
    return _library != null && ! _library.getLibraryType().equals(LibraryType.RNAI);
  }
  
  public int getLibrarySize()
  {
    if (_library == null) {
      return 0;
    }
    return _library.getWells().size();
  }
  
  
  /* JSF Application methods */

  /**
   * A command to saved the user's edits.
   */
  public String save()
  {
    return create();
  }
  
  public String create()
  {
    try {
      _dao.persistEntity(_library);
    }
    catch (Exception e) {
      String msg = "error during entity save/create: " + e.getMessage();
      log.info(msg);
      FacesContext.getCurrentInstance().addMessage("libraryForm", new FacesMessage(msg));
      return REDISPLAY_PAGE_ACTION_RESULT; // redisplay
    }
    return DONE_ACTION_RESULT;
  }
  
  public String cancel() 
  {
    return "cancel";
  }

  public String goImportRNAiLibraryContents()
  {
    _rnaiLibraryContentsImporter.setLibraryViewer(this);
    return "goImportRNAiLibraryContents";
  }

  public String goImportCompoundLibraryContents()
  {
    _compoundLibraryContentsImporter.setLibraryViewer(this);
    return "goImportCompoundLibraryContents";
  }

  public String viewLibraryContents()
  {
    SearchResults<Well> searchResults = new WellSearchResults(
      new ArrayList<Well>(_library.getWells()),
      this,
      _wellViewerController,
      _compoundViewerController,
      _geneViewerController);
    _wellSearchResultsController.setSearchResults(searchResults);
    return "goWellSearchResults";
  }
  
  
  /* JSF Action event listeners */

  /**
   * An action event listener to revert the user's edits.
   */
  public void revertEventHandler(ActionEvent event) {
    log.debug("revert action event handled");
    FacesContext.getCurrentInstance().addMessage("libraryForm", new FacesMessage("\"Revert\" command not yet implemented!"));
  }
  
  public void showAdvancedEventListener(ActionEvent event) {
    _advancedMode = event.getComponent().getId().equals("showAdvanced");
    log.debug("show advanced action invoked: advancedMode=" + _advancedMode);
    FacesContext.getCurrentInstance().renderResponse();
  }
}
