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
public class OpenBabelClient
{
  
  // private static final fields
  
  private static final Logger log = Logger.getLogger(OpenBabelClient.class);
  private static final String OPEN_BABEL_EXE = "/usr/local/openbabel/bin/babel";

  
  // private fields
  
  private String _output;
  
  
  // public method
  
  public String convertMolfileToSmiles(String molfile)
  {
    return convertMolecule("mol", "smi", molfile);
  }
  
  public String convertSmilesToInchi(String smiles)
  {
    return convertMolecule("smi", "inchi", smiles);
  }
  
  public synchronized String convertMolecule(
    String inputFormat,
    String outputFormat,
    String input)
  {
    try {
      Process openBabelProcess = Runtime.getRuntime().exec(new String [] {
        OPEN_BABEL_EXE,
        "-i" + inputFormat,
        "-o" + outputFormat
      });
      OutputStream openBabelInput = openBabelProcess.getOutputStream();
      openBabelInput.write(input.getBytes());
      openBabelInput.close();
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
      openBabelOutputThread.join();
    }
    catch (IOException e) {
      log.info("error running Open Babel", e);
    }
    catch (InterruptedException e) {
      log.info("error waiting for Open Babel to complete", e);
    }
    
    if (outputFormat.equals("smi") && _output.indexOf('\t') != -1) {
      // open babel SMILES output is postfixed "\tStructure\d+\n"
      _output = _output.substring(0, _output.indexOf('\t'));
    }
    else if (outputFormat.equals("inchi") && _output.indexOf('\n') != -1) {
      // trim useless \n
      _output = _output.substring(0, _output.indexOf('\n'));
    }
    
    return _output;
  }
}

