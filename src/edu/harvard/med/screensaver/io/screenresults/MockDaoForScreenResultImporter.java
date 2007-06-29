// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/io/screenresults/MockDaoForScreenResultImporter.java $
// $Id: MockDaoForScreenResultImporter.java 1071 2007-02-14 14:26:14Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.screenresults;


import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.db.ScreenResultsDAO;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellType;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.ui.searchresults.SortDirection;

import org.apache.log4j.Logger;

/**
 * Enable invocation of ScreenResultParser in non-database environment.
 * 
 * @motivation for command-line ScreenResultImporter, when used in validation
 *             mode (i.e., non-import mode).
 * @motivation testing ScreenResultImporter
 * @author ant
 */
public class MockDaoForScreenResultImporter implements GenericEntityDAO, ScreenResultsDAO, LibrariesDAO 
{
  private static final Logger log = Logger.getLogger(MockDaoForScreenResultImporter.class);
  
  private static final String NAME_OF_PSEUDO_LIBRARY_FOR_IMPORT = "PseudoLibraryForScreenResultImport";

  private Library _library;

  private Screen _screen;

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
  
  public void deleteLibraryContents(Library library)
  {
  }

  public List<Library> findLibrariesDisplayedInLibrariesBrowser()
  {
    return null;
  }

  public SilencingReagent findSilencingReagent(Gene gene, SilencingReagentType silencingReagentType, String sequence)
  {
    return null;
  }

  public Set<Well> findWellsForPlate(int plate)
  {
    return null;
  }

  public void loadOrCreateWellsForLibrary(Library library)
  {
  }

  public <E extends AbstractEntity> E defineEntity(Class<E> entityClass, Object... constructorArguments)
  {
    return null;
  }

  public void deleteEntity(AbstractEntity entity)
  {
  }

  public void doInTransaction(DAOTransaction daoTransaction)
  {
    daoTransaction.runTransaction();
  }

  public <E extends AbstractEntity> List<E> findAllEntitiesOfType(Class<E> entityClass)
  {
    return null;
  }

  public <E extends AbstractEntity> List<E> findAllEntitiesOfType(Class<E> entityClass, boolean readOnly, String... relationships)
  {
    return null;
  }

  public <E extends AbstractEntity> List<E> findEntitiesByHql(Class<E> entityClass, String hql, Object... hqlParameters)
  {
    return null;
  }

  public <E extends AbstractEntity> List<E> findEntitiesByProperties(Class<E> entityClass, Map<String,Object> name2Value)
  {
    return null;
  }

  public <E extends AbstractEntity> List<E> findEntitiesByProperties(Class<E> entityClass, Map<String,Object> name2Value, boolean readOnly, String... relationshipsIn)
  {
    return null;
  }

  public <E extends AbstractEntity> List<E> findEntitiesByProperty(Class<E> entityClass, String propertyName, Object propertyValue)
  {
    return null;
  }

  public <E extends AbstractEntity> List<E> findEntitiesByProperty(Class<E> entityClass, String propertyName, Object propertyValue, boolean readOnly, String... relationships)
  {
    return null;
  }

  public <E extends AbstractEntity> E findEntityById(Class<E> entityClass, Serializable id)
  {
    return null;
  }

  public <E extends AbstractEntity> E findEntityById(Class<E> entityClass, Serializable id, boolean readOnly, String... relationships)
  {
    return null;
  }

  public <E extends AbstractEntity> E findEntityByProperties(Class<E> entityClass, Map<String,Object> name2Value)
  {
    return null;
  }

  public <E extends AbstractEntity> E findEntityByProperties(Class<E> entityClass, Map<String,Object> name2Value, boolean readOnly, String... relationships)
  {
    return null;
  }

  @SuppressWarnings("unchecked")
  public <E extends AbstractEntity> E findEntityByProperty(Class<E> entityClass, String propertyName, Object propertyValue)
  {
    if (entityClass.equals(Screen.class) && propertyName.equals("hbnScreenNumber")) {
      return (E) MakeDummyEntities.makeDummyScreen(((Integer) propertyValue));
    }
    return null;
  }

  public <E extends AbstractEntity> E findEntityByProperty(Class<E> entityClass, String propertyName, Object propertyValue, boolean readOnly, String... relationships)
  {
    return null;
  }

  public void flush()
  {
  }

  public void need(AbstractEntity entity, String... relationships)
  {
  }

  public void needReadOnly(AbstractEntity entity, String... relationships)
  {
  }

  public void persistEntity(AbstractEntity entity)
  {
  }

  public <E extends AbstractEntity> E reattachEntity(E entity)
  {
    return null;
  }

  public int relationshipSize(Object persistentCollection)
  {
    return 0;
  }

  public int relationshipSize(AbstractEntity entity, String relationship)
  {
    return 0;
  }

  public int relationshipSize(AbstractEntity entity, String relationship, String relationshipProperty, String relationshipPropertyValue)
  {
    return 0;
  }

  public <E extends AbstractEntity> E reloadEntity(E entity)
  {
    return null;
  }

  public <E extends AbstractEntity> E reloadEntity(E entity, boolean readOnly, String... relationships)
  {
    return null;
  }

  public void deleteScreenResult(ScreenResult screenResult)
  {
  }

  public Map<WellKey,ResultValue> findResultValuesByPlate(Integer plateNumber, ResultValueType rvt)
  {
    return null;
  }

  public Map<WellKey,List<ResultValue>> findResultValuesByPlate(Integer plateNumber, List<ResultValueType> rvt)
  {
    return null;
  }

  public Map<WellKey,List<ResultValue>> findSortedResultValueTableByRange(List<ResultValueType> selectedRvts,
                                                                          int sortBy,
                                                                          SortDirection sortDirection,
                                                                          int fromIndex,
                                                                          Integer rowsToFetch,
                                                                          ResultValueType hitsOnlyRvt,
                                                                          Integer plateNumber)
  {
    return null;
  }
}
