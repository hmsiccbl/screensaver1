// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;


public class RequiredPropertyException extends DataModelViolationException
{
  private static final long serialVersionUID = -1;

  public RequiredPropertyException(Class<? extends AbstractEntity> entityClass, Class<? extends AbstractEntity> relatedEntityClass)
  {
    super("value required for " + entityClass.getSimpleName() + " " + relatedEntityClass.getSimpleName());
  }

  public RequiredPropertyException(Class<? extends AbstractEntity> entityClass, String propertyName)
  {
    super("value required for " + entityClass.getSimpleName() + " " + propertyName);
  }

  public RequiredPropertyException(AbstractEntity entity, Class<? extends AbstractEntity> relatedEntityClass)
  {
    this(entity.getEntityClass(), relatedEntityClass);
  }

  public RequiredPropertyException(AbstractEntity entity, String propertyName)
  {
    this(entity.getEntityClass(), propertyName);
  }
}

