//$HeadURL$
//$Id$

//Copyright 2006 by the President and Fellows of Harvard College.

//Screensaver is an open-source project developed by the ICCB-L and NSRB labs
//at Harvard Medical School. This software is distributed under the terms of
//the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Observer;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.joda.time.DateTime;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.Query;
import edu.harvard.med.screensaver.db.datafetcher.AllEntitiesOfTypeDataFetcher;
import edu.harvard.med.screensaver.db.datafetcher.EntitySetDataFetcher;
import edu.harvard.med.screensaver.db.datafetcher.ParentedEntityDataFetcher;
import edu.harvard.med.screensaver.db.hibernate.HqlBuilder;
import edu.harvard.med.screensaver.db.hibernate.JoinType;
import edu.harvard.med.screensaver.io.DataExporter;
import edu.harvard.med.screensaver.model.PropertyPath;
import edu.harvard.med.screensaver.model.RelationshipPath;
import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.model.screens.StudyType;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.ui.libraries.CompoundViewer;
import edu.harvard.med.screensaver.ui.libraries.GeneViewer;
import edu.harvard.med.screensaver.ui.libraries.ReagentViewer;
import edu.harvard.med.screensaver.ui.table.Criterion.Operator;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.ui.table.column.TableColumnManager;
import edu.harvard.med.screensaver.ui.table.column.entity.EntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.ListEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.RealEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.TextEntityColumn;
import edu.harvard.med.screensaver.util.Triple;


