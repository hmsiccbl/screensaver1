// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.cherrypickrequests;

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

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellName;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Sets;

// TODO: design so this is independent of default well plate size
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
    for (Integer fullColumn : getFullColumns(wellNames)) {
      if (s.length() > 0) { s.append(DELIMITER); }
      s.append("Col:").append(WellName.getColumnLabel(fullColumn));
      toRemove.addAll(makeFullColumn(fullColumn));
    }
    for (Integer fullRow : getFullRows(wellNames)) {
      if (s.length() > 0) { s.append(DELIMITER); }
      s.append("Row:").append(WellName.getRowLabel(fullRow));
      toRemove.addAll(makeFullRow(fullRow));
    }
    wellNames.removeAll(toRemove);
    if (s.length() > 0 && wellNames.size() > 0) { s.append(DELIMITER); }
    s.append(StringUtils.join(wellNames.iterator(), DELIMITER));
    return s.toString();
  }

  public static List<WellName> makeFullRow(int row)
  {
    List<WellName> rowWellNames = new ArrayList<WellName>();
    for (int col = 0; col < ScreensaverConstants.DEFAULT_PLATE_SIZE.getColumns(); ++col) {
      rowWellNames.add(new WellName(row, col));
    }
    return rowWellNames;
  }

  public static List<WellName> makeFullColumn(int col)
  {
    List<WellName> colWellNames = new ArrayList<WellName>();
    for (int row = 0; row < ScreensaverConstants.DEFAULT_PLATE_SIZE.getRows(); ++row) {
      colWellNames.add(new WellName(row, col));
    }
    return colWellNames;
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
        wellNames.addAll(makeFullRow(WellName.parseRowLabel(rowMatcher.group(1))));
        continue;
      }
      Matcher colMatcher = colPattern.matcher(token);
      if (colMatcher.matches()) {
        wellNames.addAll(makeFullColumn(WellName.parseColumnLabel(colMatcher.group(1))));
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

  private Set<Integer> getFullRows(Set<WellName> wellNames)
  {
    int[] rowCounts = new int[ScreensaverConstants.DEFAULT_PLATE_SIZE.getRows()];
    Arrays.fill(rowCounts, 0, rowCounts.length, 0);
    for (WellName wellName : wellNames) {
      rowCounts[wellName.getRowIndex()]++;
    }
    SortedSet<Integer> fullRowIndexes = Sets.newTreeSet();
    for (int rowIndex = 0; rowIndex < rowCounts.length; ++rowIndex) {
      if (rowCounts[rowIndex] == ScreensaverConstants.DEFAULT_PLATE_SIZE.getColumns()) {
        fullRowIndexes.add(rowIndex);
      }
    }
    return fullRowIndexes;
  }

  private SortedSet<Integer> getFullColumns(Set<WellName> wellNames)
  {
    int[] columnCounts = new int[ScreensaverConstants.DEFAULT_PLATE_SIZE.getColumns()];
    Arrays.fill(columnCounts, 0, columnCounts.length, 0);
    for (WellName wellName : wellNames) {
      columnCounts[wellName.getColumnIndex()]++;
    }
    SortedSet<Integer> fullColumnIndexes = Sets.newTreeSet();
    for (int colIndex = 0; colIndex < columnCounts.length; ++colIndex) {
      if (columnCounts[colIndex] == ScreensaverConstants.DEFAULT_PLATE_SIZE.getRows()) {
        fullColumnIndexes.add(colIndex);
      }
    }
    return fullColumnIndexes;
  }

}
