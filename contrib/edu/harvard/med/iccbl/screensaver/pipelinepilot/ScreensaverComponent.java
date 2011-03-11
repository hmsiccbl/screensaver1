// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.pipelinepilot;


public interface ScreensaverComponent
{

  public static final String PROPERTY_LIST_DELIMITER = "listDelimiter";
  public static final String PROPERTY_INPUT_FIELD = "inputField";
  public static final String PROPERTY_OUTPUT_FIELD = "outputField";
  public static final String PROPERTY_STANDARDIZED_SMILES = "standardized_smiles";

  /**
   * This flag, if set, indicates that the record has an error (i.e. missing SMILES...) 
   * that will cause it to always fail.  In this case we want to "fail fast".
   */
  public static final String PROPERTY_FAIL_FAST_FIELD = "fail_fast";
}