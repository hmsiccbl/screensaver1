// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/model/DuplicateEntityException.java $
// $Id: DuplicateEntityException.java 1985 2007-10-19 18:27:49Z ant4 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;


public class RequiredPropertyException extends DataModelViolationException
{
  private static final long serialVersionUID = -1;

  public RequiredPropertyException(AbstractEntity entity, Class<? extends AbstractEntity> relatedEntityClass)
  {
    super("value required for " + entity.getEntityClass().getSimpleName() + " " + relatedEntityClass.getSimpleName());
  }

  public RequiredPropertyException(AbstractEntity entity, String propertyName)
  {
    super("value required for " + entity.getEntityClass().getSimpleName() + " " + propertyName);
  }
}

