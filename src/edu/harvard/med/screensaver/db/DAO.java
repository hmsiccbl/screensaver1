// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import org.springframework.transaction.annotation.Transactional;

import edu.harvard.med.screensaver.model.AbstractEntity;

/**
 * A Data Access Object for the beans in the
 * {@link edu.harvard.med.screensaver.model data model}.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Transactional
public interface DAO
{

  /**
   * Create, register, and return a new {@link AbstractEntity Entity} of the
   * specified type with all required fields initialized.
   * 
   * @param <E> The type of entity to create
   * @param entityClass The class of the entity to create
   * @param constructorArguments arguments for the required fields
   * @return The newly-created entity
   * @exception InvalidArgumentException when the supplied constructor arguments
   *              do not match the parameter list of the required-fields
   *              constructor
   */
  public <E extends AbstractEntity> E defineEntity(Class<E> entityClass,
    Object... constructorArguments);
}
