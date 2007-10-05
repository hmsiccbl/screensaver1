// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.namevaluetable.GeneNameValueTable;

public class GeneViewer extends AbstractBackingBean
{

  // private instance fields

  private GenericEntityDAO _dao;

  private Gene _gene;
  private GeneNameValueTable _geneNameValueTable;
  private boolean _showNavigationBar;
  private Well _parentWellOfInterest;


  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected GeneViewer()
  {
  }

  public GeneViewer(GenericEntityDAO dao)
  {
    _dao = dao;
  }


  // public instance methods

  public Gene getGene()
  {
    return _gene;
  }

  public void setGene(Gene gene)
  {
    _gene = gene;
  }

  public GeneNameValueTable getGeneNameValueTable()
  {
    return _geneNameValueTable;
  }

  public void setGeneNameValueTable(GeneNameValueTable geneNameValueTable)
  {
    _geneNameValueTable = geneNameValueTable;
  }

  /**
   * @motivation for JSF saveState component
   */
  public void setShowNavigationBar(boolean showNavigationBar)
  {
    _showNavigationBar = showNavigationBar;
  }

  public boolean isShowNavigationBar()
  {
    return _showNavigationBar;
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

  @UIControllerMethod
  public String viewGene(final Gene geneIn)
  {
    _dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        if (geneIn != null) {
          Gene gene = _dao.reloadEntity(geneIn, false);
          _dao.needReadOnly(gene,
                            "genbankAccessionNumbers",
                            "silencingReagents.wells.library");
          setGene(gene);
          setGeneNameValueTable(new GeneNameValueTable(gene, GeneViewer.this));
        }
        else {
          setGene(null);
          setGeneNameValueTable(null);
        }
      }
    });
    return VIEW_GENE;
  }

  @UIControllerMethod
  public String viewGene(Gene gene, Well forWell, boolean showNavigationBar)
  {
    _parentWellOfInterest = forWell;
    _showNavigationBar = showNavigationBar;
    return viewGene(gene);
  }

}
