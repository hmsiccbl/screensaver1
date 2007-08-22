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

import org.apache.log4j.Logger;
import org.apache.myfaces.custom.fileupload.UploadedFile;

import edu.harvard.med.screensaver.io.libraries.compound.NaturalProductsLibraryContentsParser;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.control.LibrariesController;

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

  private LibrariesController _librariesController;
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

  public NaturalProductsLibraryContentsImporter(LibrariesController librariesController,
                                                NaturalProductsLibraryContentsParser naturalProductsLibraryContentsParser)
  {
    _librariesController = librariesController;
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
    return _librariesController.viewLibrary(_library);
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

