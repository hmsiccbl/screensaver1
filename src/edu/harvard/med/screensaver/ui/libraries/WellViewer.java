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
import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.namevaluetable.WellNameValueTable;
import edu.harvard.med.screensaver.ui.util.JSFUtils;

import org.apache.log4j.Logger;

public class WellViewer extends AbstractBackingBean
{

  private static final Logger log = Logger.getLogger(WellViewer.class);


  // private instance fields

  private GenericEntityDAO _dao;
  private LibraryViewer _libraryViewer;
  private GeneViewer _geneViewer;
  private CompoundViewer _compoundViewer;

  private Well _well;
  private WellNameValueTable _wellNameValueTable;
  private boolean _showNavigationBar;


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
    _dao = dao;
    _libraryViewer = libraryViewer;
    _geneViewer = geneViewer;
    _compoundViewer = compoundViewer;
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
    _showNavigationBar = showNavigationBar;
    try {
      _dao.doInTransaction(new DAOTransaction() {
        public void runTransaction()
        {
          Well well = _dao.findEntityById(Well.class,
                                          wellKey.toString(),
                                          true,
                                          "hbnLibrary",
                                          "hbnSilencingReagents.gene.genbankAccessionNumbers",
                                          "hbnCompounds.compoundNames",
                                          "hbnCompounds.pubchemCids",
                                          "hbnCompounds.nscNumbers",
                                          "hbnCompounds.casNumbers");
          if (well == null) {
            throw new IllegalArgumentException("no such well");
          }
          setWell(well);
          setWellNameValueTable(new WellNameValueTable(well,
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
    return _geneViewer.viewGene(gene, _well, _showNavigationBar);
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
    return _compoundViewer.viewCompound(compound, _well, _showNavigationBar);
  }

  @UIControllerMethod
  public String downloadWellSDFile()
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
