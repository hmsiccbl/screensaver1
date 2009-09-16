//$HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
//$Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $

//Copyright 2006 by the President and Fellows of Harvard College.

//Screensaver is an open-source project developed by the ICCB-L and NSRB labs
//at Harvard Medical School. This software is distributed under the terms of
//the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import edu.harvard.med.screensaver.io.TableDataExporter;
import edu.harvard.med.screensaver.io.workbook2.Workbook2Utils;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.ui.table.model.DataTableModel;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class GenericDataExporter<T> implements TableDataExporter<T> 
{
  private static Logger log = Logger.getLogger(GenericDataExporter.class);

  public static final String FORMAT_NAME = "Excel Workbook";
  public static final String FORMAT_MIME_TYPE = "application/excel";
  public static final String FILE_EXTENSION = ".xls";
  private static final int HEADER_ROW_INDEX = 0;

  private static final int MAX_DATA_ROWS = 65536 - 1;

  private String _dataTypeName;
  private List<TableColumn<T,?>> _columns;


  public GenericDataExporter(String dataTypeName)
  {
    _dataTypeName = dataTypeName;
  }

  public void setTableColumns(List<TableColumn<T,?>> columns)
  {
    _columns = columns;
  }

  public InputStream export(DataTableModel<T> model)
    throws IOException
  {
    assert _columns != null : "must call setTableColumns() first";
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      WritableWorkbook workbook = Workbook.createWorkbook(out);
      writeWorkbook(workbook, model);
      workbook.write();
      workbook.close();
      out.close();
      return new ByteArrayInputStream(out.toByteArray());
    }
    catch (Exception e) {
      if (e instanceof IOException) {
        throw (IOException) e;
      }
      throw new IOException(e.getMessage());
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
                             DataTableModel<T> model) 
    throws RowsExceededException, WriteException
  {
    WritableSheet sheet = null;
    for (int i = 0; i < model.getRowCount(); ++i) {
      if (i % MAX_DATA_ROWS == 0) {
        sheet = createSheet(workbook);
      }
      writeRow(i, sheet, model);
    }
  }

  private void writeRow(int i, WritableSheet sheet, DataTableModel<T> model)
    throws WriteException, RowsExceededException
  {
    model.setRowIndex(i);
    T entity = (T) model.getRowData();
    int colIndex = 0;
    for (TableColumn<T,?> column : _columns) {
      Workbook2Utils.writeCell(sheet, (i % MAX_DATA_ROWS) + 1, colIndex++, column.getCellValue(entity));
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

  private void writeHeaders(WritableSheet sheet) throws RowsExceededException, WriteException
  {
    int colIndex = 0;
    for (TableColumn<T,?> column : _columns) {
      Workbook2Utils.writeCell(sheet, HEADER_ROW_INDEX, colIndex++, column.getName());
    }
  }
}


