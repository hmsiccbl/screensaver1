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
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.springframework.test.annotation.IfProfileValue;

import edu.harvard.med.screensaver.AbstractSpringTest;

@IfProfileValue(name = "screensaver.ui.feature.cellHTS2", value = "true")
public class RserveExtensionsTest extends AbstractSpringTest
{

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
        String expected = "R error: \"Error in try({ : object 'a' not found\n\"";
				assertTrue("expected: " + expected + " , actual:" + s , expected.equals(s) );
			}
		}finally {
			if (conn != null) {
				conn.close();
			}
		}
	}
	
	 public void testRetrieveThreeDimensionalObject() throws RserveException, REXPMismatchException{
	    // 1. PREPARE INPUT
	    RserveExtensions rserveExtensions = new RserveExtensions();
	    RConnection conn = null;
	      try {
	         conn= new RConnection();
  	    // 2. RUN METHOD
  	      try {
             rserveExtensions.tryEval(conn,"");	         
  	         String rExpr= " nrWells = 6; nrPlates = 1; nrReps = 2; nrChannels = 2; " +
  	           "dimNames <- list(Features=c(1:6),Sample=c(1,2),Channels=c(\"ch1\",\"ch2\"));" + 
  	           "dataNormTarget <- array(as.numeric(NA), dim=c(nrWells * nrPlates,nrReps,nrChannels), dimnames=dimNames);" +
  	           "dataNormTarget[,1,1] <- c(0.25,0.75,0.5,0.75,1.25,1);" + //#r1_c1
  	           "dataNormTarget[,2,1] <- c(0.5,0.83,0.67,0.83,1.17,1);" + //#r2_c1 
  	           "dataNormTarget[,1,2] <- c(0.57,0.86,0.71,0.86,1.14,1);" + //#r1_c2 
  	           "dataNormTarget[,2,2] <- c(0.67,0.89,0.78,0.89,1.11,1);" + //#r2_c2
  	           "dataNormTarget";
  	         
  	         REXP result = rserveExtensions.tryEval(conn,rExpr);  
  	         int[] dim = result.dim();
  	         // [6, 2, 2]
  	         int i = 1;
  	         
  	      }catch (RException rs) {
  	        String s = rs.getMessage();
  	      }
	    }finally {
	      if (conn != null) {
	        conn.close();
	      }
	    }
	  }
	
	// TODO write separate test for RserveException

}
