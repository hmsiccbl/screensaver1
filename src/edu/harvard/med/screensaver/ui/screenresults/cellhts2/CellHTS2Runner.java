package edu.harvard.med.screensaver.ui.screenresults.cellhts2;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.analysis.cellhts2.NormalizePlatesMethod;
import edu.harvard.med.screensaver.analysis.cellhts2.NormalizePlatesNegControls;
import edu.harvard.med.screensaver.analysis.cellhts2.NormalizePlatesScale;
import edu.harvard.med.screensaver.analysis.cellhts2.RMethod;
import edu.harvard.med.screensaver.analysis.cellhts2.ScoreReplicatesMethod;
import edu.harvard.med.screensaver.analysis.cellhts2.SummarizeReplicatesMethod;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.service.cellhts2.CellHts2Annotator;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.UICommand;
import edu.harvard.med.screensaver.ui.util.JSFUtils;

/**
 * @author Siew Cheng Aw
 */
public class CellHTS2Runner extends AbstractBackingBean
{
  private static Logger log = Logger.getLogger(CellHTS2Runner.class);

  private ScreenResult _screenResult;
  private NormalizePlatesMethod _normalizePlatesMethod = NormalizePlatesMethod.MEDIAN;
  private NormalizePlatesScale _normalizePlatesScale = NormalizePlatesScale.ADDITIVE;
  private NormalizePlatesNegControls _normalizePlatesNegControls = NormalizePlatesNegControls.NEG;
  private ScoreReplicatesMethod _scoreReplicatesMethod = ScoreReplicatesMethod.ZSCORE;
  private SummarizeReplicatesMethod _summarizeReplicatesMethod = SummarizeReplicatesMethod.MEAN;
  private File _reportFilePath;
  private String _reportUrl;

  private boolean _addNewCellHtsDataColumns;
  private CellHts2Annotator _cellHts2Annotator;

  /**
   * @motivation for CGLIB2
   */
  protected CellHTS2Runner()
  {}

  public CellHTS2Runner(CellHts2Annotator cellHts2Annotator)
  {
    _cellHts2Annotator = cellHts2Annotator;
  }

  public boolean isEnabled()
  {
    return getApplicationProperties().isFeatureEnabled("cellHTS2");
  }

  public void setScreenResult(ScreenResult screenResult)
  {
    _screenResult = screenResult;
    if (screenResult != null) {
      _reportFilePath = new File(new File(new File(getApplicationProperties().getProperty("cellHTS2.report.directory"), getScreensaverUser().getEntityId().toString()),
                                          screenResult.getEntityId().toString()),
                                 "index.html");
      _reportUrl = ScreensaverConstants.CELLHTS2_REPORTS_BASE_URL + _screenResult.getScreenResultId() + "/index.html";
    }
    else {
      _reportFilePath = null;
      _reportUrl = null;
    }
    _addNewCellHtsDataColumns = false; // always reset, for safety of data
  }

  public ScreenResult getScreenResult()
  {
    return _screenResult;
  }

  public boolean isCellHTS2ReportFileExists()
  {
    return _reportFilePath.exists();
  }

  public String getReportURL()
  {
    return _reportUrl;
  }

  public NormalizePlatesMethod getNormalizePlatesMethod()
  {
    return _normalizePlatesMethod;
  }

  public void setNormalizePlatesMethod(NormalizePlatesMethod normalizePlatesMethod)
  {
    _normalizePlatesMethod = normalizePlatesMethod;
  }

  public NormalizePlatesNegControls getNormalizePlatesNegControls()
  {
    return _normalizePlatesNegControls;
  }

  public void setNormalizePlatesNegControls(NormalizePlatesNegControls normalizePlatesNegControls)
  {
    _normalizePlatesNegControls = normalizePlatesNegControls;
  }

  public NormalizePlatesScale getNormalizePlatesScale()
  {
    return _normalizePlatesScale;
  }

