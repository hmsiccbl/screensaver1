// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.io.libraries.compound;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import edu.harvard.med.screensaver.io.libraries.compound.StructureImageProvider;
import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.util.CryptoUtils;

import org.apache.log4j.Logger;

/**
 * Service that provides images of small molecule structures, given a
 * {@link Compound}. The images will have been pre-generated and stored in a
 * directory structure under the specified base URL as:
 * 
 * <code>d<sub>1</sub>/d<sub>2</sub>/d<sub>1</sub>d<sub>2</sub>d<sub>3</sub>...d<sub>40</sub></code>
 * where d<sub>1</sub>...d<sub>40</sub> is the 40-digit
 * SHA5 hash of the compound's smiles identifier.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @see SmallMoleculeLibraryStructureImageGenerator
 */
public class StaticHashedSmilesStructureImageProvider implements StructureImageProvider
{
  private static Logger log = Logger.getLogger(StaticHashedSmilesStructureImageProvider.class);

  private String _baseUrl;

  public StaticHashedSmilesStructureImageProvider(String baseUrl)
  {
    _baseUrl = baseUrl;
  }

  public InputStream getImage(Compound compound) throws IOException
  {
    String smiles = compound.getSmiles();
    if (smiles == null) {
      return null;
    }
    return getImageUrl(compound).openStream();
  }

  public URL getImageUrl(Compound compound)
  {
    try {
      return new URL(_baseUrl + makeRelativeImageFilePath(compound.getSmiles()));
    }
    catch (MalformedURLException e) {
      return null;
    }
    catch (IOException e) {
      return null;
    }
  }

  private String getPubchemCid(Compound compound)
  {
    if (compound.getPubchemCids().size() > 0) {
      return compound.getPubchemCids().iterator().next();
    }
    return null;
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
