// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.model.AbstractEntity;

import org.apache.log4j.Logger;

public class UISelectManyEntityBean<E extends AbstractEntity> extends UISelectManyBean<E>
{

  // static members

  private static Logger log = Logger.getLogger(UISelectManyEntityBean.class);


  // instance data members

  private DAO _dao;

  
  // public constructors and methods

  public UISelectManyEntityBean(Collection<E> entities, DAO dao)
  {
    this(entities, null, dao);
  }
  
  public UISelectManyEntityBean(Collection<E> entities, Collection<E> defaultSelections, DAO dao)
  {
    super(entities, defaultSelections);
    _dao = dao;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public List<E> getSelections()
  {
    List<E> entities = super.getSelections();
    List<E> attachedEntities = new ArrayList<E>(entities.size());
    for (E entity : entities) {
      E attachedEntity = (E)
      // can't do this, cuz entity may have already been loaded into the current session, causing NonUniqueObjectException
      //_dao.persistEntity(entity); 
      _dao.findEntityById(entity.getClass(), entity.getEntityId());
      attachedEntities.add(attachedEntity);
    }
    return attachedEntities;
  }

}

