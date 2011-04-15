// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.analysis;

import java.util.Collection;

import org.apache.log4j.Logger;

public class IdentityFunction implements AggregateFunction<Double>
{
  private static Logger log = Logger.getLogger(IdentityFunction.class);

  public Double compute(Double value)
  {
    return value;
  }

  public void initializeAggregates(Collection<Double> valuesToNormalizeOverController)
  {
  }

}

