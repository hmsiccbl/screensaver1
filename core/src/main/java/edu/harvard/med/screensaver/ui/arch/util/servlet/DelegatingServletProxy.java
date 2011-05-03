// $HeadURL: $
// $Id: $
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.util.servlet;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import edu.harvard.med.screensaver.ScreensaverProperties;
import edu.harvard.med.screensaver.ui.ApplicationInfo;
import edu.harvard.med.screensaver.util.StringUtils;

/**
 * Proxy for a standard Servlet, delegating to a Spring-managed
 * bean that implements the Servlet interface.<br>
 * The servlet init-param, "beanName", in <code>web.xml</code>, specifying the name of the
 * target bean in the Spring application context is required.
 * <p>
 * <code>web.xml</code> will usually contain a DelegatingServletProxy definition, with the specified
 * <code>servlet-name</code> corresponding to a bean name in Spring's root application context. All calls to the servlet
 * proxy will then be delegated to that bean in the Spring context, which is required to conform to the {@link Servlet}
 * interface and implement the {@link Servlet#service(ServletRequest, ServletResponse)} method.
 * <p>
 * This is useful when it is desired to initialize bean state (parameters) from properties in the
 * {@link ScreensaverProperties} or the {@link ApplicationInfo} .
 */
@SuppressWarnings("serial")
public class DelegatingServletProxy extends HttpServlet // implements   BeanNameAware, ServletContextAware
{
  private static Logger log = Logger.getLogger(DelegatingServletProxy.class);

  private static final String PARAMETER_BEAN_NAME = "beanName";

  private String _beanName;
  private Servlet _delegate;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
  {
    service(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
  {
    super.doPost(req, resp);
  }

  @Override
  public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException
  {
    getDelegate().service(req, res);
  }

  public void setBeanName(String name)
  {
    _beanName = name;
  }

  public String getBeanName()
  {
    return _beanName;
  }

  private synchronized Servlet getDelegate()
  {
    if (this._delegate == null) {
      WebApplicationContext wac = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
      if (wac == null) {
        throw new IllegalStateException("No WebApplicationContext found: no ContextLoaderListener registered?");
      }
      this._delegate = (Servlet) wac.getBean(getBeanName(), Servlet.class);
      if (_delegate == null) {
        throw new IllegalStateException("The bean: \"" + getBeanName() +
          "\" could not be retrieved from the WebApplicationContext.");
      }
    }
    return this._delegate;
  }

  /**
   * Implement the init method, checking for the req'd property: <br>
   * <b>beanName</b><br>
   * Initialize the instance to the delegate Spring bean corresponding to the beanName.
   */
  @Override
  public void init(ServletConfig servletConfig) throws ServletException
  {
    super.init(servletConfig);
    if (servletConfig == null) throw new ServletException("servletConfig must not be null");
    if (log.isDebugEnabled()) {
      log.debug("Initializing filter '" + servletConfig.getServletName() + "'");
    }
    String beanName = servletConfig.getInitParameter(PARAMETER_BEAN_NAME);
    if (StringUtils.isEmpty(beanName)) {
      throw new ServletException("The parameter: " + PARAMETER_BEAN_NAME + " must be given.");
    }
    setBeanName(beanName);
    getDelegate(); // force initialization
  }
}
