// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults;

import java.awt.Color;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import edu.harvard.med.screensaver.analysis.ChainedFilter;
import edu.harvard.med.screensaver.analysis.Filter;
import edu.harvard.med.screensaver.analysis.heatmaps.ColorFunction;
import edu.harvard.med.screensaver.analysis.heatmaps.ControlWellsFilter;
import edu.harvard.med.screensaver.analysis.heatmaps.DefaultMultiColorGradient;
import edu.harvard.med.screensaver.analysis.heatmaps.EdgeWellsFilter;
import edu.harvard.med.screensaver.analysis.heatmaps.HeatMap;
import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.control.LibrariesController;
import edu.harvard.med.screensaver.ui.util.UISelectManyBean;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;
import edu.harvard.med.screensaver.util.Pair;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class HeatMapViewer extends AbstractBackingBean
{

  // static data members

  private static final HeatMapCell EMPTY_HEAT_MAP_CELL = new HeatMapCell();
  private static final List<NumberFormat> NUMBER_FORMATS = new ArrayList<NumberFormat>();
  static {
    DecimalFormat nf1 = new HackedDecimalFormat("0.0##E0;-0.0##E0");
    NUMBER_FORMATS.add(nf1);
    DecimalFormat nf1m = new HackedDecimalFormat("0.0##E0;(0.0##E0)");
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
  private static final int PLATE_ROWS = 26;
  private static final String[] HEAT_MAP_ROW_LABELS = new String[PLATE_ROWS];
  static {
    for (int i = 0; i < HEAT_MAP_ROW_LABELS.length; i++) {
      HEAT_MAP_ROW_LABELS[i] = Character.toString((char) ('A' + i));
    }
  }
  private static final Filter<ResultValue> IMPLICIT_FILTER = new ExcludedOrNonDataProducingWellFilter();
  private static final Double SAMPLE_NUMBER = new Double(-1234.567);
  private static final NumberFormat DECIMAL_FORMAT = new DecimalFormat("0.0##");
  private static final int COLOR_LEGEND_GRADIENT_STEPS = 10;


  private static Logger log = Logger.getLogger(HeatMapViewer.class);
  
  
  // instance data members

  private ScreenResult _screenResult;
  private UISelectOneBean<Integer> _plateNumber;
  private DAO _dao;
  private ArrayList<HeatMap> _heatMaps;
  private boolean _showValues;
  private List<HeatMapConfiguration> _heatMapConfigurations;
  private DataModel _heatMapConfigurationsDataModel;
  private List<DataModel> _heatMapDataModels;
  private List<DataModel> _heatMapStatistics;
  private List<DataModel> _heatMapColumnDataModels;
  private List<String> _heatMapRowLabels;
  private LibrariesController _librariesController;

  
  // bean property methods
  
  public void setDao(DAO dao)
  {
    _dao = dao;
  }

  public void setLibrariesController(LibrariesController librariesController) {
    _librariesController = librariesController;
  }

  public void setScreenResult(ScreenResult screenResult)
  {
    _screenResult = screenResult;
    if (_screenResult != null) {
      _plateNumber = new UISelectOneBean<Integer>(_screenResult.getPlateNumbers());
      _heatMaps = new ArrayList<HeatMap>();
      _heatMapConfigurations = new ArrayList<HeatMapConfiguration>();
      addHeatMap();
    }
  }
  
  public ScreenResult getScreenResult()
  {
    return _screenResult;
  }
  
  public UISelectOneBean<Integer> getPlateNumber()
  {
    return _plateNumber;
  }
  
  public boolean isShowValues() {
    return _showValues;
  }

  public void setShowValues(boolean showValues) {
    _showValues = showValues;
  }

  public String[] getHeatMapRowLabels()
  {
    return HEAT_MAP_ROW_LABELS;
  }

  public List<DataModel> getHeatMapDataModels()
  {
    return _heatMapDataModels;
  }
  
  public List<DataModel> getHeatMapColumnDataModels()
  {
    return _heatMapColumnDataModels;
  }
  
  public DataModel getColorLegendDataModel()
  {
    int heatMapIndex = _heatMapConfigurationsDataModel.getRowIndex();
    HeatMapConfiguration heatMapConfig = _heatMapConfigurations.get(heatMapIndex);
    ColorFunction colorFn = _heatMaps.get(heatMapIndex).getColorFunction();
    NumberFormat format = heatMapConfig.getNumericFormat().getSelection();
    double min = _heatMaps.get(heatMapIndex).getMin();
    double max = _heatMaps.get(heatMapIndex).getMax();
    double range = max - min;
    List<Pair<String,String>> steps = new ArrayList<Pair<String,String>>(COLOR_LEGEND_GRADIENT_STEPS);
    for (int i = 0; i <= COLOR_LEGEND_GRADIENT_STEPS; ++i) {
      double stepValue = min + range * ((double) i / (double) COLOR_LEGEND_GRADIENT_STEPS);
      Color color = colorFn.getColor(stepValue);
      String cssStyle = String.format("background-color: #%02x%02x%02x",
                                      color.getRed(),
                                      color.getGreen(),
                                      color.getBlue());
      steps.add(new Pair<String,String>(cssStyle, format.format(stepValue)));
    }
    return new ListDataModel(steps);
  }
  
  public List<HeatMap> getHeatMaps()
  {
    return _heatMaps;
  }
  
  public List<DataModel> getHeatMapStatisticsDataModels()
  {
    return _heatMapStatistics;
  }
  
  public DataModel getHeatMapConfigurationsDataModel()
  {
    return _heatMapConfigurationsDataModel;
  }

  @SuppressWarnings("unchecked")
  public HeatMapCell getHeatMapCell()
  {
    int heatMapIndex = _heatMapConfigurationsDataModel.getRowIndex();
    DataModel heatMapDataModel = (DataModel) _heatMapDataModels.get(heatMapIndex);
    DataModel heatMapColumnDataModel = _heatMapColumnDataModels.get(heatMapIndex);
    if (heatMapColumnDataModel.isRowAvailable()) {
      Integer columnIndex = (Integer) heatMapColumnDataModel.getRowData(); // getRowData() is really getColumnData()
      List<HeatMapCell> row = (List<HeatMapCell>) heatMapDataModel.getRowData();
      return row.get(columnIndex);
    }
    return EMPTY_HEAT_MAP_CELL;
  }
  
  public String getHeatMapTitle()
  {
    int heatMapIndex = _heatMapConfigurationsDataModel.getRowIndex();
    HeatMapConfiguration heatMapConfiguration = _heatMapConfigurations.get(heatMapIndex);
    StringBuilder title = new StringBuilder();
    title.append(heatMapConfiguration.getDataHeaders().getSelection().getName());
    title.append(": ");
    title.append(heatMapConfiguration.getScoringType().getSelection().toString());
    List<Filter<ResultValue>> filterSelections = heatMapConfiguration.getExcludedWellFilters().getSelections();
    if (filterSelections != null && filterSelections.size() > 0) {
      title.append(" (exclude ");
      boolean first = true;
      for (Filter filter : filterSelections) {
        if (first) {
          first = false;
        } 
        else {
          title.append(", ");
        }
        title.append(filter.toString().toLowerCase());
      }
      title.append(")");
    }
    return title.toString();
  }


  // JSF application methods

  @SuppressWarnings("unchecked")
  public String update()
  {
    _heatMapDataModels = new ArrayList<DataModel>();
    _heatMapColumnDataModels = new ArrayList<DataModel>();
    _heatMaps = new ArrayList<HeatMap>();
    _heatMapStatistics = new ArrayList<DataModel>();
      for (HeatMapConfiguration heatMapConfig : _heatMapConfigurations) {
        Map<WellKey,ResultValue> resultValues = 
          _dao.findResultValuesByPlate(_plateNumber.getSelection(), 
                                       heatMapConfig.getDataHeaders().getSelection());
        HeatMap heatMap = new HeatMap(_plateNumber.getSelection(),
                                    resultValues,
                                    new ChainedFilter<ResultValue>(IMPLICIT_FILTER,
                                                                   new ChainedFilter<ResultValue>(heatMapConfig.getExcludedWellFilters()
                                                                                                               .getSelections())),
                                    heatMapConfig.getScoringType()
                                                 .getSelection()
                                                 .getFunction(),
                                    new DefaultMultiColorGradient());

      NumberFormat format = heatMapConfig.getNumericFormat()
                                         .getSelection();
      List<List<HeatMapCell>> rows = new ArrayList<List<HeatMapCell>>();
      for (int row = 0; row < heatMap.getRowCount(); row++) {
        List<HeatMapCell> rowData = new ArrayList<HeatMapCell>();
        for (int column = 0; column < heatMap.getColumnCount(); column++) {
          rowData.add(new HeatMapCell(heatMap.getResultValue(row, column),
                                      heatMap.getWellKey(row, column),
                                      heatMap.getScoredValue(row, column),
                                      heatMap.getColor(row, column),
                                      isShowValues(),
                                      format));
        }
        rows.add(rowData);
      }
      _heatMapDataModels.add(new ListDataModel(rows));

      List<Integer> columnIndexes = new ArrayList<Integer>();
      for (int column = 0; column < heatMap.getColumnCount(); column++) {
        columnIndexes.add(column);
      }
      _heatMapColumnDataModels.add(new ListDataModel(columnIndexes));
      _heatMaps.add(heatMap);

      if (format == null) {
        format = NUMBER_FORMATS.get(1);
      }
      List<FormattedStatistic> heatMapStatistics = new ArrayList<FormattedStatistic>();
      heatMapStatistics.add(new FormattedStatistic("N", heatMap.getCount()));
      heatMapStatistics.add(new FormattedStatistic("Min",
                                                   heatMap.getMin(),
                                                   format));
      heatMapStatistics.add(new FormattedStatistic("Max",
                                                   heatMap.getMax(),
                                                   format));
      heatMapStatistics.add(new FormattedStatistic("Mean",
                                                   heatMap.getMean(),
                                                   format));
      heatMapStatistics.add(new FormattedStatistic("Median",
                                                   heatMap.getMedian(),
                                                   format));
      heatMapStatistics.add(new FormattedStatistic("Stdev",
                                                   heatMap.getStandardDeviation(),
                                                   format));
      heatMapStatistics.add(new FormattedStatistic("Var",
                                                   heatMap.getVariance(),
                                                   format));
      heatMapStatistics.add(new FormattedStatistic("Skewness",
                                                   heatMap.getSkewness(),
                                                   DECIMAL_FORMAT));
      _heatMapStatistics.add(new ListDataModel(heatMapStatistics));
    }
    
    return REDISPLAY_PAGE_ACTION_RESULT;
  }

  // TODO: set initial values to previous HeatMapConfig
  public String addHeatMap()
  {
    HeatMapConfiguration heatMapConfiguration = new HeatMapConfiguration();
    heatMapConfiguration.setDataHeaders(new UISelectOneBean<ResultValueType>(_screenResult.getNumericResultValueTypes()) {
      protected String getLabel(ResultValueType t) { return t.getName(); } 
    });
    heatMapConfiguration.setScoringType(new UISelectOneBean<ScoringType>(Arrays.asList(ScoringType.values())));
    heatMapConfiguration.setNumericFormat(new UISelectOneBean<NumberFormat>(NUMBER_FORMATS) {
      protected String getLabel(NumberFormat t) { return t.format(SAMPLE_NUMBER); } 
    });
    heatMapConfiguration.setExcludedWellFilters(new UISelectManyBean<Filter<ResultValue>>(EXCLUDED_WELL_FILTERS));
    _heatMapConfigurations.add(heatMapConfiguration);
    _heatMapConfigurationsDataModel = new ListDataModel(_heatMapConfigurations);

    // set default values
    heatMapConfiguration.getDataHeaders().setValue(Integer.toString(_screenResult.getResultValueTypesList().get(0).hashCode()));
    heatMapConfiguration.getExcludedWellFilters().setValue(Arrays.asList(new String[] { 
      (String) heatMapConfiguration.getExcludedWellFilters().getSelectItems().get(0).getValue() }));
    
    return update();
  }
  
  public String deleteHeatMap()
  {
    int heatMapIndexToDelete = ((Integer) getHttpServletRequest().getAttribute("heatMapIndex")).intValue();
    _heatMapConfigurations.remove(heatMapIndexToDelete);
    _heatMapColumnDataModels.remove(heatMapIndexToDelete);
    _heatMapDataModels.remove(heatMapIndexToDelete);
    _heatMaps.remove(heatMapIndexToDelete);
    _heatMapStatistics.remove(heatMapIndexToDelete);
    _heatMapConfigurationsDataModel = new ListDataModel(_heatMapConfigurations);
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  public String viewWell()
  {
    HeatMapCell heatMapCell = getHeatMapCell();
    Map<String, Object> wellProps = new HashMap<String,Object>();
    wellProps.put("plateNumber", heatMapCell.getWellKey().getPlateNumber());
    wellProps.put("name", heatMapCell.getWellKey().getWellName());
    Well well = _dao.findEntityByProperties(Well.class, wellProps);
    return _librariesController.viewWell(well, null);
  }
  
  public String nextPlate()
  {
    return gotoPlate(_plateNumber.getSelectionIndex() + 1);
  }

  public String previousPlate()
  {
    return gotoPlate(_plateNumber.getSelectionIndex() - 1);
  }
  

  // private methods

  private String gotoPlate(int plateIndex)
  {
    plateIndex = Math.max(0,
                          Math.min(_plateNumber.getSelectItems().size() - 1,
                                   plateIndex));
    _plateNumber.setSelectionIndex(plateIndex);
    return update();
  }
  
}
