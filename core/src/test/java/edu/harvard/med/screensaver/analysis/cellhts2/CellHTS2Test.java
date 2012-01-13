// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.analysis.cellhts2;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.springframework.test.annotation.IfProfileValue;

import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.test.AbstractSpringTest;

@IfProfileValue(name = "screensaver.ui.feature.cellHTS2", value = "true")
public class CellHTS2Test extends AbstractSpringTest
{
  public void testCalculateArrayDimensions()
    throws RserveException,
    REngineException,
    REXPMismatchException
  {

    ScreenResult screenResult = MakeDummyEntitiesCellHTS2.makeSimpleDummyScreenResult();
    String title = "Dummy_Experiment";
    CellHTS2 cellHts2 = new CellHTS2(screenResult, title, ".", null);

    ArrayDimensions expected = new ArrayDimensions();

    expected.setNrChannels(1);
    expected.setNrColsPlate(2);
    expected.setNrPlates(2);
    expected.setNrReps(2);
    expected.setNrRowsPlate(2);
    expected.setNrWells(4);

    assertEquals(expected, cellHts2.getArrayDimensions());
  }

  // Requires a running Rserve and a running database server as defined in
  // the screensaver.properties file.

  public void testReadPlateListDbWithoutNull()
    throws RserveException,
    REngineException,
    REXPMismatchException
  {
    tReadPlateListDb(false,false,false);
  }

  public void testReadPlateListDbWithNull()
    throws RserveException,
    REngineException,
    REXPMismatchException
  {
    tReadPlateListDb(true,false,false);
  }

  public void tReadPlateListDb(boolean withNull)
  throws RserveException,
  REngineException,
    REXPMismatchException
  {
    tReadPlateListDb(true, false,false);
  }

  public void testReadPlateListDbWithoutA01ResultValue()
    throws RserveException,
    REngineException,
    REXPMismatchException
  {
    tReadPlateListDb(false, true,false);
  }
  
  public void testReadPlateListDbMultiChannel()
  throws RserveException,
  REngineException,
  REXPMismatchException
{
  tReadPlateListDb(false, false,true);
}  
  
  public void tReadPlateListDb(boolean withNull, boolean withoutA01ResultValue, boolean multiChannel)
    throws RserveException,
    REngineException,
    REXPMismatchException
  {

    ScreenResult screenResult;
    // 1. PREPARE INPUT
    if (multiChannel) {
      screenResult = MakeDummyEntitiesCellHTS2.makeSimpleDummyScreenResult(false,false, 2, false, true, true);
      
    }else { 
      screenResult= MakeDummyEntitiesCellHTS2.makeSimpleDummyScreenResult(false, withNull, 2, withoutA01ResultValue);
    }
    String title = "Dummy_Experiment";

    CellHTS2 cellHts2 = null;
    try {
      cellHts2 = new CellHTS2(screenResult, title, ".", null);

      // 2. RUN METHOD
      cellHts2.readPlateListDbInit();
      cellHts2.run();
      REXP output = cellHts2.getReadPlateListDbResult();
      double[][][] result = cellHts2.convertOutputRserve(output);

      // 3 CHECK EQUALS
      // CREATE DATA EXPECTED
      // In the assaydata slot, the data is now three in stead of 4 dimensional
      // as the plates are now below each other
      // De dimensions are now: "Features","Sample", "Channels" . Sample can
      // also be read as "Replicate". See fData below for the details

      ArrayDimensions arrayDimensions = cellHts2.getArrayDimensions();
      int nrWells = arrayDimensions.getNrWells();
      int nrPlates = arrayDimensions.getNrPlates();
      int nrReps = arrayDimensions.getNrReps();
      int nrChannels = arrayDimensions.getNrChannels();

      double[][][] expected = new double[(nrWells * nrPlates)][nrReps][nrChannels];

      if (multiChannel) {
        // P1R1C1: 1 2 3 4 2 null
        expected[0][0][0] = 1;
        expected[1][0][0] = 2;
        expected[2][0][0] = 3;
        expected[3][0][0] = 4;
        expected[4][0][0] = 2;
        expected[5][0][0] = CellHTS2.NA;

        // P2R1C1: 5 6 7 9 4 null
        expected[6][0][0] = 5;
        expected[7][0][0] = 6;
        expected[8][0][0] = 7;
        expected[9][0][0] = 9;
        expected[10][0][0] = 4;
        expected[11][0][0] = CellHTS2.NA;
        
        // P1R2C1: 9  10  11  14  6 null
        expected[0][1][0] = 9;
        expected[1][1][0] = 10;
        expected[2][1][0] = 11;
        expected[3][1][0] = 14;
        expected[4][1][0] = 6;
        expected[5][1][0] = CellHTS2.NA;
        
        // P2R2C1: 13 14  15  19  8 null
        expected[6][1][0] = 13;
        expected[7][1][0] = 14;
        expected[8][1][0] = 15;
        expected[9][1][0] = 19;
        expected[10][1][0] = 8;
        expected[11][1][0] = CellHTS2.NA;
        
        // P1R1C2: 4 6 5 6 8 7 
        expected[0][0][1] = 4;
        expected[1][0][1] = 6;
        expected[2][0][1] = 5;
        expected[3][0][1] = 6;
        expected[4][0][1] = 8;
        expected[5][0][1] = 7;
        
        // P2R1C2: 14 16 15 16 18 17  
        expected[6][0][1] = 14;
        expected[7][0][1] = 16;
        expected[8][0][1] = 15;
        expected[9][0][1] = 16;
        expected[10][0][1] = 18;
        expected[11][0][1] = 17;
        
        // P1R2C2: 6  8 7 8 10  9
        expected[0][1][1] = 6;
        expected[1][1][1] = 8;
        expected[2][1][1] = 7;
        expected[3][1][1] = 8;
        expected[4][1][1] = 10;
        expected[5][1][1] = 9;
        
        // P2R2C2: 16 18  17  18  20  19
        expected[6][1][1] = 16;
        expected[7][1][1] = 18;
        expected[8][1][1] = 17;
        expected[9][1][1] = 18;
        expected[10][1][1] = 20;
        expected[11][1][1] = 19;
        
      }else {
        //rep 1
        expected[0][0][0] = 1;
        expected[1][0][0] = 2;
        expected[2][0][0] = 3;
        expected[3][0][0] = 4;
        expected[4][0][0] = 5;
        expected[5][0][0] = 6;
        expected[6][0][0] = 7;
        expected[7][0][0] = 9;

        //rep 2
        expected[0][1][0] = 9;
        expected[1][1][0] = 10;
        expected[2][1][0] = 11;
        expected[3][1][0] = 14;
        expected[4][1][0] = 13;
        expected[5][1][0] = 14;
        expected[6][1][0] = 15;
        expected[7][1][0] = 19;        

      if (withNull) {
          expected[2][0][0] = CellHTS2.NA;
          expected[2][1][0] = CellHTS2.NA;
      }
      
      if (withoutA01ResultValue) {
          expected[0][0][0] =CellHTS2.NA;
          expected[4][0][0] =CellHTS2.NA;
          expected[0][1][0] =CellHTS2.NA;
          expected[4][1][0] =CellHTS2.NA;          
      }

      }

      int nrPlatesWells = nrPlates * nrWells;
      for (int pw = 0; pw < nrPlatesWells; pw++) {
        for (int r = 0; r < nrReps; r++) {
          for (int c = 0; c < nrChannels; c++) {
            assertEquals("pw:" + pw + " ,r: " + r +" ,c:" + c  ,expected[pw][r][c], result[pw][r][c]);
          }
        }
      }
    }
    finally {
      cellHts2.closeConnection();
    }

  }

