// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screens;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.validator.ValidatorException;

import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellName;

import org.apache.commons.lang.StringUtils;

public class EmptyWellsConverter implements Converter
{
  private static final String DELIMITER = ", ";
  // static members

  private static Pattern rowPattern = Pattern.compile("Row:([A-P])");
  private static Pattern colPattern = Pattern.compile("Col:(\\d{1,2})");

  public Object getAsObject(FacesContext context,
                            UIComponent component,
                            String value) throws ConverterException
  {
    if (value == null) {
      return null;
    }
    return parse(value);
  }

  public String getAsString(FacesContext context,
                            UIComponent component,
                            Object value) throws ConverterException
  {
    if (value == null) {
      return "";
    }
    Set<WellName> wellNames = new TreeSet<WellName>();
    wellNames.addAll((Set<WellName>) value);
    StringBuilder s = new StringBuilder();
    Set<WellName> toRemove = new HashSet<WellName>();
    for (Integer fullColumnName : getFullColumnNames(wellNames)) {
      if (s.length() > 0) { s.append(DELIMITER); }
      s.append("Col:").append(String.format("%02d", fullColumnName));
      toRemove.addAll(makeFullColumn(fullColumnName));
    }
    for (char fullRowName : getFullRowNames(wellNames)) {
      if (s.length() > 0) { s.append(DELIMITER); }
      s.append("Row:").append(fullRowName);
      toRemove.addAll(makeFullRow(fullRowName));
    }
    wellNames.removeAll(toRemove);
    if (s.length() > 0 && wellNames.size() > 0) { s.append(DELIMITER); }
    s.append(StringUtils.join(wellNames.iterator(), DELIMITER));
    return s.toString();
  }

  public static List<WellName> makeFullRow(char rowName)
  {
    List<WellName> row = new ArrayList<WellName>();
    for (int colName = Well.MIN_WELL_COLUMN; colName <= Well.MAX_WELL_COLUMN; colName++) {
      row.add(new WellName(rowName, colName));
    }
    return row;
  }

  public static List<WellName> makeFullColumn(int colName)
  {
    List<WellName> col = new ArrayList<WellName>();
    for (char rowName = Well.MIN_WELL_ROW; rowName <= Well.MAX_WELL_ROW; rowName++) {
      col.add(new WellName(rowName, colName));
    }
    return col;
  }

  private Object parse(String value) throws ValidatorException
  {
    Set<WellName> wellNames = new HashSet<WellName>();
    String[] tokens = value.split("\\s|,");
    for (String token: tokens) {
      if (token.length() == 0) {
        continue;
      }
      Matcher rowMatcher = rowPattern.matcher(token);
      if (rowMatcher.matches()) {
        String row = rowMatcher.group(1);
        wellNames.addAll(makeFullRow(row.charAt(0)));
        continue;
      }
      Matcher colMatcher = colPattern.matcher(token);
      if (colMatcher.matches()) {
        String col = colMatcher.group(1);
        wellNames.addAll(makeFullColumn(Integer.parseInt(col)));
        continue;
      }
      Matcher wellNameMatcher = Well._wellParsePattern.matcher(token);
      if (wellNameMatcher.matches()) {
        wellNames.add(new WellName(token));
        continue;
      }
      throw new ConverterException("illegal token '" + token + "' in empty wells string '" + value + "'");
    }
    return wellNames;
  }

  private Set<Character> getFullRowNames(Set<WellName> wellNames)
  {
    int[] rowCounts = new int[Well.PLATE_ROWS];
    Arrays.fill(rowCounts, 0, rowCounts.length, 0);
    for (WellName wellName : wellNames) {
      rowCounts[wellName.getRowIndex()]++;
    }
    SortedSet<Character> fullRowNames = new TreeSet<Character>();
    for (int rowIndex = 0; rowIndex < rowCounts.length; ++rowIndex) {
      if (rowCounts[rowIndex] == Well.PLATE_COLUMNS) {
        fullRowNames.add(Character.valueOf((char) (Well.MIN_WELL_ROW + rowIndex)));
      }
    }
    return fullRowNames;
  }

  private SortedSet<Integer> getFullColumnNames(Set<WellName> wellNames)
  {
    int[] columnCounts = new int[Well.PLATE_COLUMNS];
    Arrays.fill(columnCounts, 0, columnCounts.length, 0);
    for (WellName wellName : wellNames) {
      columnCounts[wellName.getColumnIndex()]++;
    }
    SortedSet<Integer> fullColumnNames = new TreeSet<Integer>();
    for (int colIndex = 0; colIndex < columnCounts.length; ++colIndex) {
      if (columnCounts[colIndex] == Well.PLATE_ROWS) {
        fullColumnNames.add(Well.MIN_WELL_COLUMN + colIndex);
      }
    }
    return fullColumnNames;
  }

}
