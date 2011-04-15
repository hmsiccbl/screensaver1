// $HeadURL: $
// $Id: $
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package org.hibernate.ejb;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.hibernate.event.EventListeners;
import org.hibernate.event.MergeEventListener;
import org.hibernate.event.PersistEventListener;
import org.hibernate.event.PostLoadEventListener;
import org.hibernate.event.SaveOrUpdateEventListener;

/**
 * A replacement for the HibernatePersistence class that allows <i>additional</i> Hibernate event listeners to be
 * specified as instantiated objects, allowing the listener objects to be Spring-instantiated beans. This allows the
 * specified listeners to take advantage of dependency injection, rather than just being instantiated via default
 * constructors, as HibernatePersistence would otherwise do. The default Hibernate listeners will be maintained, and the
 * specified listeners will be appended to the list of listeners of the given type.
 * <p/>
 * To use this class in a Spring context XML configuration file:
 * 
 * <pre>
 *    <bean id="entityManagerFactory" class="org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean">
 *     <property name="persistenceProvider">
 *       <bean class="org.hibernate.ejb.ConfigurableListenerBeansHibernatePersistence">
 *         <property name="postLoadEventListeners">
 *           <list>
 *             <bean class="my.application.MyPostLoadEventListener1" />
 *             <bean class="my.application.MyPostLoadEventListener2" />
 *           </list>
 *         </property>
 *       </bean>
 *     </property>
 *     ...
 *     <property name="jpaPropertyMap">...</property>
 *   </bean>
 * </pre>
 * 
 * @author atolopko
 */
// TODO: add support for all Hibernate event types
public class ConfigurableListenerBeansHibernatePersistence extends HibernatePersistence
{
  @Override
  public EntityManagerFactory createContainerEntityManagerFactory(PersistenceUnitInfo info, Map properties)
  {
    Ejb3Configuration cfg = new Ejb3Configuration();
    Ejb3Configuration configured = cfg.configure(info, properties);

    if (configured != null) {
      configureListeners(configured);
      return configured.buildEntityManagerFactory();
    }
    return null;
  }

  private List<PostLoadEventListener> _postLoadEventListeners;
  private List<SaveOrUpdateEventListener> _saveOrUpdateEventListeners;
  private List<PersistEventListener> _persistEventListeners;
  private List<MergeEventListener> _mergeEventListeners;

  public List<PostLoadEventListener> getPostLoadEventListeners()
  {
    return _postLoadEventListeners;
  }

  public void setPostLoadEventListeners(List<PostLoadEventListener> postLoadEventListeners)
  {
    _postLoadEventListeners = postLoadEventListeners;
  }

  public List<PersistEventListener> getPersistEventListeners()
  {
    return _persistEventListeners;
  }

  public void setPersistEventListeners(List<PersistEventListener> persistEventListeners)
  {
    _persistEventListeners = persistEventListeners;
  }

  public List<MergeEventListener> getMergeEventListeners()
  {
    return _mergeEventListeners;
  }

  public void setMergeEventListeners(List<MergeEventListener> mergeEventListeners)
  {
    _mergeEventListeners = mergeEventListeners;
  }

  public List<SaveOrUpdateEventListener> getSaveOrUpdateEventListeners()
  {
    return _saveOrUpdateEventListeners;
  }

  public void setSaveOrUpdateEventListeners(List<SaveOrUpdateEventListener> saveOrUpdateEventListeners)
  {
    _saveOrUpdateEventListeners = saveOrUpdateEventListeners;
  }

  private void configureListeners(Ejb3Configuration cfg)
  {
    EventListeners eventListeners = cfg.getEventListeners();

    cfg.setListeners("post-load", concatListeners(PostLoadEventListener.class, _postLoadEventListeners, eventListeners.getPostLoadEventListeners()));
    cfg.setListeners("save-update", concatListeners(SaveOrUpdateEventListener.class, _saveOrUpdateEventListeners, eventListeners.getSaveOrUpdateEventListeners()));
    cfg.setListeners("merge", concatListeners(MergeEventListener.class, _mergeEventListeners, eventListeners.getMergeEventListeners()));
    cfg.setListeners("create", concatListeners(PersistEventListener.class, _persistEventListeners, eventListeners.getPersistEventListeners()));
    // TODO: do we also need create-on-flush event?
  }

  public <L> L[] concatListeners(Class<L> listenerClass, List<L> eventListeners, L[] extantListeners)
  {
    if (eventListeners == null) {
      eventListeners = Lists.newArrayList();
    }
    if (extantListeners == null) {
      return Iterables.toArray(eventListeners, listenerClass);
    }
    return Iterables.toArray(Iterables.concat(Arrays.asList(extantListeners), eventListeners), listenerClass);
  }


}
