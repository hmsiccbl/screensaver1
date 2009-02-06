// $HeadURL: http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/iccbl/trunk/src/edu/harvard/med/screensaver/ui/libraries/LibraryViewer.java $
// $Id: LibraryViewer.java 2825 2008-11-03 18:56:46Z atolopko $
//
// Copyright 2008 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.log4j.Logger;
import org.apache.myfaces.custom.fileupload.UploadedFile;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.io.libraries.rnai.RNAiLibraryContentsParser;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;

/**
 * A thread spawned to upload library content so that user does not have to wait for
 * the upload to complete (This may also result in proxy error if waiting for too long).
 * 
 * @author <a mailto="voonkl@bii.a-star.edu.sg">Kian Loon Voon</a>
 */
public class LibraryUploadThread extends Thread {
  
  // static members
  private static Logger log = Logger.getLogger(LibraryUploadThread.class);

  // instance data members
  private GenericEntityDAO _dao;
  private RNAiLibraryContentsParser _rnaiLibraryContentsParser;
  private Library _library;
  private SilencingReagentType _silencingReagentType;
  private UploadedFile _uploadedFile;
  private String _userEmail;
  
  private int _status;
    
  // public constructors and methods
  public LibraryUploadThread() {
    _status = ScreensaverConstants.LIBRARY_UPLOAD_IDLE;
  }
  
  public LibraryUploadThread(GenericEntityDAO dao, RNAiLibraryContentsParser libraryContentsParser, Library library, 
                             SilencingReagentType reagentType, UploadedFile file, String email) {
    super();
    _dao = dao;
    _rnaiLibraryContentsParser = libraryContentsParser;
    _library = library;
    _silencingReagentType = reagentType;
    _uploadedFile = file;
    _userEmail = email;
    _status = ScreensaverConstants.LIBRARY_UPLOAD_IDLE;
  }


  public int getStatus() {
    return _status;
  }
  
  public void run() {
    _status = ScreensaverConstants.LIBRARY_UPLOAD_RUNNING; // process running

    _dao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        try {
          Library library = _dao.reloadEntity(_library);
          _rnaiLibraryContentsParser
              .setSilencingReagentType(_silencingReagentType);
          _rnaiLibraryContentsParser.parseLibraryContents(library,
              new File(_uploadedFile.getName()), _uploadedFile
                  .getInputStream(), null, null);
          _dao.saveOrUpdateEntity(library);
        } catch (Exception e) { // supposed to be IOException
          _status = ScreensaverConstants.LIBRARY_UPLOAD_FAILED; // error occurred
          
          StringWriter sw = new StringWriter();
          PrintWriter pw = new PrintWriter(sw);
          e.printStackTrace(pw);
          
          String error_message = "Library content upload FAILED.\n\n"+sw.toString();
          //EmailSender.send(_userEmail,"Library Content Upload FAILED",error_message);
          
          throw new DAOTransactionRollbackException(
              "could not access uploaded file", e);
        }
      }
    });
    _status = ScreensaverConstants.LIBRARY_UPLOAD_SUCCESSFULL; // process completed successfully
    String success_message = "Library content upload SUCCESSFUL. Please login to Screensaver to check the results.\n";
    //EmailSender.send(_userEmail,"Library Content Upload SUCCESSFUL",success_message);
    
  }
}
