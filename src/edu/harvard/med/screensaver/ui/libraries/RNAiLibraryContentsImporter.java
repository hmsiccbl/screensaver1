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
import java.util.ArrayList;
import java.util.List;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.io.libraries.ParseLibraryContentsException;
import edu.harvard.med.screensaver.io.libraries.rnai.RNAiLibraryContentsParser;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.util.JSFUtils;

import org.apache.log4j.Logger;
import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.springframework.dao.DataAccessException;

/**
 * The JSF backing bean for the rnaiLibraryContentsImporter subview.
 *
 * @author s
 */
public class RNAiLibraryContentsImporter extends AbstractBackingBean
{

  private static Logger log = Logger.getLogger(RNAiLibraryContentsImporter.class);

  // instance data

  private GenericEntityDAO _dao;
  private LibraryViewer _libraryViewer;
  private RNAiLibraryContentsParser _rnaiLibraryContentsParser;

  private UploadedFile _uploadedFile;
  private Library _library;
  private SilencingReagentType _silencingReagentType =
    RNAiLibraryContentsParser.DEFAULT_SILENCING_REAGENT_TYPE;


  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected RNAiLibraryContentsImporter()
  {
  }

  public RNAiLibraryContentsImporter(GenericEntityDAO dao,
                                     LibraryViewer libraryViewer,
                                     RNAiLibraryContentsParser rnaiLibraryContentsParser)
  {
    _dao = dao;
    _libraryViewer = libraryViewer;
    _rnaiLibraryContentsParser = rnaiLibraryContentsParser;
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
    _rnaiLibraryContentsParser.clearErrors();
  }

  /**
   * Get the silencingReagentType.
   * @return the silencingReagentType
   */
  public SilencingReagentType getSilencingReagentType()
  {
    return _silencingReagentType;
  }

  /**
   * Set the silencingReagentType.
   * @param silencingReagentType the silencingReagentType
   */
  public void setSilencingReagentType(SilencingReagentType silencingReagentType)
  {
    _silencingReagentType = silencingReagentType;
  }

  public List<SelectItem> getSilencingReagentTypeSelections()
  {
    List<SilencingReagentType> selections = new ArrayList<SilencingReagentType>();
    for (SilencingReagentType silencingReagentType : SilencingReagentType.values()) {
      selections.add(silencingReagentType);
    }
    return JSFUtils.createUISelectItems(selections);
  }

  public boolean getHasErrors()
  {
    return _rnaiLibraryContentsParser.getHasErrors();
  }

  public DataModel getImportErrors()
  {
    return new ListDataModel(_rnaiLibraryContentsParser.getErrors());
  }

  public String viewLibrary()
  {
    return _libraryViewer.viewLibrary(_library);
  }

  @UIControllerMethod
  public String viewRNAiLibraryContentsImporter(Library library)
  {
    setLibrary(library);
    return IMPORT_RNAI_LIBRARY_CONTENTS;
  }

  /**
   * Parse the RNAi library contents from the file, and go to the appropriate next page
   * depending on the result.
   * @return the control code for the appropriate next page
   */
  @UIControllerMethod
  public String importRNAiLibraryContents()
  {
    try {
      if (_uploadedFile == null || _uploadedFile.getInputStream().available() == 0) {
        showMessage("badUploadedFile", _uploadedFile.getName());
        return IMPORT_RNAI_LIBRARY_CONTENTS;
      }
      _dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          try {
            Library library = _dao.reloadEntity(_library);
            _rnaiLibraryContentsParser.setSilencingReagentType(_silencingReagentType);
            _rnaiLibraryContentsParser.parseLibraryContents(library,
                                                            new File(_uploadedFile.getName()),
                                                            _uploadedFile.getInputStream());
            _dao.saveOrUpdateEntity(library);
          }
          catch (IOException e) {
            throw new DAOTransactionRollbackException("could not access uploaded file", e);
          }
        }
      });
      showMessage("libraries.importedLibraryContents", "libraryViewer");
      // TODO: to be correct, we should regen the search results, though I don't think anything in the results would actually be different after this import
      return _libraryViewer.viewLibrary(_library);
    }
    catch (ParseLibraryContentsException e) {
      return IMPORT_RNAI_LIBRARY_CONTENTS;
    }
    catch (DataAccessException e) {
      // TODO: should reload library and goto library viewer
      reportSystemError(e);
      return IMPORT_RNAI_LIBRARY_CONTENTS;
    }
    catch (Exception e) {
      reportApplicationError(e);
      return IMPORT_RNAI_LIBRARY_CONTENTS;
    }
  }
}
