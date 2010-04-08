// $HeadURL:
// svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml
// $
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.analysis.cellhts2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import edu.harvard.med.screensaver.ScreensaverProperties;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.AssayWell;
import edu.harvard.med.screensaver.model.screenresults.AssayWellControlType;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;

import org.apache.log4j.Logger;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;

/**
 * This package contains an API for calls to a number of methods of CellHTS2
 * written in R, including normalizePlates, scoreReplicates,
 * summarizeReplicates, and writeReport.
 * <p>
 * Initialization of each {@link RMethod R Methods} is separated from the
 * retrieval of its result (from R into the Java environment). This allows for
 * lazy and step-wise invocation of the various R Methods.
 * <p>
 * It is the responsibility of calling code to ensure that all required entity
 * relationships are accessible. This implies that methods in this class should
 * either be called within an active Hibernate session (e.g. by a
 * 
 * @Transactional method), or the ScreenResult has already had the necessary
 *                relationships eagerly fetched.
 * @author Cor Lieftink
 */

public class CellHTS2 {

  public static final String CELLHTS2_DATA_COLUMN_PREFIX = "cellhts2_";
  public static final double NA = Double.longBitsToDouble(0x7ff00000000007a2L);
  
  private static Logger log = Logger.getLogger(CellHTS2.class);
  private RConnection rConnection;
  private ArrayDimensions arrayDimensions;
  private ScreenResult screenResult;
  private BiMap<Integer, Integer> plateNumberToSequenceNumberMap;
  private RMethod lastRMethodCompleted = RMethod.START;
  private RMethod lastRMethodToRun = RMethod.START;
  private String title;
  private NormalizePlatesMethod normalizePlatesMethod;
  private NormalizePlatesScale normalizePlatesScale;
  private NormalizePlatesNegControls normalizePlatesNegControls;
  private ScoreReplicatesMethod scoreReplicatesMethod;
  private SummarizeReplicatesMethod summarizeReplicatesMethod;
  private String reportOutputPath;
  private String writeReportIndexUrl;


  public CellHTS2(ScreenResult screenResult, String title) throws RserveException, REngineException,
  REXPMismatchException {
    this.screenResult = screenResult;
    this.rConnection = new RConnection();
    this.arrayDimensions = calculateArrayDimensions(screenResult);
    this.title = title;
  }

  /**
   * Can be called repeatedly, assuming later rMethods are being initialized
   * since the last call (otherwise nothing new will happen).
   * 
   * @return the update screen result
   * @throws CellHtsException
   */
  public ScreenResult run() throws CellHtsException
  {
    try {
      runPendingRMethods();
      return screenResult;
    }
    catch (CellHtsException e) {
      throw e;
    }
    catch (Exception e) {
      throw new CellHtsException(e);
    }
  }

  private void runPendingRMethods() throws RserveException, REngineException, REXPMismatchException
  {
    if (isRMethodRunnableNow(RMethod.READ_PLATELIST)) {
      assert title != null : "constructor did not initialize title";
      readPlateListDb();
      lastRMethodCompleted = RMethod.READ_PLATELIST;
    }
    if (isRMethodRunnableNow(RMethod.CONFIGURE)) {
      configureDb();
      lastRMethodCompleted = RMethod.CONFIGURE;
    }
    if (isRMethodRunnableNow(RMethod.ANNOTATE)) {
      annotateDb();
      lastRMethodCompleted = RMethod.ANNOTATE;
    }
    if (isRMethodRunnableNow(RMethod.NORMALIZE_PLATES)) {
      if (normalizePlatesMethod == null) {
        throw new CellHtsException(RMethod.NORMALIZE_PLATES  + " rMethod not initialized");
      }
      runRMethod(RMethod.NORMALIZE_PLATES);
      lastRMethodCompleted = RMethod.NORMALIZE_PLATES;
    }
    if (isRMethodRunnableNow(RMethod.SCORE_REPLICATES)) {
      if (scoreReplicatesMethod == null) {
        throw new CellHtsException(RMethod.SCORE_REPLICATES + " rMethod not initialized");
      }
      runRMethod(RMethod.SCORE_REPLICATES);
      lastRMethodCompleted = RMethod.SCORE_REPLICATES;
    }
    if (isRMethodRunnableNow(RMethod.SUMMARIZE_REPLICATES)) {
      if (this.summarizeReplicatesMethod == null) {
        throw new CellHtsException(RMethod.SUMMARIZE_REPLICATES + " rMethod not initialized");
      }
      runRMethod(RMethod.SUMMARIZE_REPLICATES);
      lastRMethodCompleted = RMethod.SUMMARIZE_REPLICATES;
    }
    if (isRMethodRunnableNow(RMethod.WRITE_REPORT)) {
      if (reportOutputPath == null) {
        throw new CellHtsException(RMethod.WRITE_REPORT  + " rMethod not initialized");
      }
      writeReport();
      lastRMethodCompleted = RMethod.WRITE_REPORT;
    }
  }

