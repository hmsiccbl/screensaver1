// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.activities;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.datafetcher.DataFetcherUtil;
import edu.harvard.med.screensaver.db.datafetcher.EntityDataFetcher;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickAssayPlate;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickLiquidTransfer;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.screens.CherryPickScreening;
import edu.harvard.med.screensaver.model.screens.LabActivity;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.IntegerEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.RelatedEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.model.InMemoryEntityDataModel;
import edu.harvard.med.screensaver.ui.cherrypickrequests.CherryPickRequestViewer;
import edu.harvard.med.screensaver.ui.screens.ScreenSearchResults;
import edu.harvard.med.screensaver.ui.screens.ScreenViewer;
import edu.harvard.med.screensaver.ui.users.UserViewer;

public class LabActivitySearchResults extends ActivitySearchResults<LabActivity>
{
  // static members

  private static final Set<String> ACTIVITY_TYPES = new HashSet<String>();
  protected static final String SCREEEN_COLUMN_GROUP = "Screen";
  
  static {
    ACTIVITY_TYPES.add(CherryPickLiquidTransfer.ACTIVITY_TYPE_NAME);
    ACTIVITY_TYPES.add(LibraryScreening.ACTIVITY_TYPE_NAME);
    ACTIVITY_TYPES.add(CherryPickScreening.ACTIVITY_TYPE_NAME);;
  }

  private static Logger log = Logger.getLogger(LabActivitySearchResults.class);
  private ScreenViewer _screenViewer;
  private CherryPickRequestViewer _cprViewer;
  private ScreenSearchResults _screenSearchResults;

  /**
   * @motivation for CGLIB2
   */
  protected LabActivitySearchResults()
  {
  }

  public LabActivitySearchResults(LabActivityViewer labActivityViewer,
                                  ScreenViewer screenViewer,
                                  CherryPickRequestViewer cprViewer,
                                  UserViewer userViewer,
                                  ScreenSearchResults screenSearchResults,
                                  GenericEntityDAO dao)
  {
    super(labActivityViewer, LabActivity.class, dao, userViewer);
    _cprViewer = cprViewer;
    _screenSearchResults = screenSearchResults;
  }

  public void searchLabActivitiesForScreen(final Screen screen)
  {
    setTitle("Lab Activities for screen " + screen.getFacilityId());
    initialize(new InMemoryEntityDataModel<LabActivity,Integer>(new EntityDataFetcher<LabActivity,Integer>(LabActivity.class, _dao) {
      @Override
      public void addDomainRestrictions(HqlBuilder hql)
      {
        DataFetcherUtil.addDomainRestrictions(hql, LabActivity.Screen, screen, getRootAlias());
      }
    }));
  }

  @SuppressWarnings("unchecked")
  @Override
  protected List<? extends TableColumn<LabActivity,?>> buildColumns()
  {
    List<TableColumn<LabActivity,?>> columns =
      (List<TableColumn<LabActivity,?>>) super.buildColumns();
    
    IntegerEntityColumn<LabActivity> column =
      new IntegerEntityColumn<LabActivity>(RelationshipPath.from(LabActivity.class).toProperty("screenedExperimentalWellCount"),
                                           "Experimental Wells Screened (Library Screening)",
                                           "The number of experimental library wells that were screened during this activity (ignoring replicates)",
                                           TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(LabActivity activity) 
      { 
        if (activity instanceof LibraryScreening) {
          return ((LibraryScreening) activity).getScreenedExperimentalWellCount();
        }
        return null;
      }
    };
    column.setVisible(false);
    columns.add(2, column);

    column = new IntegerEntityColumn<LabActivity>(CherryPickScreening.cherryPickRequest.castToSupertype(LabActivity.class),
      "Cherry Pick Request #", "The cherry pick request number, if applicable", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(LabActivity activity) 
      { 
        CherryPickRequest cherryPickRequest = getCherryPickRequest(activity);
        return cherryPickRequest == null ? null : cherryPickRequest.getCherryPickRequestNumber();
      }

      private CherryPickRequest getCherryPickRequest(LabActivity activity)
      {
        if (activity instanceof CherryPickLiquidTransfer) {
          return ((CherryPickLiquidTransfer) activity).getCherryPickRequest();
        }
        else if (activity instanceof CherryPickScreening) {
          return ((CherryPickScreening) activity).getCherryPickRequest();
        }
        return null;
      }

      @Override
      public Object cellAction(LabActivity activity) { return _cprViewer.viewEntity(getCherryPickRequest(activity)); }

      @Override
      public boolean isCommandLink() { return true; }
    };
    column.addRelationshipPath(CherryPickLiquidTransfer.cherryPickAssayPlates.to(CherryPickAssayPlate.cherryPickRequest).castToSupertype(LabActivity.class));
    column.setVisible(false);
    columns.add(2, column);
    
    Iterable<TableColumn<Screen,?>> screenColumns = Iterables.concat(_screenSearchResults.buildScreenSummaryColumns(),
                                                                     _screenSearchResults.buildScreenAdminColumns(true));
    
    screenColumns = Iterables.filter(screenColumns,
                                     new Predicate<TableColumn<Screen,?>>() { public boolean apply(TableColumn<Screen,?> c) { return !!!c.getName().equals("Date Of Last Activity"); } });
    List<TableColumn<LabActivity,?>> labActivityScreenColumns = Lists.newArrayList(Iterables.transform(screenColumns,
                        new Function<TableColumn<Screen,?>,TableColumn<LabActivity,?>>() { 
      public TableColumn<LabActivity,?> apply(TableColumn<Screen,?> delegateColumn) 
      { 
        RelatedEntityColumn<LabActivity,Screen,Object> column = new RelatedEntityColumn<LabActivity,Screen,Object>(Screen.class, LabActivity.Screen, (TableColumn<Screen,Object>) delegateColumn, SCREEEN_COLUMN_GROUP)
        { 
          public Screen getRelatedEntity(LabActivity a) { return a.getScreen(); } 
        };
        column.setVisible(false);
        return column;
      } 
    }));
    labActivityScreenColumns.get(0).setVisible(true);
    columns.addAll(labActivityScreenColumns);

    return columns;
  }

  @Override
  protected Set<String> getActivityTypes()
  {
    return ACTIVITY_TYPES;
  }
}
