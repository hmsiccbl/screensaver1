// $HeadURL: svn+ssh://js163@orchestra/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.compound;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.io.libraries.rnai.RNAiLibraryContentsParser;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Well;

class SDRecordParser
{

  // private static data
  
  private static final Logger log = Logger.getLogger(SDRecordParser.class);

  
  // private instance data
  
  private BufferedReader _sdFileReader;
  private SDFileCompoundLibraryContentsParser _libraryContentsParser;
  private String _nextLine;
  private int _sdRecordNumber = 0;
  private SDRecordData _sdRecordData;
  MolfileInterpreter _molfileInterpreter;
  
  
  // package-private constructor and instance methods
  
  /**
   * Construct a new <code>SDRecordParser</code> object.
   * @param sdFileReader the <code>BufferedReader</code> for the SDFile
   * @param libraryContentsParser the parent SDFile compound library contents
   * parser
   */
  SDRecordParser(
    BufferedReader sdFileReader,
    SDFileCompoundLibraryContentsParser libraryContentsParser)
  {
    _sdFileReader = sdFileReader;
    _libraryContentsParser = libraryContentsParser;
    prepareNextRecord();
  }
  
  /**
   * @return
   */
  boolean sdFileHasMoreRecords()
  {
    return _nextLine != null;
  }
  
  /**
   * Parse an SD record from the SDFile.
   */
  void parseSDRecord()
  {
    _sdRecordData = gatherSDRecordData();

    String molfile = _sdRecordData.getMolfile();
    if (molfile == null) {
      reportError("encountered an SD record an empty MDL molfile specification");
      prepareNextRecord();
      return;
    }
    _molfileInterpreter = new MolfileInterpreter(molfile);
    
    // TODO: do something [more] with the record data
    log.info("record data = " + _sdRecordData);
    Well well = getWell();
    if (well == null) {
      prepareNextRecord();
      return;
    }
    
    prepareNextRecord();
  }
  
  
  // private instance methods
  
  private void prepareNextRecord()
  {
    _sdRecordNumber ++;
    _nextLine = readNextLine();
  }

  /**
   * 
   */
  private String readNextLine() {
    try {
      return _sdFileReader.readLine();
    }
    catch (IOException e) {
      log.error(e, e);
      _libraryContentsParser.getErrorManager().addError(
        "encountered an IOException reading SDFile: " + e.getMessage(),
        _libraryContentsParser.getSdFile(),
        _sdRecordNumber);
      return null;
    }
  }
  
  private SDRecordData gatherSDRecordData()
  {
    StringBuffer molfile = new StringBuffer();
    String line = _nextLine;
    while (! line.equals("M  END")) {
      molfile.append(line).append('\n');
      line = readNextLine();
    }
    SDRecordData recordData = new SDRecordData();
    recordData.setMolfile(new String(molfile));
    while (! line.equals("$$$$")) {
      
      if (line.matches("^>  <.*>(\\s+\\(.*\\))?")) {
        String header = line.substring(4, line.indexOf('>', 4));
        line = readNextLine();
        
        if (header.equals("Plate")) {
          try {
            recordData.setPlateNumber(Integer.parseInt(line));
          }
          catch (NumberFormatException e) {
            _libraryContentsParser.getErrorManager().addError(
              "Plate specified was not a number",
              _libraryContentsParser.getSdFile(),
              _sdRecordNumber);
          }
        }
        else if (header.equals("Well")) {
          recordData.setWellName(line);
        }
        else if (header.equals("ICCB_NUM") || header.equals("ICCB_Num")) {
          recordData.setIccbNumber(line);
        }
        else if (header.equals("CAS_Number") || header.equals("CAS_number")) {
          recordData.setIccbNumber(line);
        }
        else if (header.equals("Vendor_ID")) {
          recordData.setVendorIdentifier(line);
        }
        else if (
          header.equals("compound_identifier") ||
          header.equals("CompoundName") ||
          header.equals("ChemicalName") ||
          header.equals("Chemical_Name")) {
          recordData.setCompoundName(line);
        }
      }
      
      line = readNextLine();
    }
    return recordData;
  }
  
  /**
   * Build and return the {@link Well} represented by this data row.
   * @return the well represented by this data row
   */
  private Well getWell()
  {
    Integer plateNumber = _sdRecordData.getPlateNumber();
    if (plateNumber == null) {
      reportError("encountered an SD record without a Plate specification");
      return null;
    }
    String wellName = _sdRecordData.getWellName();
    if (wellName == null) {
      reportError("encountered an SD record without a Well specification");
      return null;
    }
    Well well = getExistingWell(plateNumber, wellName);
    if (well == null) {
      well = new Well(_libraryContentsParser.getLibrary(), plateNumber, wellName);
      _libraryContentsParser.getParsedEntitiesMap().addWell(well);
    }
    well.setIccbNumber(_sdRecordData.getIccbNumber());
    well.setVendorIdentifier(_sdRecordData.getVendorIdentifier());
    well.setMolfile(_molfileInterpreter.getMolfile());
    well.setSmiles(_molfileInterpreter.getSmiles());
    return well;
  }

  private void reportError(String errorMessage) {
    _libraryContentsParser.getErrors().add(new SDFileParseError(
      errorMessage,
      _libraryContentsParser.getSdFile(),
      _sdRecordNumber));
  }
  
  /**
   * Get an existing well from the database with the specified plate number and well name,
   * and the library from the parent {@link RNAiLibraryContentsParser}. Return null if no
   * such well exists in the database.
   *  
   * @param plateNumber the plate number
   * @param wellName the well name
   * @return the existing well from the database. Return null if no such well exists in
   * the database
   */
  private Well getExistingWell(Integer plateNumber, String wellName)
  {
    Well well = _libraryContentsParser.getParsedEntitiesMap().getWell(plateNumber, wellName);
    if (well != null) {
      return well;
    }
    Library library = _libraryContentsParser.getLibrary();
    if (library.getLibraryId() == null) {
      return null;
    }
    DAO dao = _libraryContentsParser.getDAO();
    Map<String,Object> propertiesMap = new HashMap<String,Object>();
    propertiesMap.put("hbnLibrary", library);
    propertiesMap.put("plateNumber", plateNumber);
    propertiesMap.put("wellName", wellName);
    return dao.findEntityByProperties(Well.class, propertiesMap);
  }
}
