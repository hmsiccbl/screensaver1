// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.math.BigDecimal;

public enum VolumeUnit implements QuantityUnit<VolumeUnit>
{
  LITERS("L", 0),
  MILLILITERS("mL", 3),
  MICROLITERS("uL", 6),
  NANOLITERS("nL", 9);

  public static VolumeUnit DEFAULT = MICROLITERS;
  public static VolumeUnit NORMALIZED_UNITS = LITERS;

  public static Volume ZERO = new Volume(new BigDecimal(0), VolumeUnit.DEFAULT);  //since it's immutable, consider lazy flyweight impl

  private String _symbol;
  private int _scale;

  public static VolumeUnit[] DISPLAY_VALUES = 
    new VolumeUnit[] { MILLILITERS,MICROLITERS,NANOLITERS };    
  
  private VolumeUnit(String symbol, int scale)
  {
    _symbol = symbol;
    _scale = scale;
  }

  public String getSymbol()
  {
    return _symbol;
  }

  public int getScale()
  {
    return _scale;
  }
  
  // VocabularyTerm methods
  /**
   * Get the value of the vocabulary term.
   * @return the value of the vocabulary term
   */
  public String getValue()
  {
    return getSymbol();
  }
  
  public String printAsVocabularyTerm()
  {
    return _symbol;
  }
  
  public String toString()
  {
    return printAsVocabularyTerm();
  }
  
  public VolumeUnit[] getValues() 
  {
    return values();
  }

  public VolumeUnit getDefault()
  {
    // TODO Auto-generated method stub
    return DEFAULT;
  }
 
}
