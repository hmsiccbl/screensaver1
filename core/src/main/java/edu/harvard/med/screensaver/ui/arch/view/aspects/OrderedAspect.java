// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.view.aspects;

import org.springframework.core.Ordered;

public abstract class OrderedAspect implements Ordered
{
  // instance data members

  private int _order = 1;


  // public constructors and methods

  public int getOrder()
  {
    return _order;
  }

  public void setOrder(int order)
  {
    _order = order;
  }

  // private methods

}

