
package edu.harvard.med.lincs.screensaver.ui.libraries;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import org.apache.log4j.Logger;

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
    InMemoryEntityDataModel<Well,String,Tuple<String>> dataModel = new InMemoryEntityDataModel<Well,String,Tuple<String>>(dataFetcher);
    initialize(dataModel);
    if (!!!isNested() && _reagentsBrowser != null) {
      assert _reagentsBrowser.getReagentsBrowser() == null : "infinite recursion error in reagents browser";
      _reagentsBrowser.searchCanonicalReagentsOfWellsBrowser(this, getTitle().replaceFirst("(Reagent )?Wells", "Unique Reagents"));
    }
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
        public void addDomainRestrictions(HqlBuilder hql)
        {
          log.debug("initializing unique reagent search result");
          Set<String> distinctFacilityIds = Sets.newHashSet();
          fullWellSearchResults.getRowCount(); // force initialization
          Iterator<Tuple<String>> iter = fullWellSearchResults.getDataTableModel().iterator();
          while (iter.hasNext()) {
            // find the canonical reagent's well for the given reagent
            Well well = _dao.findEntityById(Well.class, iter.next().getKey(), true);
            distinctFacilityIds.add(well.getFacilityId());
          }
          Set<String> canonicalReagentWellIds = Sets.newHashSet();
          for (String facilityId : distinctFacilityIds) {
            Well canonicalReagentWell = _librariesDao.findCanonicalReagentWell(facilityId, null, null);
            if (canonicalReagentWell != null) {
              canonicalReagentWellIds.add(canonicalReagentWell.getWellId());
            }
          }
          DataFetcherUtil.addDomainRestrictions(hql, getRootAlias(), canonicalReagentWellIds);
        }

      };
    initialize(dataFetcher);

    // start with search panel closed
    setTableFilterMode(false);
    
    getColumnManager().setVisibilityOfColumnsInGroup("Compound Reagent Columns", false);
    getColumnManager().setVisibilityOfColumnsInGroup("Well Columns", false);
    getColumnManager().getColumn("Facility ID").setVisible(true);
      getColumnManager().getColumn("Primary Compound Name").setVisible(true);
    getColumnManager().getColumn("Compound Names").setVisible(true);
    getColumnManager().getColumn("Plate").setVisible(false);
    getColumnManager().getColumn("Well").setVisible(false);
  }
}
