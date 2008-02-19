// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;

import edu.harvard.med.screensaver.model.screenresults.ResultValueType;

import org.apache.log4j.Logger;

public class ResultValueTypesTable extends MetaDataTable<ResultValueType>
{
  // static members

  private static Logger log = Logger.getLogger(ResultValueTypesTable.class);

  // TODO: consider replacing DataHeaderRowDefinition with TableColumn<ResultValueType>
  @SuppressWarnings("unchecked")
  private final List<MetaDataTableRowDefinition<ResultValueType>> DATA_HEADER_ATTRIBUTES =
    Arrays.asList
    (
     new MetaDataTableRowDefinition<ResultValueType>("description", "Description", "A description of the data header"),
     new MetaDataTableRowDefinition<ResultValueType>("replicateOrdinal", "Replicate Number", "To which replicate this data header refers"),
     new MetaDataTableRowDefinition<ResultValueType>("assayReadoutType", "Assay Readout Type", "The type of readout used to calculate these values"),
     new MetaDataTableRowDefinition<ResultValueType>("timePoint", "Time Point", "The time point the readout was taken"),
     new MetaDataTableRowDefinition<ResultValueType>("derived", "Derived", "True when this column is derived from other data headers"),
     new MetaDataTableRowDefinition<ResultValueType>("howDerived", "How Derived", "How this column was derived from other data headers"),
     new MetaDataTableRowDefinition<ResultValueType>("typesDerivedFrom", "Types Derived From", "The data headers from which this column was derived")
     {
       @Override
       public String formatValue(ResultValueType rvt)
       {
         StringBuilder typesDerivedFromText = new StringBuilder();
         for (ResultValueType derivedFromRvt : rvt.getTypesDerivedFrom()) {
           if (typesDerivedFromText.length() > 0) {
             typesDerivedFromText.append(", ");
           }
           typesDerivedFromText.append(derivedFromRvt.getName());
         }
         return typesDerivedFromText.toString();
       }
     },
     new MetaDataTableRowDefinition<ResultValueType>("positiveIndicator", "Positive Indicator", "True if this data header is used to indicate \"positives\""),
     new MetaDataTableRowDefinition<ResultValueType>("positiveIndicatorType", "Positive Indicator Type", "'Numerical', 'Boolean', or 'Partition'"),
     new MetaDataTableRowDefinition<ResultValueType>("positiveIndicatorDirection", "Indicator Direction", "For numerical indicators, whether high or low values are \"positives\""),
     new MetaDataTableRowDefinition<ResultValueType>("positiveIndicatorCutoff", "Indicator Cutoff", "The numerical score demarking \"positives\" from \"non-positives\""),
     new MetaDataTableRowDefinition<ResultValueType>("followUpData", "Follow Up Data", "Primary or follow up screen data"),
     new MetaDataTableRowDefinition<ResultValueType>("assayPhenotype", "Assay Phenotype", "The phenotype being monitored"),
     new MetaDataTableRowDefinition<ResultValueType>("comments", "Comments", "Data header comments"),
     new MetaDataTableRowDefinition<ResultValueType>("positivesCount", "Positives", "The number of \"positives\", if this is a Positive Indicator"),
     new MetaDataTableRowDefinition<ResultValueType>("positivesPercentage", "Positive %", "The % of experimental wells in the results that have been deemed \"positive\"")
     {
       @Override
       public String formatValue(ResultValueType rvt)
       {
         if (rvt.getPositivesRatio() == null) {
           return "";
         }
         return NumberFormat.getPercentInstance().format(rvt.getPositivesRatio());
       }
     }
    );


  @Override
  protected List<MetaDataTableRowDefinition<ResultValueType>> getMetaDataTableRowDefinitions()
  {
    return DATA_HEADER_ATTRIBUTES;
  }


  // instance data members

  // public constructors and methods

  // private methods

}

