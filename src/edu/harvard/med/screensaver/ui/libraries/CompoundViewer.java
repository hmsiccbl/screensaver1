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
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.control.LibrariesController;
import edu.harvard.med.screensaver.ui.namevaluetable.CompoundNameValueTable;

import org.apache.log4j.Logger;

public class CompoundViewer extends AbstractBackingBean
{

  // private static stuff

  private static final Logger log = Logger.getLogger(CompoundViewer.class);


  // private instance fields

  private LibrariesController _librariesController;
  private Compound _compound;
  private CompoundNameValueTable _compoundNameValueTable;
  private Well _parentWellOfInterest;


  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected CompoundViewer()
  {
  }

  public CompoundViewer(LibrariesController librariesController)
  {
    _librariesController = librariesController;
  }


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
   * @param parentWellOfInterest the parent Well of interest, for which this
   *          compound is being viewed; may be null
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

  public CompoundNameValueTable getCompoundNameValueTable()
  {
    return _compoundNameValueTable;
  }

  public void setCompoundNameValueTable(CompoundNameValueTable compoundNameValueTable)
  {
    _compoundNameValueTable = compoundNameValueTable;
  }

  public String viewCompound()
  {
    return _librariesController.viewCompound(_compound);
  }
}
