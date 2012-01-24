
package edu.harvard.med.screensaver.io.image;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

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
        // Override the HostNameChecker so that any ole SSL (including our lowly dev box) will pass, and we can move on to verify this URL
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
          public boolean verify(String hostname, SSLSession session)
          {
            return true;
          }
        });
        log.info("url:" + url);
        Object content = url.getContent();
        if (content != null) {
          return url;
        }
      }
      catch (IOException e) {
      	log.error(e);
      }
    }
    log.info("image not available: " + url);
    return null;
  }

}
