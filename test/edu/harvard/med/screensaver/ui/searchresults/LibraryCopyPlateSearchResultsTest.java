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
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.io.libraries.LibraryCopyPlateListParser;
import edu.harvard.med.screensaver.io.libraries.LibraryCopyPlateListParserResult;
import edu.harvard.med.screensaver.model.TestDataFactory;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.ui.arch.datatable.Criterion.Operator;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.model.DataTableModel;
import edu.harvard.med.screensaver.ui.libraries.LibraryCopyPlateSearchResults;

public class LibraryCopyPlateSearchResultsTest extends AbstractSpringPersistenceTest
{

  protected GenericEntityDAO genericEntityDao;
  protected LibrariesDAO librariesDao;
  protected LibraryCopyPlateSearchResults libraryCopyPlatesBrowser;
  
  private static String copyNameWithSpaces = "Copy name with spaces";

  @Override
  protected void onSetUp() throws Exception
  {
    super.onSetUp();
    
    TestDataFactory dataFactory = new TestDataFactory();
    Library library = dataFactory.newInstance(Library.class);
    library.setStartPlate(100);
    library.setEndPlate(105);
    library.setLibraryType(LibraryType.COMMERCIAL);
    Copy copyA = library.createCopy((AdministratorUser) library.getCreatedBy(), CopyUsageType.LIBRARY_SCREENING_PLATES, "A");
    copyA.findPlate(100).withWellVolume(new Volume(0));
    copyA.findPlate(101).withWellVolume(new Volume(0));
    copyA.findPlate(102).withWellVolume(new Volume(0));
    Copy copyB = library.createCopy((AdministratorUser) library.getCreatedBy(), CopyUsageType.LIBRARY_SCREENING_PLATES, "B");
    copyB.findPlate(100).withWellVolume(new Volume(0));
    copyB.findPlate(101).withWellVolume(new Volume(0));
    copyB.findPlate(102).withWellVolume(new Volume(0));

    Copy copyWithSpaces = library.createCopy((AdministratorUser) library.getCreatedBy(), CopyUsageType.LIBRARY_SCREENING_PLATES, copyNameWithSpaces);
    genericEntityDao.saveOrUpdateEntity(library);
  }

  public void testSearchPlatesForCopy()
  {
    LibraryCopyPlateSearchResults searchResults = libraryCopyPlatesBrowser;
    searchResults.setNested(true);
    searchResults.searchPlatesForCopy(genericEntityDao.findEntityByProperty(Copy.class, "name", "B", true, Copy.library.getPath()));
    searchResults.getColumnManager().getColumn("Copy").setVisible(true);
    assertEquals(Lists.newArrayList(100, 101, 102, 103, 104, 105), getColumnsValues(searchResults, "Plate"));
    assertEquals(Lists.newArrayList("B", "B", "B", "B", "B", "B"), getColumnsValues(searchResults, "Copy"));
  }

  public void testSearchForPlates()
  {
    LibraryCopyPlateSearchResults searchResults = libraryCopyPlatesBrowser;
    searchResults.setNested(true);
    // NOTE, this test is put here for convenience.  It is more of a test of the LCPLPR, which contains the search method, however, since
    // the LCPLPR is using the hibernate object through the spring beans; it is better to test it here
    LibraryCopyPlateListParserResult result = LibraryCopyPlateListParser.parsePlateCopies("100-101 104,105 A");
    assertEquals("syntax errors size", 0, result.getErrors().size());

    Set<Integer> plateIds = librariesDao.queryForPlateIds(result);
    assertNotNull(plateIds);

    searchResults.searchForPlates(result.print(), plateIds);
    searchResults.getColumnManager().getColumn("Copy").setVisible(true);
    assertEquals(Lists.newArrayList(100, 101, 104, 105), getColumnsValues(searchResults, "Plate"));
    assertEquals(Lists.newArrayList("A", "A", "A", "A"), getColumnsValues(searchResults, "Copy"));
    assertEquals(4, plateIds.size());

    // test again, no copies specified
    result = LibraryCopyPlateListParser.parsePlateCopies("100");
    assertEquals("syntax errors size", 0, result.getErrors().size());

    plateIds = librariesDao.queryForPlateIds(result);
    assertNotNull(plateIds);

    searchResults.searchForPlates(result.print(), plateIds);
    searchResults.getColumnManager().getColumn("Copy").setVisible(true);
    assertEquals(Lists.newArrayList(100, 100, 100), getColumnsValues(searchResults, "Plate"));
    assertEquals(Sets.newHashSet("A", "B", copyNameWithSpaces), Sets.newHashSet(getColumnsValues(searchResults, "Copy")));
    assertEquals(3, plateIds.size());

    // NOTE, this test is put here for convenience.  It is more of a test of the LCPLPR, which contains the search method, however, since
    // the LCPLPR is using the hibernate session through the spring beans; it is better to test it here
    result = LibraryCopyPlateListParser.parsePlateCopies("104 A");
    assertEquals("syntax errors size", 0, result.getErrors().size());

    plateIds = librariesDao.queryForPlateIds(result);
    assertNotNull(plateIds);

    searchResults.searchForPlates(result.print(), plateIds);
    searchResults.getColumnManager().getColumn("Copy").setVisible(true);
    assertEquals(Lists.newArrayList(104), getColumnsValues(searchResults, "Plate"));
    assertEquals(Lists.newArrayList("A"), getColumnsValues(searchResults, "Copy"));
    assertEquals(1, plateIds.size());

    // test quote chars    
    result = LibraryCopyPlateListParser.parsePlateCopies("104 \"" + copyNameWithSpaces + "\"");
    assertEquals("syntax errors size", 0, result.getErrors().size());

    plateIds = librariesDao.queryForPlateIds(result);
    assertNotNull(plateIds);

    searchResults.searchForPlates(result.print(), plateIds);
    searchResults.getColumnManager().getColumn("Copy").setVisible(true);
    assertEquals(Lists.newArrayList(104), getColumnsValues(searchResults, "Plate"));
    assertEquals(Lists.newArrayList(copyNameWithSpaces), getColumnsValues(searchResults, "Copy"));
    assertEquals(1, plateIds.size());
  }

