// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.AbstractEntity;

import org.apache.log4j.Logger;

public class UISelectManyEntityBean<E extends AbstractEntity> extends UISelectManyBean<E>
{

  // static members

  private static Logger log = Logger.getLogger(UISelectManyEntityBean.class);


  // instance data members

  private GenericEntityDAO _dao;

  
  // public constructors and methods

  public UISelectManyEntityBean(Collection<E> entities, GenericEntityDAO dao)
  {
    this(entities, Collections.<E>emptySet(), dao);
  }
  
  public UISelectManyEntityBean(Collection<E> entities, Collection<E> defaultSelections, GenericEntityDAO dao)
  {
    super(entities, defaultSelections);
    _dao = dao;
  }
  
  @Override
  public List<E> getSelections()
  {
    List<E> entities = super.getSelections();
    List<E> attachedEntities = new ArrayList<E>(entities.size());
    for (E entity : entities) {
      attachedEntities.add(_dao.reloadEntity(entity));
    }
    return attachedEntities;
  }

}

