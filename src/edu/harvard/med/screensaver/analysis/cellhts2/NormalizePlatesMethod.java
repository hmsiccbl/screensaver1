// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.analysis.cellhts2;

import edu.harvard.med.screensaver.model.VocabularyTerm;
import edu.harvard.med.screensaver.model.VocabularyUserType;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;

// BII (Siew Cheng): Implement VocabularyTerm
public enum NormalizePlatesMethod implements VocabularyTerm
{
  // the vocabulary
	
  /* POC("POC"),
  NEGATIVES("negatives"),
  NPI("NPI"),*/
  MEAN("mean"), 
  MEDIAN("median"); 
 /* SHORTH("shorth"),
  BSCORE("Bscore"),
  LOCFIT("locfit"),
  LOESS("loess"); */
  
  // BII (Siew Cheng) start: implement VocabularyTerm
  // static inner class

  /**
   * A Hibernate <code>UserType</code> to map the {@link NormalizePlatesMethod} vocabulary.
   */
  public static class UserType extends VocabularyUserType<NormalizePlatesMethod>
  {
    public UserType()
    {
      super(NormalizePlatesMethod.values());
    }
  }

  // private instance field and constructor

  private String _value;

  /**
   * Constructs a <code>NormalizePlatesMethod</code> vocabulary term.
   * @param value The value of the term.
   */
  private NormalizePlatesMethod(String value)
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
  // BII end
}