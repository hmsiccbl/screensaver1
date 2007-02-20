// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import edu.harvard.med.screensaver.model.AbstractEntity;

import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

/**
 * WARNING: Any classes that contain methods matching Pointcuts found here must
 * extend a Java interface IF they are also being Spring-injected into another
 * bean. If the bean being injected specifies a concrete type (rather than an
 * interface), the AOP proxy created for the injected bean will not be
 * considered of the appropriate type, and Spring will fail during
 * initializaiton. The original case of this is the LibrariesController being
 * injected into the ScreensController.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Aspect
public class UserActivityLoggerAspect
{
  private static Logger log = Logger.getLogger(UserActivityLoggerAspect.class);
  

  /* Pointcuts (define sets of methods that will have advice applied to them) */

  /**
   * Selects all methods annotated with the AbstractUIController annotation and
   * whose first arg is an AbstractEntity (additional args are allowed, but
   * ignored)
   */
//@Pointcut("@annotation(edu.harvard.med.screensaver.ui.control.UIControllerMethod) && args(entity,..)")
//public void anyControllerMethod(AbstractEntity entity) {}
  @Pointcut("@annotation(edu.harvard.med.screensaver.ui.control.UIControllerMethod)")
  public void anyControllerMethod() {}
  
  /* Advice (the behavior to apply to particular pointcuts) */
  
//@Before("anyControllerMethod(entity)")
//public void beforeAnyControllerMethod(AbstractEntity entity)

  @Before("anyControllerMethod()")
  public void beforeAnyControllerMethod(JoinPoint joinPoint)
  {
    AbstractEntity entity = null;
    if (joinPoint.getArgs().length > 0 &&
      joinPoint.getArgs()[0] instanceof AbstractEntity) {
      entity = (AbstractEntity) joinPoint.getArgs()[0];
    }
    log.info(joinPoint.getSignature().getName() + (entity == null ? "" : ": " + entity));
  }
    
}

