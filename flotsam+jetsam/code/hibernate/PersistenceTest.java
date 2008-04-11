// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.util.Date;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.SchemaUtil;
import edu.harvard.med.screensaver.io.screenresults.MockDaoForScreenResultImporter;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;

import org.apache.log4j.Logger;

public class PersistenceTest
{
  private static Logger log = Logger.getLogger(PersistenceTest.class);

  public static void main(String[] args)
  {
    CommandLineApplication app = new CommandLineApplication(args);
    final GenericEntityDAO dao = (GenericEntityDAO) app.getSpringBean("genericEntityDao");
    SchemaUtil schemaUtil = (SchemaUtil) app.getSpringBean("schemaUtil");
    final int screenNumber = 9999;
    
    schemaUtil.recreateSchema();

    Screen s1 = MockDaoForScreenResultImporter.makeDummyScreen(screenNumber);
    new ScreenResult(s1, new Date());
    dao.persistEntity(s1); 
    Screen s2 = dao.findEntityByProperty(Screen.class, "hbnScreenNumber", screenNumber);
    log.info("screen " + 
             (s2== null ? "did NOT" : "DID") +
             " get persisted");
    if (s2 != null) {
      log.info("screen result " + 
               (s2.getScreenResult() == null ? "did NOT" : "DID") + 
               " get persisted");
    }
    
//    dao.doInTransaction(new DAOTransaction()
//    {
//      public void runTransaction()
//      {
//        Screen s = dao.findEntityByProperty(Screen.class, "hbnScreenNumber", screenNumber);
//        s.getLabHead().getHbnScreensHeaded().remove(s);
//        s.getLeadScreener().getHbnScreensLed().remove(s);
//        dao.deleteEntity(s.getScreenResult()); // too bad we have to do this, in addition to the next line
//        dao.deleteEntity(s);
//      }
//    });
//    ScreenResult sr2 = dao.findEntityById(ScreenResult.class, sr.getEntityId());
//    log.info("screen result " + 
//             (sr2 != null ? "did NOT" : "DID") + " get deleted with screen");
//
  }

}

