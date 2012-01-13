// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.analysis.cellhts2;

import org.apache.log4j.Logger;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

public class RserveExtensions {
	// static members

	private static Logger log = Logger.getLogger(RserveExtensions.class);

	// TODO when thread safe, probably this method can be made static
	// .. as in the original example code of Simon Urbanek
	
  // Rserve will only return a general error 127 when something goes wrong
  // .. in order to get the actual R error message, tryEval statement is created.
	
	public REXP tryEval(RConnection c, String s) throws RserveException,  
	RException, REXPMismatchException {
		//  silent: logical: should the report of error messages be suppressed
	   REXP r  = c.eval("try({" + s + "}, silent=TRUE)");
	   if (r.inherits("try-error")) throw new RException(r.asString());
	   return r;
	}


}
