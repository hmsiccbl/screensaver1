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

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFRow;

import edu.harvard.med.screensaver.io.libraries.DataRowType;
import edu.harvard.med.screensaver.io.workbook.Cell;
import edu.harvard.med.screensaver.io.workbook.Cell.Factory;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.Well;


/**
 * Parses a single data row for the {@link RNAiLibraryContentsLoader}.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class DataRowParser
{
  
  // private static data
  
  private static final Logger log = Logger.getLogger(DataRowParser.class);

  
  // private instance data
  
  private RNAiLibraryContentsLoader _loader;
  private RNAiLibraryColumnHeaders _columnHeaders;
  private HSSFRow _dataRow;
  private short _rowIndex;
  private Factory _cellFactory;
  
  private Map<RequiredRNAiLibraryColumn,String> _dataRowContents;
  
  
  // package constructor and instance method
  
  /**
   * Construct a new <code>DataRowParser</code> object.
   * @param loader the parent library contents loader
   * @param columnHeaders the column headers
   * @param dataRow the data row
   * @param rowIndex the index of the data row in the sheet
   * @param cellFactory the cell factory
   */
  public DataRowParser(
    RNAiLibraryContentsLoader loader,
    RNAiLibraryColumnHeaders columnHeaders,
    HSSFRow dataRow,
    short rowIndex,
    Factory cellFactory)
  {
    _loader = loader;
    _columnHeaders = columnHeaders;
    _dataRow = dataRow;
    _rowIndex = rowIndex;
    _cellFactory = cellFactory;   
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
   * Parse the well.
   */
  private void parseWell()
  {
    String plateWellAbbreviation = getPlateWellAbbreviation();
    if (plateWellAbbreviation == null) {
      return;
    }
    log.info("loading empty plate-well " + plateWellAbbreviation);
    getWell();
  }

  /**
   * Build and return the {@link Well} represented by this data row.
   * @return the well represented by this data row
   */
  private Well getWell()
  {
    Integer plateNumber = _loader.getPlateNumberParser().parse(_cellFactory.getCell(
      _columnHeaders.getColumnIndex(RequiredRNAiLibraryColumn.PLATE),
      _rowIndex));
    if (plateNumber == -1) {
      return null;
    }
    String wellName = _loader.getWellNameParser().parse(_cellFactory.getCell(
      _columnHeaders.getColumnIndex(RequiredRNAiLibraryColumn.WELL),
      _rowIndex));
    if (wellName.equals("")) {
      return null;
    }
    Well well = new Well(_loader.getLibrary(), plateNumber, wellName);
    String vendorIdentifier = _cellFactory.getCell(
      _columnHeaders.getColumnIndex(RequiredRNAiLibraryColumn.VENDOR_IDENTIFIER),
      _rowIndex).getString();
    if (! (vendorIdentifier == null || vendorIdentifier.equals(""))) {
      well.setVendorIdentifier(vendorIdentifier);
    }
    return well;
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
    log.info("loading data for plate-well " + plateWellAbbreviation);
    
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
   * Get an abbreviation for the plate-well. For use in log messages.
   * @return an abbreviation for the plate-well
   */
  private String getPlateWellAbbreviation()
  {
    Integer plateNumber = _loader.getPlateNumberParser().parse(_cellFactory.getCell(
      _columnHeaders.getColumnIndex(RequiredRNAiLibraryColumn.PLATE),
      _rowIndex));
    if (plateNumber == -1) {
      return null;
    }
    String wellName = _loader.getWellNameParser().parse(_cellFactory.getCell(
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
      _loader.getGeneInfoProvider().getGeneInfoForEntrezgeneId(entrezgeneId, entrezgeneIdCell);
    if (geneInfo == null) {
      return null;
    }
    
    // buildin tha gene
    return new Gene(
      geneInfo.getGeneName(),
      entrezgeneId,
      entrezgeneSymbol,
      genbankAccessionNumber,
      geneInfo.getSpeciesName());
  }

  /**
   * Build and return the set of {@link SilencingReagent SilencingReagents} represented
   * by this data row.
   * @return the set of silencing reagents represented by this data row
   */
  private Set<SilencingReagent> getSilencingReagents(Gene gene)
  {
    Set<SilencingReagent> silencingReagents = new HashSet<SilencingReagent>();
    String sequences = _cellFactory.getCell(
      _columnHeaders.getColumnIndex(RequiredRNAiLibraryColumn.SEQUENCES),
      _rowIndex).getString();
    if (sequences == null || sequences.equals("")) {
      SilencingReagent silencingReagent =
        new SilencingReagent(gene, _loader.getUnknownSilencingReagentType(), "");
      silencingReagents.add(silencingReagent);
    }
    else {
      for (String sequence : sequences.split("[,;]")) {
        SilencingReagent silencingReagent =
          new SilencingReagent(gene, _loader.getSilencingReagentType(), sequence);
        silencingReagents.add(silencingReagent);        
      }
    }
    return silencingReagents;
  }
}
