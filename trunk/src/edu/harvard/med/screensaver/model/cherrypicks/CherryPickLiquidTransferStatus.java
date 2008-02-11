// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/model/screens/ScreenType.java $
// $Id: ScreenType.java 823 2006-12-08 17:48:54Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.cherrypicks;

import edu.harvard.med.screensaver.model.VocabularyTerm;
import edu.harvard.med.screensaver.model.VocabularyUserType;

/**
 * The vocabulary of statuses for CherryPickLiquidTransfers.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public enum CherryPickLiquidTransferStatus implements VocabularyTerm
{

  // the vocabulary
  
  SUCCESSFUL("Successful"),
  FAILED("Failed"),
  CANCELED("Canceled")
  ;

 
  // static inner class

  /**
   * A Hibernate <code>UserType</code> to map the {@link CherryPickLiquidTransferStatus} vocabulary.
   */
  public static class UserType extends VocabularyUserType<CherryPickLiquidTransferStatus>
  {
    public UserType()
    {
      super(CherryPickLiquidTransferStatus.values());
    }
  }


  // private instance field and constructor

  private String _value;

  /**
   * Constructs a <code>ScreenType</code> vocabulary term.
   * @param value The value of the term.
   */
  private CherryPickLiquidTransferStatus(String value)
  {
    _value = value;
  }


  // public instance methods

  /**
   * Get the value of the vocabulary term.
   * @return the value of the vocabulary term
   */
  public String getValue()
  {
    return _value;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    return getValue();
  }
}
