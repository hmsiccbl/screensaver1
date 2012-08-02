// $HeadURL$
// $Id$

// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.

// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.db.datafetcher.DataFetcherUtil;
import edu.harvard.med.screensaver.db.datafetcher.EntityDataFetcher;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.policy.EntityViewPolicy;
import edu.harvard.med.screensaver.ui.annotations.AnnotationViewer;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.IntegerEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.TextEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.model.InMemoryEntityDataModel;
import edu.harvard.med.screensaver.ui.arch.searchresults.EntityBasedEntitySearchResults;
import edu.harvard.med.screensaver.ui.screens.StudyViewer;

public class AnnotationSearchResults extends EntityBasedEntitySearchResults<AnnotationValue,Integer>
{
  private static final Logger log = Logger.getLogger(AnnotationSearchResults.class);
  private GenericEntityDAO _dao;
  private StudyViewer _studyViewer;
  private WellViewer _wellViewer;
  private EntityViewPolicy _entityViewPolicy;
  private LibrariesDAO _librariesDao;
  private Well _well;
  private Screen _study;

  private boolean _isWellViewerMode = true;

  /**
   * @motivation for CGLIB2
   */
  protected AnnotationSearchResults()
  {}

  public AnnotationSearchResults(AnnotationViewer annotationViewer,
                                 StudyViewer studyViewer,
                                 WellViewer wellViewer,
                                 EntityViewPolicy entityViewPolicy,
                                 LibrariesDAO librariesDao,
                                 GenericEntityDAO dao)
  {
    super(annotationViewer);
    _studyViewer = studyViewer;
    _wellViewer = wellViewer;
    _dao = dao;
    _librariesDao = librariesDao;
    _entityViewPolicy = entityViewPolicy;
  }

  @Override
  public void initialize()
  {
    super.initialize();
  }

  public void searchAll()
  {
    EntityDataFetcher<AnnotationValue,Integer> dataFetcher =
      (EntityDataFetcher<AnnotationValue,Integer>) new EntityDataFetcher<AnnotationValue,Integer>(AnnotationValue.class, _dao);
    initialize(new InMemoryEntityDataModel<AnnotationValue,Integer,AnnotationValue>(dataFetcher));

    getColumnManager().setSortAscending(false);
  }

  // implementations of the SearchResults abstract methods

