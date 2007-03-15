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
 * Indicates that an attempt has been made to alter data in a way that violates
 * data requirements and/or real-world constraints. In general, the classes
 * implementing the data model are capable of preventing any <i>static</i>
 * state of the data from being invalid, but cannot capture whether transitions
 * between any two states is valid. When a state transition is invalid, the
 * system should throw a BusinessRuleViolationException.
 * <p>
 * BusinessRuleViolationExceptions should not be thrown while interacting with
 * the data model via the Screensaver web user interface. If this occurs, logic
 * in the user interface is flawed and should be fixed.  
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class BusinessRuleViolationException extends RuntimeException
{
  // static members

  private static final long serialVersionUID = 1L;
  private static Logger log = Logger.getLogger(BusinessRuleViolationException.class);
  

  // public constructors and methods

  public BusinessRuleViolationException(String message)
  {
    super(message);
  }

}

