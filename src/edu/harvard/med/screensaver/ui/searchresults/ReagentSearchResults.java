//$HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/ui/searchresults/WellSearchResults.java $
//$Id: WellSearchResults.java 1775 2007-09-05 15:43:44Z s $

//Copyright 2006 by the President and Fellows of Harvard College.

//Screensaver is an open-source project developed by the ICCB-L and NSRB labs
//at Harvard Medical School. This software is distributed under the terms of
//the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import edu.harvard.med.screensaver.db.AnnotationsDAO;
import edu.harvard.med.screensaver.io.DataExporter;
import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellType;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.ui.annotations.AnnotationTypesTable;
import edu.harvard.med.screensaver.ui.libraries.CompoundViewer;
import edu.harvard.med.screensaver.ui.libraries.GeneViewer;
import edu.harvard.med.screensaver.ui.libraries.ReagentViewer;
import edu.harvard.med.screensaver.ui.table.TableColumn;
import edu.harvard.med.screensaver.ui.util.UISelectManyBean;

import org.apache.log4j.Logger;


/**
 * A {@link SearchResults} for {@link Well Wells}. Provides user-selectable
 * annotation type columns.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class ReagentSearchResults extends EntitySearchResults<Well> implements Observer
{

  // private static final fields

  private static final Logger log = Logger.getLogger(ReagentSearchResults.class);


  // instance fields

  private AnnotationTypesTable _annotationTypesTable;
  private Map<String,Boolean> _isPanelCollapsedMap;
  private ReagentViewer _reagentViewer;
  private CompoundViewer _compoundViewer;
  private GeneViewer _geneViewer;
  private AnnotationsDAO _annotationsDao;

  private List<TableColumn<Well>> _columns;
  private Map<ReagentVendorIdentifier,List<AnnotationValue>> _annotationValues;


  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected ReagentSearchResults()
  {
  }

  /**
   * Construct a new <code>WellSearchResultsViewer</code> object.
   */
  public ReagentSearchResults(AnnotationTypesTable annotationTypesTable,
                              ReagentViewer reagentViewer,
                              CompoundViewer compoundViewer,
                              GeneViewer geneViewer,
                              AnnotationsDAO annotationsDao,
                              List<DataExporter<Well>> dataExporters)
  {
    super(dataExporters);
    _annotationTypesTable = annotationTypesTable;
    _reagentViewer = reagentViewer;
    _compoundViewer = compoundViewer;
    _geneViewer = geneViewer;
    _annotationsDao = annotationsDao;

    _isPanelCollapsedMap = new HashMap<String,Boolean>();
    // HACK: the "annotationTypes" panel must be expanded initially, if all metadata type selections are to be selected on initialization (MetaDataTable.initialize())
    _isPanelCollapsedMap.put("annotationTypes", false);
    _isPanelCollapsedMap.put("annotationValues", true);

  }

  public void setContents(Collection<Well> unsortedResults,
                          String description,
                          List<AnnotationType> annotationTypes)
  {
    _annotationTypesTable.initialize(annotationTypes, this);
    List<ReagentVendorIdentifier> rvids = new ArrayList<ReagentVendorIdentifier>(unsortedResults.size());
    for (Well well : unsortedResults) {
      rvids.add(well.getReagentVendorIdentifier());
    }
    _annotationValues = _annotationsDao.findAnnotationValues(rvids, annotationTypes);

    super.setContents(unsortedResults, description);
  }

  public void update(Observable observable, Object selections)
  {
    log.debug("annotation type selection changed");
    getDataTable().rebuildColumnsAndRows();
  }

  public UISelectManyBean<AnnotationType> getAnnotationTypeSelector()
  {
    return getAnnotationTypesTable().getSelector();
  }

  public List<AnnotationType> getSelectedAnnotationTypes()
  {
    return getAnnotationTypesTable().getSelector().getSelections();
  }

  public Map<?,?> getIsPanelCollapsedMap()
  {
    return _isPanelCollapsedMap;
  }

  public AnnotationTypesTable getAnnotationTypesTable()
  {
    return _annotationTypesTable;
  }


  // implementations of the SearchResults abstract methods

  @Override
  protected List<TableColumn<Well>> getColumns()
  {
    _columns = new ArrayList<TableColumn<Well>>();
    _columns.add(new TableColumn<Well>("Reagent Source ID", "The vendor-assigned identifier for the reagent.") {
      @Override
      public Object getCellValue(Well well) { return well.getFullVendorIdentifier(); }

      @Override
      public boolean isCommandLink() { return true; }

      @Override
      public Object cellAction(Well well)
      {
        return viewCurrentEntity();
      }
    });
    _columns.add(new TableColumn<Well>("Contents", "The gene name for the silencing reagent, or SMILES for the compound reagent") {
      @Override
      public Object getCellValue(Well well) { return getContentsValue(well); }

      @Override
      protected Comparator<Well> getAscendingComparator()
      {
        return new Comparator<Well>() {
          @SuppressWarnings("unchecked")
          public int compare(Well w1, Well w2) {
            Object o1 = getContentsValue(w1);
            String s1 = (o1 instanceof String) ? (String) o1 : ((List<String>) o1).get(0);
            Object o2 = getContentsValue(w2);
            String s2 = (o2 instanceof String) ? (String) o2 : ((List<String>) o2).get(0);
            return s1.compareTo(s2);
          }
        };
      }

      @Override
      public boolean isCommandLink() { return getContentsCount(getRowData()) == 1; }

      @Override
      public boolean isCommandLinkList() { return getContentsCount(getRowData()) > 1; }

      @Override
      public Object cellAction(Well well)
      {
        if (getGeneCount(well) == 1) {
          return _geneViewer.viewGene(well.getGene(), well);
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
          return _compoundViewer.viewCompound(compound, well);
        }
        return REDISPLAY_PAGE_ACTION_RESULT;
      }
    });

    int i = 0;
    for (AnnotationType annotationType : getSelectedAnnotationTypes()) {
      _columns.add(new AnnotationTypeColumn(annotationType, i++));
    }

    return _columns;
  }

  private class AnnotationTypeColumn extends TableColumn<Well>
  {
    private AnnotationType _annotationType;
    private int _index;

    public AnnotationTypeColumn(AnnotationType annotationType, int index)
    {
      super(annotationType.getName(),
            annotationType.getDescription(),
            annotationType.isNumeric());
      _annotationType = annotationType;
      _index = index;
    }

    @Override
    public Object getCellValue(Well well)
    {
      List<AnnotationValue> annotationValues = _annotationValues.get(well.getReagentVendorIdentifier());
      if (annotationValues == null) {
        return null;
      }
      return annotationValues.get(_index).getValue();
    }
  }

  @Override
  protected void setEntityToView(Well well)
  {
    _reagentViewer.viewReagent(well);
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
