// $HeadURL: svn+ssh://js163@orchestra/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.ui.SearchResults;


/**
 * A {@link SearchResults} for {@link Well Wells}.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class WellSearchResults extends SearchResults<Well>
{
  
  // private static final fields
  
  private static final Logger log = Logger.getLogger(WellSearchResults.class);

  private static final String LIBRARY   = "Library";
  private static final String PLATE     = "Plate";
  private static final String WELL      = "Well";
  private static final String WELL_TYPE = "Well Type";
  private static final String CONTENTS  = "Contents";
  
  
  // instance fields
  
  private LibraryViewerController _libraryViewerController;
  private WellViewerController _wellViewerController;
  private CompoundViewerController _compoundViewerController;
  private GeneViewerController _geneViewerController;
  
  
  // public constructor
  
  /**
   * Construct a new <code>WellSearchResults</code> object.
   * @param wells the list of wells
   */
  public WellSearchResults(
    List<Well> unsortedResults,
    LibraryViewerController libraryViewerController,
    WellViewerController wellViewerController,
    CompoundViewerController compoundViewerController,
    GeneViewerController geneViewerController)
  {
    super(unsortedResults);
    _libraryViewerController = libraryViewerController;
    _wellViewerController = wellViewerController;
    _compoundViewerController = compoundViewerController;
    _geneViewerController = geneViewerController;
  }

  
  // implementations of the SearchResults abstract methods
  
  @Override
  public String showSummaryView()
  {
    // NOTE: if there were more ways to get to a well search results, then this method would
    // need to be more intelligent
    
    // TODO: may want to initialize the screens browser here as well, eg,
    // "return _libraryViewer.viewLibraryContents();", but i would like to wait until control is
    // factored out
    
    return "goWellSearchResults";
  }
  
  @Override
  protected List<String> getColumnHeaders()
  {
    List<String> columnHeaders = new ArrayList<String>();
    columnHeaders.add(LIBRARY);
    columnHeaders.add(PLATE);
    columnHeaders.add(WELL);
    columnHeaders.add(WELL_TYPE);
    columnHeaders.add(CONTENTS);
    return columnHeaders;
  }
  
  @Override
  protected boolean isCommandLink(String columnName)
  {
    return
      columnName.equals(LIBRARY) ||
      columnName.equals(WELL) ||
      (columnName.equals(CONTENTS) && getContentsCount(getEntity()) == 1);
  }
  
  @Override
  protected boolean isCommandLinkList(String columnName)
  {
    return columnName.equals(CONTENTS) && getContentsCount(getEntity()) > 1;
  }
  
  @Override
  protected Object getCellValue(Well well, String columnName)
  {
    if (columnName.equals(LIBRARY)) {
      return well.getLibrary().getShortName();
    }
    if (columnName.equals(PLATE)) {
      return well.getPlateNumber();
    }
    if (columnName.equals(WELL)) {
      return well.getWellName();
    }
    if (columnName.equals(WELL_TYPE)) {
      return well.getWellType();
    }
    if (columnName.equals(CONTENTS)) {
      return getContentsValue(well);
    }
    return null;
  }
  
  @Override
  protected Object cellAction(Well well, String columnName)
  {
    if (columnName.equals(LIBRARY)) {
      _libraryViewerController.setSearchResults(null);
      _libraryViewerController.setLibrary(well.getLibrary());
      return "showLibrary";
    }
    if (columnName.equals(WELL)) {
      _wellViewerController.setSearchResults(this);
      _wellViewerController.setWell(well);
      return "showWell";
    }
    if (columnName.equals(CONTENTS)) {
      if (getGeneCount(well) == 1) {
        _geneViewerController.setSearchResults(this);
        _geneViewerController.setGene(getGeneForWell(well));
        return "showGene";
      }
      if (getCompoundCount(well) > 0) {
        String compoundId = (String) getRequestParameter("commandValue");
        Compound compound = null;
        for (Compound compound2 : well.getCompounds()) {
          if (compound2.getCompoundId().equals(compoundId)) {
            compound = compound2;
            break;
          }
        }
        _compoundViewerController.setSearchResults(this);
        _compoundViewerController.setCompound(compound);
        return "showCompound";
      }
    }
    return null;
  }
  
  @Override
  protected Comparator<Well> getComparatorForColumnName(String columnName)
  {
    if (columnName.equals(LIBRARY)) {
      return new Comparator<Well>() {
        public int compare(Well w1, Well w2) {
          return w1.getLibrary().getShortName().compareTo(w2.getLibrary().getShortName());
        }
      };
    }
    if (columnName.equals(PLATE)) {
      return new Comparator<Well>() {
        public int compare(Well w1, Well w2) {
          return w1.getPlateNumber().compareTo(w2.getPlateNumber());
        }
      };
    }
    if (columnName.equals(WELL)) {
      return new Comparator<Well>() {
        public int compare(Well w1, Well w2) {
          return w1.getWellName().compareTo(w2.getWellName());
        }
      };
    }
    if (columnName.equals(WELL_TYPE)) {
      return new Comparator<Well>() {
        public int compare(Well w1, Well w2) {
          return w1.getWellType().compareTo(w2.getWellType());
        }
      };
    }
    if (columnName.equals(CONTENTS)) {
      return new Comparator<Well>() {
        @SuppressWarnings("unchecked")
        public int compare(Well w1, Well w2) {
          Object o1 = getContentsValue(w1);
          String s1 = (o1 instanceof String) ?
            (String) o1 : ((List<String>) o1).get(0);
          Object o2 = getContentsValue(w2);
          String s2 = (o2 instanceof String) ?
            (String) o2 : ((List<String>) o2).get(0);
          return s1.compareTo(s2);
        }
      };
    }
    return null;
  }

  @Override
  protected void setEntityToView(Well well)
  {
    // NOTE the hidden bugg: scrolling through the compounds/genes for a well search
    // results will only give the first gene in a well; others will be skipped.
    // this should never be a problem since there should really only be a single
    // gene in a well.
    _geneViewerController.setGene(getGeneForWell(well));
    _compoundViewerController.setCompound(getCompoundWithLongestSmiles(well));
    _wellViewerController.setWell(well);
  }

  
  // private instance methods
  
  private Object getContentsValue(Well well)
  {
    int geneCount = getGeneCount(well);
    if (geneCount == 1) {
      return getGeneForWell(well).getGeneName();
    }
    if (geneCount > 1) {
      return "multiple genes";
    }
    int compoundCount = getCompoundCount(well);
    if (compoundCount == 1) {
      return well.getCompounds().iterator().next().getSmiles();
    }
    if (compoundCount > 1) {
      List<String> smiles = new ArrayList<String>();
      for (Compound compound : well.getCompounds()) {
        smiles.add(compound.getSmiles());
      }
      return smiles;
    }
    return "empty well";
  }
  
  private int getContentsCount(Well well)
  {
    return getGeneCount(well) + getCompoundCount(well);
  }
  
  private int getCompoundCount(Well well)
  {
    return well.getCompounds().size();
  }

  private int getGeneCount(Well well)
  {
    return well.getGenes().size();
  }
  
  private Compound getCompoundWithLongestSmiles(Well well)
  {
    Compound compoundWithLongestSmiles = null;
    for (Compound compound : well.getCompounds()) {
      if (
        compoundWithLongestSmiles == null ||
        compound.getSmiles().length() > compoundWithLongestSmiles.getSmiles().length()) {
        compoundWithLongestSmiles = compound;
      }
    }
    return compoundWithLongestSmiles;
  }

  private Gene getGeneForWell(Well well)
  {
    Set<Gene> genes = well.getGenes();
    if (genes.size() == 0) {
      return null;
    }
    return genes.iterator().next();
  }
}
