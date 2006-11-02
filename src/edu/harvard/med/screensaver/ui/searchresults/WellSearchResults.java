// $HeadURL: svn+ssh://js163@orchestra/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.ui.control.LibrariesController;


/**
 * A {@link SearchResults} for {@link Well Wells}.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class WellSearchResults extends SearchResults<Well>
{
  
  // private static final fields
  
  private static final Logger log = Logger.getLogger(WellSearchResults.class);

  private static final String LIBRARY   = "Library";
  private static final String PLATE     = "Plate";
  private static final String WELL      = "Well";
  private static final String WELL_TYPE = "Well Type";
  private static final String CONTENTS  = "Contents";
  
  
  // instance fields
  
  private LibrariesController _librariesController;
  
  
  // public constructor
  
  /**
   * Construct a new <code>WellSearchResultsViewer</code> object.
   * @param wells the list of wells
   */
  public WellSearchResults(
    List<Well> unsortedResults,
    LibrariesController librariesController)
  {
    super(unsortedResults);
    _librariesController = librariesController;
  }

  
  // overrides of public SearchResults methods

  @Override
  public boolean getIsDownloadable()
  {
    return true;
  }
  
  // implementations of the SearchResults abstract methods
  
  @Override
  public String showSummaryView()
  {
    // NOTE: if there were more ways to get to a well search results, then this method would
    // need to be more intelligent
    return _librariesController.viewWellSearchResults(this);
  }
  
  @Override
  protected List<String> getColumnHeaders()
  {
    List<String> columnHeaders = new ArrayList<String>();
    columnHeaders.add(LIBRARY);
    columnHeaders.add(PLATE);
    columnHeaders.add(WELL);
    columnHeaders.add(WELL_TYPE);
    columnHeaders.add(CONTENTS);
    return columnHeaders;
  }
  
  @Override
  protected boolean isCommandLink(String columnName)
  {
    return
      columnName.equals(LIBRARY) ||
      columnName.equals(WELL) ||
      (columnName.equals(CONTENTS) && getContentsCount(getEntity()) == 1);
  }
  
  @Override
  protected boolean isCommandLinkList(String columnName)
  {
    return columnName.equals(CONTENTS) && getContentsCount(getEntity()) > 1;
  }
  
  @Override
  protected Object getCellValue(Well well, String columnName)
  {
    if (columnName.equals(LIBRARY)) {
      return well.getLibrary().getShortName();
    }
    if (columnName.equals(PLATE)) {
      return well.getPlateNumber();
    }
    if (columnName.equals(WELL)) {
      return well.getWellName();
    }
    if (columnName.equals(WELL_TYPE)) {
      return well.getWellType();
    }
    if (columnName.equals(CONTENTS)) {
      return getContentsValue(well);
    }
    return null;
  }
  
  @Override
  protected Object cellAction(Well well, String columnName)
  {
    if (columnName.equals(LIBRARY)) {
      return _librariesController.viewLibrary(well.getLibrary(), null);
    }
    if (columnName.equals(WELL)) {
      return _librariesController.viewWell(well, this);
    }
    if (columnName.equals(CONTENTS)) {
      if (getGeneCount(well) == 1) {
        return _librariesController.viewGene(well.getGene(), this);
      }
      if (getCompoundCount(well) > 0) {
        String compoundId = (String) getRequestParameter("commandValue");
        Compound compound = null;
        for (Compound compound2 : well.getCompounds()) {
          if (compound2.getCompoundId().equals(compoundId)) {
            compound = compound2;
            break;
          }
        }
        return _librariesController.viewCompound(compound, this);
      }
    }
    return null;
  }
  
  @Override
  protected Comparator<Well> getComparatorForColumnName(String columnName)
  {
    if (columnName.equals(LIBRARY)) {
      return new Comparator<Well>() {
        public int compare(Well w1, Well w2) {
          return w1.getLibrary().getShortName().compareTo(w2.getLibrary().getShortName());
        }
      };
    }
    if (columnName.equals(PLATE)) {
      return new Comparator<Well>() {
        public int compare(Well w1, Well w2) {
          return w1.getPlateNumber().compareTo(w2.getPlateNumber());
        }
      };
    }
    if (columnName.equals(WELL)) {
      return new Comparator<Well>() {
        public int compare(Well w1, Well w2) {
          return w1.getWellName().compareTo(w2.getWellName());
        }
      };
    }
    if (columnName.equals(WELL_TYPE)) {
      return new Comparator<Well>() {
        public int compare(Well w1, Well w2) {
          return w1.getWellType().compareTo(w2.getWellType());
        }
      };
    }
    if (columnName.equals(CONTENTS)) {
      return new Comparator<Well>() {
        @SuppressWarnings("unchecked")
        public int compare(Well w1, Well w2) {
          Object o1 = getContentsValue(w1);
          String s1 = (o1 instanceof String) ?
            (String) o1 : ((List<String>) o1).get(0);
          Object o2 = getContentsValue(w2);
          String s2 = (o2 instanceof String) ?
            (String) o2 : ((List<String>) o2).get(0);
          return s1.compareTo(s2);
        }
      };
    }
    return null;
  }

  @Override
  protected void setEntityToView(Well well)
  {
    _librariesController.viewWell(well, this);
    _librariesController.viewGene(well.getGene(), this);
    _librariesController.viewCompound(well.getPrimaryCompound(), this);
  }

  @Override
  protected void writeSDFileSearchResults(PrintWriter searchResultsPrintWriter)
  {
    for (Well well : _currentSort) {
      well.writeToSDFile(searchResultsPrintWriter);
    }
  }
  
  @Override
  protected void writeExcelFileSearchResults(HSSFWorkbook searchResultsWorkbook)
  {
    HSSFCellStyle style = searchResultsWorkbook.createCellStyle();
    HSSFFont font = searchResultsWorkbook.createFont();
    font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
    font.setUnderline(HSSFFont.U_SINGLE);
    style.setFont(font);
    HSSFSheet sheet = searchResultsWorkbook.createSheet();
    HSSFRow headerRow = sheet.createRow(0);
    headerRow.createCell((short) 0).setCellValue("Library");
    headerRow.createCell((short) 1).setCellValue("Plate");
    headerRow.createCell((short) 2).setCellValue("Well");
    headerRow.createCell((short) 3).setCellValue("Well Type");
    headerRow.createCell((short) 4).setCellValue("Vendor Identifier");
    headerRow.createCell((short) 5).setCellValue("ICCB Number");
    headerRow.createCell((short) 6).setCellValue("Smiles");
    headerRow.createCell((short) 7).setCellValue("Compound Names");
    headerRow.createCell((short) 8).setCellValue("CAS Numbers");
    headerRow.createCell((short) 9).setCellValue("NSC Numbers");
    headerRow.createCell((short) 10).setCellValue("PubChem CID");
    headerRow.createCell((short) 11).setCellValue("ChemBank ID");
    headerRow.createCell((short) 12).setCellValue("EntrezGene ID");
    headerRow.createCell((short) 13).setCellValue("GenBank Accession Number");
    for (short i = 0; i < 14; i ++) {
      headerRow.getCell(i).setCellStyle(style);      
    }
    for (int i = 0; i < _currentSort.size(); i ++) {
      Well well = _currentSort.get(i);
      writeWellToExcelFileSearchResults(well, sheet, i + 1);
    }
  }
  
  private void writeWellToExcelFileSearchResults(Well well, HSSFSheet sheet, int rowNumber)
  {
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
    if (well.getSmiles() != null) {
      row.createCell((short) 6).setCellValue(well.getSmiles());
    }
    Compound compound = well.getPrimaryCompound();
    if (compound != null) {
      Set<String> compoundNames = compound.getCompoundNames();
      if (compoundNames.size() > 0) {
        row.createCell((short) 7).setCellValue(listStrings(compoundNames));
      }
      Set<String> casNumbers = compound.getCasNumbers();
      if (casNumbers.size() > 0) {
        row.createCell((short) 8).setCellValue(listStrings(casNumbers));
      }
      Set<String> nscNumbers = compound.getNscNumbers();
      if (nscNumbers.size() > 0) {
        row.createCell((short) 9).setCellValue(listStrings(nscNumbers));
      }
      if (compound.getPubchemCid() != null) {
        row.createCell((short) 10).setCellValue(compound.getPubchemCid());
      }
      if (compound.getChembankId() != null) {
        row.createCell((short) 11).setCellValue(compound.getChembankId());
      }
    }
    Set<Gene> genes = well.getGenes();
    if (genes.size() > 0) {
      Gene gene = genes.iterator().next();
      row.createCell((short) 12).setCellValue(gene.getEntrezgeneId());
      Set<String> genbankAccessionNumbers = gene.getGenbankAccessionNumbers();
      if (genbankAccessionNumbers.size() > 0) {
        row.createCell((short) 13).setCellValue(listStrings(genbankAccessionNumbers));
      }
    }
  }
  
  /**
   * Assumes the set of strings has at least one element!
   */
  private String listStrings(Set<String> strings)
  {
    StringBuffer stringBuffer = new StringBuffer();
    for (String string : strings) {
      stringBuffer.append(string).append("; ");
    }
    stringBuffer.setLength(stringBuffer.length() - 2);
    return stringBuffer.toString();
  }
  
  
  // private instance methods
  
  private Object getContentsValue(Well well)
  {
    int geneCount = getGeneCount(well);
    if (geneCount == 1) {
      return well.getGene().getGeneName();
    }
    if (geneCount > 1) {
      return "multiple genes";
    }
    int compoundCount = getCompoundCount(well);
    if (compoundCount == 1) {
      return well.getCompounds().iterator().next().getSmiles();
    }
    if (compoundCount > 1) {
      List<String> smiles = new ArrayList<String>();
      for (Compound compound : well.getCompounds()) {
        smiles.add(compound.getSmiles());
      }
      return smiles;
    }
    return "empty well";
  }
  
  private int getContentsCount(Well well)
  {
    return getGeneCount(well) + getCompoundCount(well);
  }
  
  private int getCompoundCount(Well well)
  {
    return well.getCompounds().size();
  }

  private int getGeneCount(Well well)
  {
    return well.getGenes().size();
  }
}
