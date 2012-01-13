// $HeadURL$
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import edu.harvard.med.screensaver.DatabaseConnectionSettings;

public interface DatabaseConnectionSettingsResolver
{
  /**
   * @return null if the settings cannot be resolved using the method supported the resolver (returning null does not
   *         indicate an error, but rather the lack of support for a given resolution mechanism)
   * @throws DatabaseConnectionSettingsResolutionException if the resolved settings are invalid or incomplete
   */
  DatabaseConnectionSettings resolve() throws DatabaseConnectionSettingsResolutionException;
}
