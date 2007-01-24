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
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.namevaluetable.GeneNameValueTable;
import edu.harvard.med.screensaver.ui.searchresults.WellSearchResults;

public class GeneViewer extends AbstractBackingBean
{
  
  // private instance fields
  
  private Gene _gene;
  private WellSearchResults _wellSearchResults;
  private GeneNameValueTable _geneNameValueTable;

  
  // public instance methods

  public Gene getGene()
  {
    return _gene;
  }

  public void setGene(Gene gene)
  {
    _gene = gene;
  }

  public WellSearchResults getWellSearchResults()
  {
    return _wellSearchResults;
  }

  public void setWellSearchResults(WellSearchResults wellSearchResults)
  {
    _wellSearchResults = wellSearchResults;
  }

  public GeneNameValueTable getGeneNameValueTable()
  {
    return _geneNameValueTable;
  }

  public void setGeneNameValueTable(GeneNameValueTable geneNameValueTable)
  {
    _geneNameValueTable = geneNameValueTable;
  }
}
