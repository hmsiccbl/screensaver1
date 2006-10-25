// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.ui.AbstractController;
import edu.harvard.med.screensaver.ui.SearchResultsRegistryController;

public class WellViewerController extends AbstractController
{
  
  private static final Logger log = Logger.getLogger(WellViewerController.class);
  
  
  // private instance fields
  
  private Well _well;
  private LibraryViewerController _libraryViewerController;
  private GeneViewerController _geneViewerController;
  private CompoundViewerController _compoundViewerController;
  private SearchResultsRegistryController _searchResultsRegistry;
  
  
  // public instance methods
  
  public WellViewerController()
  {
  }
  
  public Well getWell()
  {
    return _well;
  }

  public void setWell(Well well)
  {
    _well = well;
  }
  
  public LibraryViewerController getLibraryViewer()
  {
    return _libraryViewerController;
  }

  public void setLibraryViewer(LibraryViewerController libraryViewerController)
  {
    _libraryViewerController = libraryViewerController;
  }

  public SearchResultsRegistryController getSearchResultsRegistry()
  {
    return _searchResultsRegistry;
  }

  public void setSearchResultsRegistry(SearchResultsRegistryController searchResultsRegistry)
  {
    _searchResultsRegistry = searchResultsRegistry;
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

  public String showLibrary()
  {
    _libraryViewerController.setLibrary(_well.getLibrary());
    _searchResultsRegistry.setCurrentSearchType(Library.class);
    return "showLibrary";
  }
  
  public String showGene()
  {
    String geneId = (String) getFacesContext().getExternalContext().getRequestParameterMap().get("geneId");
    Gene gene = null;
    for (Gene gene2 : _well.getGenes()) {
      if (gene2.getGeneId().equals(geneId)) {
        gene = gene2;
        break;
      }
    }
    _geneViewerController.setGene(gene);
    return "showGene";
  }
  
  public String showCompound()
  {
    String compoundId = (String) getFacesContext().getExternalContext().getRequestParameterMap().get("compoundId");
    Compound compound = null;
    for (Compound compound2 : _well.getCompounds()) {
      if (compound2.getCompoundId().equals(compoundId)) {
        compound = compound2;
        break;
      }
    }
    _compoundViewerController.setCompound(compound);
    return "showCompound";
  }
  
  
  // NOTE: I turned off the Done button for the time being. Sorry! -s
  
  public boolean getDisplayDone()
  {
    return false;
  }
  
  public String done()
  {
    return DONE_ACTION_RESULT;
  }
}