  private boolean isRMethodRunnableNow(RMethod rMethod)
  {
    return lastRMethodCompleted.getIndex() == rMethod.getIndex() - 1 &&
    lastRMethodToRun.getIndex() >= rMethod.getIndex();
  }
  
  private boolean isRMethodCompleted(RMethod rMethod)
  {
    return rMethod.getIndex() <= lastRMethodCompleted.getIndex();  
  }
  

  private void setLastRMethodToRun(RMethod rMethod)
  {
    if (rMethod.getIndex() > lastRMethodCompleted.getIndex()) {
      lastRMethodToRun = rMethod;
    }
  }

  /**
   * Calculates array dimensions in terms of number of
   * <ul>
   * <li>rows per plate
   * <li>columns per plate
   * <li>wells
   * <li>plates
   * <li>replicates
   * <li>channels
   * </ul>
   * 
   * @param screenResult
   * @return ArrayDimensions type, containing all the above values
   */

  private ArrayDimensions calculateArrayDimensions(ScreenResult screenResult) {

    ArrayDimensions result = new ArrayDimensions();

    int nrRowsPlate = 0;
    int nrColsPlate = 0;
    int row;
    int col;
    WellKey wellKey;

    for (Well well : screenResult.getWells()) {
      wellKey = well.getWellKey();
      row = wellKey.getRow();
      if (row > nrRowsPlate) {
        nrRowsPlate = row;
      }
      col = wellKey.getColumn();
      if (col > nrColsPlate) {
        nrColsPlate = col;
      }
    }
    // row and col indexes start with 0
    nrRowsPlate += 1;
    nrColsPlate += 1;

    result.setNrRowsPlate(nrRowsPlate);
    result.setNrColsPlate(nrColsPlate);

    result.setNrWells(nrRowsPlate * nrColsPlate);
    result.setNrPlates(screenResult.getPlateNumberCount());
    result.setNrReps(screenResult.getReplicateCount());
    result.setNrChannels(1);

    return (result);
  }

  public ArrayDimensions getArrayDimensions() {
    return (this.arrayDimensions);
  }
  
  public void readPlateListDbInit()
  {
    setLastRMethodToRun(RMethod.READ_PLATELIST);
  }

