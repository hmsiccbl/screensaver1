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

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.collection.PersistentCollection;

/**
 * A base class whose concrete subclasses are used to ensure that a JSF view's
 * Hibernate entities are "accessible". By "accessible", we mean that an entity
 * is either attached to the current Hibernate session or has its persistent
 * collections and relationships pre-loaded (if they are lazy intialized).
 * Furthermore, the subclass must also ensure that entity objects that have
 * become out-of-date w.r.t. the database can be reloaded and re-injected into
 * the appropriate viewer backing beans.
 * <p>
 * It is the responsibility of the subclass to "know" what parts of an entity's
 * object network must be made accessible to the JSF view it is servicing. The
 * subclass can follow one of two strategies for ensuring that entities are
 * accessible: 1) reattaching them to the current Hibernate session, by
 * overriding and implementing {@link #reattach()}, or 2) pre-loading all
 * required lazy-initialized members, by overriding and implementing
 * {@link #inflateEntities()}. Only one of these methods should be implemented.
 * <p>
 * When entities have become out-of-date w.r.t. the database, {@link #reload()}
 * will be called, followed by {@link #setEntities()}. The subclass should make
 * the appropriate DAO calls in reload, and make the appropriate setter calls on
 * the viewer in {@link #setEntities}. Note that
 * {@link #reload(AbstractEntity)} does not have to be overriden if the view
 * exclusively makes use of entities that are immutable (i.e., can never change
 * in the database).
 * <p>
 * Subclasses are intended to be declared as anonymous classes in
 * "UIControllerMethod"s within Controller classes.
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
 *             loadied into) the current Hibernate session. Furthermore, if an
 *             entity becomes out-of-date w.r.t the database, a user of
 *             Hibernate's only recourse is to reload the updated objects in a
 *             new Hibernate session, trashing the old session with the
 *             out-of-date objects.
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public abstract class ViewEntityInitializer
{
  private static final Logger log = Logger.getLogger(ViewEntityInitializer.class);
  
  public static final String LAST_VIEW_ENTITY_INITIALIZER = "lastViewEntityInitializer";

  private DAO _dao;
  
  /**
   * Called at instantiation time.
   */
  final private void initializeView() 
  {
    log.debug("initializing view's entities");
    registerCurrentViewInitializer();
    setup();
    reattachEntities();
    inflateEntities();
    setEntities();
  }

  final public void reinitializeView() 
  {
    log.debug("reinitializing view's entities");
    reattachEntities();
  }
  
  final public void reloadView()
  {
    log.debug("reloading view's entities");
    reloadEntities();
    inflateEntities();
    setEntities();
  }

  /**
   * Override this method to perform any initial setup required by the class.
   * Usually to set private data members.
   * 
   * @motivation Provide a means of performing instantiation-time setup for
   *             anonymous classes, which cannot declare constructors.
   */
  protected void setup()
  {
  }

  /**
   * Allows the subclass to (optionally) inflate required entities in the
   * current Hibernate session. Called on initial HTTP request. May make use of
   * {@link #need}, as a convenience. Either this method or
   * {@link #reattachEntities} must be overridden and implemented, but not both.
   */
  protected void inflateEntities() 
  {
  }

  /**
   * Allows the subclass to (optionally) reattach required entities to the
   * current Hibernate session. Called at the start of every HTTP request. May
   * make use of {@link #reattach}, as a convenience. Either this method or
   * {@link #inflateEntities} must be overridden and implemented, but not both.
   */
  protected void reattachEntities()
  {
  }
  
  /**
   * Allows the subclass to reload entities that have become out-of-date w.r.t
   * the database. Called if Spring DataAccessException occurs. Implementation
   * is mandatory if entities used by view are mutable (anywhere in the
   * application, not just editable by the view!). May make use of
   * {@link #reload}, as a convenience.
   */
  protected void reloadEntities() 
  {
    throw new UnsupportedOperationException(getClass().getName()
                                            + " needs to implement reloadEntities(), as managed entities are mutable");
  }

  /**
   * The implementation should inject into its view any entity objects that it
   * requires (by calling setters on the view object). Called once at
   * instantiation time (after inflateEntities() and reattachEntities() is
   * called), and any time after reloadEntities() has been invoked.
   */
  protected void setEntities()
  {
  }
 
  protected ViewEntityInitializer(DAO dao)
  {
    _dao = dao;
    initializeView();
  }

  protected DAO getDAO() 
  { 
    return _dao; 
  }
  
  final protected void reattach(AbstractEntity entity)
  {
    if (entity != null) {
      _dao.persistEntity(entity);
    }
  }
  
  final protected AbstractEntity reload(AbstractEntity entity)
  {
    if (entity != null) {
      return _dao.findEntityById(entity.getClass(), entity.getEntityId());
    }
    return null;
  }
  
  final protected void need(AbstractEntity entity)
  {
    if (entity != null) {
      Hibernate.initialize(entity);
    }
  }
  
  final protected void need(PersistentCollection persistentCollection)
  {
    if (persistentCollection != null) {
      Hibernate.initialize(persistentCollection);
    }
  }

  final protected void need(Collection collection)
  {
    if (collection != null) {
      collection.iterator();
    }
  }
  
  @SuppressWarnings("unchecked")
  final private void registerCurrentViewInitializer()
  {
    log.debug("setting " + getClass().getName() + " as current view initializer");
    FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(LAST_VIEW_ENTITY_INITIALIZER, this);
  }
}

