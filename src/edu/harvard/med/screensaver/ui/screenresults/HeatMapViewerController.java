// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import edu.harvard.med.screensaver.analysis.heatmaps.ControlWellsFilter;
import edu.harvard.med.screensaver.analysis.heatmaps.DefaultMultiColorGradient;
import edu.harvard.med.screensaver.analysis.heatmaps.HeatMap;
import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.ui.AbstractController;
import edu.harvard.med.screensaver.ui.util.UISelectManyBean;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;

import org.apache.log4j.Logger;

public class HeatMapViewerController extends AbstractController
{

  // static data members

  private static final HeatMapCell EMPTY_HEAT_MAP_CELL = new HeatMapCell();

  private static Logger log = Logger.getLogger(HeatMapViewerController.class);
  
  
  // instance data members

  private ScreenResult _screenResult;
  private UISelectManyBean<ResultValueType> _dataHeaders;
  private UISelectOneBean<Integer> _plateNumber;
  private UISelectOneBean<ScoringType> _scoringType;
  private ListDataModel _heatMapDataModelsDataModel;
  private List<DataModel> _heatMapColumnDataModels;
  private DAO _dao;

  
  // bean property methods
  
  public void setDao(DAO dao)
  {
    _dao = dao;
  }

  public void setScreenResult(ScreenResult screenResult)
  {
    _screenResult = screenResult;
    _dataHeaders = new UISelectManyBean<ResultValueType>(_screenResult.getResultValueTypes()) {
      protected String getLabel(ResultValueType t) { return t.getName(); } };
    _plateNumber = new UISelectOneBean<Integer>(_screenResult.getPlateNumbers());
    _scoringType = new UISelectOneBean<ScoringType>(Arrays.asList(ScoringType.values()));
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

  public DataModel getHeatMapDataModelsDataModel()
  {
    return _heatMapDataModelsDataModel;
  }
  
  public List<DataModel> getHeatMapColumnDataModels()
  {
    return _heatMapColumnDataModels;
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
    for (ResultValueType rvt : _dataHeaders.getSelections()) {
      HeatMap _heatMap = new HeatMap(rvt,
                                     _plateNumber.getSelection(),
                                     new ControlWellsFilter(),
                                     _scoringType.getSelection().getFunction(),
                                     new DefaultMultiColorGradient());

      List<List<HeatMapCell>> rows = new ArrayList<List<HeatMapCell>>();
      for (int row = 0; row < _heatMap.getRowCount(); row++) {
        List<HeatMapCell> rowData = new ArrayList<HeatMapCell>();
        for (int column = 0; column < _heatMap.getColumnCount(); column++) {
          rowData.add(new HeatMapCell(_heatMap.getScoredValue(row, column),
                                      _heatMap.getColor(row, column)));
        }
        rows.add(rowData);
      }
      heatMapDataModels.add(new ListDataModel(rows));
      
      List<Integer> columnIndexes = new ArrayList<Integer>();
      for (int column = 0; column < _heatMap.getColumnCount(); column++) {
        columnIndexes.add(column);
      }
      _heatMapColumnDataModels.add(new ListDataModel(columnIndexes));
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
