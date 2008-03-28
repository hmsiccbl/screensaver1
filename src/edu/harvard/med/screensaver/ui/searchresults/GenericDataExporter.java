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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import edu.harvard.med.screensaver.db.datafetcher.DataFetcher;
import edu.harvard.med.screensaver.db.datafetcher.EntityDataFetcher;
import edu.harvard.med.screensaver.io.TableDataExporter;
import edu.harvard.med.screensaver.io.workbook2.Workbook2Utils;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class GenericDataExporter<T extends AbstractEntity,K> implements TableDataExporter<T,K> 
{
  // static members

  private static Logger log = Logger.getLogger(GenericDataExporter.class);

  public static final String FORMAT_NAME = "Excel Workbook";
  public static final String FORMAT_MIME_TYPE = "application/excel";
  public static final String FILE_EXTENSION = ".xls";

  private static final int HEADER_ROW_INDEX = 0;
  private static final int FETCH_BATCH_SIZE = 256;


  // instance data members

  private String _dataTypeName;
  private List<TableColumn<T,?>> _columns;


  // public constructors and methods

  public GenericDataExporter(String dataTypeName)
  {
    _dataTypeName = dataTypeName;
  }

  public void setTableColumns(List<TableColumn<T,?>> columns)
  {
    _columns = columns;
  }

  public InputStream export(EntityDataFetcher<T,K> dataFetcher)
    throws IOException
  {
    assert _columns != null : "must call setTableColumns() first";
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      WritableWorkbook workbook = Workbook.createWorkbook(out);
      writeWorkbook(workbook, dataFetcher);
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

  
  // private methods
  
  private void writeWorkbook(WritableWorkbook workbook, 
                             DataFetcher<T,K,?> dataFetcher) 
    throws RowsExceededException, WriteException
  {
    WritableSheet sheet = workbook.createSheet("data", 0);
    writeHeaders(sheet);
    writeData(sheet, dataFetcher);
  }

  private void writeData(WritableSheet sheet, 
                         DataFetcher<T,K,?> dataFetcher) throws RowsExceededException, WriteException
  {
    List<K> keys = dataFetcher.findAllKeys();
    int i = 0;
    List<K> batchOfKeysToFetchList = new ArrayList<K>();
    Set<K> batchOfKeysToFetchSet = new HashSet<K>();
    while (i < keys.size()) {
      batchOfKeysToFetchList.clear();
      batchOfKeysToFetchSet.clear();
      batchOfKeysToFetchList.addAll(keys.subList(i, Math.min(i + FETCH_BATCH_SIZE, keys.size())));
      batchOfKeysToFetchSet.addAll(batchOfKeysToFetchList);
      Map<K,T> entities = dataFetcher.fetchData(batchOfKeysToFetchSet);
      for (K key : batchOfKeysToFetchList) {
        T entity = entities.get(key);
        int colIndex = 0;
        for (TableColumn<T,?> column : _columns) {
          Workbook2Utils.writeCell(sheet, i + 1, colIndex++, column.getCellValue(entity));
        }
        ++i;
      }
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