  @Override
  protected List<TableColumn<AnnotationValue,?>> buildColumns()
  {
    List<TableColumn<AnnotationValue,?>> columns = Lists.newArrayList();

    columns.add(new TextEntityColumn<AnnotationValue>(
                                                           AnnotationValue.study.toProperty("screenNumber"),
                                                           "Study ID",
                                                           "The study number",
                                                           TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(AnnotationValue av)
      {
        return av.getAnnotationType().getStudy().getFacilityId();
      }

      @SuppressWarnings("unchecked")
      @Override
      public Object cellAction(AnnotationValue av)
      {
        return _studyViewer.viewEntity(av.getAnnotationType().getStudy());
      }

      @Override
      public boolean isCommandLink()
      {
        return true;
      }
    });
    columns.get(columns.size() - 1).setVisible(_isWellViewerMode);

    columns.add(new TextEntityColumn<AnnotationValue>(AnnotationValue.study.toProperty("title"),
                                                                   "Study Title",
                                                                   "Study Title",
                                                                   TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(AnnotationValue av)
      {
        return av.getAnnotationType().getStudy().getTitle();
      }
    });
    columns.get(columns.size() - 1).setVisible(_isWellViewerMode);

    columns.add(new TextEntityColumn<AnnotationValue>(AnnotationValue.study.toProperty("summary"),
                                                                                          "Study Summary",
                                                                                          "Study Summary",
                                                                                          TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(AnnotationValue av)
      {
        return av.getAnnotationType().getStudy().getSummary();
      }
    });
    columns.get(columns.size() - 1).setVisible(false);

    columns.add(new TextEntityColumn<AnnotationValue>(AnnotationValue.study.toProperty("studyType"),
                                                                   "Study Type",
                                                                   "Type of study",
                                                                   TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(AnnotationValue av)
      {
        return av.getAnnotationType().getStudy().getStudyType().getValue();
      }
    });
    columns.get(columns.size() - 1).setVisible(false);

    if (!_isWellViewerMode) {
      TextEntityColumn column = new TextEntityColumn<AnnotationValue>(AnnotationValue.reagent.toProperty("well"),
                                                                      "Facility ID", "The facility ID for the compound studied",
                                                                      TableColumn.UNGROUPED) {

        @Override
        public String getCellValue(AnnotationValue row)
          {
            return row.getReagent().getWell().getFacilityId();
          }

        @Override
        public boolean isCommandLink()
          {
            return true;
          }

        @Override
        public Object cellAction(AnnotationValue row)
          {
            return _wellViewer.viewEntity(row.getReagent().getWell());
          }
      };
      column.addRelationshipPath(AnnotationValue.reagent.to(Reagent.well));
      column.setVisible(_study.getWellStudied() == null);
      columns.add(column);

      IntegerEntityColumn col2 = null;
      if (_study.getScreenType().equals(ScreenType.SMALL_MOLECULE)) {
        col2 = new IntegerEntityColumn<AnnotationValue>(AnnotationValue.reagent,
                                                                        "Salt ID", "The salt ID for the compound studied",
                                                                        TableColumn.UNGROUPED) {

          @Override
          public Integer getCellValue(AnnotationValue row)
            {
              return ((SmallMoleculeReagent) row.getReagent()).getSaltFormId();
            }

          @Override
          public boolean isCommandLink()
            {
              return true;
            }

          @Override
          public Object cellAction(AnnotationValue row)
            {
              return _wellViewer.viewEntity(row.getReagent().getWell());
            }
        };
        col2.addRelationshipPath(AnnotationValue.reagent.to(Reagent.well));
        col2.setVisible(false);
        columns.add(col2);
      }      
      
      if (_study.getScreenType().equals(ScreenType.SMALL_MOLECULE) && isLINCS()) { // added for LINCS
        column = new TextEntityColumn<AnnotationValue>(AnnotationValue.reagent,
            "Primary Compound Name", "The primary name for the compound studied",
            TableColumn.UNGROUPED) {

						@Override
						public String getCellValue(AnnotationValue row)
						{
							SmallMoleculeReagent smr = _dao.findEntityById(SmallMoleculeReagent.class, row.getReagent().getEntityId(), true, SmallMoleculeReagent.compoundNames);
							return smr.getPrimaryCompoundName();
							
							//return ((SmallMoleculeReagent)row.getReagent()).getCompoundNames().get(0);
							//return "blah";
						}
						
						@Override
						public boolean isCommandLink()
						{
						return true;
						}
						
						@Override
						public Object cellAction(AnnotationValue row)
						{
							return _wellViewer.viewEntity(row.getReagent().getWell());
						}
					};
					// TODO: this fails with NPE at hibernate BasicLoader:99
					//column.addRelationshipPath(AnnotationValue.reagent.toCollectionOfValues("compoundNames"));
					column.setVisible(false);
					columns.add(column);
      }

      col2 = new IntegerEntityColumn<AnnotationValue>(AnnotationValue.reagent,
                                                                        "Batch ID", "The batch ID for the compound studied",
                                                                        TableColumn.UNGROUPED) {

        @Override
        public Integer getCellValue(AnnotationValue row)
            {
              return row.getReagent().getFacilityBatchId();
            }

        @Override
        public boolean isCommandLink()
            {
              return true;
            }

        @Override
        public Object cellAction(AnnotationValue row)
            {
              return _wellViewer.viewEntity(row.getReagent().getWell());
            }
      };
      col2.addRelationshipPath(AnnotationValue.reagent.to(Reagent.well));
      col2.setVisible(false);
      columns.add(col2);
    }

    columns.add(new TextEntityColumn<AnnotationValue>(AnnotationValue.annotationType.toProperty("name"),
                                                                   "Annotation Name",
                                                                   "Name",
                                                                   TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(AnnotationValue av)
      {
        return av.getAnnotationType().getName();
      }
    });

    columns.add(new TextEntityColumn<AnnotationValue>(AnnotationValue.annotationType.toProperty("description"),
                                                                   "Annotation Description",
                                                                   "defined by the Study creator",
                                                                   TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(AnnotationValue av)
      {
        return av.getAnnotationType().getDescription();
      }
    });
    columns.get(columns.size() - 1).setVisible(false);

    columns.add(new TextEntityColumn<AnnotationValue>(AnnotationValue.annotationType,
                                                                   "Annotation Type",
                                                                   "'Numeric' or 'Text'",
                                                                   TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(AnnotationValue av)
      {
        return av.getAnnotationType().isNumeric() ? "Numeric" : "Text";
      }
    });
    columns.get(columns.size() - 1).setVisible(false);

    columns.add(new TextEntityColumn<AnnotationValue>(RelationshipPath.from(AnnotationValue.class).toProperty("value"),
                                                           "Value",
                                                           "Value of the annotation",
                                                           TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(AnnotationValue row)
      {
        return row.getFormattedValue();
      }
    });

    return columns;
  }

