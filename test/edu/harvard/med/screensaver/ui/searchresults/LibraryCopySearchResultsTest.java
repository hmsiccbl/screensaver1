// $HeadURL: http://forge.abcd.harvard.edu/svn/screensaver/branches/iccbl/library-copy-mgmt/test/edu/harvard/med/screensaver/ui/searchresults/PlateScreeningStatusSearchResultsTest.java $
// $Id: PlateScreeningStatusSearchResultsTest.java 4450 2010-07-28 17:12:57Z atolopko $
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.math.BigDecimal;
import java.util.List;

import com.google.common.collect.Lists;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.TestDataFactory;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.ui.table.model.DataTableModel;

public class LibraryCopySearchResultsTest extends AbstractSpringPersistenceTest
{
  protected LibrariesDAO librariesDao;
  
  @Override
  protected void onSetUp() throws Exception
  {
    super.onSetUp();
    
    TestDataFactory dataFactory = new TestDataFactory();
    Library library = dataFactory.newInstance(Library.class);
    library.setStartPlate(100);
    library.setEndPlate(103);
    Copy copyA = library.createCopy((AdministratorUser) library.getCreatedBy(), CopyUsageType.LIBRARY_SCREENING_PLATES, "A");
    Plate plate100A = copyA.findPlate(100).withWellVolume(new Volume(0));
    Plate plate101A = copyA.findPlate(101).withWellVolume(new Volume(0));
    Plate plate102A = copyA.findPlate(102).withWellVolume(new Volume(0));
    Copy copyB = library.createCopy((AdministratorUser) library.getCreatedBy(), CopyUsageType.LIBRARY_SCREENING_PLATES, "B");
    Plate plate100B = copyB.findPlate(100).withWellVolume(new Volume(0));
    Plate plate101B = copyB.findPlate(101).withWellVolume(new Volume(0));
    Plate plate102B = copyB.findPlate(102).withWellVolume(new Volume(0));
    Copy copyC = library.createCopy((AdministratorUser) library.getCreatedBy(), CopyUsageType.LIBRARY_SCREENING_PLATES, "C");
    Plate plate103C = copyC.findPlate(103).withWellVolume(new Volume(0));
    genericEntityDao.saveOrUpdateEntity(library);
    
    Screen screen1 = dataFactory.newInstance(Screen.class);
    
    LibraryScreening ls1 = dataFactory.newInstance(LibraryScreening.class, screen1);
    ls1.setNumberOfReplicates(2);
    ls1.setComments("first");
    ls1.addAssayPlatesScreened(plate100A);
    ls1.addAssayPlatesScreened(plate101A);
    genericEntityDao.saveOrUpdateEntity(ls1.getScreen());

    LibraryScreening ls2 = dataFactory.newInstance(LibraryScreening.class, screen1);
    ls2.setNumberOfReplicates(1);
    ls2.addAssayPlatesScreened(plate101B);
    ls2.addAssayPlatesScreened(plate102B);
    genericEntityDao.saveOrUpdateEntity(ls2.getScreen());
    
    genericEntityDao.saveOrUpdateEntity(screen1);

    Screen screen2 = dataFactory.newInstance(Screen.class);

    LibraryScreening ls3 = dataFactory.newInstance(LibraryScreening.class, screen2);
    ls3.setNumberOfReplicates(2);
    ls3.setComments("first");
    ls3.addAssayPlatesScreened(plate100B);
    ls3.addAssayPlatesScreened(plate101B);
    ls3.addAssayPlatesScreened(plate102B);
    genericEntityDao.saveOrUpdateEntity(ls3.getScreen());

    genericEntityDao.saveOrUpdateEntity(screen2);
  }

  public void testSearchPlatesScreenedByLibrary()
  {
    LibraryCopySearchResults searchResults = new LibraryCopySearchResults(genericEntityDao, null, null, null, null);
    searchResults.searchCopiesByLibrary(genericEntityDao.findEntityByProperty(Library.class, "startPlate", 100));
    searchResults.getColumnManager().setSortColumnName("Copy Name");
    searchResults.getColumnManager().setSortAscending(true);
    assertEquals(Lists.newArrayList(100, 100, 100), getColumnsValues(searchResults, "Start Plate"));
    assertEquals(Lists.newArrayList("A", "B", "C"), getColumnsValues(searchResults, "Copy Name"));
    assertEquals(Lists.newArrayList(1, 2, 0), getColumnsValues(searchResults, "Screening Count"));
    assertEquals(Lists.newArrayList(2, 5, 0), getColumnsValues(searchResults, "Plate Screening Count"));
    assertEquals(Lists.newArrayList(4, 4, 4), getColumnsValues(searchResults, "Copy Plate Count"));
    assertEquals(Lists.newArrayList(new BigDecimal("0.5"), new BigDecimal("1.3"), BigDecimal.ZERO),
                 getColumnsValues(searchResults, "Plate Screening Count Average"));
  }

  private List<Object> getColumnsValues(SearchResults searchResults, String columnName)
  {
    DataTableModel<Object> model = searchResults.getDataTableModel();
    TableColumn<Object,?> column = searchResults.getColumnManager().getColumn(columnName);
    assert column != null : "no such column: " + columnName;
    List<Object> result = Lists.newArrayList();
    for (int i = 0; i < model.getRowCount(); ++i) {
      model.setRowIndex(i);
      result.add(column.getCellValue(model.getRowData()));
    }
    return result;
  }

}
