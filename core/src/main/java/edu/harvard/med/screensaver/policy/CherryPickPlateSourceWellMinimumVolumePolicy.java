// $HeadURL$
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.policy;

import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.service.cherrypicks.CherryPickRequestAllocator;

/**
 * Determines a minimum allowable volume for a cherry pick plate copy well.
 * This allows the {@link CherryPickRequestAllocator} to avoid allocating lab cherry picks from source wells that may
 * insufficient volume. The minimum allowable volume provides a buffer to account for discrepancies between calculated
 * and actual well volumes, since loss (or gain) of volume can occur in the real world due to evaporation (or
 * absorption).
 */
public interface CherryPickPlateSourceWellMinimumVolumePolicy
{
  Volume getMinimumVolumeAllowed(Well well);
}
