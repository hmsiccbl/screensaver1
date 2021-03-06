// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.view.aspects;

import javax.persistence.OptimisticLockException;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.core.Ordered;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataAccessException;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.DataModelViolationException;
import edu.harvard.med.screensaver.service.OperationRestrictedException;
import edu.harvard.med.screensaver.ui.arch.util.Messages;
import edu.harvard.med.screensaver.ui.arch.view.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.arch.view.EditableEntityViewer;

/**
 * Handles exceptions thrown by methods that are annotated
 * with {@link UICommand} by displaying an appropriate user error
 * message and then reloads the view with fresh data.
 */
public class UICommandExceptionHandlerAspect extends OrderedAspect implements Ordered
{
  private static Logger log = Logger.getLogger(UICommandExceptionHandlerAspect.class);

  private Messages _messages;

  public void setMessages(Messages messages)
  {
    _messages = messages;
  }

  public Object invoke(ProceedingJoinPoint joinPoint) throws Throwable
  {
    try {
      return joinPoint.proceed();
    }
    catch (OperationRestrictedException e) {
      return handleException(joinPoint, e, "restrictedOperation", e.getMessage());
    }
    // catch the JPA exception for concurrency-related failures, since this is (apparently) what's being thrown, rather than the expected Spring-translated ConcurrencyFailureException
    // TODO: figure out why Spring-translated exceptions are not always/ever being thrown  
    catch (OptimisticLockException e) {
      return handleException(joinPoint, e, "concurrentModificationConflict", null);
    }
    // catch the Spring-translated exception for concurrency-related failures
    catch (ConcurrencyFailureException e) {
      return handleException(joinPoint, e, "concurrentModificationConflict", null);
    }
    // catch the top-most (i.e., most general) Spring-translated exception for persistence-related failures
    catch (DataAccessException e) {
      return handleException(joinPoint, e, "databaseOperationFailed", e.getMessage());
    }
    catch (BusinessRuleViolationException e) {
      return handleException(joinPoint, e, "businessError", e.getMessage());
    }
    catch (DataModelViolationException e) {
      return handleException(joinPoint, e, "businessError", e.getMessage());
    }
    catch (Throwable e) {
      log.error("",e);
      return handleException(joinPoint, e, "systemError", e.getMessage());
    }
  }

  private Object handleException(ProceedingJoinPoint joinPoint,
                                 Throwable t,
                                 String errorMessageId,
                                 String errorMessageArg)
  {
    AbstractBackingBean backingBean = (AbstractBackingBean) joinPoint.getTarget();
    log.error("backing bean " + backingBean.getClass().getSimpleName() +
             " method " + joinPoint.getSignature().getName() +
             " threw " + t.getClass().getSimpleName() + 
             ": " + t.getMessage(), t);
    _messages.setFacesMessageForComponent(errorMessageId, null, errorMessageArg);
    if (backingBean instanceof EditableEntityViewer) {
      // TODO not working...
      // ((EntityViewer) backingBean).reload();
      // this is the best we can do with the current design
      EditableEntityViewer editableViewer = (EditableEntityViewer) backingBean;
      return editableViewer.cancel();
    }
    return ScreensaverConstants.REDISPLAY_PAGE_ACTION_RESULT;
  }
}

