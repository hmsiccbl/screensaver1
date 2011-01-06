// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.auth;

import edu.harvard.med.authentication.AuthenticationResponseException;
import edu.harvard.med.authentication.AuthenticationResult;
import edu.harvard.med.authentication.Credentials;

public class SimpleAuthenticationResult implements AuthenticationResult
{
  private Credentials _credentials;
  private boolean _isAuthenticated;
  private int _statusCode;
  private String _statusCategory;
  private String _statusMessage;
  
  public SimpleAuthenticationResult(String userName,
                                    String password,
                                    boolean isAuthenticated,
                                    int statusCode,
                                    String statusCategory,
                                    String statusMessage)
  {
    this(new Credentials(userName, password),
         isAuthenticated,
         statusCode,
         statusCategory,
         statusMessage);
  }

  public SimpleAuthenticationResult(Credentials credentials,
                                    boolean isAuthenticated,
                                    int statusCode,
                                    String statusCategory,
                                    String statusMessage)
  {
    _credentials = credentials;
    _isAuthenticated = isAuthenticated;
    _statusCode = statusCode;
    _statusCategory = statusCategory;
    _statusMessage = statusMessage;
  }

  public Credentials getCredentials()
  {
    return _credentials;
  }

  public boolean isAuthenticated() throws AuthenticationResponseException
  {
    return _isAuthenticated;
  }

  public int getStatusCode() throws AuthenticationResponseException
  {
    return _statusCode;
  }

  public String getStatusCodeCategory() throws AuthenticationResponseException
  {
    return _statusCategory;
  }

  public String getStatusMessage() throws AuthenticationResponseException
  {
    return _statusMessage;
  }

}
