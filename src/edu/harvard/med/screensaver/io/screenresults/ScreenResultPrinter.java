// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.screenresults;

import java.io.PrintWriter;
import java.util.TreeSet;

import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;

public class ScreenResultPrinter
{
  private ScreenResult _screenResult;

  public ScreenResultPrinter(ScreenResult screenResult)
  {
    _screenResult = screenResult;
  }

  public void print()
  {
    print(null);
  }

  public void print(Integer maxResultValuesToPrint)
  {
    PrintWriter printer = new PrintWriter(System.out);
    if (_screenResult == null) {
      printer.println("ScreenResult was not parsed (probably due to an invalid input file)");
      return;
    }

    printer.println("dateCreated=" + _screenResult.getDateCreated());
    printer.println("replicateCount=" + _screenResult.getReplicateCount());
    printer.println("dataColumnCount=" + _screenResult.getDataColumns().size());

    for (DataColumn dataColumn : _screenResult.getDataColumns()) {
      printer.println("Data Column:");
      printer.println("\tordinal=" + dataColumn.getOrdinal());
      printer.println("\tname=" + dataColumn.getName());
      printer.println("\tdataType=" + dataColumn.getDataType());
      printer.println("\tdecimalPlaces=" + dataColumn.getDecimalPlaces());
      printer.println("\tdescription="+dataColumn.getDescription());
      printer.println("\tisNumeric=" + dataColumn.isNumeric());
      printer.println("\treplicateOrdinal=" + dataColumn.getReplicateOrdinal());
      printer.println("\ttimePoint=" + dataColumn.getTimePoint());
      printer.println("\tassayPhenotype=" + dataColumn.getAssayPhenotype());
      printer.println("\tassayReadoutType=" + dataColumn.getAssayReadoutType());
      printer.println("\tisDerived=" + dataColumn.isDerived());
      printer.println("\tderivedFrom="+dataColumn.getTypesDerivedFrom()); // TODO
      printer.println("\thowDerived=" + dataColumn.getHowDerived());
      printer.println("\tisActivityIndicator=" + dataColumn.isPositiveIndicator());
      printer.println("\tisFollowupData=" + dataColumn.isFollowUpData());
      printer.println("\tcomments="+dataColumn.getComments());

      int nResultValues = dataColumn.getResultValues().size();
      printer.println("\tResult Values: (" + nResultValues + ")");
      int n = 0;
      boolean ellipsesOnce = false;

      //TODO: reload the dataColumns for the wells here because we are clearing them during parse now - sde4
      for (WellKey wellKey : new TreeSet<WellKey>(dataColumn.getWellKeyToResultValueMap().keySet())) {
        if (maxResultValuesToPrint != null) {
          if (n < maxResultValuesToPrint / 2 || n >= nResultValues - maxResultValuesToPrint / 2) {
            ResultValue resultValue = dataColumn.getWellKeyToResultValueMap().get(wellKey);
            printer.println("\t\t" + wellKey + "\t" + resultValue.getTypedValue());
          }
          else if (!ellipsesOnce) {
            printer.println("\t\t...");
            ellipsesOnce = true;
          }
        }
        ++n;
      }
    }
    //Note: don't do this as it closes the output stream for other services like log4j: printer.close();
  }
}
