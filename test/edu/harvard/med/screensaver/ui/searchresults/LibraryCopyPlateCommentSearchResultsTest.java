// $HeadURL: http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/iccbl/2.2.2-dev/test/edu/harvard/med/screensaver/ui/searchresults/LibraryCopyPlateSearchResultsTest.java $
// $Id: LibraryCopyPlateSearchResultsTest.java 5072 2010-11-29 17:26:27Z seanderickson1 $
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.TestDataFactory;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.PlateActivity;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.model.DataTableModel;
import edu.harvard.med.screensaver.ui.libraries.LibraryCopyPlateCommentSearchResults;
import edu.harvard.med.screensaver.ui.libraries.LibraryCopyPlateSearchResults;

public class LibraryCopyPlateCommentSearchResultsTest extends AbstractSpringPersistenceTest
{

  protected GenericEntityDAO genericEntityDao;
  protected LibrariesDAO librariesDao;
  protected LibraryCopyPlateSearchResults libraryCopyPlatesBrowser;
  protected LibraryCopyPlateCommentSearchResults libraryCopyPlateCommentsBrowser;
  
  private Set<String> expectedCommentsA = Sets.newHashSet();
  private Set<String> expectedComments_101_102 = Sets.newHashSet();
  private Set<String> expectedCommentsB = Sets.newHashSet("b test comment", "b test comment1", "b test comment2");

  private Copy _copyA, _copyB;

  @Override
  protected void onSetUp() throws Exception
  {
    super.onSetUp();
    
    TestDataFactory dataFactory = new TestDataFactory();
    Library library = dataFactory.newInstance(Library.class);
    library.setStartPlate(100);
    library.setEndPlate(105);
    Copy copyA = library.createCopy((AdministratorUser) library.getCreatedBy(), CopyUsageType.LIBRARY_SCREENING_PLATES, "A");
    _copyA = copyA;
    copyA.findPlate(100).withWellVolume(new Volume(0));
    copyA.findPlate(101).withWellVolume(new Volume(0));
    copyA.findPlate(102).withWellVolume(new Volume(0));
    Copy copyB = library.createCopy((AdministratorUser) library.getCreatedBy(), CopyUsageType.LIBRARY_SCREENING_PLATES, "B");
    _copyB = copyB;
    copyB.findPlate(100).withWellVolume(new Volume(0));
    copyB.findPlate(101).withWellVolume(new Volume(0));
    copyB.findPlate(102).withWellVolume(new Volume(0));

    for (int i = 0; i < 3; i++) {
      for (int j = 100; j < 103; j++) {
        String comment = "copy-" + copyA.getName() + "-plate-" + j + "-comment: " + i;
        if (j == 100 || j == 102) expectedComments_101_102.add(comment);
        expectedCommentsA.add(comment);
        AdministrativeActivity a = copyA.findPlate(j).createUpdateActivity(AdministrativeActivityType.COMMENT, (AdministratorUser) library.getCreatedBy(), comment);
      }
    }
    for (String comment : expectedCommentsB) {
      copyB.findPlate(100).createUpdateActivity(AdministrativeActivityType.COMMENT, (AdministratorUser) library.getCreatedBy(), comment);
      expectedComments_101_102.add(comment);
    }
    genericEntityDao.saveOrUpdateEntity(library);
    
  }

  public void testSearchForPlates()
  {
    _copyB = genericEntityDao.reloadEntity(_copyB, true, Copy.plates.getPath());
    _copyA = genericEntityDao.reloadEntity(_copyA, true, Copy.plates.getPath());
    Set<Integer> plateIds = Sets.newHashSet();
    plateIds.add(_copyB.getPlates().get(100).getPlateId());
    plateIds.add(_copyA.getPlates().get(100).getPlateId());
    plateIds.add(_copyA.getPlates().get(102).getPlateId());

    LibraryCopyPlateCommentSearchResults searchResults = libraryCopyPlateCommentsBrowser;
    searchResults.searchForPlates("Search for 100,102", plateIds);
    searchResults.setNested(true);
    assertEquals(expectedComments_101_102, Sets.newHashSet(getColumnsValues(searchResults, "Comment")));

  }

  public void testSearchForCopy()
  {
    _copyA = genericEntityDao.reloadEntity(_copyA, true, Copy.plates.getPath());

    LibraryCopyPlateCommentSearchResults searchResults = libraryCopyPlateCommentsBrowser;
    searchResults.searchForCopy(_copyA);
    searchResults.setNested(true);
    assertEquals(expectedCommentsA, Sets.newHashSet(getColumnsValues(searchResults, "Comment")));
  }

  private List<Object> getColumnsValues(LibraryCopyPlateCommentSearchResults searchResults, String columnName)
  {
    DataTableModel<PlateActivity> model = searchResults.getDataTableModel();
    TableColumn<PlateActivity,?> column = searchResults.getColumnManager().getColumn(columnName);
    assert column != null : "no such column: " + columnName;
    List<Object> result = Lists.newArrayList();
    for (int i = 0; i < model.getRowCount(); ++i) {
      model.setRowIndex(i);
      result.add(column.getCellValue((PlateActivity) model.getRowData()));
    }
    return result;
  }

}
