// $HeadURL:
// svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml
// $
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.analysis.cellhts2;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;

public class CellHTS2Test extends AbstractSpringTest
{
  public void testCalculateArrayDimensions()
    throws RserveException,
    REngineException,
    REXPMismatchException
  {

    ScreenResult screenResult = MakeDummyEntitiesCellHTS2.makeSimpleDummyScreenResult();
    String title = "Dummy_Experiment";
    CellHTS2 cellHts2 = new CellHTS2(screenResult, title);

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
    tReadPlateListDb(false);
  }

  public void testReadPlateListDbWithNull()
    throws RserveException,
    REngineException,
    REXPMismatchException
  {
    tReadPlateListDb(true);
  }

  public void tReadPlateListDb(boolean withNull)
  throws RserveException,
  REngineException,
  REXPMismatchException {
    tReadPlateListDb(true,false);
  }

  public void testReadPlateListDbWithoutA01ResultValue()
    throws RserveException,
    REngineException,
    REXPMismatchException
  {
    tReadPlateListDb(false,true);
  }
  
  
  
  public void tReadPlateListDb(boolean withNull,boolean withoutA01ResultValue)
    throws RserveException,
    REngineException,
    REXPMismatchException
  {

    // 1. PREPARE INPUT
    ScreenResult screenResult = MakeDummyEntitiesCellHTS2.makeSimpleDummyScreenResult(false,
                                                                                      withNull,
                                                                                      2,
                                                                                      withoutA01ResultValue);
    String title = "Dummy_Experiment";

    CellHTS2 cellHts2 = null;
    try {
      cellHts2 = new CellHTS2(screenResult, title);

      // 2. RUN METHOD
      cellHts2.readPlateListDbInit();
      cellHts2.run();
      double[][] result = cellHts2.getReadPlateListDbResult();

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
      // int nrChannels = arrayDimensions.getNrChannels();

      double[][] expected = new double[nrReps][(nrWells * nrPlates)];

      double[] Rep1Values = { 1, 2, 3, 4, 5, 6, 7, 9 };
      double[] Rep2Values = { 9, 10, 11, 14, 13, 14, 15, 19 };
      if (withNull) {
        Rep1Values[2] = CellHTS2.NA;
        Rep2Values[2] = CellHTS2.NA;
      }
      
      if (withoutA01ResultValue) {
        Rep1Values[0] = CellHTS2.NA;
        Rep1Values[4] = CellHTS2.NA;
        Rep2Values[0] = CellHTS2.NA;
        Rep2Values[4] = CellHTS2.NA;
      }

      expected[0] = Rep1Values;
      expected[1] = Rep2Values;

      // JUnit cannot compare multidimensional double arrays at once.
      // It will then just compare the reference address
      for (int r = 0; r < 2; r++) {
        for (int i = 0; i < 8; i++) {
          assertEquals(expected[r][i], result[r][i]);
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
      cellHts2 = new CellHTS2(screenResult, title);

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
      String[] expectedResultSlog = { "1.0", "1.0" }; // values in column Plate
      // .
      // TODO other check for the entries in the other columns ic.
      // Well,Sample,Channel,Flag.
      String[] actualResultSlog = cellHts2.getConfigureDbResultSlog();
      // assertEquals cannot compare String[] straight away. It will compare the
      // memory address
      for (int i = 0; i < expectedResultSlog.length; i++) {
        assertEquals(expectedResultSlog[i], actualResultSlog[i]);
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
      cellHts2 = new CellHTS2(screenResult, title);

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

  public void testNormalizePlatesMedianMultiplicative()
    throws RserveException,
    REngineException,
    REXPMismatchException
  {
    this.normalizePlatesAssert(NormalizePlatesMethod.MEDIAN,
                               NormalizePlatesScale.MULTIPLICATIVE,
                               3);
  }

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

  public void testNormalizePlatesNpiNeg()
  throws RserveException,
  REngineException,
  REXPMismatchException
{
  this.normalizePlatesAssert(NormalizePlatesMethod.NPI,
                             NormalizePlatesScale.ADDITIVE, // not relevant for NPI
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


    ScreenResult screenResult = MakeDummyEntitiesCellHTS2.makeSimpleDummyScreenResult(false,
                                                                                      false,
                                                                                      nrPlateColumns,
                                                                                      false,
                                                                                      inclNS);
    String title = "Dummy_Experiment";

    CellHTS2 cellHts2 = null;
    try {
      cellHts2 = new CellHTS2(screenResult, title);
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
 
      
      if (normalizePlatesMethod.equals(NormalizePlatesMethod.NPI)) {
        // NPI (x - mean(P)/(mean(P)- mean(N)) 
/*        ##        (mean(P) -x)  / (mean(P) - mean(N)
          ## P1R1
          ## A01 P 1  (1-1)/ (1-3) = 0.0   
          ## A02 X 2  (1-2)/ (1-3) = 0.5
          ## B01 N 3  (1-3)/ (1-3) = 1.0
          ## B02 X 4  (1-4)/ (1-3) = 1.5 
          
          ## P2R1
          ## A01 P 5  (5-5)/ (5-7) = 0.0   
          ## A02 X 6  (5-6)/ (5-7) = 0.5
          ## B01 N 7  (5-7)/ (5-7) = 1.0
          ## B02 X 9  (5-9)/ (5-7) = 2.0  

          ##P1R2 
          ## A01 P 9  (9-9)/ (9-11)= 0.0
          ## A02 X 10 (9-10)/ (9-11)= 0.5
          ## B01 N 11 (9-11)/ (9-11)= 1
          ## B02 X 14 (9-14)/ (9-11)= 2.5
          
          ##P2R2 
          ## A01 P 13 (13-13)/ (13-15)= 0.0
          ## A02 X 14 (13-14)/ (13-15)= 0.5
          ## B01 N 15 (13-15)/ (13-15)= 1.0
          ## B02 X 19 (13-19)/ (13-15)= 3.0*/
          
        Collections.addAll(eValuesRep1,
                           new Double(-0.0),  //TODO check why it has to be -.0.0
                           new Double(0.5),  
                           new Double(1.0),    
                           new Double(1.5),  
                           new Double(-0.0),   
                           new Double(0.5),  
                           new Double(1.0),    
                           new Double(2.0));   
          
          Collections.addAll(eValuesRep2,
                             new Double(-0.0),  
                             new Double(0.5),  
                             new Double(1.0),    
                             new Double(2.5),  
                             new Double(-0.0),   
                             new Double(0.5),  
                             new Double(1.0),    
                             new Double(3.0));             
        
      }
      else {
        if (normalizePlatesMethod.equals(NormalizePlatesMethod.NEGATIVES)) {
  /*
   * Negative controls 1 well: B01: voor rep1 plate 1: 3 voor rep1 plate 2: 7 voor
   * rep2 plate 1: 11 voor rep2 plate 2: 15 NC plate 1: 2 original values for rep1
   * (2 columns) : 1,2,3,4,5,6,7,9
   */
          if (normalizePlatesNegControls == NormalizePlatesNegControls.NEG_SHARED) {
            /* plate : 2 cols,3 rows
             * R1P1: AO1 1 , A02 2 , B01 3 , B02 4 , C01 2 , C02 null
             * R1P2: AO1 5 , A02 6 , B01 7 , B02 9 , C01 4 , C02 null
             * BO1: (normal) negative control, ic.  P1: 3, P2: 7)
             * CO1: S (shared negative control, ic. P1: 2, P2: 4)
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
            else { // multiplicative
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
                               new Double(0.3333333333333333),
                               new Double(0.6666666666666666),
                               new Double(1),
                               new Double(1.3333333333333333),
                               new Double(0.6666666666666666),
                               CellHTS2.NA,
                               new Double(0.7142857142857143),
                               new Double(0.8571428571428571),
                               new Double(1),
                               new Double(1.2857142857142858),
                               new Double(0.5714285714285714), 
                               CellHTS2.NA
                              );
          }
        }
        else { // not NEGATIVES
          if (normalizePlatesMethod.equals(NormalizePlatesMethod.LOESS)) {
            // test only for nrPlateColumns==3
            Collections.addAll(eValuesRep1,
                               new Double(1),
                               new Double(0.6649677294952294),
                               new Double(0.9999512278450635),
                               new Double(4),
                               new Double(1.6799167589536141),
                               new Double(2.0149002573034487),
                               new Double(7),
                               new Double(3.004852950739628),
                               new Double(3.3448357162939013),
                               new Double(11),
                               new Double(5.034751009656404),
                               new Double(4.359784745752295));
  
            Collections.addAll(eValuesRep2,
                               new Double(9),
                               new Double(3.3448357162939013),
                               new Double(3.679819214643736),
                               new Double(14),
                               new Double(4.359784745752295),
                               new Double(4.69476824410213),
                               new Double(15),
                               new Double(6.353021511836488),
                               new Double(7.021322086134678),
                               new Double(23),
                               new Double(8.721235913906071),
                               new Double(9.727852831357055));
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
      List<ResultValue> actualValuesRep1 = null;
      List<ResultValue> actualValuesRep2 = null;
      for (DataColumn dataColumn : screenResult.getDataColumnsList()) {
        if (dataColumn.getName()
               .length() >= 13) {
          String substr = dataColumn.getName()
                             .substring(0, 13);
          if (substr.equals("cellhts2_norm")) {
            if (dataColumn.getReplicateOrdinal() == 1) {
              actualValuesRep1 = new ArrayList<ResultValue>(dataColumn.getResultValues());
            }
            else if (dataColumn.getReplicateOrdinal() == 2) {
              actualValuesRep2 = new ArrayList<ResultValue>(dataColumn.getResultValues());
            }
          }
        }
      }

      if (actualValuesRep1 == null || actualValuesRep2 == null) {
        assertTrue("actualValuesRep1 or actualValuesRep2 are null", false);
      }
      else { // compare the individual values
        for (int i = 0; i < eValuesRep1.size(); i++) {
          Double aValueRep1 = new Double(actualValuesRep1.get(i)
                                                         .getNumericValue()).doubleValue();
          assertEquals(eValuesRep1.get(i), aValueRep1);
          // System.out.println(aValueRep1);

        }
        for (int i = 0; i < eValuesRep2.size(); i++) { //
          Double aValueRep2 = new Double(actualValuesRep2.get(i)
                                                         .getNumericValue()).doubleValue();
          assertEquals(eValuesRep2.get(i), aValueRep2);
          // System.out.println(aValueRep2);
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
      cellHts2 = new CellHTS2(screenResult, title);

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
          assertEquals(expectedValuesRep1[i], actValue);

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
      cellHts2 = new CellHTS2(screenResult, title);

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
      cellHts2 = new CellHTS2(screenResult, title);
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
    assertEquals("/tmp/screensaver/output/index.html", indexUrl);

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
