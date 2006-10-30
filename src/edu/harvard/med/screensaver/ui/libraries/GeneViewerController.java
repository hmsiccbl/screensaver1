// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.ui.AbstractController;
import edu.harvard.med.screensaver.ui.SearchResults;

public class GeneViewerController extends AbstractController
{
  private Gene _gene;
  private SearchResults<Well> _searchResults;
  
  public Gene getGene()
  {
    return _gene;
  }

  public void setGene(Gene gene)
  {
    this._gene = gene;
  }
  
  public SearchResults<Well> getSearchResults()
  {
    return _searchResults;
  }

  public void setSearchResults(SearchResults<Well> searchResults)
  {
    _searchResults = searchResults;
  }

  public String showGene()
  {
    return "showGene";
  }
}
