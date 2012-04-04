// $HeadURL: http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/serickson/3200/core/src/main/java/edu/harvard/med/screensaver/model/activities/ServiceActivityType.java $
// $Id: ServiceActivityType.java 7010 2012-02-02 02:34:34Z seanderickson1 $
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.cells;

import edu.harvard.med.screensaver.model.VocabularyTerm;
import edu.harvard.med.screensaver.model.VocabularyUserType;
/**
 * unused
 */
public enum CellLineType implements VocabularyTerm
{
  LINE("Cell Line"),
  PRIMARY("Primary");

  /**
   * A Hibernate <code>UserType</code> to map the {@link CellLineType} vocabulary.
   */
  public static class UserType extends VocabularyUserType<CellLineType>
  {
    public UserType()
    {
      super(CellLineType.values());
    }
  }

  private String _value;

  private CellLineType(String value)
  {
    _value = value;
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
}
