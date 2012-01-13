// $HeadURL$
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

import edu.harvard.med.screensaver.model.Entity;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;

public class EntityInflator<E extends Entity>
{
  private GenericEntityDAO _dao;
  private E _entity;
  private boolean _readOnly;
  private List<RelationshipPath<? super E>> paths = Lists.newArrayList();

  public EntityInflator(GenericEntityDAO dao, E entity, boolean readOnly)
  {
    _dao = dao;
    _entity = entity;
    _readOnly = readOnly;
  }

  public EntityInflator<E> need(RelationshipPath<? super E> path)
  {
    paths.add(path);
    return this;
  }

  public E inflate()
  {
   _dao.doInTransaction(new DAOTransaction() {
    @Override
    public void runTransaction()
    {
      Iterator<RelationshipPath<? super E>> iter = paths.iterator();
      if (iter.hasNext()) {
        _entity = (E) _dao.reloadEntity(_entity, _readOnly, iter.next());
        assert _entity != null;
      }
      while (iter.hasNext()) {
        if (_readOnly) {
          _dao.needReadOnly(_entity, iter.next());
        }
        else {
          _dao.need(_entity, iter.next());
        }
      }
    }
    });
    return _entity;
  }
}
