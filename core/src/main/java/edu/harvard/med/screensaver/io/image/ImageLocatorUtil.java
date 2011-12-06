package edu.harvard.med.screensaver.io.image;

import java.io.IOException;
import java.net.URL;

import org.apache.log4j.Logger;

public class ImageLocatorUtil
{
  private static final Logger log = Logger.getLogger(ImageLocatorUtil.class);

  /**
   * @return the same URL iff the content of the URL is accessible, otherwise null
   * @motivation allow UI code to gracefully handle non-extant images that are identified by URL
   */
  public static URL toExtantContentUrl(URL url)
  {
    if (url != null) {
      try {
        Object content = url.getContent();
        if (content != null) {
          return url;
        }
      }
      catch (IOException e) {}
    }
    log.debug("image not available: " + url);
    return null;
  }

}
