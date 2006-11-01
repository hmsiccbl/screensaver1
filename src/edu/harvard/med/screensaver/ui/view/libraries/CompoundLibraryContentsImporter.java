// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.view.libraries;

import java.io.File;
import java.io.IOException;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import org.apache.log4j.Logger;
import org.apache.myfaces.custom.fileupload.UploadedFile;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.io.libraries.compound.SDFileCompoundLibraryContentsParser;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;

/**
 * The JSF backing bean for the compoundLibraryContentsImporter subview.
 * 
 * @author s
 */
public class CompoundLibraryContentsImporter extends AbstractBackingBean
{
  
  private static Logger log = Logger.getLogger(CompoundLibraryContentsImporter.class);

  // instance data

  private DAO _dao;
  private SDFileCompoundLibraryContentsParser _compoundLibraryContentsParser;
  private LibraryViewer _libraryViewer;
  private UploadedFile _uploadedFile;
  private Library _library;
  

  // backing bean property getter and setter methods

  public DAO getDao()
  {
    return _dao;
  }

  public void setDao(DAO dao)
  {
    _dao = dao;
  }

  public SDFileCompoundLibraryContentsParser getCompoundLibraryContentsParser()
  {
    return _compoundLibraryContentsParser;
  }

  public void setCompoundLibraryContentsParser(
    SDFileCompoundLibraryContentsParser compoundLibraryContentsParser)
  {
    _compoundLibraryContentsParser = compoundLibraryContentsParser;
  }

  public LibraryViewer getLibraryViewer()
  {
    return _libraryViewer;
  }

  public void setLibraryViewer(LibraryViewer libraryViewer)
  {
    _libraryViewer = libraryViewer;
    _library = _libraryViewer.getLibrary();
    _uploadedFile = null;
    _compoundLibraryContentsParser.clearErrors();
  }

  public void setUploadedFile(UploadedFile uploadedFile)
  {
    _uploadedFile = uploadedFile;
  }

  public UploadedFile getUploadedFile()
  {
    return _uploadedFile;
  }

  public Library getLibrary()
  {
    return _library;
  }

  public void setLibrary(Library library)
  {
    _library = library;
  }

  public DataModel getImportErrors()
  {
    return new ListDataModel(_compoundLibraryContentsParser.getErrors());
  }


  // JSF application methods
  
  public String update()
  {
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public String done()
  {
    return DONE_ACTION_RESULT;
  }
  
  public String submit()
  {
    try {
      if (_uploadedFile != null && _uploadedFile.getInputStream().available() > 0) {
        _dao.doInTransaction(new DAOTransaction() {
          public void runTransaction()
          {
            try {
              _compoundLibraryContentsParser.parseLibraryContents(
                _library,
                new File(_uploadedFile.getName()),
                _uploadedFile.getInputStream());
              _dao.persistEntity(_library);
            }
            catch (IOException e) {
              reportSystemError(e);
            }
          }
        });
      }
      else {
        showMessage("badUploadedFile", _uploadedFile.getName());
        return REDISPLAY_PAGE_ACTION_RESULT;
      }

      if (_compoundLibraryContentsParser.getHasErrors()) {
        return ERROR_ACTION_RESULT;
      }
      else {
        showMessage("libraries.importedLibraryContents", "libraryViewer");
        return SUCCESS_ACTION_RESULT;
      }
    }
    catch (IOException e) {
      reportSystemError(e);
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
  }
}

  