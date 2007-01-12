// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.analysis.heatmaps;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import edu.harvard.med.screensaver.analysis.AggregateFunction;
import edu.harvard.med.screensaver.analysis.Filter;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.util.Pair;

import org.apache.commons.math.stat.descriptive.DescriptiveStatisticsImpl;
import org.apache.commons.math.stat.descriptive.rank.Median;
import org.apache.commons.math.util.ResizableDoubleArray;
import org.apache.log4j.Logger;

/**
 * A heat map implementation for an array of ResultValues.
 * Scope-of-responsibility:
 * <ul>
 * <li>applies a scoring function to a filtered subset of the array
 * <li>calculates statistics for the filtered subset
 * <li>provides access to a particular value and that value's associated color,
 * given an array coordinate
 * </ul>
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
// TODO: cache converted numeric values
public class HeatMap
{
  // static members

  private static Logger log = Logger.getLogger(HeatMap.class);
  

  // instance data members
  
  private Map<WellKey,ResultValue> _resultValues;
  private int _plateNumber;
  private ScalableColorFunction _scalableColorFunction;
  private AggregateFunction<Double> _scoringFunc;
  private DescriptiveStatisticsImpl _statistics;
  private double _median;


  // public constructors and methods
  
  /**
   * @param scoringFilter a filter that determines which ResultValues to
   *          exclude from scoring computations
   * @param scoringFunc the scoring function to be applied to
   *          each ResultValue when calculating its heat map color
   * @param colorFunction maps a range of continuous values to colors
   */
  public HeatMap(int plateNumber,
                 Map<WellKey,ResultValue> resultValues,
                 Filter<Pair<WellKey,ResultValue>> scoringFilter,
                 AggregateFunction<Double> scoringFunc,
                 ColorFunction colorFunction)
  {
    _resultValues = resultValues;
    _plateNumber = plateNumber;
    _scalableColorFunction = new ScalableColorFunction(colorFunction);
    _scoringFunc = scoringFunc;
    initialize(scoringFilter, scoringFunc);
  }

  public Color getColor(int row, int column)
  {
    return _scalableColorFunction.getColor(getScoredValue(row, column));
  }

  /**
   * @motivation for rendering color legends
   * @return
   */
  public ColorFunction getColorFunction()
  {
    return _scalableColorFunction;
  }
   
  public WellKey getWellKey(int row, int column)
  {
    return new WellKey(_plateNumber, row, column);
  }

  public ResultValue getResultValue(int row, int column)
  {
    return _resultValues.get(new WellKey(_plateNumber, row, column));
  }
  
  public double getRawValue(int row, int column)
  {
    ResultValue rv = getResultValue(row, column);
    if (rv == null) {
      return Double.NaN;
    }
    return rv.getNumericValue();
  }

  public double getScoredValue(int row, int column)
  {
    ResultValue rv = getResultValue(row, column);
    if (rv == null) {
      return Double.NaN;
    }
    return _scoringFunc.compute(getRawValue(row, column));
  }

  public int getCount()
  {
    return (int) _statistics.getN();
  }
  
  public double getMedian()
  {
    return _median;
  }
  
  public double getMax()
  {
    return _statistics.getMax();
  }

  public double getMin()
  {
    return _statistics.getMin();
  }
  
  public double getVariance()
  {
    return _statistics.getVariance();
  }

  public double getStandardDeviation()
  {
    return _statistics.getStandardDeviation();
  }
  
  public double getMean()
  {
    return _statistics.getMean();
  }

  public double getSkewness()
  {
    return _statistics.getSkewness();
  }

  public int getRowCount()
  {
    return (Well.MAX_WELL_ROW - Well.MIN_WELL_ROW) + 1;
  }

  public int getColumnCount()
  {
    return Well.MAX_WELL_COLUMN;
  }
 
  
  // private methods

  private void initialize(Filter<Pair<WellKey,ResultValue>> scoringFilter,
                          AggregateFunction<Double> scoringFunc)
  {

    Collection<Double> aggregationValues = new ArrayList<Double>();
    for (WellKey wellKey : _resultValues.keySet()) {
      if (wellKey.getPlateNumber() == _plateNumber) {
        ResultValue rv = getResultValue(wellKey.getRow(), wellKey.getColumn());
        if (rv != null && !scoringFilter.exclude(new Pair<WellKey,ResultValue>(wellKey, rv))) {
          aggregationValues.add(getRawValue(wellKey.getRow(), wellKey.getColumn()));
        }
      }
    }
    
    scoringFunc.initializeAggregates(aggregationValues);

    _statistics = new DescriptiveStatisticsImpl();
    ResizableDoubleArray medianValues = new ResizableDoubleArray();
    for (Double rawValue : aggregationValues) {
      double scoredValue = scoringFunc.compute(rawValue);
      _statistics.addValue(scoredValue);
      medianValues.addElement(scoredValue);
    }

    _median = new Median().evaluate(medianValues.getElements());

    _scalableColorFunction.setLowerLimit(_statistics.getMin());
    _scalableColorFunction.setUpperLimit(_statistics.getMax());
  }
  
}

