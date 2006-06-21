// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * Encapsulates the instantiation of an HSSFWorkbook from a file. Also allows
 * the workbook to be associated with the filename it originated from.
 * 
 * @motivation HSSFWorkbook does not store the file whence it orignated
 * @author ant
 */
public class Workbook
{
  private File _workbookFile;
  private HSSFWorkbook _workbook;
  

  public Workbook(File workbookFile) throws IOException, FileNotFoundException
  {
    _workbookFile = workbookFile;
    InputStream inputStream = new FileInputStream(_workbookFile);
    // TODO: do we need this check?
    if (inputStream == null) {
      throw new FileNotFoundException("could not find file " + _workbookFile);
    }
    POIFSFileSystem dataFs = new POIFSFileSystem(new BufferedInputStream(inputStream));
    _workbook = new HSSFWorkbook(dataFs);
  }
  
  public HSSFWorkbook getWorkbook()
  {
    return _workbook;
  }
  
  public File getWorkbookFile()
  {
    return _workbookFile;
  }
  
  public String toString()
  {
    return _workbookFile.getName();
  }
  
}
