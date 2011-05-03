// $HeadURL$
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.searchresults;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import com.google.common.base.Function;

import edu.harvard.med.screensaver.io.DataExporter;

public class DataExporterAdapter<F,T> implements DataExporter<F>
{
  private DataExporter<T> _dataExporter;
  private Function<F,T> _adapterFunction;

  public DataExporterAdapter(DataExporter<T> dataExporter,
                             Function<F,T> adapterFunction)
  {
    _dataExporter = dataExporter;
    _adapterFunction = adapterFunction;
  }

  @Override
  public InputStream export(final Iterator<F> fromIterator) throws IOException
  {
    Iterator<T> toIterator = new Iterator<T>() {
      @Override
      public boolean hasNext()
      {
        return fromIterator.hasNext();
      }

      @Override
      public T next()
      {
        return _adapterFunction.apply(fromIterator.next());
      }

      @Override
      public void remove()
      {
        throw new UnsupportedOperationException();
      }
    };

    return _dataExporter.export(toIterator);
  }

  @Override
  public String getFileName()
  {
    return _dataExporter.getFileName();
  }

  @Override
  public String getFormatName()
  {
    return _dataExporter.getFormatName();
  }

  @Override
  public String getMimeType()
  {
    return _dataExporter.getMimeType();
  }
}
