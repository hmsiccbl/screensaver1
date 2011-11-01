package edu.harvard.med.screensaver.rest;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.view.AbstractView;

import edu.harvard.med.iccbl.screensaver.ui.EntityViewerInitializer;
import edu.harvard.med.screensaver.model.Entity;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.ui.arch.view.EntityViewerBackingBean;

public class ScreensaverEntityView extends AbstractView
{
  private static final Logger log = Logger.getLogger(ScreensaverEntityView.class);

  private String modelEntityKey;
  private Map<Class<? extends Entity>,ViewerMapping> viewerMappings;

  public static class ViewerMapping
  {
    public static Function<ViewerMapping,Class<? extends Entity>> toKey = new Function<ViewerMapping,Class<? extends Entity>>() {
      @Override
      public Class<? extends Entity> apply(ViewerMapping from)
      {
        return from.getEntityClass();
      }
    };
    
    Class<? extends Entity> entityClass;
    String relativeUrlPath;
    EntityViewerBackingBean viewer;

    public Class<? extends Entity> getEntityClass()
    {
      return entityClass;
    }

    public void setEntityClass(Class<? extends Entity> entityClass)
    {
      this.entityClass = entityClass;
    }

    public String getRelativeUrlPath()
    {
      return relativeUrlPath;
    }

    public void setRelativeUrlPath(String relativeUrlPath)
    {
      this.relativeUrlPath = relativeUrlPath;
    }

    public EntityViewerBackingBean getViewer()
    {
      return viewer;
    }

    public void setViewer(EntityViewerBackingBean viewer)
    {
      this.viewer = viewer;
    }
  }

  public void setEntityViewers(List<ViewerMapping> viewerMappings)
  {
    this.viewerMappings = Maps.uniqueIndex(viewerMappings, ViewerMapping.toKey);
  }

  public String getModelEntityKey()
  {
    return modelEntityKey;
  }

  public void setModelEntityKey(String modelEntityKey)
  {
    this.modelEntityKey = modelEntityKey;
  }

  protected void renderMergedOutputModel(Map<String,Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception
  {
    Entity entity = (Entity) model.get(getModelEntityKey());  // TODO: check for an 'error' here; i.e. if the value stored is an ErrorConverter.ErrorContainer
    String urlRelativePath;
    EntityViewerBackingBean viewer;

    Class<? extends Entity> entityClass = (Class<? extends Entity>) model.get("entityClass");
    if (entityClass == null) {
      entityClass = entity.getClass();
    }
//    if(RestCollection.class.isAssignableFrom(...) ) {
//      // TODO: this is a proposed "container" for a collection result - invoke a searchResult now?  -sde4
//      throw new NotImplementedException();
//    }
    if (!!!viewerMappings.containsKey(entityClass)) {
      throw new RuntimeException("no viewer mapping for " + entity.getClass());
    }

    urlRelativePath = viewerMappings.get(entityClass).getRelativeUrlPath();
    viewer = viewerMappings.get(entityClass).getViewer();
    log.info("initializing Screensaver viewer " + viewer + " with entity " + entity);
    request.getSession().setAttribute("entityViewerInitializer", new EntityViewerInitializer(viewer, entity));

    response.sendRedirect(request.getContextPath() + urlRelativePath);
  }
}
