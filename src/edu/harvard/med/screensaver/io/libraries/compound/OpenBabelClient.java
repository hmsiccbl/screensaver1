// $HeadURL$
// $Id$
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.ScreensaverProperties;

/**
 * Encapsulates the details of locating the Open Babel executable. Converts
 * moleules between input and output types.
 * <p>
 * This no longer works with Open Babel 2.0.2. Works with 2.1.0 and 2.1.1., but be
 * careful, 2.1.0 has a significant bug that causes converting SMILES to InChI to
 * crash quite regularly. Use 2.1.1 - if it hasn't been officially released yet, get
 * a snapshot. It's better than 2.1.0.
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
  private String _stderrOutput;
  
  
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
            log.error("error reading Open Babel output", e);
          }
        }
      };
      openBabelOutputThread.start();
      
      // capture the error
      final InputStream openBabelError = openBabelProcess.getErrorStream();
      Thread openBabelErrorThread = new Thread() {
        public void run()
        {
          _stderrOutput = "";
          try {
            for (
              int ch = openBabelError.read();
              ch != -1;
              ch = openBabelError.read()) {
              _stderrOutput += (char) ch;
            }
          }
          catch (IOException e) {
            log.error("error reading Open Babel error stream", e);
          }
        }
      };
      openBabelErrorThread.start();

      // wait until all output and error has been read
      openBabelOutputThread.join();
      openBabelErrorThread.join();
      
      if (_stderrOutput.contains("error")) {
        log.error("error reported from babel tool:\n" + _stderrOutput);
      }
      else if (log.isDebugEnabled()) {
        log.debug("stderr output from babel tool:\n" + _stderrOutput);
      }
    }
    catch (IOException e) {
      log.error("error running Open Babel: " + e.getMessage());
      return null;
    }
    catch (InterruptedException e) {
      log.error("error waiting for Open Babel to complete: " + e.getMessage());
      return null;
    }
    
    // if there was an error in attempting to parse the molfile alias block, then strip
    // all the alias blocks and retry
    if (_stderrOutput.contains("  Error in alias block")) {
      log.warn("encountered an error in alias block: trimming alias block and retrying");
      Matcher matcher = Pattern.compile(
        "(.*)(^A\\s+\\d+\\s*^.*?$\\s+^)+(.*)", Pattern.MULTILINE | Pattern.DOTALL).matcher(input);
      if (matcher.matches()) {
        input = matcher.group(1) + matcher.group(3);
        return convertMolecule(inputFormat, outputFormat, input);
      }
    }
    
    if ((outputFormat.equals("inchi") || outputFormat.equals("can"))  && _output.indexOf('\n') != -1) {
      // trim useless \n
      _output = _output.substring(0, _output.indexOf('\n'));
    }
    
    return _output;
  }
}

