// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.compound;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import edu.harvard.med.screensaver.model.libraries.Compound;

/**
 * Interface for services that can provide images of small molecule structures,
 * given a {@link Compound}.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public interface StructureImageProvider
{
  /**
   * Get the URL identifying an image of the structure of the specified
   * Compound.
   * 
   * @param compound the Compound
   * @return URL identifying an image of the specified Compound; null if image
   *         cannot be provided for any reason
   */
  URL getImageUrl(Compound compound);

  /**
   * Get the InputStream providing the data for an image of the structure of the
   * specified Compound.
   * 
   * @param compound the Compound
   * @return InputStream providing the data for an image of the structure of the
   *         specified Compound.
   */
  InputStream getImage(Compound compound) throws IOException;
}
