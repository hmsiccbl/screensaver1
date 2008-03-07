// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screenresults;

import java.beans.IntrospectionException;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.io.screenresults.ScreenResultParser;
import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.Study;

import org.apache.log4j.Logger;

public class AnnotationTypeTest extends AbstractEntityInstanceTest<AnnotationType>
{
  // static members

  private static Logger log = Logger.getLogger(AnnotationTypeTest.class);


  // instance data members

  protected ScreenResultParser screenResultParser;
  protected LibrariesDAO librariesDao;


  // public constructors and methods

  public AnnotationTypeTest() throws IntrospectionException
  {
    super(AnnotationType.class);
  }

  public void testDuplicateAnnotationValue()
  {
    schemaUtil.truncateTablesOrCreateSchema();

    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        Library library = MakeDummyEntities.makeDummyLibrary(1, ScreenType.SMALL_MOLECULE, 1);
        genericEntityDao.persistEntity(library);
        Screen study = MakeDummyEntities.makeDummyStudy(library);
        genericEntityDao.persistEntity(study);
        Reagent reagent1 = new Reagent(new ReagentVendorIdentifier( "vendor", "1"));
        Reagent reagent2 = new Reagent(new ReagentVendorIdentifier( "vendor", "2"));
        AnnotationType at = study.createAnnotationType("annotation", "", false);
        assertNotNull(at.createAnnotationValue(reagent1, "a"));
        assertNotNull(at.createAnnotationValue(reagent2, "b"));
        assertNull(at.createAnnotationValue(reagent1, "c"));;
        genericEntityDao.persistEntity(study);
      }
    });

    Screen study = genericEntityDao.findEntityByProperty(Screen.class, "screenNumber", Study.MIN_STUDY_NUMBER, false, "annotationTypes.annotationValues.reagent");
    AnnotationType at = study.getAnnotationTypes().last();
    assertEquals(at.getName(), "annotation");
    Reagent reagent1 = genericEntityDao.findEntityById(Reagent.class, new ReagentVendorIdentifier("vendor", "1"));
    assertNull(at.createAnnotationValue(reagent1, "d"));
  }
}

