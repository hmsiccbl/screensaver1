// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.io.Serializable;

import edu.harvard.med.screensaver.model.Entity;

public class NoSuchEntityException extends RuntimeException
{
  private static final long serialVersionUID = 1L;
  
  private Class<? extends Entity> _entityClass;
  private Serializable _entityId;

  public NoSuchEntityException(Class<? extends Entity> entityClass, Serializable entityId)
  {
    this("no such entity", entityClass, entityId);
  }
  
  public NoSuchEntityException(String message, Class<? extends Entity> entityClass, Serializable entityId)
  {
    super(message + "( " + entityClass.getName() + ", id=" + entityId + ")");
    _entityClass = entityClass;
    _entityId = entityId;
  }

  public Class<? extends Entity> getEntityClass()
  {
    return _entityClass;
  }
}
