// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
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
  
  public NoSuchEntityException(Class<? extends Entity> entityClass, Serializable entityId)
  {
    this(entityClass, "id", entityId);
  }
  
  public NoSuchEntityException(Class<? extends Entity> entityClass, String propertyName, Object propertyValue)
  {
    super("no such entity" + " (" + entityClass.getName() + "." + propertyName + "=" + propertyValue + ")");
  }
}
