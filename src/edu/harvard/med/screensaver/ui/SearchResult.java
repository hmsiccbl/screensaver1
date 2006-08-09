// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import java.util.List;

import edu.harvard.med.screensaver.model.AbstractEntity;

/**
 * A sortable, paging search result of {@link AbstractEntity model entities}.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class SearchResult<E extends AbstractEntity>
{
  
  // public static final data
  
  public static final int DEFAULT_PAGESIZE = 10;
  

  // private instance data
  
  private List<E> _unsortedResults;
  private int _currentIndex = 0;
  private int _pageSize = DEFAULT_PAGESIZE;

  
  // public constructor and instance methods
  
  /**
   * Construct a new <code>SearchResult</code> object.
   * @param unsortedResults the unsorted list of the results, as they are returned from the
   * database
   */
  public SearchResult(List<E> unsortedResults)
  {
    _unsortedResults = unsortedResults;
  }
}
