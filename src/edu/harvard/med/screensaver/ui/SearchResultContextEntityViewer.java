// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.ui.searchresults.EntitySearchResults;

public interface SearchResultContextEntityViewer<E extends AbstractEntity,R>
{
  EntitySearchResults<E,R,?> getContextualSearchResults();
}
