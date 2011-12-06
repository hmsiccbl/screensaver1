// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.io.libraries.smallmolecule;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.io.libraries.smallmolecule.StructureImageLocator;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.util.CryptoUtils;

/**
 * Service that provides images of small molecule structures, given a
 * {@link SmallMoleculeReagent}. The images will have been pre-generated and stored in a
 * directory structure under the specified base URL as:
 * 
 * <code>d<sub>1</sub>/d<sub>2</sub>/d<sub>1</sub>d<sub>2</sub>d<sub>3</sub>...d<sub>40</sub></code>
 * where d<sub>1</sub>...d<sub>40</sub> is the 40-digit
 * SHA5 hash of the reagent's smiles identifier.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @see SmallMoleculeLibraryStructureImageGenerator
 */
public class StaticHashedSmilesStructureImageLocator implements StructureImageLocator
{
  private static Logger log = Logger.getLogger(StaticHashedSmilesStructureImageLocator.class);

  private String _baseUrl;

  public StaticHashedSmilesStructureImageLocator(String baseUrl)
  {
    _baseUrl = baseUrl;
  }

  @Override
  public URL getImageUrl(SmallMoleculeReagent reagent)
  {
    try {
      return new URL(_baseUrl + makeRelativeImageFilePath(reagent.getSmiles()));
    }
    catch (MalformedURLException e) {
      return null;
    }
    catch (IOException e) {
      return null;
    }
  }
  
  static String makeRelativeImageFilePath(String smiles) throws IOException
  {
    String hashedSmiles = CryptoUtils.digest(smiles);
    File outputDirectory = new File(hashedSmiles.substring(0, 1), 
                                    hashedSmiles.substring(1, 2));
    File imageFile = new File(outputDirectory, hashedSmiles);
    return imageFile.toString();
  }
  
}
