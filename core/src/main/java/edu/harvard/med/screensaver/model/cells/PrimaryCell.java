// $HeadURL: http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/serickson/3200/core/src/main/java/edu/harvard/med/screensaver/model/libraries/Gene.java $
// $Id: Gene.java 6946 2012-01-13 18:24:30Z seanderickson1 $
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.cells;

import java.util.SortedSet;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.PrimaryKeyJoinColumn;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;

import edu.harvard.med.screensaver.model.annotations.IgnoreImmutabilityTest;


/**
 * Information about a Primary Cell used in LINCS.
 * 
 */
@Entity
@Immutable
@IgnoreImmutabilityTest
@PrimaryKeyJoinColumn(name="cellId")
@org.hibernate.annotations.ForeignKey(name = "fk_primary_cell_to_cell")
@org.hibernate.annotations.Proxy
public class PrimaryCell extends Cell
{
  private static final long serialVersionUID = 0L;

  private String donorEthnicity;
  private int ageInYears;
  private String donorHealthStatus;
  private SortedSet<String> cellMarkers; // TODO: Controlled Vocab
  private int passageNumber;
  
	/**
   * @motivation for hibernate and proxy/concrete subclass constructors
   */
  public PrimaryCell() {
  	super();
  }
  
  public String getDonorEthnicity() {
		return donorEthnicity;
	}

	public void setDonorEthnicity(String donorEthnicity) {
		this.donorEthnicity = donorEthnicity;
	}

	public int getAgeInYears() {
		return ageInYears;
	}

	public void setAgeInYears(int ageInYears) {
		this.ageInYears = ageInYears;
	}

	public String getDonorHealthStatus() {
		return donorHealthStatus;
	}

	public void setDonorHealthStatus(String donorHealthStatus) {
		this.donorHealthStatus = donorHealthStatus;
	}

  @ElementCollection
  @Column(name="cellMarkers", nullable=false)
  @JoinTable(
    name="cellMarkers",
    joinColumns=@JoinColumn(name="cellId")
  )
  @Sort(type=SortType.NATURAL)
  @org.hibernate.annotations.Type(type="text")
  @org.hibernate.annotations.ForeignKey(name="fk_cell_markers_to_cell")
	public SortedSet<String> getCellMarkers() {
		return cellMarkers;
	}

	public void setCellMarkers(SortedSet<String> cellMarkers) {
		this.cellMarkers = cellMarkers;
	}

	public int getPassageNumber() {
		return passageNumber;
	}

	public void setPassageNumber(int passageNumber) {
		this.passageNumber = passageNumber;
	}


}
