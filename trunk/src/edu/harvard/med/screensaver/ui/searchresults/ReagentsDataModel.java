// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.ReagentsSortQuery;
import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.ui.table.TableColumn;
import edu.harvard.med.screensaver.ui.table.VirtualPagingDataModel;

import org.apache.log4j.Logger;

public class ReagentsDataModel extends VirtualPagingDataModel<Reagent,Reagent>
{
  // static members

  private static Logger log = Logger.getLogger(ReagentsDataModel.class);

  private GenericEntityDAO _dao;
  private Study _study;
  private Set<Reagent> _reagents;

  public ReagentsDataModel(Study study,
                           int rowsToFetch,
                           int totalRowCount,
                           TableColumn<Reagent,?> sortColumn,
                           SortDirection sortDirection,
                           GenericEntityDAO dao)
  {
    super(rowsToFetch,
          totalRowCount,
          sortColumn,
          sortDirection);
    _study = study;
    _dao = dao;
  }

  public ReagentsDataModel(Set<Reagent> reagents,
                           int rowsToFetch,
                           int totalRowCount,
                           TableColumn<Reagent,?> sortColumn,
                           SortDirection sortDirection,
                           GenericEntityDAO dao)
  {
    super(rowsToFetch,
          totalRowCount,
          sortColumn,
          sortDirection);
    _reagents = reagents;
    _dao = dao;
  }

  @Override
  protected List<Reagent> fetchAscendingSortOrder(TableColumn<Reagent,?> sortColumn)
  {
    ReagentsSortQuery query;
    if (_study != null) {
      query = new ReagentsSortQuery(_study);
    }
    else {
      query = new ReagentsSortQuery(_reagents);
    }
    if (sortColumn instanceof ReagentColumn) {
      query.setSortByReagentProperty(((ReagentColumn) sortColumn).getReagentProperty());
    }
    else if (sortColumn instanceof AnnotationTypeColumn) {
      query.setSortByAnnotationType(((AnnotationTypeColumn) sortColumn).getAnnotationType());
    }
    else {
      throw new IllegalArgumentException("invalid TableColumn type: "  + sortColumn.getClass());
    }
    return _dao.<Reagent>runQuery(query);
  }

  @Override
  protected Map<Reagent,Reagent> fetchData(final Set<Reagent> reagents)
  {
    final Map<Reagent,Reagent> result = new HashMap<Reagent,Reagent>(reagents.size());
    _dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction() {
        for (Reagent reagentIn : reagents) {
          // TODO: batch fetch
          Reagent reagent = _dao.reloadEntity(reagentIn,
                                              true,
                                              "annotationValues.annotationType");
          _dao.needReadOnly(reagent, "wells.silencingReagents.gene");
          _dao.needReadOnly(reagent, "wells.compounds");
          if (log.isDebugEnabled()) {
            log.debug("fetched " + reagent);
          }
          result.put(reagent, reagent);
        }
      }
    });
    return result;
  }

}
