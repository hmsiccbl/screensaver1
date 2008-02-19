// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.io.libraries.WellsDataExporter;
import edu.harvard.med.screensaver.io.libraries.WellsDataExporterFormat;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.namevaluetable.WellNameValueTable;
import edu.harvard.med.screensaver.ui.util.JSFUtils;

import org.apache.log4j.Logger;

public class WellViewer extends ReagentViewer
{

  private static final Logger log = Logger.getLogger(WellViewer.class);


  // private instance fields

  private LibraryViewer _libraryViewer;

  private Well _well;


  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected WellViewer()
  {
  }

  public WellViewer(GenericEntityDAO dao,
                    LibraryViewer libraryViewer,
                    GeneViewer geneViewer,
                    CompoundViewer compoundViewer)
  {
    super(dao, geneViewer, compoundViewer);
    _libraryViewer = libraryViewer;
  }


  // public instance methods

  public void setWell(Well well)
  {
    _well = well;
    setReagent(_well.getReagent(),
               _well.getGenes(),
               _well.getCompounds());
  }
  
  public Well getWell()
  {
    return _well;
  }

  @UIControllerMethod
  public String viewWell()
  {
    WellKey wellKey = new WellKey((String) getRequestParameter("wellId"));
    return viewWell(wellKey);
  }

  @UIControllerMethod
  public String viewWell(Well well)
  {
    if (well == null) {
      reportApplicationError("attempted to view an unknown well (not in database)");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    return viewWell(well.getWellKey());
  }

  @UIControllerMethod
  public String viewWell(final WellKey wellKey)
  {
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
    return _libraryViewer.viewLibrary(_well.getLibrary());
  }

  @UIControllerMethod
  public String downloadSDFile()
  {
    try {
      WellsDataExporter dataExporter = new WellsDataExporter(_dao, WellsDataExporterFormat.SDF);
      Set<Well> wells = new HashSet<Well>(1, 2.0f);
      wells.add(_well);
      InputStream inputStream = dataExporter.export(wells);
      JSFUtils.handleUserDownloadRequest(getFacesContext(),
                                         inputStream,
                                         dataExporter.getFileName(),
                                         dataExporter.getMimeType());
    }
    catch (IOException e) {
      reportApplicationError(e.toString());
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
}
