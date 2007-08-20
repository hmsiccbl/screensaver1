// $HeadURL: $
// $Id: $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.aspects;

public abstract class OrderedAspect
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
