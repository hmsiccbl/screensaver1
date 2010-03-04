// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryContentsVersion;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.MolecularFormula;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.AssayWell;
import edu.harvard.med.screensaver.model.screenresults.AssayWellType;
import edu.harvard.med.screensaver.model.screenresults.PartitionedValue;
import edu.harvard.med.screensaver.model.screenresults.PositiveIndicatorDirection;
import edu.harvard.med.screensaver.model.screenresults.PositiveIndicatorType;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.model.screens.StudyType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.AffiliationCategory;
import edu.harvard.med.screensaver.model.users.LabAffiliation;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;

import com.google.common.collect.Sets;

public class MakeDummyEntities
{
  // static members

  private static Logger log = Logger.getLogger(MakeDummyEntities.class);
  private static TestDataFactory dataFactory = new TestDataFactory();

  public static ScreeningRoomUser makeDummyUser(int screenNumber, String first, String last)
  {
    return new ScreeningRoomUser(first,
                                 last + "_" + screenNumber);
  }

  public static LabHead makeDummyLabHead(int screenNumber, String first, String last)
  {
    return new LabHead(first,
                       last + "_" + screenNumber,
                       new LabAffiliation("affiliation " + screenNumber, AffiliationCategory.HMS));
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
    LabHead labHead = makeDummyLabHead(screenNumber, "Lab", "Head");
    ScreeningRoomUser leadScreener = makeDummyUser(screenNumber, "Lead", "Screener");
    Screen screen = new Screen(leadScreener,
                               labHead,
                               screenNumber,
                               screenType,
                               studyType,
                               "Dummy screen");
    return screen;
  }

