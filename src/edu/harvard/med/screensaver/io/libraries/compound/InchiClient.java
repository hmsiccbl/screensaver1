// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.compound;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

/**
 * Encapsulates the details of locating the Open Babel executable. Converts
 * moleules between input and output types.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class InchiClient
{
  
  // private static final fields
  
  private static final Logger log = Logger.getLogger(InchiClient.class);
  private static final String INCHI_EXECUTABLE_PATH =
    "/Users/s/Documents/wj/inchi/InChI-1-API/INCHI_API/gcc_makefile/cInChI-1";

  
  // private fields
  
  private String _output;
  private String _error;
  
  
  // public method
  
  public String convertMolfileToInchi(String molfile)
  {
    try {
      Process openBabelProcess = Runtime.getRuntime().exec(new String [] {
        INCHI_EXECUTABLE_PATH,
        "-AuxNone",
        "-STDIO",
        //"-SRel",
        //"-SRac",
        //"-SUCF",
      });
      OutputStream openBabelInput = openBabelProcess.getOutputStream();
      openBabelInput.write(molfile.getBytes());
      openBabelInput.close();
      
      // capture the output
      final InputStream openBabelOutput = openBabelProcess.getInputStream();
      Thread openBabelOutputThread = new Thread() {
        public void run()
        {
          _output = "";
          try {
            for (
              int ch = openBabelOutput.read();
              ch != -1;
              ch = openBabelOutput.read()) {
              _output += (char) ch;
            }
          }
          catch (IOException e) {
            log.info("error reading cInChI output", e);
          }
        }
      };
      openBabelOutputThread.start();
      
      // capture the error
      final InputStream openBabelError = openBabelProcess.getErrorStream();
      Thread openBabelErrorThread = new Thread() {
        public void run()
        {
          _error = "";
          try {
            for (
              int ch = openBabelError.read();
              ch != -1;
              ch = openBabelError.read()) {
              _error += (char) ch;
            }
          }
          catch (IOException e) {
            log.info("error reading cInChI error stream", e);
          }
        }
      };
      openBabelErrorThread.start();

      // wait until all output and error has been read
      openBabelOutputThread.join();
      openBabelErrorThread.join();
      
      if (! _error.equals("")) {
        // this tool writes copious amounts to STDERR that is normally not worth looking at -
        // probably not even for debugging purposes - so i am turning off debugging output
        // from this tool for now. -s
        //log.debug("error reported from InChI tool: " + _error);
      }
    }
    catch (IOException e) {
      log.error("error running cInChI: " + e.getMessage());
      return null;
    }
    catch (InterruptedException e) {
      log.info("error waiting for cInChI to complete: " + e.getMessage());
      return null;
    }
    
    // trim useless first line
    _output = _output.substring(_output.indexOf('\n') + 1);
    
    // trim useless trailing \n
    _output = _output.substring(0, _output.indexOf('\n'));

    return _output;
  }
}

