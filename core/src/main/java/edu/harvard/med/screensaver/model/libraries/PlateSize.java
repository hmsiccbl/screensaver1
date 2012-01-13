// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.harvard.med.screensaver.model.VocabularyTerm;
import edu.harvard.med.screensaver.model.VocabularyUserType;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;


public enum PlateSize implements VocabularyTerm
{
  WELLS_1(1, 1), // NOTE: this "vial" size is a LINCS-only feature
  WELLS_96(12, 8),
  WELLS_384(24, 16),
  WELLS_1536(48, 32);

  final static PlateSize MAX_PLATE_SIZE = WELLS_1536;

  private int _columns;
  private int _rows;
  private List<String> _rowLabels;
  private List<String> _columnLabels;

  /**
   * A Hibernate <code>UserType</code> to map the {@link PlateSize} vocabulary.
   */
  public static class UserType extends VocabularyUserType<PlateSize>
  {
    public UserType()
    {
      super(PlateSize.values());
    }
  }

  private PlateSize(int columns, int rows)
  {
    _columns = columns;
    _rows = rows;
    _columnLabels = new ArrayList<String>();
    for (int i = 0; i < _columns; i++) {
      _columnLabels.add(WellName.getColumnLabel(i));
    }
    _columnLabels = ImmutableList.copyOf(_columnLabels);
    _rowLabels = new ArrayList<String>();
    for (int i = 0; i < _rows; i++) {
      _rowLabels.add(WellName.getRowLabel(i));
    }
    _rowLabels = ImmutableList.copyOf(_rowLabels);

  }

  public List<String> getColumnsLabels()
  {
    return _columnLabels;
  }

  public List<String> getRowsLabels()
  {
    return _rowLabels;
  }

  public int getColumns()
  {
    return _columns;
  }

  public int getRows()
  {
    return _rows;
  }

  public int getWellCount()
  {
    return _columns * _rows;
  }

  public String getValue()
  {
    return Integer.toString(getWellCount());
  }

  @Override
  public String toString()
  {
    return getValue();
  }

  public Set<WellName> getEdgeWellNames(int n) 
  {
    Set<WellName> emptyWells = Sets.newTreeSet();
    for (int i = 0; i < n; ++i) {
      for (int row = 0; row < getRows(); ++row) {
        for (int col = 0; col < getColumns(); ++col) {
          if (row < n || row >= getRows() - n ||
            col < n || col >= getColumns() - n) {
            emptyWells.add(new WellName(row, col));
          }
        }
      }
    }
    return emptyWells;
  }
}
