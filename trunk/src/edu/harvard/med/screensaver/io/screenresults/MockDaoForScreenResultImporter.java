// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.screenresults;


import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.db.Query;
import edu.harvard.med.screensaver.db.ScreenResultsDAO;
import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
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
import edu.harvard.med.screensaver.ui.libraries.WellCopyVolume;

import org.apache.log4j.Logger;

/**
 * A DAO implementation that can be used in a database-free environment.
 *
 * @motivation for command-line ScreenResultImporter, when used in parse-only
 *             (non-import) mode.
 * @motivation testing ScreenResultImporter
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
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
    try {
      return _library.createWell(wellKey, WellType.EXPERIMENTAL);
    }
    catch (DuplicateEntityException e) {
      for (Well well : _library.getWells()) {
        if (well.getWellKey().equals(wellKey)) {
          return well;
        }
      }
      return null;
    }
  }

  public Well findWell(WellKey wellKey, boolean loadContents)
  {
    return findWell(wellKey);
  }


  public List<Well> findReagentWellsByVendorId(ReagentVendorIdentifier reagentVendorIdentifier)
  {
    return new ArrayList<Well>();
  }

  public Collection<String> findAllVendorNames()
  {
    return new ArrayList<String>();
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

  @SuppressWarnings("unchecked")
  public <E extends AbstractEntity> E findEntityByProperty(Class<E> entityClass, String propertyName, Object propertyValue)
  {
    if (entityClass.equals(Screen.class) && propertyName.equals("screenNumber")) {
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

  public void saveOrUpdateEntity(AbstractEntity entity)
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
                                                                          ResultValueType positivesOnlyRvt,
                                                                          Integer plateNumber)
  {
    return null;
  }

  public BigDecimal findRemainingVolumeInWellCopy(Well well, Copy copy)
  {
    return BigDecimal.ZERO.setScale(Well.VOLUME_SCALE);
  }

  public <E extends AbstractEntity> List<E> findEntitiesByProperties(Class<E> entityClass, Map<String,Object> name2Value)
  {
    return null;
  }

  public <E extends AbstractEntity> List<E> findEntitiesByProperties(Class<E> entityClass, Map<String,Object> name2Value, boolean readOnly, String... relationshipsIn)
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

  public List<WellCopyVolume> findWellCopyVolumes(Library library)
  {
    return null;
  }

  public List<WellCopyVolume> findWellCopyVolumes(Copy copy)
  {
    return null;
  }

  public List<WellCopyVolume> findWellCopyVolumes(Integer plateNumber)
  {
    return null;
  }

  public Collection<WellCopyVolume> findWellCopyVolumes(Copy copy, Integer plateNumber)
  {
    return null;
  }

  public Collection<WellCopyVolume> findWellCopyVolumes(WellKey wellKey)
  {
    return null;
  }

  public Collection<WellCopyVolume> findWellCopyVolumes(CherryPickRequest cherryPickRequest,
                                                        boolean forUnfufilledLabCherryPicksOnly)
  {
    return null;
  }

  public List<Library> findLibrariesOfType(LibraryType[] libraryTypes,
                                           ScreenType[] screenTypes)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean isPlateRangeAvailable(Integer startPlate, Integer endPlate)
  {
    return false;
  }

  public void clear() {}

  public <E> List<E> runQuery(Query query)
  {
    return null;
  }

}
