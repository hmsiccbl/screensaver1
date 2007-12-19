// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/io/screens/IccbCompoundsStudyCreator.java $
// $Id: IccbCompoundsStudyCreator.java 2061 2007-12-17 15:57:14Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.screens;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;

import jxl.Sheet;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.io.FatalParseException;
import edu.harvard.med.screensaver.io.workbook2.Cell;
import edu.harvard.med.screensaver.io.workbook2.CellVocabularyParser;
import edu.harvard.med.screensaver.io.workbook2.ParseErrorManager;
import edu.harvard.med.screensaver.io.workbook2.Workbook;
import edu.harvard.med.screensaver.io.workbook2.Cell.Factory;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
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
  private static final String SUMMARY = "<pending>";
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
    dao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        try {
          deleteExistingStudy(dao);

          ScreeningRoomUser labHead = ScreenCreator.findOrCreateScreeningRoomUser(dao, "Gregory", "Cuny", "gcuny@rics.bwh.harvard.edu");
          ScreeningRoomUser leadScreener = ScreenCreator.findOrCreateScreeningRoomUser(dao, "Kyungae", "Lee", "kyungae_lee@hms.harvard.edu");

          Screen study = new Screen(leadScreener, labHead, STUDY_NUMBER, new Date(), ScreenType.SMALL_MOLECULE, StudyType.IN_VITRO, TITLE);
          study.setSummary(SUMMARY);

          AnnotationType annotType = study.createAnnotationType("Notes on Suitability",
                                                               A_NO_SPECIFIC_CONCERNS + ". " +
                                                               B_POTENTIAL_LIABILITY + ". " +
                                                               C_SUBSTANTIAL_LIABILITY + ". " +
                                                               "No value indicates the compound has not yet been reviewed by this study.",
                                                               false);
          int n = loadAndCreateReagents(app.getCommandLineOptionValue("f", File.class), dao, annotType);
          dao.saveOrUpdateEntity(study);
          log.info("created " + n + " annotations");
        }
        catch (Exception e) {
          throw new DAOTransactionRollbackException(e);
        }
      }

       private void deleteExistingStudy(GenericEntityDAO dao)
      {
        Screen study = dao.findEntityByProperty(Screen.class, "screenNumber", STUDY_NUMBER);
        if (study != null) {
          //dao.deleteEntity(study.getLeadScreener());
          //dao.deleteEntity(study.getLabHead());
          dao.deleteEntity(study);
          dao.flush();
          log.info("deleted existing study");
        }
      }
    });
    log.info("study successfully added to database");
  }

  protected static int loadAndCreateReagents(File workbookFile, GenericEntityDAO dao, AnnotationType annotType) throws FileNotFoundException
  {
    int n = 0;
    ParseErrorManager errors = new ParseErrorManager();
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
      short iVendorColumn = (short) findColumnForHeader(sheet, "Vendor");
      short iVendorIdColumn = (short) findColumnForHeader(sheet, "Vendor_ID");
      short iMedChemCommentColumn = (short) findColumnForHeader(sheet, "Mechem comment" /* "Mechem" [sic] */);
      for (int iRow = 0; iRow < sheet.getRows(); ++iRow) {
        Cell vendorCell = (Cell) cellFactory.getCell(iVendorColumn, iRow, true).clone();
        Cell vendorIdCell = (Cell) cellFactory.getCell(iVendorIdColumn, iRow, true).clone();
        Cell medChemCommentCell = (Cell) cellFactory.getCell(iMedChemCommentColumn, iRow, true).clone();
        ReagentVendorIdentifier rvi = new ReagentVendorIdentifier(vendorCell.getAsString(), vendorIdCell.getAsString());
        Reagent reagent = findOrCreateReagent(rvi, dao);
        String value = MEDCHEM_COMMENT_CELL_PARSER.parse(medChemCommentCell);
        if (value != null) {
          annotType.createAnnotationValue(reagent, value);
          ++n;
        }
      }
      return n;
    }
    catch (FatalParseException e) {
      errors.addError(e.getMessage());
      return 0;
    }
  }

  private static Reagent findOrCreateReagent(ReagentVendorIdentifier rvi, GenericEntityDAO dao)
  {
    Reagent reagent = dao.findEntityById(Reagent.class, rvi);
    if (reagent == null) {
      reagent = new Reagent(rvi);
      dao.persistEntity(reagent);
      log.info("created new reagent " + reagent);
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
    throw new FatalParseException("no such column header " + headerName + " on sheet " + sheet.getName());
  }
}