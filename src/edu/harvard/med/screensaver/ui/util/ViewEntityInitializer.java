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
 * {@link #inflateEntities()}. Only one of these methods should be implemented,
 * based upon these guidelines:
 * <ul>
 * <li>Implement entity reattachment (invoked per request) when either:
 * <ul>
 * <li> entity is mutable (via user actions)
 * <li> developer is not sure what "lazy" relationships of an entity's object
 * network will be accessed, or the "lazy" relationships that are accessed is
 * conditionally determined and inflating them all at once is expensive
 * </ul>
 * <li>Implement entity inflation (invoked as one-time initialization) when
 * entities are immutable and entities' inflated object network size is large
 * (since each entity in the object network generates an SQL call if
 * reattachment is used; e.g. library.wells)
 * </ul>
 * <p>
 * When entities have become out-of-date (w.r.t. the database),
 * {@link #reload()} will be called, followed by {@link #setEntities()}. The
 * subclass should make the appropriate DAO calls in reload, and make the
 * appropriate setter calls on the viewer in {@link #setEntities}. Note that
 * {@link #reload(AbstractEntity)} does not have to be overriden if the view
 * exclusively makes use of entities that are immutable (i.e., can never change
 * in the database).
 * <p>
 * Subclasses are intended to be declared as anonymous classes and instantiated
 * in a Controller class's "UIControllerMethod"s. Thus, when a new view has been
 * requested and is being initialized by a UIControllerMethod, a
 * ViewEntityInitializer should be declared and instantiated. Note that the act
 * of instantiating a concrete ViewEntityInitializer will cause it to be
 * registered as the "current" ViewEntityInitializer. If a UIControllerMethod
 * does not have any entity initialization needs, it should not instantiate a
 * ViewEntityInitializer, but should instead call the static
 * {@link ViewEntityInitializer#unregisterCurrentViewEntityInitializer()}
 * method. This ensures that any previous view's ViewEntityInitializer will not
 * be invoked on subsequent page requests.
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

  private String _name;
  private DAO _dao;

  
  /**
   * Called at instantiation time.
   */
  final private void initializeView() 
  {
    log.debug("initializing entities for " + _name);
    registerCurrentViewInitializer();
    setup();
    reattachEntities();
    inflateEntities();
    setEntities();
  }

  /**
   * Called when the view is requested again.
   */
  final public void reinitializeView() 
  {
    log.debug("reinitializing entities for " + _name);
    reattachEntities();
  }

  /**
   * Called when a DataAccessException occurs while handling a request for the
   * view.
   */
  final public void reloadView()
  {
    log.debug("reloading entities for " + _name);
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
 
  /**
   * 
   * Constructs a ViewEntityInitializer object.
   * @param name a name that identifies this ViewEntityInitializer; for debug purposes only
   * @param dao the DAO used to reload entities
   */
  protected ViewEntityInitializer(String name, DAO dao)
  {
    _name = name;
    _dao = dao;
    initializeView();
  }

  final protected DAO getDAO() 
  { 
    return _dao; 
  }
  
  final protected void reattach(AbstractEntity entity)
  {
    if (entity != null) {
      log.debug("reattaching entity " + entity);
      _dao.persistEntity(entity);
    }
  }
  
  /**
   * @param entity the entity to be reloaded; assumption is that it does not
   *          already exist in the Hibernate session
   * @return a new instance of the specified entity
   */
  final protected AbstractEntity reload(AbstractEntity entity)
  {
    if (entity != null) {
      log.debug("reloading entity " + entity);
      return _dao.findEntityById(entity.getClass(), entity.getEntityId());
    }
    return null;
  }
  
  final protected void need(AbstractEntity entity)
  {
    if (entity != null) {
      log.debug("inflating entity " + entity);
      Hibernate.initialize(entity);
    }
  }
  
  final protected void need(PersistentCollection persistentCollection)
  {
    if (persistentCollection != null) {
      log.debug("inflating collection " + persistentCollection);
      Hibernate.initialize(persistentCollection);
    }
  }

  final protected void need(Collection collection)
  {
    if (collection != null) {
      log.debug("inflating collection " + collection);
      collection.iterator();
    }
  }
  
  @SuppressWarnings("unchecked")
  final private void registerCurrentViewInitializer()
  {
    log.debug("registered " + _name + " as current view initializer");
    FacesContext facesContext = FacesContext.getCurrentInstance();
    facesContext.getExternalContext().getSessionMap().put(LAST_VIEW_ENTITY_INITIALIZER, this);
  }
  
  public static void unregisterCurrentViewEntityInitializer()
  {
    FacesContext facesContext = FacesContext.getCurrentInstance();
    ViewEntityInitializer vei = (ViewEntityInitializer)
      facesContext.getExternalContext().getSessionMap().get(LAST_VIEW_ENTITY_INITIALIZER);
    if (vei != null) {
      facesContext.getExternalContext().getSessionMap().remove(LAST_VIEW_ENTITY_INITIALIZER);
      log.debug("unregistered " + vei._name + " view initializer");
    }
  }
}

