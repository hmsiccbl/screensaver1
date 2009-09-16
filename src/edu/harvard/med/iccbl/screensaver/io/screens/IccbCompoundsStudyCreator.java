// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/io/screens/IccbCompoundsStudyCreator.java $
// $Id: IccbCompoundsStudyCreator.java 2540 2008-07-07 19:07:14Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.io.screens;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.ScreenDAO;
import edu.harvard.med.screensaver.io.libraries.ExtantLibraryException;
import edu.harvard.med.screensaver.io.screens.ScreenCreator;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.StudyType;
import edu.harvard.med.screensaver.model.users.LabAffiliation;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

public class IccbCompoundsStudyCreator
{

  // static members


  private static Logger log = Logger.getLogger(IccbCompoundsStudyCreator.class);

  private static final int STUDY_NUMBER = 100001;
  private static final String TITLE = "Annotations on Suitability of Compounds: Miscellaneous Sources";
  private static final String SUMMARY = "Annotations for ICCB-L compounds, from sources other than Kyungae Lee and Greg Cuny";
  private static final String LAB_AFFILIATION_NAME = "Harvard Medical School, Institute of Chemistry and Cell Biology";

  public static void main(String[] args)
  {
    CommandLineApplication app = new CommandLineApplication(args);
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
          LabHead labHead = (LabHead) ScreenCreator.findOrCreateScreeningRoomUser(dao, "Caroline", "Shamu", "caroline_shamu@hms.harvard.edu", true, labAffiliation);
          ScreeningRoomUser leadScreener = labHead;

          study = new Screen(leadScreener, labHead, STUDY_NUMBER, ScreenType.SMALL_MOLECULE, StudyType.IN_VITRO, TITLE);
          study.setSummary(SUMMARY);

          AnnotationType unsuitableAnnotType = study.createAnnotationType("Unsuitable", "Flag indicating whether compound is unsuitable for screening.", false);
          AnnotationType commentAnnotType = study.createAnnotationType("Notes on Suitability", "Explanation of why compound may be undesirable for screening.", false);

          Reagent reagent = find(dao);
          unsuitableAnnotType.createAnnotationValue(reagent, "true");
          commentAnnotType.createAnnotationValue(reagent, "Chelates metal and has shown up in a number of assays; from Rez Halese (Novartis), December 2007.");
            
            dao.saveOrUpdateEntity(study);
        }
        catch (Exception e) {
          throw new DAOTransactionRollbackException(e);
        }
      }

      private Reagent find(final GenericEntityDAO dao) throws ExtantLibraryException
      {
        ReagentVendorIdentifier rvi = new ReagentVendorIdentifier("Novartis", "NIBR1 K839-0057");
        Reagent reagent = dao.findEntityById(Reagent.class, rvi);
        if (reagent == null) {
          throw new ExtantLibraryException("no library contains reagent " + rvi);
        }
        return reagent;
      }
    });
    log.info("study successfully added to database");
  }
}
