// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.smallmolecule;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Sets;
import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.io.DataExporter;
import edu.harvard.med.screensaver.io.ParseError;
import edu.harvard.med.screensaver.io.ParseException;
import edu.harvard.med.screensaver.io.libraries.LibraryContentsParser;
import edu.harvard.med.screensaver.model.MolarConcentration;
import edu.harvard.med.screensaver.model.MolarUnit;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.MolecularFormula;
import edu.harvard.med.screensaver.model.libraries.WellName;

//TODO: use common file specification with WellSdfWriter
public class SDRecordParser
{
  private static final String END_OF_MOLFILE_MARKER = "M  END";
  private static final String END_OF_RECORD_DELIMITER = "$$$$";

  public static Set EMPTY_FIELD_MARKERS = Sets.newHashSet(new String[] { "n/a", "none" });

  private static final Logger log = Logger.getLogger(SDRecordParser.class);
  private static final Pattern dataHeaderPattern = Pattern.compile("^>.*<(.*)>.*");
  private BufferedReader _sdFileReader;
  private int _lineNumber;

  SDRecordParser(BufferedReader sdFileReader)
                 throws IOException
  {
    _sdFileReader = sdFileReader;
  }

  public SDRecord next()
    throws IOException, ParseException
  {
    return parseNextRecord();
  }

  private String readNextLine() throws IOException
  {
    String line = _sdFileReader.readLine();
    if (line != null) {
      ++_lineNumber;
    }
    return line;
  }

  private SDRecord parseNextRecord() throws IOException, ParseException
  {
    SDRecord sdRecord = new SDRecord();
    boolean emptyRecord = true;
    StringBuilder molfile = new StringBuilder();
    String line = readNextLine();

    // read the molfile, unless it is missing
    while (line != null && ! line.startsWith(">") && !line.equals(END_OF_MOLFILE_MARKER)) {
      molfile.append(line).append("\n");
      line = readNextLine();
    }
    if (molfile.length() > 0) {
      emptyRecord = false;
      molfile.append(END_OF_MOLFILE_MARKER).append("\n");
      sdRecord.setMolfile(molfile.toString());
      if (log.isDebugEnabled()) {
        log.debug("molfile: " + molfile.toString());
      }
    }

    // read the "associated data" part of the SD record
    while (line != null && ! line.equals(END_OF_RECORD_DELIMITER)) {
      Matcher dataHeaderMatcher = dataHeaderPattern.matcher(line);
      if (dataHeaderMatcher.matches()) {
        String fieldName = dataHeaderMatcher.group(1).toLowerCase();
        line = readNextLine().trim();
        if (line.length() == 0) continue;
        if (EMPTY_FIELD_MARKERS.contains(line)) continue;
        try {
          boolean unusedField = false;
          if (fieldName.equals("plate")) { 
            sdRecord.setPlateNumber(Integer.parseInt(line));
          }
          else if (fieldName.equals("well")) {
            sdRecord.setWellName(new WellName(line));
          }
          else if (fieldName.equals("well_type")) {
            sdRecord.setLibraryWellType(LibraryWellType.valueOf(line.toUpperCase()));
          }
          else if (fieldName.equals("facility_reagent_id")) {
            log.info("facility reagent id: " + line);
            sdRecord.setFacilityId(line);
          }
          else if (fieldName.equals("vendor")) {
            sdRecord.setVendor(line);
          }
          else if (fieldName.equals("vendor_reagent_id")) {
            sdRecord.setVendorIdentifier(line);
          }
          else if (fieldName.equals("vendor_batch_id")) {
            sdRecord.setVendorBatchId(line);
          }
          else if (fieldName.equals("facility_batch_id")) {
            sdRecord.setFacilityBatchId(Integer.parseInt(line));
          }
          else if (fieldName.equals("salt_form_id")) {
            sdRecord.setSaltFormId(Integer.parseInt(line));
          }
          else if (fieldName.equals("chemical_name")) {
            String[] names = line.split(DataExporter.LIST_DELIMITER);
            for (String name : names) {
              // trim whitespace from the names
              name = name.trim();
              sdRecord.getCompoundNames().add(name);
            }
            //sdRecord.getCompoundNames().add(line);
          }
          else if (fieldName.equals("pubchem_cid")) 
          {
            String[] ids = line.split(DataExporter.LIST_DELIMITER);
            for(String id:ids)
            {
              sdRecord.getPubchemCids().add(Integer.parseInt(id));
            }
          }
          else if (fieldName.equals("chembank_id")) {
            String[] ids = line.split(DataExporter.LIST_DELIMITER);
            for(String id:ids)
            {
              sdRecord.getChembankIds().add(Integer.parseInt(id));
            }
          }
          else if (fieldName.equals("chembl_id")) {
            String[] ids = line.split(DataExporter.LIST_DELIMITER);
            for(String id:ids)
            {
              sdRecord.getChemblIds().add(Integer.parseInt(id));
            }
          }
          else if (fieldName.equals("pubmed_id")) {
            String[] ids = line.split(DataExporter.LIST_DELIMITER);
            for (String id : ids) {
              sdRecord.getPubmedIds().add(Integer.parseInt(id));
              log.info("pubmed id: " + id);
            }
          }
          else if (fieldName.equals("concentration")) {
            Matcher matcher = LibraryContentsParser.molarConcentrationPattern.matcher(line);
            if (matcher.matches()) {
              MolarUnit unit = MolarUnit.forSymbol(matcher.group(2));
              sdRecord.setMolarConcentration(MolarConcentration.makeConcentration(matcher.group(1), unit));
            }
            else {
              matcher = LibraryContentsParser.mgMlConcentrationPattern.matcher(line);
              if (matcher.matches()) {
                sdRecord.setMgMlConcentration(new BigDecimal(matcher.group(1) ));
              }
              else {
                throw new ParseException(new ParseError("field 'concentration' value could not be interpreted as a molar or mg/ml concentration:" +
                  line));
              }
            }
          }
          else if (fieldName.equals("molecular_mass")) {
            sdRecord.setMolecularMass(new BigDecimal(line));
          }
          else if (fieldName.equals("molecular_weight")) {
            sdRecord.setMolecularWeight(new BigDecimal(line));
          }
          else if (fieldName.equals("molecular_formula")) {
            sdRecord.setMolecularFormula(new MolecularFormula(line));
          }
          else if (fieldName.equals("smiles")) {
            sdRecord.setSmiles(line);
          }
          else if (fieldName.equals("inchi")) {
            sdRecord.setInChi(line);
          } 
          else {
            unusedField = true;
            if (log.isDebugEnabled()) {
              log.debug("unused field: " + fieldName + ": " + line);
            }
          }
          if (!unusedField) {
            emptyRecord = false;
          }
        }
        catch (Exception e) {
          skipRestOfRecord();
          log.warn("bad value in field '" + fieldName + "'" + _lineNumber, e);
          if (e instanceof ParseException) throw (ParseException) e;
          //TODO: why not add something from the exception message here (this would show up in the UI)? - sde4
          throw new ParseException(new ParseError("field '" + fieldName + "' error: " + e.getMessage(), _lineNumber));
        }
      }
      line = readNextLine();
    }
    return emptyRecord ? null : sdRecord;
  }
  
  private void skipRestOfRecord() throws IOException
  {
    String line;
    do {
      line = readNextLine();
    } while (line != null && ! line.equals(END_OF_RECORD_DELIMITER));    
  }

  public int getLineNumber()
  {
    return _lineNumber;
  }
}
