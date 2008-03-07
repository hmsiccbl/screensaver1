// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.beans.IntrospectionException;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.Study;

import org.apache.log4j.Logger;

public class ReagentTest extends AbstractEntityInstanceTest<Reagent>
{
  private static Logger log = Logger.getLogger(ReagentTest.class);

  //protected LibrariesDAO librariesDao;

  public ReagentTest() throws IntrospectionException
  {
    super(Reagent.class);
  }

  public void testAnnotationValueMap()
  {
    schemaUtil.truncateTablesOrCreateSchema();
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        Library library = MakeDummyEntities.makeDummyLibrary(1, ScreenType.SMALL_MOLECULE, 1);
        genericEntityDao.persistEntity(library);

        Reagent reagent1 = new Reagent(new ReagentVendorIdentifier("Vendor:1"));
        Reagent reagent2 = new Reagent(new ReagentVendorIdentifier("Vendor:2"));
        genericEntityDao.persistEntity(reagent1);
        genericEntityDao.persistEntity(reagent2);

        Screen study = MakeDummyEntities.makeDummyScreen(Study.MIN_STUDY_NUMBER);
        AnnotationType annotType1 = study.createAnnotationType("annotType1", "", false);
        AnnotationType annotType2 = study.createAnnotationType("annotType2", "", false);
        annotType1.createAnnotationValue(reagent1, "annotType1_annotValue1");
        annotType1.createAnnotationValue(reagent2, "annotType1_annotValue2");
        annotType2.createAnnotationValue(reagent1, "annotType2_annotValue1");
        annotType2.createAnnotationValue(reagent2, "annotType2_annotValue2");
        genericEntityDao.saveOrUpdateEntity(study.getLeadScreener());
        genericEntityDao.saveOrUpdateEntity(study.getLabHead());
        genericEntityDao.persistEntity(study);
      }
    });

    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        Reagent reagent1 = genericEntityDao.findEntityById(Reagent.class, new ReagentVendorIdentifier("Vendor:1"));
        Reagent reagent2 = genericEntityDao.findEntityById(Reagent.class, new ReagentVendorIdentifier("Vendor:2"));
        assertEquals("reagent1.annotationValues size", 2, reagent1.getAnnotationValues().size());
        assertEquals("reagent2.annotationValues size", 2, reagent2.getAnnotationValues().size());

        AnnotationType at1 = genericEntityDao.findEntityByProperty(AnnotationType.class, "name", "annotType1");
        AnnotationValue av1 = reagent1.getAnnotationValues().get(at1);
        assertNotNull(av1);
        assertEquals(at1, av1.getAnnotationType());

        AnnotationType at2 = genericEntityDao.findEntityByProperty(AnnotationType.class, "name", "annotType2");
        AnnotationValue av2 = reagent1.getAnnotationValues().get(at2);
        assertNotNull(av2);
        assertEquals(at2, av2.getAnnotationType());
      }
    });

  }

}

