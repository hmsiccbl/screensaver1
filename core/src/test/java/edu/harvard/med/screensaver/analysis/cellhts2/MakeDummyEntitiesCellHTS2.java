// $HeadURL$
// $Id$
//
// Copyright © 2008, 2010, 2011, 2012 by the President and Fellows of NKI.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.
/**
 * Uses the MakeDummyEntities in edu.harvard.med.screensaver.model, however it
 * overrides the resultValues with simple, fixed data in stead of randomized.
 * This fixed data is also used in the RUnit tests.
 */

/**
 */

package edu.harvard.med.screensaver.analysis.cellhts2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableMap;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;

import edu.harvard.med.screensaver.model.activities.AdministrativeActivity;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.PlateSize;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.AssayWell;
import edu.harvard.med.screensaver.model.screenresults.AssayWellControlType;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.test.MakeDummyEntities;

public class MakeDummyEntitiesCellHTS2
{
  private static final ImmutableMap<String,AssayWellControlType> ASSAY_CONTROL_WELLS = ImmutableMap.of("A01", AssayWellControlType.ASSAY_POSITIVE_CONTROL,
                                                                                                       "B01", AssayWellControlType.ASSAY_CONTROL,
                                                                                                       "C01", AssayWellControlType.ASSAY_CONTROL_SHARED);

  // static members
  private static Logger log = Logger.getLogger(MakeDummyEntitiesCellHTS2.class);

  public static Library makeSimpleDummyLibrary(int id,
                                               ScreenType screenType,
                                               int nPlates)
  {
    return makeSimpleDummyLibrary(id, screenType, nPlates, 2);
  }

  
  public static Library makeSimpleDummyLibrary(int id,
                                               ScreenType screenType,
                                               int nPlates,
                                               int nrPlateColumns) {
    
    return(makeSimpleDummyLibrary(id,screenType,nPlates,nrPlateColumns,false));
  }
  
  public static Library makeSimpleDummyLibrary(int id,
                                               ScreenType screenType,
                                               int nPlates,
                                               int nrPlateColumns,
                                               boolean inclNS)
  {
    int startPlate = 1;
    int endPlate = startPlate + nPlates;
    int nrPlateRows;
    if (inclNS) {
      nrPlateRows = 3;
    }else {
      nrPlateRows = 2;
    }
    
    
    int nWells = nPlates * nrPlateColumns * nrPlateRows;
    Library library = new Library(null,
                                  "library " + id,
                                  "l" + id,
                                  screenType,
                                  LibraryType.COMMERCIAL,
                                  startPlate,
                                  endPlate,
                                  PlateSize.WELLS_384);

    library.createContentsVersion(null);
    library.getLatestContentsVersion().release(new AdministrativeActivity((AdministratorUser) library.getLatestContentsVersion().getLoadingActivity().getPerformedBy(), new LocalDate(), AdministrativeActivityType.LIBRARY_CONTENTS_VERSION_RELEASE));
    
    List<Well> wells = new ArrayList<Well>(nWells);
    for (int i = 0; i < nWells; ++i) {
      int plate = startPlate + (i / (nrPlateColumns * nrPlateRows));
      int row = (i % (nrPlateColumns * nrPlateRows)) / nrPlateColumns;
      int col = (i % (nrPlateColumns * nrPlateRows)) % nrPlateColumns;
      WellKey wellKey = new WellKey(plate, row, col);
      Well well = library.createWell(wellKey,
                                     ASSAY_CONTROL_WELLS.containsKey(wellKey.getWellName()) ? LibraryWellType.EMPTY
                                       : LibraryWellType.EXPERIMENTAL);
      ReagentVendorIdentifier rvi = new ReagentVendorIdentifier("Vendor" + id, "" + i);
      SilencingReagent reagent = well.createSilencingReagent(rvi, SilencingReagentType.SIRNA, "ACTG");
      reagent.getVendorGene().withEntrezgeneSymbol("geneEntrezSym" + wellKey)
      .withSpeciesName("species")
      .withGenbankAccessionNumber("GB" + wellKey.hashCode());      wells.add(well);
    }
    return library;
  }

  public static ScreenResult makeSimpleDummyScreenResult()
  {
    return (makeSimpleDummyScreenResult(false, false, 2));

  }
  
  public static ScreenResult makeSimpleDummyScreenResult(boolean withExcl, boolean withNull,int nrPlateColumns){
    return (makeSimpleDummyScreenResult( withExcl,  withNull, nrPlateColumns,false));
    
  }
  
  public static ScreenResult makeSimpleDummyScreenResult(boolean withExcl, boolean withNull,int nrPlateColumns,boolean withoutA01ResultValue) {
    return (makeSimpleDummyScreenResult( withExcl,  withNull, nrPlateColumns,withoutA01ResultValue,false));
  }    
  
  public static ScreenResult makeSimpleDummyScreenResult(boolean withExcl, boolean withNull, int nrPlateColumns,boolean withoutA01ResultValue, boolean inclNS){
    return (makeSimpleDummyScreenResult( withExcl,  withNull, nrPlateColumns,withoutA01ResultValue,inclNS,false));
  }

