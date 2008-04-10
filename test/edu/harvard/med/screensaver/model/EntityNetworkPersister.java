// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;

/**
 * Traverses an entity network, persisting entities in an appropriate order, so that all
 * entities in the network persist without persistence errors. Particular care is taken
 * to consider cascading and non-cascading relationships. Entities that are reached by a
 * cascade of another entity are not explicitly persisted. Entities that are reached by a
 * non-cascading relationship must be persisted first, to avoid a "null or transient"
 * persistence error.
 *
 * <p>
 *
 * I'll have to think this over a bit, but bi-directional cascades seem like a model error.
 *
 * @author John Sullivan
 */
public class EntityNetworkPersister
{
  // static members

  private static Logger log = Logger.getLogger(EntityNetworkPersister.class);


  // instance data members

  private GenericEntityDAO _genericEntityDAO;
  private AbstractEntity _rootEntity;
  private Set<AbstractEntity> _visitedEntities = new HashSet<AbstractEntity>();
  private EntityNetworkPersisterException _entityNetworkPersisterException;
  public class EntityNetworkPersisterException extends Exception {
    private static final long serialVersionUID = 1L;
    public EntityNetworkPersisterException(Exception inner) { super(inner); }
    public EntityNetworkPersisterException(String message) { super(message); }
  };


  // public constructor

  /**
   * Construct an <code>EntityNetworkPersister</code>.
   * @param genericEntityDAO the dao used to persist the individual entities
   * @param rootEntity the root entity of the entity network to be persisted
   */
  public EntityNetworkPersister(GenericEntityDAO genericEntityDAO, AbstractEntity rootEntity)
  {
    _genericEntityDAO = genericEntityDAO;
    _rootEntity = rootEntity;
  }


  // public instance method

