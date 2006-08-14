// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import java.util.HashMap;
import java.util.Map;

import edu.harvard.med.screensaver.model.AbstractEntity;

/**
 * Maintains a registry of the latest {@link SearchResults} for every type of {@link
 * AbstractEntity}. Also maintains the <i>current</i> <code>SearchResults</code>, which is
 * used by searchResults.jsp to do search results display.
 * 
 * <p>
 * 
 * The registrant
 * of the <code>SearchResults</code> is also tracked, in order to allow lookup on the
 * registrant. This is useful, for example, to see if the current search results were
 * registered by the calling code.
 * 
 * <p>
 * 
 * This probably looks like a bit of overengineering - why maintain more than one set of
 * search results? The reason why I did it this way goes something like this: Suppose I am
 * searching through {@link Compound Compounds}, and I have a particular search result and
 * search order and position in the search. Then as a side-step, I go check out something
 * else; maybe I go to "Browse Libraries". Then I go back to my compounds search results,
 * using, say, the back button on my browser. It's cool to have the same search results in
 * place, even though I have done a "search" on another type!
 * 
 * <p>
 * 
 * This also might have usefulness for related searches. For instance, suppose I do a search
 * that returns a list of {@link Well Wells}. From there, I go to the compound viewer page
 * for one of the {@link Compound Compounds} in the <code>Well</code>. It would be cool if
 * the compound viewer page had First, Prev, Next and Last buttons that matched with my
 * <code>Well</code> search results!
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class SearchResultsRegistryController extends AbstractController
{

  // private instance data
  
  private Map<Class<? extends AbstractEntity>,SearchResults<? extends AbstractEntity>> _searchResultsCache =
    new HashMap<Class<? extends AbstractEntity>,SearchResults<? extends AbstractEntity>>();
  
  private Map<Class<? extends AbstractEntity>,AbstractController> _searchResultsRegistrants =
    new HashMap<Class<? extends AbstractEntity>,AbstractController>();
  
  private Class<? extends AbstractEntity> _currentSearchType;
  
  
  // public instance methods
  
  /**
   * Register a {@link SearchResults} for the specified search type. The previous
   * <code>SearchResults</code> for the specified search type is released.
   * 
   * @param <E> the search type
   * @param searchType the search type
   * @param registrant the registrant of the search results
   * @param searchResults the search results
   */
  public <E extends AbstractEntity> void registerSearchResults(
    Class<E> searchType,
    AbstractController registrant,
    SearchResults<E> searchResults)
  {
    _searchResultsCache.put(searchType, searchResults);
    _searchResultsRegistrants.put(searchType, registrant);
  }
  
  /**
   * Get the search results for the specified search type.
   * 
   * @param <E> the search type
   * @param searchType the search type
   * @return the search results for the specified search type
   */
  @SuppressWarnings("unchecked")
  public <E extends AbstractEntity> SearchResults<E> getSearchResults(Class<E> searchType)
  {
    return (SearchResults<E>) _searchResultsCache.get(searchType);
  }
  
  /**
   * Get the registrant of the search results for the specified search type.
   * 
   * @param <E> the search type
   * @param searchType the search type
   * @return the registrant of the search results for the specified search type.
   */
  public <E extends AbstractEntity> AbstractController getSearchResultsRegistrant(Class<E> searchType)
  {
    return _searchResultsRegistrants.get(searchType);
  }
  
  /**
   * Get the current search type.
   * @return the current search type
   */
  public Class<? extends AbstractEntity> getCurrentSearchType()
  {
    return _currentSearchType;
  }

  /**
   * Set the current search type.
   * @param searchType the current search type
   */
  public void setCurrentSearchType(Class<? extends AbstractEntity> searchType)
  {
    _currentSearchType = searchType;
  }
  
  /**
   * Get the current search results: the search results for the current search type.
   * @return the search results for the current search type
   */
  public SearchResults<? extends AbstractEntity> getSearchResults()
  {
    return getSearchResults(getCurrentSearchType());
  }
}
