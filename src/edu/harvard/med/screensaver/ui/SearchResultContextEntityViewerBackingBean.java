// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.ui.searchresults.EntitySearchResults;

import org.springframework.transaction.annotation.Transactional;


public abstract class SearchResultContextEntityViewerBackingBean<E extends AbstractEntity> extends EntityViewerBackingBean<E> implements SearchResultContextEntityViewer<E>
{
  private EntitySearchResults<E,?> _entitySearchResults;
  private String _browserActionResult;

  public SearchResultContextEntityViewerBackingBean() {}

  public SearchResultContextEntityViewerBackingBean(SearchResultContextEntityViewerBackingBean<E> thisProxy,
                                                    Class<E> entityClass,
                                                    String browserActionResult,
                                                    String viewerActionResult,
                                                    GenericEntityDAO dao,
                                                    EntitySearchResults<E,?> entitySearchResults)
  {
    super(thisProxy, entityClass, viewerActionResult, dao);
    _browserActionResult = browserActionResult;
    _entitySearchResults = entitySearchResults;
  }

  public EntitySearchResults<E,?> getContextualSearchResults()
  {
    return _entitySearchResults;
  }

  @Transactional
  @UICommand
  @Override
  public String viewEntity(E entity)
  {
    if (_entitySearchResults.findEntity(entity)) {
      return _browserActionResult;
    }
    log.debug("entity " + entity + " is not a member of the current search results; entity will be viewed independently");
    return super.viewEntity(entity);
  }
}
