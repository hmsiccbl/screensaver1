// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import com.google.common.base.Joiner;

import edu.harvard.med.screensaver.util.Pair;

public class LibraryCopyPlateListParser
{
  private static Logger log = Logger.getLogger(LibraryCopyPlateListParser.class);

  public static LibraryCopyPlateListParserResult parsePlateCopies(String plateCopyList)
  {
    LibraryCopyPlateListParserResult result = new LibraryCopyPlateListParserResult();

    if (plateCopyList == null || StringUtils.isEmpty(plateCopyList.trim())) {
      result.addError("no search terms specified");
      return result;
    }
    plateCopyList = plateCopyList.trim();

    Pattern numberCopyPattern = Pattern.compile("(^\\d+)([^\\-]*)$"); // modified, re [#2728] do not require whitespace or punctuation between plate and copy name
    Pattern numberRangePattern = Pattern.compile("^(\\d+)[-]+(\\d+)$"); // note [-]+ allows more than one dash
    // TODO: there may be other error patterns
    Pattern errorRangePattern = Pattern.compile("^(\\d+)[-]+$");

    List<String> list = edu.harvard.med.screensaver.util.StringUtils.tokenizeQuotedWordList(plateCopyList);

    if (log.isDebugEnabled()) log.debug("parsed terms: " + Joiner.on(",").join(list));
    for (String s : list) {
      if (numberCopyPattern.matcher(s).matches()) {
        Matcher m = numberCopyPattern.matcher(s);
        if (m.matches()) { // here, allow the plate-copy to be concatenated, as in "111A"
          result.addPlate(Integer.parseInt(m.group(1)));
          String temp = m.group(2);
          if (!StringUtils.isEmpty(temp)) {
            result.addCopy(temp);
          }
        }
      }
      else if (numberRangePattern.matcher(s).matches()) {
        Matcher m = numberRangePattern.matcher(s);
        if (m.matches()) {
          Integer first = Integer.parseInt(m.group(1));
          Integer second = Integer.parseInt(m.group(2));
          result.addPlateRange(Pair.newPair(first, second));
        }
        else {/** nop **/
        }
      }else { // implied: if the other patterns don't match, then this is the "copyMatcher"
        if (errorRangePattern.matcher(s).matches()) {
          result.addError("unparseable range: " + s);
        }else{
          // Remove the quote chars that were left in by the top level matcher
          s = s.replaceAll("\"|'+", "");
          result.addCopy(s);
        }
      }
    }
    if (log.isDebugEnabled()) log.debug("parsed: " + result);
    return result;
  }
}

