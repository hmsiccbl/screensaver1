// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import edu.harvard.med.screensaver.ui.util.Messages;

/**
 * A base Controller class for JSF backing beans (beans that handle JSF actions
 * and events).
 * 
 * @author ant
 */
public abstract class AbstractController
{

  private Messages _messages;

  protected Messages getMessages() {
    return _messages;
  }

  public void setMessages(Messages messages) {
    _messages = messages;
  }

}