  private void readPlateListDb() throws RserveException,
      REngineException, REXPMismatchException {

    // 1. PREPARE INPUT OBJECTS IN R
    int nrWells = arrayDimensions.getNrWells();
    int nrPlates = arrayDimensions.getNrPlates();
    int nrReps = arrayDimensions.getNrReps();
    int nrChannels = arrayDimensions.getNrChannels();

    // Building up the object xraw in R using data retrieved from database using
    // Hibernate via inputparameter ScreenResult.
    // put string values in separate strings. This strings can then directly
    // used for testing in R self
    String strInitXraw = "xraw = array(as.numeric(NA), dim=c(" + nrWells + ","
        + nrPlates + "," + nrReps + "," + nrChannels + "))";
    RserveExtensions rserveExtensions = new RserveExtensions();
    rserveExtensions.tryEval(rConnection, strInitXraw);

    // Assign values to xraw in R.
    String strAssignVar;

    // For now it is assumed that there are no different channels. Channel index
    // therefore will always be 1.
    int c = 0;

    this.plateNumberToSequenceNumberMap = createPlateNumberSequenceMapping(this.screenResult);

    Collection<ResultValue> resultValues;
    for (DataColumn col : this.screenResult.getRawNumericDataColumns()) {
      Integer r = col.getReplicateOrdinal();
      if (r == null) {
        r = 1;
      }
      resultValues = col.getResultValues();

      // For transfer the values for all the wells of a replicate to R at once,
      // create a double[][]
      // .. variable in Java representing the values per plate en per well.
      double[][] values = new double[nrPlates][nrWells];
      
     /* ScreenResult is allowed to have wells in the upper-left part of a plate that do
      not have ResultValues. */
      for (int p = 0; p < nrPlates; p++) {
        for (int w = 0; w < nrWells; w++) {
            values[p][w] = CellHTS2.NA;
        }
      }
      
      WellKey wellKey;
      for (ResultValue rv : resultValues) {
        wellKey = rv.getWell().getWellKey();
        assert this.plateNumberToSequenceNumberMap.containsKey(wellKey
            .getPlateNumber());
        int p = this.plateNumberToSequenceNumberMap.get(wellKey.getPlateNumber());

        int w = (wellKey.getRow() * arrayDimensions.getNrColsPlate())
            + (wellKey.getColumn());
        
        Double value = rv.getNumericValue();
        if (value == null) {
        	value = CellHTS2.NA;
        }
        values[p - 1][w] = value;
      }
      // w: well, p:plate, r: rep, c: channel

      // Transfer values to R object
      for (int p = 0; p < nrPlates; p++) {
        // indices in R starts with 1, therefore restore p to its original
        // value, ic. + 1

        // cannot assign straight to xraw. So first create a variable in R and
        // assign the value to it.
        rConnection.assign("plateValues", values[p]);

        // assign the the variable plateValues to the xraw
        strAssignVar = "xraw[," + (p + 1) + "," + r + "," + (c + 1)
            + "] <- plateValues";
        rserveExtensions.tryEval(rConnection, strAssignVar);
      }
    }

    // 2. RUN METHOD IN R
    String runReadPlateListDb = "r <- readPlateListDb(xraw," + "\"" + title
        + "\"" + "," + arrayDimensions.getNrRowsPlate() + ","
        + arrayDimensions.getNrColsPlate() + ")";

    String rExpr = "library(cellHTS2Db);"
        + runReadPlateListDb + ";\"OK\""; // Added OK, Otherwise returns the
                                          // variable r (==cellHTS2 object)
    // and there is apparantly a bug, as the result REXP does contain elements
    // count 16 and names count 31
    // .. based on the name it will retrieve an element and then
    // arrayIndexOutOfBoundError
    rserveExtensions.tryEval(rConnection, rExpr);
  }
  
  public double[][] getReadPlateListDbResult() throws RserveException,
    REngineException, REXPMismatchException {
  // Retrieve data is only relevant for running tests on partial results of
  // the cellHTS2 run, ic. de readPlateListDb.
  // Data(r) is three in stead of 4 dimensional as the plates are now below
  // each other
  // dim 1: plate + well
  // dim 2: repliate
  // dim 3: channel
  
  int nrWells = arrayDimensions.getNrWells();
  int nrPlates = arrayDimensions.getNrPlates();
  int nrReps = arrayDimensions.getNrReps();
  int nrChannels = arrayDimensions.getNrChannels();
  double[][] result = new double[nrReps][nrWells * nrPlates];
  RserveExtensions rserveExtensions = new RserveExtensions();
  
  // put data in R from dataframe into multidimensional array
  String strEval = "rData <- array(Data(r),c(" + (nrWells * nrPlates) + ","
      + nrReps + "))";
  rserveExtensions.tryEval(rConnection, strEval);
  
  // num [1:8, 1:2, 1] 1 3 4 5 ..
  
  // retrieve dataobject at once in stead of per replicate
  
  // retrieve data per replicate over all plates
  for (int r = 0; r < nrReps; r++) {
    strEval = "rData[," + (r + 1) + "]"; // indexes in R start with 1
    result[r] = rserveExtensions.tryEval(rConnection, strEval).asDoubles();
  }
  
  return (result);

}
  
  
  public void configureDbInit()
  {
    setLastRMethodToRun(RMethod.CONFIGURE);
  }
  

