// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screenresults;

import edu.harvard.med.screensaver.model.VocabularyTerm;
import edu.harvard.med.screensaver.model.VocabularyUserType;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screens.ProjectPhase;
import edu.harvard.med.screensaver.model.screens.Screen;

/**
 * Vocabulary of {@link DataColumn} data types.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public enum DataType implements VocabularyTerm
{

  // the vocabulary
  
  /** {@link DataColumn} contains numeric data. */
  NUMERIC("Numeric", false),
  /** {@link DataColumn} contains textual data. */
  TEXT("Text", false),
  /**
   * {@link DataColumn} contains positive determinations data as true/false values, indicating whether a tested
   * {@link Well} {@link Reagent} has
   * been identified as having the desired biological activity in the screening assay.
   */
  POSITIVE_INDICATOR_BOOLEAN("Boolean Positive Indicator", true),
  /**
   * {@link DataColumn} contains positive determinations data as {@link PartitionedValue} values ("strong", "medium",
   * "weak", or "not positive"), indicating indicating whether a tested {@link Well} {@link Reagent} has
   * been identified as having the desired biological activity in the screening assay, and if so, with what strength.
   */
  POSITIVE_INDICATOR_PARTITION("Partition Positive Indicator", true),
  /**
   * {@link DataColumn} contains confirmed positive determinations data, indicating whether a
   * {@link ProjectPhase#FOLLOW_UP_SCREEN follow-up} screen has confirmed (reproduced) the original positive
   * determination of a related, {@link ProjectPhase#PRIMARY_SCREEN primary} {@link Screen}
   */
  CONFIRMED_POSITIVE_INDICATOR("Confirmed Positive Indicator", true)
  ;

 
  // static inner class

  /**
   * A Hibernate <code>UserType</code> to map the {@link DataType} vocabulary.
   */
  public static class UserType extends VocabularyUserType<DataType>
  {
    public UserType()
    {
      super(DataType.values());
    }
  }

  private String _value;
  private boolean _isPositiveIndicator;

  
  /**
   * Constructs a <code>DataType</code> vocabulary term.
   * @param value The value of the term.
   */
  private DataType(String value, boolean isPositiveIndicator)
  {
    _value = value;
    _isPositiveIndicator = isPositiveIndicator;
  }

  /**
   * Get the value of the vocabulary term.
   * @return the value of the vocabulary term
   */
  public String getValue()
  {
    return _value;
  }

  @Override
  public String toString()
  {
    return getValue();
  }


  public boolean isPositiveIndicator()
  {
    return _isPositiveIndicator;
  }
}
