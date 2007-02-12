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

import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.control.LibrariesController;
import edu.harvard.med.screensaver.ui.control.UIControllerMethod;
import edu.harvard.med.screensaver.ui.namevaluetable.CompoundNameValueTable;
import edu.harvard.med.screensaver.ui.searchresults.WellSearchResults;

import org.apache.log4j.Logger;

public class CompoundViewer extends AbstractBackingBean
{
  
  // private static stuff
  
  private static final Logger log = Logger.getLogger(CompoundViewer.class);
  private static final String _screensaver0ImageRenderer =
    "http://screensaver1:insecure@screensaver.med.harvard.edu/screenbank/compounds-screensaver1/render_molecule.png";
  
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
  
  
  // private instance fields
  
  private LibrariesController _librariesController;
  private Compound _compound;
  private WellSearchResults _wellSearchResults;
  private CompoundNameValueTable _compoundNameValueTable;
  private Well _parentWellOfInterest;

  
  // public getters and setters
  
  public LibrariesController getLibrariesController()
  {
    return _librariesController;
  }
  
  public void setLibrariesController(LibrariesController librariesController)
  {
    _librariesController = librariesController;
  }
  
  /**
   * Set the parent Well of interest, for which this compound is being viewed (a
   * compound can be in multiple wells, but the UI may want to be explicit about
   * which Well "led" to this viewer").
   * 
   * @param the parent Well of interest, for which this compound is being
   *          viewed; may be null
   * @return the parent Well of intereset
   */
  public void setParentWellOfInterest(Well parentWellOfInterest)
  {
    _parentWellOfInterest = parentWellOfInterest;
  }

  /**
   * Get the parent Well of interest, for which this compound is being viewed (a
   * compound can be in multiple wells, but the UI may want to be explicit about
   * which Well "led" to this viewer").
   * 
   * @return the parent Well of interest, for which this compound is being
   *         viewed; may be null
   */
  public Well getParentWellOfInterest()
  {
    return _parentWellOfInterest;
  }
  
  public Compound getCompound()
  {
    return _compound;
  }

  public void setCompound(Compound compound)
  {
    _compound = compound;
  }
  
  public WellSearchResults getWellSearchResults()
  {
    return _wellSearchResults;
  }

  public void setWellSearchResults(WellSearchResults wellSearchResults)
  {
    _wellSearchResults = wellSearchResults;
  }

  public CompoundNameValueTable getCompoundNameValueTable()
  {
    return _compoundNameValueTable;
  }

  public void setCompoundNameValueTable(CompoundNameValueTable compoundNameValueTable)
  {
    _compoundNameValueTable = compoundNameValueTable;
  }

  public Map<String,String> getCompoundImageUrl()
  {
    return _compoundImageUrl;
  }
  
  @UIControllerMethod
  public String viewCompound()
  {
    return _librariesController.viewCompound(_compound, _wellSearchResults);
  }
}
