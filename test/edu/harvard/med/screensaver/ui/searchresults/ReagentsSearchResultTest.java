// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;

//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
//import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
//import edu.harvard.med.screensaver.db.DAOTransaction;
//import edu.harvard.med.screensaver.db.SortDirection;
//import edu.harvard.med.screensaver.db.ReagentsSortQuery.SortByReagentProperty;
//import edu.harvard.med.screensaver.model.MakeDummyEntities;
//import edu.harvard.med.screensaver.model.libraries.Library;
//import edu.harvard.med.screensaver.model.libraries.Reagent;
//import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
//import edu.harvard.med.screensaver.model.libraries.Well;
//import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
//import edu.harvard.med.screensaver.model.screens.Screen;
//import edu.harvard.med.screensaver.model.screens.ScreenType;
//import edu.harvard.med.screensaver.ui.table.TableColumn;
//
//import org.apache.log4j.Logger;

public class ReagentsSearchResultTest extends AbstractSpringPersistenceTest
{
//  // static members
//
//  private static Logger log = Logger.getLogger(ReagentsDataModelTest.class);
//
//  private Screen _study;
//
//  private List<TableColumn<Reagent,?>> _columns;
//
//  @Override
//  protected void onSetUp() throws Exception
//  {
//    super.onSetUp();
//
//    genericEntityDao.doInTransaction(new DAOTransaction() {
//      public void runTransaction() {
//        String vendor = "vendor";
//        Library library = MakeDummyEntities.makeDummyLibrary(1, ScreenType.RNAI, 1);
//        library.setVendor(vendor);
//        genericEntityDao.persistEntity(library);
//
//        _study = MakeDummyEntities.makeDummyScreen(1, ScreenType.RNAI);
//        AnnotationType annot1 = new AnnotationType(_study, "annot1", "numeric annotation", 1, true);
//        AnnotationType annot2 = new AnnotationType(_study, "annot2", "text annotation", 2, false);
//        int n = library.getWells().size();
//        int i = 1;
//        for (Well well : library.getWells()) {
//          Reagent reagent = new Reagent(new ReagentVendorIdentifier(vendor, "reagent" + i));
//          reagent.addWell(well);
//          annot1.createAnnotationValue(reagent, String.format("%03d", i));
//          annot2.createAnnotationValue(reagent, String.format("text%03d", (n - i)));
//          ++i;
//        }
//        genericEntityDao.persistEntity(_study);
//      }
//    });
//
//    setupColumns();
//  }
//
//  private void setupColumns()
//  {
//    _columns = new ArrayList<TableColumn<Reagent,?>>();
//    _columns.add(new ReagentColumn<String>(SortByReagentProperty.ID,
//                                   "Reagent Source ID",
//                                   "The vendor-assigned identifier for the reagent.",
//                                   false) {
//      @Override
//      public String getCellValue(Reagent reagent) { return reagent.getEntityId().getReagentId(); }
//    });
////    _columns.add(new ReagentColumn(SortByReagentProperty.CONTENTS,
////                                   "Contents",
////                                   "The gene name for the silencing reagent, or SMILES for the compound reagent",
////                                   false) {
////      @Override
////      public Object getCellValue(Reagent reagent) { return getContentsValue(reagent); }
////
////    });
//
//    for (AnnotationType annotationType : _study.getAnnotationTypes()) {
//      if (annotationType.isNumeric()) {
//        _columns.add(new AnnotationTypeColumn<Double>(annotationType));
//      }
//      else {
//        _columns.add(new AnnotationTypeColumn<String>(annotationType));
//      }
//    }
//  }
//
//  public void testWholeStudyReagentsDataModel()
//  {
//    ReagentsDataModel dataModel = new ReagentsDataModel(_study, 10, _columns, null, null, genericEntityDao);
//    doTestSortForAllColumnsAndDirections(dataModel);
//  }
//
//  public void testArbitrarySetReagentsDataModel()
//  {
//    ReagentsDataModel dataModel = new ReagentsDataModel(_study.getReagents(), 10, _columns, null, null, genericEntityDao);
//    doTestSortForAllColumnsAndDirections(dataModel);
//  }
//
//  @SuppressWarnings("unchecked")
//  private void doTestSortForAllColumnsAndDirections(ReagentsDataModel dataModel)
//  {
//    for (SortDirection sortDirection : SortDirection.values()) {
//      for (TableColumn<Reagent,?> sortColumn : _columns) {
//        doTestSort(dataModel,
//                   sortColumn,
//                   sortDirection);
//      }
//    }
//  }
//
//  @SuppressWarnings("unchecked")
//  private void doTestSort(ReagentsDataModel dataModel,
//                          TableColumn<Reagent,?> sortColumn,
//                          SortDirection sortDirection)
//  {
//    log.info("testing sort on " + sortColumn + " in " + sortDirection);
//    dataModel.sort(sortColumn, sortDirection);
//    List<Comparable> actualSortedValues = new ArrayList<Comparable>();
//    List<Comparable> expectedSortedValues = new ArrayList<Comparable>();
//    for (int i = 0; i < 10; i++) {
//      dataModel.setRowIndex(i);
//      Reagent rowData = dataModel.getRowData();
//      assertNotNull("row data not null for row " + i + ", column " + sortColumn,
//                    rowData);
//      actualSortedValues.add((Comparable) sortColumn.getCellValue(rowData));
//    }
//    assertEquals("row count", 10, actualSortedValues.size());
//    expectedSortedValues.addAll(actualSortedValues);
//    Collections.sort(expectedSortedValues);
//    if (sortDirection == SortDirection.DESCENDING) {
//      Collections.reverse(expectedSortedValues);
//    }
//    assertEquals("sorted values on " + sortColumn.getName() + ", " + sortDirection,
//                 expectedSortedValues,
//                 actualSortedValues);
//  }

}
