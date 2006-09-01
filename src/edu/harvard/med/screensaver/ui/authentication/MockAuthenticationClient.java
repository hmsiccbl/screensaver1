// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.authentication;

import edu.harvard.med.authentication.AuthenticationClient;
import edu.harvard.med.authentication.AuthenticationRequestException;
import edu.harvard.med.authentication.AuthenticationResponseException;
import edu.harvard.med.authentication.AuthenticationResult;
import edu.harvard.med.authentication.Credentials;

public class MockAuthenticationClient implements AuthenticationClient
{

  public AuthenticationResult authenticate(Credentials credentials)
    throws AuthenticationRequestException,
    AuthenticationResponseException
  {
    return new SimpleAuthenticationResult(credentials,
                                          true,
                                          1,
                                          "success",
                                          "user authorized");
  }

}
