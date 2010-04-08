// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults.heatmaps;

import java.text.NumberFormat;

import org.apache.log4j.Logger;

public class FormattedStatistic
{
  // static members

  private static Logger log = Logger.getLogger(FormattedStatistic.class);


  // instance data members
  
  private String _name;
  private String _value;

  
  // public constructors and methods

  public FormattedStatistic(String name, int value)
  {
    _name = name;
    _value = Integer.toString(value);
  }

  public FormattedStatistic(String name, double value, NumberFormat numberFormat)
  {
    _name = name;
    _value = numberFormat.format(value);
  }

  public String getValue()
  {
    return _value;
  }

  public String getName()
  {
    return _name;
  }

  

  // private methods

}

