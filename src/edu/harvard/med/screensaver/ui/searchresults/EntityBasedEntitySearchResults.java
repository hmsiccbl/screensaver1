// $HeadURL$
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.ui.EntityViewer;

public abstract class EntityBasedEntitySearchResults<E extends AbstractEntity,K> extends EntitySearchResults<E,E,K>
{
  /**
   * @motivation for CGLIB2
   */
  protected EntityBasedEntitySearchResults()
  {}

  protected EntityBasedEntitySearchResults(EntityViewer<E> entityViewer)
  {
    super(entityViewer);
  }

  @Override
  protected E rowToEntity(E row)
  {
    return row;
  }
}
