// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.screens;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.SortedMap;
import java.util.TreeMap;

import jxl.Sheet;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.ScreenDAO;
import edu.harvard.med.screensaver.io.FatalParseException;
import edu.harvard.med.screensaver.io.workbook2.Cell;
import edu.harvard.med.screensaver.io.workbook2.CellVocabularyParser;
import edu.harvard.med.screensaver.io.workbook2.ParseErrorManager;
import edu.harvard.med.screensaver.io.workbook2.Workbook;
import edu.harvard.med.screensaver.io.workbook2.WorkbookParseError;
import edu.harvard.med.screensaver.io.workbook2.Cell.Factory;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.StudyType;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

public class MedicinalCompoundsStudyCreator
{

  // static members

  private static Logger log = Logger.getLogger(MedicinalCompoundsStudyCreator.class);

  private static final int STUDY_NUMBER = 100002;
  private static final String TITLE = "Annotations on Suitability of Compounds: G. Cuny & K. Lee";
  private static final String SUMMARY =
    "Note for screeners regarding medchem annotation:\n" +
    "For historical reasons, the screening libraries contain many compounds that may be unsuitable for further study.  These include compounds that are unstable and/or reactive, the very properties that may have contributed to apparent activity upon screening.  Although experiments are needed to establish definitively whether compounds are false (or physiologically uninteresting) positives in a given screen, some compounds have obvious, chemically offensive functionalities from a structural point of view.  Those compounds can be de-prioritized, so that attention can be directed to compounds that are more likely to be true positives.  " +
    "The medicinal chemistry group evaluates the structures of library compounds on an ongoing basis, and the following annotations are applied:\n" +
    "'A': Compounds that have no obvious liabilities and that are deemed acceptable for initial follow-up work.\n" +
    "'B': Compounds that are deemed risky for follow-up and, if resources are limiting, should probably be given lower priority than Category A compounds.\n" +
    "'C': Compounds that are not recommended for follow-up, and, if resources are limiting, should be given lower priority than Category A and B compounds.\n" +
    "No flag: Compounds that have not yet been evaluated by the medicinal chemistry group.";
  private static final String A_NO_SPECIFIC_CONCERNS = "A: No specific concerns";
  private static final String B_POTENTIAL_LIABILITY = "B: Potential liability";
  private static final String C_SUBSTANTIAL_LIABILITY = "C: Substantial liability";
  private static final SortedMap<String,String> VALID_MEDCHEM_COMMENT_VALUES = new TreeMap<String,String>();
  static {
    VALID_MEDCHEM_COMMENT_VALUES.put("A", A_NO_SPECIFIC_CONCERNS);
    VALID_MEDCHEM_COMMENT_VALUES.put("a", A_NO_SPECIFIC_CONCERNS);
    VALID_MEDCHEM_COMMENT_VALUES.put("B", B_POTENTIAL_LIABILITY);
    VALID_MEDCHEM_COMMENT_VALUES.put("b", B_POTENTIAL_LIABILITY);
    VALID_MEDCHEM_COMMENT_VALUES.put("C", C_SUBSTANTIAL_LIABILITY);
    VALID_MEDCHEM_COMMENT_VALUES.put("c", C_SUBSTANTIAL_LIABILITY);
    VALID_MEDCHEM_COMMENT_VALUES.put("", null);
  }
  private static final String MECHEM_COMMENT_COLUMN_HEADER = "Medchem comment";
  private static final String VENDOR_ID_COLUMN_HEADER = "Vendor_ID";
  private static final String VENDOR_COLUMN_HEADER = "Vendor";
  private static final String PLATE_COLUMN_HEADER = "Plate";
  private static final String WELL_COLUMN_HEADER = "Well";

