// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.io.screens.rnaiglobal;

import jxl.Cell;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;

interface AnnotationValueBuilder
{
  /**
   * This value should match the value in our database for Dharmacon libraries.
   */
  public static final String DHARMACON_VENDOR_NAME = "Dharmacon";

  public void addAnnotationValue(Cell[] row);

  public AnnotationType getAnnotationType();
}
