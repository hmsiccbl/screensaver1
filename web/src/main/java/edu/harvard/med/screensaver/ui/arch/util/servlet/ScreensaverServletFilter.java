// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.util.servlet;

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
public class ScreensaverServletFilter extends OncePerRequestFilter 
{
  private static Logger log = Logger.getLogger(ScreensaverServletFilter.class);

  public static final String DEFAULT_SESSION_FACTORY_BEAN_NAME = "sessionFactory";
  public static final String CLOSE_HTTP_SESSION = "closeHttpSession";
  private static final String REPORT_EXCEPTION_URL = "/main/reportException.jsf";

  private String sessionFactoryBeanName = DEFAULT_SESSION_FACTORY_BEAN_NAME;

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
      log.error("caught exception during invocation of servlet filter chain:", e);
      caughtException.printStackTrace();
    }
    finally {
      if (caughtException != null) {
        httpSession.setAttribute("javax.servlet.error.exception", caughtException);
        try {
          response.sendRedirect(request.getContextPath() + REPORT_EXCEPTION_URL);
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
   */
  private boolean isRequestForApplicationView(HttpServletRequest request)
  {
    return request.getRequestURI().endsWith(".jsf");
  }
}
