//$HeadURL$
//$Id$

//Copyright © 2006, 2010 by the President and Fellows of Harvard College.

//Screensaver is an open-source project developed by the ICCB-L and NSRB labs
//at Harvard Medical School. This software is distributed under the terms of
//the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.searchresults;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.io.TableDataExporter;
import edu.harvard.med.screensaver.io.libraries.smallmolecule.StructureImageProvider;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;
import edu.harvard.med.screensaver.util.CSVPrintWriter;
import edu.harvard.med.screensaver.util.DevelopmentException;

public class CsvDataExporter<T> implements TableDataExporter<T>
{
  private static Logger log = Logger.getLogger(CsvDataExporter.class);

  public static final String FORMAT_NAME = "CSV";
  public static final String FORMAT_MIME_TYPE = "text/csv";
  public static final String FILE_EXTENSION = ".csv";

  private String _dataTypeName;
  private List<TableColumn<T,?>> _columns;

  private StructureImageProvider _structureImageProvider;


  public CsvDataExporter(String dataTypeName)
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
    CSVPrintWriter csvWriter = null;
    try {
      File file = File.createTempFile("screensaver", "csv");
      Writer writer = new BufferedWriter(new FileWriter(file));
      csvWriter = new CSVPrintWriter(writer, "\n");
      writeFile(csvWriter, iter);
      csvWriter.close();
      return new BufferedInputStream(new FileInputStream(file));
    }
    catch (Exception e) {
      if (e instanceof IOException) {
        throw (IOException) e;
      }
      throw new DevelopmentException(e.getMessage());
    }
    finally {
      if (csvWriter != null) {
        IOUtils.closeQuietly(csvWriter);
      }
    }
  }

  @Override
  public String getFileName()
  {
    return _dataTypeName + FILE_EXTENSION; 
  }

  @Override
  public String getFormatName()
  {
    return FORMAT_NAME;
  }

  @Override
  public String getMimeType()
  {
    return FORMAT_MIME_TYPE;
  }

  private void writeFile(CSVPrintWriter writer, Iterator<T> iter)
    throws IOException
  {
    writeHeaders(writer);
    int i = 0;
    while (iter.hasNext()) {
      writeRow(i, writer, iter.next());
      ++i;
      if (i % 1000 == 0) {
        if (log.isDebugEnabled()) {
          log.debug("wrote " + i + " rows");
        }
      }
    }
  }

  private void writeRow(int i, CSVPrintWriter writer, T datum)
    throws IOException
  {
    for (TableColumn<T,?> column : _columns) {
      writer.print(column.getCellValue(datum));
    }
    writer.println();
  }

  private void writeHeaders(CSVPrintWriter writer)
  {
    for (TableColumn<T,?> column : _columns) {
      writer.print(column.getName());
    }
    writer.println();
  }
}


