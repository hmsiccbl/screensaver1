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
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Well;

/**
 * 
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class CompoundWellSearchResults extends WellSearchResults
{
  
  // static members

  private static Logger log = Logger.getLogger(CompoundWellSearchResults.class);
  private static final String COMPOUND = "Compound";

  
  // instance members
  
  private CompoundViewerController _compoundViewerController;
  

  // public constructor
  
  /**
   * Construct a new <code>RNAiWellSearchResults</code> object.
   * @param wells the list of wells
   */
  public CompoundWellSearchResults(
    List<Well> unsortedResults,
    LibraryViewerController libraryViewerController,
    WellViewerController wellViewerController,
    CompoundViewerController compoundViewerController)
  {
    super(unsortedResults, libraryViewerController, wellViewerController);
    _compoundViewerController = compoundViewerController;
  }

  
  // protected methods


  @Override
  protected List<String> getColumnHeaders()
  {
    List<String> columnHeaders = super.getColumnHeaders();
    columnHeaders.add(COMPOUND);
    return columnHeaders;
  }
  
  @Override
  protected boolean isCommandLink(String columnName)
  {
    return
      (columnName.equals(COMPOUND) && getCompoundCount() == 1) || super.isCommandLink(columnName);
  }
  
  @Override
  protected Object getCellValue(Well well, String columnName)
  {
    if (columnName.equals(COMPOUND)) {
      return getCompoundSmiles(well);
    }
    return super.getCellValue(well, columnName);
  }


  @Override
  protected Object cellAction(Well well, String columnName)
  {
    if (columnName.equals(COMPOUND)) {
      _compoundViewerController.setCompound(getCompoundsForWell(well).iterator().next());
      return "showCompound";
    }
    return super.cellAction(well, columnName);
  }
  
  @Override
  protected Comparator<Well> getComparatorForColumnName(String columnName)
  {
    if (columnName.equals(COMPOUND)) {
      return new Comparator<Well>() {
        public int compare(Well w1, Well w2) {
          return getCompoundSmiles(w1).compareTo(getCompoundSmiles(w2));
        }
      };
    }
    return super.getComparatorForColumnName(columnName);
  }

  
  // private instance methods
  
  private int getCompoundCount()
  {
    return getCompoundsForWell(getEntity()).size();
  }
  
  private Set<Compound> getCompoundsForWell(Well well)
  {
    return well.getCompounds();
  }

  private String getCompoundSmiles(Well well) {
    Set<Compound> compounds = getCompoundsForWell(well);
    int count = compounds.size();
    switch (count) {
    case 0:
      return "no compound";
    case 1:
      return compounds.iterator().next().getSmiles();
    default:
      return "multiple compounds";  
    }
  }
  
  @Override
  protected void setEntityToView(Well well)
  {
    _compoundViewerController.setCompound(getCompoundsForWell(well).iterator().next());
    super.setEntityToView(well);
  }
}

