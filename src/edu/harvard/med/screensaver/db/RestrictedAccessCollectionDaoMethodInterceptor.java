// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.Collection;
import java.util.Iterator;

import edu.harvard.med.screensaver.model.AbstractEntity;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;

public class RestrictedAccessCollectionDaoMethodInterceptor extends RestrictedAccessDaoMethodInterceptor
{
  // static members

  private static Logger log = Logger.getLogger(RestrictedAccessCollectionDaoMethodInterceptor.class);

  
  // instance data members
  
  @SuppressWarnings("unchecked")
  public Object invoke(MethodInvocation methodInvocation) throws Throwable
  {
    if (log.isDebugEnabled()) {
      log.debug("handling DAO method invocation: " + methodInvocation.getMethod().getName());
    }
    Object baseReturnValue = methodInvocation.proceed();
    
    Collection<AbstractEntity> entities = (Collection<AbstractEntity>) baseReturnValue;
      for (Iterator iter = entities.iterator(); iter.hasNext();) {
      AbstractEntity entity = (AbstractEntity) iter.next();
      if (!checkAccess(entity)) {
        iter.remove();
      }
    }
    return entities;
  }
}

