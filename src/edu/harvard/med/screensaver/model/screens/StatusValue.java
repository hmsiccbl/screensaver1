// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import edu.harvard.med.screensaver.model.VocabularyTerm;
import edu.harvard.med.screensaver.model.VocabularyUserType;

/**
 * The vocabulary of values for StatusItem. The ranking of these vocabularly
 * items is significant, as they represent a temporal sequence of events. In
 * particular the following ordering is critical:
 * <ul>
 * <li>Pending*</li>
 * <li>Piloted</li>
 * <li>Accepted</li>
 * <li>Ongoing</li>
 * <li>Completed*</li>
 * </ul>
 * Statuses after and including <code>Completed*</code> are mutually
 * exclusive, but always represent a final status. A screen may only contain one
 * status with a given <code>rank<code> value.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public enum StatusValue implements VocabularyTerm
{

  // the vocabulary

  PENDING_LEGACY("Pending - Legacy", 0), // for ScreenDB pending statuses, to differentiate from the two new pending statuses
  PENDING_ICCB("Pending - ICCB-L", 0),
  PENDING_NSRB("Pending - NSRB", 0),
  PILOTED("Piloting", 1),
  ACCEPTED("Accepted", 2),
  ONGOING("Ongoing", 3),
  COMPLETED("Completed", 4),
  COMPLETED_DUPLICATE_WITH_ONGOING("Completed - Duplicate with Ongoing", 4),
  NEVER_INITIATED("Never Initiated", 4),
  DROPPED_TECHNICAL("Dropped - Technical", 4),
  DROPPED_RESOURCES("Dropped - Resources", 4),
  TRANSFERRED_TO_BROAD_INSTITUTE("Transferred to Broad Institute", 5)
  ;


  // static inner class

  /**
   * A Hibernate <code>UserType</code> to map the {@link StatusValue} vocabulary.
   */
  public static class UserType extends VocabularyUserType<StatusValue>
  {
    public UserType()
    {
      super(StatusValue.values());
    }
  }


  // private instance field and constructor

  private String _value;
  private Integer _rank;

  /**
   * Constructs a <code>StatusValue</code> vocabulary term.
   * @param value The value of the term.
   */
  private StatusValue(String value, Integer rank)
  {
    _value = value;
    _rank = rank;
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


  public Integer getRank()
  {
    return _rank;
  }
}
