// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table.model;



import java.util.Iterator;
import java.util.List;

import edu.harvard.med.screensaver.db.datafetcher.EntityDataFetcher;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;


/**
 * An {@link InMemoryDataModel} that fetches data using an
 * {@link EntityDataFetcher}. Filters out top-level entities that are
 * restricted, as determined by the current {@link DataAccessPolicy}. Note that
 * this makes InMemoryEntityDataModel behave differently than
 * {@link VirtualPagingEntityDataModel}, which cannot apply the
 * DataAccessPolicy as that would require retrieving all of the entities.
 * 
 * @author drew
 */
public class InMemoryEntityDataModel<E extends AbstractEntity> extends InMemoryDataModel<E>
{
  // static members

  public InMemoryEntityDataModel(EntityDataFetcher<E,?> dataFetcher)
  {
    super(dataFetcher);
  }
  
  @Override
  public void fetch(List<? extends TableColumn<E,?>> columns)
  {
    ((EntityDataFetcher<E,?>) _dataFetcher).setRelationshipsToFetch(VirtualPagingEntityDataModel.getRelationshipPaths(columns));
    super.fetch(columns);
    for (Iterator<E> iter = _unfilteredData.iterator(); iter.hasNext();) {
      E entity = iter.next();
      if (entity.isRestricted()) {
        iter.remove();
      }
    }
  }
}
