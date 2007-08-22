// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.control.LibrariesController;
import edu.harvard.med.screensaver.ui.namevaluetable.WellNameValueTable;

import org.apache.log4j.Logger;

public class WellViewer extends AbstractBackingBean
{

  private static final Logger log = Logger.getLogger(WellViewer.class);


  // private instance fields

  private LibrariesController _librariesController;

  private Well _well;
  private WellNameValueTable _wellNameValueTable;


  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected WellViewer()
  {
  }

  public WellViewer(LibrariesController librariesController)
  {
    _librariesController = librariesController;
  }


  // public instance methods

  public Well getWell()
  {
    return _well;
  }

  public void setWell(Well well)
  {
    _well = well;
  }

  public WellNameValueTable getWellNameValueTable()
  {
    return _wellNameValueTable;
  }

  public void setWellNameValueTable(WellNameValueTable wellNameValueTable)
  {
    _wellNameValueTable = wellNameValueTable;
  }

  public String viewLibrary()
  {
    return _librariesController.viewLibrary(_well.getLibrary());
  }

  public String viewGene()
  {
    String geneId = (String) getFacesContext().getExternalContext().getRequestParameterMap().get("geneId");
    Gene gene = null;
    for (Gene gene2 : _well.getGenes()) {
      if (gene2.getGeneId().equals(geneId)) {
        gene = gene2;
        break;
      }
    }
    return _librariesController.viewGene(gene);
  }

  public String viewCompound()
  {
    String compoundId = (String) getRequestParameter("compoundId");
    Compound compound = null;
    for (Compound compound2 : _well.getCompounds()) {
      if (compound2.getCompoundId().equals(compoundId)) {
        compound = compound2;
        break;
      }
    }
    return _librariesController.viewCompound(compound);
  }

  public String downloadWellSDFile()
  {
    return _librariesController.downloadWellSDFile(_well);
  }
}
