// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletException;

import edu.harvard.med.screensaver.ui.AbstractController;

/**
 * 
 * @author adapted from code found at http://wiki.apache.org/myfaces/Handling_Server_Errors
 */
public class ExceptionReporterController extends AbstractController
{
    public String getInfoMessage() 
    {
      return "An unexpected processing error has occurred.  Some developer is going to be in big trouble!";
    }

    public String getStackTrace() 
    {
        Throwable ex = (Throwable) getRequestMap().get("javax.servlet.error.exception");
        StringWriter writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        fillStackTrace(ex, pw);
        return writer.toString();
    }
    
    private void fillStackTrace(Throwable ex, PrintWriter pw) 
    {
      if (ex == null) {
        return;
      }

      ex.printStackTrace(pw);

      if (ex instanceof ServletException) {
        Throwable cause = ((ServletException) ex).getRootCause();
        
        if (null != cause) {
          pw.println("Root Cause:");
          fillStackTrace(cause, pw);
        }
      } else {
        Throwable cause = ex.getCause();
        
        if (null != cause) {
          pw.println("Cause:");
          fillStackTrace(cause, pw);
        }
      }
    }

}
