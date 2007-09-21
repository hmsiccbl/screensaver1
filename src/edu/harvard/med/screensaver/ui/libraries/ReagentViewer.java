//$HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
//$Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $

//Copyright 2006 by the President and Fellows of Harvard College.

//Screensaver is an open-source project developed by the ICCB-L and NSRB labs
//at Harvard Medical School. This software is distributed under the terms of
//the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.harvard.med.screensaver.db.AnnotationsDAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.io.libraries.WellsDataExporter;
import edu.harvard.med.screensaver.io.libraries.WellsDataExporterFormat;
import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.namevaluetable.NameValueTable;
import edu.harvard.med.screensaver.ui.namevaluetable.ReagentNameValueTable;
import edu.harvard.med.screensaver.ui.util.JSFUtils;

import org.apache.log4j.Logger;

public class ReagentViewer extends AbstractBackingBean
{
  // static members

  private static Logger log = Logger.getLogger(ReagentViewer.class);


  // private instance fields

  protected GenericEntityDAO _dao;
  private AnnotationsDAO _annotationsDao;
  protected GeneViewer _geneViewer;
  protected CompoundViewer _compoundViewer;

  private Well _well;
  private NameValueTable _nameValueTable;
  private NameValueTable _annotationNameValueTable;
  private boolean _showNavigationBar;


  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected ReagentViewer()
  {
  }

  public ReagentViewer(GenericEntityDAO dao,
                       AnnotationsDAO annotationsDao,
                       GeneViewer geneViewer,
                       CompoundViewer compoundViewer)
  {
    _dao = dao;
    _annotationsDao = annotationsDao;
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
    List<AnnotationValue> annotationValues =
      _annotationsDao.findAnnotationValuesForReagent(new ReagentVendorIdentifier(_well.getLibrary().getVendor(),
                                                                                 _well.getVendorIdentifier()));
    for (Iterator iterator = annotationValues.iterator(); iterator.hasNext();) {
      AnnotationValue annotationValue = (AnnotationValue) iterator.next();
      if (annotationValue.isRestricted()) {
        iterator.remove();
      }
    }
    setAnnotationNameValueTable(new AnnotationNameValueTable(annotationValues));
  }

  public NameValueTable getNameValueTable()
  {
    return _nameValueTable;
  }

  public void setNameValueTable(NameValueTable nameValueTable)
  {
    _nameValueTable = nameValueTable;
  }

  public NameValueTable getAnnotationNameValueTable()
  {
    return _annotationNameValueTable;
  }

  public void setAnnotationNameValueTable(NameValueTable annotationNameValueTable)
  {
    _annotationNameValueTable = annotationNameValueTable;
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
  public String viewReagent()
  {
    WellKey wellKey = new WellKey((String) getRequestParameter("wellId"));
    return viewReagent(wellKey, false);
  }

  @UIControllerMethod
  public String viewReagent(Well well)
  {
    return viewReagent(well, false);
  }

  @UIControllerMethod
  public String viewReagent(Well well, boolean showNavigationBar)
  {
    if (well == null) {
      reportApplicationError("attempted to view an unknown well (not in database)");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    return viewReagent(well.getWellKey(), showNavigationBar);
  }

  @UIControllerMethod
  public String viewReagent(final WellKey wellKey, boolean showNavigationBar)
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
            throw new IllegalArgumentException("no such reagent");
          }
          setWell(well);
          setNameValueTable(new ReagentNameValueTable(well,
                                                      ReagentViewer.this,
                                                      _geneViewer,
                                                      _compoundViewer));
        }
      });
    }
    catch (IllegalArgumentException e) {
      showMessage("libraries.noSuchWell", wellKey.getPlateNumber(), wellKey.getWellName());
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    return VIEW_REAGENT;
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

