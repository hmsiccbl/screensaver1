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
import edu.harvard.med.screensaver.ui.control.LibrariesController;
import edu.harvard.med.screensaver.ui.searchresults.WellSearchResults;

public class WellViewer extends AbstractBackingBean
{
  
  private static final Logger log = Logger.getLogger(WellViewer.class);
  
  
  // private instance fields
  
  private LibrariesController _librariesController;
  private Well _well;
  private WellSearchResults _wellSearchResults;
  
  
  // public instance methods
  
  public LibrariesController getLibrariesController()
  {
    return _librariesController;
  }

  public void setLibrariesController(LibrariesController librariesController)
  {
    _librariesController = librariesController;
  }

  public Well getWell()
  {
    return _well;
  }

  public void setWell(Well well)
  {
    _well = well;
  }
  
  public WellSearchResults getWellSearchResults()
  {
    return _wellSearchResults;
  }

  public void setWellSearchResults(WellSearchResults searchResults)
  {
    _wellSearchResults = searchResults;
  }

  public String viewLibrary()
  {
    return _librariesController.viewLibrary(_well.getLibrary(), null);
  }
  
  public String viewGene()
  {
    String geneId = (String) getFacesContext().getExternalContext().getRequestParameterMap().get("geneId");
    Gene gene = null;
    for (Gene gene2 : _well.getGenes()) {
      if (gene2.getGeneId().equals(geneId)) {
        gene = gene2;
        break;
      }
    }
    return _librariesController.viewGene(gene, _wellSearchResults);
  }
  
  public String viewCompound()
  {
    String compoundId = (String) getRequestParameter("compoundId");
    Compound compound = null;
    for (Compound compound2 : _well.getCompounds()) {
      if (compound2.getCompoundId().equals(compoundId)) {
        compound = compound2;
        break;
      }
    }
    return _librariesController.viewCompound(compound, _wellSearchResults);
  }
}
