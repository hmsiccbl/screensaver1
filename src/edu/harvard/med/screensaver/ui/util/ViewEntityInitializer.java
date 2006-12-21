// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.util;

import java.util.Collection;

import javax.faces.context.FacesContext;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.model.AbstractEntity;

import org.hibernate.Hibernate;
import org.hibernate.collection.PersistentCollection;

public abstract class ViewEntityInitializer
{
  public static final String LAST_VIEW_ENTITY_INITIALIZER = "lastViewEntityInitializer";

  private DAO _dao;
  
  final public void initializeView() 
  {
    revivifyAndPrimeEntities();
    setEntities();
  }
  
  protected abstract void revivifyAndPrimeEntities();

  protected abstract void setEntities();
  
  
  @SuppressWarnings("unchecked")
  protected ViewEntityInitializer(DAO dao)
  {
    _dao = dao;
    FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(LAST_VIEW_ENTITY_INITIALIZER, this);
  }

  protected DAO getDAO() 
  { 
    return _dao; 
  }
  
  protected void reattach(AbstractEntity entity)
  {
    _dao.persistEntity(entity);
  }
  
  protected AbstractEntity reload(AbstractEntity entity)
  {
    return _dao.findEntityById(entity.getClass(), entity.getEntityId());
  }
  
  protected void need(AbstractEntity entity)
  {
    Hibernate.initialize(entity);
  }
  
  protected void need(PersistentCollection persistentCollection)
  {
    Hibernate.initialize(persistentCollection);
  }

  protected void need(Collection collection)
  {
    collection.iterator();
  }
  
}

