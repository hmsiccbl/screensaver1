// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.ToManyRelationship;
import edu.harvard.med.screensaver.model.users.AdministratorUser;

import org.apache.log4j.Logger;

/**
 * Tracks the activity of correcting the "known" volume of a set of wells on a
 * library copy plate, thereby reconciling differences between actual (physical)
 * volumes and the calculated volume, as determined by the cumulative
 * {@link WellVolumeAdjustment WellVolumeAdjustments}. Any difference, of
 * course, will always be due to inaccuracies or errors in lab operations,
 * rather than Screensaver's fault! :)
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @hibernate.joined-subclass table="well_volume_correction_activity"
 *                            lazy="true"
 * @hibernate.joined-subclass-key column="activity_id"
 */
public class WellVolumeCorrectionActivity extends AdministrativeActivity
{
  // static members

  private static final long serialVersionUID = 1L;

  private static Logger log = Logger.getLogger(WellVolumeCorrectionActivity.class);

  private Set<WellVolumeAdjustment> _wellVolumeAdjustments = new HashSet<WellVolumeAdjustment>();


  // instance data members


  // public constructors and methods

  public WellVolumeCorrectionActivity(AdministratorUser performedBy,
                                      Date datePerformed,
                                      Set<WellVolumeAdjustment> wellVolumeAdjustments)
  {
    super(performedBy, datePerformed);
    _wellVolumeAdjustments = wellVolumeAdjustments;
  }

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }

  /**
   * @hibernate.set
   *   cascade="all-delete-orphan"
   *   inverse="false"
   *   lazy="true"
   * @hibernate.collection-key
   *   column="well_volume_correction_activity_id"
   * @hibernate.collection-one-to-many
   *   class="edu.harvard.med.screensaver.model.libraries.WellVolumeAdjustment"
   * @motivation for hibernate and maintenance of bi-directional relationships
   */
  @ToManyRelationship(unidirectional=true,inverseProperty="wellVolumeAdjustments")
  public Set<WellVolumeAdjustment> getWellVolumeAdjustments()
  {
    return _wellVolumeAdjustments;
  }

  private void setWellVolumeAdjustments(Set<WellVolumeAdjustment> wellVolumeAdjustments)
  {
    _wellVolumeAdjustments = wellVolumeAdjustments;
  }

}
