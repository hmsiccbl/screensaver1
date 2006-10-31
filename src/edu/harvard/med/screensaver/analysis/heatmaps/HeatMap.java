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

import edu.harvard.med.screensaver.analysis.Filter;
import edu.harvard.med.screensaver.analysis.AggregateFunction;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;

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
public class HeatMap
{
  // static members

  private static Logger log = Logger.getLogger(HeatMap.class);
  

  // instance data members
  
  private ResultValueType _resultValueType;
  private int _plateNumber;
  private ResultValue[][] _data;
  private double[][] _rawValues;
  private double[][] _scoredValues;
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
  public HeatMap(ResultValueType rvt,
                 int plateNumber,
                 Filter<ResultValue> scoringFilter,
                 AggregateFunction<Double> scoringFunc,
                 ColorFunction colorFunction)
  {
    _resultValueType = rvt;
    _plateNumber = plateNumber;
    _scalableColorFunction = new ScalableColorFunction(colorFunction);
    _scoringFunc = scoringFunc;
    initialize(scoringFilter, scoringFunc);
  }

  public Color getColor(int row, int column)
  {
    return _scalableColorFunction.getColor(getScoredValue(row, column));
  }
   
  public double getRawValue(int row, int column)
  {
    return _rawValues[row][column];
  }

  public double getScoredValue(int row, int column)
  {
    return _scoringFunc.compute(_rawValues[row][column]);
  }

  public ResultValue getResultValue(int row, int column)
  {
    return _data[row][column];
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
    return _data.length;
  }

  public int getColumnCount()
  {
    return _data[0].length;
  }
  

  // private methods

  private void initialize(Filter<ResultValue> scoringFilter,
                          AggregateFunction<Double> scoringFunc)
  {
    // TODO: get the plate extents from somewhere else!
    _data = new ResultValue['P' - 'A' + 1][24];
    _rawValues = new double[_data.length][_data[0].length];
    Collection<Double> aggregationValues = new ArrayList<Double>();
    for (ResultValue rv : _resultValueType.getResultValues()) {
      if (rv.getWell().getPlateNumber() == _plateNumber) {
        int row = rv.getWell().getRow();
        int col = rv.getWell().getColumn();
        _data[row][col] = rv;
        double rawValue = Double.parseDouble(rv.getValue());
        _rawValues[row][col] = rawValue;
        if (!scoringFilter.exclude(rv)) {
          aggregationValues.add(rawValue);
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

