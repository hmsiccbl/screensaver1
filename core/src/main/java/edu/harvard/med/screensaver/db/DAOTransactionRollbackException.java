// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import org.springframework.core.NestedRuntimeException;
import org.springframework.transaction.annotation.Transactional;

/**
 * Can be used within an {@link Transactional}-annotated method or
 * {@link DAOTransaction#runTransaction()} method to convert checked exceptions
 * into runtime exceptions, allowing these runtime exceptions to propagate out
 * of the method call and force a transaction rollback. Note that any runtime
 * exception could be used instead, since any runtime exception will trigger a
 * rollback, but explicitly using DAOTransactionRollbackException indicates that
 * a rollback is desired. Also note that {@link Transactional}-annotated
 * methods will <i>not</i> rollback on checked exceptions, unless the
 * annotation specifies which checked exceptions should effect a rollback.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class DAOTransactionRollbackException extends NestedRuntimeException
{
  private static final long serialVersionUID = 1L;

  public DAOTransactionRollbackException(String msg)
  {
    super(msg, null);
  }

  public DAOTransactionRollbackException(Throwable ex)
  {
    super(null, ex);
  }

  public DAOTransactionRollbackException(String msg, Throwable ex)
  {
    super(msg, ex);
  }
}

