// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/ui/libraries/LibraryViewer.java $
// $Id: LibraryViewer.java 711 2006-10-31 23:40:24Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.view.libraries;

import java.util.ArrayList;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.searchresults.SearchResults;

public class LibraryViewer extends AbstractBackingBean
{
  private static Logger log = Logger.getLogger(LibraryViewer.class);
  
  private DAO _dao;
  private Library _library;
  private SearchResults<Library> _searchResults;
  private SearchResults<Well> _wellSearchResults;
  private RNAiLibraryContentsImporter _rnaiLibraryContentsImporter;
  private CompoundLibraryContentsImporter _compoundLibraryContentsImporter;
  private WellViewer _wellViewerController;
  private GeneViewer _geneViewerController;
  private CompoundViewer _compoundViewer;
  private WellSearchResults _wellSearchResultsView;
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

  public RNAiLibraryContentsImporter getRnaiLibraryContentsImporter()
  {
    return _rnaiLibraryContentsImporter;
  }

  public void setRnaiLibraryContentsImporter(
    RNAiLibraryContentsImporter rnaiLibraryContentsImporter)
  {
    _rnaiLibraryContentsImporter = rnaiLibraryContentsImporter;
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
  
  public WellViewer getWellViewer()
  {
    return _wellViewerController;
  }

  public void setWellViewer(WellViewer wellViewerController)
  {
    _wellViewerController = wellViewerController;
    _wellViewerController.setLibraryViewer(this);
  }

  public GeneViewer getGeneViewer()
  {
    return _geneViewerController;
  }

  public void setGeneViewer(GeneViewer geneViewerController)
  {
    _geneViewerController = geneViewerController;
  }

  public CompoundViewer getCompoundViewer()
  {
    return _compoundViewer;
  }

  public void setCompoundViewer(CompoundViewer compoundViewer)
  {
    _compoundViewer = compoundViewer;
  }

  public WellSearchResults getWellSearchResults()
  {
    return _wellSearchResultsView;
  }

  public void setWellSearchResults(WellSearchResults wellSearchResultsController)
  {
    _wellSearchResultsView = wellSearchResultsController;
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
    if (_wellSearchResults == null) {
      _wellSearchResults = new edu.harvard.med.screensaver.ui.searchresults.WellSearchResults(
        new ArrayList<Well>(_library.getWells()),
        this,
        _wellViewerController,
        _compoundViewer,
        _geneViewerController);
    }
    _wellSearchResultsView.setSearchResults(_wellSearchResults);
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
