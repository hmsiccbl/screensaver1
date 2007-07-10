// $HeadURL: svn+ssh://js163@orchestra/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import edu.harvard.med.screensaver.io.DataExporter;
import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellType;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.ui.control.LibrariesController;

import org.apache.log4j.Logger;


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
  
  private LibrariesController _librariesController;
  private List<DataExporter<Well>> _dataExporters;
  
  
  // public constructor
  
  /**
   * Construct a new <code>WellSearchResultsViewer</code> object.
   * @param wells the list of wells
   */
  public WellSearchResults(
    List<Well> unsortedResults,
    LibrariesController librariesController,
    List<DataExporter<Well>> dataExporters)
  {
    super(unsortedResults);
    _librariesController = librariesController;
    _dataExporters = dataExporters;
  }

  
  // implementations of the SearchResults abstract methods
  
  @Override
  public List<DataExporter<Well>> getDataExporters()
  {
    return _dataExporters;
  }
  
  @Override
  public String showSummaryView()
  {
    // NOTE: if there were more ways to get to a well search results, then this method would
    // need to be more intelligent
    return _librariesController.viewWellSearchResults(this);
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
  protected String getColumnDescription(String columnName)
  {
    if (columnName.equals(LIBRARY)) {
      return "The library containing the well";
    }
    if (columnName.equals(PLATE)) {
      return "The number of the plate the well is located on";
    }
    if (columnName.equals(WELL)) {
      return "The plate coordinates of the well";
    }
    if (columnName.equals(WELL_TYPE)) {
      return "The type of well, e.g., 'Experimental', 'Control', 'Empty', etc.";
    }
    if (columnName.equals(CONTENTS)) {
      return "The gene name for the silencing reagent, or SMILES for the compound, in the well";
    }
    return null;
  }
  
  @Override
  protected Object getCellValue(Well well, String columnName)
  {
    if (columnName.equals(LIBRARY)) {
      return well.getLibrary().getLibraryName();
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
      return _librariesController.viewLibrary(well.getLibrary(), null);
    }
    if (columnName.equals(WELL)) {
      return _librariesController.viewWell(well, this);
    }
    if (columnName.equals(CONTENTS)) {
      if (getGeneCount(well) == 1) {
        return _librariesController.viewGene(well.getGene(), this);
      }
      if (getCompoundCount(well) > 0) {
        // commandValue is really a smiles, not a compoundId
        String smiles = (String) getRequestParameter("commandValue");
        Compound compound = null;
        for (Compound compound2 : well.getCompounds()) {
          if (compound2.getSmiles().equals(smiles)) {
            compound = compound2;
            break;
          }
        }
        return _librariesController.viewCompound(compound, this);
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
    // TODO: we should really only call the view*() method for the mode we're in; otherwise we're doing extra db work
    _librariesController.viewWell(well, this);
    // we need to call these view methods even, if the gene/compound is null, so
    // that the view can at least be updated to reflect the emptiness of the
    // well
    _librariesController.viewGene(well.getGene(), this);
    _librariesController.viewCompound(well.getPrimaryCompound(), this);
  }

  
  // private instance methods

  
  private Object getContentsValue(Well well)
  {
    int geneCount = getGeneCount(well);
    if (geneCount == 1) {
      return well.getGene().getGeneName();
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
      for (Compound compound : well.getOrderedCompounds()) {
        smiles.add(compound.getSmiles());
      }
      return smiles;
    }
    
    // at this point we know the well has no compounds or genes in it. but is it empty?
    
    if (well.getLibrary().getScreenType().equals(ScreenType.SMALL_MOLECULE) &&
      well.getWellType().equals(WellType.EXPERIMENTAL)) {
      return "compound with unknown structure";
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
}
