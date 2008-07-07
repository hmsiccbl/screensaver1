// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.util.List;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.PropertyPath;
import edu.harvard.med.screensaver.model.RelationshipPath;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUserClassification;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.TextEntityColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.VocabularyEntityColumn;
import edu.harvard.med.screensaver.ui.users.UserViewer;
import edu.harvard.med.screensaver.ui.util.ScreeningRoomUserClassificationConverter;

public class ScreenerSearchResults extends UserSearchResults<ScreeningRoomUser>
{
  protected ScreenerSearchResults()
  {
  }

  public ScreenerSearchResults(GenericEntityDAO dao,
                               UserViewer userViewer)
  {
    super(ScreeningRoomUser.class, dao, userViewer);
  }
  
  @Override
  protected List<? extends TableColumn<ScreeningRoomUser,?>> buildColumns()
  {
    List<TableColumn<ScreeningRoomUser,?>> columns = (List<TableColumn<ScreeningRoomUser,?>>) super.buildColumns();
    columns.add(3, new TextEntityColumn<ScreeningRoomUser>(
      new RelationshipPath<ScreeningRoomUser>(ScreeningRoomUser.class, "labHead"),
      "Lab Name", "The name of the lab with which the user is associated", TableColumn.UNGROUPED) {
      @Override
      public String getCellValue(ScreeningRoomUser user)
      {
        return user.getLab().getLabName();
      }
    });
    columns.add(4, new VocabularyEntityColumn<ScreeningRoomUser,ScreeningRoomUserClassification>(
      new PropertyPath<ScreeningRoomUser>(ScreeningRoomUser.class, "userClassification"),
      "User Classification", "The user's classsification", TableColumn.UNGROUPED,
      new ScreeningRoomUserClassificationConverter(),
      ScreeningRoomUserClassification.values()) {
      @Override
      public ScreeningRoomUserClassification getCellValue(ScreeningRoomUser user)
      {
        return user.getUserClassification();
      }
    });
    return columns;
  }
}
