// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.cherrypicks;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jxl.Cell;
import jxl.CellType;
import jxl.LabelCell;
import jxl.NumberCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyInfo;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screens.CherryPickRequest;
import edu.harvard.med.screensaver.model.screens.LabCherryPick;
import edu.harvard.med.screensaver.model.screens.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.screens.ScreenerCherryPick;
import edu.harvard.med.screensaver.service.libraries.rnai.LibraryPoolToDuplexWellMapper;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * Imports legacy RNAi cherry pick data from an Excel workbook. Intended to be
 * used as a one-time import. The workbook is expected to have 2 worksheets:
 * <ul>
 * <li>"cherry pick copies": contains the starting volumes for each RNAi duplex
 * library. Columns:
 * <ul>
 * <li>plate
 * <li>well
 * <li>volume
 * </ul>
 * <li>"cherry pick requests": contains the master list of RNAi duplex cherry
 * picks requested by all RNAi screeners. Columns:
 * <ul>
 * <li>Visit ID
 * <li>Source Plate
 * <li>Source Copy
 * <li>Source Well
 * <li>Source Plate Type
 * <li>Destination Well
 * <li>Destination Plate Type
 * <li>Person Visiting
 * <li>Screen Number
 * <li>Volume
 * </ul>
 * </ul>
 * <p>
 * The "cherry pick requests" sheet must have a ScreenDB "Visit ID" column,
 * which associates each cherry pick from the RNAi Cherry Pick Request whence it
 * originated. The "Visit ID" maps to Screensaver's
 * CherryPickRequest.cherryPickRequestNumber field.
 * <p>
 * Note:
 * <ul>
 * <li>The LabCherryPicks are not being mapped to their respective
 * CherryPickAssayPlates.
 * <li>The ScreenerCherryPicks that are created for each LabCherryPick are just
 * the duplexes, not the pools, which will require a reverse lookup. We can do
 * this, but haven't done so yet.
 * </ul>
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class AllCherryPicksImporter
{
  // static members

  private static Logger log = Logger.getLogger(AllCherryPicksImporter.class);

  private static final String CHERRY_PICK_COPIES_SHEET_NAME = "cherry pick copies";
  private static final String CHERRY_PICK_REQUESTS_SHEET_NAME = "cherry pick requests";
  
  private static final int COPY_PLATE_COLUMN_INDEX = 0;
  private static final int COPY_COPY_NAME_COLUMN_INDEX = 1;
  private static final int COPY_VOLUME_COLUMN_INDEX = 2;

  private static final WorkbookSettings WORKBOOK_SETTINGS = new WorkbookSettings();

  protected static final int VISIT_ID_COLUMN_INDEX = 0;
  protected static final int PLATE_NUMBER_COLUMN_INDEX = 1;
  protected static final int SOURCE_COPY_COLUMN_INDEX = 2;
  protected static final int WELL_NAME_COLUMN_INDEX = 3;
  protected static final int VOLUME_COLUMN_INDEX = 9;

  private static final int FIRST_DATA_ROW_INDEX = 1;
  
  static {
    WORKBOOK_SETTINGS.setIgnoreBlanks(true);
  }
  
  @SuppressWarnings("static-access")
  public static void main(String[] args)
  {
    try {
      CommandLineApplication app = new CommandLineApplication(args);
      app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("workbook file").withLongOpt("input-file").create("f"));
      app.addCommandLineOption(OptionBuilder.hasArg().withArgName("cherry pick request number").withLongOpt("cpr-number").withDescription("for debugging").create("n"));
      app.addCommandLineOption(OptionBuilder.hasArg().withArgName("row#").withLongOpt("from-row").withDescription("for debugging").create("r"));
      app.addCommandLineOption(OptionBuilder.hasArg(false).withLongOpt("fail-fast").withDescription("for debugging").create("x"));
      if (!app.processOptions(true, true)) {
        System.exit(1);
      }
      AllCherryPicksImporter importer = (AllCherryPicksImporter) app.getSpringBean("allCherryPicksImporter");
      importer.setFromRow(app.getCommandLineOptionValue("r", Integer.class));
      importer.setCherryPickRequestNumber(app.getCommandLineOptionValue("n", Integer.class));
      importer.setFailFast(app.isCommandLineFlagSet("x"));
      File workbookFile = app.getCommandLineOptionValue("f", File.class);
      BufferedInputStream bis = new BufferedInputStream(new FileInputStream(workbookFile));
      importer.importCherryPickCopiesAndRnaiCherryPicks(bis);
    }
    catch (Exception e) {
      e.printStackTrace();
      log.error(e.toString());
      System.err.println("error: " + e.toString());
      System.exit(1);
    }
  }

  
  // instance data members
  
  private DAO _dao;
  private LibraryPoolToDuplexWellMapper _libraryPoolToDuplexWellMapper;
  private Library _cachedLibrary;
  private Boolean _failFast; // for debugging
  private Integer _fromRow; // for debugging
  private Integer _cherryPickRequestNumber; // for debugging



  // public constructors and methods
  
  public AllCherryPicksImporter(DAO dao,
                                LibraryPoolToDuplexWellMapper libraryPoolToDuplexWellMapper)
  {
    _dao = dao;
    _libraryPoolToDuplexWellMapper = libraryPoolToDuplexWellMapper;
  }

  private void setCherryPickRequestNumber(Integer cherryPickRequestNumber)
  {
    _cherryPickRequestNumber = cherryPickRequestNumber;
  }

  public void setFromRow(Integer fromRow)
  {
    _fromRow = fromRow;
  }
  
  public int getFromRow()
  {
    if (_fromRow == null) {
      return FIRST_DATA_ROW_INDEX;
    }
    return Math.max(FIRST_DATA_ROW_INDEX, _fromRow - 1);
  }
  
  public void setFailFast(boolean failFast)
  {
    _failFast = failFast;
  }

  public boolean isFailFast()
  {
    return _failFast == null ? false : _failFast;
  }

  public void importCherryPickCopiesAndRnaiCherryPicks(InputStream workbookInputStream) throws FatalParseException
  {
    try {
      final Workbook workbook = Workbook.getWorkbook(workbookInputStream, WORKBOOK_SETTINGS);
      _dao.doInTransaction(new DAOTransaction() 
      {
        public void runTransaction() 
        {
          importCherryPickCopies(workbook);
          importRnaiCherryPicks(workbook);
        }
      });
    }
    catch (Exception e) {
      throw new FatalParseException(e);
    }
  }
  
  public Set<Copy> importCherryPickCopies(File workbookFile) 
    throws CherryPickCopiesDataException, FatalParseException
  {
    BufferedInputStream bis = null;
    try {
      bis = new BufferedInputStream(new FileInputStream(workbookFile));
      Workbook workbook = Workbook.getWorkbook(bis, WORKBOOK_SETTINGS);
      return importCherryPickCopies(workbook);
    }
    catch (BiffException e) {
      throw new FatalParseException(e);
    }
    catch (IOException e) {
      throw new FatalParseException(e);
    }
    finally {
      IOUtils.closeQuietly(bis);
    }
  }

  public void importRnaiCherryPicks(File workbookFile) 
    throws CherryPicksDataException, FatalParseException
  {
    BufferedInputStream bis = null;
    try {
      bis = new BufferedInputStream(new FileInputStream(workbookFile));
      Workbook workbook = Workbook.getWorkbook(bis, WORKBOOK_SETTINGS);
      importRnaiCherryPicks(workbook);
    }
    catch (BiffException e) {
      throw new FatalParseException(e);
    }
    catch (IOException e) {
      throw new FatalParseException(e);
    }
    finally {
      IOUtils.closeQuietly(bis);
    }
  }

  public Set<Copy> importCherryPickCopies(Workbook workbook) 
    throws CherryPickCopiesDataException
  {

    final Sheet sheet = workbook.getSheet(CHERRY_PICK_COPIES_SHEET_NAME);
    final Set<Copy> copies = new HashSet<Copy>();
    final Map<CopyInfo,Integer> copyInfo2Volume = new HashMap<CopyInfo,Integer>();
    _dao.doInTransaction(new DAOTransaction() 
    {
      public void runTransaction() 
      {
        for (int iRow = 1; iRow < sheet.getRows(); iRow++) {
          if (isRowBlank(sheet, iRow)) {
            break;
          }
          Integer plate;
          String copyName;
          BigDecimal volume;
          int iCol = 0;
          try {
            plate = new Integer((int) ((NumberCell) sheet.getCell(COPY_PLATE_COLUMN_INDEX, iRow)).getValue());
            ++iCol;
            copyName = ((LabelCell) sheet.getCell(COPY_COPY_NAME_COLUMN_INDEX, iRow)).getString();
            ++iCol;
            volume = new BigDecimal(((NumberCell) sheet.getCell(COPY_VOLUME_COLUMN_INDEX, iRow)).getValue());
          }
          catch (Exception e) {
            throw new CherryPickCopiesDataException("illegal data type: "  + e.getMessage(), iRow, iCol);
          }

          Library library = _dao.findLibraryWithPlate(plate);
          if (library == null) {
            throw new CherryPickCopiesDataException("invalid plate number (no library for plate)", iRow);
          }
          Copy copy = library.getCopy(copyName);
          if (copy == null) {
            copy = new Copy(library, CopyUsageType.FOR_CHERRY_PICK_SCREENING, copyName);
            log.info("created " + copy + " for library " + library.getLibraryName());
          }
          CopyInfo copyInfo = copy.getCopyInfo(plate);
          if (copyInfo == null) {
            copyInfo = new CopyInfo(copy, plate, "<unknown>", PlateType.EPPENDORF, volume);
            log.info("created " + copyInfo + " for " + copy);
          }
          if (copyInfo2Volume.containsKey(copyInfo)) {
            Integer expectedVolume = copyInfo2Volume.get(copyInfo);
            if (!volume.equals(expectedVolume)) {
              throw new CherryPickCopiesDataException("volume not consistent for plate " + plate, iRow);
            }
          }
          copies.add(copy);
        }
      }
    });
    log.info("imported cherry pick copies");
    return copies;
  }
  
  public void importRnaiCherryPicks(Workbook workbook)
  throws CherryPicksDataException, FatalParseException
  {
    final Sheet sheet = workbook.getSheet(CHERRY_PICK_REQUESTS_SHEET_NAME);
    _dao.doInTransaction(new DAOTransaction() 
    {
      public void runTransaction() 
      {
        int iRow = 0;
        boolean encounteredErrors = false;
        Map<Integer,CherryPickRequest> visitId2CherryPickRequest = buildVisitIdToCherryPickRequestMap();
        CherryPickRequest cherryPickRequest = null;
        Collection<CherryPickRequest> touchedCherryPickRequests = new HashSet<CherryPickRequest>();

        for (iRow = getFromRow(); iRow < sheet.getRows(); iRow++) {
          if (isRowBlank(sheet, iRow)) {
            break;
          }
          Integer visitId = null;
          Well labCherryPickWell = null;
          try {
            NumberCell visitIdCell = (NumberCell) sheet.getCell(VISIT_ID_COLUMN_INDEX, iRow);
            NumberCell plateNumberCell = (NumberCell) sheet.getCell(PLATE_NUMBER_COLUMN_INDEX, iRow);
            Cell wellNameCell = sheet.getCell(WELL_NAME_COLUMN_INDEX, iRow);
            Cell sourceCopyCell = sheet.getCell(SOURCE_COPY_COLUMN_INDEX, iRow);
            visitId = (int) visitIdCell.getValue();

            cherryPickRequest = visitId2CherryPickRequest.get(visitId);
            addImportCommentsToCherryPickRequest(cherryPickRequest);
            
            if (cherryPickRequest == null) {
              throw new CherryPicksDataException("no such cherry pick request", visitId, iRow);
            }

            // for debugging: only import the cherry pick of the specified cherry pick request number
            if (_cherryPickRequestNumber != null &&
              !cherryPickRequest.getCherryPickRequestNumber().equals(_cherryPickRequestNumber)) {
              continue;
            }

            if (cherryPickRequest.getScreenerCherryPicks().size() > 0 && 
              !touchedCherryPickRequests.contains(cherryPickRequest)) {
              throw new CherryPicksDataException("cherry pick request " + 
                                                 cherryPickRequest.getCherryPickRequestNumber() + 
                                                 " already has cherry picks", 
                                                 visitId,
                                                 iRow);
            }

            if (cherryPickRequest.getMicroliterTransferVolumePerWellApproved() == null) {
              if (cherryPickRequest.getMicroliterTransferVolumePerWellRequested() == null) {
                throw new CherryPicksDataException("both requested and approved volumes are null for cherry pick request " + 
                                                   cherryPickRequest.getCherryPickRequestNumber(), 
                                                   visitId,
                                                   iRow);
              }
              else {
                // set (missing) approved volume to requested volume, which is
                // okay to do, since we know the volume was approved by virtue
                // of the cherry pick (visit) being included in the
                // AllCherryPicks.xls file
                log.info("CPR " + visitId + " setting cherry pick request approved volume to requested volume: " + 
                         cherryPickRequest.getMicroliterTransferVolumePerWellRequested());
                cherryPickRequest.setMicroliterTransferVolumePerWellApproved(cherryPickRequest.getMicroliterTransferVolumePerWellRequested());
              }
            }

            WellKey wellKey = new WellKey((int) plateNumberCell.getValue(),
                                          wellNameCell.getContents());
            labCherryPickWell = _dao.findWell(wellKey);
            if (labCherryPickWell == null) {
              throw new CherryPicksDataException("no such well: " + wellKey,
                                                 visitId,
                                                 iRow, 
                                                 WELL_NAME_COLUMN_INDEX);
            }
            Well screenerCherryPickWell = findScreenerCherryPickWell(iRow, visitId, labCherryPickWell);
            ScreenerCherryPick screenerCherryPick = new ScreenerCherryPick(cherryPickRequest, screenerCherryPickWell);
            LabCherryPick labCherryPick = new LabCherryPick(screenerCherryPick, labCherryPickWell);
            // note: we validate volume *after* creating the lab cherry pick,
            // only because the potential DuplicateEntityException is the more
            // critical error to report, in case both volume-validation and
            // duplicate-lab-cherrry-pick errors are triggered for the same
            // row
            validateLabCherryPickVolumeMatchesCherryPickRequestVolume(cherryPickRequest,
                                                                      sheet,
                                                                      iRow);
            labCherryPick.setAllocated(findCopy((int) plateNumberCell.getValue(),
                                                sourceCopyCell.getContents(),
                                                iRow));
            // TODO: labCherryPick.setMapped().  Will require knowing the plate each cherry pick was mapped to.
          }
          catch (CherryPicksDataException e) {
            log.error(e.getMessage());
            encounteredErrors = true;
          }
          catch (DuplicateEntityException e) {
            log.error("CPR " + visitId + " should have been split into multiple visits, since there are duplicate cherry picks from well " + labCherryPickWell);
            encounteredErrors = true;
          }
          catch (BusinessRuleViolationException e) {
            log.error("CPR " + visitId + " could not create a lab cherry pick for well " + labCherryPickWell + ": " + e.getMessage());
            encounteredErrors = true;
          }
          catch (Exception e) {
            log.error("CPR " + visitId + "unexpected error at row " + iRow + ":" + e.toString());
            encounteredErrors = true;
          }
          finally {
            touchedCherryPickRequests.add(cherryPickRequest);
          }
          if (encounteredErrors && isFailFast()) {
            break;
          }
        }

        if (encounteredErrors) {
          throw new FatalParseException("import failed due to errors (see log); database remains unchanged");
        }
        else {
          log.info("imported cherry pick requests");
        }
      }

    });
  }
  
  
  // private methods
  
  private void addImportCommentsToCherryPickRequest(CherryPickRequest cherryPickRequest)
  {
    StringBuilder s = new StringBuilder();
    s.append(cherryPickRequest.getComments()).append('\n');
    s.append(DateFormat.getDateInstance(DateFormat.SHORT).format(new Date())).append(": ");
    s.append("Imported cherry picks from AllCherryPicks.xls.  " +
        "Note that Screener Cherry Picks reflect duplex wells, and not pool wells.  " +
        "Also note that the mapping of Lab Cherry Picks to their respective Cherry Pick Plates has not been imported.");
  }

  private Well findScreenerCherryPickWell(int iRow, Integer visitId, Well labCherryPickWell)
  {
    // ant 2007-04-26: for imported cherry picks, decision is that it's okay to just use the duplex
    // well in the screener cherry pick, rather than trying to determine the pool well, which 
    // requires a reverse lookup and sometimes fails
    return labCherryPickWell;
    //  Well screenerCherryPickWell = _libraryPoolToDuplexWellMapper.mapDuplexWellToPoolWell(labCherryPickWell);
    //  if (screenerCherryPickWell == null) {
    //  if (labCherryPickWell.getSilencingReagents().size() > 1) {
    //  log.warn("CPR " + visitId + " cherry pick well " + labCherryPickWell + 
    //  " appears to be a pool well: screenerCherryPick will be same as labCherryPick");
    //  screenerCherryPickWell = labCherryPickWell;
    //  }
    //  else {
    //  throw new CherryPicksDataException("cherry pick well " + labCherryPickWell + 
    //  " cannot be mapped to a pool well and does not appear to be a pool well itself", 
    //  visitId,
    //  iRow);
    //  }
    //  }
    //  return screenerCherryPickWell;
  }

  private void validateLabCherryPickVolumeMatchesCherryPickRequestVolume(CherryPickRequest cherryPickRequest,
                                                                         Sheet sheet,
                                                                         int iRow)
  {
    Cell volumeCell = sheet.getCell(VOLUME_COLUMN_INDEX, iRow);
    BigDecimal cprVolume = cherryPickRequest.getMicroliterTransferVolumePerWellApproved();
    if (cprVolume == null) {
      throw new CherryPicksDataException("cherry pick request " + cherryPickRequest.getCherryPickRequestNumber() +
                                         " does not have an approved volume, so we cannot validate that " +
                                         "the imported cherry pick volume is correct",
                                         cherryPickRequest.getCherryPickRequestNumber(),
                                         iRow,
                                         VOLUME_COLUMN_INDEX);
    }

    BigDecimal labCherryPickVolume = new BigDecimal(volumeCell.getContents()).setScale(CherryPickRequest.VOLUME_SCALE);
    if (!labCherryPickVolume.equals(cprVolume)) {
      throw new CherryPicksDataException("cherry pick volume (" + volumeCell.getContents() + 
                                         ") does not match the cherry pick request approved volume (" + 
                                         cprVolume + ")",
                                         cherryPickRequest.getCherryPickRequestNumber(),
                                         iRow,
                                         VOLUME_COLUMN_INDEX);
    }
  }
    
  private Copy findCopy(int plateNumber, String copyName, int iRow)
  {
    if (_cachedLibrary == null || !_cachedLibrary.containsPlate(plateNumber)) {
      _cachedLibrary = _dao.findLibraryWithPlate(plateNumber);
      if (_cachedLibrary == null) {
        throw new CherryPickCopiesDataException("no library for plate " + plateNumber, iRow);
      }
    }
    Copy copy = _cachedLibrary.getCopy(copyName);
    if (copy == null) {
      throw new CherryPickCopiesDataException("no such copy " + copyName, iRow);
    }
    return copy;
  }

  // private methods

  private boolean isRowBlank(Sheet sheet, int row)
  {
    return sheet.getCell(0, row).getType().equals(CellType.EMPTY);
  }

  private Map<Integer,CherryPickRequest> buildVisitIdToCherryPickRequestMap()
  {
    Map<Integer,CherryPickRequest> visit2CherryPickRequest = new HashMap<Integer,CherryPickRequest>();
    List<RNAiCherryPickRequest> cherryPickRequests = _dao.findAllEntitiesWithType(RNAiCherryPickRequest.class);
    for (RNAiCherryPickRequest cherryPickRequest : cherryPickRequests) {
      visit2CherryPickRequest.put(cherryPickRequest.getCherryPickRequestNumber(),
                                  cherryPickRequest);
    }
    return visit2CherryPickRequest;
  }
}
