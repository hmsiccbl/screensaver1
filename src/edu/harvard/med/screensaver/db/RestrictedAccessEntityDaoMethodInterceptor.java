// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import edu.harvard.med.screensaver.model.AbstractEntity;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;

public class RestrictedAccessEntityDaoMethodInterceptor extends RestrictedAccessDaoMethodInterceptor
{
  // static members

  private static Logger log = Logger.getLogger(RestrictedAccessEntityDaoMethodInterceptor.class);
  

  // instance data members
  
  
  // public methods

  public Object invoke(MethodInvocation methodInvocation) throws Throwable
  {
    if (log.isDebugEnabled()) {
      log.debug("handling DAO method invocation: " + methodInvocation.getMethod().getName());
    }

    Object baseReturnValue = methodInvocation.proceed();
    
    AbstractEntity entity = (AbstractEntity) baseReturnValue;
    if (!checkAccess(entity)) {
      return null;
    }
    return entity;
  }
}

