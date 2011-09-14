// $HeadURL: http://forge.abcd.harvard.edu/svn/screensaver/branches/iccbl/2.3.2-dev/core/src/main/java/edu/harvard/med/screensaver/ui/activities/ActivitySearchResults.java $
// $Id: ActivitySearchResults.java 5158 2011-01-06 14:26:53Z atolopko $

// Copyright © 2006, 2010 by the President and Fellows of Harvard College.

// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.attachedFiles;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.EntityInflator;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.datafetcher.Tuple;
import edu.harvard.med.screensaver.db.datafetcher.TupleDataFetcher;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.model.AttachedFile;
import edu.harvard.med.screensaver.model.AttachedFileType;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.screens.ScreenAttachedFileType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.UserAttachedFileType;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.DateTupleColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.IntegerTupleColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.TextTupleColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.VocabularyTupleColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.model.DataTableModel;
import edu.harvard.med.screensaver.ui.arch.datatable.model.InMemoryEntityDataModel;
import edu.harvard.med.screensaver.ui.arch.searchresults.SearchResults;
import edu.harvard.med.screensaver.ui.arch.searchresults.TupleBasedEntitySearchResults;
import edu.harvard.med.screensaver.ui.arch.util.AttachedFiles;
import edu.harvard.med.screensaver.ui.arch.util.converter.VocabularyConverter;
import edu.harvard.med.screensaver.ui.screens.ScreenViewer;
import edu.harvard.med.screensaver.ui.users.UserViewer;

