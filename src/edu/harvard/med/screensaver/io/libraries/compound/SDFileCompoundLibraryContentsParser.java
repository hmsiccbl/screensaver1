// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.compound;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.io.libraries.LibraryContentsParser;
import edu.harvard.med.screensaver.io.libraries.ParsedEntitiesMap;
import edu.harvard.med.screensaver.model.libraries.Library;


/**
 * Parses the contents (either partial or complete) of an compound library
 * from an SDFile into the entity model.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class SDFileCompoundLibraryContentsParser implements LibraryContentsParser
{
  
  // private static data
  
  private static final Logger log = Logger.getLogger(SDFileCompoundLibraryContentsParser.class);

  
  // private instance data
  
  private DAO _dao;
  private Library _library;
  private File _sdFile;
  private BufferedReader _sdFileReader;
  private SDFileParseErrorManager _errorManager;
  private ParsedEntitiesMap _parsedEntitiesMap;

  
  // public constructor and instance methods
  
  /**
   * Construct a new <code>SDFileCompoundLibraryContentsParser</code> object.
   * @param dao the data access object
   */
  public SDFileCompoundLibraryContentsParser(DAO dao)
  {
    _dao = dao;
  }
  
  /**
   * Load library contents (either partial or complete) from an input
   * stream of an Excel spreadsheet into a library.
   * @param library the library to load contents of
   * @param file the name of the file that contains the library contents
   * @param stream the input stream to load library contents from
   * @return the library with the contents loaded
   */
  public Library parseLibraryContents(
    Library library,
    File file,
    InputStream stream)
  {
    initialize(library, file, stream);
    SDRecordParser sdRecordParser = new SDRecordParser(_sdFileReader, this);
    while (sdRecordParser.sdFileHasMoreRecords()) {
      sdRecordParser.parseSDRecord();
    }
    return _library;
  }
  
  public List<SDFileParseError> getErrors()
  {
    return _errorManager.getErrors();
  }
  
    
  // package getters, for the SDRecordParser

  /**
   * Get the {@link DAO data access object}.
   * @return the data access object
   */
  DAO getDAO()
  {
    return _dao;
  }

  /**
   * Get the {@link Library}.
   * @return the library.
   */
  Library getLibrary()
  {
    return _library;
  }
  
  /**
   * Get the SDFile.
   * @return the SDFile
   */
  File getSdFile()
  {
    return _sdFile;
  }

  /**
   * Get the error manager.
   * @return the error manager
   */
  SDFileParseErrorManager getErrorManager()
  {
    return _errorManager;
  }
  
  /**
   * Get the parsed entities map.
   * @return the parsed entities map
   */
  ParsedEntitiesMap getParsedEntitiesMap()
  {
    return _parsedEntitiesMap;
  }

  
  // private instance methods
  
  /**
  /**
   * Initialize the instance variables.
   * @param library the library to load contents of
   * @param file the name of the file that contains the library contents
   * @param stream the input stream to load library contents from
   */
  private void initialize(Library library, File file, InputStream stream)
  {
    _library = library;
    _sdFile = file;
    _sdFileReader = new BufferedReader(new InputStreamReader(stream));
    _errorManager = new SDFileParseErrorManager();
    _parsedEntitiesMap = new ParsedEntitiesMap();
  }
}
