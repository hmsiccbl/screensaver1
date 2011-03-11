// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.view;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.NoSuchEntityException;
import edu.harvard.med.screensaver.model.Entity;
import edu.harvard.med.screensaver.policy.EntityRestrictedException;
import edu.harvard.med.screensaver.ui.arch.view.aspects.UICommand;

public abstract class EntityViewerBackingBean<E extends Entity<?>> extends AbstractBackingBean implements EntityViewer<E>
{
  protected static Logger log = Logger.getLogger(EntityViewerBackingBean.class);
  
  private EntityViewerBackingBean<E> _thisProxy;

  private GenericEntityDAO _dao;
  private Class<E> _entityClass;  
 
  private E _entity;
  private String _viewerActionResult;
  
  protected EntityViewerBackingBean() {}
  
  protected EntityViewerBackingBean(EntityViewerBackingBean<E> thisProxy,
                                    Class<E> entityClass,
                                    String viewerActionResult,
                                    GenericEntityDAO dao)
  {
    _thisProxy = thisProxy;
    _entityClass = entityClass;
    _viewerActionResult = viewerActionResult;
    _dao = dao;
  }

  public EntityViewerBackingBean<E> getThisProxy()
  {
    if (_thisProxy == null) {
      log.warn("request for \"this proxy\" is returning non-proxied instance: " + this);
      return this;
    }
    return _thisProxy;
  }

  /**
   * Template method, called by {@link #setEntity(Entity)}. This method should
   * not be called directly by other code, use {@link #setEntity(Entity)}. This
   * method will be called within an active read-only transaction. The passed-in
   * entity may either be transient or managed by the current Hibernate session. The
   * entity argument will never be null. Implementations should only use the
   * passed-in entity argument, and should never call getEntity() (which would
   * return null).
   * 
   * @param entity
   */
  protected abstract void initializeViewer(E entity);

  /**
   * Subclasses should override this method to eager fetch relationships that
   * will be need when viewing the entity. It will be called within a transaction.
   */
  protected abstract void initializeEntity(E entity); 

  /*final*/ public E getEntity()
  {
    return _entity;
  }

  protected void _setEntity(E entity)
  {
    _entity = entity;
  }

  @Transactional
  /*final*/ public void setEntity(E entityIn)
  {
    E entity = null;
    if (entityIn == null || entityIn.isTransient()) {
      log.warn("tried to view a null or transient entity: " + entityIn);
      _entity = null;
      return;
    }
    entity = _dao.reloadEntity(entityIn, true);
    if (entity == null) { 
     throw new NoSuchEntityException(entityIn.getClass(), entityIn.getEntityId()); 
    }

    // TODO: implement as aspect
    if (entity.isRestricted()) {
      throw new EntityRestrictedException(entity);
    }
    
    
    _entity = null; // prevent initializeViewer() implementations from erroneously calling getEntity()
    initializeEntity(entity);
    initializeViewer(entity);
    // note: we don't store the entity unless initializeViewer() returns successfully; otherwise, we might expose an entity that should be restricted
    _entity = entity;
  }
  
  public Class<E> getEntityClass()
  {
    return _entityClass;
  }

  @UICommand
  /*final*/ public String view()
  {
    return _viewerActionResult;
  }
  
  @UICommand
  @Transactional
  /*final*/ public String viewEntity(E entity)
  {
    setEntity(entity);
    return view();
  }
  
  /**
   * View the entity identified in the 'entityId' request param
   */
  @UICommand
  @Transactional
  /*final*/ public String viewEntity()
  {
    String entityIdAsString = (String) getRequestParameter("entityId");
    Serializable entityId = convertEntityId(entityIdAsString);
    E entity = (E) _dao.findEntityById((Class<Entity>) (Class<?>) _entityClass, entityId);
    return _thisProxy.viewEntity(entity);
  }

  /**
   * Override this method if the entity type of this viewer has an ID that is not of type Integer
   */
  protected Serializable convertEntityId(String entityIdAsString)
  {
    return Integer.valueOf(entityIdAsString);
  }

  /*final*/ public String reload()
  {
    return viewEntity(_entity);
  }

  protected GenericEntityDAO getDao()
  {
    return _dao;
  }
}
