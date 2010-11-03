package edu.harvard.med.iccbl.screensaver.io;

import java.io.IOException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.google.common.collect.Sets;

import edu.harvard.med.screensaver.util.StringUtils;

/**
 * This filter will perform Authentication for selected URL-patterns.
 * <p/>
 * Restrict access for a set of servlet paths to a role.<br/>
 * Params:<br/>
 * <br/>   
 *    - roles: comma separated list of roles<br/>
 *    <br/>
 *    - preRegexString: a regex pattern that is used to pre-filter;<br/> 
 *    group(1) for this pattern (the "subDir") is used in a second-level match<br/>
 *    against each of the "regexes".  Should have the form regex_pattern(subDir pattern)<br/> 
 *    <br/>
 *    - regexes: comma separated list of regex patterns<br/> 
 *    to match against the group(1) match (the "subDir") from the preRegexString<br/>
 *    <br/>
 *    Operation: if one of the regexes matches a subDir, then see if the current user is in<br/> 
 *    one of the roles (using HttpServletRequest.isUserInRole )<br/> 
 * <br/>
 * <br/>
 * references: <br/>
 * http://publib.boulder.ibm.com/infocenter/wasinfo/v6r1/index.jsp?topic=/com.ibm.websphere.express.doc/info/exp/ae/tsec_servlet.html<br/>
 * http://java.sun.com/javaee/5/docs/tutorial/doc/bncav.html
 *
 */
public class AuthenticationFilter implements Filter
{
  private static Logger log = Logger.getLogger(AuthenticationFilter.class);
  
  private FilterConfig config = null;
  
  private Pattern _preRegex;
  private Set<Pattern> _regexs = Sets.newHashSet();
  private Set<String> _roles = Sets.newHashSet();
  
  public void init(FilterConfig config) throws ServletException
  {
    this.config = config;
    
    String preRegexString = this.config.getInitParameter("preRegexString");
    String regexesString = this.config.getInitParameter("regexs");
    String roles = this.config.getInitParameter("roles");
    
    String errMsg = "";
    if (StringUtils.isEmpty(preRegexString))
    {
      errMsg +=  "Param \"preRegexString\" must not be empty";
    }
    if (StringUtils.isEmpty(regexesString))
    {
      errMsg +=  (errMsg.length()==0?"":", ") + "Param \"regexs\" must not be empty";
    }
    if (StringUtils.isEmpty(roles))
    {
      errMsg +=  (errMsg.length()==0?"":", ") + "Param \"roles\" must not be empty";
    }
    if(errMsg.length() > 0 )
    {
      throw new ServletException("Authentication Servlet Usage:  " + errMsg );
    }
    if (log.isDebugEnabled()) {
      log.debug("regexesString:" + regexesString);
    }
    
    _preRegex = Pattern.compile(preRegexString);

    for (String regex:regexesString.split(","))
    {
      if (log.isDebugEnabled()) {
        log.debug("compile: " + regex);
      }
      _regexs.add(Pattern.compile(regex));
    }

    for (String role:roles.split(","))
    {
      _roles.add(role);
    } 
  } 

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    throws IOException, ServletException
  {
    if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
      throw new ServletException("SecurityFilter only accepts HTTP requests");
    }
    if (request instanceof HttpServletRequest) {
      HttpServletRequest req = (HttpServletRequest) request;
      HttpServletResponse res = (HttpServletResponse) response;

      String pathInfo = req.getServletPath();
      if (pathInfo != null) {
        Matcher match = _preRegex.matcher(pathInfo);
        if (match.matches()) {
          String subdir = match.group(1);

          for (Pattern p : _regexs) {
            if (p.matcher(subdir).matches()) {
              boolean roleFound = false;
              if (req.getUserPrincipal() != null) {
                for (String role : _roles) {
                  if (req.isUserInRole(role)) {
                    roleFound = true;
                    break;
                  }
                }
              }
              if (!roleFound) {
                if (req.getUserPrincipal() != null) {
                  log.info("access for the user \"" + req.getUserPrincipal().getName() + "\" to: " + pathInfo
                           + ", subdir: " + subdir
                           + " has been denied!");
                }
                else {
                  log.info("access for unauthenticated user at: " + req.getRemoteAddr() + " to: " + pathInfo + ", subdir: " +
                    subdir + " has been denied!");
                }
                res.sendError(javax.servlet.http.HttpServletResponse.SC_FORBIDDEN);
                return;
              }
            }// match subdir
          }
        }// match preRegex
      }
      // post login action
      chain.doFilter(request, response);
    }
    else {
      // only allow http servlet requests
      throw new ServletException("Invalid request type (must be http servlet request): "
          + request.getClass().getName());
    }
  }
  
  public void destroy()
  {
    // TODO Auto-generated method stub
    
  }
}
