// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;


public class DuplicateEntityException extends DataModelViolationException
{
  private static final long serialVersionUID = 4834277126994099526L;

  public DuplicateEntityException(AbstractEntity entity)
  {
    super(entity + " already exists");
  }
  
  public DuplicateEntityException(AbstractEntity entity, String propertyName, Object propertyValue )
  {
    super(entity + " with the property: " + propertyName + "=\"" + propertyValue + "\" already exists");
  }

  public DuplicateEntityException(AbstractEntity parentEntity,
                                  AbstractEntity duplicatedChildEntity)
  {
    super(parentEntity + " already contains " + duplicatedChildEntity);
  }
}

