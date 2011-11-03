package edu.harvard.med.iccbl.screensaver.io;

import java.net.URL;

import edu.harvard.med.screensaver.model.Entity;

public interface ImageProvider<T extends Entity>
{
  /**
   * Get the URL identifying an image that can be looked up using the specified entity
   * 
   * @return null if image cannot be provided for any reason
   */
  URL getImageUrl(T entity);
}
