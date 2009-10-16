//$HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $

package edu.harvard.med.screensaver.analysis.cellhts2;

import edu.harvard.med.screensaver.model.VocabularyTerm;
import edu.harvard.med.screensaver.model.VocabularyUserType;

public enum NormalizePlatesNegControls implements VocabularyTerm
{
  NEG("N"),
  NEG_SHARED("S"); // used as negative control to normalize over plates with different conditions

  /**
   * A Hibernate <code>UserType</code> to map the {@link NormalizePlatesMethod} vocabulary.
   */
  public static class UserType extends VocabularyUserType<NormalizePlatesNegControls>
  {
    public UserType()
    {
      super(NormalizePlatesNegControls.values());
    }
  }

  // private instance field and constructor

  private String _value;

  /**
   * Constructs a <code>NormalizePlatesMethod</code> vocabulary term.
   * @param value The value of the term.
   */
  private NormalizePlatesNegControls(String value)
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