  /**
   * CHECK UPDATES OF SLOTS
   */

  public void testConfigureDb()
    throws RserveException,
    REngineException,
    REXPMismatchException
  {

    // 1. PREPARE INPUT
    ScreenResult screenResult = MakeDummyEntitiesCellHTS2.makeSimpleDummyScreenResult(true,
                                                                                      false,
                                                                                      2);

    String title = "Dummy_Experiment";
    CellHTS2 cellHts2 = null;
    try {
      cellHts2 = new CellHTS2(screenResult, title, ".", null);

      // 2. RUN METHOD CONFIGUREDBS
      cellHts2.configureDbInit();
      cellHts2.run();
      String[] actualResultConf = cellHts2.getConfigureDbResultConf();

      // 3. CHECKEQUALS
      // Check for the content field in the result
      String[] expectedResultConf = { "pos", "sample", "N", "sample", "pos", "sample", "N", "sample" };

      // assertEquals cannot compare String[] straight away. It will compare the
      // memory address
      for (int i = 0; i < expectedResultConf.length; i++) {
        assertEquals(expectedResultConf[i], actualResultConf[i]);
      }

      // Check for result screenlog
      //[[1.0, 1.0], [B01, B01], [1.0, 2.0], [NA, NA], [1, 1]]
      String[][] expectedResultSlog = new String[5][2];
      expectedResultSlog[0][0] = "1.0";
      expectedResultSlog[0][1] = "1.0";
      expectedResultSlog[1][0] = "B01";
      expectedResultSlog[1][1] = "B01";
      expectedResultSlog[2][0] = "1.0";
      expectedResultSlog[2][1] = "2.0";    
      expectedResultSlog[3][0] = "NA";
      expectedResultSlog[3][1] = "NA";        
      expectedResultSlog[4][0] = "1";
      expectedResultSlog[4][1] = "1";    
      
      // TODO other check for the entries in the other columns ic.
      // Well,Sample,Channel,Flag.
      String[][] actualResultSlog = cellHts2.getConfigureDbResultSlog();

      for (int i = 0; i < expectedResultSlog.length; i++) {
        for (int j = 0; i < expectedResultSlog[0].length; i++) {
          assertEquals("i=" + i + " ,j=" + j + " ", expectedResultSlog[i][j], actualResultSlog[i][j]);
        }
      }

      // TODO
      // #3 screenDesc: object of class 'character' containing what was read
      // from input file 'descripFile'.

      // #4 state: the processing status of the 'cellHTS' object is updated in
      // to 'state["configured"]=TRUE'.
      // checkEquals(rc@state[["configured"]],TRUE)

      // #5 featureData: the column 'controlStatus' is updated having into
      // account the well annotation given by the plate configuration file.
      // controlStatusTarget <-
      // factor(c("pos","sample","neg","sample","pos","sample","neg","sample"),levels=c("sample","pos","neg"))
      // controlStatus <- fData(rc)$controlStatus
      // checkEquals(controlStatusTarget,controlStatus)

      // #6 experimentData: an object of class 'MIAME' containing descriptions
      // of the experiment, constructed from the screen description file.
    }
    finally {
      cellHts2.closeConnection();
    }

  }

  
  public void testConfigureDbMultichannel()
  throws RserveException,
  REngineException,
  REXPMismatchException
{

  // 1. PREPARE INPUT
    ScreenResult screenResult = MakeDummyEntitiesCellHTS2.makeSimpleDummyScreenResult(true,
                                                                                      false,
                                                                                      2,
                                                                                      false,
                                                                                      true,
                                                                                      true);  
    

  

  String title = "Dummy_Experiment";
  CellHTS2 cellHts2 = null;
  try {
      cellHts2 = new CellHTS2(screenResult, title, ".", null);

    // 2. RUN METHOD CONFIGUREDBS
    cellHts2.configureDbInit();
    cellHts2.run();


    // 3. CHECKEQUALS
    // Check for the content field in the result. 
    //String[] actualResultConf = cellHts2.getConfigureDbResultConf();
    //TODO (see testConfigureDb)

    // Check for result screenlog
    
    String[][] expectedResultSlog = new String[5][4];
    //[[1.0, 1.0, 1.0, 1.0], [B01, B01, B01, B01], [1.0, 2.0, 1.0, 2.0], [NA, NA, NA, NA], [1, 1, 1, 1]]
    expectedResultSlog[0][0] = "1.0";
    expectedResultSlog[0][1] = "1.0";
    expectedResultSlog[0][2] = "1.0";
    expectedResultSlog[0][3] = "1.0";
    
    expectedResultSlog[1][0] = "B01";
    expectedResultSlog[1][1] = "B01";
    expectedResultSlog[1][2] = "B01";
    expectedResultSlog[1][3] = "B01";

    expectedResultSlog[2][0] = "1.0";
    expectedResultSlog[2][1] = "2.0";    
    expectedResultSlog[2][2] = "1.0";
    expectedResultSlog[2][3] = "2.0";    

    
    expectedResultSlog[3][0] = "NA";
    expectedResultSlog[3][1] = "NA";        
    expectedResultSlog[3][2] = "NA";
    expectedResultSlog[3][3] = "NA";        

    expectedResultSlog[4][0] = "1";
    expectedResultSlog[4][1] = "1";    
    expectedResultSlog[4][2] = "2";
    expectedResultSlog[4][3] = "2";    
    
    // TODO other check for the entries in the other columns ic.
    // Well,Sample,Channel,Flag.
    String[][] actualResultSlog = cellHts2.getConfigureDbResultSlog();

    for (int i = 0; i < expectedResultSlog.length; i++) {
      for (int j = 0; i < expectedResultSlog[0].length; i++) {
        assertEquals("i=" + i + " ,j=" + j + " ", expectedResultSlog[i][j], actualResultSlog[i][j]);
      }
    }


  }
  finally {
    cellHts2.closeConnection();
  }

}

