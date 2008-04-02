// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.compound;

import java.io.BufferedReader;
import java.io.IOException;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellType;
import edu.harvard.med.screensaver.util.eutils.EutilsException;
import edu.harvard.med.screensaver.util.eutils.PubchemCidListProvider;

class SDRecordParser
{

  // private static data

  private static final Logger log = Logger.getLogger(SDRecordParser.class);


  // private instance data

  private GenericEntityDAO _dao;
  private BufferedReader _sdFileReader;
  private SDFileCompoundLibraryContentsParser _parser;
  private Library _library;
  private String _nextLine;
  private int _sdRecordNumber = 0;
  private SDRecordData _sdRecordData;
  private MolfileToSmiles _molfileToSmiles;
  private OpenBabelClient _openBabelClient = new OpenBabelClient();
  private PubchemCidListProvider _pubchemCidListProvider = new PubchemCidListProvider();


  // package-private constructor and instance methods

  /**
   * Construct a new <code>SDRecordParser</code> object.
   * @param sdFileReader the <code>BufferedReader</code> for the SDFile
   * @param libraryContentsParser the parent SDFile compound library contents
   * parser
   */
  SDRecordParser(
    GenericEntityDAO dao,
    BufferedReader sdFileReader,
    SDFileCompoundLibraryContentsParser libraryContentsParser)
  {
    _dao = dao;
    _sdFileReader = sdFileReader;
    _parser = libraryContentsParser;
    _library = _parser.getLibrary();
    prepareNextRecord();
  }

  /**

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
      // TODO: it would be nice if it showed up on the ui that this error occurred. but calling logError() here
      // causes the whole file load to fail, and this should not be a fatal error. probably should configure the
      // ErrorMgr to handle both fatal and non-fatal errors.
      log.warn("encountered an SD record with an empty MDL molfile specification");
      _molfileToSmiles = null;
    }
    else {
      _molfileToSmiles = new MolfileToSmiles(molfile);
    }

    Well well = getWell();
    if (well != null && _molfileToSmiles != null && _molfileToSmiles.getSmiles() != null) {
      if (! _library.equals(well.getLibrary())) {
        reportError(
          "SD record specifies a well from the wrong library: " +
          well.getLibrary().getLibraryName());
        return;
      }

      addSmilesToWell(_molfileToSmiles.getPrimaryCompoundSmiles(), well, true);
      for (String secondaryCompoundSmiles : _molfileToSmiles.getSecondaryCompoundsSmiles()) {
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
        line = readNextLine().trim();

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
          recordData.setCasNumber(line);
        }
        else if (header.equals("Vendor")) {
          recordData.setVendor(line);
        }
        else if (header.equals("Vendor_ID") || header.equals("Vendor_Identifier")) {
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
      reportError("internal error: well " + wellKey + " was not created");
      return null;
    }
    well.setWellType(WellType.EXPERIMENTAL);
    well.setIccbNumber(_sdRecordData.getIccbNumber());

    if (well.getReagent() == null) {
      String vendor = _sdRecordData.getVendor();
      if (vendor == null) {
        _library.getVendor();
      }
      String vendorIdentifier = _sdRecordData.getVendorIdentifier();
      ReagentVendorIdentifier reagentVendorIdentifier = new ReagentVendorIdentifier(vendor, vendorIdentifier);
      Reagent reagent = _dao.findEntityById(Reagent.class, reagentVendorIdentifier);
      if (reagent == null) {
        reagent = new Reagent(reagentVendorIdentifier);
        _dao.saveOrUpdateEntity(reagent); // place into session so it can be found again before flush
        log.info("created new reagent " + reagent + " for " + well);
      }
      well.setReagent(reagent);
    }

    if (_molfileToSmiles != null) {
      well.setMolfile(_molfileToSmiles.getMolfile());
      well.setSmiles(_molfileToSmiles.getSmiles());
    }
    return well;
  }

  private void reportError(String errorMessage)
  {
    _parser.getErrorManager().addError(
      errorMessage,
      _parser.getSdFile(),
      _sdRecordNumber);
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
      compound = createCompoundFromSmiles(smiles);
      _parser.cacheCompound(compound);
      _parser.getDAO().saveOrUpdateEntity(compound);
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
      String inchi = _openBabelClient.convertMolfileToInchi(_sdRecordData.getMolfile());
      addPubchemCidsToCompound(compound, inchi);
    }
    well.addCompound(compound);
  }

  /**
   * Create a compound from a SMILES string, filling in the InChI and the PubChem CIDs.
   * @param smiles

   */
  private Compound createCompoundFromSmiles(String smiles)
  {
    String inchi = _openBabelClient.convertSmilesToInchi(smiles);
    Compound compound = new Compound(smiles, inchi);
    addPubchemCidsToCompound(compound, inchi);
    return compound;
  }

  private void addPubchemCidsToCompound(Compound compound, String inchi) {
    try {
      for (String pubchemCid : _pubchemCidListProvider.getPubchemCidListForInchi(inchi)) {
        compound.addPubchemCid(pubchemCid);
      }
    }
    catch (EutilsException e) {
      _parser.getErrorManager().addError(e.getMessage(), _parser.getSdFile(), _sdRecordNumber);
    }
  }
}
