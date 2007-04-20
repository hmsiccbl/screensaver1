// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screens;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;

import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.apache.myfaces.custom.fileupload.UploadedFile;

/**
 * The JSF backing bean for both the screenResultImporter subview and
 * screenResultImportErrors view.
 * 
 * @author ant
 */
public class AllCherryPicksImporter extends AbstractBackingBean
{

  // static data
  
  private static final Logger log = Logger.getLogger(AllCherryPicksImporter.class);
  
  // static methods
  
  public static void main(String[] args) throws ParseException
  {
    CommandLineApplication app = new CommandLineApplication(args);
    app.processOptions(true, true);
  }

  
  // instance data

  private UploadedFile _uploadedFile;
  private edu.harvard.med.screensaver.io.cherrypicks.AllCherryPicksImporter _allCherryPicksImporter;


  // backing bean property getter and setter methods

  public void setAllCherryPicksImporter(edu.harvard.med.screensaver.io.cherrypicks.AllCherryPicksImporter allCherryPicksImporter) {
    _allCherryPicksImporter = allCherryPicksImporter;
  }

  public edu.harvard.med.screensaver.io.cherrypicks.AllCherryPicksImporter getAllCherryPicksImporter()
  {
    return _allCherryPicksImporter;
  }

  public void setUploadedFile(UploadedFile uploadedFile)
  {
    _uploadedFile = uploadedFile;
  }

  public UploadedFile getUploadedFile()
  {
    return _uploadedFile;
  }


  // JSF application methods
  
  public String importAllCherryPicks()
  {
    try {
      _allCherryPicksImporter.importCherryPickCopiesAndRnaiCherryPicks(_uploadedFile.getInputStream());
      showMessage("cherryPicks.importedAllCherryPicksFile");
    }
    catch (Exception e) {
      showMessage("cherryPicks.errorImportingAllCherryPicksFile", e.getMessage());
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
}
