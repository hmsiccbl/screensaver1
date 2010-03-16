// $HeadURL$
// $Id$
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

import edu.harvard.med.screensaver.model.screenresults.DataType;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;

import org.apache.log4j.Logger;

import com.google.common.base.Joiner;

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
     new MetaDataTableRowDefinition<ResultValueType>("dataType", "Data Type", Joiner.on(", ").join(DataType.values())),
     new MetaDataTableRowDefinition<ResultValueType>("decimalPlaces", "Decimal Places", "The number of decimal places that are significant"),
     new MetaDataTableRowDefinition<ResultValueType>("replicateOrdinal", "Replicate Number", "To which replicate this data header refers"),
     new MetaDataTableRowDefinition<ResultValueType>("timePoint", "Time Point", "The time point the readout was taken"),
     new MetaDataTableRowDefinition<ResultValueType>("timePointOrdinal", "Time Point Ordinal", "The ordinal of the time point the image was taken"),
     new MetaDataTableRowDefinition<ResultValueType>("channel", "Channel", "The channel in which the readout was done"),
     new MetaDataTableRowDefinition<ResultValueType>("zdepthOrdinal", "Zdepth Ordinal", "The depth or z-value the image was taken "),
     
     new MetaDataTableRowDefinition<ResultValueType>("assayReadoutType", "Assay Readout Type", "The type of readout used to calculate these values"),
     new MetaDataTableRowDefinition<ResultValueType>("derived", "Derived", "True when this column is derived from other data headers"),
     new MetaDataTableRowDefinition<ResultValueType>("howDerived", "How Derived", "How this column was derived from other data headers"),
     new MetaDataTableRowDefinition<ResultValueType>("typesDerivedFrom", "Derived From", "The data headers from which this column was derived")
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

