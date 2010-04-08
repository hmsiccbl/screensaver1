// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import org.apache.log4j.Logger;

public abstract class AggregateSearchResults<R,K> extends SearchResults<R,K,Object>
{
  // static members

  private static Logger log = Logger.getLogger(AggregateSearchResults.class);

  
  // instance data members

  // public constructors and methods
  
  public AggregateSearchResults()
  {
  }
  
  public AggregateSearchResults(String[] capabilities)
  {
    super(capabilities);
  }

  // private methods

}
