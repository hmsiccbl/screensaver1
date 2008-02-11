// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/ui/searchresults/ScreenSearchResults.java $
// $Id: ScreenSearchResults.java 1729 2007-08-22 02:42:54Z ant4 $

// Copyright 2006 by the President and Fellows of Harvard College.

// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.util.ArrayList;
import java.util.List;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.model.screens.StudyType;
import edu.harvard.med.screensaver.model.users.ScreensaverUser;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.screens.StudyViewer;
import edu.harvard.med.screensaver.ui.table.TableColumn;


/**
 * A {@link SearchResults} for {@link Study Studies}.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class StudySearchResults extends EntitySearchResults<Study>
{

  // private static final fields


  // instance fields

  private StudyViewer _studyViewer;
  private GenericEntityDAO _dao;

  private ArrayList<TableColumn<Study,?>> _columns;



  // public constructor

  /**
   * @motivation for CGLIB2
   */
  protected StudySearchResults()
  {
  }

  public StudySearchResults(StudyViewer studyViewer, GenericEntityDAO dao)
  {
    _studyViewer = studyViewer;
    _dao = dao;
  }


  // implementations of the SearchResults abstract methods

  protected List<TableColumn<Study,?>> getColumns()
  {
    if (_columns == null) {
      _columns = new ArrayList<TableColumn<Study,?>>();
      _columns.add(new IntegerColumn<Study>("Study Number", "The study number") {
        @Override
        public Integer getCellValue(Study study) { return study.getStudyNumber(); }

        @Override
        public Object cellAction(Study study) { return viewCurrentEntity(); }

        @Override
        public boolean isCommandLink() { return true; }
      });
      _columns.add(new TextColumn<Study>("Title", "The title of the study") {
        @Override
        public String getCellValue(Study study) { return study.getTitle(); }
      });
      _columns.add(new UserNameColumn<Study>("Lab Head", "The head of the lab performing the study") {
        @Override
        public ScreensaverUser getUser(Study study) { return study.getLabHead(); }
      });
      _columns.add(new UserNameColumn<Study>("Study Head", "The scientist primarily responsible for running the study") {
        @Override
        public ScreensaverUser getUser(Study study) { return study.getLeadScreener(); }
      });
      _columns.add(new EnumColumn<Study,StudyType>("Study Type", "'" + StudyType.IN_SILICO + "'' or '" + StudyType.IN_VITRO +"'",
        StudyType.values()) {
        @Override
        public StudyType getCellValue(Study study) { return study.getStudyType(); }
      });
      _columns.add(new EnumColumn<Study,ScreenType>("Library Screen Type", "'RNAi' or 'Small Molecule'", ScreenType.values()) {
        @Override
        public ScreenType getCellValue(Study study) { return study.getScreenType(); }
      });
    }
    return _columns;
  }

  @Override
  protected List<Integer[]> getCompoundSorts()
  {
    List<Integer[]> compoundSorts = super.getCompoundSorts();
    compoundSorts.add(new Integer[] {2, 3, 0});
    compoundSorts.add(new Integer[] {3, 2, 0});
    return compoundSorts;
  }

  private boolean showStatusFields()
  {
    return isUserInRole(ScreensaverUserRole.SCREENS_ADMIN/*TODO: need STUDY_ADMIN, perhaps*/) ||
      isUserInRole(ScreensaverUserRole.READ_EVERYTHING_ADMIN);
  }

  @Override
  protected void setEntityToView(Study study)
  {
    _studyViewer.viewStudy(study);
  }
}
