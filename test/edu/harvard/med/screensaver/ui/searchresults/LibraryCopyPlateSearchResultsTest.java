// $HeadURL$
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

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
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.ui.table.model.DataTableModel;

public class LibraryCopyPlateSearchResultsTest extends AbstractSpringPersistenceTest
{
  protected LibrariesDAO librariesDao;
  protected LibraryCopyPlateSearchResults libraryCopyPlatesBrowser;
  
  @Override
  protected void onSetUp() throws Exception
  {
    super.onSetUp();
    
    TestDataFactory dataFactory = new TestDataFactory();
    Library library = dataFactory.newInstance(Library.class);
    library.setStartPlate(100);
    library.setEndPlate(102);
    Copy copyA = library.createCopy((AdministratorUser) library.getCreatedBy(), CopyUsageType.LIBRARY_SCREENING_PLATES, "A");
    copyA.findPlate(100).withWellVolume(new Volume(0));
    copyA.findPlate(101).withWellVolume(new Volume(0));
    copyA.findPlate(102).withWellVolume(new Volume(0));
    Copy copyB = library.createCopy((AdministratorUser) library.getCreatedBy(), CopyUsageType.LIBRARY_SCREENING_PLATES, "B");
    copyB.findPlate(100).withWellVolume(new Volume(0));
    copyB.findPlate(101).withWellVolume(new Volume(0));
    copyB.findPlate(102).withWellVolume(new Volume(0));
    genericEntityDao.saveOrUpdateEntity(library);
  }

  public void testSearchPlatesForCopy()
  {
    LibraryCopyPlateSearchResults searchResults = libraryCopyPlatesBrowser;
    searchResults.searchPlatesForCopy(genericEntityDao.findEntityByProperty(Copy.class, "name", "B", true, Copy.library.getPath()));
    assertEquals(Lists.newArrayList(100, 101, 102), getColumnsValues(searchResults, "Plate"));
    assertEquals(Lists.newArrayList("B", "B", "B"), getColumnsValues(searchResults, "Copy"));
  }

  private List<Object> getColumnsValues(LibraryCopyPlateSearchResults searchResults, String columnName)
  {
    DataTableModel<Plate> model = searchResults.getDataTableModel();
    TableColumn<Plate,?> column = searchResults.getColumnManager().getColumn(columnName);
    assert column != null : "no such column: " + columnName;
    List<Object> result = Lists.newArrayList();
    for (int i = 0; i < model.getRowCount(); ++i) {
      model.setRowIndex(i);
      result.add(column.getCellValue((Plate) model.getRowData()));
    }
    return result;
  }

}
