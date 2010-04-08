// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.util.List;

import com.google.common.collect.ImmutableList;

public enum ConcentrationUnit implements QuantityUnit<ConcentrationUnit>
{
  MOLAR("M", 0),
  MILLIMOLAR("mM", 3),
  MICROMOLAR("uM", 6),
  NANOMOLAR("nM", 9),
  PICOMOLAR("pM", 12);
  
  public static final ConcentrationUnit DEFAULT = MILLIMOLAR;
  public static final ConcentrationUnit NORMALIZED_UNITS = MOLAR;
  public static final List<ConcentrationUnit> DISPLAY_VALUES = ImmutableList.of(MILLIMOLAR,MICROMOLAR, NANOMOLAR, PICOMOLAR);    
  
  private String _symbol;
  private int _scale;

  private ConcentrationUnit(String symbol, int scale)
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
  
  public ConcentrationUnit[] getValues() 
  {
    return values();
  }

  public ConcentrationUnit getDefault()
  {
    return DEFAULT;
  }


}
