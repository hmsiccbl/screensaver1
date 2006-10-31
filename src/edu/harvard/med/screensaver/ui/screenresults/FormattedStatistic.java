// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults;

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

