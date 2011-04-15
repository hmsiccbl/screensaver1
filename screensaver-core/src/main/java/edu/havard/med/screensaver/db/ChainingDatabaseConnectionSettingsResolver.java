// $HeadURL: $
// $Id: $
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.havard.med.screensaver.db;

import java.util.List;

import com.google.common.collect.ImmutableList;

import edu.harvard.med.screensaver.DatabaseConnectionSettings;
import edu.harvard.med.screensaver.db.DatabaseConnectionSettingsResolutionException;
import edu.harvard.med.screensaver.db.DatabaseConnectionSettingsResolver;

public class ChainingDatabaseConnectionSettingsResolver implements DatabaseConnectionSettingsResolver
{
  private List<DatabaseConnectionSettingsResolver> _resolvers;

  protected ChainingDatabaseConnectionSettingsResolver()
  {}

  protected ChainingDatabaseConnectionSettingsResolver(List<DatabaseConnectionSettingsResolver> resolvers)
  {
    _resolvers = ImmutableList.copyOf(resolvers);
  }

  @Override
  public DatabaseConnectionSettings resolve() throws DatabaseConnectionSettingsResolutionException
  {
    for (DatabaseConnectionSettingsResolver resolver : _resolvers) {
      DatabaseConnectionSettings settings = resolver.resolve();
      if (settings != null) {
        return settings;
      }
    }
    return null;
  }
}
