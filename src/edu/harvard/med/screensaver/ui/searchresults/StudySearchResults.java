// $HeadURL$
// $Id$

// Copyright © 2006, 2010 by the President and Fellows of Harvard College.

// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.util.List;

import com.google.common.collect.Lists;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.datafetcher.EntityDataFetcher;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.model.screens.StudyType;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.screens.StudyViewer;
import edu.harvard.med.screensaver.ui.table.Criterion.Operator;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.EnumEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.TextEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.UserNameColumn;
import edu.harvard.med.screensaver.ui.table.model.InMemoryEntityDataModel;
import edu.harvard.med.screensaver.ui.users.UserViewer;


/**
 * A {@link SearchResults} for {@link Study Studies}.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class StudySearchResults extends EntityBasedEntitySearchResults<Screen,Integer>
{

  // private static final fields


  // instance fields

  private UserViewer _userViewer;
  private GenericEntityDAO _dao;


  // public constructor

  /**
   * @motivation for CGLIB2
   */
  protected StudySearchResults()
  {
  }

  public StudySearchResults(StudyViewer studyViewer, 
                            UserViewer userViewer, 
                            GenericEntityDAO dao)
  {
    super(studyViewer);
    _userViewer = userViewer;
    _dao = dao;
  }

  @Override
  public void searchAll()
  {
    setTitle("Studies");
    initialize(new InMemoryEntityDataModel<Screen,Integer>(new EntityDataFetcher<Screen,Integer>(Screen.class, _dao) {
      @Override
      public void addDomainRestrictions(HqlBuilder hql)
      {
        super.addDomainRestrictions(hql);
        hql.where(getRootAlias(), Screen.facilityId.getPropertyName(), Operator.TEXT_STARTS_WITH, Study.STUDY_FACILITY_ID_PREFIX);
      }
    }));
  }


  // implementations of the SearchResults abstract methods

  @Override
  protected List<? extends TableColumn<Screen,?>> buildColumns()
  {
    List<TableColumn<Screen,?>> columns = Lists.newArrayList();
    columns.add(new TextEntityColumn<Screen>(Screen.facilityId,
                                             "Study Facility ID", "The facility-assigned study identifier", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(Screen study)
      {
        return study.getFacilityId();
      }

      @Override
      public Object cellAction(Screen study) { return viewSelectedEntity(); }

      @Override
      public boolean isCommandLink() { return true; }
    });
    columns.add(new TextEntityColumn<Screen>(Screen.thisEntity.toProperty("title"),
      "Title", "The title of the study", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(Screen study) { return study.getTitle(); }
    });
    columns.add(new UserNameColumn<Screen,ScreeningRoomUser>(Screen.labHead,
      "Lab Head", "The head of the lab performing the study", TableColumn.UNGROUPED, _userViewer) {
      @Override
      public ScreeningRoomUser getUser(Screen study) { return study.getLabHead(); }
    });
    columns.add(new UserNameColumn<Screen,ScreeningRoomUser>(Screen.leadScreener,
      "Study Head", "The scientist primarily responsible for running the study", TableColumn.UNGROUPED, _userViewer) {
      @Override
      public ScreeningRoomUser getUser(Screen study) { return study.getLeadScreener(); }
    });
    columns.add(new EnumEntityColumn<Screen,StudyType>(Screen.thisEntity.toProperty("studyType"),
      "Study Type", "'" + StudyType.IN_SILICO + "'' or '" + StudyType.IN_VITRO +"'",
      TableColumn.UNGROUPED, StudyType.values()) {
      @Override
      public StudyType getCellValue(Screen study) { return study.getStudyType(); }
    });
    columns.add(new EnumEntityColumn<Screen,ScreenType>(Screen.thisEntity.toProperty("screenType"),
      "Library Screen Type", "'RNAi' or 'Small Molecule'", TableColumn.UNGROUPED, ScreenType.values()) {
      @Override
      public ScreenType getCellValue(Screen study) { return study.getScreenType(); }
    });

//  TableColumnManager<Screen> columnManager = getColumnManager();
//  columnManager.addCompoundSortColumns(columnManager.getColumn("Lab Head"),
//  columnManager.getColumn("Lead Screener"),
//  columnManager.getColumn("Screen Number"));
//  columnManager.addCompoundSortColumns(columnManager.getColumn("Lead Screener"),
//  columnManager.getColumn("Lab Head"),
//  columnManager.getColumn("Screen Number"));

    return columns;
  }

  private boolean showStatusFields()
  {
    return isUserInRole(ScreensaverUserRole.SCREENS_ADMIN/*TODO: need STUDY_ADMIN, perhaps*/) ||
      isUserInRole(ScreensaverUserRole.READ_EVERYTHING_ADMIN);
  }
}
