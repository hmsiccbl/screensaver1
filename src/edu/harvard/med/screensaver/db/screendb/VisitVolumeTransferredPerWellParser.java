// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.screendb;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * Parses volume transferred per well values from ScreenDB visits into microliter-units
 * BigDecimal values, according to an email group discussion on the matter. relevant quote
 * from the group discussion is here:
 * 
 * <blockquote>
 * 2. Units for Volume of Compound Transferred: For one RNAi Screen (612), 
 * I have unitless values "11" that I will take as "11 uL". For the rest, I 
 * will take unitless values >= 12 to have nL as unit, and values <= 2.5 to 
 * have uL as unit. The report includes a worksheet with all visits with 
 * unitless values.
 * 
 * FURTHER COMMENT FROM SU: If it helps, you can also use the rule that RNAi
 * screens are always going to be uL quantities, because transfers are always
 * done with tips and not pin arrays. 
 *
 * 3. Products and Sums for Volume of Compound Transferred: For the 
 * products, such as "2 X 40nL", 8 out of 28 have a "Number of Replicates" 
 * that matches the unitless multiplicand. This is not a great match, so I 
 * don't want to try to deduce much from that. The report includes a 
 * worksheet with these visits, including my notes on how the Assay 
 * Protocol jives with the values for Number of Replicates and Volume of 
 * Compound Transferred. (In most cases, they do not jive very well. The 
 * impression I got is that the assay protocols recorded were preliminary 
 * versions, and were tweaked later on, so that the actual protocol for the
 * visit differs in minor ways from the protocol recorded in ScreenDB.)
 *
 * For the time being, when synchronizing, I will just carry out the 
 * arithmetic, store the total volume transferred with nL units, and add a 
 * note in the comments section containing the original value. If we manage 
 * to clean this data up in ScreenDB, then the cleaned up data will 
 * transfer to Screensaver as soon as the ScreenDBSynchronizer is run again.
 * </blockquote>
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class VisitVolumeTransferredPerWellParser
{
  // static members

  private static Logger log = Logger.getLogger(VisitVolumeTransferredPerWellParser.class);
  private static Pattern _numericalVolumeTransferredPattern =
    Pattern.compile(".*?([\\d.]+)(([nu][lL])?\\s*(x|X|and)\\s*(\\d+))?.*");
  
  
  // public instance methods

  public BigDecimal getVolumeTransferredPerWell(
    String volumeTransferredString,
    String screenType)
  throws ScreenDBSynchronizationException
  {
    BigDecimal microliterVolumeTransferedPerWell = null;
    if (volumeTransferredString != null &&
      ! volumeTransferredString.equals("") &&
      ! volumeTransferredString.equals("0")) {
      
      // get the numerical portion of the volume transferred string
      float numericalVolumeTransferred =
        getNumericalVolumeTransferred(volumeTransferredString);
      
      // units are either nL or uL - figure out which
      boolean unitsAreNanoliters = areVolumeTransferredUnitsNanoliters(
        screenType, volumeTransferredString, numericalVolumeTransferred);
      
      if (unitsAreNanoliters) {
        microliterVolumeTransferedPerWell =
          new BigDecimal(numericalVolumeTransferred / 1000);
      }
      else {
        microliterVolumeTransferedPerWell = new BigDecimal(numericalVolumeTransferred);
      }
    }
    return microliterVolumeTransferedPerWell;
  }

  
  // private instance methods
  
  private float getNumericalVolumeTransferred(String volumeTransferredString)
  {
    float numericalVolumeTransferred;
    Matcher numericalVolumeTransferredMatcher =
      _numericalVolumeTransferredPattern.matcher(volumeTransferredString);
    if (! numericalVolumeTransferredMatcher.matches()) {
      throw new ScreenDBSynchronizationException(
        "no match found for volume transferred \"" + volumeTransferredString + "\"!");
    }
    String leftOperandString = numericalVolumeTransferredMatcher.group(1);
    String operator = numericalVolumeTransferredMatcher.group(4);
    String rightOperandString = numericalVolumeTransferredMatcher.group(5);
    float leftOperand = Float.parseFloat(leftOperandString);
    if (operator == null) {
      numericalVolumeTransferred = leftOperand;
    }
    else {
      float rightOperand = Float.parseFloat(rightOperandString);
      if (operator.equalsIgnoreCase("x")) {
        numericalVolumeTransferred = leftOperand * rightOperand;
      }
      else {
        assert(operator.equals("and"));
        numericalVolumeTransferred = leftOperand + rightOperand;
      }
    }
    return numericalVolumeTransferred;
  }
  
  private boolean areVolumeTransferredUnitsNanoliters(
    String screenType,
    String volumeTransferredString,
    float numericalVolumeTransferred)
  {
    // RNAi screens are always microliters
    if (screenType.equals("RNAi")) {
      return false;
    }
    if (volumeTransferredString.contains("nl")) {
      return true;
    }
    if (volumeTransferredString.contains("nL")) {
      return true;
    }
    if (volumeTransferredString.contains("ul")) {
      return false;
    }
    if (volumeTransferredString.contains("uL")) {
      return false;
    }
    return numericalVolumeTransferred > 10;
  }
}

