// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.beans.IntrospectionException;
import java.math.BigDecimal;

import junit.framework.TestSuite;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.AbstractEntityInstanceTest;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.StudyType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.test.MakeDummyEntities;

public class ReagentTest extends AbstractEntityInstanceTest<Reagent>
{
  public static TestSuite suite()
  {
    return buildTestSuite(ReagentTest.class, Reagent.class);
  }

  public ReagentTest()
  {
    super(Reagent.class);
  }

  public void testAnnotationValueMap()
  {
    schemaUtil.truncateTables();
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        Library library = dataFactory.newInstance(Library.class);
        library.createContentsVersion(dataFactory.newInstance(AdministratorUser.class));
        Well well1 = library.createWell(new WellKey(library.getStartPlate(), 0, 0), LibraryWellType.EXPERIMENTAL);
        Well well2 = library.createWell(new WellKey(library.getStartPlate(), 0, 1), LibraryWellType.EXPERIMENTAL);
        library.setScreenType(ScreenType.SMALL_MOLECULE);

        Reagent reagent1 = well1.createSmallMoleculeReagent(new ReagentVendorIdentifier("Vendor", "1"), "molfile", "smiles", "inchi", new BigDecimal("1.000"), new BigDecimal("1.000"), new MolecularFormula("CCC"));
        Reagent reagent2 = well2.createSmallMoleculeReagent(new ReagentVendorIdentifier("Vendor", "2"), "molfile", "smiles", "inchi", new BigDecimal("2.000"), new BigDecimal("2.000"), new MolecularFormula("CCCCCC"));
        genericEntityDao.persistEntity(library);

        Screen study = MakeDummyEntities.makeDummyScreen("S", ScreenType.SMALL_MOLECULE, StudyType.IN_SILICO);
        AnnotationType annotType1 = study.createAnnotationType("annotType1", "", false);
        AnnotationType annotType2 = study.createAnnotationType("annotType2", "", false);
        annotType1.createAnnotationValue(reagent1, "annotType1_annotValue1");
        annotType1.createAnnotationValue(reagent2, "annotType1_annotValue2");
        annotType2.createAnnotationValue(reagent1, "annotType2_annotValue1");
        annotType2.createAnnotationValue(reagent2, "annotType2_annotValue2");
        genericEntityDao.persistEntity(study);
      }
    });

    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        Reagent reagent1 = genericEntityDao.findEntityByProperty(Reagent.class, "vendorId.vendorIdentifier", "1");
        Reagent reagent2 = genericEntityDao.findEntityByProperty(Reagent.class, "vendorId.vendorIdentifier", "2");
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

