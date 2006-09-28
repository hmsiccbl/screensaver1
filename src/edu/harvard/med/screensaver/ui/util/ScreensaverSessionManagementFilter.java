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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.orm.hibernate3.support.OpenSessionInViewFilter;
import org.springframework.orm.hibernate3.support.OpenSessionInViewInterceptor;
import org.springframework.transaction.support.TransactionSynchronizationManager;
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
public class ScreensaverSessionManagementFilter extends OncePerRequestFilter {

  
  // static data members
  
  private static Logger log = Logger.getLogger(ScreensaverSessionManagementFilter.class);

  public static final String DEFAULT_SESSION_FACTORY_BEAN_NAME = "sessionFactory";
  public static final String CLOSE_HTTP_AND_HIBERNATE_SESSIONS = "closeHttpAndHibernateSessions";
  public static final String CLOSE_HIBERNATE_SESSION = "closeHibernateSession";


  // instance data members
  
  private String sessionFactoryBeanName = DEFAULT_SESSION_FACTORY_BEAN_NAME;

  private Map<String,Session> _httpSession2HibernateSession;


  // methods
  
  @Override
  protected void initFilterBean() throws ServletException
  {
    _httpSession2HibernateSession = new HashMap<String,Session>();
  }
  
  @Override
  public void destroy()
  {
    log.info("destroying filter");
    for (Iterator iter = _httpSession2HibernateSession.values().iterator(); iter.hasNext();) {
      Session hibSession = (Session) iter.next();
      log.info("closing Hibernate session " + SessionFactoryUtils.toString(hibSession));
      hibSession.close();
    }
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

  protected void doFilterInternal(
    HttpServletRequest request, 
    HttpServletResponse response, 
    FilterChain filterChain)
  throws ServletException, IOException 
  {
    // we don't perform any of our filter's special logic unless this is a
    // request for one of our application's views; static resources do not
    // require us to perform these steps
    if (!isRequestForApplicationView(request)) {
      filterChain.doFilter(request, response);
      return;
    }

    HttpSession httpSession = request.getSession();
    String httpSessionId = httpSession.getId();
    log.info(">>>> Screensaver STARTING to process HTTP request for session " + 
             httpSessionId + " @ " + request.getRequestURI());
    SessionFactory sessionFactory = lookupSessionFactoryViaSpring();
    Session hibSession = getOrCreateHibernateSession(sessionFactory, httpSessionId);
    String hibSessionLogId = SessionFactoryUtils.toString(hibSession);
    hibSession.setFlushMode(FlushMode.COMMIT); // not sure this is any better/worse then FlushMode.AUTO
    
    // set the Hibernate session that Spring/Hibernate will use for this thread/request.
    TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(hibSession));

    try {
      filterChain.doFilter(request, response);
    }
    finally {
      log.debug("Hibernate session " + hibSessionLogId + 
                " for HTTP session " + httpSessionId +
                " is " + (hibSession.isOpen() ? "open" : "closed") +
                // call isDirty() apparently causes a flush side effect! causes all kinds of strange problems...
                // ", " + (hibSession.isDirty() ? "dirty" : "clean") +
                ", " + (hibSession.isConnected() ? "connected" : "disconnected"));
      
      // unset the Hibernate session that Spring/Hibernate was used for this thread/request.
      TransactionSynchronizationManager.unbindResource(sessionFactory);
      
      boolean closeHttpSession = 
        Boolean.TRUE.equals(httpSession.getAttribute(CLOSE_HTTP_AND_HIBERNATE_SESSIONS));
      boolean closeHibernateSession = 
        closeHttpSession || 
        Boolean.TRUE.equals(httpSession.getAttribute(CLOSE_HIBERNATE_SESSION));
      
      if (closeHibernateSession) {
        SessionFactoryUtils.releaseSession(hibSession, sessionFactory);
        synchronized (this) {
          _httpSession2HibernateSession.remove(httpSessionId);
        }
        log.info("closed Hibernate session " + hibSessionLogId + 
                 " for HTTP session " + httpSessionId);
        
        if (closeHttpSession) {
          httpSession.invalidate();
          log.info("closed HTTP session " + httpSessionId);
        }
      }
      assert !hibSession.isOpen() : 
        "Hibernate session " + hibSessionLogId + 
        " was not closed for HTTP session " + httpSessionId;
      
      log.info("<<<< Screensaver FINISHED processing HTTP request for session " + 
               httpSessionId + " @ " + request.getRequestURI());
    }
  }