  /**
   * Makes a call to the R function configureDb
   */

  private String[] configureDb() throws RserveException, REngineException, REXPMismatchException {

    // 1. PREPARE INPUT OBJECTS IN R FOR R METHOD CONFIGURE
    // String[] plate : "1","1","1"
    // String[] well : "AO1", "A02", "AO3"

    // calculate length of arrays
    int l = arrayDimensions.getNrPlates() * arrayDimensions.getNrWells();
    String[] plates = new String[l];
    String[] wells = new String[l];
    String[] contents = new String[l];
    
    // Each replicate will be similar in content. Information is taken from the
    // first one. need to loop over resultvalues in stead over wells, because we
    // need
    // information about assayWellControlType.
    int i = -1;
    DataColumn col = screenResult.getDataColumns().first();
    for (ResultValue rv : col.getResultValues()) {
      i++;
      WellKey wellKey = rv.getWell().getWellKey();
      plates[i] = this.plateNumberToSequenceNumberMap.get(wellKey.getPlateNumber()).toString();
      wells[i] = wellKey.getWellName();

      // cellHTS2 requires at least the exact annotation "sample", otherwise
      // f.e. normalizePlates will generate error.
      // TODO add the preserved words in cellHTS2 (if exists) for E, C, B, DMSO,
      // O
      if (rv.getAssayWellControlType() == AssayWellControlType.ASSAY_POSITIVE_CONTROL) {
        contents[i] = "pos";
      } else if (rv.getAssayWellControlType() == AssayWellControlType.ASSAY_CONTROL) {
        contents[i] = "N";
      } else if (rv.getAssayWellControlType() == AssayWellControlType.ASSAY_CONTROL_SHARED) {
        contents[i] = "S";
      } else if (rv.getWell().getLibraryWellType() == LibraryWellType.EXPERIMENTAL) {
        contents[i] = "sample";
      }
      // TODO: should use library controls too
      // TODO: how to handle AssayWellControlType.OTHER?

    }

    // run first readPlateList
    rConnection.assign("plate", plates);
    rConnection.assign("well", wells);
    rConnection.assign("content", contents);

    // 2. RUN R-METHOD CONFIGURE IN R
    RserveExtensions rserveExtensions = new RserveExtensions();
    StringBuffer rExpr = new StringBuffer();
    rExpr.append("library(cellHTS2Db);");
    rExpr.append("conf <- data.frame(Plate=plate, Well=well, Content=content,stringsAsFactors=FALSE);");
    
    // CREATE SLOG
    // CellHTS2 requires the Plate, Sample, Channel as numeric fields
    ArrayList<Integer> slogP = new ArrayList<Integer>();
    ArrayList<String> slogW = new ArrayList<String>();
    ArrayList<Integer> slogS = new ArrayList<Integer>();
    for (DataColumn col2 : screenResult.getRawNumericDataColumns()) {
      for (ResultValue rv : col2.getResultValues()) {
        if (rv.isExclude()) {
          slogP.add(this.plateNumberToSequenceNumberMap.get(
              rv.getWell().getPlateNumber()));
          slogW.add(rv.getWell().getWellName());
          slogS.add(col2.getReplicateOrdinal());
        }
      }
    }

    // In case of at least one screenlog entry
    if (slogP.size() > 0) {
      // Create slog data object
      // Convert the ArrayLists to String[]
      double [] slogPd  = new double [slogP.size()];
      String [] slogWd  = new String [slogW.size()];
      double [] slogSd  = new double [slogS.size()];
      
      for (int j=0; j < slogP.size(); j++) {
    	  slogPd[j]=slogP.get(j);
    	  slogWd[j]=slogW.get(j);
    	  slogSd[j]=slogS.get(j);
      }

      rConnection.assign("slogP",slogPd );
      rConnection.assign("slogW",slogWd );
      rConnection.assign("slogS",slogSd );
      
      double[] slogC = new double[slogP.size()] ;
      String [] slogF = new String [slogP.size()] ;
      for (i=0 ; i < slogP.size(); i++) {
          slogC[i]=1;
          slogF[i]="NA";
      }
      // rConnection.assign("slogC" ,slogC);
      rConnection.assign("slogF" ,slogF);
      
      // Add slog statement to R expression
      rExpr.append("slog <- data.frame(Plate=slogP,Well=slogW,Sample=slogS, Flag=slogF,stringsAsFactors=FALSE);");
      rExpr.append("rc <- configureDb(r,conf,slog);");
    } else {
      rExpr.append("rc <- configureDb(r,conf);");
    }
    
    rExpr.append("\"OK\""); // OK, otherwise rc is retrieved in Rserve client
    rserveExtensions.tryEval(rConnection, rExpr.toString());

    return (contents);
  }
  // 3. RETRIEVE DATA FROM R (ONLY RELEVANT FOR RUNIT TEST)
  public String[] getConfigureDbResultConf() throws RserveException, REngineException, REXPMismatchException {
    String[] resultPlates = null;
    String rExpr2 = "plateConf <- rc@plateConf;"
        + "resultContent <- plateConf$Content";
    RserveExtensions rserveExtensions = new RserveExtensions();
    resultPlates = rserveExtensions.tryEval(rConnection, rExpr2).asStrings();

    // TODO also return plate and well
    // # 'data.frame', f.e. 3 obs. of 3 variables:
    // # $ Plate : chr "1" "1" "1"
    // # $ Well : chr "A01" "A02" "B01"
    // # $ Content: chr "sample" "pos" "neg"
    return (resultPlates);
  }

