// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellType;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.PartitionedValue;
import edu.harvard.med.screensaver.model.screenresults.PositiveIndicatorDirection;
import edu.harvard.med.screensaver.model.screenresults.PositiveIndicatorType;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.model.screens.StudyType;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUserClassification;

import org.apache.log4j.Logger;

public class MakeDummyEntities
{
  // static members

  private static Logger log = Logger.getLogger(MakeDummyEntities.class);

  public static ScreeningRoomUser makeDummyUser(int screenNumber, String first, String last)
  {
    return new ScreeningRoomUser(new Date(),
                                 first,
                                 last + "_" + screenNumber,
                                 first.toLowerCase() + "_" + last.toLowerCase() + "_" + screenNumber + "@hms.harvard.edu",
                                 "",
                                 "",
                                 "",
                                 "",
                                 "",
                                 ScreeningRoomUserClassification.ICCBL_NSRB_STAFF,
                                 true);
  }

  public static Screen makeDummyScreen(int screenNumber, ScreenType screenType)
  {
    return makeDummyScreen(screenNumber, screenType, StudyType.IN_VITRO);
  }

  public static Screen makeDummyScreen(int screenNumber)
  {
    return makeDummyScreen(screenNumber, ScreenType.SMALL_MOLECULE);
  }

  public static Screen makeDummyScreen(int screenNumber,
                                       ScreenType screenType,
                                       StudyType studyType)
  {
    ScreeningRoomUser labHead = makeDummyUser(screenNumber, "Lab", "Head");
    ScreeningRoomUser leadScreener = makeDummyUser(screenNumber, "Lead", "Screener");
    Screen screen = new Screen(labHead,
                               leadScreener,
                               screenNumber,
                               new Date(),
                               screenType,
                               studyType,
                               "Dummy screen");
    return screen;
  }

  public static ScreenResult makeDummyScreenResult(Screen screen, Library library)
  {
    ScreenResult screenResult = screen.createScreenResult(new Date());

    // create ResultValueTypes

    screenResult.createResultValueType("numeric_repl1", 1, false, false, false, "phenotype").detemineNumericalness(true);
    screenResult.createResultValueType("numeric_repl2", 2, false, false, false, "phenotype").detemineNumericalness(true);
    screenResult.createResultValueType("text_repl1", 1, false, false, false, "phenotype").detemineNumericalness(false);
    screenResult.createResultValueType("text_repl2", 2, false, false, false, "phenotype").detemineNumericalness(false);

    ResultValueType positive1Rvt = screenResult.createResultValueType("positive1", 1, true, true, false, "phenotype");
    positive1Rvt.setHowDerived("from replicate 1");
    positive1Rvt.addTypeDerivedFrom(screenResult.getResultValueTypesList().get(0));
    positive1Rvt.addTypeDerivedFrom(screenResult.getResultValueTypesList().get(2));
    positive1Rvt.setPositiveIndicatorType(PositiveIndicatorType.NUMERICAL);
    positive1Rvt.setPositiveIndicatorDirection(PositiveIndicatorDirection.HIGH_VALUES_INDICATE);
    positive1Rvt.setPositiveIndicatorCutoff(10.0);

    ResultValueType positive2Rvt = screenResult.createResultValueType("positive2", 2, true, true, false, "phenotype");
    positive2Rvt.setHowDerived("from replicate 2");
    positive2Rvt.addTypeDerivedFrom(screenResult.getResultValueTypesList().get(1));
    positive2Rvt.addTypeDerivedFrom(screenResult.getResultValueTypesList().get(3));
    positive2Rvt.setPositiveIndicatorType(PositiveIndicatorType.NUMERICAL);
    positive2Rvt.setPositiveIndicatorDirection(PositiveIndicatorDirection.HIGH_VALUES_INDICATE);
    positive2Rvt.setPositiveIndicatorCutoff(10.0);

    ResultValueType positiveRvt = screenResult.createResultValueType("positive", null, true, true, false, "phenotype");
    positiveRvt.setHowDerived("from both replicates");
    positiveRvt.addTypeDerivedFrom(screenResult.getResultValueTypesList().get(4));
    positiveRvt.addTypeDerivedFrom(screenResult.getResultValueTypesList().get(5));
    positiveRvt.detemineNumericalness(false);
    positiveRvt.setPositiveIndicatorType(PositiveIndicatorType.PARTITION);

    ResultValueType commentsRvt = screenResult.createResultValueType("comments");
    commentsRvt.detemineNumericalness(false);
    commentsRvt.setDescription("a data header with sparse values (some are null, some are empty strings)");

    // create ResultValues

    List<Well> wells = new ArrayList<Well>(library.getWells());
    Collections.sort(wells);
    for (int i = 0; i < wells.size(); ++i) {
      Well well = wells.get(i);
      for (ResultValueType rvt : screenResult.getResultValueTypesList()) {
        if (rvt.isNumeric()) {
          rvt.createResultValue(well, Math.random() * 200.0 - 100.0, 3);
        }
        else if (rvt.isPositiveIndicator() && rvt.getPositiveIndicatorType() == PositiveIndicatorType.PARTITION) {
          rvt.createResultValue(well, PartitionedValue.values()[i % PartitionedValue.values().length].getValue());
        }
        else if (rvt.equals(commentsRvt)) {
          PartitionedValue pv = PartitionedValue.values()[i % PartitionedValue.values().length];
          if (pv != PartitionedValue.NONE) { // else, a null test value, by virtue of not creating a RV for this well/rvt
            rvt.createResultValue(well, pv == PartitionedValue.STRONG ? "what a positive!" :
              pv == PartitionedValue.MEDIUM ? "so so" :
                // a "empty string" test value
                "");
          }
        }
        else if (!rvt.isNumeric()) {
          rvt.createResultValue(well, String.format("text%05d", i));
        }
        else {
          throw new RuntimeException("unhandled rvt type" + rvt);
        }
      }
    }

    return screenResult;
  }

