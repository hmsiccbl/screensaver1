// $HeadURL: http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/iccbl/2.2.2-dev/test/edu/harvard/med/screensaver/ui/searchresults/LibraryCopyPlateSearchResultsTest.java $
// $Id: LibraryCopyPlateSearchResultsTest.java 5072 2010-11-29 17:26:27Z seanderickson1 $
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import static edu.harvard.med.screensaver.util.CollectionUtils.listOf;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;

import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.libraries.PlateActivity;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.model.DataTableModel;
import edu.harvard.med.screensaver.ui.arch.view.AbstractBackingBeanTest;
import edu.harvard.med.screensaver.ui.libraries.LibraryCopyPlateCommentSearchResults;
import edu.harvard.med.screensaver.ui.libraries.LibraryCopyPlateSearchResults;

public class LibraryCopyPlateCommentSearchResultsTest extends AbstractBackingBeanTest
{
  @Autowired
  protected LibrariesDAO librariesDao;
  @Autowired
  protected LibraryCopyPlateSearchResults libraryCopyPlatesBrowser;
  @Autowired
  protected LibraryCopyPlateCommentSearchResults libraryCopyPlateCommentSearchResultNested;

  private Library _library;
  private Copy _copyA, _copyB;
  private String expectedCommentBy;


  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    
    _library = dataFactory.newInstance(Library.class);
    _library.setStartPlate(100);
    _library.setEndPlate(105);
    Copy copyA = _library.createCopy(_admin, CopyUsageType.LIBRARY_SCREENING_PLATES, "A");
    _copyA = copyA;
    copyA.findPlate(100).withWellVolume(new Volume(0));
    copyA.findPlate(101).withWellVolume(new Volume(0));
    copyA.findPlate(102).withWellVolume(new Volume(0));
    Copy copyB = _library.createCopy(_admin, CopyUsageType.LIBRARY_SCREENING_PLATES, "B");
    _copyB = copyB;
    copyB.findPlate(100).withWellVolume(new Volume(0));
    copyB.findPlate(101).withWellVolume(new Volume(0));
    copyB.findPlate(102).withWellVolume(new Volume(0));

    for (Copy copy : _library.getCopies()) {
      for (int p = 100; p <= 102; ++p) {
        Plate plate = copy.findPlate(p);
        for (int i = 0; i < 2; ++i) {
          plate.createUpdateActivity(AdministrativeActivityType.COMMENT, 
                                     _admin, 
                                     "copy" + copy.getName() + "-plate" + plate.getPlateNumber() + "-comment" + i);
        }
      }
    }
    /* _library = */genericEntityDao.saveOrUpdateEntity(_library);
    
    expectedCommentBy = _admin.getFullNameLastFirst();
  }

  public void testSearchForPlates()
  {
    Set<Integer> plateIds = Sets.newHashSet(librariesDao.findPlate(100, "B").getPlateId(),
                                            librariesDao.findPlate(100, "A").getPlateId(),
                                            librariesDao.findPlate(102, "A").getPlateId());

    LibraryCopyPlateCommentSearchResults searchResults = libraryCopyPlateCommentSearchResultNested;
    searchResults.searchForPlates("Search for 100:B, 100:A, 102:A", plateIds);
    searchResults.setNested(true);
    assertEquals(Sets.newHashSet("copyA-plate100-comment0",
                                 "copyA-plate100-comment1",
                                 "copyA-plate102-comment0",
                                 "copyA-plate102-comment1",
                                 "copyB-plate100-comment0",
                                 "copyB-plate100-comment1"),
                 Sets.newHashSet(getColumnsValues(searchResults, "Comment")));
    assertEquals(listOf(expectedCommentBy, 6), getColumnsValues(searchResults, "Comment by"));
  }

  public void testSearchForCopy()
  {
    LibraryCopyPlateCommentSearchResults searchResults = libraryCopyPlateCommentSearchResultNested;
    searchResults.searchForCopy(_copyA);
    searchResults.setNested(true);
    assertEquals(Sets.newHashSet("copyA-plate100-comment0",
                                 "copyA-plate100-comment1",
                                 "copyA-plate101-comment0",
                                 "copyA-plate101-comment1",
                                 "copyA-plate102-comment0",
                                 "copyA-plate102-comment1"),
                 Sets.newHashSet(getColumnsValues(searchResults, "Comment")));
    assertEquals(listOf(expectedCommentBy, 6), getColumnsValues(searchResults, "Comment by"));
  }

  public void testSearchForLibrary()
  {
    LibraryCopyPlateCommentSearchResults searchResults = libraryCopyPlateCommentSearchResultNested;
    searchResults.searchForLibrary(_copyA.getLibrary());
    searchResults.setNested(true);
    assertEquals(Sets.newHashSet("copyA-plate100-comment0",
                                 "copyA-plate100-comment1",
                                 "copyA-plate101-comment0",
                                 "copyA-plate101-comment1",
                                 "copyA-plate102-comment0",
                                 "copyA-plate102-comment1",
                                 "copyB-plate100-comment0",
                                 "copyB-plate100-comment1",
                                 "copyB-plate101-comment0",
                                 "copyB-plate101-comment1",
                                 "copyB-plate102-comment0",
                                 "copyB-plate102-comment1"),
                 Sets.newHashSet(getColumnsValues(searchResults, "Comment")));
    assertEquals(listOf(expectedCommentBy, 12), getColumnsValues(searchResults, "Comment by"));
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
