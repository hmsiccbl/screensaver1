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

import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;

import org.apache.log4j.Logger;

class SDRecordParser
{

  // private static data
  
  private static final Logger log = Logger.getLogger(SDRecordParser.class);

  
  // private instance data
  
  private BufferedReader _sdFileReader;
  private SDFileCompoundLibraryContentsParser _parser;
  private String _nextLine;
  private int _sdRecordNumber = 0;
  private SDRecordData _sdRecordData;
  private MolfileInterpreter _molfileInterpreter;
  
  
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
    _parser = libraryContentsParser;
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
      reportError("encountered an SD record with an empty MDL molfile specification");
      _molfileInterpreter = null;
    }
    else {
      _molfileInterpreter = new MolfileInterpreter(molfile);
    }
    
    Well well = getWell();
    if (well != null && _molfileInterpreter != null) {
      addSmilesToWell(_molfileInterpreter.getPrimaryCompoundSmiles(), well, true);
      for (String secondaryCompoundSmiles : _molfileInterpreter.getSecondaryCompoundsSmiles()) {
        addSmilesToWell(secondaryCompoundSmiles, well, false);
      }
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
      String nextLine = _sdFileReader.readLine();
      return nextLine; 
    }
    catch (IOException e) {
      log.error(e, e);
      _parser.getErrorManager().addError(
        "encountered an IOException reading SDFile: " + e.getMessage(),
        _parser.getSdFile(),
        _sdRecordNumber);
      return null;
    }
  }
  
  private SDRecordData gatherSDRecordData()
  {
    // initialize things
    String line = _nextLine;
    SDRecordData recordData = new SDRecordData();
    
    // read the molfile, unless it is missing
    if (! line.startsWith(">")) {
      StringBuffer molfileBuffer = new StringBuffer();
      while (! line.equals("M  END")) {
        molfileBuffer.append(line).append('\n');
        line = readNextLine();
      }
      molfileBuffer.append(line);
      String molfile = new String(molfileBuffer);
      recordData.setMolfile(molfile);
    }
    
    // read the "associated data" part of the SD record
    while (! line.equals("$$$$")) {
      
      if (line.matches("^>  <.*>(\\s+\\(.*\\))?")) {
        String header = line.substring(4, line.indexOf('>', 4));
        line = readNextLine();
        
        if (header.equals("Plate")) {
          try {
            recordData.setPlateNumber(Integer.parseInt(line));
          }
          catch (NumberFormatException e) {
            _parser.getErrorManager().addError(
              "Plate specified was not a number",
              _parser.getSdFile(),
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
    
    // return the accumulated data
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
    WellKey wellKey = new WellKey(plateNumber, wellName);
    Well well = _parser.getWell(wellKey);
    if (well == null) {
      reportError("internal error: well " + wellKey + " was not created);");
      return null;
    }
    well.setIccbNumber(_sdRecordData.getIccbNumber());
    well.setVendorIdentifier(_sdRecordData.getVendorIdentifier());
    if (_molfileInterpreter != null) {
      well.setMolfile(_molfileInterpreter.getMolfile());
      well.setSmiles(_molfileInterpreter.getSmiles());
    }
    return well;
  }

  private void reportError(String errorMessage) {
    _parser.getErrors().add(new SDFileParseError(
      errorMessage,
      _parser.getSdFile(),
      _sdRecordNumber));
  }
  
  /**
   * Retrieve or create a compound for the smiles. If it is the primary compound, then add
   * naming information such as the CAS number and the compound name. Add the compound to
   * the well.
   * 
   * @param smiles
   * @param well
   * @param isPrimaryCompound
   */
  private void addSmilesToWell(String smiles, Well well, boolean isPrimaryCompound)
  {
    Compound compound = _parser.getExistingCompound(smiles);
    if (compound == null) {
      compound = new Compound(smiles);
      _parser.cacheCompound(compound);
      _parser.getDAO().persistEntity(compound);
    }
    if (isPrimaryCompound) {
      String compoundName = _sdRecordData.getCompoundName();
      if (compoundName != null) {
        compound.addCompoundName(compoundName);
      }
      String casNumber = _sdRecordData.getCasNumber();
      if (casNumber != null) {
        compound.addCasNumber(casNumber);
      }
    }
    well.addCompound(compound);
  }
}
