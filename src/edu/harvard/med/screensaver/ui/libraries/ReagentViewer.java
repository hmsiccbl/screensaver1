//$HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
//$Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $

//Copyright 2006 by the President and Fellows of Harvard College.

//Screensaver is an open-source project developed by the ICCB-L and NSRB labs
//at Harvard Medical School. This software is distributed under the terms of
//the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.namevaluetable.NameValueTable;
import edu.harvard.med.screensaver.ui.namevaluetable.ReagentNameValueTable;

import org.apache.log4j.Logger;

public class ReagentViewer extends AbstractBackingBean
{
  // static members

  private static Logger log = Logger.getLogger(ReagentViewer.class);


  // private instance fields

  protected GenericEntityDAO _dao;
  protected GeneViewer _geneViewer;
  protected CompoundViewer _compoundViewer;

  private Reagent _reagent;
  private Collection<Gene> _genes;
  private Collection<Compound> _compounds;
  private NameValueTable _nameValueTable;
  private NameValueTable _annotationNameValueTable;


  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected ReagentViewer()
  {
  }

  public ReagentViewer(GenericEntityDAO dao,
                       GeneViewer geneViewer,
                       CompoundViewer compoundViewer)
  {
    _dao = dao;
    _geneViewer = geneViewer;
    _compoundViewer = compoundViewer;
  }


  // public instance methods

  public Reagent getReagent()
  {
    return _reagent;
  }

  public void setReagent(Reagent reagent,
                         Collection<Gene> genes,
                         Collection<Compound> compounds)
  {
    List<AnnotationValue> annotationValues = new ArrayList<AnnotationValue>();
    if (reagent != null) { // note: reagent can be null when used in the context of WellViewer subclass
      annotationValues.addAll(reagent.getAnnotationValues().values());
      for (Iterator iterator = annotationValues.iterator(); iterator.hasNext();) {
        AnnotationValue annotationValue = (AnnotationValue) iterator.next();
        if (annotationValue.isRestricted()) {
          iterator.remove();
        }
      }
    }
    _genes = genes;
    _compounds = compounds;
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

  @UIControllerMethod
  public String viewReagent()
  {
    ReagentVendorIdentifier rvi = new ReagentVendorIdentifier((String) getRequestParameter("reagentId"));
    return viewReagent(rvi);
  }

  @UIControllerMethod
  public String viewReagent(Reagent reagent)
  {
    if (reagent == null) {
      reportApplicationError("attempted to view an unknown reagent (not in database)");
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    return viewReagent(reagent.getReagentId());
  }

  @UIControllerMethod
  public String viewReagent(final ReagentVendorIdentifier rvi)
  {
    try {
      _dao.doInTransaction(new DAOTransaction() {

        public void runTransaction()
        {
          Reagent reagent = _dao.findEntityById(Reagent.class,
                                                rvi,
                                                true,
                                                "wells.library",
                                                "wells.silencingReagents.gene.genbankAccessionNumbers",
                                                "wells.compounds.compoundNames",
                                                "wells.compounds.pubchemCids",
                                                "wells.compounds.nscNumbers",
                                                "wells.compounds.casNumbers");
          if (reagent == null) {
            throw new IllegalArgumentException("no such reagent");
          }
          Set<Gene> genes = Collections.emptySet();
          Set<Compound> compounds = Collections.emptySet();
          if (reagent.getWells().size() > 0) {
            Well representativeWell = reagent.getWells().iterator().next();
            genes = representativeWell.getGenes();
            compounds = representativeWell.getCompounds();
          }
          setReagent(reagent,
                     genes,
                     compounds);
          setNameValueTable(new ReagentNameValueTable(reagent,
                                                      ReagentViewer.this,
                                                      _geneViewer,
                                                      _compoundViewer));
        }
      });
    }
    catch (IllegalArgumentException e) {
      showMessage("libraries.noSuchReagent", rvi);
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    return VIEW_REAGENT;
  }

  public String viewGene()
  {
    String geneId = (String) getFacesContext().getExternalContext().getRequestParameterMap().get("geneId");
    Gene gene = null;
    for (Gene gene2 : _genes) {
      if (gene2.getGeneId().equals(geneId)) {
        gene = gene2;
        break;
      }
    }
    return _geneViewer.viewGene(gene);
  }

  public String viewCompound()
  {
    String compoundId = (String) getRequestParameter("compoundId");
    Compound compound = null;
    for (Compound compound2 : _compounds) {
      if (compound2.getCompoundId().equals(compoundId)) {
        compound = compound2;
        break;
      }
    }
    return _compoundViewer.viewCompound(compound);
  }

  public Collection<Gene> getGenes()
  {
    return _genes;
  }

  public Collection<Compound> getCompounds()
  {
    return _compounds;
  }

}

