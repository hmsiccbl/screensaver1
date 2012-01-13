// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service;

import edu.harvard.med.screensaver.model.AbstractEntity;

// TODO: redundant with NoSuchEntityException
public class EntityNotFoundException extends Exception
{
  private static final long serialVersionUID = 1L;

  private Class<? extends AbstractEntity> entityClass;
  
  public EntityNotFoundException(Class<? extends AbstractEntity> entityClass, Object key)
  {
    super("Entity: " + entityClass.getName() + ", key: " + key);
    this.entityClass = entityClass;
  }

  public EntityNotFoundException(Class<? extends AbstractEntity> entityClass, Object key, Throwable cause)
  {
    super("Entity: " + entityClass.getName() + ", key: " + key, cause);
    this.entityClass = entityClass;
  }

}
