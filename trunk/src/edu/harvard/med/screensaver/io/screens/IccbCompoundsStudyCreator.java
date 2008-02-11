// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.screens;

import java.util.Date;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.StudyType;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

public class IccbCompoundsStudyCreator
{

  // static members

  private static Logger log = Logger.getLogger(IccbCompoundsStudyCreator.class);

  private static final int STUDY_NUMBER = 100001;
  private static final String TITLE = "Annotations on Suitability of Compounds: Miscellaneous Sources";
  private static final String SUMMARY = "Annotations for ICCB-L compounds, heard through the grapevine.";

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
    dao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        try {
          deleteExistingStudy(dao);

          ScreeningRoomUser labHead = ScreenCreator.findOrCreateScreeningRoomUser(dao, "Caroline", "Shamu", "caroline_shamu@hms.harvard.edu");
          ScreeningRoomUser leadScreener = labHead;

          Screen study = new Screen(leadScreener, labHead, STUDY_NUMBER, new Date(), ScreenType.SMALL_MOLECULE, StudyType.IN_VITRO, TITLE);
          study.setSummary(SUMMARY);

          AnnotationType unsuitableAnnotType = study.createAnnotationType("Unsuitable", "Flag indicating whether compound is unsuitable for screening.", false);
          AnnotationType commentAnnotType = study.createAnnotationType("Notes on Suitability", "Explanation of why compound may be undesirable for screening.", false);

          Reagent reagent = findOrCreateReagent(dao);
          unsuitableAnnotType.createAnnotationValue(reagent, "true");
          commentAnnotType.createAnnotationValue(reagent, "Chelates metal and has shown up in a number of assays");

          dao.saveOrUpdateEntity(study);
        }
        catch (Exception e) {
          throw new DAOTransactionRollbackException(e);
        }
      }

      private Reagent findOrCreateReagent(final GenericEntityDAO dao)
      {
        ReagentVendorIdentifier rvi = new ReagentVendorIdentifier("Novartis", "NIBR1 K839-0057");
        Reagent reagent = dao.findEntityById(Reagent.class, rvi);
        if (reagent == null) {
          reagent = new Reagent(rvi);
        }
        return reagent;
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
}
