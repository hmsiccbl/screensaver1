// $HeadURL: $
// $Id: $
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import org.hibernate.event.MergeEventListener;
import org.hibernate.event.PersistEventListener;
import org.hibernate.event.PostLoadEventListener;
import org.hibernate.event.SaveOrUpdateEventListener;

/**
 * Interface that groups together all Hibernate events whose occurrence causes an entity object to be "managed" by a
 * Hibernate session.
 * 
 * @author atolopko
 */
public interface ManagedEventsListener extends PostLoadEventListener, MergeEventListener, PersistEventListener, SaveOrUpdateEventListener
{
}
