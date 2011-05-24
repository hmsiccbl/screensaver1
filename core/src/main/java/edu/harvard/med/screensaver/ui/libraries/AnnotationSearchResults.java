// $HeadURL:  $
// $Id: WellSearchResults.java 4405 2010-07-15 16:15:38Z seanderickson1 $

// Copyright © 2006, 2010 by the President and Fellows of Harvard College.

// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.util.List;

import com.google.common.collect.Lists;
import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.datafetcher.DataFetcherUtil;
import edu.harvard.med.screensaver.db.datafetcher.EntityDataFetcher;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.screenresults.AnnotationValue;
import edu.harvard.med.screensaver.policy.EntityViewPolicy;
import edu.harvard.med.screensaver.ui.annotations.AnnotationViewer;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.TextEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.model.InMemoryEntityDataModel;
import edu.harvard.med.screensaver.ui.arch.searchresults.EntityBasedEntitySearchResults;
import edu.harvard.med.screensaver.ui.screens.StudyViewer;

public class AnnotationSearchResults extends EntityBasedEntitySearchResults<AnnotationValue,Integer>
{
  private static final Logger log = Logger.getLogger(AnnotationSearchResults.class);
  private GenericEntityDAO _dao;
  private StudyViewer _studyViewer;
  private EntityViewPolicy _entityViewPolicy;
  private Well _well;

  /**
   * @motivation for CGLIB2
   */
  protected AnnotationSearchResults()
  {}

  public AnnotationSearchResults(AnnotationViewer annotationViewer,
                                 StudyViewer studyViewer,
                                 EntityViewPolicy entityViewPolicy,
                                 GenericEntityDAO dao)
  {
    super(annotationViewer);
    _studyViewer = studyViewer;
    _dao = dao;
    _entityViewPolicy = entityViewPolicy;
  }

  public void searchAll()
  {
    EntityDataFetcher<AnnotationValue,Integer> dataFetcher =
      (EntityDataFetcher<AnnotationValue,Integer>) new EntityDataFetcher<AnnotationValue,Integer>(AnnotationValue.class, _dao);
    initialize(new InMemoryEntityDataModel<AnnotationValue,Integer>(dataFetcher));

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

    TextEntityColumn<AnnotationValue> studyColumn = new TextEntityColumn<AnnotationValue>(AnnotationValue.study.toProperty("summary"),
                                                                                          "Study Summary",
                                                                                          "Study Summary",
                                                                                          TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(AnnotationValue av)
      {
        return av.getAnnotationType().getStudy().getSummary();
      }
    };
    studyColumn.setVisible(false);
    columns.add(studyColumn);

    studyColumn = new TextEntityColumn<AnnotationValue>(AnnotationValue.study.toProperty("studyType"),
                                                                   "Study Type",
                                                                   "Type of study",
                                                                   TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(AnnotationValue av)
      {
        return av.getAnnotationType().getStudy().getStudyType().getValue();
      }
    };
    studyColumn.setVisible(false);
    columns.add(studyColumn);

    //    studyColumn = new TextEntityColumn<AnnotationValue>(AnnotationValue.study.toProperty("labHead"),
    //                                                                   "Lab Head",
    //                                                                   "Lab Head",
    //                                                                   TableColumn.UNGROUPED) {
    //      @Override
    //      public String getCellValue(AnnotationValue av)
    //      {
    //        return av.getAnnotationType().getStudy().getLabHead().getFullNameFirstLast();
    //      }
    //
    //      //      @SuppressWarnings("unchecked")
    //      //      @Override
    //      //      public Object cellAction(AnnotationValue av)
    //      //      {
    //      //        return _userViewer.viewEntity(av.getAnnotationType().getStudy().getLabHead());
    //      //      }
    //      //
    //      //      @Override
    //      //      public boolean isCommandLink()
    //      //      {
    //      //        return true;
    //      //      }
    //    };
    //    studyColumn.setVisible(false);
    //    columns.add(studyColumn);

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
      initialize(new InMemoryEntityDataModel<AnnotationValue,Integer>(new EntityDataFetcher<AnnotationValue,Integer>(AnnotationValue.class, _dao)
        {
          @Override
          public void addDomainRestrictions(HqlBuilder hql)
          {
            DataFetcherUtil.addDomainRestrictions(hql,
                                                  AnnotationValue.reagent,
                                                  well.getLatestReleasedReagent(),
                                                  getRootAlias());
            //            DataFetcherUtil.addDomainRestrictions(hql,
            //                                                  AnnotationValue.reagent.to(Reagent.well),
            //                                                  well,
            //                                                  getRootAlias());
          }
        }));
    }
  }
}