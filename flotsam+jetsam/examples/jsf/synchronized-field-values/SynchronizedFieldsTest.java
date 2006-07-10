// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import javax.faces.component.UIInput;
import javax.faces.event.ValueChangeEvent;

import org.apache.log4j.Logger;

public class SynchronizedFieldsTest
{
  static Logger log = Logger.getLogger(SynchronizedFieldsTest.class);
  
  UIInput firstInput;
  UIInput secondInput;
  String firstValue;
  String secondValue;

  public UIInput getFirstInput()
  {
    return firstInput;
  }

  public void setFirstInput(UIInput firstInput)
  {
    log.info("setFirstInput=" + firstInput.getId());
    this.firstInput = firstInput;
  }

  public String getFirstValue()
  {
    return firstValue;
  }

  public void setFirstValue(String firstValue)
  {
    log.info("setFirstValue=" + firstValue);
    this.firstValue = firstValue;
  }

  public UIInput getSecondInput()
  {
    return secondInput;
  }

  public void setSecondInput(UIInput secondInput)
  {
    log.info("setSecondInput=" + secondInput.getId());
    this.secondInput = secondInput;
  }

  public String getSecondValue()
  {
    return secondValue;
  }

  public void setSecondValue(String secondValue)
  {
    log.info("setSecondValue=" + secondValue);
    this.secondValue = secondValue;
  }
  
  public void secondListener(ValueChangeEvent event)
  {
    log.info("secondListener new_value=" + event.getNewValue());
//  firstValue = event.getNewValue().toString();  // gets overwritten by JSF during UpdateModelValues lifecycle phase
    firstInput.setValue(event.getNewValue());
    
  }

  public void firstListener(ValueChangeEvent event)
  {
    log.info("firstListener new_value=" + event.getNewValue());
  }
  
  public String update()
  {
    log.info("update()");
    return null;
  }
}
