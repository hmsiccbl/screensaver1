// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.screens;

import edu.harvard.med.screensaver.model.VocabularyTerm;
import edu.harvard.med.screensaver.model.VocabularyUserType;

/**
 * The vocabulary of values for StatusItem. The ranking of these vocabulary
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
public enum ScreenStatus implements VocabularyTerm
{
  PENDING_LEGACY("Pending - Legacy", 0), // for ScreenDB pending statuses, to differentiate from the two new pending statuses
  PENDING_ICCB("Pending - ICCB-L", 0),
  PENDING_NSRB("Pending - NSRB", 0),
  PILOTED("Piloting", 1),
  ACCEPTED("Accepted", 2),
  ONGOING("Ongoing", 3),
  HOLD("Hold", 4), // note: this is mutually exclusive with other rank 4 statuses and is intended to be a transient status that will be deleted first once the screen proceeds on to another rank 4 status
  COMPLETED("Completed", 4),
  COMPLETED_DUPLICATE_WITH_ONGOING("Completed - Duplicate with Ongoing", 4),
  NEVER_INITIATED("Never Initiated", 4),
  DROPPED_TECHNICAL("Dropped - Technical", 4),
  DROPPED_RESOURCES("Dropped - Resources", 4),
  TRANSFERRED_TO_BROAD_INSTITUTE("Transferred to Broad Institute", 5),
  ARCHIVED("Archived", 5)
  ;


  /**
   * A Hibernate <code>UserType</code> to map the {@link ScreenStatus} vocabulary.
   */
  public static class UserType extends VocabularyUserType<ScreenStatus>
  {
    public UserType()
    {
      super(ScreenStatus.values());
    }
  }

  private String _value;
  private Integer _rank;

  private ScreenStatus(String value, Integer rank)
  {
    _value = value;
    _rank = rank;
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

  public Integer getRank()
  {
    return _rank;
  }
}
