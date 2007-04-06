// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import java.beans.IntrospectionException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.io.screenresults.MockDaoForScreenResultImporter;
import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.service.cherrypicks.CherryPickRequestAllocatorTest;

import org.apache.log4j.Logger;

public class RNAiCherryPickRequestTest extends AbstractEntityInstanceTest
{
  // static members

  private static Logger log = Logger.getLogger(RNAiCherryPickRequestTest.class);


  public RNAiCherryPickRequestTest() throws IntrospectionException
  {
    super(RNAiCherryPickRequest.class);
  }
  
  public void testRequestedEmptyColumnsOnAssayPlate()
  {
    schemaUtil.truncateTablesOrCreateSchema();

    final Set<Integer> requestedEmptyColumns = new HashSet<Integer>(Arrays.asList(3, 7, 11));
    dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction() 
      {
        Screen screen = MockDaoForScreenResultImporter.makeDummyScreen(1);
        screen.setScreenType(ScreenType.RNAI);
        CherryPickRequest cherryPickRequest = screen.createCherryPickRequest();
        cherryPickRequest.setRequestedEmptyColumnsOnAssayPlate(requestedEmptyColumns);
        dao.persistEntity(cherryPickRequest); // why do we need this, if we're also persisting the screen?!
        dao.persistEntity(screen);
      }
    });
    
    dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction() 
      {
        Screen screen2 = dao.findEntityByProperty(Screen.class, "hbnScreenNumber", 1);
        assertEquals(requestedEmptyColumns,
                     screen2.getCherryPickRequests().iterator().next().getRequestedEmptyColumnsOnAssayPlate());
      }
    });
  }

  /**
   * Note that we're test w/o creating assay plates for the lab cherry picks.  This is intentional.
   */
  public void testCherryPickAllowance()
  {
    dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Screen screen = MockDaoForScreenResultImporter.makeDummyScreen(1);
        screen.setScreenType(ScreenType.RNAI);
        RNAiCherryPickRequest cherryPickRequest = (RNAiCherryPickRequest) screen.createCherryPickRequest();
        dao.persistEntity(CherryPickRequestAllocatorTest.makeRNAiDuplexLibrary("Duplexes Library", 50001, 50007, 384));

        for (int plateOrdinal = 0; plateOrdinal < 6; ++plateOrdinal) {
          for (int attempt = 0; attempt <= plateOrdinal; ++attempt) {
            WellKey[] allWellsOnPlate = new WellKey[Well.PLATE_COLUMNS * Well.PLATE_ROWS];
            int i = 0;
            for (int iRow = 0; iRow < Well.PLATE_ROWS; ++iRow) {
              for (int iCol = 0; iCol < Well.PLATE_COLUMNS; ++iCol) {
                WellKey wellKey = new WellKey(plateOrdinal + 50001, iRow, iCol);
                Well well = dao.findWell(wellKey);
                new ScreenerCherryPick(cherryPickRequest, well);
                allWellsOnPlate[i++] = wellKey;
              }
            }
          }
        }
        dao.persistEntity(cherryPickRequest); // avoid hib errors on flush
        dao.persistEntity(screen); // avoid hib errors on flush

        assertEquals("cherry pick allowance used", 384 * 6, cherryPickRequest.getCherryPickAllowanceUsed());
      }
    });
  }
}