  private boolean isRequestForApplicationView(HttpServletRequest request)
  {
    return request.getRequestURI().endsWith(".jsf") || request.getRequestURI().endsWith(".jsp");
  }

  protected Session getOrCreateHibernateSession(SessionFactory sessionFactory, String httpSessionId)
  {
    synchronized (_httpSession2HibernateSession) {
      Session hibSession = _httpSession2HibernateSession.get(httpSessionId);
      if (hibSession == null) {
        hibSession = sessionFactory.openSession();
        _httpSession2HibernateSession.put(httpSessionId, hibSession);
      log.info("created Hibernate session " + SessionFactoryUtils.toString(hibSession) + 
               " for HTTP session " + httpSessionId);
      log.debug("new session flush mode is " + hibSession.getFlushMode());
      }
      else {
        log.debug("using existing Hibernate session " + 
                  SessionFactoryUtils.toString(hibSession) + 
                  " for HTTP session " + httpSessionId);
      }
      return hibSession;
    }
  }

  /**
   * Look up the SessionFactory that this filter should use.
   * <p>Default implementation looks for a bean with the specified name
   * in Spring's root application context.
   * @return the SessionFactory to use
   * @see #getSessionFactoryBeanName
   */
  protected SessionFactory lookupSessionFactoryViaSpring() {
    if (logger.isDebugEnabled()) {
      logger.debug("Using SessionFactory '" + getSessionFactoryBeanName() + "' for " + getClass().getName());
    }
    WebApplicationContext wac =
      WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
    return (SessionFactory) wac.getBean(getSessionFactoryBeanName(), SessionFactory.class);
  }

//  /**
//   * Get a Session for the SessionFactory that this filter uses.
//   * Note that this just applies in single session mode!
//   * <p>The default implementation delegates to SessionFactoryUtils'
//   * getSession method and sets the Session's flushMode to NEVER.
//   * <p>Can be overridden in subclasses for creating a Session with a custom
//   * entity interceptor or JDBC exception translator.
//   * @param sessionFactory the SessionFactory that this filter uses
//   * @return the Session to use
//   * @throws DataAccessResourceFailureException if the Session could not be created
//   * @see org.springframework.orm.hibernate3.SessionFactoryUtils#getSession(SessionFactory, boolean)
//   * @see org.hibernate.FlushMode#NEVER
//   */
//  protected Session getSession(SessionFactory sessionFactory) throws DataAccessResourceFailureException {
//    Session session = SessionFactoryUtils.getSession(sessionFactory, true);
//    session.setFlushMode(FlushMode.NEVER);
//    return session;
//  }
//
//  /**
//   * Close the given Session.
//   * Note that this just applies in single session mode!
//   * <p>The default implementation delegates to SessionFactoryUtils'
//   * releaseSession method.
//   * <p>Can be overridden in subclasses, e.g. for flushing the Session before
//   * closing it. See class-level javadoc for a discussion of flush handling.
//   * Note that you should also override getSession accordingly, to set
//   * the flush mode to something else than NEVER.
//   * @param session the Session used for filtering
//   * @param sessionFactory the SessionFactory that this filter uses
//   */
//  protected void closeSession(Session session, SessionFactory sessionFactory) {
//    SessionFactoryUtils.releaseSession(session, sessionFactory);
//  }

}
