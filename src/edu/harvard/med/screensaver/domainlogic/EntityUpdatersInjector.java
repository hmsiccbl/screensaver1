// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.domainlogic;

import java.util.List;
import java.util.Map;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.Entity;

import org.hibernate.HibernateException;
import org.hibernate.event.MergeEvent;
import org.hibernate.event.MergeEventListener;
import org.hibernate.event.PostLoadEvent;
import org.hibernate.event.PostLoadEventListener;
import org.hibernate.event.SaveOrUpdateEvent;
import org.hibernate.event.SaveOrUpdateEventListener;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

import com.google.common.base.Function;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;

/**
 * A Hibernate event listener that injects the appropriate EntityUpdaters into
 * every {@link Entity} object that becomes managed by Hibernate (i.e., via
 * saveUpdate, merge, or load event).
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
// HACK: we're forced to make this a BeanFactoryAware bean, in order to obtain the
// entityUpdaters bean without creating a circular dependency in Spring
// configuration files (and using the Spring-recommended setter-based injection
// strategy for handling circular dependencies could not be made to work).
public class EntityUpdatersInjector 
  implements SaveOrUpdateEventListener, MergeEventListener, PostLoadEventListener, BeanFactoryAware
{
  private static final long serialVersionUID = 1L;

  private BeanFactory _beanFactory;
  private ListMultimap<Class<? extends Entity>,EntityUpdater> _entityUpdaters;
  
  public void setBeanFactory(BeanFactory beanFactory) throws BeansException
  {
    _beanFactory = beanFactory;
  }
  
  public ListMultimap<Class<? extends Entity>,EntityUpdater> getEntityUpdaters()
  {
    if (_entityUpdaters == null) {
      List<EntityUpdater> entityUpdatersList = (List<EntityUpdater>) _beanFactory.getBean("entityUpdatersList");
      _entityUpdaters = Multimaps.index(entityUpdatersList, 
                                        new Function<EntityUpdater,Class<? extends Entity>>() { 
        public Class<? extends Entity> apply(EntityUpdater eu) { return eu.getEntityClass(); } 
      }); 
    }
    return _entityUpdaters;
  }
  
  @Override
  public void onPostLoad(PostLoadEvent event)
  {
    injectEntityUpdaters(event.getEntity());
  }

  @Override
  public void onSaveOrUpdate(SaveOrUpdateEvent event) throws HibernateException
  {
    injectEntityUpdaters(event.getObject());
  }

  @Override
  public void onMerge(MergeEvent event) throws HibernateException
  {
    injectEntityUpdaters(event.getEntity());
  }

  @Override
  public void onMerge(MergeEvent event, Map copiedAlready)
    throws HibernateException
  {
    injectEntityUpdaters(event.getEntity());
  }

  private void injectEntityUpdaters(Object object)
  {
    if (object instanceof AbstractEntity) {
      AbstractEntity entity = (AbstractEntity) object;
      entity.setEntityUpdaters(getEntityUpdaters().get(entity.getEntityClass()));
    }
  }
}

