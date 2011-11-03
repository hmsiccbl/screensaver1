package edu.harvard.med.screensaver.ui.arch.util.servlet;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.rest.ScreensaverEntityView;
import edu.harvard.med.screensaver.ui.arch.view.EntityViewerBackingBean;
import edu.harvard.med.screensaver.ui.arch.view.EntityViewerInitializer;

/**
 * Initializes an {@link EntityViewerBackingBean} before JSF attempts to render to the viewer.
 * 
 * @motivation for servicing RESTful URLs redirects into the web application by the Spring MVC framework, which cannot
 *             initialize the viewer until a user has been authenticated by the web application.
 * @see EntityViewerInitializer
 * @see ScreensaverEntityView
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class EntityViewerInitializerPhaseListener implements PhaseListener
{
  private static final Logger log = Logger.getLogger(EntityViewerInitializerPhaseListener.class);

  @Override
  public PhaseId getPhaseId()
  {
    return PhaseId.RENDER_RESPONSE;
  }

  @Override
  public void beforePhase(PhaseEvent event)
  {
    log.info("intercepted " + event);
    HttpSession httpSession = (HttpSession) event.getFacesContext().getExternalContext().getSession(false);
    Object entityViewerInitializer = httpSession.getAttribute("entityViewerInitializer");
    if (entityViewerInitializer != null) {
      // viewers rely upon having an authenticated user in place, which is not the case if Tomcat has sent us to the login page
      if (((EntityViewerInitializer) entityViewerInitializer).isReadyForInitialization()) {
        httpSession.removeAttribute("entityViewerInitializer");
        ((EntityViewerInitializer) entityViewerInitializer).apply();
      }
    }
  }

  @Override
  public void afterPhase(PhaseEvent event)
  {
  }
}
