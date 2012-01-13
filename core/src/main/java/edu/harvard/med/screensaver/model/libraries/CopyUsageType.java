// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import edu.harvard.med.screensaver.model.VocabularyTerm;
import edu.harvard.med.screensaver.model.VocabularyUserType;

/**
 * The copy usage type vocabulary.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public enum CopyUsageType implements VocabularyTerm
{
  /**
   * Vendor-provided library plates. There will be one set of master stock plates for a library.
   */
  MASTER_STOCK_PLATES("96 Stock Plates"), // ICCB-L staff prefers calling this "96 Stock Plates", since the master stock plates are always 96-well plates
  /**
   * Reformatted library plates, often in a different plate size than the master stock plates. There may be multiple
   * sets ("copies") of stock plates for a library.
   */
  STOCK_PLATES("Stock Plates"),
  /**
   * Plates used to directly create library screening plates (assay plates). There may be multiple
   * sets ("copies") of library screening plates for a library.
   */
  LIBRARY_SCREENING_PLATES("Library Screening Plates"),
  /**
   * Plates used to directly create cherry pick plates. There may be multiple
   * sets ("copies") of cherry pick source plates for a library.
   */
  CHERRY_PICK_SOURCE_PLATES("Cherry Pick Source Plates")
  ;

  /**
   * A Hibernate <code>UserType</code> to map the {@link CopyUsageType} vocabulary.
   */
  public static class UserType extends VocabularyUserType<CopyUsageType>
  {
    public UserType()
    {
      super(CopyUsageType.values());
    }
  }

  private String _value;

  /**
   * Constructs a <code>CopyUsageType</code> vocabulary term.
   * @param value The value of the term.
   */
  private CopyUsageType(String value)
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