  public String[] getConfigureDbResultSlog() throws RserveException, REngineException, REXPMismatchException {
    String rExpr2 = "screenLog <- rc@screenLog;"
        + "resultContent <- screenLog$Plate";
    RserveExtensions rserveExtensions = new RserveExtensions();
    REXP result = rserveExtensions.tryEval(rConnection, rExpr2);
   
    if ( result.isNull()) {
       return null;
    }else {
      return result.asStrings();
    }

  }

  
  public void annotateDbInit()
  {
    setLastRMethodToRun(RMethod.ANNOTATE);
  }

  /**
   * Creates the geneIDs data.frame in the cellHTS2 object: ..
   * data.frame(Plate=plate, Well=well, HFAID=hfaid,
   * GeneID=geneid,stringsAsFactors=FALSE)
   */

  private void annotateDb() throws REngineException, RserveException, REXPMismatchException {
    // 1. PREPARE INPUT
    // TODO retrieve data from screenResult, for now using dummy data.

    // int[] plate = {1,1,1,1,2,2,2,2};
    // rConnection.assign("plate", plate);
    //
    // String[] well = {"A01","A02","B01","B02","A01","A02","B01","B02"};
    // rConnection.assign("well", well);
    //
    // // The "NA" will be converted into NA in annotate.R
    // String[] hfaid =
    // {"NA","NM_018243","NA","NM_001777","NA","NM_001087","NA","NM_001778"};
    // rConnection.assign("hfaid", hfaid);
    //
    // String[] geneid =
    // {"MOCK","SEPT11","MOCK","CD47","MOCK","AAMP","MOCK","CD48"};
    // rConnection.assign("geneid", geneid);

    DataColumn firstCol = screenResult.getDataColumns().first();
    int n = firstCol.getResultValues().size();
    int[] plates = new int[n];
    String[] wells = new String[n];
    String[] hfaIds = new String[n];
    String[] geneIds = new String[n];
    int i = 0;
    for (ResultValue rv : firstCol.getResultValues()) {
      plates[i] = this.plateNumberToSequenceNumberMap.get(rv.getWell().getPlateNumber());
      wells[i] = rv.getWell().getWellName();
      hfaIds[i] = "NA";
      geneIds[i] = "GENE" + i;
      ++i;
    }
    rConnection.assign("plate", plates);
    rConnection.assign("well", wells);
    rConnection.assign("hfaid", hfaIds);
    rConnection.assign("geneid", geneIds);

    // 2. RUN ANNOTATEDB
    String rExpr = "library(cellHTS2);"
        + "library(cellHTS2Db);"
        + "geneIDs <- data.frame(Plate=plate, Well=well, HFAID=hfaid, GeneID=geneid,stringsAsFactors=FALSE);"
        + "rca <- annotateDb(rc,geneIDs);" + "\"OK\"";

    RserveExtensions rserveExtensions = new RserveExtensions();
    rserveExtensions.tryEval(rConnection, rExpr);

    // rConnection.voidEval("save(rca,file=\"/tmp/rca.Rda\")");



  }

