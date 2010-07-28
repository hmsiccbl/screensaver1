// $HeadURL$
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.io.Serializable;
import java.util.List;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.datafetcher.Tuple;
import edu.harvard.med.screensaver.io.DataExporter;
import edu.harvard.med.screensaver.model.Entity;
import edu.harvard.med.screensaver.ui.EntityViewer;

public abstract class TupleBasedEntitySearchResults<E extends Entity<K>,K extends Serializable> extends EntitySearchResults<E,Tuple<K>,K>
{
  private GenericEntityDAO _dao;
  private Class<E> _rootEntityClass;

  /**
   * @motivation for CGLIB2
   */
  protected TupleBasedEntitySearchResults()
  {}

  public TupleBasedEntitySearchResults(Class<E> rootEntityClass,
                                       GenericEntityDAO dao,
                                       List<DataExporter<Tuple<K>>> dataExporters,
                                       EntityViewer<E> wellViewer)
  {
    super(dataExporters, wellViewer);
    _dao = dao;
    _rootEntityClass = rootEntityClass;
  }

  @Override
  protected E rowToEntity(Tuple<K> tuple)
  {
    return _dao.findEntityById(_rootEntityClass, tuple.getKey());
  }
}
