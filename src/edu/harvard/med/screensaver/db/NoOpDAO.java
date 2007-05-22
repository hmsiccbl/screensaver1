// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.CherryPickRequest;
import edu.harvard.med.screensaver.model.screens.LabCherryPick;
import edu.harvard.med.screensaver.model.screens.ScreenerCherryPick;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.ui.searchresults.SortDirection;

import org.apache.log4j.Logger;
import org.hibernate.collection.PersistentCollection;

public class NoOpDAO implements DAO
{
  // static members

  private static Logger log = Logger.getLogger(NoOpDAO.class);
  

  // public methods

  public <E extends AbstractEntity> E defineEntity(Class<E> entityClassController,
                                                   Object... constructorArgumentsController)
  {
    throw new UnsupportedOperationException();
  }

  public void deleteScreenResult(ScreenResult screenResultController)
  {
  }
  
  public void deleteLibraryContents(Library library)
  {
  }

  public void flush()
  {
  }

  public void doInTransaction(DAOTransaction daoTransaction)
  {
    // As a courtesy to the caller, we'll run it's callback code, even though
    // we're technically a "No Op" DAO.  We're just *too* nice...
    daoTransaction.runTransaction();
  }

  public void doInReadOnlyTransaction(DAOTransaction daoTransaction)
  {
    // As a courtesy to the caller, we'll run it's callback code, even though
    // we're technically a "No Op" DAO.  We're just *too* nice...
    daoTransaction.runTransaction();
  }

  public <E extends AbstractEntity> List<E> findAllEntitiesWithType(Class<E> entityClassController)
  {
    return null;
  }

  public SortedSet<ScreeningRoomUser> findAllLabHeads()
  {
    return null;
  }

  public SortedSet<ScreeningRoomUser> findCandidateCollaborators()
  {
    return null;
  }

  public <E extends AbstractEntity> List<E> findEntitiesByProperty(Class<E> entityClassController,
                                                                   String propertyNameController,
                                                                   Object propertyValueController)
  {
    return null;
  }

  public <E extends AbstractEntity> List<E> findEntitiesByPropertyPattern(Class<E> entityClassController,
                                                                          String propertyNameController,
                                                                          String propertyPatternController)
  {
    return null;
  }
  
  public <E extends AbstractEntity> List<E> findEntitiesByHql(Class<E> entityClass, String hql,
    Object [] hqlParameters)
  {
    return null;
  }
  
  public <E extends AbstractEntity> E findEntityById(Class<E> entityClassController,
                                                     Serializable idController)
  {
    return null;
  }

  public <E extends AbstractEntity> E findEntityByProperty(Class<E> entityClassController,
                                                           String propertyNameController,
                                                           Object propertyValueController)
  {
    return null;
  }

  public Library findLibraryWithPlate(Integer plateNumberController)
  {
    return null;
  }

  public SilencingReagent findSilencingReagent(Gene gene,
                                               SilencingReagentType silencingReagentType,
                                               String sequence)
  {
    return null;
  }

  public Well findWell(WellKey wellKey)
  {
    return null;
  }

  public <E extends AbstractEntity> E reattachEntity(E entity)
  {
    return null;
  }

  public void persistEntity(AbstractEntity entityController)
  {
  }
  
  public void deleteEntity(AbstractEntity entity)
  {
  }

  public Map<WellKey,List<ResultValue>> findSortedResultValueTableByRange(List<ResultValueType> selectedRvts,
                                                                          int sortBy,
                                                                          SortDirection sortDirection,
                                                                          int fromIndex,
                                                                          int toIndex,
                                                                          ResultValueType hitsOnlyRvt)
  {
    return null;
  }
  
  public Map<WellKey,ResultValue> findResultValuesByPlate(Integer plateNumber, ResultValueType rvt)
  {
    return null;
  }

  public Set<Well> findWellsForPlate(int plate)
  {
    return null;
  }

  public Set<LabCherryPick> findLabCherryPicksForWell(Well well)
  {
    return null;
  }

  public Set<ScreenerCherryPick> findScreenerCherryPicksForWell(Well well)
  {
    return null;
  }
  
  public void loadOrCreateWellsForLibrary(Library library)
  {
  }

  public List<Library> findLibrariesDisplayedInLibrariesBrowser()
  {
    return null;
  }
  
  public List<String> findDeveloperECommonsIds()
  {
    return null;
  }

  public void need(AbstractEntity entity, String... relationships)
  {
  }
  
  public void needReadOnly(AbstractEntity entity, String... relationships)
  {
  }
  
  public <T> List<T> needAll(Class<T> entityClass, String... relationships) 
  {
    return null;
  }

  public void need(AbstractEntity entity)
  {
  }

  public void need(PersistentCollection persistentCollection)
  {
  }

  public void need(Collection collection)
  {
  }

  public <E extends AbstractEntity> E reloadEntity(E entity)
  {
    return null;
  }

  public int relationshipSize(Object persistentCollection) 
  {
    return -1;
  }

  public int relationshipSize(AbstractEntity entity, String relationship)
  {
    return -1;
  }

  public int relationshipSize(final AbstractEntity entity, final String relationship,
    final String relationshipProperty, final String relationshipPropertyValue)
  {
    return -1;
  }

  public void deleteLabCherryPick(LabCherryPick cherryPick)
  {
  }

  public void deleteScreenerCherryPick(ScreenerCherryPick cherryPick)
  {
  }

  public void deleteCherryPickRequest(CherryPickRequest cherryPickRequest)
  {
    deleteCherryPickRequest(cherryPickRequest, false);
  }
  
  public void deleteCherryPickRequest(CherryPickRequest cherryPickRequest, boolean bypassBusinessRuleViolationChecks)
  {
  }

  public void deleteAllCherryPickRequests()
  {
  }

  public CherryPickRequest findCherryPickRequestByNumber(int cherryPickRequestNumber)
  {
    return null;
  }

  public <E extends AbstractEntity> List<E> findAllEntitiesWithType(Class<E> entityClass, boolean readOnly, String... relationships)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public <E extends AbstractEntity> List<E> findEntitiesByProperties(Class<E> entityClass, Map<String,Object> name2Value)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public <E extends AbstractEntity> List<E> findEntitiesByProperties(Class<E> entityClass, Map<String,Object> name2Value, boolean readOnly, String... relationships)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public <E extends AbstractEntity> List<E> findEntitiesByProperty(Class<E> entityClass, String propertyName, Object propertyValue, boolean readOnly, String... relationships)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public <E extends AbstractEntity> E findEntityById(Class<E> entityClass, Serializable id, boolean readOnly, String... relationships)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public <E extends AbstractEntity> E findEntityByProperties(Class<E> entityClass, Map<String,Object> name2Value)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public <E extends AbstractEntity> E findEntityByProperties(Class<E> entityClass, Map<String,Object> name2Value, boolean readOnly, String... relationships)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public <E extends AbstractEntity> E findEntityByProperty(Class<E> entityClass, String propertyName, Object propertyValue, boolean readOnly, String... relationships)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public <E extends AbstractEntity> E reloadEntity(E entity, boolean readOnly, String... relationships)
  {
    // TODO Auto-generated method stub
    return null;
  }
}
