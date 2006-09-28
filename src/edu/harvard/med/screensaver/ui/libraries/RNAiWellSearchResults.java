// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.Well;

/**
 * 
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class RNAiWellSearchResults extends WellSearchResults
{
  // static members

  private static Logger log = Logger.getLogger(RNAiWellSearchResults.class);
  private static final String GENE = "Gene";

  
  // instance members
  
  private GeneViewerController _geneViewerController;
  

  // public constructor
  
  /**
   * Construct a new <code>RNAiWellSearchResults</code> object.
   * @param wells the list of wells
   */
  public RNAiWellSearchResults(
    List<Well> unsortedResults,
    LibraryViewerController libraryViewerController,
    WellViewerController wellViewerController,
    GeneViewerController geneViewerController)
  {
    super(unsortedResults, libraryViewerController, wellViewerController);
    _geneViewerController = geneViewerController;
  }

  
  // protected methods


  @Override
  protected List<String> getColumnHeaders()
  {
    List<String> columnHeaders = super.getColumnHeaders();
    columnHeaders.add(GENE);
    return columnHeaders;
  }
  
  @Override
  protected boolean isCommandLink(String columnName)
  {
    return
      (columnName.equals(GENE) && getGeneCount() == 1) || super.isCommandLink(columnName);
  }
  
  @Override
  protected Object getCellValue(Well well, String columnName)
  {
    if (columnName.equals(GENE)) {
      return getGeneName(well);
    }
    return super.getCellValue(well, columnName);
  }


  @Override
  protected Object cellAction(Well well, String columnName)
  {
    if (columnName.equals(GENE)) {
      _geneViewerController.setGene(getGenesForWell(well).iterator().next());
      return "showGene";
    }
    return super.cellAction(well, columnName);
  }
  
  @Override
  protected Comparator<Well> getComparatorForColumnName(String columnName)
  {
    if (columnName.equals(GENE)) {
      return new Comparator<Well>() {
        public int compare(Well w1, Well w2) {
          return getGeneName(w1).compareTo(getGeneName(w2));
        }
      };
    }
    return super.getComparatorForColumnName(columnName);
  }

  
  // private instance methods
  
  private int getGeneCount()
  {
    return getGenesForWell(getEntity()).size();
  }
  
  private Set<Gene> getGenesForWell(Well well)
  {
    Set<Gene> genes = new HashSet<Gene>();
    for (SilencingReagent reagent : well.getSilencingReagents()) {
      genes.add(reagent.getGene());
    }
    return genes;
  }

  private String getGeneName(Well well) {
    Set<Gene> genes = getGenesForWell(well);
    int count = genes.size();
    switch (count) {
    case 0:
      return "no gene";
    case 1:
      return genes.iterator().next().getGeneName();
    default:
      return "multiple genes";  
    }
  }
}

