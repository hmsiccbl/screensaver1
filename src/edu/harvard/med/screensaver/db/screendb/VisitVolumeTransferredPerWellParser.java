// $HeadURL$
// $Id$
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

import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.Volume.Units;

import org.apache.log4j.Logger;

/**
 * Parses volume transferred per well values from ScreenDB visits into 
 * Volume values, according to an email group discussion on the matter. 
 * relevant quote from the group discussion is here:
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

  public Volume getVolumeTransferredPerWell(
    String volumeTransferredString,
    String screenType)
  throws ScreenDBSynchronizationException
  {
    Volume volumeTransferredPerWell = null;
    if (volumeTransferredString != null &&
      ! volumeTransferredString.equals("") &&
      ! volumeTransferredString.equals("0")) {
      volumeTransferredPerWell = getNumericalVolumeTransferred(volumeTransferredString, screenType);
    }
    return volumeTransferredPerWell;
  }

  
  // private instance methods
  
  private Volume getNumericalVolumeTransferred(String volumeTransferredString,
                                                   String screenType)
  {
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
    BigDecimal numericalVolumeTransferred;
    if (operator == null) {
      numericalVolumeTransferred = new BigDecimal(leftOperandString);
    }
    else {
      float rightOperand = Float.parseFloat(rightOperandString);
      if (operator.equalsIgnoreCase("x")) {
        numericalVolumeTransferred = new BigDecimal(leftOperand).multiply(new BigDecimal(rightOperand));
      }
      else {
        assert(operator.equals("and"));
        numericalVolumeTransferred = new BigDecimal(leftOperand).add(new BigDecimal(rightOperand));
      }
    }
    return new Volume(numericalVolumeTransferred.toString(), 
                          getVolumeTransferredUnits(screenType, 
                                                    volumeTransferredString, 
                                                    numericalVolumeTransferred));
  }
  
  private Units getVolumeTransferredUnits(
    String screenType,
    String volumeTransferredString,
    BigDecimal numericalVolumeTransferred)
  {
    if (screenType.equals("RNAi")) {
      return Units.MICROLITERS;
    }
    if (volumeTransferredString.contains("nl")) {
      return Units.NANOLITERS;
    }
    if (volumeTransferredString.contains("nL")) {
      return Units.NANOLITERS;
    }
    if (volumeTransferredString.contains("ul")) {
      return Units.MICROLITERS;
    }
    if (volumeTransferredString.contains("uL")) {
      return Units.MICROLITERS;
    }
    return numericalVolumeTransferred.compareTo(BigDecimal.TEN) > 1 ? Units.NANOLITERS : Units.MICROLITERS;
  }
}
