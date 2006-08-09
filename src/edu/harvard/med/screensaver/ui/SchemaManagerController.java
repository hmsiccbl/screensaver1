// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import edu.harvard.med.screensaver.db.SchemaUtil;
import edu.harvard.med.screensaver.db.screendb.ScreenDBDataImporter;

public class SchemaManagerController extends AbstractController
{
  private SchemaUtil _schemaUtil;
  private ScreenDBDataImporter _screenDBDataImporter;

  
  // getters and setters
  
  public SchemaUtil getSchemaUtil()
  {
    return _schemaUtil;
  }

  public void setSchemaUtil(SchemaUtil schemaUtil)
  {
    _schemaUtil = schemaUtil;
  }

  public ScreenDBDataImporter getScreenDBDataImporter()
  {
    return _screenDBDataImporter;
  }

  public void setScreenDBDataImporter(ScreenDBDataImporter screenDBDataImporter)
  {
    _screenDBDataImporter = screenDBDataImporter;
  }


  // JSF application methods

  public void dropSchema()
  {
    _schemaUtil.dropSchema();
  }
  
  public void createSchema()
  {
    _schemaUtil.createSchema();
  }
  
  public void truncateTables()
  {
    _schemaUtil.truncateTablesOrCreateSchema();
  }
  
  public void loadScreenDB()
  {
    _screenDBDataImporter.loadScreenDBData();
  }
}
