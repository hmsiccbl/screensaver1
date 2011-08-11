// $HeadURL:
// http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/lincs/ui-cleanup/core/src/test/java/edu/harvard/med/screensaver/io/screens/StudyAnnotationParserTest.java
// $
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.screens;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import edu.harvard.med.screensaver.db.EntityInflator;
import edu.harvard.med.screensaver.io.UnrecoverableParseException;
import edu.harvard.med.screensaver.io.workbook2.Workbook;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.test.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.test.MakeDummyEntities;

public class StudyAnnotationParserTest extends AbstractSpringPersistenceTest
{
  private Resource WORKBOOK_FILE_BY_RVI = new ClassPathResource("/screens/test-study-by-rvi.xls");
  private Resource WORKBOOK_FILE_BY_WELLKEY = new ClassPathResource("/screens/test-study-by-wellkey.xls");
  private Resource WORKBOOK_FILE_WITH_AT_IN_COL1_BY_WELLKEY = new ClassPathResource("/screens/test-study-with-at-in-col1-by-wellkey.xls");
  private Resource WORKBOOK_FILE_WITH_AT_IN_COL1_BY_COMPOUND_NAME = new ClassPathResource("/screens/test-study-with-at-in-col1-by-compoundName.xls");

  private static Logger log = Logger.getLogger(StudyAnnotationParserTest.class);

  @Autowired
  protected StudyAnnotationParser studyAnnotationParser;

  public void testStudyAnnotationParseByRvi() throws UnrecoverableParseException, FileNotFoundException, IOException
  {
    Library library = MakeDummyEntities.makeDummyLibrary(1, ScreenType.RNAI, 1);
    genericEntityDao.persistEntity(library);

    Screen study = MakeDummyEntities.makeDummyScreen(1, library.getScreenType());
    studyAnnotationParser.parse(study,
                                new Workbook(WORKBOOK_FILE_BY_RVI.getFile()),
                                StudyAnnotationParser.KEY_COLUMN.RVI,
                                false, false);
    doTest(study);
  }

  public void testStudyAnnotationParseByWellKey() throws UnrecoverableParseException, FileNotFoundException, IOException
  {
    Library library = MakeDummyEntities.makeDummyLibrary(1, ScreenType.RNAI, 1);
    genericEntityDao.persistEntity(library);

    Screen study = MakeDummyEntities.makeDummyScreen(2, library.getScreenType());
    studyAnnotationParser.parse(study,
                                new Workbook(WORKBOOK_FILE_BY_WELLKEY.getFile()),
                                StudyAnnotationParser.KEY_COLUMN.WELL_ID,
                                false, false);
    doTest(study);
  }

  public void testStudyAnnotationParseWithTypeInCol1ByCompoundName() throws UnrecoverableParseException, FileNotFoundException, IOException
  {
    Library library = MakeDummyEntities.makeDummyLibrary(1, ScreenType.SMALL_MOLECULE, 1);
    genericEntityDao.persistEntity(library);

    Screen study = MakeDummyEntities.makeDummyScreen(3, library.getScreenType());
    studyAnnotationParser.parse(study,
                                new Workbook(WORKBOOK_FILE_WITH_AT_IN_COL1_BY_COMPOUND_NAME.getFile()),
                                StudyAnnotationParser.KEY_COLUMN.COMPOUND_NAME,
                                true, false);
    genericEntityDao.persistEntity(study);
    doSMCompoundTest(study);
  }

  public void testStudyAnnotationParseWithTypeInCol1ByWellKey() throws UnrecoverableParseException, FileNotFoundException, IOException
  {
    Library library = MakeDummyEntities.makeDummyLibrary(1, ScreenType.RNAI, 1);
    genericEntityDao.persistEntity(library);

    Screen study = MakeDummyEntities.makeDummyScreen(3, library.getScreenType());
    studyAnnotationParser.parse(study,
                                new Workbook(WORKBOOK_FILE_WITH_AT_IN_COL1_BY_WELLKEY.getFile()),
                                StudyAnnotationParser.KEY_COLUMN.WELL_ID,
                                true, false);
    doTest(study);
  }