  public void testAnnotateDb()
    throws RserveException,
    REngineException,
    REXPMismatchException
  {
    // 1. PREPARE INPUT

    ScreenResult screenResult = MakeDummyEntitiesCellHTS2.makeSimpleDummyScreenResult();
    String title = "Dummy_Experiment";
    CellHTS2 cellHts2 = null;
    try {
      cellHts2 = new CellHTS2(screenResult, title, ".", null);

      // 2. RUN UNTIL AND INCLUSIVE METHOD ANNOTATEDB
      cellHts2.annotateDbInit();
      cellHts2.run();
      String[] actualResult = cellHts2.getAnnotateDbResult();

      // 3. CHECKEQUALS
      // TODO also check for hfaid especially as here are NA used
      // TODO ADJUST SOON AS THE METHOD ANNOTATE IS FULLY CODED IN STEAD OF
      // GENERATING GENE NAMES.
/*
 * String[] expectedResult = { "MOCK", "SEPT11", "MOCK", "CD47", "MOCK", "AAMP",
 * "MOCK", "CD48" };
 */
      String[] expectedResult = { "GENE0", "GENE1", "GENE2", "GENE3", "GENE4", "GENE5", "GENE6", "GENE7" };

      // assertEquals cannot compare String[] straight away. It will compare the
      // memory address
      for (int i = 0; i < expectedResult.length; i++) {
        assertEquals(expectedResult[i], actualResult[i]);
      }

    }
    finally {
      cellHts2.closeConnection();
    }
  }

  public void testNormalizePlatesMean()
    throws RserveException,
    REngineException,
    REXPMismatchException
  {
    this.normalizePlatesAssert(NormalizePlatesMethod.MEAN,
                               NormalizePlatesScale.ADDITIVE,
                               2);
  }

  public void testNormalizePlatesMedian()
    throws RserveException,
    REngineException,
    REXPMismatchException
  {
    this.normalizePlatesAssert(NormalizePlatesMethod.MEDIAN,
                               NormalizePlatesScale.ADDITIVE,
                               3);
  }


  //conversion is now based on REXP object in stead of double[] 
  //TODO look how to define a REXP object
/*  public void testNormalizePlatesMedianMultiplicative()
    throws RserveException,
    REngineException,
    REXPMismatchException
  {
    this.normalizePlatesAssert(NormalizePlatesMethod.MEDIAN,
                               NormalizePlatesScale.MULTIPLICATIVE,
                               3);
  }

  public void testConvertOutputRserve()
    throws RserveException,
    REngineException,
    REXPMismatchException
  {
    double[] r1c1 = { 0.25, 0.75, 0.5, 0.75, 1.25, 1.0 };
    double[] r2c1 = { 0.5, 0.83, 0.67, 0.83, 1.17, 1.0 };
    double[] r1c2 = { 0.57, 0.86, 0.71, 0.86, 1.14, 1.0 };
    double[] r2c2 = { 0.67, 0.89, 0.78, 0.89, 1.11, 1 };

    double[] output = new double[24];
    System.arraycopy(r1c1, 0, output, 0, 6);
    System.arraycopy(r2c1, 0, output, 6, 6);
    System.arraycopy(r1c2, 0, output, 12, 6);
    System.arraycopy(r2c2, 0, output, 18, 6);

    double[][][] expValues = new double[6][2][2];
    for (int i = 0; i < 6; i++) {
      expValues[i][0][0] = r1c1[i];
      expValues[i][1][0] = r2c1[i];
      expValues[i][0][1] = r1c2[i];
      expValues[i][1][1] = r2c2[i];
    }

    ScreenResult screenResult = MakeDummyEntitiesCellHTS2.makeSimpleDummyScreenResult();
    String title = "Dummy_Experiment";
    CellHTS2 cellHts2 = new CellHTS2(screenResult, title);

    int nrPlates = 2;
    int nrWells = 3;
    int nrReps = 2;
    int nrChannels = 2;
    

    double[][][] actValues = cellHts2.convertOutputRserve(output);

    int nrPlatesWells = 6;
    for (int i = 0; i < nrPlatesWells; i++) {
      for (int j = 0; j < nrReps; j++) {
        for (int k = 0; k < nrChannels; k++) {
          assertTrue("i=" + i + ",j=" + j + ",k=" + k + ",expValues[i][j][k]=" + expValues[i][j][k] + ",actValues[i][j][k]=" +  actValues[i][j][k],
                     expValues[i][j][k] == actValues[i][j][k]);
        }
      }
    }


  }*/

  public void testNormalizePlatesLoess()
    throws RserveException,
    REngineException,
    REXPMismatchException
  {
    this.normalizePlatesAssert(NormalizePlatesMethod.LOESS,
                               NormalizePlatesScale.ADDITIVE,
                               3);
  }


  public void testNormalizePlatesNegativesAdditive()
    throws RserveException,
    REngineException,
    REXPMismatchException
  {
    this.normalizePlatesAssert(NormalizePlatesMethod.NEGATIVES,
                               NormalizePlatesScale.ADDITIVE,
                               2,
                               NormalizePlatesNegControls.NEG,
                               true);
  }

  public void testNormalizePlatesNegativesMultiplicative()
    throws RserveException,
    REngineException,
    REXPMismatchException
  {
    this.normalizePlatesAssert(NormalizePlatesMethod.NEGATIVES,
                               NormalizePlatesScale.MULTIPLICATIVE,
                               2,
                               NormalizePlatesNegControls.NEG,
                               true);
  }

  public void testNormalizePlatesNegativesNegSharedMultiplicative()
    throws RserveException,
    REngineException,
    REXPMismatchException
  {
    this.normalizePlatesAssert(NormalizePlatesMethod.NEGATIVES,
                               NormalizePlatesScale.MULTIPLICATIVE,
                               2,
                               NormalizePlatesNegControls.NEG_SHARED,
                               true);
  }