  public static ScreenResult makeSimpleDummyScreenResult(boolean withExcl,
                                                         boolean withNull,
                                                         int nrPlateColumns,
                                                         boolean withoutA01ResultValue,
                                                         boolean inclNS,
                                                         boolean multiChannel)
  {

    Screen screen = MakeDummyEntities.makeDummyScreen(1, ScreenType.RNAI); 

    
    
    Library library = makeSimpleDummyLibrary(1,
                                             screen.getScreenType(),
                                             2,
                                             nrPlateColumns,
                                             inclNS);
    
    ScreenResult screenResult = screen.createScreenResult();

    // create DataColumns
    // Define two replicates
    DataColumn normRep1 = screenResult.createDataColumn("rep1").forReplicate(1).forPhenotype("phenotype").makeNumeric(3);
    DataColumn normRep2 = screenResult.createDataColumn("rep2").forReplicate(2).forPhenotype("phenotype").makeNumeric(3);
    
    DataColumn rep1C2 = null;
    DataColumn rep2C2 = null;
    if (multiChannel) {
      rep1C2 = screenResult.createDataColumn("c2_rep1").forReplicate(1).forChannel(2).forPhenotype("phenotype").makeNumeric(3);
      rep2C2 = screenResult.createDataColumn("c2_rep2").forReplicate(2).forChannel(2).forPhenotype("phenotype").makeNumeric(3);
    }


    // create ResultValues
    // [Well(00001:A01), Well(00001:A02), Well(00001:B01), Well(00001:B02),
    // Well(00002:A01), Well(00002:A02), Well(00002:B01), Well(00002:B02)]
    List<Well> wells = new ArrayList<Well>(library.getWells());
    // In the library the wells are a Set and not a SortedSet
    Collections.sort(wells);
    // Use the same values as in RUnit tests much as possible
    // [Well(00001:A01), Well(00001:A02), Well(00001:A03), Well(00001:B01),
    // Well(00001:B02), Well(00001:B03), Well(00002:A01), Well(00002:A02),
    // Well(00002:A03), Well(00002:B01), Well(00002:B02), Well(00002:B03)]

    ArrayList<Double> rep1Values = new ArrayList<Double>();
    rep1Values.add(new Double(1));
    rep1Values.add(new Double(2));
    rep1Values.add(new Double(3));
    rep1Values.add(new Double(4));
    if (inclNS) {
      rep1Values.add(new Double(2));
      rep1Values.add(null);
    }
    
    rep1Values.add(new Double(5));
    rep1Values.add(new Double(6));
    rep1Values.add(new Double(7));
    rep1Values.add(new Double(9));
    if (inclNS) {
      rep1Values.add(new Double(4));
      rep1Values.add(null);
    }    

    

    ArrayList<Double> rep2Values = new ArrayList<Double>();
    rep2Values.add(new Double(9));
    rep2Values.add(new Double(10));
    rep2Values.add(new Double(11));
    rep2Values.add(new Double(14));
    if (inclNS) {
      rep2Values.add(new Double(6));
      rep2Values.add(null);
    }
    rep2Values.add(new Double(13));
    rep2Values.add(new Double(14));
    rep2Values.add(new Double(15));
    rep2Values.add(new Double(19));
    if (inclNS) {
      rep2Values.add(new Double(8));
      rep2Values.add(null);
    }   
    
    if (nrPlateColumns == 3) {
      rep1Values.add(new Double(10));
      rep1Values.add(new Double(11));
      rep1Values.add(new Double(15));
      rep1Values.add(new Double(13));

      rep2Values.add(new Double(21));
      rep2Values.add(new Double(23));
      rep2Values.add(new Double(26));
      rep2Values.add(new Double(29));
    }
    
    ArrayList<Double> rep1C2Values = new ArrayList<Double>();
    ArrayList<Double> rep2C2Values = new ArrayList<Double>();
    if (multiChannel) {
        
      rep1C2Values.add(new Double(4));
      rep1C2Values.add(new Double(6));
      rep1C2Values.add(new Double(5));
      rep1C2Values.add(new Double(6));
      rep1C2Values.add(new Double(8));
      rep1C2Values.add(new Double(7));

      rep1C2Values.add(new Double(14));
      rep1C2Values.add(new Double(16));
      rep1C2Values.add(new Double(15));
      rep1C2Values.add(new Double(16));
      rep1C2Values.add(new Double(18));
      rep1C2Values.add(new Double(17));
 
      rep2C2Values.add(new Double(6));
      rep2C2Values.add(new Double(8));
      rep2C2Values.add(new Double(7));
      rep2C2Values.add(new Double(8));
      rep2C2Values.add(new Double(10));
      rep2C2Values.add(new Double(9));

      rep2C2Values.add(new Double(16));
      rep2C2Values.add(new Double(18));
      rep2C2Values.add(new Double(17));
      rep2C2Values.add(new Double(18));
      rep2C2Values.add(new Double(20));
      rep2C2Values.add(new Double(19));
      
    }


    AssayWellControlType assayWellControlType = null;
    for (int i = 0; i < wells.size(); ++i) {
      Well well = wells.get(i);
      String wellName = well.getWellKey().getWellName();
      boolean exclude = false;
      if (withExcl && i == 2) { // set exclude for plate 1 well B01. In case of multiple channels, all channels.
        exclude = true;
      }

      // also test for null value
      if (withNull) {
        rep1Values.set(2, null);
        rep2Values.set(2, null);
      }
      
      AssayWell assayWell = screenResult.createAssayWell(well);
      if (ASSAY_CONTROL_WELLS.containsKey(assayWell.getLibraryWell().getWellName())) {
        assayWell.setAssayWellControlType(ASSAY_CONTROL_WELLS.get(assayWell.getLibraryWell().getWellName()));
      }

      if (withoutA01ResultValue && wellName.equals("A01")) { } 
      else {
        normRep1.createResultValue(assayWell, rep1Values.get(i), exclude);
        normRep2.createResultValue(assayWell, rep2Values.get(i), exclude);
        if (multiChannel) {
          rep1C2.createResultValue(assayWell, rep1C2Values.get(i), exclude);
          rep2C2.createResultValue(assayWell, rep2C2Values.get(i), exclude);
        }
      }
    }
    screen.setLibraryPlatesDataLoadedCount(2);
    return screenResult;
  }

}
