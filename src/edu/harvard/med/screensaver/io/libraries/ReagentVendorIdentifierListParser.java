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

  // public constructors and methods

  public ReagentVendorIdentifierParserResult parseReagentVendorIdentifiers(String vendorName,
                                                                           String reagentVendorIdentifierList)
  {
    ReagentVendorIdentifierParserResult result = new ReagentVendorIdentifierParserResult();
    BufferedReader inputReader = new BufferedReader(new StringReader(reagentVendorIdentifierList));
    try {
      for (String identifier = inputReader.readLine(); identifier != null; identifier = inputReader.readLine()) {
        // trim leading and trailing whitespace, and skip blank lines
        identifier = identifier.trim();
        if (identifier.length() > 0) {
          ReagentVendorIdentifier rvi = new ReagentVendorIdentifier(vendorName, identifier);
        // TODO: we could maintain a list of valid vendor identifier regex patterns for each vendor, and validate the input
//          if (...) {
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
}

