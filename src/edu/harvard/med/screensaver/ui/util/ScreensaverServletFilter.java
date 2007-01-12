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

import org.apache.log4j.Logger;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Servlet filter for Screensaver applications that
 * <ul>
 * <li>Filters out HTTP requests that are not for JSF-managed pages (e.g. static resources, such as images).
 * <li>Handles exceptions by redirecting to a custom exception page.
 * <li>Handles requests for termination of user sessions (session invalidation needs to be delayed until after JSF is done handling the request).
 * <li>Generates "flanking" debug output around the handling of JSF requests.
 * </ul>
 */
// TODO: this filter has taken on too many roles: txn mgmt, HTTP session mgmt, and error handling.  We should break up responsibilities into individual filters (or, minimally, rename this class)
public class ScreensaverServletFilter extends OncePerRequestFilter {

  
  // static data members
  

  private static Logger log = Logger.getLogger(ScreensaverServletFilter.class);

  public static final String DEFAULT_SESSION_FACTORY_BEAN_NAME = "sessionFactory";
  public static final String CLOSE_HTTP_SESSION = "closeHttpSession";
  public static final String SYSTEM_ERROR_ENCOUNTERED = "systemError";
  private static final String CONCURRENT_MODIFICATION_MESSAGE = "concurrentModificationConflict";
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

  protected void doFilterInternal(final HttpServletRequest request, 
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


    Throwable caughtException = null;
    try {
      filterChain.doFilter(request, response);
    }
    catch (Exception e) {
      if (e instanceof ServletException) {
        caughtException = ((ServletException) e).getRootCause();
      } 
      else {
        caughtException = e;
      }
      log.error("caught exception during invocation of servlet filter chain:" + e);
      caughtException.printStackTrace();
    }
    finally {
      if (caughtException != null) {
        httpSession.setAttribute("javax.servlet.error.exception", caughtException);
        try {
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
    }
    if (Boolean.TRUE.equals(httpSession.getAttribute(CLOSE_HTTP_SESSION))) {
      httpSession.invalidate();
      log.info("closed HTTP session " + httpSessionId);
    }

    log.info("<<<< Screensaver FINISHED processing HTTP request for session " + 
             httpSessionId + " @ " + request.getRequestURI());

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
}
