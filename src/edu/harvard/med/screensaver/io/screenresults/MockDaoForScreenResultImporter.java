// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/io/screenresults/MockDaoForScreenResultImporter.java $
// $Id: MockDaoForScreenResultImporter.java 1071 2007-02-14 14:26:14Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.screenresults;

import java.util.Date;

import edu.harvard.med.screensaver.db.NoOpDAO;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellType;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUserClassification;

import org.apache.log4j.Logger;

/**
 * Enable invocation of ScreenResultParser in non-database environment.
 * 
 * @motivation for command-line ScreenResultImporter, when used in validation
 *             mode (i.e., non-import mode).
 * @motivation testing ScreenResultImporter
 * @author ant
 */
public class MockDaoForScreenResultImporter extends NoOpDAO
{
  private static final Logger log = Logger.getLogger(MockDaoForScreenResultImporter.class);
  
  private static final String NAME_OF_PSEUDO_LIBRARY_FOR_IMPORT = "PseudoLibraryForScreenResultImport";

  private Library _library;

  public MockDaoForScreenResultImporter()
  {
    _library = 
      new Library(NAME_OF_PSEUDO_LIBRARY_FOR_IMPORT,
                  NAME_OF_PSEUDO_LIBRARY_FOR_IMPORT,
                  ScreenType.OTHER,
                  LibraryType.OTHER,
                  1,
                  Integer.MAX_VALUE);
  }

  public Well findWell(WellKey wellKey)
  {
    return new Well(_library, wellKey, WellType.EXPERIMENTAL);
  }
  
  public Library findLibraryWithPlate(Integer plateNumber)
  {
    return _library;
  }
  
  @SuppressWarnings("unchecked")
  public <E extends AbstractEntity> E findEntityByProperty(Class<E> entityClass,
                                                           String propertyName,
                                                           Object propertyValue) 
  {
    if (entityClass.equals(Screen.class) && 
      propertyName.equals("hbnScreenNumber") && 
      propertyValue instanceof Integer) {
      return (E) makeDummyScreen((Integer) propertyValue);
    }
    throw new UnsupportedOperationException("mock implementation of findEntityByProperty() cannot handle request for a "
                                            + entityClass.getSimpleName()
                                            + " with property " + propertyName + "=" + propertyValue);
  }

  
  public static ScreeningRoomUser makeDummyUser(int screenNumber, String first, String last)
  {
    return new ScreeningRoomUser(new Date(),
                                 first,
                                 last,
                                 first.toLowerCase() + "_" + last.toLowerCase() + "_" + screenNumber + "@hms.harvard.edu",
                                 "",
                                 "",
                                 "",
                                 "",
                                 "",
                                 ScreeningRoomUserClassification.ICCBL_NSRB_STAFF,
                                 true);
  }

  public static Screen makeDummyScreen(int screenNumber)
  {
    ScreeningRoomUser labHead = MockDaoForScreenResultImporter.makeDummyUser(screenNumber, "Joe", "Screener");
    Screen screen = new Screen(labHead,
                               labHead,
                               screenNumber,
                               new Date(),
                               ScreenType.SMALL_MOLECULE,
                               "Dummy screen");
    return screen;
  }

}
