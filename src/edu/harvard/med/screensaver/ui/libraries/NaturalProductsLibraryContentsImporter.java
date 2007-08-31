// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.io.File;
import java.io.IOException;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.io.libraries.compound.NaturalProductsLibraryContentsParser;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.UIControllerMethod;

import org.apache.log4j.Logger;
import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.springframework.dao.DataAccessException;

/**
 * The JSF backing bean for the naturalProductsLibraryContentsImporter subview.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class NaturalProductsLibraryContentsImporter extends AbstractBackingBean
{

  private static Logger log = Logger.getLogger(NaturalProductsLibraryContentsImporter.class);

  // instance data

  private GenericEntityDAO _dao;
  private LibraryViewer _libraryViewer;
  private NaturalProductsLibraryContentsParser _naturalProductsLibraryContentsParser;

  private UploadedFile _uploadedFile;
  private Library _library;


  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected NaturalProductsLibraryContentsImporter()
  {
  }

  public NaturalProductsLibraryContentsImporter(GenericEntityDAO dao,
                                                LibraryViewer libraryViewer,
                                                NaturalProductsLibraryContentsParser naturalProductsLibraryContentsParser)
  {
    _dao = dao;
    _libraryViewer = libraryViewer;
    _naturalProductsLibraryContentsParser = naturalProductsLibraryContentsParser;
  }


  // backing bean property getter and setter methods

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
    setUploadedFile(null);
    _naturalProductsLibraryContentsParser.clearErrors();
  }

  public boolean getHasErrors()
  {
    return _naturalProductsLibraryContentsParser.getHasErrors();
  }

  public DataModel getImportErrors()
  {
    return new ListDataModel(_naturalProductsLibraryContentsParser.getErrors());
  }

  @UIControllerMethod
  public String viewLibrary()
  {
    return _libraryViewer.viewLibrary(_library);
  }

  @UIControllerMethod
  public String viewNaturalProductsLibraryContentsImporter(Library library)
  {
    setLibrary(library);
    return IMPORT_NATURAL_PRODUCTS_LIBRARY_CONTENTS;
  }

  /**
   * Parse the natural products library contents from the file, and go to the appropriate next page
   * depending on the result.
   * @return the control code for the appropriate next page
   */
  @UIControllerMethod
  public String importNaturalProductsLibraryContents()
  {
    try {
      if (_uploadedFile == null || _uploadedFile.getInputStream().available() == 0) {
        showMessage("badUploadedFile", _uploadedFile.getName());
        return IMPORT_NATURAL_PRODUCTS_LIBRARY_CONTENTS;
      }
      _dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          try {
            Library library = _dao.reloadEntity(_library);
            _naturalProductsLibraryContentsParser.parseLibraryContents(
              library,
              new File(_uploadedFile.getName()),
              _uploadedFile.getInputStream());
            _dao.persistEntity(library);
          }
          catch (IOException e) {
            throw new DAOTransactionRollbackException("could not access uploaded file", e);
          }
        }
      });
      if (_naturalProductsLibraryContentsParser.getHasErrors()) {
        return IMPORT_NATURAL_PRODUCTS_LIBRARY_CONTENTS;
      }
      else {
        showMessage("libraries.importedLibraryContents", "libraryViewer");
        // TODO: to be correct, we should regen the search results, though I don't think anything in the results would actually be different after this import
        return _libraryViewer.viewLibrary(_library);
      }
    }
    catch (DataAccessException e) {
      // TODO: should reload library and goto library viewer
      reportSystemError(e);
      return IMPORT_NATURAL_PRODUCTS_LIBRARY_CONTENTS;
    }
    catch (IOException e) {
      reportSystemError(e);
      return IMPORT_NATURAL_PRODUCTS_LIBRARY_CONTENTS;
    }
  }


}

