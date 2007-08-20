// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import edu.harvard.med.screensaver.io.libraries.compound.SDFileCompoundLibraryContentsParser;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.control.LibrariesController;

import org.apache.log4j.Logger;
import org.apache.myfaces.custom.fileupload.UploadedFile;

/**
 * The JSF backing bean for the compoundLibraryContentsImporter subview.
 * 
 * @author s
 */
public class CompoundLibraryContentsImporter extends AbstractBackingBean
{
  
  private static Logger log = Logger.getLogger(CompoundLibraryContentsImporter.class);

  // instance data

  private LibrariesController _librariesController;
  private SDFileCompoundLibraryContentsParser _compoundLibraryContentsParser;
  private UploadedFile _uploadedFile;
  private Library _library;
  

  // backing bean property getter and setter methods

  public LibrariesController getLibrariesController()
  {
    return _librariesController;
  }
  
  public void setLibrariesController(LibrariesController librariesController)
  {
    _librariesController = librariesController;
  }
  
  public SDFileCompoundLibraryContentsParser getCompoundLibraryContentsParser()
  {
    return _compoundLibraryContentsParser;
  }

  public void setCompoundLibraryContentsParser(SDFileCompoundLibraryContentsParser compoundLibraryContentsParser)
  {
    _compoundLibraryContentsParser = compoundLibraryContentsParser;
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

  public boolean getHasErrors()
  {
    return _compoundLibraryContentsParser.getHasErrors();
  }
  
  public DataModel getImportErrors()
  {
    return new ListDataModel(_compoundLibraryContentsParser.getErrors());
  }

  public String viewLibrary()
  {
    return _librariesController.viewLibrary(_library, null);
  }

  /**
   * Parse the compound library contents from the file, and go to the appropriate next page
   * depending on the result.
   * @return the control code for the appropriate next page
   */
  public String importCompoundLibraryContents()
  {
    return _librariesController.importCompoundLibraryContents(_library, _uploadedFile);
  }
}

  