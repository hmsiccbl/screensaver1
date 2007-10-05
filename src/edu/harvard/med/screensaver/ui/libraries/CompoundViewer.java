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
import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.namevaluetable.CompoundNameValueTable;

import org.apache.log4j.Logger;

public class CompoundViewer extends AbstractBackingBean
{

  // private static stuff

  private static final Logger log = Logger.getLogger(CompoundViewer.class);


  // private instance fields

  private GenericEntityDAO _dao;

  private Compound _compound;
  private CompoundNameValueTable _compoundNameValueTable;
  private boolean _showNavigationBar;
  private Well _parentWellOfInterest;


  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected CompoundViewer()
  {
  }

  public CompoundViewer(GenericEntityDAO dao)
  {
    _dao = dao;
  }


  // public instance methods

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

  public Compound getCompound()
  {
    return _compound;
  }

  public void setCompound(Compound compound)
  {
    _compound = compound;
  }

  public CompoundNameValueTable getCompoundNameValueTable()
  {
    return _compoundNameValueTable;
  }

  public void setCompoundNameValueTable(CompoundNameValueTable compoundNameValueTable)
  {
    _compoundNameValueTable = compoundNameValueTable;
  }

  @UIControllerMethod
  public String viewCompound(final Compound compoundIn)
  {
    _dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        if (compoundIn != null) {
          Compound compound = _dao.reloadEntity(compoundIn, true);
          _dao.needReadOnly(compound,
                            "compoundNames",
                            "pubchemCids",
                            "casNumbers",
                            "nscNumbers",
                            "wells.library");
          setCompound(compound);
          setCompoundNameValueTable(new CompoundNameValueTable(compound, CompoundViewer.this));
        }
        else {
          setCompound(null);
          setCompoundNameValueTable(null);
        }
      }
    });
    return VIEW_COMPOUND;
  }

  @UIControllerMethod
  public String viewCompound(Compound compound, Well forWell, boolean showNavigationBar)
  {
    _parentWellOfInterest = forWell;
    _showNavigationBar = showNavigationBar;
    return viewCompound(compound);
  }

}
