// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults.heatmaps;

import java.awt.Color;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.db.ScreenResultsDAO;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.EntityViewer;
import edu.harvard.med.screensaver.ui.libraries.WellViewer;
import edu.harvard.med.screensaver.ui.util.UISelectManyBean;
import edu.harvard.med.screensaver.ui.util.UISelectOneBean;
import edu.harvard.med.screensaver.util.Pair;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class HeatMapViewer extends AbstractBackingBean implements EntityViewer
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
  private static final Collection<Filter<Pair<WellKey,ResultValue>>> EXCLUDED_WELL_FILTERS = new ArrayList<Filter<Pair<WellKey,ResultValue>>>();
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
  private static final Filter<Pair<WellKey,ResultValue>> IMPLICIT_FILTER = new ExcludedOrNonDataProducingWellFilter();
  private static final Double SAMPLE_NUMBER = new Double(-1234.567);
  private static final NumberFormat DECIMAL_FORMAT = new DecimalFormat("0.0##");
  private static final int COLOR_LEGEND_GRADIENT_STEPS = 10;
  private static final DataModel HEAT_MAP_CELL_LEGEND_MODEL;
  static {
    List<LegendItem> legendItems = new ArrayList<LegendItem>();
    legendItems.add(new LegendItem("Experimental", HeatMapCell.getStyle(true, false, true, false, Color.GREEN)));
    legendItems.add(new LegendItem("Control", HeatMapCell.getStyle(true, false, false, true, Color.BLUE)));
    legendItems.add(new LegendItem("Empty/Excluded", HeatMapCell.getStyle(true, true, false, false, Color.WHITE)));
    HEAT_MAP_CELL_LEGEND_MODEL = new ListDataModel(legendItems);
  }


  private static Logger log = Logger.getLogger(HeatMapViewer.class);


  // instance data members

  private GenericEntityDAO _dao;
  private ScreenResultsDAO _screenResultsDao;
  private LibrariesDAO _librariesDao;
  private WellViewer _wellViewer;

  private ScreenResult _screenResult;
  private UISelectOneBean<Integer> _plateNumber;
  private ArrayList<HeatMap> _heatMaps;
  private boolean _showValues;
  private List<HeatMapConfiguration> _heatMapConfigurations;
  private DataModel _heatMapConfigurationsDataModel;
  private List<DataModel> _heatMapDataModels;
  private List<DataModel> _heatMapStatistics;
  private List<DataModel> _heatMapColumnDataModels;
  private List<String> _heatMapRowLabels;
  private boolean _updateNeeded = true;


  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected HeatMapViewer()
  {
  }

  public HeatMapViewer(GenericEntityDAO dao,
                       ScreenResultsDAO screenResultsDao,
                       LibrariesDAO librariesDao,
                       WellViewer wellViewer)
  {
    _dao = dao;
    _screenResultsDao = screenResultsDao;
    _librariesDao = librariesDao;
    _wellViewer = wellViewer;
    resetView(); // basically, initialize collections
  }


  // bean property methods

  public AbstractEntity getEntity()
  {
    return getScreenResult();
  }

  public void setScreenResult(ScreenResult screenResult)
  {
    _screenResult = screenResult;
    if (_screenResult != null) {
      resetView();
      _plateNumber.setDomain(_screenResult.getPlateNumbers());      
      addHeatMap();
    }
  }

  private void resetView()
  {
    _plateNumber = new UISelectOneBean<Integer>();
    _heatMaps = new ArrayList<HeatMap>();
    _heatMapConfigurations = new ArrayList<HeatMapConfiguration>();
    _heatMapConfigurationsDataModel = null;
    _heatMapDataModels = null;
    _heatMapStatistics = null;
    _heatMapColumnDataModels = null;
    _heatMapRowLabels = null;
    _updateNeeded = true;
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

  public DataModel getCellTypeLegendDataModel()
  {
    return HEAT_MAP_CELL_LEGEND_MODEL;
  }

  public List<DataModel> getHeatMapDataModels()
  {
    doLazyUpdate();
    return _heatMapDataModels;
  }

  public List<DataModel> getHeatMapColumnDataModels()
  {
    doLazyUpdate();
    return _heatMapColumnDataModels;
  }

  public DataModel getColorLegendDataModel()
  {
    doLazyUpdate();
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
    doLazyUpdate();
    return _heatMaps;
  }

  public List<DataModel> getHeatMapStatisticsDataModels()
  {
    doLazyUpdate();
    return _heatMapStatistics;
  }

  public DataModel getHeatMapConfigurationsDataModel()
  {
    doLazyUpdate();
    return _heatMapConfigurationsDataModel;
  }

  @SuppressWarnings("unchecked")
  public HeatMapCell getHeatMapCell()
  {
    doLazyUpdate();
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
    doLazyUpdate();
    int heatMapIndex = _heatMapConfigurationsDataModel.getRowIndex();
    HeatMapConfiguration heatMapConfiguration = _heatMapConfigurations.get(heatMapIndex);
    StringBuilder title = new StringBuilder();
    title.append(heatMapConfiguration.getDataHeaders().getSelection().getName());
    title.append(": ");
    title.append(heatMapConfiguration.getScoringType().getSelection().toString());
    List<Filter<Pair<WellKey,ResultValue>>> filterSelections = heatMapConfiguration.getExcludedWellFilters().getSelections();
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
    _updateNeeded = true;
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  private void doLazyUpdate()
  {
    if (_updateNeeded) {
      _updateNeeded = false;
      _heatMapDataModels = new ArrayList<DataModel>();
      _heatMapColumnDataModels = new ArrayList<DataModel>();
      _heatMaps = new ArrayList<HeatMap>();
      _heatMapStatistics = new ArrayList<DataModel>();
      for (HeatMapConfiguration heatMapConfig : _heatMapConfigurations) {
        if (heatMapConfig.getDataHeaders().getSelection() != null &&
          _plateNumber.getSelection() != null) {
          Map<WellKey,ResultValue> resultValues =
            _screenResultsDao.findResultValuesByPlate(_plateNumber.getSelection(),
                                                      heatMapConfig.getDataHeaders().getSelection());
          HeatMap heatMap = new HeatMap(_plateNumber.getSelection(),
                                        resultValues,
                                        new ChainedFilter<Pair<WellKey,ResultValue>>(
                                          IMPLICIT_FILTER,
                                          new ChainedFilter<Pair<WellKey,ResultValue>>(heatMapConfig.getExcludedWellFilters().getSelections())),
                                          heatMapConfig.getScoringType().getSelection().getFunction(),
                                          new DefaultMultiColorGradient());

          NumberFormat format = heatMapConfig.getNumericFormat().getSelection();
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
      }
      _heatMapConfigurationsDataModel = new ListDataModel(_heatMapConfigurations);
    }
  }
  
  // TODO: set initial values to previous HeatMapConfig
  public String addHeatMap()
  {
    if (_screenResult.getNumericResultValueTypes().size() == 0) {
      return REDISPLAY_PAGE_ACTION_RESULT;
    }

    HeatMapConfiguration heatMapConfiguration = new HeatMapConfiguration();
    heatMapConfiguration.setDataHeaders(new UISelectOneBean<ResultValueType>(_screenResult.getNumericResultValueTypes()) {
      protected String getLabel(ResultValueType t) { return t.getName(); }
    });
    heatMapConfiguration.setScoringType(new UISelectOneBean<ScoringType>(Arrays.asList(ScoringType.values())));
    heatMapConfiguration.setNumericFormat(new UISelectOneBean<NumberFormat>(NUMBER_FORMATS) {
      protected String getLabel(NumberFormat t) { return t.format(SAMPLE_NUMBER); }
    });
    heatMapConfiguration.setExcludedWellFilters(new UISelectManyBean<Filter<Pair<WellKey,ResultValue>>>(EXCLUDED_WELL_FILTERS));
    _heatMapConfigurations.add(heatMapConfiguration);

    // set default values
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
    Well well = _librariesDao.findWell(heatMapCell.getWellKey());
    return _wellViewer.viewWell(well);
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