/**
 * A {@link SearchResults} for {@link Reagent Reagents}. Provides
 * user-selectable annotation type columns.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class ReagentSearchResults extends EntitySearchResults<Reagent,ReagentVendorIdentifier> implements Observer
{

  // private static final fields

  private static final Logger log = Logger.getLogger(ReagentSearchResults.class);

  private static final String REAGENT_COLUMNS_GROUP = TableColumn.UNGROUPED;
  private static final String OUR_ANNOTATION_TYPES_COLUMN_GROUP = "Annotations";
  private static final String OTHER_ANNOTATION_TYPES_COLUMN_GROUP = "Annotations (Other Studies)";
  private static final String ANNOTATION_HEADER_COLUMN_GROUP = "Header Information";


  // instance fields

  private ReagentViewer _reagentViewer;
  private CompoundViewer _compoundViewer;
  private GeneViewer _geneViewer;
  private GenericEntityDAO _dao;
  private List<TableColumn<Reagent,?>> _summaryVisibleCols;

  private Study _study;
  private Set<ReagentVendorIdentifier> _reagentIds;


  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected ReagentSearchResults()
  {
  }

  /**
   * Construct a new <code>ReagentSearchResultsViewer</code> object.
   */
  public ReagentSearchResults(ReagentViewer reagentViewer,
                              CompoundViewer compoundViewer,
                              GeneViewer geneViewer,
                              GenericEntityDAO dao,
                              List<DataExporter<?>> dataExporters)
  {
    super(dataExporters);
    _reagentViewer = reagentViewer;
    _compoundViewer = compoundViewer;
    _geneViewer = geneViewer;
    _dao = dao;
  }

  public void searchAllReagents()
  {
    _study = null;
    _reagentIds = null;
    initialize(new AllEntitiesOfTypeDataFetcher<Reagent,ReagentVendorIdentifier>(Reagent.class, _dao));
    getColumnManager().getColumn("Silencing Reagents").setVisible(true);
    getColumnManager().getColumn("Compounds").setVisible(true);
  }

  public void searchReagentsForStudy(Study study)
  {
    _study = study;
    _reagentIds = null;
    initialize(new ParentedEntityDataFetcher<Reagent,ReagentVendorIdentifier>(
      Reagent.class,
      new RelationshipPath<Reagent>(Reagent.class, "studies"),
      study,
      _dao));
    getColumnManager().getColumn("Silencing Reagents").setVisible(study.getScreenType() == ScreenType.RNAI);
    getColumnManager().getColumn("Compounds").setVisible(study.getScreenType() == ScreenType.SMALL_MOLECULE);
    // show columns for this screenResult's data headers
    for (AnnotationType at : study.getAnnotationTypes()) {
      getColumnManager().getColumn(WellSearchResults.makeColumnName(at, _study.getStudyNumber())).setVisible(true);
    }
  }

  /**
   * Prepare the model for the subsequent view.
   */
  public void searchReagents(Set<ReagentVendorIdentifier> reagentIds)
  {
    _study = null;
    _reagentIds = reagentIds;
    
    /*new HashSet<String>();
    for (ReagentVendorIdentifier rvi : reagentIds) {
      _reagentIds.add(rvi.getReagentId());
    }*/
    initialize(new EntitySetDataFetcher<Reagent,ReagentVendorIdentifier>(Reagent.class, reagentIds, _dao));
    getColumnManager().getColumn("Silencing Reagents").setVisible(true);
    getColumnManager().getColumn("Compounds").setVisible(true);
  }

  // implementations of the SearchResults abstract methods

  @Override
  protected List<? extends TableColumn<Reagent,?>> buildColumns()
  {
    List<EntityColumn<Reagent,?>> columns = new ArrayList<EntityColumn<Reagent,?>>();
    buildReagentPropertyColumns(columns);
    buildAnnotationTypeColumns(columns);
    return columns;
  }

  @Override
  protected void setEntityToView(Reagent reagent)
  {
    //TODO: limit visible header info in reagent view
    //    _summaryVisibleCols = new ArrayList<TableColumn<Reagent,?>>();
    //    for(TableColumn<Reagent,?> col : getColumnManager().getAllColumns() )
    //    {
    //      if(col.isVisible()) _summaryVisibleCols.add(col);
    //    }
    //    _reagentViewer.viewReagent(reagent, _summaryVisibleCols);
    _reagentViewer.viewReagent(reagent);
  }


  // private instance methods

  private Well getRepresentativeWell(Reagent reagent)
  {
    Well representativeWell = Well.NULL_WELL;
    if (reagent.getWells().size() > 0) {
      representativeWell = reagent.getWells().iterator().next();
    }
    return representativeWell;
  }

  private boolean showWellContentsForScreenType(ScreenType screenType)
  {
    return _study == null || _study.getScreenType().equals(screenType);
  }

  private void buildReagentPropertyColumns(List<EntityColumn<Reagent,?>> columns)
  {
    columns.add(new TextEntityColumn<Reagent>(
      new PropertyPath<Reagent>(Reagent.class, "reagentIdString"),
      "Reagent Source ID",
      "The vendor-assigned identifier for the reagent.", REAGENT_COLUMNS_GROUP) {
      @Override
      public String getCellValue(Reagent reagent) { return reagent.getEntityId().getReagentId(); }

      @Override
      public boolean isCommandLink() { return true; }

      @Override
      public Object cellAction(Reagent reagent)
      {
        return viewSelectedEntity();
      }
    });
    columns.add(new TextEntityColumn<Reagent>(
      // TODO: note that "wells.gene" is loading multiple wells, when we should be restricting to a single well; problem will go away once Reagent entity is fully implemented and contains gene directly
      new PropertyPath<Reagent>(Reagent.class, "wells.gene", "geneName"),
      "Silencing Reagents",
      "The gene name for the silencing reagent in the well", REAGENT_COLUMNS_GROUP) {
      @Override
      public String getCellValue(Reagent reagent)
      {
        Gene gene = getRepresentativeWell(reagent).getGene();
        return gene == null ? null : gene.getGeneName();
      }

      @Override
      public boolean isCommandLink() { return true; }

      @Override
      public Object cellAction(Reagent reagent)
      {
        return _geneViewer.viewGene(getRepresentativeWell(reagent).getGene());
      }
    });
    columns.add(new ListEntityColumn<Reagent>(
      // TODO: note that "wells.compounds" is loading multiple wells, when we should be restricting to a single well; problem will go away once Reagent entity is fully implemented and contains compounds directly
      new PropertyPath<Reagent>(Reagent.class, "wells.compounds", "smiles"),
      "Compounds",
      "The SMILES for the compound in the well", REAGENT_COLUMNS_GROUP) {
      @Override
      public List<String> getCellValue(Reagent reagent)
      {
        List<String> smiles = new ArrayList<String>();
        for (Compound compound : getRepresentativeWell(reagent).getOrderedCompounds()) {
          smiles.add(compound.getSmiles());
        }
        return smiles;
      }

      @Override
      public boolean isCommandLink() { return true; }

      @Override
      public Object cellAction(Reagent reagent)
      {
        if (getRepresentativeWell(reagent).getCompounds().size() > 0) {
          // commandValue is really a smiles, not a compoundId
          String smiles = (String) getRequestParameter("commandValue");
          Compound compound = null;
          for (Compound compound2 : getRepresentativeWell(reagent).getCompounds()) {
            if (compound2.getSmiles().equals(smiles)) {
              compound = compound2;
              break;
            }
          }
          return _compoundViewer.viewCompound(compound);
        }
        return REDISPLAY_PAGE_ACTION_RESULT;
      }
    });
  }

  // TODO: make this code shared with WellSearchResults.buildAnnotationTypeColumn()
  private void buildAnnotationTypeColumns(List<EntityColumn<Reagent,?>> columns)
  {
    List<EntityColumn<Reagent,?>> otherColumns = new ArrayList<EntityColumn<Reagent,?>>();
    List<EntityColumn<Reagent,?>> headerColumns = new ArrayList<EntityColumn<Reagent,?>>();

    Set<Integer> processedStudies = new HashSet<Integer>();
    for (Triple<AnnotationType,Integer,String> atAndStudyNumberAndTitle: findValidAnnotationTypes()) {
      final AnnotationType annotationType = atAndStudyNumberAndTitle.getFirst();
      final Integer studyNumber = atAndStudyNumberAndTitle.getSecond();
      final String studyTitle = atAndStudyNumberAndTitle.getThird();
      EntityColumn<Reagent,?> column;

      if (annotationType.isNumeric()) {
        column = new RealEntityColumn<Reagent>(
          new PropertyPath<Reagent>(Reagent.class,
            "annotationValues[annotationType]",
            "numericValue",
            annotationType),
            WellSearchResults.makeColumnName(annotationType, studyNumber),
            WellSearchResults.makeColumnDescription(annotationType, studyNumber, studyTitle),
            makeColumnGroup(studyNumber, studyTitle)) {
          @Override
          public Double getCellValue(Reagent reagent)
          {
            AnnotationValue av = reagent.getAnnotationValues().get(annotationType);
            if (av != null) {
              return av.getNumericValue();
            }
            return null;
          }
        };
      }
      else {
        column = new TextEntityColumn<Reagent>(
          new PropertyPath<Reagent>(Reagent.class,
            "annotationValues[annotationType]",
            "value",
            annotationType),
            WellSearchResults.makeColumnName(annotationType, studyNumber),
            WellSearchResults.makeColumnDescription(annotationType, studyNumber, studyTitle),
            makeColumnGroup(studyNumber, studyTitle)) {
          @Override
          public String getCellValue(Reagent reagent)
          {
            AnnotationValue av = reagent.getAnnotationValues().get(annotationType);
            if (av != null) {
              return av.getValue();
            }
            return null;
          }
        };
      }

      // request eager fetching of annotationType, since Hibernate will otherwise fetch these with individual SELECTs
      column.addRelationshipPath(new RelationshipPath<Reagent>(Reagent.class, "annotationValues.annotationType"));

      if (column.getName().equals(OUR_ANNOTATION_TYPES_COLUMN_GROUP)) {
        columns.add(column);
        column.setVisible(true);
      }
      else {
        otherColumns.add(column);
        column.setVisible(false);
      }
      
      if (processedStudies.add(studyNumber)) {
        for (AnnotationHeaderColumn headerCol : EnumSet.allOf(AnnotationHeaderColumn.class)) {
          TextEntityColumn<Reagent> column1 = 
            new TextEntityColumn<Reagent>(
              new PropertyPath<Reagent>(
                Reagent.class,
                headerCol._path,
                headerCol._propertyName,
                annotationType),
                headerCol.getSummaryColName(studyNumber),
                WellSearchResults.makeColumnDescription(studyTitle,
                                                        headerCol._description,
                                                        studyNumber,
                                                        studyTitle),
                                                        makeHeaderColumnGroup(studyNumber,
                                                                              studyTitle)) {
            @Override
            public String getCellValue(Reagent reagent)
            {
              AnnotationHeaderColumn headerCol = null;
              if ((headerCol = AnnotationHeaderColumn.fromSummaryColName(getName())) != null) {
                return headerCol.getValue(reagent, annotationType);
              }
              else {
                log.warn("cannot find the annotation type for the column: " + getName());
              }
              return null;
            }
          };
          column1.setVisible(false);
          headerColumns.add(column1);
        }
      }      
    }
    
    columns.addAll(otherColumns);
    columns.addAll(headerColumns);
  }
  
  public enum AnnotationHeaderColumn
  {
    STUDY_NAME("Study Name", "Name of the Study", "annotationValues[annotationType].annotationType.study", "title")
    {
      public String getValueInternal(Reagent r, AnnotationType type)
      {
        return r.getAnnotationValues().get(type).getAnnotationType().getStudy().getTitle();
      }
    },
    
    DATE("Date", "Date this study was entered into the system", "annotationValues[annotationType].annotationType.study", "dateCreated")
    {
      public String getValueInternal(Reagent r, AnnotationType type)
      {
        DateTime d =  r.getAnnotationValues().get(type).getAnnotationType().getStudy().getDateCreated();
        return d == null? "" : DateFormat.getDateInstance(DateFormat.SHORT).format(d.toDate());
      }
    },
    
    STUDY_LEAD("Study Lead", "Lead Screener for the Study", "annotationValues[annotationType].annotationType.study.leadScreener", "name")
    {
      public String getValueInternal(Reagent r, AnnotationType type)
      {
        ScreeningRoomUser s = r.getAnnotationValues().get(type).getAnnotationType().getStudy().getLeadScreener();
        return s==null ? "" : s.getName();
      }
      
    },
    
    LAB_HEAD("Lab Head", "Lab Head", "annotationValues[annotationType].annotationType.study.labHead", "name")
    {
      public String getValueInternal(Reagent r, AnnotationType type)
      {
        ScreeningRoomUser s = r.getAnnotationValues().get(type).getAnnotationType().getStudy().getLabHead();
        return s==null ? "" : s.getName();
      }
      
    },
    
    COLLABORATORS("Collaborators", "Collaborators", "annotationValues[annotationType].annotationType.study.collaborators", PropertyPath.COLLECTION_OF_VALUES)
    {
      public String getValueInternal(Reagent r, AnnotationType type)
      {
        return r.getAnnotationValues().get(type).getAnnotationType().getStudy().getCollaboratorsString();
      }
      
    },
    
    SUMMARY("Summary", "Summary description of the study", "annotationValues[annotationType].annotationType.study", "summary")
    {
      public String getValueInternal(Reagent r, AnnotationType type)
      {
        return r.getAnnotationValues().get(type).getAnnotationType().getStudy().getSummary();
      }
    },

    STUDY_TYPE("Type", "Study type", "annotationValues[annotationType].annotationType.study", "studyType")
    {
      public String getValueInternal(Reagent r, AnnotationType type)
      {
        StudyType t = r.getAnnotationValues().get(type).getAnnotationType().getStudy().getStudyType();
        return t == null ? "" : t.getValue();
      }
    };
    
    public final String _basename;
    public final String _description;
    public final String _path; 
    public final String _propertyName;
    
    private AnnotationHeaderColumn(String basename, String description, String path, String propertyName)
    {
      _basename = basename;
      _description = description;
      _path = path;
      _propertyName = propertyName;
    }
    
    public String getSummaryColName(Integer studyNumber)
    {
      return String.format("%s [%d]", _basename, studyNumber);
    }
    
    public String getColName()
    {
      return _basename;
    }
    
    public String getDescription() { return _description; }
    
    public boolean matchesOnSummaryColName(TableColumn<Reagent,?> col, Integer studyNumber)
    {
      return col.getName().indexOf(_basename) == 0 &&
        col.getName().indexOf(studyNumber.toString()) > 0;
    }
    
    public String getValue(Reagent r, AnnotationType type)
    {
      if(r.getAnnotationValues().get(type) != null)
      {
        return getValueInternal(r,type);
      }
      return "";
    }
    
    public abstract String getValueInternal(Reagent r, AnnotationType type);
    
    public static AnnotationHeaderColumn fromSummaryColName(String colName)
    {
      for(AnnotationHeaderColumn roc: EnumSet.allOf(AnnotationHeaderColumn.class) )
      {
        if(colName.matches(roc._basename  + "\\ \\[\\d+\\]")  )
        {
          return roc;
        }
      }
      return null;
    }
    
  }
  
  private String makeColumnGroup(Integer studyNumber, String studyTitle)
  {
    String columnGroup;
    if (_study != null && studyNumber.equals(_study.getStudyNumber())) {
      columnGroup = OUR_ANNOTATION_TYPES_COLUMN_GROUP;
    }
    else {
      columnGroup = OTHER_ANNOTATION_TYPES_COLUMN_GROUP + TableColumnManager.GROUP_NODE_DELIMITER + studyNumber + " (" + studyTitle + ")";
    }
    return columnGroup;
  }
  
  private String makeHeaderColumnGroup(Integer studyNumber, String studyTitle)
  {
    return makeColumnGroup(studyNumber, studyTitle) + TableColumnManager.GROUP_NODE_DELIMITER + ANNOTATION_HEADER_COLUMN_GROUP;
  }

  private List<Triple<AnnotationType,Integer,String>> findValidAnnotationTypes()
  {
    return _dao.runQuery(new Query() {
      public List<Triple<AnnotationType,Integer,String>> execute(Session session)
      {
        HqlBuilder hql = new HqlBuilder();
        hql.distinctProjectionValues();
        hql.from(AnnotationType.class, "at").from("at", "study", "s", JoinType.INNER);
        hql.select("at").select("s", "screenNumber").select("s", "title");
        hql.orderBy("s", "screenNumber").orderBy("at", "ordinal");

        if ( _study != null) {
          hql.where("s", "screenType", Operator.EQUAL, _study.getScreenType());
        }
        else if (_reagentIds != null) {
          if (_reagentIds.size() == 0) {
            return Collections.emptyList();
          }
          // select annotation types for studies that have same screenType of the libraries containing the set of reagents in this search result
          hql.from(Library.class, "l").from("l", "wells", "w", JoinType.INNER).from("w", "reagent", "r", JoinType.INNER);
          hql.whereIn("r", "id", _reagentIds);
          hql.where("l", "screenType", Operator.EQUAL, "s", "screenType");
        }
        else {
          // find all annotation types; no additional HQL needed
        }
        if (log.isDebugEnabled()) {
          log.debug("findValidAnnotationTypes query: " + hql.toHql());
        }
        org.hibernate.Query query = hql.toQuery(session, true);
        query.setResultTransformer(new MetaDataColumnResultTransformer<AnnotationType>());
        List<Triple<AnnotationType,Integer,String>> result = query.list();
        return result;
      }
    });
  }
}
