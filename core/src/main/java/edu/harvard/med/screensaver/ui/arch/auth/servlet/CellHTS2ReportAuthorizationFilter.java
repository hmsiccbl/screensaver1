package edu.harvard.med.screensaver.ui.arch.auth.servlet;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.filter.OncePerRequestFilter;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;

/**
 * Servlet filter for cellHTS2 report authorization
 * @author siew cheng (BII). Thanks to Andrew for the code to retrieve bean from session.
 * 
 */

public class CellHTS2ReportAuthorizationFilter extends OncePerRequestFilter
{
  private static Logger log = Logger.getLogger(CellHTS2ReportAuthorizationFilter.class);

  private static final Pattern ScreenResultIdPattern =
    Pattern.compile("^" + ScreensaverConstants.CELLHTS2_REPORTS_BASE_URL + "([0-9]+)/.*$");

  private GenericEntityDAO _dao;

  public CellHTS2ReportAuthorizationFilter(GenericEntityDAO dao)
  {
    _dao = dao;
  }

  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain)
  throws ServletException, IOException 
  {
    try {
      Integer screenResultId = getScreenResultId(request);
      if (isReportRestricted(screenResultId)) {
        log.error("unauthorized access of cellHTS2 report for screen result " + screenResultId + " by " + request.getRemoteUser());
        response.sendError(401);
      }
      filterChain.doFilter(request, response);
    }
    catch (Exception e) {
      log.error(e.toString());
      throw new ServletException(e);
    }
  }

  public boolean isReportRestricted(Integer screenResultId)
  {
    ScreenResult screenResult = _dao.findEntityById(ScreenResult.class, screenResultId);
    return screenResult == null || screenResult.isRestricted();
  }

  public static Integer getScreenResultId(HttpServletRequest request)
  {
    Matcher matcher = ScreenResultIdPattern.matcher(request.getRequestURI());
    if (matcher.matches()) {
      return Integer.valueOf(matcher.group(1));
    }
    throw new IllegalArgumentException("cannot parse screen result ID from URL " + request.getRequestURI());
  }
}