  public void setNormalizePlatesScale(NormalizePlatesScale normalizePlatesScale) {
    _normalizePlatesScale = normalizePlatesScale;
  }

  public ScoreReplicatesMethod getScoreReplicatesMethod()
  {
    return _scoreReplicatesMethod;
  }

  public void setScoreReplicatesMethod(ScoreReplicatesMethod scoreReplicatesMethod)
  {
    _scoreReplicatesMethod = scoreReplicatesMethod;
  }

  public SummarizeReplicatesMethod getSummarizeReplicatesMethod()
  {
    return _summarizeReplicatesMethod;
  }

  public void setSummarizeReplicatesMethod(SummarizeReplicatesMethod summarizeReplicatesMethod)
  {
    _summarizeReplicatesMethod = summarizeReplicatesMethod;
  }

  public boolean isAddNewCellHtsDataColumns()
  {
    return _addNewCellHtsDataColumns;
  }

  public void setAddNewCellHtsDataColumns(boolean addNewCellHtsDataColumns)
  {
    _addNewCellHtsDataColumns = addNewCellHtsDataColumns;
  }

  @UICommand
  public String runCellHTS2()
  {
    _cellHts2Annotator.runCellhts2(RMethod.WRITE_REPORT,
                                   _screenResult,
                                   _screenResult.getScreen().getTitle(),
                                   _normalizePlatesMethod,
                                   _normalizePlatesNegControls,
                                   _normalizePlatesScale,
                                   _scoreReplicatesMethod,
                                   _summarizeReplicatesMethod,
                                   _addNewCellHtsDataColumns,
                                   _reportFilePath.getPath(),
                                   getApplicationProperties().isPropertySet("cellHTS2report.saveRObjects") ?
                                     getApplicationProperties().getProperty("cellHTS2report.saveRObjects.path") : null);
    return VIEW_SCREEN;
  }

  public String viewCellHTS2Runner(ScreenResult screenResult)
  {
    setScreenResult(screenResult);
    return RUN_CELLHTS2;
  }


  public List<SelectItem> getNormalizePlatesMethodSelections()
  {
    List<NormalizePlatesMethod> selections = new ArrayList<NormalizePlatesMethod>();
    for (NormalizePlatesMethod normalizePlatesMethod : NormalizePlatesMethod.values()) {
      selections.add(normalizePlatesMethod);
    }
    return JSFUtils.createUISelectItems(selections);
  }

  public List<SelectItem> getNormalizePlatesScaleSelections()
  {
    List<NormalizePlatesScale> selections = new ArrayList<NormalizePlatesScale>();
    for (NormalizePlatesScale normalizePlatesScale : NormalizePlatesScale.values()) {
      selections.add(normalizePlatesScale);
    }
    return JSFUtils.createUISelectItems(selections);
  }

  public List<SelectItem> getNormalizePlatesNegControlsSelections()
  {
    List<NormalizePlatesNegControls> selections = new ArrayList<NormalizePlatesNegControls>();
    for (NormalizePlatesNegControls normalizePlatesNegControls : NormalizePlatesNegControls.values()) {
      selections.add(normalizePlatesNegControls);
    }
    return JSFUtils.createUISelectItems(selections);
  }

  public List<SelectItem> getScoreReplicatesMethodSelections()
  {
    List<ScoreReplicatesMethod> selections = new ArrayList<ScoreReplicatesMethod>();
    for (ScoreReplicatesMethod scoreReplicatesMethod : ScoreReplicatesMethod.values()) {
      selections.add(scoreReplicatesMethod);
    }
    return JSFUtils.createUISelectItems(selections);
  }

  public List<SelectItem> getSummarizeReplicatesMethodSelections()
  {
    List<SummarizeReplicatesMethod> selections = new ArrayList<SummarizeReplicatesMethod>();
    for (SummarizeReplicatesMethod summarizeReplicatesMethod : SummarizeReplicatesMethod.values()) {
      selections.add(summarizeReplicatesMethod);
    }
    return JSFUtils.createUISelectItems(selections);
  }
}