  // 3. RETRIEVE RESULTDATA ONLY RELEVANT FOR JUNIT TEST
  public String[] getAnnotateDbResult() throws REngineException, RserveException, REXPMismatchException {
    RserveExtensions rserveExtensions = new RserveExtensions();
    String[] result = rserveExtensions.tryEval(rConnection, "fData(rca)$GeneID").asStrings();
  return result;
  }
  
  // TODO change method in a parameters' key - value pairs

  /**
   * Runs additional cellHTS2 methods using data stored in R connection during
   * previous methods.
   */

  private void runRMethod(RMethod rMethod) throws RserveException, REXPMismatchException {

    // In the assaydata slot, the data is now three in stead of 4 dimensional as
    // the plates are now below each other
    // De dimensions are now: "Features","Sample", "Channels" . Sample can also
    // be read as "Replicate". See fData below for the details
    String runMethod = null;
    String cellHtsObjectName = null;
    if (rMethod.equals(RMethod.NORMALIZE_PLATES)) {
      cellHtsObjectName = "rcan";
      runMethod = cellHtsObjectName +
      // [atolopko, 2008-03-12] this commented out line includes the version I used to produce matching analysis results for the example data supplied by Boutros cellHTS2 tutorial
      //" <- normalizePlates(rca,scale=\"multiplicative\", log=FALSE, varianceAdjust=\"none\", method=\""
	     " <- normalizePlates(rca," +"scale=\"" + this.normalizePlatesScale.getValue() + "\"," +
      		"method=\""+ this.normalizePlatesMethod.getValue() + "\"," + "negControls=\"" + this.normalizePlatesNegControls.getValue()  + "\");";
    } else if (rMethod.equals(RMethod.SCORE_REPLICATES)) {
      cellHtsObjectName = "rcans";
      // [atolopko, 2008-03-12] this commented out line includes the version I used to produce matching analysis results for the example data supplied by Boutros cellHTS2 tutorial
      runMethod = cellHtsObjectName + 
      // [atolopko, 2008-03-12] this commented out line includes the version I used to produce matching analysis results for the example data supplied by Boutros cellHTS2 tutorial
      //" <- scoreReplicates(rcan, sign=\"-\", method=\""
      " <- scoreReplicates(rcan, method=\""
          + this.scoreReplicatesMethod.getValue() + "\");";
    } else if (rMethod.equals(RMethod.SUMMARIZE_REPLICATES)) {
      cellHtsObjectName = "rcanss";
      runMethod = cellHtsObjectName
          + " <- summarizeReplicates(rcans,summary=\"" + this.summarizeReplicatesMethod.getValue() + "\");";
    }

    int nrWells = arrayDimensions.getNrWells();
    int nrPlates = arrayDimensions.getNrPlates();
    int nrReps;
    if (rMethod.equals(RMethod.SUMMARIZE_REPLICATES)) {
      nrReps = 1; // that dimensions is limited to 1
    } else {
      nrReps = arrayDimensions.getNrReps();
    }

    // int nrChannels = arrayDimensions.getNrChannels();
    // str(rcanData): num [1:8, 1:2, 1] 1 3 4 5 .. , with dimension 1:
    // plate-well combination, dim. 2: replicate, dim 3: channel
    String createReturnObject = cellHtsObjectName + "Data <- array(Data("
        + cellHtsObjectName + "),c(" + (nrWells * nrPlates) + "," + nrReps
        + "));";

    String rExpr = runMethod + createReturnObject + "\"OK\"";
    RserveExtensions rserveExtensions = new RserveExtensions();
    rserveExtensions.tryEval(rConnection, rExpr);
}
  
