// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2008 by the President and Fellows of NKI.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.
/**
 * Uses the MakeDummyEntities in edu.harvard.med.screensaver.model, 
 * however it overrides the resultValues with simple, fixed data in stead of randomized.
 * This fixed data is also used in the RUnit tests. 
 */

/**
 */

package edu.harvard.med.screensaver.analysis.cellhts2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.MakeDummyEntities;
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
import edu.harvard.med.screensaver.model.screenresults.AssayWellType;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;

public class MakeDummyEntitiesCellHTS2 {
  // static members

  private static Logger log = Logger.getLogger(MakeDummyEntitiesCellHTS2.class);

  public static Library makeSimpleDummyLibrary(int id, ScreenType screenType,
                                               int nPlates ) {
    return makeSimpleDummyLibrary(id, screenType, nPlates ,2);
  }
  
  public static Library makeSimpleDummyLibrary(int id, ScreenType screenType,
      int nPlates ,int nrPlateColumns) {
    int startPlate = 1;
    int endPlate = startPlate + nPlates;
    int nrPlateRows = 2;
    int nWells = nPlates * nrPlateColumns * nrPlateRows;
    Library library = new Library("library " + id, "l" + id, screenType,
        LibraryType.COMMERCIAL, startPlate, endPlate);

    List<Well> wells = new ArrayList<Well>(nWells);
    for (int i = 0; i < nWells; ++i) {
      int plate = startPlate + (i / (nrPlateColumns * nrPlateRows));
      int row = (i % (nrPlateColumns * nrPlateRows)) / nrPlateColumns;
      int col = (i % (nrPlateColumns * nrPlateRows)) % nrPlateColumns;
      WellKey wellKey = new WellKey(plate, row, col);
      Well well = library.createWell(wellKey, WellType.EXPERIMENTAL);
      Gene gene = new Gene("geneName" + wellKey, wellKey.hashCode(),
          "geneEntrezSym" + wellKey, "species");
      gene.addGenbankAccessionNumber("GB" + wellKey.hashCode());
      SilencingReagent silencingReagent = gene.createSilencingReagent(
          SilencingReagentType.SIRNA, "ACTG");
      well.addSilencingReagent(silencingReagent);
      well.setReagent(new Reagent(new ReagentVendorIdentifier("Vendor" + id
          + ":" + i)));
      wells.add(well);
    }
    return library;
  }

  public static ScreenResult makeSimpleDummyScreenResult() {
    return(makeSimpleDummyScreenResult(false,false,2));
    
  }
  
  public static ScreenResult makeSimpleDummyScreenResult(boolean withExcl, boolean withNull,int nrPlateColumns){
    return (makeSimpleDummyScreenResult( withExcl,  withNull, nrPlateColumns,false));
    
  }
  
  public static ScreenResult makeSimpleDummyScreenResult(boolean withExcl, boolean withNull,int nrPlateColumns,boolean withoutA01ResultValue) {

    Screen screen = MakeDummyEntities.makeDummyScreen(1, ScreenType.RNAI); //That is without the results

    Library library = makeSimpleDummyLibrary(screen.getScreenNumber(), screen.getScreenType(), 2,nrPlateColumns);

    ScreenResult screenResult = screen.createScreenResult();

    // create ResultValueTypes
    // Define two replicates
    ResultValueType normRep1 = screenResult.createResultValueType("rep1", 1,
        false, false, false, "phenotype");
    normRep1.setNumeric(true);
    ResultValueType normRep2 = screenResult.createResultValueType("rep2", 2,
        false, false, false, "phenotype");
    normRep2.setNumeric(true);

    // create ResultValues
 // [Well(00001:A01), Well(00001:A02), Well(00001:B01), Well(00001:B02), Well(00002:A01), Well(00002:A02), Well(00002:B01), Well(00002:B02)]
    List<Well> wells = new ArrayList<Well>(library.getWells());
    //In  the library the wells are a Set and not a SortedSet
    Collections.sort(wells);
    // Use the same values as in RUnit tests much as possible

    ArrayList<Double> rep1Values = new ArrayList<Double>();
    rep1Values.add(new Double(1));
    rep1Values.add(new Double(2));
    rep1Values.add(new Double(3));
    rep1Values.add(new Double(4));
    rep1Values.add(new Double(5));
    rep1Values.add(new Double(6));
    rep1Values.add(new Double(7));
    rep1Values.add(new Double(9));

    ArrayList<Double> rep2Values = new ArrayList<Double>();    
    rep2Values.add(new Double(9));    
    rep2Values.add(new Double(10));    
    rep2Values.add(new Double(11));    
    rep2Values.add(new Double(14));    
    rep2Values.add(new Double(13));    
    rep2Values.add(new Double(14));    
    rep2Values.add(new Double(15));    
    rep2Values.add(new Double(19));    

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
    
      
    AssayWellType assayWellType = null;
    for (int i = 0; i < wells.size(); ++i) {
      Well well = wells.get(i);
      String wellName = well.getWellKey().getWellName();
      if (wellName.equals("A01")) {
        assayWellType = AssayWellType.ASSAY_POSITIVE_CONTROL;
      } else if (wellName.equals("B01")) {
        assayWellType = AssayWellType.ASSAY_CONTROL;
      } else {
        assayWellType = AssayWellType.EXPERIMENTAL;
      }

      boolean exclude =false;
      if (withExcl &&  i==2){ //set exclude for plate 1 well B01 
        exclude=true;
      }
      
      
      //also test for null value
      if (withNull) {
        rep1Values.set(2, null);
        rep2Values.set(2, null);
      }
      
      if (withoutA01ResultValue && wellName.equals("A01")) { } 
      else {
        normRep1.createResultValue(well, assayWellType, rep1Values.get(i), 3, exclude);
        normRep2.createResultValue(well, assayWellType, rep2Values.get(i), 3, exclude);
      }
      

      // rvt.getResultValues();
    }

    return screenResult;
  }

}
