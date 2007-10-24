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

}
