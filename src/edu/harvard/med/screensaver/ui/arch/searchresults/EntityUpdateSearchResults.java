// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.searchresults;

import java.io.Serializable;
import java.util.List;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.db.datafetcher.DataFetcherUtil;
import edu.harvard.med.screensaver.db.datafetcher.EntityDataFetcher;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.AuditedAbstractEntity;
import edu.harvard.med.screensaver.model.Entity;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.DateEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.DateTimeEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.TextEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.UserNameColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.VocabularyEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.model.InMemoryEntityDataModel;
import edu.harvard.med.screensaver.ui.arch.util.converter.VocabularyConverter;

public class EntityUpdateSearchResults<AE extends AuditedAbstractEntity<K>, K extends Serializable> 

  extends EntityBasedEntitySearchResults<AdministrativeActivity,Integer>
{
  private static Logger log = Logger.getLogger(EntityUpdateSearchResults.class);

  private GenericEntityDAO _dao;

  private AE _auditedEntity;


  protected EntityUpdateSearchResults() {}

  public EntityUpdateSearchResults(GenericEntityDAO dao) 
  {
    _dao = dao;
  }

  @Override
  public void searchAll()
  {
    setTitle("");
    throw new UnsupportedOperationException("should only be used to find update activities for a single entity");
  }

  public void searchForParentEntity(AE auditedEntity)
  {
    setTitle("Update History for " + auditedEntity); // TODO: need user-friendly toString(), see [#2560]
    _auditedEntity = _dao.reloadEntity(auditedEntity, true, AuditedAbstractEntity.updateActivities.getPath());

    EntityDataFetcher<AdministrativeActivity,Integer> dataFetcher =
      new EntityDataFetcher<AdministrativeActivity,Integer>(AdministrativeActivity.class, _dao) {
      @Override
      public void addDomainRestrictions(HqlBuilder hql)
      {
        DataFetcherUtil.addDomainRestrictions(hql, getRootAlias(), Sets.newHashSet(Iterables.transform(_auditedEntity.getUpdateActivities(), Entity.ToEntityId)));
      }
      };
    initialize(new InMemoryEntityDataModel<AdministrativeActivity,Integer>(dataFetcher));
    getColumnManager().setSortColumnName("Date");
    getColumnManager().setSortDirection(SortDirection.DESCENDING);
  }

  public AE getAuditedEntity()
  {
    return _auditedEntity;
  }

  @Override
  protected List<? extends TableColumn<AdministrativeActivity,?>> buildColumns()
  {
    List<TableColumn<AdministrativeActivity,?>> columns = Lists.newArrayList();
    columns.add(new DateTimeEntityColumn<AdministrativeActivity>(RelationshipPath.from(AdministrativeActivity.class).toProperty("dateCreated"),
                                                                 "Date Recorded", "The date the update was recorded",
                                                                 TableColumn.UNGROUPED) {
      @Override
      protected DateTime getDateTime(AdministrativeActivity activity)
      {
        return activity.getDateCreated();
      }
    });

    columns.add(new DateEntityColumn<AdministrativeActivity>(RelationshipPath.from(AdministrativeActivity.class).toProperty("dateOfActivity"),
                                                             "Date of Activity", "The date that a corresponding real-world activity was performed",
      TableColumn.UNGROUPED) {
      @Override
      protected LocalDate getDate(AdministrativeActivity activity)
      {
        return activity.getDateOfActivity();
      }
    });

    columns.add(new UserNameColumn<AdministrativeActivity,AdministratorUser>(
      RelationshipPath.from(AdministrativeActivity.class).to("performedBy"),
      "Updated By", "The person who made the update",
      TableColumn.UNGROUPED, 
      null) {
      @Override public AdministratorUser getUser(AdministrativeActivity a) { return (AdministratorUser) a.getPerformedBy(); }
    });

    columns.add(new VocabularyEntityColumn<AdministrativeActivity,AdministrativeActivityType>(RelationshipPath.from(AdministrativeActivity.class).toProperty("type"),
                                                                                              "Update Type", "The type of the update",
                                                                                              TableColumn.UNGROUPED,
                                                                                              new VocabularyConverter<AdministrativeActivityType>(AdministrativeActivityType.values()),
                                                                                              AdministrativeActivityType.values()) {
      @Override
      public AdministrativeActivityType getCellValue(AdministrativeActivity activity)
      {
        return activity.getType();
      }
    });

    columns.add(new TextEntityColumn<AdministrativeActivity>(RelationshipPath.from(AdministrativeActivity.class).toProperty("comments"),
      "Change", "Description of the change",
      TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(AdministrativeActivity activity) { return activity.getComments(); }
    });

    return columns;
  }
}
