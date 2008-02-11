// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries;

/**
 * The type of data row being parsed. Is it empty? Does it have just plate-well? Is it
 * a full-fledged data row?
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public enum DataRowType {
  
  /**
   * An essentially empty data row, probably empty for formatting purposes. All required
   * columns are empty. 
   */
  EMPTY,
  
  /**
   * A data row that does not have any significant data in it besides plate-well
   * information. Probably a control well. All required columns besides Plate and Well are
   * empty. 
   */
  PLATE_WELL_ONLY,
  
  /**
   * While this data row contains data, it is not necessarily a well-formatted row. Some
   * required columns are populated, but not just Plate and Well. Essentially, this is a
   * catch-all for rows that are not {@link #EMPTY empty} or {@link #PLATE_WELL_ONLY
   * plate-well only}.  
   */
  NON_EMPTY;
}
