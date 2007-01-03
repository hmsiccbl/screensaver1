/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.harvard.med.screensaver.ui.util;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.db.DAOTransaction;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.orm.hibernate3.support.OpenSessionInViewFilter;
import org.springframework.orm.hibernate3.support.OpenSessionInViewInterceptor;
import org.springframework.transaction.TransactionException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Servlet 2.3 Filter that binds a Hibernate Session to the thread until the
 * session parameter {@link #RELEASE_HTTP_AND_HIBERNATE_SESSIONS} is set with a
 * value of <code>true</code>. This is an extension for the "Open Session in
 * View" pattern, which allows for lazy loading of Hibernate-managed persistent
 * objects in web views despite the original transactions already being
 * completed. This class has been adapted from Spring's{@link OpenSessionInViewFilter}.
 * <p>
 * Hibernate docs 11.3.2:
 * <ul>
 * <li>"This pattern is problematic if the Session is too big to be stored
 * during user think time, e.g. an HttpSession should be kept as small as
 * possible. As the Session is also the (mandatory) first-level cache and
 * contains all loaded objects, we can probably use this strategy only for a few
 * request/response cycles. You should use a Session only for a single
 * conversation, as it will soon also have stale data."
 * <li>To keep read-only data up-to-date (in sync with other users' changes),
 * issue Session.lock(readOnlyObj, LockMode.READ):
 * </ul>
 * <p>
 * In light of the above, web pages will have to detect if their persistent
 * objects are 1) in an open session and 2) up-to-date. If not, the web page
 * must handle the condition gracefully. For example, by asking the user to
 * re-run a search, or warn them the object being edited was changed by another
 * user and will be reloaded.
 * <p>
 * Looks up the SessionFactory in Spring's root web application context.
 * Supports a "sessionFactoryBeanName" filter init-param in <code>web.xml</code>;
 * the default bean name is "sessionFactory". Looks up the SessionFactory on
 * each request, to avoid initialization order issues (when using
 * ContextLoaderServlet, the root application context will get initialized
 * <i>after</i> this filter).
 * 
 * @see OpenSessionInViewInterceptor
 */
// TODO: this filter has taken on too many roles: txn mgmt, HTTP session mgmt, and error handling.  We should break up responsibilities into individual filters (or, minimally, rename this class)
public class ScreensaverSessionManagementFilter extends OncePerRequestFilter {

  
  // static data members
  

  private static Logger log = Logger.getLogger(ScreensaverSessionManagementFilter.class);

  public static final String DEFAULT_SESSION_FACTORY_BEAN_NAME = "sessionFactory";
  public static final String CLOSE_HTTP_SESSION = "closeHttpSession";
  public static final String SYSTEM_ERROR_ENCOUNTERED = "systemError";
  private static final String CONCURRENT_MODIFICATION_MESSAGE = "concurrentModificationConflict";
  private static final String OPERATION_CANCELLED_MESSAGE = "databaseOperationAborted";
  private static final String REPORT_EXCEPTION_URL = "/screensaver/reportException.jsf";
  private static final String LOGIN_URL = "/screensaver/login.jsf";

  private static final String RELOAD_VIEW_ENTITIES_SESSION_PARAM = "reloadViewEntities";


  // instance data members
  
  private String sessionFactoryBeanName = DEFAULT_SESSION_FACTORY_BEAN_NAME;



  // methods
  
  @Override
  protected void initFilterBean() throws ServletException
  {
  }
  
  @Override
  public void destroy()
  {
    log.info("destroying filter");
  }

  /**
   * Set the bean name of the SessionFactory to fetch from Spring's
   * root application context. Default is "sessionFactory".
   * @see #DEFAULT_SESSION_FACTORY_BEAN_NAME
   */
  public void setSessionFactoryBeanName(String sessionFactoryBeanName) {
    this.sessionFactoryBeanName = sessionFactoryBeanName;
  }

  /**
   * Return the bean name of the SessionFactory to fetch from Spring's
   * root application context.
   */
  protected String getSessionFactoryBeanName() {
    return sessionFactoryBeanName;
  }

  protected String getTransactionManagerBeanName() {
    return sessionFactoryBeanName;
  }

