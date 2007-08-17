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

/**
 * Runs a transactional section of code through the Data Access Object.
 * <p>
 * <i>It is now preferred that any code that needs to be executed within a
 * transaction is instead contained within a method of a Spring-managed bean
 * class that has a {@link Transactional} annotation.</i> 
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public interface DAOTransaction
{

  /**
   * Run a transactional section of code. Intended to be called by
   * {@link AbstractDAO#doInTransaction}. Method should throw a
   * {@link DAOTransactionRollbackException} if a rollback is required (any
   * runtime exception will do, but this exception is explicit in its intent).
   */
  public void runTransaction();
}
