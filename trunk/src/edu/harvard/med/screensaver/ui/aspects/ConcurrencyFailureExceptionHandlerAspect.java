// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.aspects;

import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.util.Messages;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.dao.ConcurrencyFailureException;

public class ConcurrencyFailureExceptionHandlerAspect extends OrderedAspect
{
  // static members

  private static Logger log = Logger.getLogger(ConcurrencyFailureExceptionHandlerAspect.class);


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
      AbstractBackingBean backingBean = (AbstractBackingBean) joinPoint.getTarget();
      log.debug("backing bean " + backingBean.getClass().getSimpleName() +
               " method " + joinPoint.getSignature().getName() +
               " threw " + e.getClass().getSimpleName());
      _messages.setFacesMessageForComponent("concurrentModificationConflict", null);
      // TODO: backingBean.reload();
      return AbstractBackingBean.REDISPLAY_PAGE_ACTION_RESULT;
    }
  }

  // private methods

}

