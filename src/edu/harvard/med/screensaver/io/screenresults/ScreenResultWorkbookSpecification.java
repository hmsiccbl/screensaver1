// $HeadURL: svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.settings/org.eclipse.jdt.ui.prefs $
// $Id: org.eclipse.jdt.ui.prefs 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.screenresults;


public interface ScreenResultWorkbookSpecification
{
  public static final String FIRST_DATE_SCREENED = "First Date Screened";
  public static final String SCREEN_INFO_SHEET_NAME = "Screen Info";
  public static final String METADATA_META_SHEET_NAME = "meta"; // legacy format only
  public static final String DATA_HEADERS_SHEET_NAME = "Data Headers";
  public static final String DATA_SHEET_NAME = "Data";
  public static final int LEGACY_DATA_HEADERS_SHEET_INDEX = 0;

  

  public static final String DATA_HEADER_COLUMN_TYPE = "data"; // for legacy format
  public static final short METADATA_FILENAMES_CELL_COLUMN_INDEX = 1;
  public static final int METADATA_FILENAMES_CELL_ROW_INDEX = 0; // legacy format only
  public static final int METADATA_FIRST_DATA_ROW_INDEX = 1;
  public static final int METADATA_FIRST_DATA_HEADER_COLUMN_INDEX = 1;
  public static final char METADATA_FIRST_DATA_HEADER_COLUMN_ID = 'A' + (char) METADATA_FIRST_DATA_HEADER_COLUMN_INDEX;
  public static final int METADATA_ROW_NAMES_COLUMN_INDEX = 0;
  public static final int RAWDATA_HEADER_ROW_INDEX = 0;
  public static final int RAWDATA_FIRST_DATA_ROW_INDEX = 1;
  public static final int RAWDATA_FIRST_DATA_HEADER_COLUMN_INDEX = DataColumn.values().length;
  public static final char RAWDATA_FIRST_DATA_HEADER_COLUMN_ID = (char) ('A' + RAWDATA_FIRST_DATA_HEADER_COLUMN_INDEX);
  public static final int SCREENINFO_ROW_HEADER_COLUMN_INDEX = 0;
  public static final int SCREENINFO_VALUE_COLUMN_INDEX = 1;
  public static final int SCREENINFO_FIRST_DATA_ROW_INDEX = 0;

  /**
   * The data rows of the "Data Headers" worksheet. Order of enum values is
   * significant, as we use the ordinal() method.
   */
  public enum MetadataRow { 
    COLUMN_IN_DATA_WORKSHEET("\"Data\" Worksheet Column"),
    COLUMN_TYPE("Type"), // for legacy format 
    NAME("Name"),
    DESCRIPTION("Description"),
    REPLICATE("Replicate Number"),
    TIME_POINT("Time point"),
    RAW_OR_DERIVED("Raw or Derived?"),
    HOW_DERIVED("If derived, how?"),
    COLUMNS_DERIVED_FROM("If derived, from which columns?"),
    IS_ASSAY_ACTIVITY_INDICATOR("Is it an Assay Activity Indicator? (yes/no)"),
    ACTIVITY_INDICATOR_TYPE("Assay Activity Indicator Type (numerical, partitioned (S/M/W), boolean)"),
    NUMERICAL_INDICATOR_DIRECTION("Assay Activity Indicator Numeric Cutoff Direction (< or >)"),
    NUMERICAL_INDICATOR_CUTOFF("Assay Activity Indicator Numeric Cutoff Value"),
    PRIMARY_OR_FOLLOWUP("Primary or Follow Up?"),
    ASSAY_PHENOTYPE("Which Assay Phenotype does it belong to?"),
    IS_CHERRY_PICK("Is it a cherry pick? (yes/no)"),
    COMMENTS("Comments"),
    ;
    
    private String _displayText;
    
    private MetadataRow(String displayText)
    { 
      _displayText = displayText;
    }
    
    public String getDisplayText()
    {
      return _displayText;
    }
  };
  
  public static final int METADATA_ROW_COUNT = MetadataRow.values().length;
  public static final String FILENAMES_LIST_DELIMITER = "\\s*,\\s*";

  /**
   * The standard columns of the "Data" worksheet. Order of enum values is
   * significant, as we use the ordinal() method.
   */
  public enum DataColumn {
    STOCK_PLATE_ID("Stock Plate ID"),
    WELL_NAME("Well"),
    TYPE("Type"),
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
    FIRST_DATE_SCREENED("First Date Screened"),
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

  public static final String PRIMARY_VALUE = "primary";
  public static final String FOLLOWUP_VALUE = "follow up";

  public static final String EXCLUDE_ALL_VALUE = "all";

  public static final String PLATE_NUMBER_REGEX = "(PL[-_])?(\\d+)(\\.0)?";
  public static final String PLATE_NUMBER_FORMAT = "PL-%05d";

}

