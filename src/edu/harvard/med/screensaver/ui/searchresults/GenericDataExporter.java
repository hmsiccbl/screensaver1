//$HeadURL$
//$Id$

//Copyright © 2006, 2010 by the President and Fellows of Harvard College.

//Screensaver is an open-source project developed by the ICCB-L and NSRB labs
//at Harvard Medical School. This software is distributed under the terms of
//the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import jxl.CellView;
import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.io.TableDataExporter;
import edu.harvard.med.screensaver.io.libraries.smallmolecule.StructureImageProvider;
import edu.harvard.med.screensaver.io.workbook2.Workbook2Utils;
import edu.harvard.med.screensaver.ui.table.column.ColumnType;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.util.DevelopmentException;

public class GenericDataExporter<T> implements TableDataExporter<T> 
{
  private static Logger log = Logger.getLogger(GenericDataExporter.class);

  public static final String FORMAT_NAME = "Excel Workbook";
  public static final String FORMAT_MIME_TYPE = "application/excel";
  public static final String FILE_EXTENSION = ".xls";
  private static final int HEADER_ROW_INDEX = 0;
  private static final int MAX_DATA_ROWS = 65536 - 1;
  private static final int IMAGE_CELL_HEIGHT_2IN = 2880; // empirically determined value! see jxl.CellView#setSize()
  private static final int IMAGE_CELL_WIDTH_2IN = 6642;  // empirically determined value! see jxl.CellView#setSize()

  private String _dataTypeName;
  private List<TableColumn<T,?>> _columns;

  private StructureImageProvider _structureImageProvider;


  public GenericDataExporter(String dataTypeName)
  {
    _dataTypeName = dataTypeName;
  }

  public void setTableColumns(List<TableColumn<T,?>> columns)
  {
    _columns = columns;
  }

  @Override
  public InputStream export(Iterator<T> iter) throws IOException
  {
    assert _columns != null : "must call setTableColumns() first";
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      WritableWorkbook workbook = Workbook.createWorkbook(out);
      writeWorkbook(workbook, iter);
      workbook.write();
      workbook.close();
      out.close();
      return new ByteArrayInputStream(out.toByteArray());
    }
    catch (Exception e) {
      if (e instanceof IOException) {
        throw (IOException) e;
      }
      throw new DevelopmentException(e.getMessage());
    }
    finally {
      IOUtils.closeQuietly(out);
    }
  }

  public String getFileName()
  {
    return _dataTypeName + FILE_EXTENSION; 
  }

  public String getFormatName()
  {
    return FORMAT_NAME;
  }

  public String getMimeType()
  {
    return FORMAT_MIME_TYPE;
  }

  private void writeWorkbook(WritableWorkbook workbook, 
                             Iterator<T> iter)
    throws RowsExceededException, WriteException
  {
    WritableSheet sheet = null;
    int i = 0;
    while (iter.hasNext()) {
      if (i % MAX_DATA_ROWS == 0) {
        sheet = createSheet(workbook);
      }
      writeRow(i, sheet, iter.next());
      ++i;
      if (i % 1000 == 0) {
        if (log.isDebugEnabled()) {
          log.debug("wrote " + i + " rows to workbook");
        }
      }
    }
  }

  private void writeRow(int i, WritableSheet sheet, T datum)
    throws WriteException, RowsExceededException
  {
    int rowIndex = (i % MAX_DATA_ROWS) + 1;
    int colIndex = 0;
    for (TableColumn<T,?> column : _columns) {
      adjustCellSizes(sheet, rowIndex, colIndex);
      if (column.getColumnType() == ColumnType.IMAGE) {
        if (column.getCellValue(datum) != null) {
          try {
            byte[] imageData = IOUtils.toByteArray(new URL(column.getCellValue(datum).toString()).openStream());
            Workbook2Utils.writeImage(sheet, rowIndex, colIndex, imageData);
          }
          catch (Exception e) {
            Workbook2Utils.writeCell(sheet, rowIndex, colIndex, "<error: bad image source: " + column.getCellValue(datum) + ">");
          }
        }
      }
      else {
        Workbook2Utils.writeCell(sheet, rowIndex, colIndex, column.getCellValue(datum));
      }
      colIndex++;
    } 
  }

  private WritableSheet createSheet(WritableWorkbook workbook)
    throws RowsExceededException, WriteException
  {
      int nextSheetIndex = workbook.getNumberOfSheets();
      WritableSheet sheet = workbook.createSheet("data" + (nextSheetIndex + 1), nextSheetIndex);
      writeHeaders(sheet);
      return sheet;
  }

  private void adjustCellSizes(WritableSheet sheet, int rowIndex, int colIndex) throws RowsExceededException
  {
    TableColumn<T,?> column = _columns.get(colIndex);
    if (column.getColumnType() == ColumnType.IMAGE) {
      CellView colCellView = new CellView();
      colCellView.setSize(IMAGE_CELL_WIDTH_2IN);
      sheet.setColumnView(colIndex, colCellView);
      CellView rowCellView = new CellView();
      rowCellView.setSize(IMAGE_CELL_HEIGHT_2IN);
      sheet.setRowView(rowIndex, rowCellView);
    }
  }

  private void writeHeaders(WritableSheet sheet) throws RowsExceededException, WriteException
  {
    int colIndex = 0;
    for (TableColumn<T,?> column : _columns) {
      Workbook2Utils.writeCell(sheet, HEADER_ROW_INDEX, colIndex++, column.getName());
    }
  }
}


