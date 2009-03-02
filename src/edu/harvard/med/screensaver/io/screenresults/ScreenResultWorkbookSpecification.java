// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.screenresults;

import edu.harvard.med.screensaver.ScreensaverConstants;


public interface ScreenResultWorkbookSpecification
{
  public static final String SCREEN_INFO_SHEET_NAME = "Screen Info";
  public static final String DATA_HEADERS_SHEET_NAME = "Data Headers";
  public static final int FIRST_DATA_SHEET_INDEX = 2;

  public static final int DATA_HEADERS_FIRST_DATA_HEADER_COLUMN_INDEX = 1;
  public static final char DATA_SHEET__FIRST_DATA_HEADER_COLUMN_LABEL = 'E';
  public static final int METADATA_ROW_NAMES_COLUMN_INDEX = 0;
  public static final int RAWDATA_HEADER_ROW_INDEX = 0;
  public static final int RAWDATA_FIRST_DATA_ROW_INDEX = 1;
  public static final int RAWDATA_FIRST_DATA_HEADER_COLUMN_INDEX = DataColumn.values().length;
  public static final char RAWDATA_FIRST_DATA_HEADER_COLUMN_LABEL = (char) ('A' + RAWDATA_FIRST_DATA_HEADER_COLUMN_INDEX);
  public static final int SCREENINFO_ROW_HEADER_COLUMN_INDEX = 0;
  public static final int SCREENINFO_VALUE_COLUMN_INDEX = 1;
  public static final int SCREENINFO_FIRST_DATA_ROW_INDEX = 0;

  public static final String NUMERICAL_INDICATOR_DIRECTION_HIGH_VALUES_INDICATE = ">";
  public static final String NUMERICAL_INDICATOR_DIRECTION_LOW_VALUES_INDICATE = "<";

  /**
   * The data rows of the "Data Headers" worksheet. Order of enum values is
   * significant, as we use the ordinal() method.
   */
  public enum DataHeaderRow {
    COLUMN_IN_DATA_WORKSHEET("\"Data\" Worksheet Column", 0),
    NAME("Name", 1),
    DESCRIPTION("Description", 2),
    REPLICATE("Replicate Number", 3),
    TIME_POINT("Time point", 4),
    ASSAY_READOUT_TYPE("Assay readout type", 5),
    RAW_OR_DERIVED("Raw or Derived?", 6),
    HOW_DERIVED("If derived, how?", 7),
    COLUMNS_DERIVED_FROM("If derived, from which columns?", 8),
    IS_ASSAY_ACTIVITY_INDICATOR("Is it an Screening Positive Indicator? (yes/no)", 9),
    ACTIVITY_INDICATOR_TYPE("Screening Positive Indicator Type (numerical, partitioned (S/M/W), boolean)", 10),
    NUMERICAL_INDICATOR_DIRECTION("Numerical Screening Positive Indicator Cutoff Direction (< or >)", 11),
    NUMERICAL_INDICATOR_CUTOFF("Numerical Screening Positive Indicator Cutoff Value", 12),
    PRIMARY_OR_FOLLOWUP("Primary or Follow Up?", 13),
    ASSAY_PHENOTYPE("Which Assay Phenotype does it belong to?", 14),
    COMMENTS("Comments", 15),
    CHANNEL("Channel",16)
    ;


    private String _displayText;
    private Integer _rowIndex;

    private DataHeaderRow(String displayText,
                        Integer rowIndex)
    {
      _displayText = displayText;
      _rowIndex = rowIndex;
    }

    public String getDisplayText()
    {
      return _displayText;
    }

    public Integer getRowIndex()
    {
      return _rowIndex;
    }
  };

  public static final int METADATA_ROW_COUNT = DataHeaderRow.values().length;
  public static final String FILENAMES_LIST_DELIMITER = "\\s*,\\s*";

  /**
   * The standard columns of the "Data" worksheet. Order of enum values is
   * significant, as we use the ordinal() method.
   */
  public enum DataColumn {
    STOCK_PLATE_ID("Stock Plate ID"),
    WELL_NAME("Well"),
    ASSAY_WELL_TYPE("Type"),
    EXCLUDE("Exclude");

    private String _displayText;

    private DataColumn(String displayText)
    {
      _displayText = displayText;
    }

    public String getDisplayText()
    {
      return _displayText;
    }
  }

  /**
   * The row of the "Screen Info" worksheet.
   */
  public enum ScreenInfoRow {
    ID("ID"),
    TITLE("Screen Title"),
    SUMMARY("Screen Summary"),
    PI_LAB("PI/Lab"),
    LEAD_SCREENER("Lead Screener"),
    COLLABORATORS("Collaborators"),
    PUBMED_ID("Pubmed ID"),
    DATE_FIRST_LIBRARY_SCREENING("Date of First Library Screening"),
    EMAIL("Email"),
    LAB_AFFILIATION("Lab Affiliation"),
    ;

    private String _displayText;

    private ScreenInfoRow(String displayText)
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

  public static final String RAW_VALUE = "raw";
  public static final String DERIVED_VALUE = "derived";
  public static final String DERIVED_FROM_MISSING = "n/a";
  
  public static final String PRIMARY_VALUE = "primary";
  public static final String FOLLOWUP_VALUE = "follow up";

  public static final String EXCLUDE_ALL_VALUE = "all";

  // TODO: move to WellKey (or some central location)
  public static final String PLATE_NUMBER_REGEX = "(PL[-_])?(\\d+)(\\.0)?";
  public static final String WELL_NAME_REGEX = "[A-P]\\d\\d";

}

