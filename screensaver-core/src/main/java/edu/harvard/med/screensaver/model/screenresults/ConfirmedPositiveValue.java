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

/**
 * Vocabulary for {@link ResultValue#getConfirmedPositiveValue()} when parent's {@link DataColumn#getDataType()()} is
 * {@link edu.harvard.med.screensaver.model.screenresults.DataType#CONFIRMED_POSITIVE_INDICATOR}
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public enum ConfirmedPositiveValue implements VocabularyTerm
{
  NOT_TESTED("NT", "0"),
  INCONCLUSIVE("I", "1"),
  FALSE_POSITIVE("FP", "2"), // TODO: Sean doesn't like this name, it seems overloaded...
  CONFIRMED_POSITIVE("CP", "3");
  
  private String _value;
  private String _storageValue;

  private ConfirmedPositiveValue(String value, String storageValue)
  {
    _value = value;
    _storageValue = storageValue;
  }

  public String getValue()
  {
    return _value;
  }
  
  @Override
  public String toString()
  {
    return getValue();
  }

  public String toStorageValue()
  {
    return _storageValue;
  }
}
