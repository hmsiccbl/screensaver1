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
import java.util.Date;
import java.util.List;

import edu.harvard.med.screensaver.io.screenresults.MockDaoForScreenResultImporter;
import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.model.libraries.PlateType;

import org.apache.log4j.Logger;

public class CherryPickRequestTest extends AbstractEntityInstanceTest
{
  // static members

  private static Logger log = Logger.getLogger(CherryPickRequestTest.class);


  // instance data members

  
  // public constructors and methods

  public CherryPickRequestTest() throws IntrospectionException
  {
    super(CherryPickRequest.class);
  }
  
  public void testGetActiveCherryPickAssayPlates()
  {
    Screen screen = MockDaoForScreenResultImporter.makeDummyScreen(1);
    CherryPickRequest cherryPickRequest = new RNAiCherryPickRequest(screen, 
                                                                    screen.getLeadScreener(), 
                                                                    new Date());
    for (int plateOrdinal = 0; plateOrdinal < 3; ++plateOrdinal) {
      for (int attempt = 0; attempt <= plateOrdinal; ++attempt) {
        new CherryPickAssayPlate(cherryPickRequest,
                                 plateOrdinal,
                                 attempt,
                                 PlateType.EPPENDORF);
      }
    }
    List<CherryPickAssayPlate> activeAssayPlates = cherryPickRequest.getActiveCherryPickAssayPlates();
    assertEquals(3, activeAssayPlates.size()); 
    int expectedAttemptOrdinal = 0;
    for (CherryPickAssayPlate activeAssayPlate : activeAssayPlates) {
      assertEquals("active assay plate is the last one attempted", 
                   expectedAttemptOrdinal++, 
                   activeAssayPlate.getAttemptOrdinal().intValue());
    }
  }
  
  public void testAssayPlateRequiringSourcePlateReload()
  {
    fail("not implemented");
  }

}

