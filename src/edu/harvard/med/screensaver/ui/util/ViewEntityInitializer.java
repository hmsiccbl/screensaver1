// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.util;

import java.util.Collection;

import javax.faces.context.FacesContext;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.model.AbstractEntity;

import org.hibernate.Hibernate;
import org.hibernate.collection.PersistentCollection;

/**
 * A base class whose concrete subclasses are used to initialize and (and
 * re-initialize) a JSF view's Hibernate entities by reattaching them to the
 * current Hibernate session (optionally by reloading them from the database)
 * and then explicitly loading any persistent collections and relationships
 * (proxies) that will be accessed by the JSF view that the subclass services.
 * <p>
 * It is the responsibility of the subclass to "know" what parts of an entity's
 * object network will be accessed by the JSF view it is servicing. The subclass
 * can explicitly load parts of an entity's object network by calling this
 * class's need() convenience methods for the persistent collections and related
 * entities that need to be accessible.
 * <p>
 * The subclass can follow one of two strategies for re-loading entities: 1)
 * reattach them by calling reattach(), or 2) reload them by calling reload().
 * Reload() is preferred in general, as it requeries the database for the
 * entity, and will thus capture any recent concurrent changes that were made to
 * the entity (by other users, e.g.). Reattach() is more efficient, but will not
 * capture new changes in the database; it should only be used for entities that
 * are considered immutable. <i>Reloading creates a new object for the entity,
 * reattaching reuses the same entity object.</i>.
 * <p>
 * Subclasses must implement {@link #revivifyAndPrimeEntities} and
 * {@link #setEntities}.
 * 
 * @motivation Hibernate sucks. Okay, well, it doesn't suck, but it does
 *             introduce a lazy intialization problem. Lazy persistent
 *             collections and relationships (proxies) are (as the name implies)
 *             loaded only when requested, which is problematic for entity
 *             objects that are accessed in subsequent HTTP requests. Since a
 *             new Hibernate session is created for each HTTP request, an entity
 *             that was loaded in a Hibernate session of a previous HTTP request
 *             cannot have its persistent collections and relationships lazy
 *             initialized by subsequent HTTP requests, unless those objects are
 *             re-attached to the new Hibernate session. If a view is passed an
 *             entity, it cannot assume that its persistent collections and
 *             relationships have already been initialized, and cannot attempt
 *             accessing them before they have been reattached to (or newly
 *             loadied into) the current Hibernate session.
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public abstract class ViewEntityInitializer
{
  public static final String LAST_VIEW_ENTITY_INITIALIZER = "lastViewEntityInitializer";

  private DAO _dao;
  
  final public void initializeView() 
  {
    registerCurrentViewInitializer();
    revivifyAndPrimeEntities();
    setEntities();
  }

  /**
   * The implementation should add reload or reattach required entities, and
   * then force initialization of persistent collections and relationships that
   * will be needed by the view that is being serviced.
   */
  protected abstract void revivifyAndPrimeEntities();

  /**
   * The implementation should pass to its view any new entity objects that were
   * returned by reload().
   */
  protected abstract void setEntities();
 
  protected ViewEntityInitializer(DAO dao)
  {
    _dao = dao;
  }

  protected DAO getDAO() 
  { 
    return _dao; 
  }
  
  final protected void reattach(AbstractEntity entity)
  {
    _dao.persistEntity(entity);
  }
  
  final protected AbstractEntity reload(AbstractEntity entity)
  {
    return _dao.findEntityById(entity.getClass(), entity.getEntityId());
  }
  
  final protected void need(AbstractEntity entity)
  {
    Hibernate.initialize(entity);
  }
  
  final protected void need(PersistentCollection persistentCollection)
  {
    Hibernate.initialize(persistentCollection);
  }

  final protected void need(Collection collection)
  {
    collection.iterator();
  }
  
  @SuppressWarnings("unchecked")
  final private void registerCurrentViewInitializer()
  {
    FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(LAST_VIEW_ENTITY_INITIALIZER, this);
  }

}

