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

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;

import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryPlate;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.model.DataTableModel;
import edu.harvard.med.screensaver.ui.arch.view.AbstractBackingBeanTest;
import edu.harvard.med.screensaver.ui.libraries.LibraryPlateSearchResults;

public class LibraryPlateSearchResultsTest extends AbstractBackingBeanTest
{
  @Autowired
  protected LibrariesDAO librariesDao;
  // note: we must get the Spring-instantiated libraryPlatesBrowser, to ensure Transactional behavior
  @Autowired
  protected LibraryPlateSearchResults libraryPlatesBrowser;

  private Screen _screen;
  private Library _library;
  
  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    
    _library = dataFactory.newInstance(Library.class);
    _library.setStartPlate(100);
    _library.setEndPlate(103);
    Copy copyA = _library.createCopy((AdministratorUser) _library.getCreatedBy(), CopyUsageType.LIBRARY_SCREENING_PLATES, "A");
    Copy copyB = _library.createCopy((AdministratorUser) _library.getCreatedBy(), CopyUsageType.LIBRARY_SCREENING_PLATES, "B");
    genericEntityDao.saveOrUpdateEntity(_library);
    
    _screen = dataFactory.newInstance(Screen.class);
    
    LibraryScreening ls1 = _screen.createLibraryScreening(dataFactory.newInstance(AdministratorUser.class),
                                                          dataFactory.newInstance(ScreeningRoomUser.class),
                                                          new LocalDate());
    ls1.setNumberOfReplicates(2);
    ls1.setComments("first");
    ls1.addAssayPlatesScreened(copyA.getPlates().get(100));
    ls1.addAssayPlatesScreened(copyA.getPlates().get(101));
    genericEntityDao.saveOrUpdateEntity(ls1.getScreen());

    LibraryScreening ls2 = _screen.createLibraryScreening(dataFactory.newInstance(AdministratorUser.class),
                                                          dataFactory.newInstance(ScreeningRoomUser.class),
                                                          new LocalDate());
    ls2.setNumberOfReplicates(1);
    ls2.addAssayPlatesScreened(copyB.getPlates().get(101));
    ls2.addAssayPlatesScreened(copyB.getPlates().get(102));
    genericEntityDao.saveOrUpdateEntity(ls2.getScreen());
    
    genericEntityDao.saveOrUpdateEntity(_screen);
  }

  public void testSearchLibraryPlatesByLibrary()
  {
    libraryPlatesBrowser.searchLibraryPlatesByLibrary(_library);
    assertEquals(Lists.newArrayList(100, 101, 102, 103), getColumnsValues(libraryPlatesBrowser, "Plate"));
    assertEquals(Lists.newArrayList(1, 2, 1, 0), getColumnsValues(libraryPlatesBrowser, "Screening Count"));
    assertEquals(Lists.newArrayList(ImmutableSortedSet.of("A"), ImmutableSortedSet.of("A", "B"), ImmutableSortedSet.of("B"), ImmutableSortedSet.of()), getColumnsValues(libraryPlatesBrowser, "Copies Screened"));
  }

  public void testSearchPlatesScreenedByScreen()
  {
    libraryPlatesBrowser.searchLibraryPlatesScreenedByScreen(_screen);
    assertEquals(Lists.newArrayList(100, 101, 102), getColumnsValues(libraryPlatesBrowser, "Plate"));
    assertEquals(Lists.newArrayList(1, 2, 1), getColumnsValues(libraryPlatesBrowser, "Screening Count"));
    assertEquals(Lists.newArrayList(ImmutableSortedSet.of("A"), ImmutableSortedSet.of("A", "B"), ImmutableSortedSet.of("B")), getColumnsValues(libraryPlatesBrowser, "Copies Screened"));
  }
  
  public void testSearchPlatesScreenedByLibraryScreening()
  {
    libraryPlatesBrowser.searchLibraryPlatesScreenedByLibraryScreening(genericEntityDao.findEntityByProperty(LibraryScreening.class, "comments", "first"));
    assertEquals(Lists.newArrayList(100, 101), getColumnsValues(libraryPlatesBrowser, "Plate"));
    assertEquals(Lists.newArrayList(1, 1), getColumnsValues(libraryPlatesBrowser, "Screening Count"));
    assertEquals(Lists.newArrayList(ImmutableSortedSet.of("A"), ImmutableSortedSet.of("A")), getColumnsValues(libraryPlatesBrowser, "Copies Screened"));
  }
  
  private List<Object> getColumnsValues(LibraryPlateSearchResults searchResults, String columnName)
  {
    DataTableModel<LibraryPlate> model = searchResults.getDataTableModel();
    TableColumn<LibraryPlate,?> column = searchResults.getColumnManager().getColumn(columnName);
    assert column != null : "no such column: " + columnName;
    List<Object> result = Lists.newArrayList();
    for (int i = 0; i < model.getRowCount(); ++i) {
      model.setRowIndex(i);
      result.add(column.getCellValue((LibraryPlate) model.getRowData()));
    }
    return result;
  }

}
