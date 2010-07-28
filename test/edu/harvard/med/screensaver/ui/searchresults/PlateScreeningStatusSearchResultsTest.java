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

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.TestDataFactory;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.PlateScreeningStatus;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.ui.table.model.DataTableModel;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;

public class PlateScreeningStatusSearchResultsTest extends AbstractSpringPersistenceTest 
{
  protected LibrariesDAO librariesDao;
  
  @Override
  protected void onSetUp() throws Exception
  {
    super.onSetUp();
    
    TestDataFactory dataFactory = new TestDataFactory();
    Library library = dataFactory.newInstance(Library.class);
    library.setStartPlate(100);
    library.setEndPlate(102);
    Copy copyA = library.createCopy(CopyUsageType.FOR_LIBRARY_SCREENING, "A");
    Plate plate100A = copyA.createPlate(100, "", PlateType.ABGENE, new Volume(0));
    Plate plate101A = copyA.createPlate(101, "", PlateType.ABGENE, new Volume(0));
    Plate plate102A = copyA.createPlate(102, "", PlateType.ABGENE, new Volume(0));
    Copy copyB = library.createCopy(CopyUsageType.FOR_LIBRARY_SCREENING, "B");
    Plate plate100B = copyB.createPlate(100, "", PlateType.ABGENE, new Volume(0));
    Plate plate101B = copyB.createPlate(101, "", PlateType.ABGENE, new Volume(0));
    Plate plate102B = copyB.createPlate(102, "", PlateType.ABGENE, new Volume(0));
    genericEntityDao.saveOrUpdateEntity(library);
    
    Screen screen = dataFactory.newInstance(Screen.class);
    
    LibraryScreening ls1 = dataFactory.newInstance(LibraryScreening.class, screen);
    ls1.setNumberOfReplicates(2);
    ls1.setComments("first");
    ls1.addAssayPlatesScreened(plate100A);
    ls1.addAssayPlatesScreened(plate101A);
    genericEntityDao.saveOrUpdateEntity(ls1.getScreen());

    LibraryScreening ls2 = dataFactory.newInstance(LibraryScreening.class, screen);
    ls2.setNumberOfReplicates(1);
    ls2.addAssayPlatesScreened(plate101B);
    ls2.addAssayPlatesScreened(plate102B);
    genericEntityDao.saveOrUpdateEntity(ls2.getScreen());
    
    genericEntityDao.saveOrUpdateEntity(screen);
  }

  public void testSearchPlatesScreenedByScreen()
  {
    PlateScreeningStatusSearchResults searchResults = new PlateScreeningStatusSearchResults(genericEntityDao, librariesDao, null, null);
    searchResults.searchPlatesScreenedByScreen(genericEntityDao.findEntityByProperty(LibraryScreening.class, "comments", "first").getScreen());
    assertEquals(Lists.newArrayList(100, 101, 102), getColumnsValues(searchResults, "Plate"));
    assertEquals(Lists.newArrayList(1, 2, 1), getColumnsValues(searchResults, "Screening Count"));
    assertEquals(Lists.newArrayList(ImmutableSortedSet.of("A"), ImmutableSortedSet.of("A", "B"), ImmutableSortedSet.of("B")), getColumnsValues(searchResults, "Copies"));
  }
  
  public void testSearchPlatesScreenedByLibraryScreening()
  {
    PlateScreeningStatusSearchResults searchResults = new PlateScreeningStatusSearchResults(genericEntityDao, librariesDao, null, null);
    searchResults.searchPlatesScreenedByLibraryScreening(genericEntityDao.findEntityByProperty(LibraryScreening.class, "comments", "first"));
    assertEquals(Lists.newArrayList(100, 101), getColumnsValues(searchResults, "Plate"));
    assertEquals(Lists.newArrayList(1, 1), getColumnsValues(searchResults, "Screening Count"));
    assertEquals(Lists.newArrayList(ImmutableSortedSet.of("A"), ImmutableSortedSet.of("A")), getColumnsValues(searchResults, "Copies"));
  }
  
  private List<Object> getColumnsValues(PlateScreeningStatusSearchResults searchResults, String columnName)
  {
    DataTableModel<PlateScreeningStatus> model = searchResults.getDataTableModel();
    TableColumn<PlateScreeningStatus,?> column = searchResults.getColumnManager().getColumn(columnName);
    assert column != null : "no such column: " + columnName;
    List<Object> result = Lists.newArrayList();
    for (int i = 0; i < model.getRowCount(); ++i) {
      model.setRowIndex(i);
      result.add(column.getCellValue((PlateScreeningStatus) model.getRowData()));
    }
    return result;
  }
}
