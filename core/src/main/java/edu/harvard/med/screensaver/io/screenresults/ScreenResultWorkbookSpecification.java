// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.screenresults;



public interface ScreenResultWorkbookSpecification
{
  public static final String DATA_COLUMNS_SHEET_NAME = "Data Columns";
  public static final int DATA_COLUMNS_SHEET__FIRST_DATA_COLUMN__WORKSHEET_COLUMN_INDEX = 1;
  public static final int DATA_SHEET__FIRST_DATA_ROW_INDEX = 1;

  /**
   * The rows of the "Data Columns" worksheet.
   */
  public enum DataColumnProperty {
    COLUMN_IN_DATA_WORKSHEET("\"Data\" Worksheet Column"),
    NAME("Name"),
    DATA_TYPE("Data Type"),
    DECIMAL_PLACES("Decimal Places"),
    DESCRIPTION("Description"),
    REPLICATE("Replicate Number"),
    TIME_POINT("Time point"),
    ASSAY_READOUT_TYPE("Assay readout type"),
    HOW_DERIVED("If derived, how?"),
    COLUMNS_DERIVED_FROM("If derived, from which columns?"),
    PRIMARY_OR_FOLLOWUP("Primary or Follow Up?"),
    ASSAY_PHENOTYPE("Which Assay Phenotype does it belong to?"),
    COMMENTS("Comments"),
    CHANNEL("Channel"),
    TIME_POINT_ORDINAL("Time point ordinal"),
    ZDEPTH_ORDINAL("Zdepth ordinal"),
    CELL_LINE("Cell Line");

    private String _displayText;

    private DataColumnProperty(String displayText)
    {
      _displayText = displayText;
    }

    public String getDisplayText()
    {
      return _displayText;
    }
    
    public static DataColumnProperty fromDisplayText(String displayText)
    {
      for (DataColumnProperty e : values()) {
        if (e.getDisplayText().equalsIgnoreCase(displayText)) {
          return e;
        }
      }
      return null;
    }
  };

  public static final int METADATA_ROW_COUNT = DataColumnProperty.values().length;
  public static final String FILENAMES_LIST_DELIMITER = "\\s*,\\s*";

  /**
   * The standard columns of the "Data" worksheets. Order of enum values is
   * significant, as we use the ordinal() method.
   */
  public enum WellInfoColumn {
    PLATE("Plate"),
    WELL_NAME("Well"),
    ASSAY_WELL_TYPE("Type"),
    EXCLUDE("Exclude");

    private String _displayText;

    private WellInfoColumn(String displayText)
    {
      _displayText = displayText;
    }

    public String getDisplayText()
    {
      return _displayText;
    }
  }

  public static final String YES_VALUE = "yes";
  public static final String NO_VALUE = "no";

  public static final String PRIMARY_VALUE = "primary";
  public static final String FOLLOWUP_VALUE = "follow up";

  public static final String EXCLUDE_ALL_VALUE = "all";

  // TODO: move to WellKey (or some central location)
  public static final String PLATE_NUMBER_REGEX = "(PL[-_])?(\\d+)(\\.0)?";
  public static final String WELL_NAME_REGEX = "[A-P]\\d\\d";

}

