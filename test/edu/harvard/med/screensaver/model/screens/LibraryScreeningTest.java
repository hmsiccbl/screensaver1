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

import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.model.MakeDummyEntities;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;

public class LibraryScreeningTest extends AbstractEntityInstanceTest<LibraryScreening>
{
  // static members

  private static Logger log = Logger.getLogger(LibraryScreeningTest.class);


  // instance data members

  
  // public constructors and methods

  public LibraryScreeningTest() throws IntrospectionException
  {
    super(LibraryScreening.class);
  }
  
  public void testAddLibraryScreeningDuplicatesAssayProtocolInfo()
  {
    Screen screen = MakeDummyEntities.makeDummyScreen(1);
    LibraryScreening previousScreening = screen.createLibraryScreening(screen.getLeadScreener(), new LocalDate());
    LibraryScreening currentScreening = screen.createLibraryScreening(screen.getLeadScreener(), new LocalDate());
    assertEquals(previousScreening.getAssayProtocol(), currentScreening.getAssayProtocol());
    assertEquals(previousScreening.getAssayProtocolLastModifiedDate(), currentScreening.getAssayProtocolLastModifiedDate());
    assertEquals(previousScreening.getAssayProtocolType(), currentScreening.getAssayProtocolType());

    previousScreening = currentScreening;
    previousScreening.setAssayProtocol("previous assay protocol");
    currentScreening = screen.createLibraryScreening(screen.getLeadScreener(), new LocalDate());
    assertEquals(previousScreening.getAssayProtocol(), currentScreening.getAssayProtocol());
    assertEquals(previousScreening.getAssayProtocolLastModifiedDate(), currentScreening.getAssayProtocolLastModifiedDate());
    assertEquals(previousScreening.getAssayProtocolType(), currentScreening.getAssayProtocolType());
    
    previousScreening = currentScreening;
    previousScreening.setAssayProtocolLastModifiedDate(new LocalDate(2000, 1, 1));
    currentScreening = screen.createLibraryScreening(screen.getLeadScreener(), new LocalDate());
    assertEquals(previousScreening.getAssayProtocol(), currentScreening.getAssayProtocol());
    assertEquals(previousScreening.getAssayProtocolLastModifiedDate(), currentScreening.getAssayProtocolLastModifiedDate());
    assertEquals(previousScreening.getAssayProtocolType(), currentScreening.getAssayProtocolType());
    
    previousScreening = currentScreening;
    previousScreening.setAssayProtocolType(AssayProtocolType.ESTABLISHED);
    currentScreening = screen.createLibraryScreening(screen.getLeadScreener(), new LocalDate());
    assertEquals(previousScreening.getAssayProtocol(), currentScreening.getAssayProtocol());
    assertEquals(previousScreening.getAssayProtocolLastModifiedDate(), currentScreening.getAssayProtocolLastModifiedDate());
    assertEquals(previousScreening.getAssayProtocolType(), currentScreening.getAssayProtocolType());
  }

}