  public void testNormalizePlatesNegativesNegSharedMultiplicativeMultiChannel()
    throws RserveException,
    REngineException,
    REXPMismatchException
  {
    this.normalizePlatesAssert(NormalizePlatesMethod.NEGATIVES,
                               NormalizePlatesScale.MULTIPLICATIVE,
                               2,
                               NormalizePlatesNegControls.NEG_SHARED,
                               true,
                               true);
  }

  public void testNormalizePlatesNpiNeg()
  throws RserveException,
  REngineException,
  REXPMismatchException
{
  this.normalizePlatesAssert(NormalizePlatesMethod.NPI,
                               NormalizePlatesScale.ADDITIVE, // not relevant
                                                              // for NPI
                             2);
}


  public void normalizePlatesAssert(NormalizePlatesMethod normalizePlatesMethod,
                                    NormalizePlatesScale normalizePlatesScale,
                                    int nrPlateColumns)
    throws RserveException,
    REngineException,
    REXPMismatchException
  {
    normalizePlatesAssert(normalizePlatesMethod,
                          normalizePlatesScale,
                          nrPlateColumns,
                          NormalizePlatesNegControls.NEG,
                          false);
  }
  

  public void normalizePlatesAssert(NormalizePlatesMethod normalizePlatesMethod,
                                    NormalizePlatesScale normalizePlatesScale,
                                    int nrPlateColumns,
                                    NormalizePlatesNegControls normalizePlatesNegControls,
                                    boolean inclNS)
    throws RserveException,
    REngineException,
    REXPMismatchException
  {
    normalizePlatesAssert(normalizePlatesMethod,
                          normalizePlatesScale,
                          nrPlateColumns,
                          NormalizePlatesNegControls.NEG,
                          inclNS,
                          false);
  }

  public double round(double d, int decimalPlace){
    // see the Javadoc about why we use a String in the constructor
    // http://java.sun.com/j2se/1.5.0/docs/api/java/math/BigDecimal.html#BigDecimal(double)
    
    if (new Double(d).equals(CellHTS2.NA)) {
      return d;
    }
    else {
      BigDecimal bd = new BigDecimal(Double.toString(d));
      bd = bd.setScale(decimalPlace,BigDecimal.ROUND_HALF_UP);
      return bd.doubleValue();
    }
  }
  
  public void testRound(){
    double d = round(CellHTS2.NA,2);
  }

