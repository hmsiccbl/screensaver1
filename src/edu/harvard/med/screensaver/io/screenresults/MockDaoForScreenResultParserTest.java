// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.screenresults;

import java.util.Map;

import edu.harvard.med.screensaver.db.NoOpDAO;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screens.ScreenType;

/**
 * Enable testing of ScreenResultParser (via ScreenResultParserTest), in mock
 * persistence environment.
 * 
 * @motivation speed of testing
 * @motivation libraries (and wells) are not yet loaded into our database, and
 *             don't want special logic in ScreenResultParser to handle this
 * @author ant
 */
public class MockDaoForScreenResultParserTest extends NoOpDAO
{
  
  private static final String NAME_OF_PSEUDO_LIBRARY_FOR_IMPORT = "PseudoLibraryForScreenResultImport";

  private Library _library;

  public MockDaoForScreenResultParserTest()
  {
    _library = 
      new Library(NAME_OF_PSEUDO_LIBRARY_FOR_IMPORT,
                  NAME_OF_PSEUDO_LIBRARY_FOR_IMPORT,
                  ScreenType.OTHER,
                  LibraryType.OTHER,
                  1,
                  9999);
  }
    


  @SuppressWarnings("unchecked")
  public <E extends AbstractEntity> E findEntityByProperties(Class<E> entityClass, Map<String,Object> name2Value)
  {
    if (Well.class.equals(entityClass)) {
      return (E) new Well(_library,
                          (Integer) name2Value.get("plateNumber"),
                          (String) name2Value.get("wellName"));
    }
    return null;
  }

  public Well findWell(Integer plateNumber, String wellName)
  {
    return new Well(_library, plateNumber, wellName);
  }
  
  public Library findLibraryWithPlate(Integer plateNumber)
  {
    return _library;
  }
}
