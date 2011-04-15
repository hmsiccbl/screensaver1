// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.analysis.cellhts2;

public class RException extends RuntimeException { 
	private static final long serialVersionUID = 1L;
	
	public RException(String msg) { 
		super("R error: \""+msg+"\""); 
	} 
}

