// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.users.AffiliationCategory;
import edu.harvard.med.screensaver.model.users.Lab;
import edu.harvard.med.screensaver.model.users.LabHead;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUserClassification;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.TextEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.VocabularyEntityColumn;
import edu.harvard.med.screensaver.ui.users.UserViewer;
import edu.harvard.med.screensaver.ui.util.AffiliationCategoryConverter;
import edu.harvard.med.screensaver.ui.util.ScreeningRoomUserClassificationConverter;

public class ScreenerSearchResults extends UserSearchResults<ScreeningRoomUser>
{
  private GenericEntityDAO _dao;

  protected ScreenerSearchResults()
  {
  }

  public ScreenerSearchResults(GenericEntityDAO dao,
                               UserViewer userViewer)
  {
    super(ScreeningRoomUser.class, dao, userViewer);
    _dao = dao;
  }

  @Transactional
  public void searchAssociatedUsers(ScreeningRoomUser screener)
  {
    screener = _dao.reloadEntity(screener, true, "labHead", "labMembers");
    searchUsers(screener.getAssociatedUsers(), "Associates of screener " + screener.getFullNameFirstLast());
  }

  @Override
  protected List<? extends TableColumn<ScreeningRoomUser,?>> buildColumns()
  {
    List<TableColumn<ScreeningRoomUser,?>> columns = (List<TableColumn<ScreeningRoomUser,?>>) super.buildColumns();

    columns.add(3, new VocabularyEntityColumn<ScreeningRoomUser,ScreeningRoomUserClassification>(
      RelationshipPath.from(ScreeningRoomUser.class).toProperty("userClassification"),
      "User Classification", "The user's classsification", TableColumn.UNGROUPED,
      new ScreeningRoomUserClassificationConverter(),
      ScreeningRoomUserClassification.values()) {
      @Override
      public ScreeningRoomUserClassification getCellValue(ScreeningRoomUser user)
      {
        return user.getUserClassification();
      }
    });

    
    columns.add(4, new TextEntityColumn<ScreeningRoomUser>(
      RelationshipPath.from(ScreeningRoomUser.class).to(ScreeningRoomUser.LabHead),
      "Lab Name", "The name of the lab with which the user is associated", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(ScreeningRoomUser user)
      {
        return user.getLab().getLabName();
      }
    });

    columns.add(5, new VocabularyEntityColumn<ScreeningRoomUser,AffiliationCategory>(RelationshipPath.from(ScreeningRoomUser.class).to(ScreeningRoomUser.LabHead).to(LabHead.labAffiliation),
      "Lab Affiliation Category", "The lab affiliation category", TableColumn.UNGROUPED,
      new AffiliationCategoryConverter(),
      AffiliationCategory.values()) {
      @Override
      public AffiliationCategory getCellValue(ScreeningRoomUser user)
      {
        Lab lab = user.getLab();
        if(lab != null)
        {
          return lab.getLabAffiliation() == null ? null : lab.getLabAffiliation().getAffiliationCategory();
        }
        return null;
      }
    });
    columns.get(columns.size() - 1).setAdministrative(true);
    columns.get(5).setVisible(false);

    return columns;
  }
}
