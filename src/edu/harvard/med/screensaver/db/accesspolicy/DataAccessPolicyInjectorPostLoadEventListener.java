// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.accesspolicy;

import edu.harvard.med.screensaver.model.AbstractEntity;

import org.hibernate.event.PostLoadEvent;
import org.hibernate.event.def.DefaultPostLoadEventListener;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

/**
 * A Hibernate event listener that injects the specified DataAccessPolicy into
 * every AbstractEntity object loaded by Hibernate.
 * 
 * @see AbstractEntity#isRestricted()
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
// HACK: we're forced to make this a BeanFactoryAware bean, in order to obtain a
// DataAccessPolicy without creating a circular dependency in Spring
// configuration files (and using the Spring-recommended setter-based injection
// strategy for handling circular dependencies could not be made to work).
public class DataAccessPolicyInjectorPostLoadEventListener extends DefaultPostLoadEventListener implements BeanFactoryAware
{
  private static final long serialVersionUID = 1L;

  private BeanFactory _beanFactory;
  private DataAccessPolicy _dataAccessPolicy;
  
  public void setBeanFactory(BeanFactory beanFactory) throws BeansException
  {
    _beanFactory = beanFactory;
  }
  public DataAccessPolicy getDataAccessPolicy()
  {
    if (_dataAccessPolicy == null) {
      _dataAccessPolicy = (DataAccessPolicy) _beanFactory.getBean("dataAccessPolicy");
    }
    return _dataAccessPolicy;
  }

  @Override
  public void onPostLoad(PostLoadEvent event)
  {
    // let the Hibernate core do its thing
    super.onPostLoad(event);
    
    if (event.getEntity() instanceof AbstractEntity) {
      AbstractEntity entity = (AbstractEntity) event.getEntity();
      entity.setDataAccessPolicy(getDataAccessPolicy());
    }
  }
}

