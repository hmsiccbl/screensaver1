// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
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

