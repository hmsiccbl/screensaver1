// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.screens;

import java.util.Map;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.io.UnrecoverableParseException;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.Study;

public class StudyAnnotationParserTest extends AbstractSpringPersistenceTest
{
  private static final String WORKBOOK_FILE = "edu/harvard/med/screensaver/io/screens/test-study.xls";

  private static Logger log = Logger.getLogger(StudyAnnotationParserTest.class);

  protected StudyAnnotationParser studyAnnotationParser;

  public void testStudyAnnotationParser() throws UnrecoverableParseException
  {
    String[] rvIds = { "Vendor1 rnai1", "Vendor1 rnai2", "Vendor1 rnai3" };
    Object[][] expectedData = {
      { 1.0, 2.0, 3.0 },
      { "a", "b", "c" }
    };

    Library library = MakeDummyEntities.makeDummyLibrary(1, ScreenType.RNAI, 1);
    genericEntityDao.persistEntity(library);
    
    Screen study = MakeDummyEntities.makeDummyScreen(Study.MIN_STUDY_NUMBER, library.getScreenType());
    studyAnnotationParser.parse(study, getClass().getClassLoader().getResourceAsStream(WORKBOOK_FILE));
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
