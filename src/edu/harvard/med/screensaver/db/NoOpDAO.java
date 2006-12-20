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
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.ui.searchresults.SortDirection;

import org.apache.log4j.Logger;

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

  public void flush()
  {
  }

  public void doInTransaction(DAOTransaction daoTransactionController)
  {
    // As a courtesy to the caller, we'll run it's callback code, even though
    // we're technically a "No Op" DAO.  We're just *too* nice...
    daoTransactionController.runTransaction();
  }

  public <E extends AbstractEntity> List<E> findAllEntitiesWithType(Class<E> entityClassController)
  {
    return null;
  }

  public List<ScreeningRoomUser> findAllLabHeads()
  {
    return null;
  }

  public <E extends AbstractEntity> List<E> findEntitiesByProperties(Class<E> entityClassController,
                                                                     Map<String,Object> name2ValueController)
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

  public <E extends AbstractEntity> E findEntityById(Class<E> entityClassController,
                                                     Serializable idController)
  {
    return null;
  }

  public <E extends AbstractEntity> E findEntityByProperties(Class<E> entityClassController,
                                                             Map<String,Object> name2ValueController)
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

  public SilencingReagent findSilencingReagent(Gene geneController,
                                               SilencingReagentType silencingReagentTypeController,
                                               String sequenceController)
  {
    return null;
  }

  public Well findWell(Integer plateNumberController, String wellNameController)
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

  public void createWellsForLibrary(Library library)
  {
  }

  public List<Library> findLibrariesDisplayedInLibrariesBrowser()
  {
    return null;
  }
}
