// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.io.DataExporter;
import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class WellsDataExporter implements DataExporter<Well>
{
  // static members

  private static Logger log = Logger.getLogger(WellsDataExporter.class);
  private static final String LIST_DELIMITER = "; ";

  
  // instance data members

  private GenericEntityDAO _dao;
  private WellsDataExporterFormat _dataFormat;
  

  // public constructors and methods

  public WellsDataExporter(GenericEntityDAO dao,
                           WellsDataExporterFormat dataFormat)
  {
    _dao = dao;
    _dataFormat = dataFormat;
  }

  public InputStream export(final List<Well> wells)
  {
    // TODO: logUserActivity("downloadWellSearchResults");
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    _dao.doInTransaction(new DAOTransaction() 
    {
      @SuppressWarnings("unchecked")
      public void runTransaction() 
      {
        List<Well> inflatedWells = eagerFetchWellsForDownloadWellSearchResults(wells);
        try {
          if (_dataFormat.equals(WellsDataExporterFormat.SDF)) {
            PrintWriter searchResultsPrintWriter = new PrintWriter(out);
            writeSDFileSearchResults(searchResultsPrintWriter, inflatedWells);
            searchResultsPrintWriter.close();
          }
          else {
            HSSFWorkbook searchResultsWorkbook = new HSSFWorkbook();
            writeExcelFileSearchResults(searchResultsWorkbook, inflatedWells);
            searchResultsWorkbook.write(out);
            out.close();
          }
        }
        catch (IOException e)
        {
          throw new DAOTransactionRollbackException("could not create export file", e);
        }
        finally {
          IOUtils.closeQuietly(out);
        }
      }
    });
    return new ByteArrayInputStream(out.toByteArray()); 
  }

  public String getFileName()
  {
    return "wellSearchResults." +_dataFormat.name().toLowerCase();
  }
  
  public String getFormatName()
  {
    return _dataFormat.toString();
  }

  public String getMimeType()
  {
    return _dataFormat.getMimeType();
  }


  // private methods

  private List<Well> eagerFetchWellsForDownloadWellSearchResults(final List<Well> wells) {
    // eager fetch all of the data that will be needed to generate the downloaded file 
    Set<Library> libraries = new HashSet<Library>();
    List<Well> inflatedWells = new ArrayList<Well>();
    for (Well well : wells) {
      libraries.add(well.getLibrary());
    }
    // optimization: eager fetch full set of wells from each library only if
    // many wells are retrieved from each library (on average); this is just
    // a heuristic and the '100' value is quite arbitrary
    if (wells.size() / libraries.size() < 100) {
      for (Well well : wells) {
        if (well.getLibrary().getScreenType().equals(ScreenType.SMALL_MOLECULE)) {
          inflatedWells.add(_dao.reloadEntity(well,
                                              true,
                                              "hbnMolfile",
                                              "hbnCompounds.compoundNames",
                                              "hbnCompounds.casNumbers",
                                              "hbnCompounds.nscNumbers",
                                              "hbnCompounds.pubchemCids"));
        }
        else if (well.getLibrary().getScreenType().equals(ScreenType.RNAI)) {
          inflatedWells.add(_dao.reloadEntity(well,
                                              true,
                                              "hbnSilencingReagents.gene.genbankAccessionNumbers",
                                              "hbnSilencingReagents.gene.oldEntrezgeneIds",
                                              "hbnSilencingReagents.gene.oldEntrezgeneSymbols"));
        }
      }
    }
    else {
      for (Library library : libraries) {
        if (library.getScreenType().equals(ScreenType.SMALL_MOLECULE)) {
          _dao.reloadEntity(library,
                            true,
                            "hbnWells.hbnMolfile",
                            "hbnWells.hbnCompounds.compoundNames",
                            "hbnWells.hbnCompounds.casNumbers",
                            "hbnWells.hbnCompounds.nscNumbers",
                            "hbnWells.hbnCompounds.pubchemCids");
        }
        else if (library.getScreenType().equals(ScreenType.RNAI)) {
          _dao.reloadEntity(library,
                            true,
                            "hbnWells.hbnSilencingReagents.gene.genbankAccessionNumbers",
                            "hbnWells.hbnSilencingReagents.gene.oldEntrezgeneIds",
                            "hbnWells.hbnSilencingReagents.gene.oldEntrezgeneSymbols");
        }
      }
      for (Well well : wells) {
        inflatedWells.add(_dao.findEntityById(Well.class, well.getEntityId()));
      }
    }
    return inflatedWells;
  }
  
  public void writeSDFileSearchResults(PrintWriter searchResultsPrintWriter,
                                       List<Well> wells)
  {
    for (Well well : wells) {
      if (well.getLibrary().getScreenType().equals(ScreenType.SMALL_MOLECULE)) {
        writeToSDFile(searchResultsPrintWriter, well);
      }
    }
  }
  
  public void writeExcelFileSearchResults(HSSFWorkbook searchResultsWorkbook,
                                          List<Well> wells)
  {
    HSSFCellStyle style = createHeaderStyle(searchResultsWorkbook);
    HSSFSheet rnaiSheet = createRNAiSheet(searchResultsWorkbook, style);
    searchResultsWorkbook.setSheetName(0, "Genes");
    HSSFSheet compoundSheet = createCompoundSheet(searchResultsWorkbook, style);
    searchResultsWorkbook.setSheetName(1, "Compounds");
    int rnaiSheetRow = 1;
    int compoundSheetRow = 1;
    for (Well well : wells) {
      if (well.getLibrary().getScreenType().equals(ScreenType.RNAI)) {
        if (writeWellToRNAiSheet(well, rnaiSheet, rnaiSheetRow)) {
          rnaiSheetRow ++;
        }
      }
      if (well.getLibrary().getScreenType().equals(ScreenType.SMALL_MOLECULE)) {
        if (writeWellToCompoundSheet(well, compoundSheet, compoundSheetRow)) {
          compoundSheetRow ++;
        }
      }
    }
    // note: remove compounds sheet first, in case genes sheet is also removed
    if (compoundSheetRow == 1) {
      searchResultsWorkbook.removeSheetAt(1);
    }
    if (rnaiSheetRow == 1) {
      searchResultsWorkbook.removeSheetAt(0);
    }
  }
  
  private HSSFCellStyle createHeaderStyle(HSSFWorkbook searchResultsWorkbook) {
    HSSFCellStyle style = searchResultsWorkbook.createCellStyle();
    HSSFFont font = searchResultsWorkbook.createFont();
    font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
    font.setUnderline(HSSFFont.U_SINGLE);
    style.setFont(font);
    return style;
  }

  // TODO: factor out a generic createSheet(String[]) method
  private HSSFSheet createRNAiSheet(HSSFWorkbook searchResultsWorkbook, HSSFCellStyle style) {
    HSSFSheet rnaiSheet = searchResultsWorkbook.createSheet();
    HSSFRow rnaiHeaderRow = rnaiSheet.createRow(0);
    rnaiHeaderRow.createCell((short) 0).setCellValue("Library");
    rnaiHeaderRow.createCell((short) 1).setCellValue("Plate");
    rnaiHeaderRow.createCell((short) 2).setCellValue("Well");
    rnaiHeaderRow.createCell((short) 3).setCellValue("Well Type");
    rnaiHeaderRow.createCell((short) 4).setCellValue("Vendor Identifier");
    rnaiHeaderRow.createCell((short) 5).setCellValue("ICCB Number");
    rnaiHeaderRow.createCell((short) 6).setCellValue("EntrezGene ID");
    rnaiHeaderRow.createCell((short) 7).setCellValue("EntrezGene Symbol");
    rnaiHeaderRow.createCell((short) 8).setCellValue("GenBank Accession Number");
    rnaiHeaderRow.createCell((short) 9).setCellValue("Gene Name");
    for (short i = 0; i < 10; i ++) {
      rnaiHeaderRow.getCell(i).setCellStyle(style);      
    }
    return rnaiSheet;
  }

  private HSSFSheet createCompoundSheet(HSSFWorkbook searchResultsWorkbook, HSSFCellStyle style) {
    HSSFSheet compoundSheet = searchResultsWorkbook.createSheet();
    HSSFRow compoundHeaderRow = compoundSheet.createRow(0);
    compoundHeaderRow.createCell((short) 0).setCellValue("Library");
    compoundHeaderRow.createCell((short) 1).setCellValue("Plate");
    compoundHeaderRow.createCell((short) 2).setCellValue("Well");
    compoundHeaderRow.createCell((short) 3).setCellValue("Well Type");
    compoundHeaderRow.createCell((short) 4).setCellValue("Vendor Identifier");
    compoundHeaderRow.createCell((short) 5).setCellValue("ICCB Number");
    compoundHeaderRow.createCell((short) 6).setCellValue("Smiles");
    compoundHeaderRow.createCell((short) 7).setCellValue("Compound Names");
    compoundHeaderRow.createCell((short) 8).setCellValue("CAS Numbers");
    compoundHeaderRow.createCell((short) 9).setCellValue("NSC Numbers");
    compoundHeaderRow.createCell((short) 10).setCellValue("PubChem CID");
    compoundHeaderRow.createCell((short) 11).setCellValue("ChemBank ID");
    for (short i = 0; i < 12; i ++) {
      compoundHeaderRow.getCell(i).setCellStyle(style);      
    }
    return compoundSheet;
  }

  private boolean writeWellToRNAiSheet(Well well, HSSFSheet sheet, int rowNumber)
  {
    Gene gene = well.getGene();
    if (gene == null) {
      return false;
    }
    HSSFRow row = createRow(well, sheet, rowNumber);
    row.createCell((short) 6).setCellValue(gene.getEntrezgeneId());
    row.createCell((short) 7).setCellValue(gene.getEntrezgeneSymbol());
    Set<String> genbankAccessionNumbers = gene.getGenbankAccessionNumbers();
    if (genbankAccessionNumbers.size() > 0) {
      row.createCell((short) 8).setCellValue(StringUtils.makeListString(genbankAccessionNumbers, LIST_DELIMITER));
    }
    row.createCell((short) 9).setCellValue(gene.getGeneName());
    return true;
  }

  private boolean writeWellToCompoundSheet(Well well, HSSFSheet sheet, int rowNumber)
  {
    Compound compound = well.getPrimaryCompound();
    if (compound == null) {
      return false;
    }
    HSSFRow row = createRow(well, sheet, rowNumber);
    if (well.getSmiles() != null) {
      row.createCell((short) 6).setCellValue(well.getSmiles());
    }
    Set<String> compoundNames = compound.getCompoundNames();
    if (compoundNames.size() > 0) {
      row.createCell((short) 7).setCellValue(StringUtils.makeListString(compoundNames, LIST_DELIMITER));
    }
    Set<String> casNumbers = compound.getCasNumbers();
    if (casNumbers.size() > 0) {
      row.createCell((short) 8).setCellValue(StringUtils.makeListString(casNumbers, LIST_DELIMITER));
    }
    Set<String> nscNumbers = compound.getNscNumbers();
    if (nscNumbers.size() > 0) {
      row.createCell((short) 9).setCellValue(StringUtils.makeListString(nscNumbers, LIST_DELIMITER));
    }
    Set<String> pubchemCids = compound.getPubchemCids();
    if (pubchemCids.size() > 0) {
      row.createCell((short) 10).setCellValue(StringUtils.makeListString(pubchemCids, LIST_DELIMITER));
    }
    if (compound.getChembankId() != null) {
      row.createCell((short) 11).setCellValue(compound.getChembankId());
    }
    return true;
  }

  private HSSFRow createRow(Well well, HSSFSheet sheet, int rowNumber) {
    HSSFRow row = sheet.createRow(rowNumber);
    row.createCell((short) 0).setCellValue(well.getLibrary().getLibraryName());
    row.createCell((short) 1).setCellValue(well.getPlateNumber());
    row.createCell((short) 2).setCellValue(well.getWellName());
    row.createCell((short) 3).setCellValue(well.getWellType().getValue());
    if (well.getVendorIdentifier() != null) {
      row.createCell((short) 4).setCellValue(well.getVendorIdentifier());
    }
    if (well.getIccbNumber() != null) {
      row.createCell((short) 5).setCellValue(well.getIccbNumber());
    }
    return row;
  }

  /**
   * Write the well contents out as an SD file record to the print writer.
   * @param pw the SD file print writer
   */
  public void writeToSDFile(PrintWriter pw, Well well)
  {
    if (well.getMolfile() == null) {
      return;
    }
    pw.println(well.getMolfile());
    pw.println(">  <Library>");
    pw.println(well.getLibrary().getLibraryName());
    pw.println();
    pw.println(">  <Plate>");
    pw.println(well.getPlateNumber().intValue());
    pw.println();
    pw.println(">  <Well>");
    pw.println(well.getWellName());
    pw.println();
    pw.println(">  <Plate_Well>");
    pw.println(well.getPlateNumber() + well.getWellName());
    pw.println();
    pw.println(">  <Well_Type>");
    pw.println(well.getWellType().getValue());
    pw.println();
    pw.println(">  <Smiles>");
    pw.println(well.getSmiles());
    pw.println();
    if (well.getIccbNumber() != null) {
      pw.println(">  <ICCB_Number>");
      pw.println(well.getIccbNumber());
      pw.println();
    }
    if (well.getFullVendorIdentifier() != null) {
      pw.println(">  <Vendor_Identifier>");
      pw.println(well.getFullVendorIdentifier());
      pw.println();
    }
    Compound compound = well.getPrimaryCompound();
    if (compound != null) {
      for (String compoundName : compound.getCompoundNames()) {
        pw.println(">  <Compound_Name>");
        pw.println(compoundName);
        pw.println();
      }
      for (String casNumber : compound.getCasNumbers()) {
        pw.println(">  <CAS_Number>");
        pw.println(casNumber);
        pw.println();
      }
      for (String nscNumber : compound.getNscNumbers()) {
        pw.println(">  <NSC_Number>");
        pw.println(nscNumber);
        pw.println();
      }
      for (String pubchemCid : compound.getPubchemCids()) {
        pw.println(">  <PubChem_CID>");
        pw.println(pubchemCid);
        pw.println();        
      }
      if (compound.getChembankId() != null) {
        pw.println(">  <ChemBank_ID>");
        pw.println(compound.getChembankId());
        pw.println();        
      }
    }
    pw.println("$$$$");
  }
}