/**
 * AttachedFile {@link SearchResults} for {@link AttachedFile AttachedFiles}.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class AttachedFileSearchResults extends TupleBasedEntitySearchResults<AttachedFile,Integer>
{
  private static final Logger log = Logger.getLogger(AttachedFileSearchResults.class);

  private UserViewer _userViewer;
  private ScreenViewer _screenViewer;

  private Set<AttachedFileType> _attachedFileTypesFilter;
  private TreeSet<AttachedFileType> _allAttachedFileTypes;

  private TreeSet<AttachedFileType> _letterOfSupportAttachedFileTypes;

  /**
   * @motivation for CGLIB2
   */
  protected AttachedFileSearchResults()
  {
  }

  public AttachedFileSearchResults(GenericEntityDAO dao,
                                   UserViewer userViewer,
                                   ScreenViewer screenViewer)
  {
    super(AttachedFile.class, dao, null);
    _userViewer = userViewer;
    _screenViewer = screenViewer;

  }

  @Override
  public void searchAll()
  {
    _attachedFileTypesFilter = null;
    setTitle("Attached Files");
    // note: using TupleDataFetcher to avoid eager fetching all attached files' *contents*, as EntityDataFetcher does
    initialize(new InMemoryEntityDataModel<AttachedFile,Integer,Tuple<Integer>>(new TupleDataFetcher<AttachedFile,Integer>(AttachedFile.class, _dao)));
  }

  public void searchForTypes(final Set<AttachedFileType> attachedFileTypes, String title)
  {
    _attachedFileTypesFilter = attachedFileTypes;
    setTitle(title);
    // note: using TupleDataFetcher to avoid eager fetching all attached files' *contents*, as EntityDataFetcher does
    initialize(new InMemoryEntityDataModel<AttachedFile,Integer,Tuple<Integer>>(new TupleDataFetcher<AttachedFile,Integer>(AttachedFile.class, _dao) {
      @Override
      public void addDomainRestrictions(HqlBuilder hql)
      {
        hql.from(getRootAlias(), AttachedFile.fileType, "ft");
        hql.whereIn("ft", getAttachedFileTypesFilter());
      }
    }));
  }

  public void initialize(DataTableModel<Tuple<Integer>> model)
  {
    _allAttachedFileTypes = Sets.newTreeSet(_dao.findAllEntitiesOfType(AttachedFileType.class));
    super.initialize(model);
  }

  @Override
  protected List<? extends TableColumn<Tuple<Integer>,?>> buildColumns()
  {
    List<TableColumn<Tuple<Integer>,?>> columns = Lists.newArrayList();

    if (_attachedFileTypesFilter == null ||
      Iterables.any(_attachedFileTypesFilter, Predicates.instanceOf(UserAttachedFileType.class))) {
      columns.add(new IntegerTupleColumn<AttachedFile,Integer>(RelationshipPath.from(AttachedFile.class).to(AttachedFile.screeningRoomUser).toProperty("id"),
        "Screener ID",
        "The identifier of the screener to which the file is attached",
        TableColumn.UNGROUPED) {

        @Override
        public Object cellAction(Tuple<Integer> t)
        {
          return _userViewer.viewEntity(getAttachedFile(t).getScreeningRoomUser());
        }

        @Override
        public boolean isCommandLink()
        {
          return true;
        }
      });

      columns.add(new TextTupleColumn<AttachedFile,Integer>(RelationshipPath.from(AttachedFile.class).to(AttachedFile.screeningRoomUser).toProperty("lastName"),
        "User Last Name",
        "The last name of the user to which the file is attached",
        TableColumn.UNGROUPED));
      columns.add(new TextTupleColumn<AttachedFile,Integer>(RelationshipPath.from(AttachedFile.class).to(AttachedFile.screeningRoomUser).toProperty("firstName"),
        "User First Name",
        "The first name of the user to which the file is attached",
        TableColumn.UNGROUPED));

      final PropertyPath<AttachedFile> labHeadLabAffiliationPropertyPath = RelationshipPath.from(AttachedFile.class).to(AttachedFile.screeningRoomUser).to(ScreeningRoomUser.LabHead).to(LabHead.labAffiliation).toProperty("affiliationName");
      columns.add(new TextTupleColumn<AttachedFile,Integer>(RelationshipPath.from(AttachedFile.class).to(AttachedFile.screeningRoomUser).to(LabHead.labAffiliation).toProperty("affiliationName"),
        "User Lab Affiliation",
        "The lab affiliation of the user to which the file is attached",
        TableColumn.UNGROUPED) {
        @Override
        public String getCellValue(Tuple<Integer> tuple)
        {
          String cellValue = super.getCellValue(tuple);
          if (cellValue == null) {
            cellValue = (String) tuple.getProperty(TupleDataFetcher.makePropertyKey(labHeadLabAffiliationPropertyPath));
          }
          return cellValue;
        }
      });
      ((TextTupleColumn) Iterables.getLast(columns)).addRelationshipPath(labHeadLabAffiliationPropertyPath);
    }

    if (_attachedFileTypesFilter == null ||
      Iterables.any(_attachedFileTypesFilter, Predicates.instanceOf(ScreenAttachedFileType.class))) {
      columns.add(new TextTupleColumn<AttachedFile,Integer>(AttachedFile.screen.toProperty("facilityId"),
        "Screen",
        "The screen to which the file is attached",
        TableColumn.UNGROUPED) {
        @Override
        public Object cellAction(Tuple<Integer> t)
        {
          return _screenViewer.viewEntity(getAttachedFile(t).getScreen());
        }

        @Override
        public boolean isCommandLink()
        {
          return true;
        }
      });
    }

    if (_attachedFileTypesFilter == null || _attachedFileTypesFilter.size() > 1) {
      columns.add(new VocabularyTupleColumn<AttachedFile,Integer,AttachedFileType>(RelationshipPath.from(AttachedFile.class).toProperty("fileType"),
        "Type",
        "The type of the attached file",
        TableColumn.UNGROUPED,
        new VocabularyConverter<AttachedFileType>(_allAttachedFileTypes), _allAttachedFileTypes));
    }

    columns.add(new TextTupleColumn<AttachedFile,Integer>(RelationshipPath.from(AttachedFile.class).toProperty("filename"),
      "File Name",
      "The name of the attached file",
      TableColumn.UNGROUPED) {
      @Override
      public Object cellAction(Tuple<Integer> t)
      {
        try {
          return AttachedFiles.doDownloadAttachedFile(getAttachedFile(t),
                                                      getFacesContext(),
                                                      _dao);
        }
        catch (Exception e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public boolean isCommandLink()
      {
        return true;
      }
    });

    columns.add(new DateTupleColumn<AttachedFile,Integer>(RelationshipPath.from(AttachedFile.class).toProperty("fileDate"),
                                                          "File Date",
                                                          "The date of the attached file",
                                                          TableColumn.UNGROUPED));

    return columns;
  }

  protected AttachedFile getAttachedFile(Tuple<Integer> t)
  {
    return new EntityInflator<AttachedFile>(_dao,
                                            _dao.findEntityById(AttachedFile.class, t.getKey()),
                                            true).
      need(AttachedFile.screen).
      need(AttachedFile.screeningRoomUser).inflate();
  }

  public Set<AttachedFileType> getAttachedFileTypesFilter()
  {
    return _attachedFileTypesFilter;
  }

  public String addLetterOfSupportForNewLabHead()
  {
    _userViewer.editNewEntity(new LabHead((AdministratorUser) getScreensaverUser()));
    _userViewer.getAttachedFiles().getNewAttachedFileType().setSelection(getLetterOfSupportAttachedFileTypes().first());
    return VIEW_USER;
  }

  public String addLetterOfSupportForNewScreeningRoomUser()
  {
    _userViewer.editNewEntity(new ScreeningRoomUser((AdministratorUser) getScreensaverUser()));
    _userViewer.getAttachedFiles().getNewAttachedFileType().setSelection(getLetterOfSupportAttachedFileTypes().first());
    return VIEW_USER;
  }

  private static final String LETTER_OF_SUPPORT_ATTACHED_FILE_TYPES_PREFIX = "Letter of Support%";

  public SortedSet<AttachedFileType> getLetterOfSupportAttachedFileTypes()
  {
    if (_letterOfSupportAttachedFileTypes == null) {
      _letterOfSupportAttachedFileTypes = Sets.newTreeSet(_dao.findEntitiesByHql(AttachedFileType.class,
                                                                                 "from CellLine aft where aft.value like ?",
                                                                                 LETTER_OF_SUPPORT_ATTACHED_FILE_TYPES_PREFIX));
      if (_letterOfSupportAttachedFileTypes == null) {
        throw new BusinessRuleViolationException("'" + LETTER_OF_SUPPORT_ATTACHED_FILE_TYPES_PREFIX +
          "' attached file types do not exist");
      }
    }
    return _letterOfSupportAttachedFileTypes;
  }
}
