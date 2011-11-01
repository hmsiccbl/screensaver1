
package edu.harvard.med.lincs.screensaver.ui.libraries;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.db.datafetcher.DataFetcher;
import edu.harvard.med.screensaver.db.datafetcher.DataFetcherUtil;
import edu.harvard.med.screensaver.db.datafetcher.Tuple;
import edu.harvard.med.screensaver.db.datafetcher.TupleDataFetcher;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.io.DataExporter;
import edu.harvard.med.screensaver.io.libraries.smallmolecule.LibraryContentsVersionReference;
import edu.harvard.med.screensaver.io.libraries.smallmolecule.StructureImageProvider;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.policy.EntityViewPolicy;
import edu.harvard.med.screensaver.ui.arch.datatable.Criterion;
import edu.harvard.med.screensaver.ui.arch.datatable.Criterion.Operator;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.model.InMemoryEntityDataModel;
import edu.harvard.med.screensaver.ui.libraries.LibraryViewer;
import edu.harvard.med.screensaver.ui.libraries.WellSearchResults;

/**
 * LINCS-specific WellSearchResults that:
 * <ul>
 * <li>Uses an InMemoryEntityDataModel, required by its use of "virtual" columns (e.g. "Facility-Salt-Batch ID")</li>
 * <li>Coordinates with another "Unique Reagents" browser (also a LincsWellSearchResults) by initializing it with data that is based upon the current search results
 * </ul>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class LincsWellSearchResults extends WellSearchResults
{
  private static final Logger log = Logger.getLogger(LincsWellSearchResults.class);

  private LincsWellSearchResults _reagentsBrowser;
  private GenericEntityDAO _dao;
  private LibrariesDAO _librariesDao;

  protected LincsWellSearchResults()
  {
    super();
  }

  public LincsWellSearchResults(GenericEntityDAO dao,
                                LibrariesDAO librariesDao,
                                EntityViewPolicy entityViewPolicy,
                                LibraryViewer libraryViewer,
                                WellViewer wellViewer,
                                StructureImageProvider structureImageProvider,
                                LibraryContentsVersionReference libraryContentsVersionRef,
                                List<DataExporter<Tuple<String>>> dataExporters)
  {
    super(dao, librariesDao, entityViewPolicy, libraryViewer, wellViewer, structureImageProvider, libraryContentsVersionRef, dataExporters);
    _dao = dao;
    _librariesDao = librariesDao;
  }

  public LincsWellSearchResults getReagentsBrowser()
  {
    return _reagentsBrowser;
  }

  public void setReagentsBrowser(LincsWellSearchResults reagentsBrowser)
  {
    _reagentsBrowser = reagentsBrowser;
  }

  @Override
  protected void initialize(DataFetcher<Tuple<String>,String,PropertyPath<Well>> dataFetcher)
  {
    InMemoryEntityDataModel<Well,String,Tuple<String>> dataModel = new InMemoryEntityDataModel<Well,String,Tuple<String>>(dataFetcher)
    {
      @Override
      public void filter(List<? extends TableColumn<Tuple<String>,?>> columns)
      {
        if(((Collection)getWrappedData()).isEmpty())
        {
          refetch();
        }
        super.filter(columns);
      }
    };
    
    initialize(dataModel);
    if (!!!isNested() && _reagentsBrowser != null ) {
      assert _reagentsBrowser.getReagentsBrowser() == null : "infinite recursion error in reagents browser";
      _reagentsBrowser.searchCanonicalReagentsOfWellsBrowser(this, getTitle().replaceFirst("(Reagent )?Wells", "Unique Reagents"));
    }
  }
  
    @Override
  public void searchAll()
  {
    setTitle("Wells Search Result");
    setMode(WellSearchResultMode.SET_OF_CANONICAL_REAGENT_WELLS);
    // initially, show an empty search result, but with all columns available
    TupleDataFetcher<Well,String> dataFetcher = new TupleDataFetcher<Well,String>(Well.class, _dao)
    {
      @Override
      public List<Tuple<String>> fetchAllData()
      {
        //log.info("fetchAllData: " + hasCriteriaDefined(getCriterion(getColumnManager().getAllColumns())) );
        if (!hasCriteriaDefined(getCriterion(getColumnManager().getAllColumns()))) { 
          return Collections.emptyList();
        }
        return super.fetchAllData();
      }
      
      private List<? extends Criterion<?>> getCriterion(List<TableColumn<Tuple<String>,?>> columns)
      {
        List<Criterion<?>> criterion = Lists.newArrayList();
        for(TableColumn<Tuple<String>,?> column:columns) {
          criterion.add(column.getCriterion());
        }
        return criterion;
      }
      private boolean hasCriteriaDefined(List<? extends Criterion<?>> criteria)
      {
        for (Criterion<?> c : criteria) {
            if (!c.isUndefined()) {
              return true;
            }
        }
        return false;
      }
        @Override
        public void addDomainRestrictions(final HqlBuilder hql)
        {
          hql.from(getRootAlias(), Well.reagents, "r").where("r", "vendorId.vendorIdentifier", Operator.NOT_EMPTY, null);
        }
        
    };
    initialize(dataFetcher);

    // start with search panel open
    setTableFilterMode(true);
    adjustVisibleColumnsForLincs();
  }
  
  public void searchCanonicalReagentsOfWellsBrowser(final WellSearchResults fullWellSearchResults,
                                                    String title)
  {
    setTitle(title);
    setMode(WellSearchResultMode.SET_OF_CANONICAL_REAGENT_WELLS);
    //_screenTypes = _librariesDao.findScreenTypesForWells(wellKeys);
    TupleDataFetcher<Well,String> dataFetcher =
      new TupleDataFetcher<Well,String>(Well.class, _dao) {
        @Override
        public void addDomainRestrictions(final HqlBuilder hql)
        {
          _dao.doInTransaction(new DAOTransaction() {

            @Override
            public void runTransaction()
            {
              log.debug("initializing unique reagent search result");
              fullWellSearchResults.getRowCount(); // force initialization
              Iterable<String> fullReagentWellIds = Iterables.transform(fullWellSearchResults.getDataTableModel(), Tuple.<String>toKey());
              Set<String> canonicalReagentWellIds = _librariesDao.findCanonicalReagentWellIds(Sets.newHashSet(fullReagentWellIds));
              DataFetcherUtil.addDomainRestrictions(hql, getRootAlias(), canonicalReagentWellIds);
            }
          });
        }
      };
    initialize(dataFetcher);

    // start with search panel closed
    setTableFilterMode(false);
    adjustVisibleColumnsForLincs();
  }

  private void adjustVisibleColumnsForLincs()
  {
    getColumnManager().setVisibilityOfColumnsInGroup("Compound Reagent Columns", false);
    getColumnManager().setVisibilityOfColumnsInGroup("Well Columns", false);
    getColumnManager().getColumn("Facility ID").setVisible(true);
    getColumnManager().getColumn("Primary Compound Name").setVisible(true);
    getColumnManager().getColumn("Compound Names").setVisible(true);
    getColumnManager().getColumn("Plate").setVisible(false);
    getColumnManager().getColumn("Well").setVisible(false);
  }

  /**
   * For LINCS, it does not make sense to show study annotation data in Well Search Results, as these data only apply to
   * a single compound, and so these data are better viewed in the Study Viewer itself
   */
  protected void buildAnnotationTypeColumns(List<TableColumn<Tuple<String>,?>> columns)
  {
    return;
  }

}
