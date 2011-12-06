package edu.harvard.med.screensaver.io.image;

import java.net.URL;

import edu.harvard.med.screensaver.model.Entity;

/** SPI for determining the URL of an image for a given {@link Entity} */
public interface ImageLocator<T extends Entity>
{
  /**
   * Get the URL identifying an image that can be looked up using the specified entity
   * 
   * @return null if image's location cannot be determined for any reason
   */
  URL getImageUrl(T entity);
}
