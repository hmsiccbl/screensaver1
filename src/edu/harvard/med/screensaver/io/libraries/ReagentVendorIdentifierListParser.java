// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.ui.libraries.ReagentVendorIdentifierParserResult;

import org.apache.log4j.Logger;

public class ReagentVendorIdentifierListParser
{
  // static members

  private static Logger log = Logger.getLogger(ReagentVendorIdentifierListParser.class);

  public ReagentVendorIdentifierParserResult parseReagentVendorIdentifiers(String vendorName,
                                                                           String reagentVendorIdentifierList)
  {
    ReagentVendorIdentifierParserResult result = new ReagentVendorIdentifierParserResult();
    BufferedReader inputReader = new BufferedReader(new StringReader(reagentVendorIdentifierList));
    try {
      int lineNumber = 0;
      for (
        String line = inputReader.readLine();
        line != null;
        line = inputReader.readLine()) {

        ++lineNumber;

        // trim leading and trailing whitespace, and skip blank lines
        line = line.trim();
        if (line.equals("")) {
          continue;
        }

        // tokenize the line
        String [] tokens = line.split("[\\s;,]+");
        if (tokens.length == 0) {
          continue;
        }

        for (int i = 0; i < tokens.length; i ++) {
          ReagentVendorIdentifier rvi = new ReagentVendorIdentifier(vendorName, tokens[i]);
          // TODO: we could maintain a list of valid vendor identifier regex patterns for each vendor, and validate the input
//          if (rvi == null) {
//            result.addError(lineNumber, "invalid reagent vendor identifier " + tokens[i] +
//                            (vendorName == null ? "" : " (vendor " + vendorName + ")"));
//            continue;
//          }
          result.addParsedReagentVendorIdentifier(rvi);
        }
      }
    }
    catch (IOException e) {
      result.addError(0, "internal error: could not read reagentVendorIdentifierList");
    }
    return result;

  }

  // instance data members

  // public constructors and methods

  // private methods

}

