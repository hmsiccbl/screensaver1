// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public interface DataExporter<T>
{
  public static final String LIST_DELIMITER = ";";

  public String getFormatName();
  public String getMimeType();
  public String getFileName();

  public InputStream export(Iterator<T> iterator) throws IOException;
}

