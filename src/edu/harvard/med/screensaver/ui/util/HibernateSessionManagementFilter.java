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
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.orm.hibernate3.support.OpenSessionInViewInterceptor;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Servlet 2.3 Filter that binds a Hibernate Session to the thread for the entire
 * processing of the request. Intended for the "Open Session in View" pattern,
 * i.e. to allow for lazy loading in web views despite the original transactions
 * already being completed.
 *
 * <p>This filter works similar to the AOP HibernateInterceptor: It just makes
 * Hibernate Sessions available via the thread. It is suitable for non-transactional
 * execution but also for business layer transactions via HibernateTransactionManager
 * or JtaTransactionManager. In the latter case, Sessions pre-bound by this filter
 * will automatically be used for the transactions and flushed accordingly.
 *
 * <p><b>WARNING:</b> Applying this filter to existing logic can cause issues that
 * have not appeared before, through the use of a single Hibernate Session for the
 * processing of an entire request. In particular, the reassociation of persistent
 * objects with a Hibernate Session has to occur at the very beginning of request
 * processing, to avoid clashes will already loaded instances of the same objects.
 *
 * <p>Alternatively, turn this filter into deferred close mode, by specifying
 * "singleSession"="false": It will not use a single session per request then,
 * but rather let each data access operation or transaction use its own session
 * (like without Open Session in View). Each of those sessions will be registered
 * for deferred close, though, actually processed at request completion.
 *
 * <p>A single session per request allows for most efficient first-level caching,
 * but can cause side effects, for example on saveOrUpdate or if continuing
 * after a rolled-back transaction. The deferred close strategy is as safe as
 * no Open Session in View in that respect, while still allowing for lazy loading
 * in views (but not providing a first-level cache for the entire request).
 * 
 * <p>Looks up the SessionFactory in Spring's root web application context.
 * Supports a "sessionFactoryBeanName" filter init-param in <code>web.xml</code>;
 * the default bean name is "sessionFactory". Looks up the SessionFactory on each
 * request, to avoid initialization order issues (when using ContextLoaderServlet,
 * the root application context will get initialized <i>after</i> this filter).
 *
 * <p><b>NOTE</b>: This filter will by default <i>not</i> flush the Hibernate Session,
 * as it assumes to be used in combination with service layer transactions that care
 * for the flushing, or HibernateAccessors with flushMode FLUSH_EAGER. If you want this
 * filter to flush after completed request processing, override <code>closeSession</code>
 * and invoke <code>flush</code> on the Session before closing it. Additionally, you will
 * also need to override <code>getSession()</code> to return a Session in a flush mode
 * other than the default <code>FlushMode.NEVER</code>. Note that <code>getSession</code>
 * and <code>closeSession</code> will just be invoked in single session mode!
 *
 * @author Juergen Hoeller
 * @since 1.2
 * @see #setSingleSession
 * @see #closeSession
 * @see #lookupSessionFactory
 * @see OpenSessionInViewInterceptor
 * @see org.springframework.orm.hibernate3.HibernateInterceptor
 * @see org.springframework.orm.hibernate3.HibernateTransactionManager
 * @see org.springframework.orm.hibernate3.SessionFactoryUtils#getSession
 * @see org.springframework.transaction.support.TransactionSynchronizationManager
 */
public class HibernateSessionManagementFilter extends OncePerRequestFilter {

  private static Logger log = Logger.getLogger(HibernateSessionManagementFilter.class);

  public static final String DEFAULT_SESSION_FACTORY_BEAN_NAME = "sessionFactory";

  public static final String RELEASE_HTTP_AND_HIBERNATE_SESSIONS = "releaseHibernateSession";


  private String sessionFactoryBeanName = DEFAULT_SESSION_FACTORY_BEAN_NAME;

  private boolean singleSession = true;

  private Map<String,Session> _httpSession2HibernateSession;

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
      log.info("closing Hibernate session " + hibSession);
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
  throws ServletException, IOException {

    SessionFactory sessionFactory = lookupSessionFactory();
    HttpSession httpSession = request.getSession();
    String httpSessionId = httpSession.getId();

    Session hibSession = getOrCreateHibernateSession(sessionFactory, httpSessionId);
    String hibSessionLogId = SessionFactoryUtils.toString(hibSession);

    TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(hibSession));

    try {
      filterChain.doFilter(request, response);
    }
    finally {
      TransactionSynchronizationManager.unbindResource(sessionFactory);

      boolean releaseHibernateSession = Boolean.TRUE.equals(httpSession.getAttribute(RELEASE_HTTP_AND_HIBERNATE_SESSIONS));
      if (releaseHibernateSession) {
        httpSession.invalidate();
        SessionFactoryUtils.releaseSession(hibSession, sessionFactory);
        synchronized (this) {
          _httpSession2HibernateSession.remove(httpSessionId);
        }
        log.info("closed Hibernate session " + hibSessionLogId + 
                 " for HTTP session " + httpSessionId);
      }
      log.debug("After HTTP request, Hibernate session " + hibSessionLogId + 
                " for HTTP session " + httpSessionId + " is " +
                (hibSession.isOpen() ? "open" : "closed"));
    }
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
  protected SessionFactory lookupSessionFactory() {
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
