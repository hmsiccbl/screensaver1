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
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.namevaluetable.GeneNameValueTable;

public class GeneViewer extends AbstractBackingBean
{

  // private instance fields

  private GenericEntityDAO _dao;

  private Gene _gene;
  private GeneNameValueTable _geneNameValueTable;


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
}
