// $HeadURL:
// svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml
// $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.analysis.cellhts2;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;

public class CellHTS2Test extends AbstractSpringTest
{
  // static members

  private static Logger log = Logger.getLogger(CellHTS2.class);

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

    cellHts2.getArrayDimensions()
            .equals(expected);

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
    REXPMismatchException
  {

    // 1. PREPARE INPUT
    ScreenResult screenResult = MakeDummyEntitiesCellHTS2.makeSimpleDummyScreenResult(false,
                                                                                      withNull,
                                                                                      2);
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
      String[] expectedResultConf = { "pos", "sample", "neg", "sample", "pos", "sample", "neg", "sample" };

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
    this.normalizePlatesAssert(NormalizePlatesMethod.MEAN, 2);
  }

  public void testNormalizePlatesMedian()
    throws RserveException,
    REngineException,
    REXPMismatchException
  {
    this.normalizePlatesAssert(NormalizePlatesMethod.MEDIAN, 3);
  }


  public void normalizePlatesAssert(NormalizePlatesMethod normalizePlatesMethod,
                                    int nrPlateColumns)
    throws RserveException,
    REngineException,
    REXPMismatchException
  {

    ScreenResult screenResult = MakeDummyEntitiesCellHTS2.makeSimpleDummyScreenResult(false,
                                                                                      false,
                                                                                      nrPlateColumns);
    String title = "Dummy_Experiment";

    CellHTS2 cellHts2 = null;
    try {
      cellHts2 = new CellHTS2(screenResult, title);
      cellHts2.normalizePlatesInit(normalizePlatesMethod);
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
      if (nrPlateColumns == 2) {
        // expected values
        eValuesRep1.add(new Double(-2));
        eValuesRep1.add(new Double(-1));
        eValuesRep1.add(new Double(0));
        eValuesRep1.add(new Double(1));
        eValuesRep1.add(new Double(-2.5));
        eValuesRep1.add(new Double(-1.5));
        eValuesRep1.add(new Double(-0.5));
        eValuesRep1.add(new Double(1.5));

        eValuesRep2.add(new Double(-3));
        eValuesRep2.add(new Double(-2));
        eValuesRep2.add(new Double(-1));
        eValuesRep2.add(new Double(2));
        eValuesRep2.add(new Double(-3.5));
        eValuesRep2.add(new Double(-2.5));
        eValuesRep2.add(new Double(-1.5));
        eValuesRep2.add(new Double(2.5));

      }
      else if (nrPlateColumns == 3) {
        // conf per plate per row: control sample sample sample
        // p1r1: 1,2,3,4,5,6 => median(2,3,5,6) = 4 (with is also the mean)
        // p2r1: 7,9,10,11,15,13 => median(9,10,13,15) = 11.5 (<> mean = 11.75 )
        //p1r1
        eValuesRep1.add(new Double(-3));
        eValuesRep1.add(new Double(-2));
        eValuesRep1.add(new Double(-1));
        eValuesRep1.add(new Double(0));
        eValuesRep1.add(new Double(1));
        eValuesRep1.add(new Double(2));
        
        //p2r1
        eValuesRep1.add(new Double(-4.5));
        eValuesRep1.add(new Double(-2.5));
        eValuesRep1.add(new Double(-1.5));
        eValuesRep1.add(new Double(-0.5));
        eValuesRep1.add(new Double(3.5));
        eValuesRep1.add(new Double(1.5));


      }
      // compare the values of the two newly added normalized replicates (type:
      // ResultValueType)
      // check resultValueType cellhts2_norm_repq and cellhts2_norm_rep2 are
      // present, with values
      List<ResultValue> actualValuesRep1 = null;
      List<ResultValue> actualValuesRep2 = null;
      for (ResultValueType rvt : screenResult.getResultValueTypesList()) {
        if (rvt.getName()
               .length() >= 13) {
          String substr = rvt.getName()
                             .substring(0, 13);
          if (substr.equals("cellhts2_norm")) {
            if (rvt.getReplicateOrdinal() == 1) {
              actualValuesRep1 = new ArrayList<ResultValue>(rvt.getResultValues());
            }
            else if (rvt.getReplicateOrdinal() == 2) {
              actualValuesRep2 = new ArrayList<ResultValue>(rvt.getResultValues());
            }
          }
        }
      }

      if (actualValuesRep1 == null || actualValuesRep2 == null) {
        assertTrue("actualValuesRep1 or actualValuesRep2 are null", false);
      }
      else { // compare the individual values
        for (int i = 0; i < eValuesRep1.size(); i++) {
          assertEquals(eValuesRep1.get(i),
                       new Double(actualValuesRep1.get(i)
                                                  .getNumericValue()).doubleValue());
        }
        for (int i = 0; i < eValuesRep2.size(); i++) {
          assertEquals(eValuesRep2.get(i),
                       new Double(actualValuesRep2.get(i)
                                                  .getNumericValue()).doubleValue());
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
      cellHts2.normalizePlatesInit(NormalizePlatesMethod.MEAN);
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
      // ResultValueType)
      // check resultValueType cellhts2_scored_rep1 and cellhts2_norm_rep2 are
      // present, with values
      List<ResultValue> actualValuesRep1 = null;
      List<ResultValue> actualValuesRep2 = null;
      for (ResultValueType rvt : screenResult.getResultValueTypesList()) {
        if (rvt.getName()
               .length() >= 13) {
          String substr = rvt.getName()
                             .substring(0, 15);
          if (substr.equals("cellhts2_scored")) {
            if (rvt.getReplicateOrdinal() == 1) {
              actualValuesRep1 = new ArrayList<ResultValue>(rvt.getResultValues());
            }
            else if (rvt.getReplicateOrdinal() == 2) {
              actualValuesRep2 = new ArrayList<ResultValue>(rvt.getResultValues());
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
      cellHts2.normalizePlatesInit(NormalizePlatesMethod.MEAN);
      cellHts2.scoreReplicatesInit(ScoreReplicatesMethod.ZSCORE);
      cellHts2.summarizeReplicatesInit(SummarizeReplicatesMethod.MEAN);
      cellHts2.run();
      cellHts2.summarizeReplicatesAddResult();

      // 3. CHECK EQUALS
      double[] expectedValues = { -0.98925310, -0.56957000, -0.14988680, 0.56957000, -1.19909470, -0.77941150, -0.35972840, 0.77941150 };

      // compare the values of the newly added summarized ResultValueType
      List<ResultValue> actualValues = null;
      for (ResultValueType rvt : screenResult.getResultValueTypesList()) {
        if (rvt.getName()
               .equals("cellhts2_summarized")) {
          actualValues = new ArrayList<ResultValue>(rvt.getResultValues());
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
    ScreenResult screenResult = MakeDummyEntitiesCellHTS2.makeSimpleDummyScreenResult();
    String title = "Dummy_Experiment";
    String indexUrl = null;
    CellHTS2 cellHts2 = null;
    try {
      cellHts2 = new CellHTS2(screenResult, title);
      cellHts2.normalizePlatesInit(NormalizePlatesMethod.MEAN);
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