  protected void doFilterInternal(
    final HttpServletRequest request, 
    final HttpServletResponse response, 
    final FilterChain filterChain)
  throws ServletException, IOException 
  {
    // we don't perform Hibernate session management unless:
    // 1) this is a request for one of our application's JSF views 
    //    (static resources do not require us to perform these steps)
    // 2) a logged-in user is associated with the session (to avoid 
    //    consuming Hibernate session & associated database resources 
    //    unnecessarily)
    if (!isRequestForApplicationView(request) ||
      request.getRemoteUser() == null) {
      filterChain.doFilter(request, response);
      return;
    }
    
    final HttpSession httpSession = request.getSession();
    String httpSessionId = httpSession.getId();
    log.info(">>>> Screensaver STARTING to process HTTP request for session " + 
             httpSessionId + " @ " + request.getRequestURI());
    

    try {
      DAO dao = lookupBeanViaSpring(DAO.class, "dao");
      dao.doInTransaction(new DAOTransaction() {
        public void runTransaction() 
        {
          Throwable caughtException = null;
          try {
            int initialEntityCount = 0;
            int initialCollectionCount = 0;
            if (log.isDebugEnabled()) {
              Session session = lookupSessionFactoryViaSpring().getCurrentSession();
              initialCollectionCount = session.getStatistics().getCollectionCount();
              initialEntityCount = session.getStatistics().getEntityCount();
            }

            initializeHibernateEntities(httpSession);
            filterChain.doFilter(request, response);

            if (log.isDebugEnabled()) {
              Session session = lookupSessionFactoryViaSpring().getCurrentSession();
              int finalEntityCount = session.getStatistics().getEntityCount();
              int finalCollectionCount = session.getStatistics().getCollectionCount();
              log.debug("session entity count: pre=" + initialEntityCount + ", post=" + finalEntityCount + ", delta=" + (finalEntityCount - initialEntityCount));
              log.debug("session collection count: pre=" + initialCollectionCount + ", post=" + finalCollectionCount + ", delta=" + (finalCollectionCount - initialCollectionCount));
            }
          }
          catch (Exception e) {
            // note: if an I/O exception occurs, we should still try to complete
            // our txn, since inability to communicate our response back to the
            // client is not grounds for aborting the operation the user
            // initiated
            log.error("caught exception during invocation of servlet filter chain");
            if (e instanceof ServletException) {
              caughtException = ((ServletException) e).getRootCause();
            } 
            else {
              caughtException = e;
            }
            caughtException.printStackTrace();
          }
          finally {
            if (caughtException != null) {
              // any exceptions thrown at this point may have occurred before response was rendered, so we must redirect to an error page, if we want to guarantee the user gets a response
              httpSession.setAttribute("javax.servlet.error.exception", caughtException);
              try {
                httpSession.removeAttribute(ViewEntityInitializer.LAST_VIEW_ENTITY_INITIALIZER);
                response.sendRedirect(REPORT_EXCEPTION_URL);
              }
              catch (IOException e) {
                e.printStackTrace();
                try {
                  response.sendError(500);
                }
                catch (IOException e1) {
                  // nothing more we can do!
                  e1.printStackTrace();
                }
              }
            }
            logSessionStatistics();
          }
        }
      }); // note: data-access exceptions will be thrown when Hibernate session is being flushed, which occurs at end of txn (i.e., "here")
    }
    // TODO: it is fundamentally flawed to handle database-related exceptions here, since there is nothing we can do about it to inform the user, as the response has already been generated
    catch (ConcurrencyFailureException e) {
      getMessages().setFacesMessageForComponent(httpSession, CONCURRENT_MODIFICATION_MESSAGE, null);
      httpSession.setAttribute(RELOAD_VIEW_ENTITIES_SESSION_PARAM, true);
    }
    catch (TransactionException e) { // e.g. UnexpectedRollbackException
      getMessages().setFacesMessageForComponent(httpSession, OPERATION_CANCELLED_MESSAGE, e.getMessage());
    }
    // note: we should never receive HibernateExceptions, as Spring is supposed to wrap all HibernateExceptions in DataAccessExceptions
    // catch (HibernateException e) {}
    catch (Throwable e) {
      log.error("unexpected exception while processing HTTP request for '" + request.getRequestURI() + "' in a transaction: " + e);
      e.printStackTrace();

      // don't invalidate session yet, so that our error page, which is a JSF page and requires 
      // JSF backing beans that are stored in our HTTP session, can still operate
      httpSession.setAttribute("javax.servlet.error.exception", e);
      getMessages().setFacesMessageForComponent(httpSession, SYSTEM_ERROR_ENCOUNTERED,  null, e.getClass().getName(), e.getMessage());
    }
    finally {
      if (Boolean.TRUE.equals(httpSession.getAttribute(CLOSE_HTTP_SESSION))) {
        httpSession.invalidate();
        log.info("closed HTTP session " + httpSessionId);
      }

      log.info("<<<< Screensaver FINISHED processing HTTP request for session " + 
               httpSessionId + " @ " + request.getRequestURI());
      
    }
  }

