// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.io.InputStream;
import java.util.Collections;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.db.datafetcher.EntityDataFetcher;
import edu.harvard.med.screensaver.io.DataExporter;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.ui.table.Criterion;
import edu.harvard.med.screensaver.ui.table.Criterion.Operator;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;

import org.apache.log4j.Logger;

public class GenericDataExporterTest extends AbstractSpringPersistenceTest
{
  // static members

  private static Logger log = Logger.getLogger(GenericDataExporterTest.class);


  // instance data members
  
  
  // public constructors and methods
  
  public void testGenericDataExporter() throws Exception
  {
    final Library library = MakeDummyEntities.makeDummyLibrary(1, ScreenType.SMALL_MOLECULE, 1);
    genericEntityDao.persistEntity(library);
  
    WellSearchResults wellSearchResults = new WellSearchResults(genericEntityDao, null, null, null, null, Collections.<DataExporter<Well,String>>emptyList());
    GenericDataExporter<Well,String> exporter = (GenericDataExporter<Well,String>) wellSearchResults.getDataExporters().get(0);
    wellSearchResults.searchAllWells();
    
    TableColumn<Well,String> wellColumn = (TableColumn<Well,String>) wellSearchResults.getColumnManager().getColumn("Well");
    wellColumn.addCriterion(new Criterion<String>(Operator.TEXT_STARTS_WITH, "B"));
    wellSearchResults.getColumnManager().setSortColumn(wellColumn);
    //wellSearchResults.getColumnManager().setSortDirection(SortDirection.DESCENDING); // TODO: descending sort order not yet supported by DataFetcher interface
    wellSearchResults.getColumnManager().getColumn("Library").setVisible(false);
    wellSearchResults.getColumnManager().getColumn("Compounds SMILES").setVisible(true);
    wellSearchResults.getColumnManager().getColumn("PubChem CIDs").setVisible(true);
    exporter.setTableColumns(wellSearchResults.getColumnManager().getVisibleColumns());
    wellSearchResults.getRowCount(); // necessary to force dataFetcher to be re-initialized
    EntityDataFetcher<Well,String> dataFetcher = wellSearchResults.getDataFetcher();
    InputStream exportedData = exporter.export(dataFetcher);
    Workbook workbook = Workbook.getWorkbook(exportedData);
    Sheet sheet = workbook.getSheet(0);
    Cell[] row = sheet.getRow(0);
    assertEquals("row count", 24 + 1, sheet.getRows());
    assertEquals("column 0 header", "Plate", row[0].getContents()); 
    assertEquals("column 1 header", "Well", row[1].getContents());
    assertEquals("column 2 header", "Well Type", row[2].getContents());
    assertEquals("column 3 header", "Reagent Source ID", row[3].getContents());
    assertEquals("column 4 header", "Compounds SMILES", row[4].getContents()); 
    assertEquals("column 5 header", "PubChem CIDs", row[5].getContents()); 
    for (int rowIndex = 1; rowIndex <= 24; ++rowIndex) {
      assertEquals("filtered, sorted well column desc; rowIndex=" + rowIndex,
                   String.format("B%02d", rowIndex),
                   sheet.getCell(1, rowIndex).getContents());
    }
  }

  // private methods

}