  private void addRMethodResult(RMethod rMethod) throws RserveException, REXPMismatchException {    
	  
	 // check if rMethod is completed
     if (!isRMethodCompleted(rMethod)) {
         throw new CellHtsException(rMethod.getArgumentValue()  + " rMethod not initialized");
     }
	    // retrieve returnObject
    String rExpr2 = null;

    String colPrefix = CELLHTS2_DATA_COLUMN_PREFIX;
    if (rMethod.equals(RMethod.NORMALIZE_PLATES)) {
      rExpr2 = "rcanData";
      colPrefix += "norm_";
    } else if (rMethod.equals(RMethod.SCORE_REPLICATES)) {
      rExpr2 = "rcansData";
      colPrefix += "scored_";
    } else if (rMethod.equals(RMethod.SUMMARIZE_REPLICATES)) {
      rExpr2 = "rcanssData";
      colPrefix += "summarized";
    }

    // result = rConnection.eval(strEval).asDoubleMatrix();
    // Add to the screenResult a derived DataColumn for each of the raw
    // DataColumns
    List<AssayWell> assayWells = new ArrayList<AssayWell>(screenResult.getAssayWells());
    Collections.sort(assayWells);

    RserveExtensions rserveExtensions = new RserveExtensions();
    if (rMethod.equals(RMethod.NORMALIZE_PLATES)
        || rMethod.equals(RMethod.SCORE_REPLICATES)) {
      double[][] result = new double[arrayDimensions.getNrWells() * arrayDimensions.getNrPlates()][arrayDimensions.getNrReps()];
      result = rserveExtensions.tryEval(rConnection, rExpr2).asDoubleMatrix();

      // looping through just to get the replicate name
      for (DataColumn col : screenResult.getRawNumericDataColumns()) {

        // the position in the result matrix is based on the replicateOrdinal
        // value of the col: see readPlateListDb
        int r = col.getReplicateOrdinal();
        // col.getName();

        DataColumn colNew = screenResult.createDataColumn(colPrefix + col.getName());
        colNew.forReplicate(r).forChannel(col.getChannel()).makeDerived("cellHTS2", ImmutableSet.of(col)).makeNumeric(3).forPhenotype("phenotype");

        for (int i = 0; i < assayWells.size(); ++i) {
          AssayWell assayWell = assayWells.get(i);
          colNew.createResultValue(assayWell, result[i][r - 1]);
        }
      }
    } else if (rMethod.equals(RMethod.SUMMARIZE_REPLICATES)) {
      double[] result = new double[arrayDimensions.getNrWells() *  arrayDimensions.getNrPlates()];
      result = rserveExtensions.tryEval(rConnection, rExpr2).asDoubles();

      DataColumn colSumm = 
        screenResult.createDataColumn("cellhts2_summarized").
        makeNumeric(3).
        forPhenotype("phenotype").
        makeDerived("cellHTS2", Collections.<DataColumn>emptySet());
      for (int i = 0; i < assayWells.size(); ++i) {
        AssayWell assayWell = assayWells.get(i);
        colSumm.createResultValue(assayWell, result[i]);
      }
    }
  }
  
  /**
   * Call this if you just want to prepare the cellHTS object within the R
   * server. Could be useful if we later support saving the raw cellHTS object,
   * for a future interactive R session.
   */
  public void prepare()
  {
    setLastRMethodToRun(RMethod.ANNOTATE);
  }

  public void normalizePlatesInit(NormalizePlatesMethod normalizePlatesMethod, NormalizePlatesScale normalizePlatesScale,
          NormalizePlatesNegControls normalizePlatesNegControls)
  {
    this.normalizePlatesMethod = normalizePlatesMethod;
    this.normalizePlatesScale = normalizePlatesScale;
    this.normalizePlatesNegControls = normalizePlatesNegControls;
    setLastRMethodToRun(RMethod.NORMALIZE_PLATES);
  }


  
  public void normalizePlatesAddResult() throws RserveException, REXPMismatchException {
	  addRMethodResult(RMethod.NORMALIZE_PLATES);
  }

  
  public void scoreReplicatesInit(ScoreReplicatesMethod scoreReplicatesMethod)
  {
    this.scoreReplicatesMethod = scoreReplicatesMethod;
    setLastRMethodToRun(RMethod.SCORE_REPLICATES);
  }

