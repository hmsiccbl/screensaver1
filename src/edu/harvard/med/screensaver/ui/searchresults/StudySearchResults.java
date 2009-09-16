// $HeadURL$
// $Id$

// Copyright 2006 by the President and Fellows of Harvard College.

// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.util.List;
import java.util.Map;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.datafetcher.AllEntitiesOfTypeDataFetcher;
import edu.harvard.med.screensaver.db.hibernate.HqlBuilder;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.model.screens.StudyType;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.screens.StudyViewer;
import edu.harvard.med.screensaver.ui.table.Criterion.Operator;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.EnumEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.IntegerEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.TextEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.UserNameColumn;
import edu.harvard.med.screensaver.ui.users.UserViewer;

import com.google.common.collect.Lists;


/**
 * A {@link SearchResults} for {@link Study Studies}.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class StudySearchResults extends EntitySearchResults<Screen,Integer>
{

  // private static final fields


  // instance fields

  private StudyViewer _studyViewer;
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
    _studyViewer = studyViewer;
    _userViewer = userViewer;
    _dao = dao;
  }

  public void searchStudies()
  {
    initialize(new AllEntitiesOfTypeDataFetcher<Screen,Integer>(Screen.class, _dao) {
      @Override
      protected void addDomainRestrictions(HqlBuilder hql,
                                           Map<RelationshipPath<Screen>,String> path2Alias)
      {
        super.addDomainRestrictions(hql, path2Alias);
        hql.where(getRootAlias(), "screenNumber", Operator.GREATER_THAN_EQUAL, Study.MIN_STUDY_NUMBER);
      }
    });
  }


  // implementations of the SearchResults abstract methods

  @SuppressWarnings("unchecked")
  @Override
  protected List<? extends TableColumn<Screen,?>> buildColumns()
  {
    List<TableColumn<Screen,?>> columns = Lists.newArrayList();
    columns.add(new IntegerEntityColumn<Screen>(
      new PropertyPath(Screen.class, "screenNumber"),
      "Study Number", "The study number", TableColumn.UNGROUPED) {
      @Override
      public Integer getCellValue(Screen study) { return study.getStudyNumber(); }

      @Override
      public Object cellAction(Screen study) { return viewSelectedEntity(); }

      @Override
      public boolean isCommandLink() { return true; }
    });
    columns.add(new TextEntityColumn<Screen>(
      new PropertyPath(Screen.class, "title"),
      "Title", "The title of the study", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(Screen study) { return study.getTitle(); }
    });
    columns.add(new UserNameColumn<Screen>(
      new RelationshipPath(Screen.class, "labHead"),
      "Lab Head", "The head of the lab performing the study", TableColumn.UNGROUPED, _userViewer) {
      @Override
      public ScreensaverUser getUser(Screen study) { return study.getLabHead(); }
    });
    columns.add(new UserNameColumn<Screen>(
      new RelationshipPath(Screen.class, "leadScreener"),
      "Study Head", "The scientist primarily responsible for running the study", TableColumn.UNGROUPED, _userViewer) {
      @Override
      public ScreensaverUser getUser(Screen study) { return study.getLeadScreener(); }
    });
    columns.add(new EnumEntityColumn<Screen,StudyType>(
      new PropertyPath(Screen.class, "studyType"),
      "Study Type", "'" + StudyType.IN_SILICO + "'' or '" + StudyType.IN_VITRO +"'",
      TableColumn.UNGROUPED, StudyType.values()) {
      @Override
      public StudyType getCellValue(Screen study) { return study.getStudyType(); }
    });
    columns.add(new EnumEntityColumn<Screen,ScreenType>(
      new PropertyPath(Screen.class, "screenType"),
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

  @Override
  protected void setEntityToView(Screen study)
  {
    _studyViewer.viewStudy(study);
  }
}
