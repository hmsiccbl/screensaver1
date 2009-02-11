package edu.harvard.med.screensaver.ui.screenresults.cellhts2;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import edu.harvard.med.screensaver.ScreensaverProperties;
import edu.harvard.med.screensaver.analysis.cellhts2.NormalizePlatesMethod;
import edu.harvard.med.screensaver.analysis.cellhts2.RMethod;
import edu.harvard.med.screensaver.analysis.cellhts2.ScoreReplicatesMethod;
import edu.harvard.med.screensaver.analysis.cellhts2.SummarizeReplicatesMethod;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.service.cellhts2.CellHts2Annotator;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.EntityViewer;
import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.util.JSFUtils;

import org.apache.log4j.Logger;

/**
 * @author Siew Cheng Aw
 */
public class CellHTS2Runner extends AbstractBackingBean implements EntityViewer
{

	private static Logger log = Logger.getLogger(CellHTS2Runner.class);

    // instance data

	private ScreenResult _screenResult;
	private NormalizePlatesMethod _normalizePlatesMethod = NormalizePlatesMethod.MEDIAN;
	private ScoreReplicatesMethod _scoreReplicatesMethod = ScoreReplicatesMethod.ZSCORE;
	private SummarizeReplicatesMethod _summarizeReplicatesMethod = SummarizeReplicatesMethod.MEAN;
	private String _screenResultFilePath;

	private boolean _addNewCellHtsResultValueTypes;
	private CellHts2Annotator _cellHts2Annotator;


	/**
	 * @motivation for CGLIB2
	 */
	protected CellHTS2Runner()
	{
	}

	public CellHTS2Runner(CellHts2Annotator cellHts2Annotator)
	{
		_cellHts2Annotator = cellHts2Annotator;
	}

	
	public AbstractEntity getEntity()
	{
		return getScreenResult();
	}

	public void setScreenResult(ScreenResult screenResult)
	{
		_screenResult = screenResult;
		_screenResultFilePath = WEBAPP_ROOT + ScreensaverProperties.getProperty("cellHTS2report.filepath.prefix") + screenResult.getScreenResultId();
	}

	public ScreenResult getScreenResult()
	{
		return _screenResult;
	}

	public NormalizePlatesMethod getNormalizePlatesMethod() {
		return _normalizePlatesMethod;
	}

	public void setNormalizePlatesMethod(NormalizePlatesMethod normalizePlatesMethod) {
		_normalizePlatesMethod = normalizePlatesMethod;
	}

	public ScoreReplicatesMethod getScoreReplicatesMethod() {
		return _scoreReplicatesMethod;
	}

	public void setScoreReplicatesMethod(ScoreReplicatesMethod scoreReplicatesMethod) {
		_scoreReplicatesMethod = scoreReplicatesMethod;
	}

	public SummarizeReplicatesMethod getSummarizeReplicatesMethod() {
		return _summarizeReplicatesMethod;
	}

	public void setSummarizeReplicatesMethod(SummarizeReplicatesMethod summarizeReplicatesMethod) {
		_summarizeReplicatesMethod = summarizeReplicatesMethod;
	}


	public boolean isAddNewCellHtsResultValueTypes()
	{
		return _addNewCellHtsResultValueTypes;
	}

	public void setAddNewCellHtsResultValueTypes(boolean addNewCellHtsResultValueTypes)
	{
		_addNewCellHtsResultValueTypes = addNewCellHtsResultValueTypes;
	}

	@UIControllerMethod
	public String runCellHTS2()
	{
		_cellHts2Annotator.runCellhts2( RMethod.WRITE_REPORT,
										_screenResult,
										_screenResult.getScreen().getTitle(),
										_normalizePlatesMethod,
										_scoreReplicatesMethod,
										_summarizeReplicatesMethod,
										_addNewCellHtsResultValueTypes,
										_screenResultFilePath);
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
