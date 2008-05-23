// $HeadURL$
// $Id$
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

import edu.harvard.med.screensaver.io.libraries.DataRowType;
import edu.harvard.med.screensaver.io.workbook.Cell;
import edu.harvard.med.screensaver.io.workbook.ParseErrorManager;
import edu.harvard.med.screensaver.io.workbook.Cell.Factory;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellType;
import edu.harvard.med.screensaver.util.eutils.EutilsException;
import edu.harvard.med.screensaver.util.eutils.NCBIGeneInfo;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFRow;


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

  public static final String MISSING_ENTREZ_GENE_SYMBOL_ERROR = "missing EntrezGene Symbol";
  public static final String MISSING_ENTREZ_GENE_ID_ERROR =
    "missing or 0 for EntrezGene ID (with no LOC\\d+ EntrezGene Symbol)";
  public static final String MISSING_ENTREZ_GENBANK_ACCESSION_NUMBER_ERROR =
    "missing GenBank Accession Number";


  // private instance data

  private RNAiLibraryContentsParser _parser;
  private RNAiLibraryColumnHeaders _columnHeaders;
  private HSSFRow _dataRow;
  private int _rowIndex;
  private Factory _cellFactory;
  private ParseErrorManager _errorManager;
  private Map<ParsedRNAiLibraryColumn,String> _dataRowContents;
  private DataRowType _dataRowType;


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
    _dataRowType = _columnHeaders.getDataRowType(_dataRow);
    if (_dataRowType.equals(DataRowType.EMPTY)) {
      _dataRowContents = null;
      return;
    }
    _dataRowContents = _columnHeaders.getDataRowContents(_dataRow, _rowIndex);
  }

  void processDataRow()
    throws DataRowParserException
  {
    if (_dataRowContents == null) {
      return;
    }
    if (_dataRowType.equals(DataRowType.PLATE_WELL_ONLY)) {
      // when the data row has Plate, Well, and no gene information, we assume it is a control well
      processControlWell();
    }
    else {
      processDataRowContent();
    }
  }

  Integer getPlateNumber()
  {
    if (_dataRowContents == null || _dataRowType == DataRowType.EMPTY) {
      return null;
    }
    try {
      Integer plateNumber = _parser.getPlateNumberParser().parse(_cellFactory.getCell(_columnHeaders.getColumnIndex(ParsedRNAiLibraryColumn.PLATE),
                                                                                      _rowIndex));
      if (plateNumber < 0) {
        return null;
      }
      return plateNumber;
      //return new BigDecimal(_dataRowContents.get(ParsedRNAiLibraryColumn.PLATE)).toBigInteger().intValue();
    }
    catch (NumberFormatException e) {
      return null;
    }
  }

  
  // private instance methods

  /**
   * Process the data row content by updating the entity model with the data
   * @throws DataRowParserException
   */
  private void processDataRowContent() throws DataRowParserException
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

    // remove any silencing reagents that were in the well from a previous import. but be careful
    // to not remove any that you are planning to add back in, or you might get a
    // org.hibernate.ObjectDeletedException
    for (SilencingReagent silencingReagent : well.getSilencingReagents()) {
      if (! silencingReagents.contains(silencingReagent)) {
        well.removeSilencingReagent(silencingReagent);
      }
    }

    for (SilencingReagent silencingReagent : silencingReagents) {
      well.addSilencingReagent(silencingReagent);
    }
  }

  private void addGenbankAccessionNumberToWell(Well well) {
    String genbankAccessionNumber = _cellFactory.getCell(
      _columnHeaders.getColumnIndex(ParsedRNAiLibraryColumn.GENBANK_ACCESSION_NUMBER),
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
  private void processControlWell() throws DataRowParserException
  {
    String plateWellAbbreviation = getPlateWellAbbreviation();
    if (plateWellAbbreviation == null) {
      return;
    }
    log.debug("loading control well " + plateWellAbbreviation);
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
      _columnHeaders.getColumnIndex(ParsedRNAiLibraryColumn.PLATE),
      _rowIndex));
    if (plateNumber == -1) {
      return null;
    }
    String wellName = _parser.getWellNameParser().parse(_cellFactory.getCell(
      _columnHeaders.getColumnIndex(ParsedRNAiLibraryColumn.WELL),
      _rowIndex));
    if (wellName.equals("")) {
      return null;
    }
    WellKey wellKey = new WellKey(plateNumber, wellName);
    Well well = _parser.getLibrariesDAO().findWell(wellKey, true);
    if (well == null) {
      throw new DataRowParserException(
        "specified well does not exist: " + wellKey + ". this is probably due to an erroneous plate number.",
        _cellFactory.getCell(
          _columnHeaders.getColumnIndex(ParsedRNAiLibraryColumn.WELL),
          _rowIndex));
    }
    if (! well.getLibrary().equals(_parser.getLibrary())) {
      throw new DataRowParserException(
          "SD record specifies a well from the wrong library: " +
          well.getLibrary().getLibraryName(),
          _cellFactory.getCell(
            _columnHeaders.getColumnIndex(ParsedRNAiLibraryColumn.WELL),
            _rowIndex));
    }
    if (isControl) {
      well.setWellType(WellType.LIBRARY_CONTROL);
    }
    else {
      well.setWellType(WellType.EXPERIMENTAL);
    }
    String vendorIdentifier = _cellFactory.getCell(
      _columnHeaders.getColumnIndex(ParsedRNAiLibraryColumn.VENDOR_IDENTIFIER),
      _rowIndex).getString();
    if (! (vendorIdentifier == null || vendorIdentifier.equals(""))) {
      if (well.getReagent() == null) {
        ReagentVendorIdentifier rvi = new ReagentVendorIdentifier(well.getLibrary().getVendor(),
                                                                  vendorIdentifier);
        Reagent reagent = _parser.getDAO().findEntityById(Reagent.class, rvi);
        if (reagent == null) {
          reagent = new Reagent(rvi);
          _parser.getDAO().saveOrUpdateEntity(reagent); // place into session so it can be found again before flush
          log.info("created new reagent " + reagent + " for " + well);
        }
        well.setReagent(reagent);
      }
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
      _columnHeaders.getColumnIndex(ParsedRNAiLibraryColumn.PLATE),
      _rowIndex));
    if (plateNumber == -1) {
      return null;
    }
    String wellName = _parser.getWellNameParser().parse(_cellFactory.getCell(
      _columnHeaders.getColumnIndex(ParsedRNAiLibraryColumn.WELL),
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

    // entrezgeneSymbol
    Cell entrezgeneSymbolCell = _cellFactory.getCell(
      _columnHeaders.getColumnIndex(ParsedRNAiLibraryColumn.ENTREZGENE_SYMBOL),
      _rowIndex,
      false);
    String entrezgeneSymbol = entrezgeneSymbolCell.getAsString();
    if (entrezgeneSymbol.equals("")) {
      _errorManager.addError(MISSING_ENTREZ_GENE_SYMBOL_ERROR, entrezgeneSymbolCell);
      return null;
    }

    // entrezgeneId
    Cell entrezgeneIdCell = _cellFactory.getCell(
      _columnHeaders.getColumnIndex(ParsedRNAiLibraryColumn.ENTREZGENE_ID),
      _rowIndex,
      false);
    Integer entrezgeneId = entrezgeneIdCell.getInteger();

    // sometimes Locus ID is 0, but Gene Symbol is LOC(\d+) with $1 being the EntrezGene ID
    if (entrezgeneId == null || entrezgeneId == 0) {
      Matcher entrezgeneLocMatcher = entrezgeneLocPattern.matcher(entrezgeneSymbol);
      if (entrezgeneLocMatcher.matches()) {
        entrezgeneId = Integer.parseInt(entrezgeneLocMatcher.group(1));
      }
      else {
        _errorManager.addError(MISSING_ENTREZ_GENE_ID_ERROR, entrezgeneIdCell);
        return null;
      }
    }

    // genbankAccessionNumber
    Cell genbankAccessionNumberCell = _cellFactory.getCell(
      _columnHeaders.getColumnIndex(ParsedRNAiLibraryColumn.GENBANK_ACCESSION_NUMBER),
      _rowIndex,
      false);
    String genbankAccessionNumber = genbankAccessionNumberCell.getString();
    if (genbankAccessionNumber == null || genbankAccessionNumber.equals("")) {
      _errorManager.addError(MISSING_ENTREZ_GENBANK_ACCESSION_NUMBER_ERROR, genbankAccessionNumberCell);
      return null;
    }

    // gene name and species name
    NCBIGeneInfo geneInfo;
    try {
      geneInfo = _parser.getGeneInfoProvider().getGeneInfoForEntrezgeneId(entrezgeneId);
    }
    catch (EutilsException e) {
      _errorManager.addError(e.getMessage(), entrezgeneIdCell);
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
      _parser.getDAO().saveOrUpdateEntity(gene);
    }
    addOldEntrezgeneIds(gene);
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
   * Add the Old EntrezGene IDs to the gene.
   */
  private void addOldEntrezgeneIds(Gene gene)
  {
    Cell oldEntrezgeneIdsCell;
    try {
      oldEntrezgeneIdsCell = _cellFactory.getCell(
        _columnHeaders.getColumnIndex(ParsedRNAiLibraryColumn.OLD_ENTREZGENE_IDS),
        _rowIndex);
    }
    catch (IndexOutOfBoundsException e) {
      return;
    }
    String oldEntrezgeneIds = oldEntrezgeneIdsCell.getAsString();
    if (oldEntrezgeneIds != null && ! oldEntrezgeneIds.equals("")) {
      for (String oldEntrezgeneIdString : oldEntrezgeneIds.split("[,;]")) {

        // hack to work around quirkiness with HSSF spreadsheet parsing library
        if (oldEntrezgeneIdString.endsWith(".0")) {
          oldEntrezgeneIdString =
            oldEntrezgeneIdString.substring(0, oldEntrezgeneIdString.length() - 2);
        }

        try {
          gene.addOldEntrezgeneId(new Integer(oldEntrezgeneIdString));
        }
        catch (NumberFormatException e) {
          _errorManager.addError(
            "encountered a non-integer Old EntrezGene ID: " + oldEntrezgeneIdString,
            oldEntrezgeneIdsCell);
        }
      }
    }
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
    String sequences = null;
    try {
      sequences = _cellFactory.getCell(
        _columnHeaders.getColumnIndex(ParsedRNAiLibraryColumn.SEQUENCES),
        _rowIndex).getString();
    }
    catch (IndexOutOfBoundsException e) {
      // leave it as null if optional column "Sequences" is missing
    }
    if (sequences == null || sequences.equals("")) {
      // add a silencing reagent with isPoolOfUnknownSequences=true
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
      _parser.getLibrariesDAO().findSilencingReagent(gene, silencingReagentType, sequence);
    if (silencingReagent == null) {
      silencingReagent = gene.createSilencingReagent(
        silencingReagentType,
        sequence,
        isPoolOfUnknownSequences);
      _parser.getDAO().saveOrUpdateEntity(silencingReagent);
    }
    return silencingReagent;
  }

}