  public void normalizePlatesAssert(NormalizePlatesMethod normalizePlatesMethod,
                                    NormalizePlatesScale normalizePlatesScale,
                                    int nrPlateColumns,
                                    NormalizePlatesNegControls normalizePlatesNegControls,
                                    boolean inclNS,
                                    boolean multiChannel)
    throws RserveException,
    REngineException,
    REXPMismatchException
  {


    ScreenResult screenResult = MakeDummyEntitiesCellHTS2.makeSimpleDummyScreenResult(false,
                                                                                      false,
                                                                                      nrPlateColumns,
                                                                                      false,
                                                                                      inclNS,
                                                                                      multiChannel);
    String title = "Dummy_Experiment";

    CellHTS2 cellHts2 = null;
    try {
      cellHts2 = new CellHTS2(screenResult, title, ".", null);
      cellHts2.normalizePlatesInit(normalizePlatesMethod,
                                   normalizePlatesScale,
                                   normalizePlatesNegControls);
      cellHts2.run();
      cellHts2.normalizePlatesAddResult();

      // 3. ASSERTEQUALS
      // // ## CREATE DATA TARGET
      // // ## In the assaydata slot, the data is now three in stead of 4
      // dimensional as the plates are now below each other
      // // ## De dimensions are now: "Features","Sample", "Channels" . Sample
      // can also be read as "Replicate". See fData below for the details
      // //
      // TODO make nrWells etc. final instance variables, and initiate them in
      // constructor.

      // prepare expected values per replicate and per wel

      ArrayList<Double> eValuesRep1 = new ArrayList<Double>();
      ArrayList<Double> eValuesRep2 = new ArrayList<Double>();
      ArrayList<Double> eValuesRep1C2 = new ArrayList<Double>();
      ArrayList<Double> eValuesRep2C2 = new ArrayList<Double>();
 
      
      if (normalizePlatesMethod.equals(NormalizePlatesMethod.NPI)) {
        // NPI (x - mean(P)/(mean(P)- mean(N)) 
/*
 * ## (mean(P) -x) / (mean(P) - mean(N) 
 * ## P1R1 
 * ## A01 P 1 (1-1)/ (1-3) = 0.0 
 * ## A02 X 2 (1-2)/ (1-3) = 0.5 
 * ## B01 N 3 (1-3)/ (1-3) = 1.0 
 * ## B02 X 4 (1-4)/ (1-3) = 1.5 
 * ## P2R1 
 * ## A01 P 5 (5-5)/ (5-7) = 0.0 
 * ## A02 X 6 (5-6)/ (5-7) = 0.5 
 * ## B01 N 7 (5-7)/ (5-7) = 1.0 
 * ## B02 X 9 (5-9)/ (5-7) = 2.0 
 * ##P1R2 
 * ## A01 P 9 (9-9)/ (9-11)= 0.0 
 * ## A02 X 10 (9-10)/ (9-11)= 0.5 
 * ## B01 N 11 (9-11)/ (9-11)= 1 
 * ## B02 X 14 (9-14)/ (9-11)= 2.5 
 * ##P2R2 
 * ## A01 P 13 (13-13)/ (13-15)= 0.0 
 * ## A02 X 14 (13-14)/ (13-15)= 0.5 
 * ## B01 N 15 (13-15)/ (13-15)= 1.0 
 * ## B02 X 19 (13-19)/ (13-15)= 3.0
 */
          
        Collections.addAll(eValuesRep1, new Double(0.0), 
                           new Double(0.5),  
                           new Double(1.0),    
                           new Double(1.5),  
                           new Double(0.0),
                           new Double(0.5),  
                           new Double(1.0),    
                           new Double(2.0));   
          
          Collections.addAll(eValuesRep2,
                           new Double(0.0),
                             new Double(0.5),  
                             new Double(1.0),    
                             new Double(2.5),  
                           new Double(0.0),
                             new Double(0.5),  
                             new Double(1.0),    
                             new Double(3.0));             
        
      }
      else {
        if (normalizePlatesMethod.equals(NormalizePlatesMethod.NEGATIVES)) {
  /*
           * Negative controls 1 well: B01: voor rep1 plate 1: 3 voor rep1 plate
           * 2: 7 voor rep2 plate 1: 11 voor rep2 plate 2: 15 NC plate 1: 2
           * original values for rep1 (2 columns) : 1,2,3,4,5,6,7,9
   */
          if (normalizePlatesNegControls == NormalizePlatesNegControls.NEG_SHARED) {
            /*
             * plate : 2 cols,3 rows R1P1: AO1 1 , A02 2 , B01 3 , B02 4 , C01 2
             * , C02 null R1P2: AO1 5 , A02 6 , B01 7 , B02 9 , C01 4 , C02 null
             * BO1: (normal) negative control, ic. P1: 3, P2: 7) CO1: S (shared
             * negative control, ic. P1: 2, P2: 4)
             */
            
            if (normalizePlatesScale == NormalizePlatesScale.ADDITIVE) {
              Collections.addAll(eValuesRep1,
                                 new Double(-1),
                                 new Double(0),
                                 new Double(1),
                                 new Double(2),
                                 new Double(3),
                                 new Double(4),
                                 new Double(5),
                                 new Double(7));
            }
            else {// multiplicative {
              if (multiChannel) {
                // A01 A02 B01 B02 CO1 CO2
                // CO1: S (shared negative control
                // P1R1C1: 1 2 3 4 2 null
                // P2R1C1: 5 6 7 9 4 null
                // P1R2C1: 9  10  11  14  6 null
                // P2R2C1: 13 14  15  19  8 null
                // P1R1C2: 4 6 5 6 8 7 
                // P2R1C2: 14 16 15 16 18 17  
                // P1R2C2: 6  8 7 8 10  9
                // P2R2C2: 16 18  17  18  20  19

                Collections.addAll(eValuesRep1,
                                   new Double(0.5),
                                   new Double(1),
                                   new Double(1.5),
                                   new Double(2),
                                   new Double(1),
                                   CellHTS2.NA,
                                   new Double(1.25),
                                   new Double(1.50),
                                   new Double(1.75),
                                   new Double(2.25),
                                   new Double(1),
                                   CellHTS2.NA);
                Collections.addAll(eValuesRep2,
                                   new Double(1.50),
                                   new Double(1.67),
                                   new Double(1.83),
                                   new Double(2.33),
                                   new Double(1.00),
                                   CellHTS2.NA,
                                   new Double(1.63),
                                   new Double(1.75),
                                   new Double(1.88),
                                   new Double(2.38),
                                   new Double(1.00),
                                   CellHTS2.NA);
                Collections.addAll(eValuesRep1C2,
                                   new Double(0.50),
                                   new Double(0.75),
                                   new Double(0.63),
                                   new Double(0.75),
                                   new Double(1.00),
                                   new Double(0.88),
                                   new Double(0.78),
                                   new Double(0.89),
                                   new Double(0.83),
                                   new Double(0.89),
                                   new Double(1.00),
                                   new Double(0.94));
                Collections.addAll(eValuesRep2C2,
                                   new Double(0.6),
                                   new Double(0.8),
                                   new Double(0.7),
                                   new Double(0.8),
                                   new Double(1.0),
                                   new Double(0.9),
                                   new Double(0.8),
                                   new Double(0.9),
                                   new Double(0.85),
                                   new Double(0.9),
                                   new Double(1.0),
                                   new Double(0.95));
              }
              else {

              Collections.addAll(eValuesRep1,
                                 new Double(0.5),
                                 new Double(1),
                                 new Double(1.5),
                                 new Double(2),
                                 new Double(1), 
                                 CellHTS2.NA,
                                 new Double(1.25),
                                 new Double(1.50),
                                 new Double(1.75),
                                 new Double(2.25),
                                 new Double(1),
                                 CellHTS2.NA);
            }
          }
          }
          else // based on "N" {
          if (normalizePlatesScale == NormalizePlatesScale.ADDITIVE) {
            Collections.addAll(eValuesRep1,
                               new Double(-2),
                               new Double(-1),
                               new Double(0),
                               new Double(1),
                               new Double(-1),
                               CellHTS2.NA,
                               new Double(-2),
                               new Double(-1),
                               new Double(-0),
                               new Double(2),
                               new Double(-3),
                               CellHTS2.NA);
          }
          else {
            Collections.addAll(eValuesRep1,
                               new Double(0.33),
                               new Double(0.67),
                               new Double(1),
                               new Double(1.33),
                               new Double(0.67),
                               CellHTS2.NA,
                               new Double(0.71),
                               new Double(0.86),
                               new Double(1),
                               new Double(1.29),
                               new Double(0.57),
                               CellHTS2.NA);
          }
        }
        else { // not NEGATIVES
          if (normalizePlatesMethod.equals(NormalizePlatesMethod.LOESS)) {
            // test only for nrPlateColumns==3
            Collections.addAll(eValuesRep1,
                               new Double(1),
                               new Double(0.67),
                               new Double(1.01),
                               new Double(4),
                               new Double(0.00),
                               new Double(0.00),
                               new Double(7),
                               new Double(3.03),
                               new Double(3.37),
                               new Double(11),
                               new Double(0),
                               new Double(0));
  
            Collections.addAll(eValuesRep2,
                               new Double(9),
                               new Double(3.37),
                               new Double(3.70),
                               new Double(14),
                               new Double(0.00),
                               new Double(0.00),
                               new Double(15),
                               new Double(6.39),
                               new Double(7.07),
                               new Double(23),
                               new Double(0.00),
                               new Double(0.00));
          }
          else { // NOT LOESS, THAN MEDIAN OR MEAN
            if (normalizePlatesScale == NormalizePlatesScale.ADDITIVE) {
              if (nrPlateColumns == 2) {
                // expected values
                Collections.addAll(eValuesRep1,
                                   new Double(-2),
                                   new Double(-1),
                                   new Double(0),
                                   new Double(1),
                                   new Double(-2.5),
                                   new Double(-1.5),
                                   new Double(-0.5),
                                   new Double(1.5));
  
                Collections.addAll(eValuesRep2,
                                   new Double(-3),
                                   new Double(-2),
                                   new Double(-1),
                                   new Double(2),
                                   new Double(-3.5),
                                   new Double(-2.5),
                                   new Double(-1.5),
                                   new Double(2.5));
              }
              else if (nrPlateColumns == 3) {
                // conf per plate per row: control sample sample sample
                // p1r1: 1,2,3,4,5,6 => median(2,3,5,6) = 4 (which is also the
                // mean)
                // p2r1: 7,9,10,11,15,13 => median(9,10,13,15) = 11.5 (<> mean =
                // 11.75 )
  
                // p1r1
                Collections.addAll(eValuesRep1,
                                   new Double(-3),
                                   new Double(-2),
                                   new Double(-1),
                                   new Double(0),
                                   new Double(1),
                                   new Double(2));
                // p2r1
                Collections.addAll(eValuesRep1,
                                   new Double(-4.5),
                                   new Double(-2.5),
                                   new Double(-1.5),
                                   new Double(-0.5),
                                   new Double(3.5),
                                   new Double(1.5));
              }
            }
            else {// NormalizePlatesScale.MULITPLICATIVE, only tested for
              // nrPlateColumns=3 and median
              {
                // conf per plate per row: control sample sample sample
                // p1r1: 1,2,3,4,5,6 => median(2,3,5,6) = 4 (which is also the
                // mean)
                // p2r1: 7,9,10,11,15,13 => median(9,10,13,15) = 11.5 (<> mean =
                // 11.75 )
  
                // p1r1
                Collections.addAll(eValuesRep1,
                                   new Double(0.25),
                                   new Double(0.50),
                                   new Double(0.75),
                                   new Double(1.00),
                                   new Double(1.25),
                                   new Double(1.50));
                // p2r1
                // Collections.addAll(eValuesRep1,new Double(7 / 11.5),new
                // Double(9 / 11.5),new Double(10 / 11.5),new Double(11 /
                // 11.5),new Double(15 / 11.5),new Double(13 / 11.5));
              }
  
            }
          }
        }
    }
      // compare the values of the two newly added normalized replicates (type:
      // DataColumn)
      // check DataColumn cellhts2_norm_rep1 and cellhts2_norm_rep2 are
      // present, with values
      List<ResultValue> aValuesRep1 = null;
      List<ResultValue> aValuesRep2 = null;
      List<ResultValue> aValuesRep1C2 = null;
      List<ResultValue> aValuesRep2C2 = null;
      for (DataColumn dataColumn : screenResult.getDataColumnsList()) {
        if (dataColumn.getName()
               .length() >= 13) {
          String substr = dataColumn.getName()
                             .substring(0, 13);
          if (substr.equals("cellhts2_norm")) {
              if (dataColumn.getReplicateOrdinal() == 1) {
                if (multiChannel && (dataColumn.getChannel()!=null) && dataColumn.getChannel().intValue() == 2) {
                    aValuesRep1C2 = new ArrayList<ResultValue>(dataColumn.getResultValues());
                }
                else {
                  aValuesRep1 = new ArrayList<ResultValue>(dataColumn.getResultValues());
                }
              }
              else if (dataColumn.getReplicateOrdinal() == 2) {
                if (multiChannel && (dataColumn.getChannel()!=null) && dataColumn.getChannel().intValue() == 2) {
                  aValuesRep2C2 = new ArrayList<ResultValue>(dataColumn.getResultValues());
                }
                else {
                  aValuesRep2 = new ArrayList<ResultValue>(dataColumn.getResultValues());
                }
              }
            }

        }
      }

      if (aValuesRep1 == null || aValuesRep2 == null) {
        assertTrue("aValuesRep1 or aValuesRep2 are null", false);
      }
      else { // compare the individual values
        for (int i = 0; i < eValuesRep1.size(); i++) {
          Double aValueRep1 = new Double(aValuesRep1.get(i)
                                                         .getNumericValue()).doubleValue();
          assertEquals("Rep1 C1, index=" + i,eValuesRep1.get(i), round(aValueRep1,2));
          // System.out.println(aValueRep1);

        }
        for (int i = 0; i < eValuesRep2.size(); i++) { //
          Double aValueRep2 = new Double(aValuesRep2.get(i)
                                                         .getNumericValue()).doubleValue();
          assertEquals("Rep2 C1, index=" + i,eValuesRep2.get(i), round(aValueRep2,2));
          // System.out.println(aValueRep2);
        }
        
        if (multiChannel) {
          for (int i = 0; i < eValuesRep1C2.size(); i++) {
            Double aValueRep1C2 = new Double(aValuesRep1C2.get(i)
                                                      .getNumericValue()).doubleValue();
            assertEquals("Rep1 C2, index=" + i ,eValuesRep1C2.get(i), round(aValueRep1C2,2));
          }
            
          for (int i = 0; i < eValuesRep2C2.size(); i++) {
            Double aValueRep2C2 = new Double(aValuesRep2C2.get(i)
                                                      .getNumericValue()).doubleValue();
            assertEquals("Rep2 C2, index=" + i,eValuesRep2C2.get(i), round(aValueRep2C2,2));
          }
        }
      }
    }
    finally {
      cellHts2.closeConnection();
    }
  }

