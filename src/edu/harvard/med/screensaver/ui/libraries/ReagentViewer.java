//$HeadURL$
//$Id$

//Copyright 2006 by the President and Fellows of Harvard College.

//Screensaver is an open-source project developed by the ICCB-L and NSRB labs
//at Harvard Medical School. This software is distributed under the terms of
//the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.Maps;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.io.libraries.compound.StructureImageProvider;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.EntityViewer;
import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.namevaluetable.NameValueTable;
import edu.harvard.med.screensaver.ui.namevaluetable.ReagentNameValueTable;
import edu.harvard.med.screensaver.ui.screens.StudyViewer;
import edu.harvard.med.screensaver.ui.searchresults.ReagentSearchResults.AnnotationHeaderColumn;
import edu.harvard.med.screensaver.ui.table.SimpleCell;
import edu.harvard.med.screensaver.util.StringUtils;

public class ReagentViewer extends AbstractBackingBean implements EntityViewer
{
  // static members

  private static Logger log = Logger.getLogger(ReagentViewer.class);


  // private instance fields

  protected GenericEntityDAO _dao;
  protected GeneViewer _geneViewer;
  protected CompoundViewer _compoundViewer;
  protected StructureImageProvider _structureImageProvider;

  private Reagent _reagent;
  private Collection<Gene> _genes;
  private Collection<Compound> _compounds;
  private NameValueTable _nameValueTable;
  private NameValueTable _annotationNameValueTable;
  private StudyViewer _studyViewer;


  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected ReagentViewer()
  {
    _studyViewer = null;
  }

  public ReagentViewer(GenericEntityDAO dao,
                       GeneViewer geneViewer,
                       CompoundViewer compoundViewer,
                       StructureImageProvider structureImageProvider,
                       StudyViewer studyViewer)
  {
    _dao = dao;
    _geneViewer = geneViewer;
    _compoundViewer = compoundViewer;
    _structureImageProvider = structureImageProvider;
    _studyViewer = studyViewer;
  }


  // public instance methods

  public AbstractEntity getEntity()
  {
    return getReagent();
  }

  public Reagent getReagent()
  {
    return _reagent;
  }

  public void setReagent(Reagent reagent,
                         Collection<Gene> genes,
                         Collection<Compound> compounds )
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
      //TODO: remove annotations that the user has not selected to view, 
      // also use user settings to see which annotations to view
    }
    _genes = genes;
    _compounds = compounds;
    
    // Optional header information
    // Note, rather than Lazy load the table, (i.e. extend DataTableModelLazyUpdateDecorator)
    // Just fill the whole table now, since if this is being created, then
    // we will need the data anyway.
    // group by study
    Map<Integer,List<SimpleCell>> studyNumberToStudyInfoMap
        = Maps.newHashMapWithExpectedSize(annotationValues.size());
    
    String summary ="s"; 
    for(AnnotationValue value: annotationValues)
    {
      final AnnotationType type = value.getAnnotationType();
      // Once per study
      Integer studyNumber = value.getAnnotationType().getStudy().getStudyNumber();
      if(! studyNumberToStudyInfoMap.containsKey(studyNumber) )
      {
        // create empty list either way
        List<SimpleCell> headerInfo = new ArrayList<SimpleCell>();
        
        //TODO: allow user to chose header colums:
        //        for(ChosenAnnotationColumn chosenColumn : chosenAnnotationColumns )
        //        {
        //          if(chosenColumn.getType().getStudy().getStudyNumber().equals(studyNumber))
        //          {
        
        // Now build a 2xn array of header values mapped to the study number
        for( AnnotationHeaderColumn headerColumn : EnumSet.allOf(AnnotationHeaderColumn.class))
        {
          String headerValue = headerColumn.getValue(reagent, type);
          if(!StringUtils.isEmpty(headerValue))
          {
            final Study study = type.getStudy();
            if(headerColumn == AnnotationHeaderColumn.STUDY_NAME )
            {
              headerInfo.add(
                new SimpleCell(headerColumn.getColName(), headerValue, headerColumn.getDescription()) 
                {
                  @Override
                  public boolean isCommandLink() { return true; }
    
                  @Override
                  public Object cellAction() 
                  { 
                    return _studyViewer.viewStudy(study); 
                  }
                });
            }else if(headerColumn == AnnotationHeaderColumn.SUMMARY ){
              // leave the summary field out for now - in the interest of UI readability (it is too long)
              //headerInfo.add(
              //               new SimpleCell(headerColumn.getColName(), headerValue, headerColumn.getDescription())
              //              .setSpanCell(true));
              summary = headerValue;
            }else{
              headerInfo.add(new SimpleCell(headerColumn.getColName(), headerValue, headerColumn.getDescription()));
            }
          }
        }
        // add even if empty - will just show the number
        studyNumberToStudyInfoMap.put(studyNumber, headerInfo);
      }
    }
    
    setAnnotationNameValueTable(new AnnotationNameValueTable(annotationValues, studyNumberToStudyInfoMap, null));
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

  private void setAnnotationNameValueTable(NameValueTable annotationNameValueTable)
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
                                                      _compoundViewer,
                                                      _structureImageProvider));
        }
      });
    }
    catch (IllegalArgumentException e) {
      showMessage("libraries.noSuchReagent", rvi);
      return REDISPLAY_PAGE_ACTION_RESULT;
    }
    return VIEW_REAGENT;
  }

  public Collection<Gene> getGenes()
  {
    return _genes;
  }

  public Collection<Compound> getCompounds()
  {
    return _compounds;
  }
  
  public boolean isSDFileDownloadEnabled() { return false; }
  
}

