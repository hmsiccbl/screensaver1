package edu.harvard.med.iccbl.screensaver.ui;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.Entity;
import edu.harvard.med.screensaver.ui.arch.util.servlet.EntityViewerInitializerPhaseListener;
import edu.harvard.med.screensaver.ui.arch.view.EntityViewerBackingBean;
import edu.harvard.med.screensaver.util.DevelopmentException;

/**
 * Initializes an {@link EntityViewerBackingBean} with the specified entity, allowing this initialization to performed
 * at an appropriate future time (e.g., after a user has been authenticated).
 * 
 * @see EntityViewerInitializerPhaseListener
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class EntityViewerInitializer
{
  private static final Logger log = Logger.getLogger(EntityViewerInitializer.class);

  private EntityViewerBackingBean _viewer;
  private Entity _entity;

  public EntityViewerInitializer(EntityViewerBackingBean viewer, Entity entity)
  {
    _viewer = viewer;
    _entity = entity;
  }

  public void apply()
  {
    if (!isReadyForInitialization()) {
      throw new DevelopmentException("viewer is not ready for initialization");
    }
    log.info("initializing " + _viewer.getClass() + " with entity " + _entity);
    _viewer.viewEntity(_entity);
  }

  public boolean isReadyForInitialization()
  {
    return _viewer.getScreensaverUser() != null;
  }
}