  public void testScoreReplicates()
    throws RserveException,
    REngineException,
    REXPMismatchException
  {
    // 1. PREPARE INPUT
    ScreenResult screenResult = MakeDummyEntitiesCellHTS2.makeSimpleDummyScreenResult();
    String title = "Dummy_Experiment";

    CellHTS2 cellHts2 = null;
    try {
      cellHts2 = new CellHTS2(screenResult, title, ".", null);

      // 2. RUN METHOD
      cellHts2.normalizePlatesInit(NormalizePlatesMethod.MEAN,
                                   NormalizePlatesScale.ADDITIVE,
                                   NormalizePlatesNegControls.NEG);
      cellHts2.scoreReplicatesInit(ScoreReplicatesMethod.ZSCORE);
      cellHts2.run();
      cellHts2.scoreReplicatesAddResult();

      // 3. ASSERTEQUALS
      // // ## CREATE DATA TARGET
      // // ## In the assaydata slot, the data is now three in stead of 4
      // dimensional as the plates are now below each other
      // // ## De dimensions are now: "Features","Sample", "Channels" . Sample
      // can also be read as "Replicate". See fData below for the details
      // //
      // TODO make nrWells etc. final instance variables, and initiate them in
      // constructor.

      // prepare expected values per replicate and per wel
      double[] expectedValuesRep1 = { -1.0791852, -0.5395926, 0, 0.5395926, -1.3489815, -0.8093889, -0.2697963, 0.8093889 };
      double[] expectedValuesRep2 = { -0.899321, -0.5995473, -0.2997737, 0.5995473, -1.0492078, -0.7494342, -0.4496605, 0.7494342 };

      // compare the values of the two newly added scored replicates (type:
      // DataColumn)
      // check dataColumn cellhts2_scored_rep1 and cellhts2_norm_rep2 are
      // present, with values
      List<ResultValue> actualValuesRep1 = null;
      List<ResultValue> actualValuesRep2 = null;
      for (DataColumn col : screenResult.getDataColumnsList()) {
        if (col.getName()
               .length() >= 13) {
          String substr = col.getName()
                             .substring(0, 15);
          if (substr.equals("cellhts2_scored")) {
            if (col.getReplicateOrdinal() == 1) {
              actualValuesRep1 = new ArrayList<ResultValue>(col.getResultValues());
            }
            else if (col.getReplicateOrdinal() == 2) {
              actualValuesRep2 = new ArrayList<ResultValue>(col.getResultValues());
            }
          }
        }
      }

      // BigDecimal bd = new BigDecimal(_input);
      // BigDecimal bd_round = bd.setScale( 2, BigDecimal.ROUND_HALF_UP );
      // return bd_round.doubleValue();
      if (actualValuesRep1 == null || actualValuesRep2 == null) {
        assertTrue("actualValuesRep1 or actualValuesRep2 are null", false);
      }
      else { // compare the individual values
        for (int i = 0; i < expectedValuesRep1.length; i++) {
          // round actual value 7 decimals
          double actValue = new BigDecimal(actualValuesRep1.get(i)
                                                           .getNumericValue()).setScale(7,
                                                                                        BigDecimal.ROUND_HALF_UP)
                                                                              .doubleValue();
          assertEquals("", expectedValuesRep1[i], actValue);

        }
        for (int i = 0; i < expectedValuesRep2.length; i++) {
          double actValue = new BigDecimal(actualValuesRep2.get(i)
                                                           .getNumericValue()).setScale(7,
                                                                                        BigDecimal.ROUND_HALF_UP)
                                                                              .doubleValue();
          assertEquals(expectedValuesRep2[i], actValue);
        }
      }
    }
    finally {
      cellHts2.closeConnection();
    }
  }

