// $HeadURL$
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.io.IOException;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.datafetcher.TupleToKeyFunction;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.ui.CurrentScreensaverUser;
import edu.harvard.med.screensaver.ui.searchresults.WellSearchResults;

public class ReagentFinderTest extends AbstractSpringPersistenceTest
{
  protected ReagentFinder reagentFinder;
  protected WellSearchResults wellsBrowser;
  protected CurrentScreensaverUser currentScreensaverUser;

  private AdministratorUser _admin;
  private Library _library1;
  private Library _library2;

  protected void onSetUp() throws Exception
  {
    super.onSetUp();
    genericEntityDao.doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        _admin = new AdministratorUser("Admin", "User", "admin_user@hms.harvard.edu", "", "", "", "", "");
        currentScreensaverUser.setScreensaverUser(_admin);
        genericEntityDao.persistEntity(_admin);
        _library1 = MakeDummyEntities.makeDummyLibrary(1, ScreenType.SMALL_MOLECULE, 1);
        genericEntityDao.persistEntity(_library1);
        _library2 = MakeDummyEntities.makeDummyLibrary(2, ScreenType.SMALL_MOLECULE, 1);
        genericEntityDao.persistEntity(_library2);
      }
    });
  }

  public void testReagentFinder() throws IOException
  {
    reagentFinder.setReagentIdentifiers("\n sm1\n\nsm3 \nxxx\n");
    reagentFinder.findReagents();
    wellsBrowser.getRowCount();
    assertEquals(Sets.newHashSet("01000:A02", "02000:A02", "01000:A04", "02000:A04"),
                 Sets.newHashSet(Iterators.transform(wellsBrowser.getDataTableModel().iterator(), new TupleToKeyFunction<String>())));
  }
}
