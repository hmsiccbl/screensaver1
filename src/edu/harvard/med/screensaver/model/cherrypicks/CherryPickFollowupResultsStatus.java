// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.cherrypicks;

import edu.harvard.med.screensaver.model.VocabularyTerm;
import edu.harvard.med.screensaver.model.VocabularyUserType;

public enum CherryPickFollowupResultsStatus implements VocabularyTerm 
{
  RECEIVED("Received"),
  NOT_RECEIVED("Not Received"),
  NOT_APPLICABLE("N/A");
  
  /**
   * A Hibernate <code>UserType</code> to map the {@link CherryPickFollowupResultsStatus} vocabulary.
   */
  public static class UserType extends VocabularyUserType<CherryPickFollowupResultsStatus>
  {
    public UserType()
    {
      super(CherryPickFollowupResultsStatus.values());
    }
  }


  private String _value;

  private CherryPickFollowupResultsStatus(String value)
  {
    _value = value;
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
}
