// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.io.IOException;
import java.util.List;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.apache.myfaces.custom.fileupload.UploadedFile;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.io.ParseError;
import edu.harvard.med.screensaver.io.ParseErrorsException;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.service.libraries.LibraryContentsLoader;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.UICommand;

/**
 * JSF backing bean for the Library Contents Importer page.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class LibraryContentsImporter extends AbstractBackingBean
{

  private static Logger log = Logger.getLogger(LibraryContentsImporter.class);

  // instance data

  private GenericEntityDAO _dao;
  private LibraryViewer _libraryViewer;
  private UploadedFile _uploadedFile;
  private String _loadingComments;
  private Library _library;
  private LibraryContentsLoader _libraryContentsLoader;

  private List<? extends ParseError> _errors;


  /**
   * @motivation for CGLIB2
   */
  protected LibraryContentsImporter()
  {
  }

  public LibraryContentsImporter(GenericEntityDAO dao,
                                 LibraryViewer libraryViewer,
                                 LibraryContentsLoader libraryContentsLoader)
  {
    _dao = dao;
    _libraryViewer = libraryViewer;
    _libraryContentsLoader = libraryContentsLoader;
  }

  public void setUploadedFile(UploadedFile uploadedFile)
  {
    _uploadedFile = uploadedFile;
  }

  public UploadedFile getUploadedFile()
  {
    return _uploadedFile;
  }

  public String getLoadingComments()
  {
    return _loadingComments;
  }

  public void setLoadingComments(String loadingComments)
  {
    _loadingComments = loadingComments;
  }

  public Library getLibrary()
  {
    return _library;
  }

  public void setLibrary(Library library)
  {
    _library = library;
    setUploadedFile(null);
    _errors = null;
    _loadingComments = null;
  }

  public boolean getHasErrors()
  {
    return _errors != null && _errors.size() > 0; 
  }

  public DataModel getImportErrors()
  {
    return new ListDataModel(_errors==null ? Lists.newLinkedList() : _errors);
  }

  public String viewLibrary()
  {
    return _libraryViewer.viewEntity(_library);
  }

  @UICommand
  public String viewLibraryContentsImporter(Library library)
  {
    setLibrary(library);
    return IMPORT_LIBRARY_CONTENTS;
  }

  /**
   * Parse the compound library contents from the file, and go to the appropriate next page
   * depending on the result.
   * @return the control code for the appropriate next page
   */
  @UICommand
  public String importLibraryContents() throws IOException
  {
    _errors = null;
    try {
      if (_uploadedFile == null || _uploadedFile.getInputStream().available() == 0) {
        showMessage("badUploadedFile", _uploadedFile == null ? "<null>" : _uploadedFile.getName());
        return REDISPLAY_PAGE_ACTION_RESULT;
      }
      
      _libraryContentsLoader.loadLibraryContents(_library, 
                                                 (AdministratorUser) getScreensaverUser(),
                                                 _loadingComments,
                                                 _uploadedFile.getInputStream()); 
      
      showMessage("libraries.importedLibraryContents", "libraryViewer");
      // TODO: to be correct, we should regen the search results, though I don't think anything in the results would actually be different after this import
      return _libraryViewer.viewEntity(_library);
    } 
    catch (ParseErrorsException e) {
      _errors = e.getErrors();
      reportApplicationError("Import failed: encountered " + e.getErrors().size() + " error(s) during import");
      return IMPORT_LIBRARY_CONTENTS;
    }
  }
}