  private void doSMCompoundTest(Screen study)
  {
    String[] compoundNames = { "compound1", "compound2", "compound3" };
    Object[][] expectedData = {
      { 1.0, 3.0, 2.0 },
      { "a", "c", "b" }
    };

    //    Library library = MakeDummyEntities.makeDummyLibrary(1, ScreenType.RNAI, 1);
    //    genericEntityDao.persistEntity(library);

    //Screen study = MakeDummyEntities.makeDummyScreen(1, library.getScreenType());
    //studyAnnotationParser.parse(study, getClass().getClassLoader().getResourceAsStream(WORKBOOK_FILE));
    assertEquals("annotation type count", expectedData.length, study.getAnnotationTypes().size());
    assertEquals("reagent count", compoundNames.length, study.getReagents().size());

    study = new EntityInflator<Screen>(genericEntityDao, study, true).
      need(Screen.annotationTypes).
      need(Screen.annotationTypes.to(AnnotationType.annotationValues)).
      need(Screen.reagents).inflate();

    AnnotationType numericAnnotationType = study.getAnnotationTypes().first();
    AnnotationType textAnnotationType = study.getAnnotationTypes().last();

    assertTrue(numericAnnotationType.isNumeric());
    assertFalse(textAnnotationType.isNumeric());
    assertEquals("NumericAnnotation", numericAnnotationType.getName());
    assertEquals("numeric annotation", numericAnnotationType.getDescription());
    assertEquals("TextAnnotation", textAnnotationType.getName());
    assertEquals("text annotation", textAnnotationType.getDescription());
    assertEquals(3, numericAnnotationType.getAnnotationValues().size());
    assertEquals(3, textAnnotationType.getAnnotationValues().size());

    Map<Reagent,AnnotationValue> numericAnnotationValues = study.getAnnotationTypes().first().getAnnotationValues();
    Map<Reagent,AnnotationValue> textAnnotationValues = study.getAnnotationTypes().last().getAnnotationValues();

    int i = 0;
    for (Reagent reagent : new TreeSet<Reagent>(study.getReagents())) {
      SmallMoleculeReagent smr = new EntityInflator<SmallMoleculeReagent>(genericEntityDao, (SmallMoleculeReagent) reagent, true).
        need(SmallMoleculeReagent.compoundNames).
        need(Reagent.annotationValues.castToSubtype(SmallMoleculeReagent.class)).
        inflate();

      assertEquals(compoundNames[i], smr.getPrimaryCompoundName());
      assertEquals(expectedData[0][i], numericAnnotationValues.get(smr).getNumericValue());
      assertTrue(!smr.getAnnotationValues().isEmpty());
      assertEquals(expectedData[0][i], smr.getAnnotationValues().get(numericAnnotationType).getNumericValue());
      assertEquals(expectedData[1][i], textAnnotationValues.get(smr).getValue());
      assertEquals(expectedData[1][i], smr.getAnnotationValues().get(textAnnotationType).getValue());
      ++i;
    }
  }

  private void doTest(Screen study)
  {
    String[] rvIds = { "Vendor1 rnai1", "Vendor1 rnai2", "Vendor1 rnai3" };
    Object[][] expectedData = {
      { 1.0, 2.0, 3.0 },
      { "a", "b", "c" }
    };

    assertEquals("annotation type count", expectedData.length, study.getAnnotationTypes().size());
    assertEquals("reagent count", rvIds.length, study.getReagents().size());

    AnnotationType numericAnnotationType = study.getAnnotationTypes().first();
    AnnotationType textAnnotationType = study.getAnnotationTypes().last();

    assertTrue(numericAnnotationType.isNumeric());
    assertFalse(textAnnotationType.isNumeric());
    assertEquals("NumericAnnotation", numericAnnotationType.getName());
    assertEquals("numeric annotation", numericAnnotationType.getDescription());
    assertEquals("TextAnnotation", textAnnotationType.getName());
    assertEquals("text annotation", textAnnotationType.getDescription());
    assertEquals(3, numericAnnotationType.getAnnotationValues().size());
    assertEquals(3, textAnnotationType.getAnnotationValues().size());

    Map<Reagent,AnnotationValue> numericAnnotationValues = study.getAnnotationTypes().first().getAnnotationValues();
    Map<Reagent,AnnotationValue> textAnnotationValues = study.getAnnotationTypes().last().getAnnotationValues();

    int i = 0;
    for (Reagent reagent : new TreeSet<Reagent>(study.getReagents())) {
      assertEquals(rvIds[i], reagent.getVendorId().toString());
      assertEquals(expectedData[0][i], numericAnnotationValues.get(reagent).getNumericValue());
      assertEquals(expectedData[0][i], reagent.getAnnotationValues().get(numericAnnotationType).getNumericValue());
      assertEquals(expectedData[1][i], textAnnotationValues.get(reagent).getValue());
      assertEquals(expectedData[1][i], reagent.getAnnotationValues().get(textAnnotationType).getValue());
      ++i;
    }
  }
}