  public static ScreenResult makeDummyScreenResult(Screen screen, Library library)
  {
    ScreenResult screenResult = screen.createScreenResult();

    // create ResultValueTypes

    screenResult.createResultValueType("numeric_repl1", 1, false, false, false, "phenotype").setNumeric(true);
    screenResult.createResultValueType("numeric_repl2", 2, false, false, false, "phenotype").setNumeric(true);
    screenResult.createResultValueType("text_repl1", 1, false, false, false, "phenotype").setNumeric(false);
    screenResult.createResultValueType("text_repl2", 2, false, false, false, "phenotype").setNumeric(false);

    ResultValueType positive1Rvt = screenResult.createResultValueType("positive1");
    positive1Rvt.setReplicateOrdinal(1);
    positive1Rvt.makeDerived("from replicate 1", Sets.newHashSet(screenResult.getResultValueTypesList().get(0), screenResult.getResultValueTypesList().get(2)));
    positive1Rvt.setAssayPhenotype("phenotype");
    positive1Rvt.makeNumericalPositivesIndicator(PositiveIndicatorDirection.HIGH_VALUES_INDICATE, 10.0);

    ResultValueType positive2Rvt = screenResult.createResultValueType("positive2");
    positive2Rvt.setReplicateOrdinal(2);
    positive2Rvt.makeDerived("from replicate 2", Sets.newHashSet(screenResult.getResultValueTypesList().get(1), screenResult.getResultValueTypesList().get(3)));
    positive2Rvt.setAssayPhenotype("phenotype");
    positive2Rvt.makeNumericalPositivesIndicator(PositiveIndicatorDirection.HIGH_VALUES_INDICATE, 10.0);
    

    ResultValueType positiveRvt = screenResult.createResultValueType("positive", null, true, true, false, "phenotype");
    positiveRvt.makeDerived("from both replicates", Sets.newHashSet(screenResult.getResultValueTypesList().get(4), screenResult.getResultValueTypesList().get(5)));
    positiveRvt.setNumeric(false);
    positiveRvt.makePositivesIndicator(PositiveIndicatorType.PARTITION);

    ResultValueType commentsRvt = screenResult.createResultValueType("comments");
    commentsRvt.setNumeric(false);
    commentsRvt.setDescription("a data header with sparse values (some are null, some are empty strings)");

    // create ResultValues

    List<Well> wells = new ArrayList<Well>(library.getWells());
    Collections.sort(wells);
    for (int i = 0; i < wells.size(); ++i) {
      Well well = wells.get(i);
      AssayWell assayWell = screenResult.createAssayWell(well, AssayWellType.EXPERIMENTAL);
      for (ResultValueType rvt : screenResult.getResultValueTypesList()) {
        if (rvt.isNumeric()) {
          rvt.createResultValue(assayWell, Math.random() * 200.0 - 100.0, 3);
        }
        else if (rvt.isPositiveIndicator() && rvt.getPositiveIndicatorType() == PositiveIndicatorType.PARTITION) {
          rvt.createResultValue(assayWell, PartitionedValue.values()[i % PartitionedValue.values().length].getValue());
        }
        else if (rvt.equals(commentsRvt)) {
          PartitionedValue pv = PartitionedValue.values()[i % PartitionedValue.values().length];
          if (pv != PartitionedValue.NONE) { // else, a null test value, by virtue of not creating a RV for this well/rvt
            rvt.createResultValue(assayWell, pv == PartitionedValue.STRONG ? "what a positive!" :
              pv == PartitionedValue.MEDIUM ? "so so" :
                // a "empty string" test value
                "");
          }
        }
        else if (!rvt.isNumeric()) {
          rvt.createResultValue(assayWell, String.format("text%05d", i));
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
    assert library.getLatestReleasedContentsVersion() != null;

    Screen study = MakeDummyEntities.makeDummyScreen(Study.MIN_STUDY_NUMBER, library.getScreenType());
    AnnotationType annotType1 = new AnnotationType(study, "numeric_annot", "numeric annotation", 0, true);
    AnnotationType annotType2 = new AnnotationType(study, "text_annot", "text annotation", 1, false);
    Iterator<Well> wellIter = new TreeSet<Well>(library.getWells()).iterator();
    Reagent reagent1 = wellIter.next().getLatestReleasedReagent();
    assert reagent1 != null : "expected reagents to exist in the provided library";
    annotType1.createAnnotationValue(reagent1, "1.01" );
    annotType2.createAnnotationValue(reagent1, "aaa" );
    Reagent reagent2 = wellIter.next().getLatestReleasedReagent();
    assert reagent2 != null : "expected reagents to exist in the provided library";
    annotType1.createAnnotationValue(reagent2, "-2.02" );
    annotType2.createAnnotationValue(reagent2, "bbb" );
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
    Library library = new Library("library " + id,
                                  "l" + id,
                                  screenType,
                                  LibraryType.COMMERCIAL,
                                  startPlate,
                                  endPlate);
    dataFactory.newInstance(LibraryContentsVersion.class, library);
    int nWells = nPlates * library.getPlateSize().getWellCount();
    List<Well> wells = new ArrayList<Well>(nWells);
    for (int i = 0; i < nWells; ++i) {
      int plate = startPlate + (i / library.getPlateSize().getWellCount());
      int row = (i % library.getPlateSize().getWellCount()) / library.getPlateSize().getColumns();
      int col = (i % library.getPlateSize().getWellCount()) % library.getPlateSize().getColumns();
      WellKey wellKey = new WellKey(plate, row, col);
      Well well = library.createWell(wellKey, LibraryWellType.EXPERIMENTAL);
      if (library.getScreenType() == ScreenType.RNAI) {
        SilencingReagent reagent = 
          well.createSilencingReagent(new ReagentVendorIdentifier("Vendor" + id, "" + i),
                                      SilencingReagentType.SIRNA,
                                      "ACTG");
        reagent.getFacilityGene()
        .withGenbankAccessionNumber("GB" + wellKey.hashCode())
        .withGeneName("geneName" + wellKey);
      }
      else {
        SmallMoleculeReagent smallMolecule = 
          well.createSmallMoleculeReagent(new ReagentVendorIdentifier("Vendor" + id, "" + i),
                                          "molfileContents",
                                          "smiles" + wellKey, 
                                          "inchi" + wellKey, 
                                          new BigDecimal("1.011"), 
                                          new BigDecimal("1.011"), 
                                          new MolecularFormula("C3"));
          smallMolecule.getPubchemCids().add(10000 + i);
          smallMolecule.getCompoundNames().add("compound" + i);
      }
      wells.add(well);
    }
    library.getLatestContentsVersion().release(new AdministrativeActivity((AdministratorUser) library.getLatestContentsVersion().getLoadingActivity().getPerformedBy(), new LocalDate(), AdministrativeActivityType.LIBRARY_CONTENTS_VERSION_RELEASE));
    return library;
  }

}

