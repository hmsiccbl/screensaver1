// $HeadURL: http://forge.abcd.harvard.edu/svn/screensaver/branches/iccbl/library-mgmt-rework/src/edu/harvard/med/screensaver/model/libraries/SmallMoleculeReagent.java $
// $Id: SmallMoleculeReagent.java 3026 2009-03-23 15:29:52Z atolopko $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import javax.persistence.Entity;

import edu.harvard.med.screensaver.model.AbstractEntityVisitor;
import edu.harvard.med.screensaver.model.annotations.ContainedEntity;

import org.hibernate.annotations.Immutable;


/**
 * Natural Products reagent.
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
@Entity
@Immutable
@ContainedEntity(containingEntityClass=Well.class)
public class NaturalProductReagent extends Reagent
{
  private static final long serialVersionUID = 1L;

  /**
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  protected NaturalProductReagent() {}

  NaturalProductReagent(ReagentVendorIdentifier rvi, Well well, LibraryContentsVersion libraryContentsVersion)
  {
    super(rvi, well, libraryContentsVersion);
  }

  @Override
  public Object acceptVisitor(AbstractEntityVisitor visitor)
  {
    return visitor.visit(this);
  }
}