  public void testSearchForPlatesByCopy()
  {
    LibraryCopyPlateSearchResults searchResults = libraryCopyPlatesBrowser;
    // test again, no plates specified
    LibraryCopyPlateListParserResult result = LibraryCopyPlateListParser.parsePlateCopies("a");
    assertEquals("syntax errors size", 0, result.getErrors().size());

    Set<Integer> plateIds = librariesDao.queryForPlateIds(result);
    assertNotNull(plateIds);

    searchResults.searchForPlates(result.print(), plateIds);
    searchResults.getColumnManager().getColumn("Copy").setVisible(true);
    assertEquals(Lists.newArrayList(100, 101, 102, 103, 104, 105), getColumnsValues(searchResults, "Plate"));
    assertEquals(Sets.newHashSet("A", "A", "A", "A", "A", "A"), Sets.newHashSet(getColumnsValues(searchResults, "Copy")));
    assertEquals(6, plateIds.size());
  }

  public void testSearchForPlatesByRange()
  {
    LibraryCopyPlateSearchResults searchResults = libraryCopyPlatesBrowser;
    // test again, no plates specified
    LibraryCopyPlateListParserResult result = LibraryCopyPlateListParser.parsePlateCopies("101-104 a");
    assertEquals("syntax errors size", 0, result.getErrors().size());

    Set<Integer> plateIds = librariesDao.queryForPlateIds(result);
    assertNotNull(plateIds);

    searchResults.searchForPlates(result.print(), plateIds);
    searchResults.getColumnManager().getColumn("Copy").setVisible(true);
    assertEquals(Lists.newArrayList(101, 102, 103, 104), getColumnsValues(searchResults, "Plate"));
    assertEquals(Sets.newHashSet("A", "A", "A", "A"), Sets.newHashSet(getColumnsValues(searchResults, "Copy")));
    assertEquals(4, plateIds.size());
  }

  public void testSearchForPlatesNotFound()
  {
    LibraryCopyPlateSearchResults searchResults = libraryCopyPlatesBrowser;
    searchResults.setNested(true);
    // NOTE, this test is put here for convenience.  It is more of a test of the LCPLPR, which contains the search method, however, since
    // the LCPLPR is using the hibernate session through the spring beans; it is better to test it here
    LibraryCopyPlateListParserResult result = LibraryCopyPlateListParser.parsePlateCopies("204 A");
    assertEquals("syntax errors size", 0, result.getErrors().size());

    Set<Integer> plateIds = librariesDao.queryForPlateIds(result);
    assertEquals(0, plateIds.size());

    searchResults.searchForPlates(result.print(), plateIds);
    assertEquals(0, searchResults.getRowCount());
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

  public void testCalculatedScreeningStatistics()
  {
    // TODO: in all cases, need to test that statistics were NOT calculated for plates not in the search result
    libraryCopyPlatesBrowser.searchAll();
    assertEquals(0, libraryCopyPlatesBrowser.getRowCount());
    //    assertTrue(Iterables.all((List<Plate>) libraryCopyPlatesBrowser.getDataTableModel().getWrappedData(),
    //                             new Predicate<Plate>() {
    //                               public boolean apply(Plate p)
    //                               {
    //                                 return p.getScreeningStatistics() == null;
    //                               }
    //                             }));

    TableColumn<Plate,Integer> nonScreeningStatisticsColumn = (TableColumn<Plate,Integer>) libraryCopyPlatesBrowser.getColumnManager().getColumn("Plate");
    nonScreeningStatisticsColumn.resetCriteria().setOperatorAndValue(Operator.EQUAL, Integer.valueOf(101));
    assertEquals(3, libraryCopyPlatesBrowser.getRowCount());
    assertTrue(Iterables.all((List<Plate>) libraryCopyPlatesBrowser.getDataTableModel().getWrappedData(),
                             new Predicate<Plate>() {
                               public boolean apply(Plate p)
                               {
                                 return p.getScreeningStatistics() != null;
                               }
                             }));

    libraryCopyPlatesBrowser.resetFilter();
    TableColumn<Plate,Integer> screeningStatisticsColumn = (TableColumn<Plate,Integer>) libraryCopyPlatesBrowser.getColumnManager().getColumn("Screening Count");
    screeningStatisticsColumn.resetCriteria().setOperatorAndValue(Operator.GREATER_THAN_EQUAL, Integer.valueOf(0));
    assertEquals(18, libraryCopyPlatesBrowser.getRowCount());
    assertTrue(Iterables.all((List<Plate>) libraryCopyPlatesBrowser.getDataTableModel().getWrappedData(),
                             new Predicate<Plate>() {
                               public boolean apply(Plate p)
                               {
                                 return p.getScreeningStatistics() != null;
                               }
                             }));

  }

}