  public static void main(String[] args)
  {
    final CommandLineApplication app = new CommandLineApplication(args);
    app.addCommandLineOption(OptionBuilder.isRequired().hasArg().withArgName("workbook file").withLongOpt("input-file").create("f"));
    try {
      app.processOptions(true, true);
    }
    catch (ParseException e1) {
      System.exit(1);
    }
    final GenericEntityDAO dao = (GenericEntityDAO) app.getSpringBean("genericEntityDao");
    final ScreenDAO screenDao = (ScreenDAO) app.getSpringBean("screenDao");
    dao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        try {
          Screen study = dao.findEntityByProperty(Screen.class, "screenNumber", STUDY_NUMBER);
          if (study != null) {
            screenDao.deleteStudy(study);
          }

          ScreeningRoomUser labHead = ScreenCreator.findOrCreateScreeningRoomUser(dao, "Gregory", "Cuny", "gcuny@rics.bwh.harvard.edu");
          ScreeningRoomUser leadScreener = ScreenCreator.findOrCreateScreeningRoomUser(dao, "Kyungae", "Lee", "kyungae_lee@hms.harvard.edu");

          study = new Screen(leadScreener, labHead, STUDY_NUMBER, ScreenType.SMALL_MOLECULE, StudyType.IN_SILICO, TITLE);
          study.setSummary(SUMMARY);
          study.setShareable(true);

          AnnotationType annotType = study.createAnnotationType("Notes on Suitability",
                                                               A_NO_SPECIFIC_CONCERNS + ". " +
                                                               B_POTENTIAL_LIABILITY + ". " +
                                                               C_SUBSTANTIAL_LIABILITY + ". " +
                                                               "No value indicates the compound has not yet been reviewed by this study.",
                                                               false);
          ParseErrorManager errors = new ParseErrorManager();
          int n = loadAndCreateReagents(app.getCommandLineOptionValue("f", File.class), dao, annotType, errors);
          dao.saveOrUpdateEntity(study);
          log.info("created " + n + " annotations");
          if (errors.getHasErrors()) {
            log.error("Encountered " + errors.getErrors().size() + " error(s).");
            for (WorkbookParseError error : errors.getErrors()) {
              log.error(error.toString());
            }
          }
        }
        catch (Exception e) {
          throw new DAOTransactionRollbackException(e);
        }
      }
    });
    log.info("study successfully added to database");
  }

  protected static int loadAndCreateReagents(File workbookFile,
                                             GenericEntityDAO dao,
                                             AnnotationType annotType,
                                             ParseErrorManager errors)
    throws FileNotFoundException
  {
    int n = 0;
    Workbook workbook = new Workbook(workbookFile, errors);

    for (int iSheet = 0; iSheet < workbook.getWorkbook().getNumberOfSheets(); ++iSheet) {
      Sheet sheet = workbook.getWorkbook().getSheet(iSheet);
      Factory cellFactory = new edu.harvard.med.screensaver.io.workbook2.Cell.Factory(workbook, iSheet, errors);
      n += loadAndCreateReagentsFromSheet(sheet, cellFactory, dao, annotType, errors);
    }
    return n;
  }

  private static int loadAndCreateReagentsFromSheet(Sheet sheet, Factory cellFactory, GenericEntityDAO dao, AnnotationType annotType, ParseErrorManager errors)
  {
    int n = 0;
    CellVocabularyParser<String> MEDCHEM_COMMENT_CELL_PARSER = new CellVocabularyParser<String>(VALID_MEDCHEM_COMMENT_VALUES, null, errors, "bad annotation value");
    try {
      short iVendorColumn = (short) findColumnForHeader(sheet, VENDOR_COLUMN_HEADER);
      short iVendorIdColumn = (short) findColumnForHeader(sheet, VENDOR_ID_COLUMN_HEADER);
      short iMedChemCommentColumn = (short) findColumnForHeader(sheet, MECHEM_COMMENT_COLUMN_HEADER);
      short iPlateColumn = (short) findColumnForHeader(sheet, PLATE_COLUMN_HEADER);
      short iWellColumn = (short) findColumnForHeader(sheet, WELL_COLUMN_HEADER);
      if (iVendorColumn >= 0 && iVendorIdColumn >= 0 && iMedChemCommentColumn >= 0) {
        for (int iRow = 1; iRow < sheet.getRows(); ++iRow) {
          Cell vendorCell = (Cell) cellFactory.getCell(iVendorColumn, iRow, true).clone();
          Cell vendorIdCell = (Cell) cellFactory.getCell(iVendorIdColumn, iRow, true).clone();
          if (vendorCell != null && vendorIdCell != null) {
            Cell medChemCommentCell = (Cell) cellFactory.getCell(iMedChemCommentColumn, iRow, false).clone();
            Reagent reagent = findReagent(cellFactory,
                                          dao,
                                          iPlateColumn,
                                          iWellColumn,
                                          iRow,
                                          vendorCell,
                                          vendorIdCell);
            if (reagent != null) {
              String value = MEDCHEM_COMMENT_CELL_PARSER.parse(medChemCommentCell);
              if (annotType.createAnnotationValue(reagent, value) != null) {
                ++n;
              }
            }
          }
        }
      }
      return n;
    }
    catch (FatalParseException e) {
      errors.addError(e.getMessage());
      return 0;
    }
  }

  private static Reagent findReagent(Factory cellFactory,
                                     GenericEntityDAO dao,
                                     short iPlateColumn,
                                     short iWellColumn,
                                     int iRow,
                                     Cell vendorCell,
                                     Cell vendorIdCell)
  {
    ReagentVendorIdentifier rvi = new ReagentVendorIdentifier(vendorCell.getAsString(), vendorIdCell.getAsString());
    Reagent reagent = dao.findEntityById(Reagent.class, rvi);
    if (reagent == null) {
      Cell plateCell = (Cell) cellFactory.getCell(iPlateColumn, iRow, true).clone();
      Cell wellCell = (Cell) cellFactory.getCell(iWellColumn, iRow, true).clone();
      WellKey wellKey = new WellKey(plateCell.getInteger(), wellCell.getString());
      //log.warn("reagent does not exist with ID " + rvi);
      Well well = dao.findEntityById(Well.class, wellKey.toString());
      if (well == null) { 
        log.error("unknown reagent " + rvi + "; looking for reagent in well, but no such well " + wellKey);
      }
      else {
        reagent = well.getReagent();
        if (reagent == null) {
          log.error("unknown reagent " + rvi + " and no reagent in well " + wellKey);
        }
      }
    }
    return reagent;
  }

  private static int findColumnForHeader(Sheet sheet, String headerName)
  {
    for (int iColumn = 0; iColumn < sheet.getColumns(); ++iColumn) {
      if (sheet.getRow(0)[iColumn].getContents().equals(headerName)) {
        return iColumn;
      }
    }
    return -1;
  }
}