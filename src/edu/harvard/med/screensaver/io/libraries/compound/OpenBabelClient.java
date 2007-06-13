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

import edu.harvard.med.screensaver.ScreensaverProperties;

/**
 * Encapsulates the details of locating the Open Babel executable. Converts
 * moleules between input and output types.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class OpenBabelClient
{
  
  // private static final fields
  
  private static final Logger log = Logger.getLogger(OpenBabelClient.class);
  private static final String BABEL_EXECUTABLE_PATH =
    ScreensaverProperties.getProperty("babel.executable.path");

  
  // private fields
  
  private String _output;
  private String _error;
  
  
  // public method
  
  public String convertMolfileToSmiles(String molfile)
  {
    return convertMolecule("mol", "can", molfile);
  }

  public String convertMolfileToInchi(String molfile)
  {
    return convertMolecule("mol", "inchi", molfile);
  }

  public String convertSmilesToInchi(String smiles)
  {
    return convertMolecule("smi", "inchi", smiles);
  }
  public String convertSmilesToMolfile(String smiles)
  {
    return convertMolecule("smi", "mol", smiles);
  }
  
  public synchronized String convertMolecule(
    String inputFormat,
    String outputFormat,
    String input)
  {
    try {
      String [] openBabelArgs = outputFormat.equals("can") ?
        new String [] {
          BABEL_EXECUTABLE_PATH,
          "-xni",
          "-i" + inputFormat,
          "-o" + outputFormat
        } :
        new String [] {
          BABEL_EXECUTABLE_PATH,
          "-i" + inputFormat,
          "-o" + outputFormat
        };
      Process openBabelProcess = Runtime.getRuntime().exec(openBabelArgs);
      OutputStream openBabelInput = openBabelProcess.getOutputStream();
      openBabelInput.write(input.getBytes());
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
            log.info("error reading Open Babel output", e);
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
            log.info("error reading Open Babel error stream", e);
          }
        }
      };
      openBabelErrorThread.start();

      // wait until all output and error has been read
      openBabelOutputThread.join();
      openBabelErrorThread.join();
      
      if (! _error.equals("")) {
        log.debug("error reported from babel tool: " + _error);
      }
    }
    catch (IOException e) {
      log.error("error running Open Babel: " + e.getMessage());
      return null;
    }
    catch (InterruptedException e) {
      log.info("error waiting for Open Babel to complete: " + e.getMessage());
      return null;
    }
    
    if ((outputFormat.equals("inchi") || outputFormat.equals("can"))  && _output.indexOf('\n') != -1) {
      // trim useless \n
      _output = _output.substring(0, _output.indexOf('\n'));
    }
    
    return _output;
  }
}

