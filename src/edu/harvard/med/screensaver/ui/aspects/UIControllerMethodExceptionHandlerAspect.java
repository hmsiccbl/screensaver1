// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.aspects;

import java.util.ConcurrentModificationException;

import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.ui.util.Messages;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataAccessException;

/**
 * Handles {@link ConcurrentModificationException} and
 * {@link DataAccessException} exceptions thrown by methods that are annotated
 * with {@link UIControllerMethod} by displaying an appropriate user error
 * message and then requesting the backing bean to reload its data.
 */
public class UIControllerMethodExceptionHandlerAspect extends OrderedAspect
{
  // static members

  private static Logger log = Logger.getLogger(UIControllerMethodExceptionHandlerAspect.class);


  // instance data members

  private Messages _messages;

  // public constructors and methods

  public void setMessages(Messages messages)
  {
    _messages = messages;
  }

  public Object invoke(ProceedingJoinPoint joinPoint) throws Throwable
  {
    try {
      return joinPoint.proceed();
    }
    catch (ConcurrencyFailureException e) {
      return handleException(joinPoint, e, "concurrentModificationConflict", null);
    }
    catch (DataAccessException e) {
      return handleException(joinPoint, e, "databaseOperationFailed", e.getMessage());
    }
  }

  private Object handleException(ProceedingJoinPoint joinPoint,
                                 Exception e,
                                 String errorMessageId,
                                 String errorMessageArg)
  {
    AbstractBackingBean backingBean = (AbstractBackingBean) joinPoint.getTarget();
    log.debug("backing bean " + backingBean.getClass().getSimpleName() +
             " method " + joinPoint.getSignature().getName() +
             " threw " + e.getClass().getSimpleName());
    _messages.setFacesMessageForComponent(errorMessageId, errorMessageArg);
    return backingBean.reload();
  }

  // private methods

}

