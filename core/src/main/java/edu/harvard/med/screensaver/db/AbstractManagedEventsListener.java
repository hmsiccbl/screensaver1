// $HeadURL$
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.Map;


import org.hibernate.HibernateException;
import org.hibernate.event.MergeEvent;
import org.hibernate.event.PersistEvent;
import org.hibernate.event.PostLoadEvent;
import org.hibernate.event.SaveOrUpdateEvent;

public abstract class AbstractManagedEventsListener implements ManagedEventsListener
{
  abstract protected void apply(Object entity);

  @Override
  public void onPostLoad(PostLoadEvent event)
  {
    apply(event.getEntity());
  }

  @Override
  public void onSaveOrUpdate(SaveOrUpdateEvent event) throws HibernateException
  {
    apply(event.getEntity());
  }

  @Override
  public void onMerge(MergeEvent event) throws HibernateException
  {
    apply(event.getEntity());
  }

  @Override
  public void onMerge(MergeEvent event, Map copiedAlready) throws HibernateException
  {
    apply(event.getEntity());
  }

  @Override
  public void onPersist(PersistEvent event) throws HibernateException
  {
    apply(event.getObject());
  }

  @Override
  public void onPersist(PersistEvent event, Map createdAlready) throws HibernateException
  {
    apply(event.getObject());
  }
}