  private void logSessionStatistics()
  {
    // requires Hibernate's "generate_statistics" configuration parameter to be set to true; this is set by our build.xml's hibernatedoclet taks when debug flag is set
    if (log.isDebugEnabled()) {
      Statistics statistics = lookupSessionFactoryViaSpring().getStatistics();
      if (statistics.isStatisticsEnabled()) {
        StringBuilder s = new StringBuilder();
        s.append("entities: ");
        s.append("loaded=").append(statistics.getEntityLoadCount());
        s.append(", fetched=").append(statistics.getEntityFetchCount());
        s.append(", updated=").append(statistics.getEntityUpdateCount());
        s.append(", inserted=").append(statistics.getEntityInsertCount());
        s.append(", deleted=").append(statistics.getEntityDeleteCount());
        log.debug(s.toString());
        s.setLength(0);
        s.append("collections: ");
        s.append("loaded=").append(statistics.getCollectionLoadCount());
        s.append(", fetched=").append(statistics.getCollectionFetchCount());
        s.append(", updated=").append(statistics.getCollectionUpdateCount());
        s.append(", recreated=").append(statistics.getCollectionRecreateCount());
        s.append(", removed=").append(statistics.getCollectionRemoveCount());
        log.debug(s.toString());
        s.setLength(0);
        s.append("flushes=").append(statistics.getFlushCount());
        log.debug(s.toString());
        s.setLength(0);
        s.append("transactions: ");
        s.append("started=").append(statistics.getTransactionCount());
        s.append(", succeeded=").append(statistics.getSuccessfulTransactionCount());
        log.debug(s.toString());
        s.setLength(0);
        s.append("optimistic failures=").append(statistics.getOptimisticFailureCount());
        log.debug(s.toString());
        statistics.clear();
      }
    }
  }

  private void initializeHibernateEntities(final HttpSession httpSession)
  {
    try {
      ViewEntityInitializer viewEntityInitializer = 
        (ViewEntityInitializer) httpSession.getAttribute(ViewEntityInitializer.LAST_VIEW_ENTITY_INITIALIZER);
      if (viewEntityInitializer != null) {
        Boolean reloadViewEntities = (Boolean) httpSession.getAttribute(RELOAD_VIEW_ENTITIES_SESSION_PARAM);
        if (reloadViewEntities != null && reloadViewEntities) {
          viewEntityInitializer.reloadView();
        }
        else {
          viewEntityInitializer.reinitializeView();
        }
      }
    }
    catch (Throwable t) {
      // in case *this* method threw the exception, ensure that we won't re-enter method when trying to view error page!
      httpSession.removeAttribute(ViewEntityInitializer.LAST_VIEW_ENTITY_INITIALIZER);
    }
    finally {
      httpSession.removeAttribute(RELOAD_VIEW_ENTITIES_SESSION_PARAM);
    }
  }

  private Messages getMessages()
  {
    return lookupBeanViaSpring(Messages.class, "messages");
  }
  
  /**
   * Returns <code>true</code> iff the request is for a JSF view, and thus
   * would make use of a Hibernate session.
   * 
   * @motivation We use this to avoid performing Hibernate session management
   *             tasks for URLs of static resources (images, etc.).
   * @param request
   * @return
   */
  private boolean isRequestForApplicationView(HttpServletRequest request)
  {
    return request.getRequestURI().endsWith(".jsf") || request.getRequestURI().endsWith(".jsp");
  }

  /**
   * Look up the SessionFactory that this filter should use.
   * <p>
   * Default implementation looks for a bean with the specified name in Spring's
   * root application context.
   * 
   * @return the SessionFactory to use
   * @see #getSessionFactoryBeanName
   */
  protected SessionFactory lookupSessionFactoryViaSpring()
  {
    if (logger.isDebugEnabled()) {
      logger.debug("Using SessionFactory '" + getSessionFactoryBeanName()
                   + "' for " + getClass().getName());
    }
    return (SessionFactory) lookupBeanViaSpring(SessionFactory.class,
                                                getSessionFactoryBeanName());
  }

  @SuppressWarnings("unchecked")
  private <C extends Object> C lookupBeanViaSpring(Class<C> clazz, String beanName) {
    WebApplicationContext wac =
      WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
    return (C) wac.getBean(beanName, clazz);
  }

}