  public void searchForAnnotations(final Well well)
  {
    // setMode(AnnotationSearchResultMode.WELL_ANNOTATIONS);
    _well = well;
    if (well == null) {
      initialize();
    }
    else {
      initialize(new InMemoryEntityDataModel<AnnotationValue,Integer,AnnotationValue>(new EntityDataFetcher<AnnotationValue,Integer>(AnnotationValue.class, _dao)
        {
          @Override
          public void addDomainRestrictions(HqlBuilder hql)
          {
            DataFetcherUtil.addDomainRestrictions(hql,
                                                  AnnotationValue.reagent,
                                                  well.getLatestReleasedReagent(),
                                                  getRootAlias());
          }
        }));
    }
  }

  public void searchForCanonicalAnnotations(final Screen study) //LINCS ONLY
  {
    _study = study;
    // get the distinct facility ids
    Set<String> distinctFacilityIds = Sets.newHashSet();
    for (Reagent r : study.getReagents()) {
      distinctFacilityIds.add(r.getWell().getFacilityId());
    }
    // get the canonical wellkeys - this is valid because the study annotation parser (for facility ID as input) will annotate all reagents 
    // having the same facilty ID (but differing batch/salt ID), therefore, the "canonical" reagent will also be annotated.
    final Set<Integer> canonicalWellReagentIds = Sets.newHashSet();
    for (String facilityId : distinctFacilityIds) {
      Well canonicalReagentWell = _librariesDao.findCanonicalReagentWell(facilityId, null, null);
      if (canonicalReagentWell != null) {
        canonicalWellReagentIds.add(canonicalReagentWell.getLatestReleasedReagent().getEntityId());
      }
    }

    initialize(new InMemoryEntityDataModel<AnnotationValue,Integer,AnnotationValue>(
                                                                                    new EntityDataFetcher<AnnotationValue,Integer>(AnnotationValue.class, _dao)
        {
          @Override
          public void addDomainRestrictions(HqlBuilder hql)
          {
            hql.from(getRootAlias(), AnnotationValue.reagent, "r");
            DataFetcherUtil.addDomainRestrictions(hql, "r", canonicalWellReagentIds);
            DataFetcherUtil.addDomainRestrictions(hql, AnnotationValue.study, study, getRootAlias());
          }
        }));
  }

  public void setStudyViewerMode()
  {
    _isWellViewerMode = false;
  }
}