  public void testScoreReplicatesMultiChannel()
  throws RserveException,
  REngineException,
  REXPMismatchException
{
  // 1. PREPARE INPUT
    
    ScreenResult screenResult = MakeDummyEntitiesCellHTS2.makeSimpleDummyScreenResult(false,
                                                                                      false,
                                                                                      2,
                                                                                      false,
                                                                                      true,
                                                                                      true);  

  String title = "Dummy_Experiment";

  CellHTS2 cellHts2 = null;
  try {
      cellHts2 = new CellHTS2(screenResult, title, ".", null);

    // 2. RUN METHOD
    cellHts2.normalizePlatesInit(NormalizePlatesMethod.NEGATIVES,
                                 NormalizePlatesScale.MULTIPLICATIVE,
                                 NormalizePlatesNegControls.NEG_SHARED);
    cellHts2.scoreReplicatesInit(ScoreReplicatesMethod.ZSCORE);
    
    cellHts2.run();
    cellHts2.normalizePlatesAddResult();
    cellHts2.scoreReplicatesAddResult();

    // 3. ASSERTEQUALS
    // // ## CREATE DATA TARGET
    // // ## In the assaydata slot, the data is now three in stead of 4
    // dimensional as the plates are now below each other
    // // ## De dimensions are now: "Features","Sample", "Channels" . Sample
    // can also be read as "Replicate". See fData below for the details
    // //
    // TODO make nrWells etc. final instance variables, and initiate them in
    // constructor.

    // prepare expected values per replicate and per wel
    double[] eValuesR1c1 = {-2.2483025,-1.3489815,-0.4496605,0.4496605,-1.3489815,CellHTS2.NA,-0.8993210,-0.4496605,0.0000000,0.8993210,-1.3489815,CellHTS2.NA};
    double[] eValuesR1c2 = {-7.4193984,-2.5630649,-4.9912316,-2.5630649,2.2932686,-0.1348982,-2.0234723,0.1348982,-0.9442871,0.1348982,2.2932686,1.2140834};
    double[] eValuesR2c1 = {-1.1691173,-0.8093889,-0.4496605,0.6295247,-2.2483025,CellHTS2.NA,-0.8993210,-0.6295247,-0.3597284,0.7194568,-2.2483025,CellHTS2.NA};
    double[] eValuesR2c2 = {-8.0938891,-2.6979630,-5.3959261,-2.6979630,2.6979630,0.0000000,-2.6979630,0.0000000,-1.3489815,0.0000000,2.6979630,1.3489815};

    // compare the values of the newly added scored replicates (type:
    // ResultValueType)
    // check resultValueType cellhts2_scored_R1 and cellhts2_norm_R2 are
    // present, with values
      for (DataColumn dataColumn : screenResult.getDataColumnsList()) {
      if (dataColumn.getName()
             .length() >= 13) {
        String substr = dataColumn.getName()
                           .substring(0, 15);
        if (substr.equals("cellhts2_scored")) {
          int c =1;
          if  (dataColumn.getChannel() !=null) {
            c = dataColumn.getChannel().intValue();
          }
          
          int r=1;
          if  (dataColumn.getReplicateOrdinal() !=null) {
            r = dataColumn.getReplicateOrdinal().intValue();
          }
          
          double[] eValues =null;
          if (r==1){
            if (c==1){
              eValues = eValuesR1c1;
            }
            else {
              eValues = eValuesR1c2;
            }
          }
          else 
            if (c==1){
              eValues = eValuesR2c1;
            }
            else {
              eValues = eValuesR2c2;
            }
            
            // compare the individual values
          int i =-1;
          for (ResultValue rv : dataColumn.getResultValues()) { 
            i++;
            //actual value
            Double d = rv.getNumericValue();
            double aValue;
            if (new Double(d).equals(CellHTS2.NA)) {
              aValue = CellHTS2.NA;
            }else {
              aValue = new BigDecimal(d).setScale(7,BigDecimal.ROUND_HALF_DOWN).doubleValue(); 
            }
            assertEquals("", eValues[i], aValue);
          }

        }
      }
    }


  }
  finally {
    cellHts2.closeConnection();
  }
}

