// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io;


public class ParseError
{
  private String _message;
  private Object _location;
  
  public ParseError(String message, Object location)
  {
    _message = message;
    _location = location;
  }
  
  public ParseError(String message)
  {
    _message = message;
  }
  
  public String toString()
  {
    return "location: " + _location + ": " + _message;
  }
  
  public String getErrorMessage() 
  {
    return _message;
  }
  
  public Object getErrorLocation()
  {
    return _location;
  }
  
  /**
   * @motivation for unit testing
   */
  public boolean equals(Object o)
  {
    if (!(o instanceof ParseError)) {
      return false;
    }
    ParseError that = (ParseError) o;
    return this.toString().equals(that.toString());
  }
  
  public int hashCode() 
  {
    return toString().hashCode();
  }
}