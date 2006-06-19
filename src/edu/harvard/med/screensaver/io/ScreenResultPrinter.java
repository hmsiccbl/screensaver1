// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io;

import java.io.PrintWriter;

import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
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
    
    printer.println("dateCreated=" + _screenResult.getDateCreated());
    printer.println("replicateCount=" + _screenResult.getReplicateCount());
    printer.println("dataHeaderCount=" + _screenResult.getResultValueTypes().size());
    
    for (ResultValueType rvt : _screenResult.getResultValueTypes()) {
      printer.println("Result Value Type:");
      printer.println("\tordinal=" + rvt.getOrdinal());
      printer.println("\tname=" + rvt.getName());
      printer.println("\tdescription="+rvt.getDescription());
      printer.println("\treplicateOrdinal=" + rvt.getReplicateOrdinal());
      printer.println("\ttimePoint=" + rvt.getTimePoint());
      printer.println("\tassayPhenotype=" + rvt.getAssayPhenotype());
      printer.println("\tassayReadoutType=" + rvt.getAssayReadoutType());
      printer.println("\tisDerived=" + rvt.isDerived());
      printer.println("\tderivedFrom="+rvt.getDerivedFrom()); // TODO
      printer.println("\thowDerived=" + rvt.getHowDerived());
      printer.println("\tisActivityIndicator=" + rvt.isActivityIndicator());
      printer.println("\tactivityIndicatorType=" + rvt.getActivityIndicatorType());
      printer.println("\tindicator cutoff=" + rvt.getIndicatorCutoff());
      printer.println("\tindicator direction=" + rvt.getIndicatorDirection());
      printer.println("\tisFollowupData=" + rvt.isFollowUpData());
      printer.println("\tcherry pick=" + rvt.isCherryPick());
      
      printer.println("\tcomments="+rvt.getComments());

      printer.println("\tResult Values: (" + rvt.getResultValues().size() + ")");
      int n = 0;
      for (ResultValue rv : rvt.getResultValues()) {
        printer.println("\t\t" + rv.getValue());
        if (maxResultValuesToPrint != null && ++n >= maxResultValuesToPrint) {
          printer.println("\t\t...");
          break;
        }
      }
    }
    printer.close();
  }
}
