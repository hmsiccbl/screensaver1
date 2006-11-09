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

import org.aopalliance.intercept.MethodInterceptor;
import org.apache.log4j.Logger;

public abstract class RestrictedAccessDaoMethodInterceptor implements MethodInterceptor
{
  // static members

  private static Logger log = Logger.getLogger(RestrictedAccessDaoMethodInterceptor.class);
  private DataAccessPolicy _policy;
  
  
  // public methods

  public void setDataAccessPolicy(DataAccessPolicy policy)
  {
    _policy = policy;
  }
  
  
  // protected methods
  
  protected boolean checkAccess(AbstractEntity entity)
  {
    if (entity == null) {
      return true;
    }
    Object visitResult = entity.acceptVisitor(_policy);
    if (visitResult != null && ((Boolean) visitResult) == false) {
      if (log.isDebugEnabled()) {
        log.debug("access restricted to " + entity);
      }
      return false;
    }    
    return true;
  }
  
}


