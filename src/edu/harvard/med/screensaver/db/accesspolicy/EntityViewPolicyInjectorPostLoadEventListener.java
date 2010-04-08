// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.accesspolicy;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.policy.EntityViewPolicy;

import org.hibernate.event.PostLoadEvent;
import org.hibernate.event.def.DefaultPostLoadEventListener;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

/**
 * A Hibernate event listener that injects the specified EntityViewPolicy into
 * every AbstractEntity object loaded by Hibernate.
 * 
 * @see AbstractEntity#isRestricted()
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
// HACK: we're forced to make this a BeanFactoryAware bean, in order to obtain an
// EntityViewPolicy without creating a circular dependency in Spring
// configuration files (and using the Spring-recommended setter-based injection
// strategy for handling circular dependencies could not be made to work).
public class EntityViewPolicyInjectorPostLoadEventListener extends DefaultPostLoadEventListener implements BeanFactoryAware
{
  private static final long serialVersionUID = 1L;

  private BeanFactory _beanFactory;
  private EntityViewPolicy _entityViewPolicy;
  
  public void setBeanFactory(BeanFactory beanFactory) throws BeansException
  {
    _beanFactory = beanFactory;
  }
  
  public EntityViewPolicy getEntityViewPolicy()
  {
    if (_entityViewPolicy == null) {
      _entityViewPolicy = (EntityViewPolicy) _beanFactory.getBean("entityViewPolicy");
    }
    return _entityViewPolicy;
  }

  @Override
  public void onPostLoad(PostLoadEvent event)
  {
    // let the Hibernate core do its thing
    super.onPostLoad(event);
    
    if (event.getEntity() instanceof AbstractEntity) {
      AbstractEntity entity = (AbstractEntity) event.getEntity();
      entity.setEntityViewPolicy(getEntityViewPolicy());
    }
  }
}

