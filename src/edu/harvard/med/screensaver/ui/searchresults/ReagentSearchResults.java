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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.SortedSet;

import javax.faces.model.DataModel;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.ReagentsSortQuery.SortByReagentProperty;
import edu.harvard.med.screensaver.io.DataExporter;
import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screens.Study;
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
public class ReagentSearchResults extends EntitySearchResults<Reagent> implements Observer
{

  // private static final fields

  private static final Logger log = Logger.getLogger(ReagentSearchResults.class);


  // instance fields

  private AnnotationTypesTable _annotationTypesTable;
  private Map<String,Boolean> _isPanelCollapsedMap;
  private ReagentViewer _reagentViewer;
  private CompoundViewer _compoundViewer;
  private GeneViewer _geneViewer;
  private GenericEntityDAO _dao;

  private Study _study;
  private int _studyReagentCount;
  private List<TableColumn<Reagent,?>> _columns;

  private transient Well _representativeWell;



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
                              GenericEntityDAO dao,
                              List<DataExporter<Reagent>> dataExporters)
  {
    super(dataExporters);
    _annotationTypesTable = annotationTypesTable;
    _reagentViewer = reagentViewer;
    _compoundViewer = compoundViewer;
    _geneViewer = geneViewer;
    _dao = dao;

    _isPanelCollapsedMap = new HashMap<String,Boolean>();
    _isPanelCollapsedMap.put("annotationTypes", true);
    _isPanelCollapsedMap.put("reagentsData", false);

    getCapabilities().remove("filter");
  }

  public void setContents(Collection<Reagent> unsortedResults,
                          List<AnnotationType> annotationTypes)
  {
    _annotationTypesTable.initialize(annotationTypes, this);
    super.setContents(unsortedResults, null);
    _study = null;
    _studyReagentCount = 0;
  }

  public void setContents(Study study,
                          int reagentCount)
  {
    _annotationTypesTable.initialize(new ArrayList<AnnotationType>(study.getAnnotationTypes()),
                                     this);
    // HACK: we should extend a superclass that is virtual-paging savvy! (i.e., doesn't require the full contents to be provided)
    super.setContents(null, null);
    _study = study;
    _studyReagentCount = reagentCount;
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
  protected List<TableColumn<Reagent,?>> getColumns()
  {
    _columns = new ArrayList<TableColumn<Reagent,?>>();
    _columns.add(new ReagentColumn<String>(SortByReagentProperty.ID,
                                   "Reagent Source ID",
                                   "The vendor-assigned identifier for the reagent.",
                                   false) {
      @Override
      public String getCellValue(Reagent reagent) { return reagent.getEntityId().getReagentId(); }

      @Override
      public boolean isCommandLink() { return true; }

      @Override
      public Object cellAction(Reagent reagent)
      {
        return viewCurrentEntity();
      }
    });
    _columns.add(new ReagentColumn<String>(SortByReagentProperty.CONTENTS,
                                   "Contents",
                                   "The gene name for the silencing reagent, or SMILES for the compound reagent",
                                   false) {
      @Override
      public String getCellValue(Reagent reagent) { return getContentsValue(reagent).toString(); }

      @Override
      protected Comparator<Reagent> getAscendingComparator()
      {
        return new Comparator<Reagent>() {
          @SuppressWarnings("unchecked")
          public int compare(Reagent w1, Reagent w2) {
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
      public Object cellAction(Reagent reagent)
      {
        if (getGeneCount(reagent) == 1) {
          return _geneViewer.viewGene(getGenes(reagent).iterator().next());
        }
        if (getCompoundCount(reagent) > 0) {
          // commandValue is really a smiles, not a compoundId
          String smiles = (String) getRequestParameter("commandValue");
          Compound compound = null;
          for (Compound compound2 : getCompounds(reagent)) {
            if (compound2.getSmiles().equals(smiles)) {
              compound = compound2;
              break;
            }
          }
          return _compoundViewer.viewCompound(compound);
        }
        return REDISPLAY_PAGE_ACTION_RESULT;
      }
    });

    for (AnnotationType annotationType : getSelectedAnnotationTypes()) {
      _columns.add(new AnnotationTypeColumn(annotationType));
    }

    return _columns;
  }

  @Override
  protected DataModel buildDataModel()
  {
    if (_study != null) {
      return new ReagentsDataModel(_study,
                                   getDataTable().getRowsPerPage(),
                                   _studyReagentCount,
                                   getDataTable().getSortManager().getSortColumn(),
                                   getDataTable().getSortManager().getSortDirection(),
                                   _dao);
    }
    else {
      return new ReagentsDataModel(new HashSet<Reagent>(getContents()),
                                   getDataTable().getRowsPerPage(),
                                   getContents().size(),
                                   getDataTable().getSortManager().getSortColumn(),
                                   getDataTable().getSortManager().getSortDirection(),
                                   _dao);
    }
  }

  @Override
  protected void setEntityToView(Reagent reagent)
  {
    _reagentViewer.viewReagent(reagent);
  }


  // private instance methods


  private Object getContentsValue(Reagent reagent)
  {
    int geneCount = getGeneCount(reagent);
    if (geneCount == 1) {
      return getGenes(reagent).iterator().next().getGeneName();
    }
    if (geneCount > 1) {
      return "multiple genes";
    }
    int compoundCount = getCompoundCount(reagent);
    if (compoundCount == 1) {
      return getCompounds(reagent).first().getSmiles();
    }
    if (compoundCount > 1) {
      List<String> smiles = new ArrayList<String>();
      for (Compound compound : getCompounds(reagent)) {
        smiles.add(compound.getSmiles());
      }
      return smiles;
    }

    return "(reagent missing)";
  }

  private Well getRepresentativeWell(Reagent reagent)
  {
    if (_representativeWell == null) {
      _representativeWell = reagent.getWells().iterator().next();
    }
    return _representativeWell;
  }

  private SortedSet<Compound> getCompounds(Reagent reagent)
  {
    return getRepresentativeWell(reagent).getOrderedCompounds();
  }

  private Set<Gene> getGenes(Reagent reagent)
  {
    return getRepresentativeWell(reagent).getGenes();
  }

  private int getContentsCount(Reagent reagent)
  {
    return getGeneCount(reagent) + getCompoundCount(reagent);
  }

  private int getCompoundCount(Reagent reagent)
  {
    return getCompounds(reagent).size();
  }

  private int getGeneCount(Reagent reagent)
  {
    return getGenes(reagent).size();
  }
}
