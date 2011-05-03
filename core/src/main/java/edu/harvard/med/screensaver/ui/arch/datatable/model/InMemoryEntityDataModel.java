// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.datatable.model;



import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import edu.harvard.med.screensaver.db.datafetcher.EntityDataFetcher;
import edu.harvard.med.screensaver.model.Entity;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.policy.EntityViewPolicy;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.FetchPaths;


/**
 * An {@link InMemoryDataModel} that fetches data using an
 * {@link EntityDataFetcher}. Filters out top-level entities that are
 * restricted, as determined by the current {@link EntityViewPolicy}. Note that
 * this makes InMemoryEntityDataModel behave differently than
 * {@link VirtualPagingEntityDataModel}, which cannot apply the
 * EntityViewPolicy as that would require retrieving all of the entities.
 * 
 * @author drew
 */
public class InMemoryEntityDataModel<E extends Entity<K>, K extends Serializable> extends InMemoryDataModel<E>
{
  public InMemoryEntityDataModel(EntityDataFetcher<E,?> dataFetcher)
  {
    super(dataFetcher);
  }
  
  @Override
  public void fetch(List<? extends TableColumn<E,?>> columns)
  {
    List<PropertyPath<E>> propertyPaths = FetchPaths.getPropertyPaths(columns);
    ((EntityDataFetcher<E,?>) _dataFetcher).setPropertiesToFetch(propertyPaths);
    super.fetch(columns);
    for (Iterator<E> iter = _unfilteredData.iterator(); iter.hasNext();) {
      E entity = iter.next();
      if (entity.isRestricted()) {
        iter.remove();
      }
    }
  }
}