  /**
   * @throws RserveException
   * @throws REXPMismatchException
   */

/*
 * private void scoreReplicates() throws RserveException, REXPMismatchException {
 * runRMethod(RMethod.SCORE_REPLICATES); }
 */ 
  public void scoreReplicatesAddResult() throws RserveException,	REXPMismatchException {
	 addRMethodResult(RMethod.SCORE_REPLICATES);
  }

  public void summarizeReplicatesInit(SummarizeReplicatesMethod summarizeReplicatesMethod)
  {
    this.summarizeReplicatesMethod = summarizeReplicatesMethod;
    setLastRMethodToRun(RMethod.SUMMARIZE_REPLICATES);
  }

  /**
   * screenResult: takes a screenResult which included resultTypes of 'scored'
   * 
   * @return
   */

 
  public void summarizeReplicatesAddResult() throws RserveException,REXPMismatchException {
	 addRMethodResult(RMethod.SUMMARIZE_REPLICATES);
  }
  

  public void writeReportInit(String reportOutputPath)
  {
    this.reportOutputPath = reportOutputPath;
    setLastRMethodToRun(RMethod.WRITE_REPORT);
  }

  /**
   * Create quality report with per replicate/ plate 1) metrics. f.e. spearman
   * rank correlation 2) boxplots both for raw and normalized data 3) for both
   * pos and neg controls a. normalized intensities b. distribution obtained
   * from kernel density estimates
   * 
   * @param outputPath: path to the directory where to store the report files,
   *          f.e. '/tmp/output'
   */

  private String writeReport() throws RserveException,  REXPMismatchException {

    // 1. PREPARE INPUT
    // not necessary

    // 2. RUN R METHOD WRITEREPORT AND RETRIEVE
    // TODO ADD "scored"=rcanss
    String progressReport = ScreensaverProperties.getProperty("cellHTS2report.writeReport.progressReport");
    if (progressReport == null) {
      progressReport = "TRUE";
    }else {
       if (progressReport.equals("FALSE") || progressReport.equals("TRUE") ) {
         ;
       }else {
         log.warn("In screensaver.properties the value for cellHTS2report.writeReport.progressReport is not valid. Only FALSE or TRUE are valid. " +
            "Given value is: " + progressReport + ". Value set to TRUE in this run.");
         progressReport = "TRUE";
       }
    }
   
    // In R 2.7.0 png needs cairo >= 1.2, in order to support lower versions like 1.0.2 
    // use value cairo1 for type
    String rCairo = "temp <- png; png <- function(...) { temp (type=\"cairo1\", ...) };";
    
    String rExpr = rCairo + "writeReport(cellHTSlist=list(\"raw\"=rca,\"normalized\"=rcan,\"scored\"=rcanss), plotPlateArgs = TRUE,"
        + "imageScreenArgs = list(zrange=c( -4, 8), ar=1), map=TRUE,force = TRUE, outdir = \""
        + this.reportOutputPath + "\")";

    RserveExtensions rserveExtensions = new RserveExtensions();
    String indexUrl = rserveExtensions.tryEval(rConnection, rExpr).asString();

    return (indexUrl);

  }

  public void closeConnection() {
    if (rConnection != null) {
      rConnection.close();
    }

  }

  public BiMap<Integer, Integer> createPlateNumberSequenceMapping(
      ScreenResult screenResult) {
    BiMap<Integer, Integer> plateNumber2SequenceNumber = HashBiMap.create();
    int nextSequenceNumber = 1;
    for (Well well : screenResult.getWells()) {
      if (!plateNumber2SequenceNumber.containsKey(well.getPlateNumber())) {
        plateNumber2SequenceNumber.put(well.getPlateNumber(),
            nextSequenceNumber++);
      }
    }
    return plateNumber2SequenceNumber;
  }

  public String getWriteReportIndexUrl() {
	 return writeReportIndexUrl;
  }

}
