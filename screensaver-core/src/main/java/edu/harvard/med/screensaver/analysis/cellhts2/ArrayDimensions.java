// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.analysis.cellhts2;

import org.apache.log4j.Logger;

public class ArrayDimensions {
	private static Logger log = Logger.getLogger(ArrayDimensions.class);
	
	int nrRowsPlate;
	int nrColsPlate;
	int nrWells;
	int nrPlates;
	int nrReps;
	int nrChannels;
	public int getNrRowsPlate() {
		return nrRowsPlate;
	}
	public void setNrRowsPlate(int nrRowsPlate) {
		this.nrRowsPlate = nrRowsPlate;
	}
	public int getNrColsPlate() {
		return nrColsPlate;
	}
	public void setNrColsPlate(int nrColsPlate) {
		this.nrColsPlate = nrColsPlate;
	}
	public int getNrWells() {
		return nrWells;
	}
	public void setNrWells(int nrWells) {
		this.nrWells = nrWells;
	}
	public int getNrPlates() {
		return nrPlates;
	}
	public void setNrPlates(int nrPlates) {
		this.nrPlates = nrPlates;
	}
	public int getNrReps() {
		return nrReps;
	}
	public void setNrReps(int nrReps) {
		this.nrReps = nrReps;
	}
	public int getNrChannels() {
		return nrChannels;
	}
	public void setNrChannels(int nrChannels) {
		this.nrChannels = nrChannels;
	}
	

	public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!this.getClass().equals(obj.getClass())) return false;
        ArrayDimensions o = (ArrayDimensions) obj;
        if (o.getNrRowsPlate() == this.getNrRowsPlate() &&  
	        o.getNrColsPlate() == this.getNrColsPlate() &&
	        o.getNrWells() == this.getNrWells() && 
	        o.getNrReps() == this.getNrReps() && 
	        o.getNrPlates() == this.getNrPlates() && 
	        o.getNrChannels() == this.getNrChannels() 
        ) {
            return true;
        }
        else return false;
	}
	
	public int hashCode() 
	{
    return 17 * nrRowsPlate +
      19 * nrColsPlate +
      23 * nrWells +
      29 * nrPlates +
      31 * nrReps +
      37 * nrChannels;
	}
}
