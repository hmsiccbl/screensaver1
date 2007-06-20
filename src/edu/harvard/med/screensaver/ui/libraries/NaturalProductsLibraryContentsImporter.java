// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import org.apache.log4j.Logger;
import org.apache.myfaces.custom.fileupload.UploadedFile;

import edu.harvard.med.screensaver.io.libraries.compound.NaturalProductsLibraryContentsParser;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.control.LibrariesController;

/**
 * The JSF backing bean for the compoundLibraryContentsImporter subview.
 * 
 * @author s
 */
public class NaturalProductsLibraryContentsImporter extends AbstractBackingBean
{
  
  private static Logger log = Logger.getLogger(NaturalProductsLibraryContentsImporter.class);

  // instance data

  private LibrariesController _librariesController;
  private NaturalProductsLibraryContentsParser _naturalProductsLibraryContentsParser;
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
  
  public NaturalProductsLibraryContentsParser getNaturalProductsLibraryContentsParser()
  {
    return _naturalProductsLibraryContentsParser;
  }

  public void setNaturalProductsLibraryContentsParser(NaturalProductsLibraryContentsParser naturalProductsLibraryContentsParser)
  {
    _naturalProductsLibraryContentsParser = naturalProductsLibraryContentsParser;
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
    return _naturalProductsLibraryContentsParser.getHasErrors();
  }
  
  public DataModel getImportErrors()
  {
    return new ListDataModel(_naturalProductsLibraryContentsParser.getErrors());
  }

  public String viewLibrary()
  {
    return _librariesController.viewLibrary(_library, null);
  }

  /**
   * Parse the natural products library contents from the file, and go to the appropriate next page
   * depending on the result.
   * @return the control code for the appropriate next page
   */
  public String importNaturalProductsLibraryContents()
  {
    return _librariesController.importNaturalProductsLibraryContents(_library, _uploadedFile);
  }
}

  