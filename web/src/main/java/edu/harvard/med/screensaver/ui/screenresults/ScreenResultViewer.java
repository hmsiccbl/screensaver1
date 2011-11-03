//$HeadURL$
// $Id$
//
//Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
//Screensaver is an open-source project developed by the ICCB-L and NSRB labs
//at Harvard Medical School. This software is distributed under the terms of
//the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.db.Criterion;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.HqlBuilderCallback;
import edu.harvard.med.screensaver.db.ScreenResultsDAO;
import edu.harvard.med.screensaver.db.Criterion.Operator;
import edu.harvard.med.screensaver.db.datafetcher.PropertyPathDataFetcher;
import edu.harvard.med.screensaver.db.datafetcher.Tuple;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.AssayReadoutType;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.service.screenresult.ScreenResultDeleter;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.FetchPaths;
import edu.harvard.med.screensaver.ui.arch.view.EntityViewerBackingBean;
import edu.harvard.med.screensaver.ui.arch.view.aspects.UICommand;
import edu.harvard.med.screensaver.ui.libraries.WellSearchResults;
import edu.harvard.med.screensaver.ui.screens.ScreenViewer;


/**
 * JSF backing bean for Screen Result Viewer web page (screenresultviewer.jsp).
 * <p>
 * The <code>screenResult</code> property should be set to the {@link ScreenResult} that is to be viewed.<br>
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
@SuppressWarnings("unchecked")
public class ScreenResultViewer extends EntityViewerBackingBean<ScreenResult>
{
  private static Logger log = Logger.getLogger(ScreenResultViewer.class);

  private ScreenResultsDAO _screenResultsDao;
  private ScreenResultDeleter _screenResultDeleter;
  private ScreenViewer _screenViewer;
  private ScreenResultDataColumnsTables _screenResultDataColumnsTables;
  private WellSearchResults _wellSearchResults;

  private boolean _isWellSearchResultsInitialized;

  private boolean _isFilterPositives = false;
  private boolean _isShowMutualColumns = false;

  private DataModel _positivesDataColumnsModel;

  /**
   * @motivation for CGLIB2
   */
  protected ScreenResultViewer()
  {}

  public ScreenResultViewer(ScreenResultViewer thisProxy,
                            GenericEntityDAO dao,
                            ScreenResultsDAO screenResultsDao,
                            ScreenResultDeleter screenResultDeleter,
                            ScreenViewer screenViewer,
                            ScreenResultDataColumnsTables screenResultDataColumnsTables,
                            WellSearchResults wellSearchResults)

  {
    super(thisProxy,
          ScreenResult.class,
          EDIT_SCREEN_RESULT,
          dao);
    _screenResultsDao = screenResultsDao;
    _screenResultDeleter = screenResultDeleter;
    _screenViewer = screenViewer;
    _screenResultDataColumnsTables = screenResultDataColumnsTables;
    _wellSearchResults = wellSearchResults;

    getIsPanelCollapsedMap().put("screeningSummary", false);
    getIsPanelCollapsedMap().put("dataColumnsTable", true);
    getIsPanelCollapsedMap().put("dataTable", true);
    getIsPanelCollapsedMap().put("heatMaps", true);
    getIsPanelCollapsedMap().put("cellHTS2", true);
  }
  
  @Override
  protected void initializeEntity(ScreenResult entity)
  {}

  @Override
  protected void initializeViewer(ScreenResult screenResult)
  {
    // lazy initialization of _wellSearchResults, for performance (avoid expense of determining columns, if not being viewed)
    _isWellSearchResultsInitialized = false;
    _isFilterPositives = false;
    _isShowMutualColumns = false;
    _positivesDataColumnsModel = null;

    // open viewer with dataTable panel closed, to avoid expense of initializing unless user explicitly requests to view it, while scrolling through multiple screen results
    getIsPanelCollapsedMap().put("dataTable", true);
    getIsPanelCollapsedMap().put("heatMaps", true);

    _screenResultDataColumnsTables.initialize(screenResult.getDataColumnsList());
  }

  /**
   * @return the assay readout types, as a formatted, comma-delimited string; "<none>" if no assay readout types
   */
  public String getAssayReadoutTypesText()
  {
    Set<AssayReadoutType> assayReadoutTypes;
    if (getEntity() == null) {
      assayReadoutTypes = Sets.newHashSet();
    }
    else {
      assayReadoutTypes = getEntity().getAssayReadoutTypes();
      if (assayReadoutTypes.isEmpty()) {
        return "<none>";
      }
    }
    return Joiner.on(", ").join(assayReadoutTypes);
  }

  public ScreenResultDataColumnsTables getDataColumnsTable()
  {
    return _screenResultDataColumnsTables;
  }

  public DataModel getPartitionedPositivesDataColumnsModel()
  {
    if (_positivesDataColumnsModel == null && getEntity() != null) {
      _positivesDataColumnsModel = new ListDataModel(getEntity().getPartitionedPositivesDataColumns());
    }
    return _positivesDataColumnsModel;
  }

  public WellSearchResults getWellSearchResults()
  {
      // lazy initialization of _wellSearchResults, for performance (avoid expense of determining columns, if not being viewed)
    if (!_isWellSearchResultsInitialized && !getIsPanelCollapsedMap().get("dataTable")) {
      _wellSearchResults.searchWellsForScreenResult(getEntity(), _isFilterPositives);
      _isWellSearchResultsInitialized = true;
      if (_isShowMutualColumns) {
        showMutualColumnsCommand();
      }
    }
    return _wellSearchResults;
  }
  

  // JSF application methods
  
  @UICommand
  public String delete()
  {
    if (getEntity() != null) {
      _screenResultDeleter.deleteScreenResult(getEntity(), 
                                              (AdministratorUser) getScreensaverUser());
     
      showMessage("deletedEntity", "screen result");
      
      return _screenViewer.reload();
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  public boolean isFilterPositives()
  {
    return _isFilterPositives;
  }

  public void setFilterPositives(boolean value)
  {
    _isFilterPositives = value;
  }

  public void toggleFilterPositives(ValueChangeEvent event)
  {
    Boolean showFilterPositives = (Boolean) event.getNewValue();
    if (showFilterPositives.booleanValue() != _isFilterPositives) {
      setFilterPositives(showFilterPositives);
    }
  }

  @UICommand
  public String filterPositivesCommand()
  {
    _isWellSearchResultsInitialized = false;
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  public boolean isShowMutualColumns()
  {
    return _isShowMutualColumns;
  }

  public void setShowMutualColumns(boolean value)
  {
    _isShowMutualColumns = value;
  }

  public void toggleShowMutualColumns(ValueChangeEvent event)
  {
    Boolean showMutualColumns = (Boolean) event.getNewValue();
    if (showMutualColumns.booleanValue() != _isShowMutualColumns) {
      setShowMutualColumns(showMutualColumns);
    }
  }

  @UICommand
  @Transactional
  public String showMutualColumnsCommand()
  {
    final ScreenResult screenResult = getEntity();
    String crossScreenCountColumnName = "";
    if (screenResult.getScreen().getScreenType() == ScreenType.SMALL_MOLECULE) {
      crossScreenCountColumnName = "[" + ScreensaverConstants.DEFAULT_BATCH_STUDY_ID_POSITIVE_COUNT_SM + "]";
    }
    else {
      crossScreenCountColumnName = "[" + ScreensaverConstants.DEFAULT_BATCH_STUDY_ID_POSITIVE_COUNT_RNAI + "]";
    }

    // TODO: must refactor to make this code unit-testable
    HqlBuilderCallback hqlBuilderCallback = new HqlBuilderCallback() {
      @Override
      public void apply(HqlBuilder hql)
      {
        hql.
        where("aw1", "screenResult", Operator.EQUAL, screenResult).
          where("aw2", "screenResult", Operator.NOT_EQUAL, screenResult); // ignore data columns from the screen result passed in
        Map<PropertyPath<Well>,List<? extends Criterion<?>>> criteria =
          FetchPaths.<Well,Tuple<String>>getFilteringCriteria(getWellSearchResults().getColumnManager().getVisibleColumns());
        PropertyPathDataFetcher.addFilteringRestrictions(hql, Maps.<RelationshipPath<Well>,String>newHashMap(), criteria);
      }
    };

    for (DataColumn mutualPositivesDataColumn : _screenResultsDao.findMutualPositiveColumns(hqlBuilderCallback)) {
      String mutualPositivesColumnName =
        WellSearchResults.makeColumnName(mutualPositivesDataColumn,
                                         getDao().reloadEntity(mutualPositivesDataColumn,
                                                               true,
                                                               DataColumn.ScreenResult.to(ScreenResult.screen)).getScreenResult().getScreen().getFacilityId());

      boolean found = false;
      for (TableColumn tc : getWellSearchResults().getColumnManager().getAllColumns()) {
        if (tc.getName().equals(mutualPositivesColumnName)) {
          /*
           * TODO: this will cause the column to be made invisible if the user has selected on their own and then they
           * check/and/uncheck the filterPositives command, we would need to maintain the set of _user_ selected columns
           * separately in order to do remember their selections; in this case, the "show mutual" button just erases the
           * users choices once it is unchecked.
           */
          tc.setVisible(_isShowMutualColumns);
          found = true;
          continue;
        }
        if (tc.getName().contains(crossScreenCountColumnName)) {
          tc.setVisible(_isShowMutualColumns);
        }
      }
      if (!found) {
        log.warn("Mutual positives data column not found in data table: " + mutualPositivesColumnName);
      }
    }

    return REDISPLAY_PAGE_ACTION_RESULT;
  }
}
