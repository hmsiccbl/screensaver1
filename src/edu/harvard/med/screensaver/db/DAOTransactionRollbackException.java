// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import org.springframework.core.NestedRuntimeException;

/**
 * Used within {@link DAOTransaction#runTransaction()} method to convert checked
 * exceptions into runtime exceptions, allowing these runtime exceptions to
 * propagate out of the method call, forcing a transaction rollback. Any runtime
 * exception could be used, since any runtime exception will trigger a rollback,
 * but explicitly using DAOTransactionRollbackException explicitly indicates
 * that a rollback is desired.
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

