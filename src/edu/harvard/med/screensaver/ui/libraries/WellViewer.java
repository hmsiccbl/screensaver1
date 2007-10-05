// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import edu.harvard.med.screensaver.db.AnnotationsDAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.namevaluetable.WellNameValueTable;

import org.apache.log4j.Logger;

public class WellViewer extends ReagentViewer
{

  private static final Logger log = Logger.getLogger(WellViewer.class);


  // private instance fields

  private LibraryViewer _libraryViewer;


  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected WellViewer()
  {
  }

  public WellViewer(GenericEntityDAO dao,
                    AnnotationsDAO annotationsDao,
                    LibraryViewer libraryViewer,
                    GeneViewer geneViewer,
                    CompoundViewer compoundViewer)
  {
    super(dao, annotationsDao, geneViewer, compoundViewer);
    _libraryViewer = libraryViewer;
  }


  // public instance methods

  @UIControllerMethod
  public String viewWell()
  {
    WellKey wellKey = new WellKey((String) getRequestParameter("wellId"));
    return viewWell(wellKey, false);
  }

  @UIControllerMethod
  public String viewWell(Well well)
  {
    return viewWell(well, false);
  }

  @UIControllerMethod
  public String viewWell(Well well, boolean showNavigationBar)
  {
    if (well == null) {
      reportApplicationError("attempted to view an unknown well (not in database)");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    return viewWell(well.getWellKey(), showNavigationBar);
  }

  @UIControllerMethod
  public String viewWell(final WellKey wellKey, boolean showNavigationBar)
  {
    setShowNavigationBar(showNavigationBar);
    try {
      _dao.doInTransaction(new DAOTransaction() {
        public void runTransaction()
        {
          Well well = _dao.findEntityById(Well.class,
                                          wellKey.toString(),
                                          true,
                                          "library",
                                          "silencingReagents.gene.genbankAccessionNumbers",
                                          "compounds.compoundNames",
                                          "compounds.pubchemCids",
                                          "compounds.nscNumbers",
                                          "compounds.casNumbers");
          if (well == null) {
            throw new IllegalArgumentException("no such well");
          }
          setWell(well);
          setNameValueTable(new WellNameValueTable(well,
                                                   WellViewer.this,
                                                   _libraryViewer,
                                                   _geneViewer,
                                                   _compoundViewer));
        }
      });
    }
    catch (IllegalArgumentException e) {
      showMessage("libraries.noSuchWell", wellKey.getPlateNumber(), wellKey.getWellName());
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    return VIEW_WELL;
  }

  public String viewLibrary()
  {
    return _libraryViewer.viewLibrary(getWell().getLibrary());
  }

}
