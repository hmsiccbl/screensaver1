// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.ui.AbstractController;

public class CompoundViewerController extends AbstractController
{
  
  // private static stuff
  
  private static final Logger log = Logger.getLogger(CompoundViewerController.class);
  private static final String _screensaver0ImageRenderer =
    "http://screensaver.med.harvard.edu/screenbank/compounds/render_molecule.png";
  
  /**
   * This map is a workaround for the JSF EL limitation of no parameters allowed to methods. 
   */
  private static final Map<String,String> _compoundImageUrl = new HashMap<String,String>() {
    private static final long serialVersionUID = 1L;
    public String get(Object key)
    {
      String smiles = (String) key;
      try {
        smiles = URLEncoder.encode(smiles, "UTF-8");
      }
      catch (UnsupportedEncodingException ex){
        throw new RuntimeException("UTF-8 not supported", ex);
      }
      return _screensaver0ImageRenderer + "?smiles=" + smiles;
    }
  };
  
  
  // instance stuff
  
  private Compound _compound;

  public Compound getCompound()
  {
    return _compound;
  }

  public void setCompound(Compound compound)
  {
    _compound = compound;
  }
  
  public Map<String,String> getCompoundImageUrl()
  {
    return _compoundImageUrl;
  }
  
  public String showCompound()
  {
    return "showCompound";
  }
}
