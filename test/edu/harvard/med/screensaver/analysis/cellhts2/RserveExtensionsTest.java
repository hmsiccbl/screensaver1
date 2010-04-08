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
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import edu.harvard.med.screensaver.AbstractSpringTest;
import edu.harvard.med.screensaver.analysis.cellhts2.RException;
import edu.harvard.med.screensaver.analysis.cellhts2.RserveExtensions;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;

import java.util.*;
import org.apache.commons.lang.StringUtils;

public class RserveExtensionsTest extends AbstractSpringTest {
	// static members

	private static Logger log = Logger.getLogger(RserveExtensionsTest.class);

	/** 
	 * test difference between R error, and Rserve error.
	 */
	
	public void testTryEvalRmessage() throws RserveException, REXPMismatchException{
		// 1. PREPARE INPUT
		RserveExtensions rserveExtensions = new RserveExtensions();
	    RConnection conn = null;
	    try {
	    	 conn= new RConnection();
		// 2. RUN METHOD
			try {
				 rserveExtensions.tryEval(conn,"a");
			}catch (RException rs) {
				String s = rs.getMessage();
		// 3. CHECK EXPECTED VS ACTUAL
				String expected = "R error: \"Error in try({ : object \"a\" not found\n\"";
				assertTrue("expected: " + expected + " , actual:" + s , expected.equals(s) );
			}
		}finally {
			if (conn != null) {
				conn.close();
			}
		}
	}
	
	// TODO write separate test for RserveException

}
