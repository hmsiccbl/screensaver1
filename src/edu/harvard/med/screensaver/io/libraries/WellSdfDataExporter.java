// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/io/libraries/WellsSdfDataExporter.java $
// $Id: WellsSdfDataExporter.java 2267 2008-03-28 19:57:42Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.io.DataExporter;
import edu.harvard.med.screensaver.model.libraries.Well;
import org.apache.log4j.Logger;

public class WellSdfDataExporter implements DataExporter<Well>
{
  // static members

  private static Logger log = Logger.getLogger(WellSdfDataExporter.class);


  // instance data members

  private GenericEntityDAO _dao;
  private WellsSdfDataExporter _wellsSdfDataExporter;


  // public constructors and methods

  public WellSdfDataExporter(GenericEntityDAO dao)
  {
    _dao = dao;
    _wellsSdfDataExporter = new WellsSdfDataExporter(dao);
  }

  public InputStream export(Well well)
  {
    Set<String> keys = new HashSet<String>();
    keys.add(well.getWellKey().toString());
    return _wellsSdfDataExporter.export(keys);
  }

  public String getFileName()
  {
    return _wellsSdfDataExporter.getFileName();
  }

  public String getFormatName()
  {
    return _wellsSdfDataExporter.getFormatName();
  }

  public String getMimeType()
  {
    return _wellsSdfDataExporter.getMimeType();
  }


  // private methods

}
