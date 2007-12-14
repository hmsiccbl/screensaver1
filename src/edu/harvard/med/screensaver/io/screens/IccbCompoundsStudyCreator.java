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
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParser;
import edu.harvard.med.screensaver.io.workbook2.WorkbookParseError;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.model.screens.StudyType;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

public class IccbCompoundsStudyCreator
{
  // static members

  private static Logger log = Logger.getLogger(IccbCompoundsStudyCreator.class);

  private static final String TITLE = "ICCB Compounds";
  private  static final String SUMMARY = "Annotations for ICCB compounds, heard through the grapevine.";

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
          ScreeningRoomUser labHead = ScreenCreator.findOrCreateScreeningRoomUser(dao, "Caroline", "Shamu", "caroline_shamu@hms.harvard.edu");
          ScreeningRoomUser leadScreener = labHead;

          Screen study = new Screen(leadScreener, labHead, 100001, new Date(), ScreenType.SMALL_MOLECULE, StudyType.IN_SILICO, TITLE);
          study.setSummary(SUMMARY);

          study.createAnnotationType("Undesirable", "Flag indicating whether compound is undesirable for screening.", false);
          study.createAnnotationType("Comment", "Comment explaining why compound is undesirable for screening.", false);

          dao.saveOrUpdateEntity(study);
        }
        catch (Exception e) {
          throw new DAOTransactionRollbackException(e);
        }
      }
    });
    log.info("study succesfully added to database");
  }
}
