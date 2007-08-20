// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import edu.harvard.med.screensaver.ui.AbstractBackingBean;

import org.apache.log4j.Logger;

public class SearchResultsViewer<E> extends AbstractBackingBean
{
  // static members

  private static Logger log = Logger.getLogger(SearchResultsViewer.class);


  // private instance fields

  private SearchResults<E> _searchResults;
  
  
  // public instance methods

  public SearchResults<E> getSearchResults()
  {
    return _searchResults;
  }

  public void setSearchResults(SearchResults<E> searchResults)
  {
    _searchResults = searchResults;
  }

  // private methods

}

