// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.model.DataModel;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.io.DataExporter;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.ui.table.Criterion;
import edu.harvard.med.screensaver.ui.table.DataTableModelType;
import edu.harvard.med.screensaver.ui.table.Criterion.Operator;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.ui.table.column.TableColumnManager;
import edu.harvard.med.screensaver.ui.table.model.DataTableModel;

import org.apache.log4j.Logger;


public class ReagentsSearchResultTest extends AbstractSpringPersistenceTest
{
  // static members

  private static Logger log = Logger.getLogger(ReagentsSearchResultTest.class);

  private ReagentSearchResults _reagentSearchResults;

  @Override
  protected void onSetUp() throws Exception
  {
    super.onSetUp();

    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        String vendor = "vendor";
        Library library = MakeDummyEntities.makeDummyLibrary(1, ScreenType.RNAI, 3 /*3 plates forces use of VirtualPagingDataModel*/);
        library.setVendor(vendor);
        genericEntityDao.persistEntity(library);
        
        Screen study = MakeDummyEntities.makeDummyStudy(library);
        genericEntityDao.persistEntity(study.getLeadScreener());
        genericEntityDao.persistEntity(study.getLabHead());
        genericEntityDao.persistEntity(study);
      }
    });
    
    _reagentSearchResults = new ReagentSearchResults(null,
                                                     null,
                                                     null,
                                                     genericEntityDao,
                                                     Collections.<DataExporter<?>>emptyList());
  }
  
  public void testFilterOnColumns()
  {
//    Study study = genericEntityDao.findEntityByProperty(Screen.class, 
//                                                        "screenNumber", 
//                                                        Study.MIN_STUDY_NUMBER,
//                                                        true,
//                                                        "annotationTypes");
//    assertNotNull("found test study", study);
//    _reagentSearchResults.searchReagentsForStudy(study);
    _reagentSearchResults.searchAllReagents();
    Map<String,Object> columnNameToExpectedValue = new HashMap<String,Object>();
    Well well = genericEntityDao.findEntityById(Well.class, new WellKey("01000:H12").toString());
    assertNotNull("found test well", well);
    columnNameToExpectedValue.put("Reagent Source ID", well.getReagent().getReagentId().getReagentId());
    doTestFilterOnColumns(_reagentSearchResults, columnNameToExpectedValue);
  }
  
  public void testSortColumns()
  {
    doTestSortForAllColumnsAndDirections(_reagentSearchResults);
  }
  
  /**
   * For each columnName/value pair, filter the search result on that column and
   * value (using 'equals' operator), and verify that all filtered rows having
   * matching value.
   * 
   * @param <R>
   * @param searchResults
   * @param columnNameToExpectedValue
   */
  private <R> void doTestFilterOnColumns(SearchResults<R,?,?> searchResults,
                                         Map<String,Object> columnNameToExpectedValue)
  {
    DataTableModel<R> model = searchResults.getDataTableModel();
    assertTrue("using VirtualPagingDataModel", model.getModelType() == DataTableModelType.VIRTUAL_PAGING);
    TableColumnManager<R> columnManager = searchResults.getColumnManager();
    DataModel columnModel = columnManager.getVisibleColumnModel();
    for (String columnName : columnNameToExpectedValue.keySet()) {
      TableColumn<R,?> column = columnManager.getColumn(columnName);
      assertNotNull("column " + columnName + " exists", columnName);
      Object expectedValue = columnNameToExpectedValue.get(column.getName());
      Criterion<Object> criterion = new Criterion<Object>(Operator.EQUAL, expectedValue);
      column.clearCriteria().addCriterion((Criterion) criterion);
      boolean foundColumnIndex = false;
      for (int c = 0; c < columnManager.getVisibleColumns().size(); ++c) {
        if (columnManager.getVisibleColumns().get(c).getName().equals(columnName)) {
          columnModel.setRowIndex(c);
          foundColumnIndex = true;
          break;
        }
      }
      assertTrue("found position of column " + columnName, foundColumnIndex);
      assertTrue("non-zero filtered row count", model.getRowCount() > 0);
      for (int i = 0; i < model.getRowCount(); ++i) {
        model.setRowIndex(i);
        assertEquals("filtered on column " + column.getName() + "=" + expectedValue + ", row " + i, 
                     expectedValue, 
                     searchResults.getCellValue());
      }
    }    
  }

  @SuppressWarnings("unchecked")
  private <R> void doTestSortForAllColumnsAndDirections(SearchResults<R,?,?> searchResults)
  {
    for (SortDirection sortDirection : SortDirection.values()) {
      for (TableColumn<R,?> sortColumn : searchResults.getColumnManager().getVisibleColumns()) {
        doTestSort(searchResults.getDataTableModel(),
                   sortColumn,
                   sortDirection);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private <R> void doTestSort(DataTableModel<R> dataModel,
                          TableColumn<R,?> sortColumn,
                          SortDirection sortDirection)
  {
    log.info("testing sort on " + sortColumn + " in " + sortDirection);
    dataModel.sort(Arrays.asList(sortColumn), sortDirection);
    List<Comparable> actualSortedValues = new ArrayList<Comparable>();
    List<Comparable> expectedSortedValues = new ArrayList<Comparable>();
    for (int i = 0; i < 10; i++) {
      dataModel.setRowIndex(i);
      R rowData = (R) dataModel.getRowData();
      assertNotNull("row data not null for row " + i + ", column " + sortColumn,
                    rowData);
      actualSortedValues.add((Comparable) sortColumn.getCellValue(rowData));
    }
    assertEquals("row count", 10, actualSortedValues.size());
    expectedSortedValues.addAll(actualSortedValues);
    Collections.sort(expectedSortedValues);
    if (sortDirection == SortDirection.DESCENDING) {
      Collections.reverse(expectedSortedValues);
    }
    assertEquals("sorted values on " + sortColumn.getName() + ", " + sortDirection,
                 expectedSortedValues,
                 actualSortedValues);
  }

}

