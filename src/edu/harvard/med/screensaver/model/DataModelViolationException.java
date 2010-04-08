// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import org.apache.log4j.Logger;

/**
 * Indicates that an attempt has been made to alter the data model in a way that
 * violates data requirements. In general, only the domain model entity classes
 * will throw this exception. However, it is also valid for methods of service classes to
 * throw this exception if they are performing "sanity checks" on the state of
 * the data model during their execution.
 *
 * @see BusinessRuleViolationException
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
// TODO: we should probably create a similar exception class that indicates that
// the data model *has* been violated (i.e. data in the database does not
// conform to the data model constraints), and use this exception to indicate
// that the data model *would* be violated, if the requested change were
// persisted to the database.
public class DataModelViolationException extends RuntimeException
{
  // static members

  private static final long serialVersionUID = 1L;
  private static Logger log = Logger.getLogger(DataModelViolationException.class);


  // public constructors and methods

  public DataModelViolationException(String message)
  {
    super(message);
  }

}

