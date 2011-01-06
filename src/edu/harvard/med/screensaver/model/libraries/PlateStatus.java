// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import edu.harvard.med.screensaver.model.VocabularyTerm;
import edu.harvard.med.screensaver.model.VocabularyUserType;

/**
 * The vocabulary of values for {@link Plate} status.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public enum PlateStatus implements VocabularyTerm
{
  /**
   * Initial plate status of plates upon creation.
   */
  NOT_SPECIFIED("Not specified"),
  /**
   * {@link Plate} is available for use (where "use" is determined by the {@link Plate} {@link Copy}'s
   * {@link CopyUsageType}; implies that plate has been created by the facility and is stored at the facility at a known
   * {@link Plate#getLocation() location}.
   */
  AVAILABLE("Available"),
  /**
   * {@link Plate} should no longer be used (where "use" is determined by the {@link Plate} {@link Copy}'s
   * {@link CopyUsageType}, but the plate is still stored at the facility at a known {@link Plate#getLocation()
   * location}.
   */
  RETIRED("Retired"),
  /**
   * {@link Plate} is no longer available for use as it is no longer stored at the facility; implies plate does not have
   * a {@link Plate#getLocation() location}.
   */
  GIVEN_AWAY("Given Away"),
  /**
   * {@link Plate} is no longer available for use as it has been discarded; implies plate does not have a
   * {@link Plate#getLocation() location}.
   */
  DISCARDED("Discarded"),
  /**
   * {@link Plate} is no longer available for use as it has been discarded, but its remaining reagent volume was first
   * transferred to another another {@link Plate}; implies plate does not have a {@link Plate#getLocation() location}
   */
  VOLUME_TRANSFERRED_AND_DISCARDED("Discarded (volume transferred)"),
  /**
   * The plate is no longer available for use, as its {@link Plate#getLocation() location} is no longer known.
   */
  LOST("Lost");

  /**
   * A Hibernate <code>UserType</code> to map the {@link PlateStatus} vocabulary.
   */
  public static class UserType extends VocabularyUserType<PlateStatus>
  {
    public UserType()
    {
      super(PlateStatus.values());
    }
  }


  private String _value;

  private PlateStatus(String value)
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
