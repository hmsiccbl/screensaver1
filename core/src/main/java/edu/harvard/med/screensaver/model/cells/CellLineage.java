// $HeadURL: http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/serickson/3200/core/src/main/java/edu/harvard/med/screensaver/model/libraries/Gene.java $
// $Id: Gene.java 6946 2012-01-13 18:24:30Z seanderickson1 $
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.cells;

import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;

import org.hibernate.annotations.Immutable;

import edu.harvard.med.screensaver.model.annotations.IgnoreImmutabilityTest;


/**
 * Information about an Immortal Cell Line.
 * 
 */
@Entity
@Immutable
@IgnoreImmutabilityTest
@PrimaryKeyJoinColumn(name="cellId")
@org.hibernate.annotations.ForeignKey(name = "fk_cell_lineage_to_cell")
@org.hibernate.annotations.Proxy
public class CellLineage extends Cell
{
  private static final long serialVersionUID = 0L;

  /**
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  public CellLineage() {}
}
