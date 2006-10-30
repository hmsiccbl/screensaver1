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
import java.util.List;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

import edu.harvard.med.screensaver.analysis.heatmaps.ControlWellsFilter;
import edu.harvard.med.screensaver.analysis.heatmaps.DefaultMultiColorGradient;
import edu.harvard.med.screensaver.analysis.heatmaps.HeatMap;
import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.model.screenresults.ActivityIndicatorType;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.ui.AbstractController;
import edu.harvard.med.screensaver.ui.UniqueDataHeaderNames;
import edu.harvard.med.screensaver.ui.util.JSFUtils;

import org.apache.log4j.Logger;

public class HeatMapViewerController extends AbstractController
{

  // static data members

  private static final HeatMapCell EMPTY_HEAT_MAP_CELL = new HeatMapCell();


  private static Logger log = Logger.getLogger(HeatMapViewerController.class);
  
  
  // instance data members

  private ScreenResult _screenResult;
  private Integer _dataHeaderIndex;
  private Integer _resultValueTypeId;
  private Integer _plateNumber;
  private ScoringType _scoringType = ScoringType.ZSCORE;
  private UniqueDataHeaderNames _uniqueDataHeaderNames;
  private HeatMap _heatMap;
  /**
   * 
   */
  private DataModel _heatMapDataModel;
  private DataModel _heatMapColumnDataModel;
  
  private DAO _dao;



  // bean property methods
  
  public void setDao(DAO dao)
  {
    _dao = dao;
  }

  public void setScreenResult(ScreenResult screenResult)
  {
    _screenResult = screenResult;
    _plateNumber = screenResult.getPlateNumbers().first();
    _uniqueDataHeaderNames = new UniqueDataHeaderNames(_screenResult);
    List<SelectItem> dataHeaderSelectItems = getDataHeaderSelectItems();
    _resultValueTypeId = dataHeaderSelectItems.size() > 0 ? (Integer) getDataHeaderSelectItems().get(0).getValue()
                                                       : null;
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
  
  public Integer getResultValueTypeId()
  {
    return _resultValueTypeId;
  }

  public void setResultValueTypeId(Integer resultValueTypeId)
  {
    _resultValueTypeId = resultValueTypeId;
  }

  public void setPlateNumber(Integer plateNumber)
  {
    _plateNumber = plateNumber;
  }
  
  public Integer getPlateNumber()
  {
    return _plateNumber;
  }
  
  public List<SelectItem> getPlateSelectItems()
  {
    return JSFUtils.createUISelectItems(getScreenResult().getPlateNumbers());
  }
  
  public ScoringType getScoringType()
  {
    return _scoringType;
  }

  public void setScoringType(ScoringType scoringType)
  {
    _scoringType = scoringType;
  }

  public List<SelectItem> getDataHeaderSelectItems()
  {
    List<SelectItem> selectItems = new ArrayList<SelectItem>();
    for (ResultValueType rvt : getScreenResult().getResultValueTypes()) {
      if (rvt.getActivityIndicatorType()
             .equals(ActivityIndicatorType.NUMERICAL)) {
        selectItems.add(new SelectItem(rvt.getEntityId(),
                                       _uniqueDataHeaderNames.get(rvt.getOrdinal())));
      }
    }
    return selectItems;
  }
  
  public List<SelectItem> getScoringTypeSelectItems()
  {
    List<SelectItem> selectItems = new ArrayList<SelectItem>();
    for (ScoringType scoringType : ScoringType.values()) {
      selectItems.add(new SelectItem(scoringType,
                                     scoringType.getDescription()));
    }
    return selectItems;
  }
  
  
  public DataModel getHeatMapDataModel()
  {
    return _heatMapDataModel;
  }
  
  public DataModel getHeatMapColumnDataModel()
  {
    return _heatMapColumnDataModel;
  }
  

  @SuppressWarnings("unchecked")
  public HeatMapCell getHeatMapCell()
  {
    if (_heatMapColumnDataModel.isRowAvailable()) {
      Integer columnIndex = (Integer) _heatMapColumnDataModel.getRowData(); // getRowData() is really getColumnData()
      List<HeatMapCell> row = (List<HeatMapCell>) _heatMapDataModel.getRowData();
      return row.get(columnIndex);
    }
    return EMPTY_HEAT_MAP_CELL;
  }


  // JSF application methods

  public String update()
  {
    ResultValueType rvt = _dao.findEntityById(ResultValueType.class, _resultValueTypeId);
    _heatMap = new HeatMap(rvt,
                           _plateNumber,
                           new ControlWellsFilter(),
                           _scoringType.getFunction(),
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
    _heatMapDataModel = new ListDataModel(rows);
    
    List<Integer> columnIndexes = new ArrayList<Integer>();
    for (int column = 0; column < _heatMap.getColumnCount(); column++) {
      columnIndexes.add(column);
    }
    _heatMapColumnDataModel = new ListDataModel(columnIndexes);

    return REDISPLAY_PAGE_ACTION_RESULT; // redisplay
  }
  
  public String done()
  {
    return DONE_ACTION_RESULT;
  }

  
  // private methods

  
}