  public static Screen makeDummyStudy(Library library)
  {
    Screen study = MakeDummyEntities.makeDummyScreen(Study.MIN_STUDY_NUMBER, library.getScreenType());
    AnnotationType annotType1 = new AnnotationType(study, "numeric_annot", "numeric annotation", 0, true);
    AnnotationType annotType2 = new AnnotationType(study, "text_annot", "text annotation", 1, false);
    Iterator<Well> wellIter = new TreeSet<Well>(library.getWells()).iterator();
    Well well = wellIter.next();
    annotType1.createAnnotationValue(well.getReagent(), "1.01" );
    annotType2.createAnnotationValue(well.getReagent(), "aaa" );
    well = wellIter.next();
    annotType1.createAnnotationValue(well.getReagent(), "-2.02" );
    annotType2.createAnnotationValue(well.getReagent(), "bbb" );
    return study;
  }

  public static Library makeDummyLibrary(int id, ScreenType screenType, int nPlates)
  {
    if (id >= 100) {
      throw new IllegalArgumentException("violated: 0 <= id < 100");
    }
    int startPlate = id * 1000;
    int endPlate = startPlate + nPlates;
    if (nPlates > 1000) {
      throw new IllegalArgumentException("too many plates requested");
    }
    int nWells = nPlates * (Well.PLATE_COLUMNS * Well.PLATE_ROWS);
    Library library = new Library("library " + id,
                                  "l" + id,
                                  screenType,
                                  LibraryType.COMMERCIAL,
                                  startPlate,
                                  endPlate);
    List<Well> wells = new ArrayList<Well>(nWells);
    for (int i = 0; i < nWells; ++i) {
      int plate = startPlate + (i / (Well.PLATE_COLUMNS * Well.PLATE_ROWS));
      int row = (i % (Well.PLATE_COLUMNS * Well.PLATE_ROWS)) / Well.PLATE_COLUMNS;
      int col = (i % (Well.PLATE_COLUMNS * Well.PLATE_ROWS)) % Well.PLATE_COLUMNS;
      WellKey wellKey = new WellKey(plate, row, col);
      Well well = library.createWell(wellKey, WellType.EXPERIMENTAL);
      if (library.getScreenType() == ScreenType.RNAI) {
        Gene gene = new Gene("geneName" + wellKey,
                             wellKey.hashCode(),
                             "geneEntrezSym" + wellKey,
                             "species");
        gene.addGenbankAccessionNumber("GB" + wellKey.hashCode());
        SilencingReagent silencingReagent = gene.createSilencingReagent(SilencingReagentType.SIRNA, "ACTG");
        well.addSilencingReagent(silencingReagent);
      }
      else {
        Compound compound = new Compound("smiles" + wellKey, "inchi" + wellKey);
        well.addCompound(compound);
      }
      well.setReagent(new Reagent(new ReagentVendorIdentifier("Vendor" + id + ":" + i)));
      wells.add(well);
    }
    return library;
  }


  // instance data members

  // public constructors and methods

  // private methods

}

