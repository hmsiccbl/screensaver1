// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.compound;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import edu.harvard.med.screensaver.model.libraries.Compound;

import org.apache.log4j.Logger;

public class PubChemStructureImageProvider implements StructureImageProvider
{
  // static members

  private static Logger log = Logger.getLogger(PubChemStructureImageProvider.class);
  
  private static final String IMAGE_URL_PREFIX =
    "http://pubchem.ncbi.nlm.nih.gov/image/imagefly.cgi?width=300&height=300&cid=";
  

  public InputStream getImage(Compound compound)
  {
    return null;
  }

  public URL getImageUrl(Compound compound)
  {
    try {
      String pubchemCid = getPubchemCid(compound);
      if (pubchemCid == null) {
        return null;
      }
      return new URL(IMAGE_URL_PREFIX + pubchemCid);
    }
    catch (MalformedURLException e) {
      // should never occur
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
    
  
//return _imageProvider.getImageUrl(_compound).toString();
//try {
//  value = URLEncoder.encode(value, "UTF-8");
//}
//catch (UnsupportedEncodingException ex){
//  throw new RuntimeException("UTF-8 not supported", ex);
//}
//return SMALL_MOLECULE_IMAGE_URL_PREFIX + value;


}
