package edu.harvard.med.screensaver.ui.util;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.web.filter.OncePerRequestFilter;

import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.ui.screens.ScreenViewer;

/**
* Servlet filter for cellHTS2 report authorization
* @author siew cheng (BII). Thanks to Andrew for the code to retrieve bean from session.
* 
*/

public class CellHTS2ReportAuthorizationFilter extends OncePerRequestFilter {

  //static data members

  private static Logger log = Logger.getLogger(CellHTS2ReportAuthorizationFilter.class);

  private static final String CELLHTS2_REPORT_ACCESS_DENIED_URL = "/screensaver/main/cellHTS2ReportAccessDenied.jsf";

  //instance data members

  //methods

  protected void doFilterInternal(final HttpServletRequest request, 
                                  final HttpServletResponse response, 
                                  final FilterChain filterChain)
  throws ServletException, IOException 
  {

    final HttpSession httpSession = request.getSession();

    ScreenViewer bean = ((ScreenViewer) httpSession.getAttribute("scopedTarget.screenViewer"));
    try {
      if (bean != null) {
        Screen screen = bean.getEntity();
        if (screen != null) {
          ScreenResult screenResult = screen.getScreenResult();
          if (screenResult != null && !screenResult.isRestricted()) {
            log.info("user authorized to view screen result for screen " + screen.getScreenNumber());
          }
          else {
            log.info("user not authorized to view screen result for screen " + screen.getScreenNumber());
            response.sendRedirect(CELLHTS2_REPORT_ACCESS_DENIED_URL);
          }
        }
        else {
          log.info("can't get screen from screenViewer in http session");
          response.sendRedirect(CELLHTS2_REPORT_ACCESS_DENIED_URL);
        }
      }
      else {
        log.info("screenViewer not in http session");
        response.sendRedirect(CELLHTS2_REPORT_ACCESS_DENIED_URL);
      }

      filterChain.doFilter(request, response);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}