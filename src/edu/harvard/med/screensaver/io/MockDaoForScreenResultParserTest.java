// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io;

import java.util.List;
import java.util.Map;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.Well;

/**
 * Enable testing of ScreenResultParser (via ScreenResultParserTest), in mock
 * persistence environment.
 * 
 * @motivation speed of testing
 * @motivation libraries (and wells) are not yet loaded into our database, and
 *             don't want special logic in ScreenResultParser to handle this
 * @author ant
 */
public class MockDaoForScreenResultParserTest implements DAO
{
  
  private static final String NAME_OF_PSEUDO_LIBRARY_FOR_IMPORT = "PseudoLibraryForScreenResultImport";

  private Library _library;

  public MockDaoForScreenResultParserTest()
  {
    _library = 
      new Library(NAME_OF_PSEUDO_LIBRARY_FOR_IMPORT,
                  NAME_OF_PSEUDO_LIBRARY_FOR_IMPORT,
                  LibraryType.OTHER,
                  1,
                  9999);
  }
    

  public void doInTransaction(DAOTransaction daoTransaction)
  {
  }

  public <E extends AbstractEntity> E defineEntity(Class<E> entityClass, Object... constructorArguments)
  {
    return null;
  }

  public void persistEntity(AbstractEntity entity)
  {
  }

  public <E extends AbstractEntity> List<E> findAllEntitiesWithType(Class<E> entityClass)
  {
    return null;
  }

  public <E extends AbstractEntity> E findEntityById(Class<E> entityClass, Integer id)
  {
    return null;
  }

  public <E extends AbstractEntity> List<E> findEntitiesByProperties(Class<E> entityClass, Map<String,Object> name2Value)
  {
    return null;
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

  public <E extends AbstractEntity> List<E> findEntitiesByProperty(Class<E> entityClass, String propertyName, Object propertyValue)
  {
    return null;
  }

  public <E extends AbstractEntity> E findEntityByProperty(Class<E> entityClass, String propertyName, Object propertyValue)
  {
    return null;
  }

  public <E extends AbstractEntity> List<E> findEntitiesByPropertyPattern(Class<E> entityClass, String propertyName, String propertyPattern)
  {
    return null;
  }

  public <E extends AbstractEntity> Integer getEntityId(E entity)
  {
    return null;
  }
}
