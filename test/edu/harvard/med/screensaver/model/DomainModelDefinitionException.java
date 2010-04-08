// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

public class DomainModelDefinitionException extends RuntimeException
{
  private static final long serialVersionUID = 1L;

  public DomainModelDefinitionException()
  {
    super();
  }

  public DomainModelDefinitionException(String message, Throwable cause)
  {
    super(message, cause);
  }

  public DomainModelDefinitionException(String message)
  {
    super(message);
  }

  public DomainModelDefinitionException(Throwable cause)
  {
    super(cause);
  }
}
