// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.cellhts2;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import edu.harvard.med.screensaver.analysis.cellhts2.CellHTS2;
import edu.harvard.med.screensaver.analysis.cellhts2.NormalizePlatesMethod;
import edu.harvard.med.screensaver.analysis.cellhts2.NormalizePlatesNegControls;
import edu.harvard.med.screensaver.analysis.cellhts2.NormalizePlatesScale;
import edu.harvard.med.screensaver.analysis.cellhts2.RMethod;
import edu.harvard.med.screensaver.analysis.cellhts2.ScoreReplicatesMethod;
import edu.harvard.med.screensaver.analysis.cellhts2.SummarizeReplicatesMethod;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.util.DeleteDir;

public class CellHts2Annotator
{
  // static members

  private static Logger log = Logger.getLogger(CellHts2Annotator.class);

  // instance data members

  private GenericEntityDAO _dao;
  private CellHTS2 _cellHts;


  // public constructors and methods

  protected CellHts2Annotator()
  {
  }

  public CellHts2Annotator(GenericEntityDAO dao)
  {
    _dao = dao;
  }

  @Transactional
  public synchronized void runCellhts2( RMethod untilInclRmethod,
                                        ScreenResult screenResult,
                                        String analysisName,
                                        NormalizePlatesMethod normalizationMethod,
                                        NormalizePlatesNegControls normalizePlatesNegControls, 
                                        NormalizePlatesScale normalizationScale,
                                        ScoreReplicatesMethod scoreReplicatesMethod,
                                        SummarizeReplicatesMethod summarizeReplicatesMethod,
                                        boolean addNewCellHtsDataColumns,
                                        String reportOutputPath,
                                        String saveRObjectsPath)
  throws CellHts2AnnotatorException
  {
    _dao.reattachEntity(screenResult);
    try {
      try {
        _cellHts = new CellHTS2(screenResult, analysisName, reportOutputPath, saveRObjectsPath);

        if (untilInclRmethod.getIndex() >= RMethod.READ_PLATELIST
          .getIndex()) {
          _cellHts.readPlateListDbInit();
        }
        if (untilInclRmethod.getIndex() >= RMethod.CONFIGURE.getIndex()) {
          _cellHts.configureDbInit();
        }
        if (untilInclRmethod.getIndex() >= RMethod.ANNOTATE.getIndex()) {
          _cellHts.annotateDbInit();
        }

        //Initiate the parameters for all the methods to be run
        if (untilInclRmethod.getIndex() >= RMethod.NORMALIZE_PLATES.getIndex()){ 
          _cellHts.normalizePlatesInit(normalizationMethod,normalizationScale,normalizePlatesNegControls);
        }
        if (untilInclRmethod.getIndex() >= RMethod.SCORE_REPLICATES.getIndex()){ 
          _cellHts.scoreReplicatesInit(scoreReplicatesMethod);
        }
        if (untilInclRmethod.getIndex() >= RMethod.SUMMARIZE_REPLICATES.getIndex()){ 
          _cellHts.summarizeReplicatesInit(summarizeReplicatesMethod);
        }
        if (untilInclRmethod.getIndex() >= RMethod.WRITE_REPORT.getIndex()){ 
          File dir = new File(reportOutputPath);
          if (dir.exists()) {
            DeleteDir.deleteDirectory(dir);
          }
          if (!dir.mkdirs()) {
            throw new IOException("could not create cellHTS2 report output directory path " + dir);
          }
          _cellHts.writeReportInit(reportOutputPath);
        }

        _cellHts.run();

        //Add result of the methods which have run
        if (addNewCellHtsDataColumns) {
          deleteCellHtsDataColumns(screenResult);
          if (untilInclRmethod.getIndex() >= RMethod.NORMALIZE_PLATES.getIndex()){ 
            _cellHts.normalizePlatesAddResult();
          }
          if (untilInclRmethod.getIndex() >= RMethod.SCORE_REPLICATES.getIndex()){
            _cellHts.scoreReplicatesAddResult();
          }
          if (untilInclRmethod.getIndex() >= RMethod.SUMMARIZE_REPLICATES.getIndex()){
            _cellHts.summarizeReplicatesAddResult();
          }
          for (DataColumn col : screenResult.getDataColumns()) {
            if (col.getName().startsWith(CellHTS2.CELLHTS2_DATA_COLUMN_PREFIX)) {
              _dao.persistEntity(col);
              log.info("persisted new " + col);
            }
          }
        }
      }
      catch (Exception e) {
        throw new CellHts2AnnotatorException(e);
      }
    }
    finally {
      _cellHts.closeConnection();
    }
  }


  private void deleteCellHtsDataColumns(ScreenResult screenResult)
  {
    Set<DataColumn> toDelete = new HashSet<DataColumn>();
    for (DataColumn col : screenResult.getDataColumns()) {
      if (col.getName().startsWith(CellHTS2.CELLHTS2_DATA_COLUMN_PREFIX)) {
        toDelete.add(col);
      }
    }
    for (DataColumn col : toDelete) {
      screenResult.deleteDataColumn(col);
    }
    log.info("deleted existing data columns " + toDelete);
  }

  // private methods

}
