// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.domainlogic;

import edu.harvard.med.screensaver.model.Entity;

/**
 * Domain logic that cannot be implemented directly within Entity classes can
 * instead be implemented in classes that implement this interface (see
 * {@link edu.harvard.med.screensaver.domainlogic}). All EntityUpdater classes
 * must be registered in spring-context-persistence.xml by adding to them to the
 * <code>entityUpdatersList</code> Spring bean. For {@link #apply(Entity)}
 * methods that issue database queries, it may be necessary to call
 * {@link edu.harvard.med.screensaver.db.GenericEntityDAO#flush} first, to
 * ensure that the database matches the state of newly set object model
 * properties that may influence the query result. It is allowable for classes
 * implementing this interface to depend upon the
 * edu.havard.med.screensaver.db.* classes, such as the DAO classes (contrast
 * this with the edu.harvard.med.screensaver.model POJO entity classes, which
 * should never have such DAO or persistence-layer dependencies).
 * 
 * @author atolopko
 */
public interface EntityUpdater
{
  Class<? extends Entity> getEntityClass();
  void apply(Entity entity);
}
