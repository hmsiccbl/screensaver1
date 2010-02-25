// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/io/screens/MedicinalCompoundsStudyCreator.java $
// $Id: MedicinalCompoundsStudyCreator.java 2540 2008-07-07 19:07:14Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.io.screens;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.ScreenDAO;
import edu.harvard.med.screensaver.io.FatalParseException;
import edu.harvard.med.screensaver.io.screens.ScreenCreator;
import edu.harvard.med.screensaver.io.workbook2.Cell;
import edu.harvard.med.screensaver.io.workbook2.CellVocabularyParser;
import edu.harvard.med.screensaver.io.workbook2.Workbook;
import edu.harvard.med.screensaver.io.workbook2.WorkbookParseError;
import edu.harvard.med.screensaver.io.workbook2.Worksheet;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.StudyType;
import edu.harvard.med.screensaver.model.users.LabAffiliation;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

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
  private static final String LAB_AFFILIATION_NAME = "Brigham and Women's Hospital";

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

          LabAffiliation labAffiliation = dao.findEntityByProperty(LabAffiliation.class, "affiliationName", LAB_AFFILIATION_NAME);
          if (labAffiliation == null) {
            throw new RuntimeException("expected lab affiliation " + LAB_AFFILIATION_NAME + " to exist");
          }
          LabHead labHead = (LabHead) ScreenCreator.findOrCreateScreeningRoomUser(dao, "Gregory", "Cuny", "gcuny@rics.bwh.harvard.edu", true, labAffiliation);
          ScreeningRoomUser leadScreener = ScreenCreator.findOrCreateScreeningRoomUser(dao, "Kyungae", "Lee", "kyungae_lee@hms.harvard.edu", false, null);

          study = new Screen(leadScreener, labHead, STUDY_NUMBER, ScreenType.SMALL_MOLECULE, StudyType.IN_SILICO, TITLE);
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
    });
    log.info("study successfully added to database");
  }

  protected static int loadAndCreateReagents(File workbookFile,
                                             GenericEntityDAO dao,
                                             AnnotationType annotType)
    throws FileNotFoundException
  {
    int n = 0;
    Workbook workbook = new Workbook(workbookFile);

    for (int iSheet = 0; iSheet < workbook.getWorkbook().getNumberOfSheets(); ++iSheet) {
      Worksheet sheet = workbook.getWorksheet(iSheet);
      n += loadAndCreateReagentsFromSheet(sheet, dao, annotType);
    }
    if (workbook.getHasErrors()) {
      log.error("Encountered " + workbook.getErrors().size() + " error(s).");
      for (WorkbookParseError error : workbook.getErrors()) {
        log.error(error.toString());
      }
    }
    return n;
  }

  private static int loadAndCreateReagentsFromSheet(Worksheet sheet, GenericEntityDAO dao, AnnotationType annotType)
  {
    int n = 0;
    try {
      short iVendorColumn = (short) findColumnForHeader(sheet, VENDOR_COLUMN_HEADER);
      short iVendorIdColumn = (short) findColumnForHeader(sheet, VENDOR_ID_COLUMN_HEADER);
      short iMedChemCommentColumn = (short) findColumnForHeader(sheet, MECHEM_COMMENT_COLUMN_HEADER);
      short iPlateColumn = (short) findColumnForHeader(sheet, PLATE_COLUMN_HEADER);
      short iWellColumn = (short) findColumnForHeader(sheet, WELL_COLUMN_HEADER);
      if (iVendorColumn >= 0 && iVendorIdColumn >= 0 && iMedChemCommentColumn >= 0) {
        for (int iRow = 1; iRow < sheet.getRows(); ++iRow) {  // TODO: use iterator
          if( !sheet.getCell(iVendorColumn, iRow, true).isEmpty()
            && !sheet.getCell(iVendorIdColumn, iRow, true).isEmpty())
          {
            Reagent reagent = findReagent(dao,
                                          sheet.getCell(iPlateColumn, iRow, true).getInteger(),
                                          sheet.getCell(iWellColumn, iRow, true).getString(),
                                          sheet.getCell(iVendorIdColumn, iRow, true).getAsString(),
                                          sheet.getCell(iVendorIdColumn, iRow, true).getAsString());
            if (reagent != null) {
              Cell medChemCommentCell = (Cell) sheet.getCell(iMedChemCommentColumn, iRow, false).clone();
              CellVocabularyParser<String> MEDCHEM_COMMENT_CELL_PARSER = new CellVocabularyParser<String>(VALID_MEDCHEM_COMMENT_VALUES, null,  "bad annotation value");
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
      sheet.addWorkbookError(e.getClass().getName() + ":" + e.getMessage());
      return 0;
    }
  }

  private static Reagent findReagent(GenericEntityDAO dao,
                                     Integer plateNumber,
                                     String wellName,
                                     String vendorName, 
                                     String reagentIdentifier)
  {
    ReagentVendorIdentifier rvi = new ReagentVendorIdentifier(vendorName, reagentIdentifier);
    Reagent reagent = dao.findEntityById(Reagent.class, rvi);
    if (reagent == null) {
      WellKey wellKey = new WellKey(plateNumber, wellName);
      //log.warn("reagent does not exist with ID " + rvi);
      Well well = dao.findEntityById(Well.class, wellKey.toString());
      if (well == null) { 
        log.error("unknown reagent " + rvi + "; looking for reagent in well, but no such well " + wellKey);
      }
      else {
        reagent = well.<Reagent>getLatestReleasedReagent();
        if (reagent == null) {
          log.error("unknown reagent " + rvi + " and no reagent in well " + wellKey);
        }
      }
    }
    return reagent;
  }

  private static int findColumnForHeader(Worksheet sheet, String headerName)
  {
    for(Cell cell: sheet.getRow(0))
    {
      if(cell.getAsString().equals(headerName)) return cell.getColumn();
    }
    return -1;
  }
}