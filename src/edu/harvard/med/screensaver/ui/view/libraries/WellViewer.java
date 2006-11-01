// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.view.libraries;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.searchresults.SearchResults;

public class WellViewer extends AbstractBackingBean
{
  
  private static final Logger log = Logger.getLogger(WellViewer.class);
  
  
  // private instance fields
  
  private Well _well;
  private SearchResults<Well> _searchResults;
  private LibraryViewer _libraryViewerController;
  private GeneViewer _geneViewerController;
  private CompoundViewer _compoundViewerController;
  
  
  // public instance methods
  
  public WellViewer()
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
  
  public SearchResults<Well> getSearchResults()
  {
    return _searchResults;
  }

  public void setSearchResults(SearchResults<Well> searchResults)
  {
    _searchResults = searchResults;
  }

  public LibraryViewer getLibraryViewer()
  {
    return _libraryViewerController;
  }

  public void setLibraryViewer(LibraryViewer libraryViewerController)
  {
    _libraryViewerController = libraryViewerController;
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
    return _compoundViewerController;
  }

  public void setCompoundViewer(CompoundViewer compoundViewerController)
  {
    _compoundViewerController = compoundViewerController;
  }

  public String showLibrary()
  {
    _libraryViewerController.setLibrary(_well.getLibrary());
    _libraryViewerController.setSearchResults(null);
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
    _geneViewerController.setSearchResults(_searchResults);
    _geneViewerController.setGene(gene);
    return "showGene";
  }
  
  public String showCompound()
  {
    String compoundId = (String) getRequestParameter("compoundId");
    Compound compound = null;
    for (Compound compound2 : _well.getCompounds()) {
      if (compound2.getCompoundId().equals(compoundId)) {
        compound = compound2;
        break;
      }
    }
    _compoundViewerController.setCompound(compound);
    _compoundViewerController.setSearchResults(_searchResults);
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
