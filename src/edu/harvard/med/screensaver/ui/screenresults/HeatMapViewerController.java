// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import edu.harvard.med.screensaver.analysis.ChainedFilter;
import edu.harvard.med.screensaver.analysis.Filter;
import edu.harvard.med.screensaver.analysis.heatmaps.ControlWellsFilter;
import edu.harvard.med.screensaver.analysis.heatmaps.DefaultMultiColorGradient;
import edu.harvard.med.screensaver.analysis.heatmaps.EdgeWellsFilter;
import edu.harvard.med.screensaver.analysis.heatmaps.HeatMap;
import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.ui.AbstractController;
import edu.harvard.med.screensaver.ui.util.UISelectManyBean;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class HeatMapViewerController extends AbstractController
{

  // static data members

  private static final HeatMapCell EMPTY_HEAT_MAP_CELL = new HeatMapCell();
  private static final Collection<NumberFormat> NUMBER_FORMATS = new ArrayList<NumberFormat>();
  static {
    NUMBER_FORMATS.add(null);
    DecimalFormat nf1 = new HackedDecimalFormat("0.0##E00;-0.0##E00");
    NUMBER_FORMATS.add(nf1);
    DecimalFormat nf1m = new HackedDecimalFormat("0.0##E00;(0.0##E00)");
    NUMBER_FORMATS.add(nf1m);
    DecimalFormat nf2 = new HackedDecimalFormat("#,##0.0##;-#,##0.0##");
    NUMBER_FORMATS.add(nf2);
    DecimalFormat nf2m = new HackedDecimalFormat("#,##0.0##;(#,##0.0##)");
    NUMBER_FORMATS.add(nf2m);
    DecimalFormat nf3 = new HackedDecimalFormat("0;-0");
    NUMBER_FORMATS.add(nf3);
    DecimalFormat nf3m = new HackedDecimalFormat("0;(0)");
    NUMBER_FORMATS.add(nf3m);
  }
  private static final Collection<Filter<ResultValue>> EXCLUDED_WELL_FILTERS = new ArrayList<Filter<ResultValue>>();
  static {
    EXCLUDED_WELL_FILTERS.add(new ControlWellsFilter());
    EXCLUDED_WELL_FILTERS.add(new EdgeWellsFilter());
  }

  private static Logger log = Logger.getLogger(HeatMapViewerController.class);
  
  
  // instance data members

  private ScreenResult _screenResult;
  private UISelectManyBean<ResultValueType> _dataHeaders;
  private UISelectOneBean<Integer> _plateNumber;
  private UISelectOneBean<ScoringType> _scoringType;
  private UISelectOneBean<NumberFormat> _numericFormat;
  private UISelectManyBean<Filter<ResultValue>> _excludedWellFilters;
  private ListDataModel _heatMapDataModelsDataModel;
  private List<DataModel> _heatMapColumnDataModels;
  private DAO _dao;
  private ArrayList<HeatMap> _heatMaps;

  
  // bean property methods
  
  public void setDao(DAO dao)
  {
    _dao = dao;
  }

  public void setScreenResult(ScreenResult screenResult)
  {
    _screenResult = screenResult;
    _dataHeaders = new UISelectManyBean<ResultValueType>(_screenResult.getResultValueTypes()) {
      protected String getLabel(ResultValueType t) { return t.getName(); } 
    };
    _plateNumber = new UISelectOneBean<Integer>(_screenResult.getPlateNumbers());
    _scoringType = new UISelectOneBean<ScoringType>(Arrays.asList(ScoringType.values()));
    _numericFormat = new UISelectOneBean<NumberFormat>(NUMBER_FORMATS) {
      protected String getLabel(NumberFormat t) { return t == null ? "<none>" : t.format(9999.333) + " / " + t.format(-9999.333); } 
    };
    _excludedWellFilters = new UISelectManyBean<Filter<ResultValue>>(EXCLUDED_WELL_FILTERS);
  }
  
  public ScreenResult getScreenResult()
  {
    // for quicker web testing
    if (_screenResult == null) {
      log.debug("using default screen result for screen 107");
      Screen screen = _dao.findEntityByProperty(Screen.class, "hbnScreenNumber", 107);
      setScreenResult(screen.getScreenResult());
    }
    return _screenResult;
  }
  
  public UISelectManyBean<ResultValueType> getDataHeaders()
  {
    return _dataHeaders;
  }
  
  public UISelectOneBean<Integer> getPlateNumber()
  {
    return _plateNumber;
  }

  public UISelectOneBean<ScoringType> getScoringType()
  {
    return _scoringType;
  }

  public UISelectOneBean<NumberFormat> getNumericFormat()
  {
    return _numericFormat;
  }

  public UISelectManyBean<Filter<ResultValue>> getExcludedWellTypes()
  {
    return _excludedWellFilters;
  }
  
  public DataModel getHeatMapDataModelsDataModel()
  {
    return _heatMapDataModelsDataModel;
  }
  
  public List<DataModel> getHeatMapColumnDataModels()
  {
    return _heatMapColumnDataModels;
  }
  
  public List<HeatMap> getHeatMaps()
  {
    return _heatMaps;
  }

  @SuppressWarnings("unchecked")
  public HeatMapCell getHeatMapCell()
  {
    DataModel heatMapDataModel = (DataModel) _heatMapDataModelsDataModel.getRowData();
    DataModel heatMapColumnDataModel = _heatMapColumnDataModels.get(_heatMapDataModelsDataModel.getRowIndex());
    if (heatMapColumnDataModel.isRowAvailable()) {
      Integer columnIndex = (Integer) heatMapColumnDataModel.getRowData(); // getRowData() is really getColumnData()
      List<HeatMapCell> row = (List<HeatMapCell>) heatMapDataModel.getRowData();
      return row.get(columnIndex);
    }
    return EMPTY_HEAT_MAP_CELL;
  }


  // JSF application methods

  public String update()
  {
    if (_plateNumber.getSelection() == null ||
      _scoringType.getSelection() == null) {
      reportSystemError("JSF validation did not catch required fields");
    }
    List<DataModel> heatMapDataModels = new ArrayList<DataModel>();
    _heatMapColumnDataModels = new ArrayList<DataModel>();
    _heatMaps = new ArrayList<HeatMap>();
    for (ResultValueType rvt : _dataHeaders.getSelections()) {
      HeatMap heatMap = new HeatMap(rvt,
                                     _plateNumber.getSelection(),
                                     new ChainedFilter<ResultValue>(_excludedWellFilters.getSelections()),
                                     _scoringType.getSelection().getFunction(),
                                     new DefaultMultiColorGradient());

      NumberFormat format = _numericFormat.getSelection();
      List<List<HeatMapCell>> rows = new ArrayList<List<HeatMapCell>>();
      for (int row = 0; row < heatMap.getRowCount(); row++) {
        List<HeatMapCell> rowData = new ArrayList<HeatMapCell>();
        for (int column = 0; column < heatMap.getColumnCount(); column++) {
          rowData.add(new HeatMapCell(heatMap.getScoredValue(row, column),
                                      heatMap.getColor(row, column),
                                      format));
        }
        rows.add(rowData);
      }
      heatMapDataModels.add(new ListDataModel(rows));
      
      List<Integer> columnIndexes = new ArrayList<Integer>();
      for (int column = 0; column < heatMap.getColumnCount(); column++) {
        columnIndexes.add(column);
      }
      _heatMapColumnDataModels.add(new ListDataModel(columnIndexes));
      _heatMaps.add(heatMap);
    }
    _heatMapDataModelsDataModel = new ListDataModel(heatMapDataModels);

    

    return REDISPLAY_PAGE_ACTION_RESULT; // redisplay
  }
  
  public String done()
  {
    return DONE_ACTION_RESULT;
  }

  
  // private methods

  
}
