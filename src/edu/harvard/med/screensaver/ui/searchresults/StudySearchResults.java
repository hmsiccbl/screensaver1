// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/ui/searchresults/ScreenSearchResults.java $
// $Id: ScreenSearchResults.java 1729 2007-08-22 02:42:54Z ant4 $

// Copyright 2006 by the President and Fellows of Harvard College.

// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import edu.harvard.med.screensaver.io.DataExporter;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.model.screens.StudyType;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.ui.screens.StudyViewer;
import edu.harvard.med.screensaver.ui.table.TableColumn;
import edu.harvard.med.screensaver.ui.util.ScreensaverUserComparator;


/**
 * A {@link SearchResults} for {@link Study Studies}.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class StudySearchResults extends SearchResults<Study,Object>
{

  // private static final fields


  // instance fields

  private StudyViewer _studyViewer;

  private ArrayList<TableColumn<Study>> _columns;


  // public constructor

  /**
   * @motivation for CGLIB2
   */
  protected StudySearchResults()
  {
  }

  public StudySearchResults(StudyViewer studyViewer)
  {
    _studyViewer = studyViewer;
  }


  // implementations of the SearchResults abstract methods

  protected List<TableColumn<Study>> getColumns()
  {
    if (_columns == null) {
      _columns = new ArrayList<TableColumn<Study>>();
      _columns.add(new TableColumn<Study>("Study Number", "The study number", true) {
        @Override
        public Object getCellValue(Study study) { return study.getStudyNumber(); }

        @Override
        public Object cellAction(Study study) { return _studyViewer.viewStudy(study); }

        @Override
        public boolean isCommandLink() { return true; }
      });
      _columns.add(new TableColumn<Study>("Study Type", "'" + StudyType.IN_SILICO + "'' or '" + StudyType.IN_VITRO +"'") {
        @Override
        public Object getCellValue(Study study) { return study.getStudyType().getValue(); }
      });
      _columns.add(new TableColumn<Study>("Library Screen Type", "'RNAi' or 'Small Molecule'") {
        @Override
        public Object getCellValue(Study study) { return study.getScreenType().getValue(); }
      });
      _columns.add(new TableColumn<Study>("Title", "The title of the study") {
        @Override
        public Object getCellValue(Study study) { return study.getTitle(); }
      });
      _columns.add(new TableColumn<Study>("Lab Head", "The head of the lab performing the study") {
        @Override
        public Object getCellValue(Study study) { return study.getLabHead().getFullNameLastFirst(); }

        @Override
        protected Comparator<Study> getAscendingComparator()
        {
          return new Comparator<Study>() {
            public int compare(Study s1, Study s2) {
              return ScreensaverUserComparator.getInstance().compare(s1.getLabHead(),
                                                                     s2.getLabHead());
            }
          };
        }
      });
      _columns.add(new TableColumn<Study>("Study Head", "The scientist primarily responsible for running the study") {
        @Override
        public Object getCellValue(Study study) { return study.getLeadScreener().getFullNameLastFirst(); }

        @Override
        protected Comparator<Study> getAscendingComparator()
        {
          return new Comparator<Study>() {
            public int compare(Study s1, Study s2) {
              return ScreensaverUserComparator.getInstance().compare(s1.getLeadScreener(),
                                                                     s2.getLeadScreener());
            }
          };
        }
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
  protected List<DataExporter<Study>> getDataExporters()
  {
    return new ArrayList<DataExporter<Study>>();
  }

  @Override
  public String showSummaryView()
  {
    return BROWSE_STUDIES;
  }

  @Override
  protected void setEntityToView(Study study)
  {
    _studyViewer.viewStudy(study);
  }
}
