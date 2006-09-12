// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.rnai;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFRow;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.io.libraries.DataRowType;
import edu.harvard.med.screensaver.io.workbook.Cell;
import edu.harvard.med.screensaver.io.workbook.Cell.Factory;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.Well;


/**
 * Parses a single data row for the {@link RNAiLibraryContentsParser}.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class DataRowParser
{
  
  // private static data
  
  private static final Logger log = Logger.getLogger(DataRowParser.class);

  
  // private instance data
  
  private RNAiLibraryContentsParser _parser;
  private RNAiLibraryColumnHeaders _columnHeaders;
  private HSSFRow _dataRow;
  private short _rowIndex;
  private Factory _cellFactory;
  private ParsedEntitiesMap _parsedEntitiesMap;
  
  private Map<RequiredRNAiLibraryColumn,String> _dataRowContents;
  
  
  // package constructor and instance method
  
  /**
   * Construct a new <code>DataRowParser</code> object.
   * @param parser the parent library contents loader
   * @param columnHeaders the column headers
   * @param dataRow the data row
   * @param rowIndex the index of the data row in the sheet
   * @param cellFactory the cell factory
   */
  public DataRowParser(
    RNAiLibraryContentsParser parser,
    RNAiLibraryColumnHeaders columnHeaders,
    HSSFRow dataRow,
    short rowIndex,
    Factory cellFactory,
    ParsedEntitiesMap parsedEntitiesMap)
  {
    _parser = parser;
    _columnHeaders = columnHeaders;
    _dataRow = dataRow;
    _rowIndex = rowIndex;
    _cellFactory = cellFactory;
    _parsedEntitiesMap = parsedEntitiesMap;
  }
  
  /**
   * Parse the data row.
   */
  void parseDataRow()
  {
    DataRowType dataRowType = _columnHeaders.getDataRowType(_dataRow);
    if (dataRowType.equals(DataRowType.EMPTY)) {
      return;
    }
    populateDataRowContents();
    if (dataRowType.equals(DataRowType.PLATE_WELL_ONLY)) {
      parseWell();
    }
    else {
      parseDataRowContent();
    }
  }

  
  // private instance methods
  
  /**
   * populate the {@link #_dataRowContents data row contents}.
   */
  private void populateDataRowContents()
  {
    _dataRowContents = _columnHeaders.getDataRowContents(_dataRow, _rowIndex);
  }
  
  /**
   * Parse the data row content
   */
  private void parseDataRowContent()
  {
    String plateWellAbbreviation = getPlateWellAbbreviation();
    if (plateWellAbbreviation == null) {
      return;
    }
    log.debug("loading data for plate-well " + plateWellAbbreviation);
    
    // get the well last, so that if we encounter any errors, we dont end up with a bogus
    // well in the library
  
    Gene gene = getGene();
    if (gene == null) {
      return;
    }
    Set<SilencingReagent> silencingReagents = getSilencingReagents(gene);
    Well well = getWell();
    if (well == null) {
      return;
    }
    for (SilencingReagent silencingReagent : silencingReagents) {
      well.addSilencingReagent(silencingReagent);
    }
  }

  /**
   * Parse the well.
   */
  private void parseWell()
  {
    String plateWellAbbreviation = getPlateWellAbbreviation();
    if (plateWellAbbreviation == null) {
      return;
    }
    log.debug("loading empty plate-well " + plateWellAbbreviation);
    getWell();
  }

  /**
   * Build and return the {@link Well} represented by this data row.
   * @return the well represented by this data row
   */
  private Well getWell()
  {
    Integer plateNumber = _parser.getPlateNumberParser().parse(_cellFactory.getCell(
      _columnHeaders.getColumnIndex(RequiredRNAiLibraryColumn.PLATE),
      _rowIndex));
    if (plateNumber == -1) {
      return null;
    }
    String wellName = _parser.getWellNameParser().parse(_cellFactory.getCell(
      _columnHeaders.getColumnIndex(RequiredRNAiLibraryColumn.WELL),
      _rowIndex));
    if (wellName.equals("")) {
      return null;
    }
    Well well = getExistingWell(plateNumber, wellName);
    if (well == null) {
      well = new Well(_parser.getLibrary(), plateNumber, wellName);
      _parsedEntitiesMap.addWell(well);
    }
    String vendorIdentifier = _cellFactory.getCell(
      _columnHeaders.getColumnIndex(RequiredRNAiLibraryColumn.VENDOR_IDENTIFIER),
      _rowIndex).getString();
    if (! (vendorIdentifier == null || vendorIdentifier.equals(""))) {
      well.setVendorIdentifier(vendorIdentifier);
    }
    return well;
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
    Well well = _parsedEntitiesMap.getWell(plateNumber, wellName);
    if (well != null) {
      return well;
    }
    Library library = _parser.getLibrary();
    if (library.getLibraryId() == null) {
      return null;
    }
    DAO dao = _parser.getDAO();
    Map<String,Object> propertiesMap = new HashMap<String,Object>();
    propertiesMap.put("hbnLibrary", _parser.getLibrary());
    propertiesMap.put("plateNumber", plateNumber);
    propertiesMap.put("wellName", wellName);
    return dao.findEntityByProperties(Well.class, propertiesMap);
  }

  /**
   * Get an abbreviation for the plate-well. For use in log messages.
   * @return an abbreviation for the plate-well
   */
  private String getPlateWellAbbreviation()
  {
    Integer plateNumber = _parser.getPlateNumberParser().parse(_cellFactory.getCell(
      _columnHeaders.getColumnIndex(RequiredRNAiLibraryColumn.PLATE),
      _rowIndex));
    if (plateNumber == -1) {
      return null;
    }
    String wellName = _parser.getWellNameParser().parse(_cellFactory.getCell(
      _columnHeaders.getColumnIndex(RequiredRNAiLibraryColumn.WELL),
      _rowIndex));
    if (wellName.equals("")) {
      return null;
    }
    return plateNumber + "-" + wellName;
  }
  
  /**
   * Build and return the {@link Gene} represented in this data row.
   * @return the Gene represented in this data row
   */
  private Gene getGene()
  {

    // entrezgeneId
    Cell entrezgeneIdCell = _cellFactory.getCell(
      _columnHeaders.getColumnIndex(RequiredRNAiLibraryColumn.ENTREZGENE_ID),
      _rowIndex,
      true);
    Integer entrezgeneId = entrezgeneIdCell.getInteger();
    if (entrezgeneId == 0) {
      return null;
    }
    
    // entrezgeneSymbol
    String entrezgeneSymbol = _cellFactory.getCell(
      _columnHeaders.getColumnIndex(RequiredRNAiLibraryColumn.ENTREZGENE_SYMBOL),
      _rowIndex,
      true).getString();
    if (entrezgeneSymbol.equals("")) {
      return null;
    }
    
    // genbankAccessionNumber
    String genbankAccessionNumber = _cellFactory.getCell(
      _columnHeaders.getColumnIndex(RequiredRNAiLibraryColumn.GENBANK_ACCESSION_NUMBER),
      _rowIndex,
      true).getString();
    if (genbankAccessionNumber.equals("")) {
      return null;
    }
    
    // gene name and species name
    NCBIGeneInfo geneInfo =
      _parser.getGeneInfoProvider().getGeneInfoForEntrezgeneId(entrezgeneId, entrezgeneIdCell);
    if (geneInfo == null) {
      return null;
    }
    
    // lookup existing gene
    Gene gene = getExistingGene(entrezgeneId);
    if (gene != null) {
      gene.setGeneName(geneInfo.getGeneName());
      gene.setEntrezgeneSymbol(entrezgeneSymbol);
      gene.addGenbankAccessionNumber(genbankAccessionNumber);
      gene.setSpeciesName(geneInfo.getSpeciesName());
    }
    else {
      // buildin tha gene
      gene = new Gene(
        geneInfo.getGeneName(),
        entrezgeneId,
        entrezgeneSymbol,
        genbankAccessionNumber,
        geneInfo.getSpeciesName());
      _parsedEntitiesMap.addGene(gene);
    }

    return gene;
  }
  
  /**
   * Get an existing gene from the database with the specified EntrezGene ID. Return null
   * if no such well exists in the database. 
   * @param entrezgeneId the EntrezGene ID
   * @return the existing gene from the database. Return null if no such gene exists in
   * the database
   */
  private Gene getExistingGene(Integer entrezgeneId)
  {
    Gene gene = _parsedEntitiesMap.getGene(entrezgeneId);
    if (gene != null) {
      return gene;
    }
    return _parser.getDAO().findEntityByProperty(Gene.class, "entrezgeneId", entrezgeneId);
  }

  /**
   * Build and return the set of {@link SilencingReagent SilencingReagents} represented
   * by this data row.
   * @return the set of silencing reagents represented by this data row
   */
  private Set<SilencingReagent> getSilencingReagents(Gene gene)
  {
    SilencingReagentType unknownSilencingReagentType =
      _parser.getUnknownSilencingReagentType();
    SilencingReagentType silencingReagentType = _parser.getSilencingReagentType();
    Set<SilencingReagent> silencingReagents = new HashSet<SilencingReagent>();
    String sequences = _cellFactory.getCell(
      _columnHeaders.getColumnIndex(RequiredRNAiLibraryColumn.SEQUENCES),
      _rowIndex).getString();
    if (sequences == null || sequences.equals("")) {
      silencingReagents.add(getSilencingReagent(gene, unknownSilencingReagentType, ""));
    }
    else {
      for (String sequence : sequences.split("[,;]")) {
        silencingReagents.add(getSilencingReagent(gene, silencingReagentType, sequence));
      }
    }
    return silencingReagents;
  }
  
  private SilencingReagent getSilencingReagent(
    Gene gene,
    SilencingReagentType silencingReagentType,
    String sequence)
  {
    SilencingReagent silencingReagent =
      getExistingSilencingReagent(gene, silencingReagentType, sequence);
    if (silencingReagent == null) {
      silencingReagent = new SilencingReagent(gene, silencingReagentType, sequence);
      _parsedEntitiesMap.addSilencingReagent(silencingReagent);
    }
    return silencingReagent;
  }
  
  /**
   * Get an existing silencing reagent from the database with the specified properties.
   * Return null if no such silencing reagent exists in the database. 
   * @param gene the gene
   * @param silencingReagentType the silencing reagent type
   * @param sequence the sequence
   * @return the existing silencing reagent from the database. Return null if no such
   * silencing reagent exists in the database
   */
  private SilencingReagent getExistingSilencingReagent(
    Gene gene,
    SilencingReagentType silencingReagentType,
    String sequence)
  {
    SilencingReagent silencingReagent =
      _parsedEntitiesMap.getSilencingReagent(gene, silencingReagentType, sequence);
    if (silencingReagent != null) {
      return silencingReagent;
    }
    if (gene.getGeneId() == null) {
      return null;
    }
    DAO dao = _parser.getDAO();
    Map<String,Object> propertiesMap = new HashMap<String,Object>();
    propertiesMap.put("hbnGene", gene);
    propertiesMap.put("hbnSilencingReagentType", silencingReagentType);
    propertiesMap.put("hbnSequence", sequence);
    return dao.findEntityByProperties(SilencingReagent.class, propertiesMap);
  }
}
