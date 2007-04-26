// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.rnai;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFRow;

import edu.harvard.med.screensaver.io.libraries.DataRowType;
import edu.harvard.med.screensaver.io.workbook.Cell;
import edu.harvard.med.screensaver.io.workbook.ParseErrorManager;
import edu.harvard.med.screensaver.io.workbook.Cell.Factory;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellType;
import edu.harvard.med.screensaver.util.eutils.NCBIGeneInfo;


/**
 * Parses a single data row for the {@link RNAiLibraryContentsParser}.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class DataRowParser
{
  
  // private static data
  
  private static final Logger log = Logger.getLogger(DataRowParser.class);
  private static final Pattern entrezgeneLocPattern = Pattern.compile("LOC(\\d+)");

  
  // private instance data
  
  private RNAiLibraryContentsParser _parser;
  private RNAiLibraryColumnHeaders _columnHeaders;
  private HSSFRow _dataRow;
  private int _rowIndex;
  private Factory _cellFactory;
  private ParseErrorManager _errorManager;
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
    int rowIndex,
    Factory cellFactory,
    ParseErrorManager errorManager)
  {
    _parser = parser;
    _columnHeaders = columnHeaders;
    _dataRow = dataRow;
    _rowIndex = rowIndex;
    _cellFactory = cellFactory;
    _errorManager = errorManager;
  }
  
  /**
   * Parse the data row.
   * @throws DataRowParserException 
   */
  void parseDataRow() throws DataRowParserException
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
   * @throws DataRowParserException 
   */
  private void parseDataRowContent() throws DataRowParserException
  {
    String plateWellAbbreviation = getPlateWellAbbreviation();
    if (plateWellAbbreviation == null) {
      return;
    }
    //log.debug("loading data for plate-well " + plateWellAbbreviation);
    
    // get the well last, so that if we encounter any errors, we dont end up with a bogus
    // well in the library
  
    Gene gene = getGene();
    if (gene == null) {
      return;
    }
    Set<SilencingReagent> silencingReagents = getSilencingReagents(gene);
    Well well = getWell(false);
    if (well == null) {
      return;
    }
    addGenbankAccessionNumberToWell(well);
    for (SilencingReagent silencingReagent : silencingReagents) {
      well.addSilencingReagent(silencingReagent);
    }
  }

  private void addGenbankAccessionNumberToWell(Well well) {
    String genbankAccessionNumber = _cellFactory.getCell(
      _columnHeaders.getColumnIndex(RequiredRNAiLibraryColumn.GENBANK_ACCESSION_NUMBER),
      _rowIndex,
      true).getString();
    if (! genbankAccessionNumber.equals("")) {
      well.setGenbankAccessionNumber(genbankAccessionNumber);
    }
  }

  /**
   * Parse the well. Assume an empty plate-well is a control. (This is true for the
   * Excel files I've been handling so far, but it is pretty ad-hoc. -s)
   * @throws DataRowParserException 
   */
  private void parseWell() throws DataRowParserException
  {
    String plateWellAbbreviation = getPlateWellAbbreviation();
    if (plateWellAbbreviation == null) {
      return;
    }
    log.debug("loading empty plate-well " + plateWellAbbreviation);
    getWell(true);
  }

  /**
   * Build and return the {@link Well} represented by this data row.
   * @return the well represented by this data row
   * @throws DataRowParserException 
   */
  private Well getWell(boolean isControl) throws DataRowParserException
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
    Well well = _parser.getDAO().findWell(new WellKey(plateNumber, wellName));
    if (well == null) {
      throw new DataRowParserException(
        "specified well does not exist. this is probably due to an erroneous plate number.",
        _cellFactory.getCell(
          _columnHeaders.getColumnIndex(RequiredRNAiLibraryColumn.WELL),
          _rowIndex));
    }
    if (! well.getLibrary().equals(_parser.getLibrary())) {
      throw new DataRowParserException(
          "SD record specifies a well from the wrong library: " +
          well.getLibrary().getLibraryName(),
          _cellFactory.getCell(
            _columnHeaders.getColumnIndex(RequiredRNAiLibraryColumn.WELL),
            _rowIndex));
    }
    if (isControl) {
      well.setWellType(WellType.LIBRARY_CONTROL);
    }
    else {
      well.setWellType(WellType.EXPERIMENTAL);
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
    
    // entrezgeneSymbol
    Cell entrezgeneSymbolCell = _cellFactory.getCell(
      _columnHeaders.getColumnIndex(RequiredRNAiLibraryColumn.ENTREZGENE_SYMBOL),
      _rowIndex,
      true);
    String entrezgeneSymbol = entrezgeneSymbolCell.getAsString();
    if (entrezgeneSymbol.equals("")) {
      _errorManager.addError("missing EntrezGene Symbol", entrezgeneSymbolCell);
      return null;
    }
    
    // sometimes Locus ID is 0, but Gene Symbol is LOC(\d+) with $1 being the EntrezGene ID
    if (entrezgeneId == 0) {
      Matcher entrezgeneLocMatcher = entrezgeneLocPattern.matcher(entrezgeneSymbol);
      if (entrezgeneLocMatcher.matches()) {
        entrezgeneId = Integer.parseInt(entrezgeneLocMatcher.group(1));
      }
      else {
        _errorManager.addError(
          "missing or 0 for EntrezGene ID (with no LOC\\d+ EntrezGene Symbol)",
          entrezgeneIdCell);
        return null;
      }
    }
    
    // genbankAccessionNumber
    Cell genbankAccessionNumberCell = _cellFactory.getCell(
      _columnHeaders.getColumnIndex(RequiredRNAiLibraryColumn.GENBANK_ACCESSION_NUMBER),
      _rowIndex,
      true);
    String genbankAccessionNumber = genbankAccessionNumberCell.getString();
    if (genbankAccessionNumber.equals("")) {
      _errorManager.addError("missing GenBank Accession Number", genbankAccessionNumberCell);
      return null;
    }
    
    // gene name and species name
    NCBIGeneInfo geneInfo =
      _parser.getGeneInfoProvider().getGeneInfoForEntrezgeneId(entrezgeneId, entrezgeneIdCell);
    if (geneInfo == null) {
      // errors in this case are handled by the NCBIGeneInfoProvider
      return null;
    }
    
    // revise the entrezgeneSymbol if the NCBIGeneInfoProvider has a better value
    if (geneInfo.getEntrezgeneSymbol() != null) {
      entrezgeneSymbol = geneInfo.getEntrezgeneSymbol();
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
      _parser.getDAO().persistEntity(gene);
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
    return _parser.getDAO().findEntityById(
      Gene.class,
      new Gene(null, entrezgeneId, null, null).getEntityId());
  }

  /**
   * Build and return the set of {@link SilencingReagent SilencingReagents} represented
   * by this data row.
   * @return the set of silencing reagents represented by this data row
   */
  private Set<SilencingReagent> getSilencingReagents(Gene gene)
  {
    SilencingReagentType silencingReagentType = _parser.getSilencingReagentType();
    Set<SilencingReagent> silencingReagents = new HashSet<SilencingReagent>();
    String sequences = _cellFactory.getCell(
      _columnHeaders.getColumnIndex(RequiredRNAiLibraryColumn.SEQUENCES),
      _rowIndex).getString();
    if (sequences == null || sequences.equals("")) {
      silencingReagents.add(getSilencingReagent(gene, silencingReagentType, "", true));
    }
    else {
      for (String sequence : sequences.split("[,;]")) {
        silencingReagents.add(getSilencingReagent(gene, silencingReagentType, sequence, false));
      }
    }
    return silencingReagents;
  }
  
  private SilencingReagent getSilencingReagent(
    Gene gene,
    SilencingReagentType silencingReagentType,
    String sequence,
    boolean isPoolOfUnknownSequences)
  {
    SilencingReagent silencingReagent =
      _parser.getDAO().findSilencingReagent(gene, silencingReagentType, sequence);
    if (silencingReagent == null) {
      silencingReagent = new SilencingReagent(
        gene,
        silencingReagentType,
        sequence,
        isPoolOfUnknownSequences);
      _parser.getDAO().persistEntity(silencingReagent);
    }
    return silencingReagent;
  }
}
