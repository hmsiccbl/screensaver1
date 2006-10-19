// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
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

import org.apache.log4j.Logger;
import org.apache.myfaces.custom.fileupload.UploadedFile;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.io.libraries.rnai.RNAiLibraryContentsParser;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.ui.AbstractController;
import edu.harvard.med.screensaver.ui.util.JSFUtils;

/**
 * The JSF backing bean for the rnaiLibraryContentsImporter subview.
 * 
 * @author s
 */
public class RNAiLibraryContentsImporterController extends AbstractController
{
  
  private static Logger log = Logger.getLogger(RNAiLibraryContentsImporterController.class);

  // instance data

  private DAO _dao;
  private RNAiLibraryContentsParser _rnaiLibraryContentsParser;
  private LibraryViewerController _libraryViewer;
  private UploadedFile _uploadedFile;
  private Library _library;
  private SilencingReagentType _silencingReagentType =
    RNAiLibraryContentsParser.DEFAULT_SILENCING_REAGENT_TYPE;


  // backing bean property getter and setter methods

  public DAO getDao()
  {
    return _dao;
  }

  public void setDao(DAO dao)
  {
    _dao = dao;
  }

  public RNAiLibraryContentsParser getRnaiLibraryContentsParser()
  {
    return _rnaiLibraryContentsParser;
  }

  public void setRnaiLibraryContentsParser(RNAiLibraryContentsParser rnaiLibraryContentsParser)
  {
    _rnaiLibraryContentsParser = rnaiLibraryContentsParser;
  }

  public LibraryViewerController getLibraryViewer()
  {
    return _libraryViewer;
  }

  public void setLibraryViewer(LibraryViewerController libraryViewer)
  {
    _libraryViewer = libraryViewer;
    _library = _libraryViewer.getLibrary();
    _uploadedFile = null;
    _rnaiLibraryContentsParser.clearErrors();
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

  public DataModel getImportErrors()
  {
    return new ListDataModel(_rnaiLibraryContentsParser.getErrors());
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
        _rnaiLibraryContentsParser.setSilencingReagentType(_silencingReagentType);
        _rnaiLibraryContentsParser.parseLibraryContents(
          _library,
          new File(_uploadedFile.getName()), _uploadedFile.getInputStream());
        _dao.persistEntity(_library);
      }
      else {
        showMessage("badUploadedFile", _uploadedFile.getName());
        return REDISPLAY_PAGE_ACTION_RESULT;
      }

      if (_rnaiLibraryContentsParser.getHasErrors()) {
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