  public void testSummarizeReplicates()
    throws RserveException,
    REngineException,
    REXPMismatchException
  {

    // 1. PREPARE INPUT
    ScreenResult screenResult = MakeDummyEntitiesCellHTS2.makeSimpleDummyScreenResult();
    String title = "Dummy_Experiment";

    CellHTS2 cellHts2 = null;
    try {
      cellHts2 = new CellHTS2(screenResult, title, ".", null);

      // 2. RUN
      cellHts2.normalizePlatesInit(NormalizePlatesMethod.MEAN,
                                   NormalizePlatesScale.ADDITIVE,
                                   NormalizePlatesNegControls.NEG);
      cellHts2.scoreReplicatesInit(ScoreReplicatesMethod.ZSCORE);
      cellHts2.summarizeReplicatesInit(SummarizeReplicatesMethod.MEAN);
      cellHts2.run();
      cellHts2.summarizeReplicatesAddResult();

      // 3. CHECK EQUALS
      double[] expectedValues = { -0.98925310, -0.56957000, -0.14988680, 0.56957000, -1.19909470, -0.77941150, -0.35972840, 0.77941150 };

      // compare the values of the newly added summarized DataColumn
      List<ResultValue> actualValues = null;
      for (DataColumn col : screenResult.getDataColumnsList()) {
        if (col.getName()
               .equals("cellhts2_summarized")) {
          actualValues = new ArrayList<ResultValue>(col.getResultValues());
        }
      }

      // BigDecimal bd = new BigDecimal(_input);
      // BigDecimal bd_round = bd.setScale( 2, BigDecimal.ROUND_HALF_UP );
      // return bd_round.doubleValue();
      if (actualValues == null) {
        assertTrue("actualValues or actualValuesRep2 are null", false);
      }
      else { // compare the individual values
        for (int i = 0; i < expectedValues.length; i++) {
          // round actual value 7 decimals
          double actValue = new BigDecimal(actualValues.get(i)
                                                       .getNumericValue()).setScale(7,
                                                                                    BigDecimal.ROUND_HALF_UP)
                                                                          .doubleValue();
          assertEquals(expectedValues[i], actValue);
        }
      }

    }
    finally {
      cellHts2.closeConnection();
    }

  }

  public void testWriteReport()
    throws RserveException,
    REngineException,
    REXPMismatchException
  {
    // 1. PREPARE INPUT
    ScreenResult screenResult = MakeDummyEntitiesCellHTS2.makeSimpleDummyScreenResult(false,
                                                                                      false,
                                                                                      3);
    String title = "Dummy_Experiment";
    String indexUrl = null;
    CellHTS2 cellHts2 = null;
    try {
      cellHts2 = new CellHTS2(screenResult, title, ".", null);
      cellHts2.normalizePlatesInit(NormalizePlatesMethod.MEAN,
                                   NormalizePlatesScale.ADDITIVE,
                                   NormalizePlatesNegControls.NEG);
      cellHts2.scoreReplicatesInit(ScoreReplicatesMethod.ZSCORE);
      cellHts2.summarizeReplicatesInit(SummarizeReplicatesMethod.MEAN);
      cellHts2.writeReportInit("/tmp/screensaver/output");
      cellHts2.run();
      indexUrl = cellHts2.getWriteReportIndexUrl();

    }
    finally {
      cellHts2.closeConnection();
    }

    // 3. check equals
  "/tmp/screensaver/output/index.html".equals(indexUrl);

  // TODO check for presence of all the report files and a timeStamp after
  // starting the testWriteReport

}
  
  public void testWriteReportMultiChannel()
  throws RserveException,
  REngineException,
  REXPMismatchException
   
{
    ScreenResult screenResult = MakeDummyEntitiesCellHTS2.makeSimpleDummyScreenResult(false,
                                                                                      false,
                                                                                      2,
                                                                                      false,
                                                                                      true,
                                                                                      true);  

  String title = "Dummy_Experiment";
  String indexUrl = null;
  CellHTS2 cellHts2 = null;
  try {
      cellHts2 = new CellHTS2(screenResult, title, ".", null);

    
    cellHts2.normalizePlatesInit(NormalizePlatesMethod.NEGATIVES,
                                 NormalizePlatesScale.MULTIPLICATIVE,
                                 NormalizePlatesNegControls.NEG_SHARED);
    cellHts2.scoreReplicatesInit(ScoreReplicatesMethod.ZSCORE);
    cellHts2.summarizeReplicatesInit(SummarizeReplicatesMethod.MEAN);   

    cellHts2.writeReportInit("/tmp/screensaver/output");
    cellHts2.run();
    indexUrl = cellHts2.getWriteReportIndexUrl();

  }
  finally {
    cellHts2.closeConnection();
  }

  // 3. check equals
  "/tmp/screensaver/output/index.html".equals(indexUrl);

  // TODO check for presence of all the report files and a timeStamp after
  // starting the testWriteReport

}
  
  /*
   * Test if the constant R results in a NA value in R
   */

  public void testNA()
    throws RserveException,
    REngineException,
    REXPMismatchException
  {
    RConnection rConnection = new RConnection();
    RserveExtensions rserveExtensions = new RserveExtensions();

    // I Checking CellHTS2.NA value transferred to R is seen in R as a NA value
    double[] x = new double[2];
    /*
     * According to Simon Urbanek one can use R's NA constant right away, which
     * can be created using Double.longBitsToDouble(0x7ff00000000007a2L)) notice
     * that this is slightly different from the Double.NaN value which equals
     * Double.longBitsToDouble(0x7ff8000000000000L) nevertheless
     * assertEquals(Double.longBitsToDouble(0x7ff00000000007a2L),
     * Double.longBitsToDouble(0x7ff8000000000000L)) returns TRUE. Simon
     * indicated one has to use the most recent Rserve (he uploaded the same
     * day), however the code also works with older versions. Apparently it ..
     * works than via the NA
     */

    x[0] = CellHTS2.NA;
    x[1] = 3;

    rConnection.assign("x", x);
    // Check the value is in the R environment actual an NA
    double isna = rserveExtensions.tryEval(rConnection,
                                           "if (is.na(x[1]) )  1  else  0;")
                                  .asDouble();
    assertEquals(isna, 1.0);
    // Check the the second value is not seen as an NA
    double isna2 = rserveExtensions.tryEval(rConnection,
                                            "if (is.na(x[2]) )  1  else  0;")
                                   .asDouble();
    assertEquals(isna2, 0.0);

    // II Checking NA data in R can be retrieved as NA values in Java
    // in R arrays start with index 1 (unlike Java which start with 0)
    double x0Res = rserveExtensions.tryEval(rConnection, "x[1]")
                                   .asDouble();
    assertEquals(x0Res, CellHTS2.NA);

    double x1Res = rserveExtensions.tryEval(rConnection, "x[2]")
                                   .asDouble();
    assertEquals(x1Res, 3.0);

  }

}
