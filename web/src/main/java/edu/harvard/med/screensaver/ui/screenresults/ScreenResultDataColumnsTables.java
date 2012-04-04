// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screenresults;

import java.util.Arrays;
import java.util.List;

import com.google.common.base.Joiner;

import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screenresults.DataType;

public class ScreenResultDataColumnsTables extends MetaDataTable<DataColumn>
{
  // TODO: consider replacing MetaDataTableRowDefinition with TableColumn<DataColumn>
  @SuppressWarnings("unchecked")
  private final List<MetaDataTableRowDefinition<DataColumn>> DATA_COLUMN_PROPERTIES =
    Arrays.asList
    (
     new MetaDataTableRowDefinition<DataColumn>("description", "Description", "A description of the data column"),
     new MetaDataTableRowDefinition<DataColumn>("dataType", "Data Type", Joiner.on(", ").join(DataType.values())),
     new MetaDataTableRowDefinition<DataColumn>("decimalPlaces", "Decimal Places", "The number of decimal places that are significant"),
     new MetaDataTableRowDefinition<DataColumn>("replicateOrdinal", "Replicate Number", "To which replicate this data column refers"),
     new MetaDataTableRowDefinition<DataColumn>("timePoint", "Time Point", "The time point the readout was taken"),
     new MetaDataTableRowDefinition<DataColumn>("experimentalCellInformationSet", "CLO IDs", " CLO IDs for the cell line used"),
     new MetaDataTableRowDefinition<DataColumn>("timePointOrdinal", "Time Point Ordinal", "The ordinal of the time point the image was taken"),
     new MetaDataTableRowDefinition<DataColumn>("channel", "Channel", "The channel in which the readout was done"),
     new MetaDataTableRowDefinition<DataColumn>("zdepthOrdinal", "Zdepth Ordinal", "The depth or z-value the image was taken "),

     new MetaDataTableRowDefinition<DataColumn>("assayReadoutType", "Assay Readout Type", "The type of readout used to calculate these values"),
     new MetaDataTableRowDefinition<DataColumn>("derived", "Derived", "True when this column is derived from other data columns"),
     new MetaDataTableRowDefinition<DataColumn>("howDerived", "How Derived", "How this column was derived from other data columns"),
     new MetaDataTableRowDefinition<DataColumn>("typesDerivedFrom", "Derived From", "The data columns from which this column was derived")
     {
       @Override
       public String formatValue(DataColumn col)
       {
         StringBuilder typesDerivedFromText = new StringBuilder();
         for (DataColumn derivedFromCol : col.getTypesDerivedFrom()) {
           if (typesDerivedFromText.length() > 0) {
             typesDerivedFromText.append(", ");
           }
           typesDerivedFromText.append(derivedFromCol.getName());
         }
         return typesDerivedFromText.toString();
       }
     },
     new MetaDataTableRowDefinition<DataColumn>("followUpData", "Follow Up Data", "Primary or follow up screen data"),
     new MetaDataTableRowDefinition<DataColumn>("assayPhenotype", "Assay Phenotype", "The phenotype being monitored"),
       new MetaDataTableRowDefinition<DataColumn>("comments", "Comments", "Data column comments"));


  @Override
  protected List<MetaDataTableRowDefinition<DataColumn>> getMetaDataTableRowDefinitions()
  {
    return DATA_COLUMN_PROPERTIES;
  }
}