  /**
   * Persist the entity network.
   * @throws EntityNetworkPersisterException
   */
  public void persistEntityNetwork() throws EntityNetworkPersisterException
  {
    log.debug("persistEntityNetwork: start transaction");
    _genericEntityDAO.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        try {
          persistEntityNetwork(_rootEntity);
        }
        catch (EntityNetworkPersisterException e) {
          _entityNetworkPersisterException = e;
        }
      }
    });
    log.debug("persistEntityNetwork: transaction complete");
    if (_entityNetworkPersisterException != null) {
      log.debug("persistEntityNetwork: exception thrown: " + _entityNetworkPersisterException);
      throw _entityNetworkPersisterException;
    }
  }


  // private instance method and inner class

  private void persistEntityNetwork(AbstractEntity entity) throws EntityNetworkPersisterException
  {
    if (_visitedEntities.contains(entity)) {
      return;
    }
    _visitedEntities.add(entity);
    new EntityPersister(entity).persistEntity();
  }

  private class EntityPersister
  {
    private AbstractEntity _entity;
    private Set<AbstractEntity> _downstreamRelations;
    private AbstractEntity _cascadingUpstreamRelation;
    private Set<AbstractEntity> _upstreamRelations;

    public EntityPersister(AbstractEntity entity) throws EntityNetworkPersisterException
    {
      _entity = entity;
      initialize();
    }

    public void persistEntity() throws EntityNetworkPersisterException
    {
      log.debug("persistEntity: begin persisting " + _entity);
      for (AbstractEntity downstreamRelation  : _downstreamRelations) {
        log.debug("persistEntity: persist downstream relation: " + downstreamRelation);
        persistEntityNetwork(downstreamRelation);
        log.debug("persistEntity: persist downstream relation complete: " + downstreamRelation);
      }
      if (_cascadingUpstreamRelation != null) {
        log.debug("persistEntity: persist cascading upstream relation: " + _cascadingUpstreamRelation);
    	  persistEntityNetwork(_cascadingUpstreamRelation);
      }
      else {
        log.debug("persistEntity: saveOrUpdateEntity: " + _entity);
        _genericEntityDAO.saveOrUpdateEntity(_entity);
      }
      for (AbstractEntity upstreamRelation : _upstreamRelations) {
        log.debug("persistEntity: persist upstream relation: " + upstreamRelation);
        persistEntityNetwork(upstreamRelation);
      }
      log.debug("persistEntity: done persisting " + _entity);
    }

    private void initialize() throws EntityNetworkPersisterException
    {
      _downstreamRelations = new HashSet<AbstractEntity>();
      _cascadingUpstreamRelation = null;
      _upstreamRelations =  new HashSet<AbstractEntity>();

      BeanInfo beanInfo = getBeanInfo();
      for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
        if (isNonEntityProperty(propertyDescriptor)) {
          continue;
        }
        Method getter = propertyDescriptor.getReadMethod();
        for (AbstractEntity relatedEntity : getRelatedEntities(propertyDescriptor, getter)) {
          if (relatedEntity != null) {
            initializeForRelatedEntity(propertyDescriptor, getter, relatedEntity);
          }
        }
      }
    }

    private BeanInfo getBeanInfo() throws EntityNetworkPersisterException
    {
      Class<? extends AbstractEntity> entityClass = _entity.getClass();
      try {
        return Introspector.getBeanInfo(entityClass);
      }
      catch (IntrospectionException e) {
        throw new EntityNetworkPersisterException(e);
      }
    }

    private boolean isNonEntityProperty(PropertyDescriptor propertyDescriptor)
    {
      String propertyName = propertyDescriptor.getName();
      if (propertyName.equals("class")) {
        return true;
      }
      if (propertyDescriptor.getReadMethod().isAnnotationPresent(Transient.class)) {
        return true;
      }
      return false;
    }

    private Collection<AbstractEntity> getRelatedEntities(PropertyDescriptor propertyDescriptor, Method getter) throws EntityNetworkPersisterException
    {
      Collection<AbstractEntity> relatedEntities = new ArrayList<AbstractEntity>();
      try {
        if (AbstractEntity.class.isAssignableFrom(getter.getReturnType())) {
          relatedEntities.add((AbstractEntity) getter.invoke(_entity));
        }
        else if (Collection.class.isAssignableFrom(getter.getReturnType())) {
          Collection relatedCollection = (Collection) getter.invoke(_entity);
          for (Object relatedCollectionMember : relatedCollection) {
            if (relatedCollectionMember instanceof AbstractEntity) {
              relatedEntities.add((AbstractEntity) relatedCollectionMember);
            }
          }
        }
        else if (Map.class.isAssignableFrom(getter.getReturnType())) {
          log.debug("getter for map: " + _entity + ":" + getter);
          Map relatedMap = (Map) getter.invoke(_entity);
          for (Object relatedMapMember : relatedMap.values()) {
            log.debug("related Map member: " + relatedMapMember);
            if (relatedMapMember instanceof AbstractEntity) {
              relatedEntities.add((AbstractEntity) relatedMapMember);
            }
          }
        }
      }
      catch (Exception e) {
        throw new EntityNetworkPersisterException(e);
      }
      return relatedEntities;
    }

    private void initializeForRelatedEntity(PropertyDescriptor propertyDescriptor, Method getter, AbstractEntity relatedEntity) throws EntityNetworkPersisterException
    {
      Method relatedGetter = getRelatedGetter(propertyDescriptor);
      if (relatedGetter == null) {
        _downstreamRelations.add(relatedEntity);
        return;
      }
      if (isCascadingRelationship(getter) && isCascadingRelationship(relatedGetter)) {
        throw new EntityNetworkPersisterException(
          "Bi-directional cascade: " + getter + " and " + relatedGetter);
      }
      if (isCascadingRelationship(getter)) {
        _downstreamRelations.add(relatedEntity);
        return;
      }
      if (isCascadingRelationship(relatedGetter)) {
        if (_cascadingUpstreamRelation != null) {
          throw new EntityNetworkPersisterException(
            "Multiple cascading upstream relationships for entity " + _entity + ": " +
            _cascadingUpstreamRelation + " and " + relatedEntity);
        }
        _cascadingUpstreamRelation = relatedEntity;
        return;
      }

      if (isNonCascadingRelationshipDownstream(getter, relatedGetter)) {
        _downstreamRelations.add(relatedEntity);
      }
      else {
        _upstreamRelations.add(relatedEntity);
      }
    }

    private Method getRelatedGetter(PropertyDescriptor propertyDescriptor)
    {
      // TODO: consider factoring out necessary code from RelatedProperty
      RelatedProperty relatedProperty =
        new RelatedProperty(_entity.getClass(), propertyDescriptor);
      PropertyDescriptor relatedPropertyDescriptor = relatedProperty.getPropertyDescriptor();
      if (relatedPropertyDescriptor == null) {
        return null;
      }
      return relatedPropertyDescriptor.getReadMethod();
    }

    private boolean isCascadingRelationship(Method getter)
    {
      org.hibernate.annotations.Cascade cascade = getter.getAnnotation(Cascade.class);
      if (cascade == null) {
        return false;
      }
      Set<CascadeType> cascadeTypes = new HashSet<CascadeType>();
      CollectionUtils.addAll(cascadeTypes, cascade.value());
      return cascadeTypes.contains(CascadeType.SAVE_UPDATE);
    }

    // NOTE this method must be reciprocal - ie, if it returns true for (getter1,getter2)
    // then it must return false for (getter2,getter1), and vice-versa
    private boolean isNonCascadingRelationshipDownstream(Method getter, Method relatedGetter) throws EntityNetworkPersisterException
    {
      // TODO: assuming OneToMany mirrors ManyToOne and vice-versa
      if (getter.getAnnotation(OneToMany.class) != null) {
        return false;
      }
      if (getter.getAnnotation(ManyToOne.class) != null) {
        return true;
      }
      // TODO: assuming exactly one side of ManyToMany has mappedBy
      if (getter.getAnnotation(ManyToMany.class) != null) {
        if (getter.getAnnotation(ManyToMany.class).mappedBy() != null) {
          return false;
        }
        return true;
      }
      // TODO: assuming exactly one side of OneToOne has mappedBy
      if (getter.getAnnotation(OneToOne.class) != null) {
        if (getter.getAnnotation(OneToOne.class).mappedBy() != null) {
          return false;
        }
        return true;
      }
      throw new EntityNetworkPersisterException("getter has no *To* annotation: " + getter);
    }
  }
}
