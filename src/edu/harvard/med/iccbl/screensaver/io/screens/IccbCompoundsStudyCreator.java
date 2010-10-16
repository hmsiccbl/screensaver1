// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.io.screens;

import java.util.Set;

import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.db.ScreenDAO;
import edu.harvard.med.screensaver.io.libraries.ExtantLibraryException;
import edu.harvard.med.screensaver.io.screens.ScreenCreator;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.model.screens.StudyType;
import edu.harvard.med.screensaver.model.users.LabAffiliation;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

public class IccbCompoundsStudyCreator
{

  // static members


  private static Logger log = Logger.getLogger(IccbCompoundsStudyCreator.class);

  private static final String STUDY_FACILITY_ID = Study.STUDY_FACILITY_ID_PREFIX + "100001";
  private static final String TITLE = "Annotations on Suitability of Compounds: Miscellaneous Sources";
  private static final String SUMMARY = "Annotations for ICCB-L compounds, from sources other than Kyungae Lee and Greg Cuny";
  private static final String LAB_AFFILIATION_NAME = "Harvard Medical School, Institute of Chemistry and Cell Biology";

  public static void main(String[] args)
  {
    final CommandLineApplication app = new CommandLineApplication(args);
    try {
      app.processOptions(true, true, true);
    }
    catch (ParseException e1) {
      System.exit(1);
    }
    final GenericEntityDAO dao = (GenericEntityDAO) app.getSpringBean("genericEntityDao");
    final LibrariesDAO librariesDao = (LibrariesDAO) app.getSpringBean("librariesDao");
    final ScreenDAO screenDao = (ScreenDAO) app.getSpringBean("screenDao");
    dao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        try {
          Screen study = dao.findEntityByProperty(Screen.class, Screen.facilityId.getPropertyName(), STUDY_FACILITY_ID);
          if (study != null) {
            screenDao.deleteStudy(study);
          }

          LabAffiliation labAffiliation = dao.findEntityByProperty(LabAffiliation.class, "affiliationName", LAB_AFFILIATION_NAME);
          if (labAffiliation == null) {
            throw new RuntimeException("expected lab affiliation " + LAB_AFFILIATION_NAME + " to exist");
          }
          LabHead labHead = (LabHead) ScreenCreator.findOrCreateScreeningRoomUser(dao, "Caroline", "Shamu", "caroline_shamu@hms.harvard.edu", true, labAffiliation);
          ScreeningRoomUser leadScreener = labHead;

          study = new Screen(app.findAdministratorUser(),
                             STUDY_FACILITY_ID,
                             leadScreener,
                             labHead,
                             ScreenType.SMALL_MOLECULE,
                             StudyType.IN_VITRO, TITLE);
          study.setSummary(SUMMARY);

          AnnotationType unsuitableAnnotType = study.createAnnotationType("Unsuitable", "Flag indicating whether compound is unsuitable for screening.", false);
          AnnotationType commentAnnotType = study.createAnnotationType("Notes on Suitability", "Explanation of why compound may be undesirable for screening.", false);

          Reagent reagent = find(librariesDao);
          unsuitableAnnotType.createAnnotationValue(reagent, "true");
          commentAnnotType.createAnnotationValue(reagent, "Chelates metal and has shown up in a number of assays; from Rez Halese (Novartis), December 2007.");
            
            dao.saveOrUpdateEntity(study);
        }
        catch (Exception e) {
          throw new DAOTransactionRollbackException(e);
        }
      }

      private Reagent find(final LibrariesDAO librariesDao) throws ExtantLibraryException
      {
        ReagentVendorIdentifier rvi = new ReagentVendorIdentifier("Novartis", "NIBR1 K839-0057");
        Set<Reagent> set = librariesDao.findReagents(rvi, false);
        if (set.isEmpty()) {
          throw new ExtantLibraryException("no library contains reagent " + rvi);
        }
        else if (set.size() > 1) {
          throw new ExtantLibraryException("more than one reagent found for RVI: " + rvi + ", reagents: " + set);
        }
        return set.iterator().next();
      }
    });
    log.info("study successfully added to database");
  }
}
