// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screens;

import java.util.HashMap;
import java.util.Map;

import edu.harvard.med.screensaver.model.screens.CherryPickAssayPlate;

public class AssayPlateRow
{
  static final String[] ASSAY_PLATES_TABLE_COLUMNS = { "Plate Number", "Attempt", "Plate Name", "Status", "Date", "Performed By", "Comments" };
  private static final String[][] ASSAY_PLATES_TABLE_SECONDARY_SORTS = { {}, { "Plate Name" }, {}, { "Plate Name" }, { "Plate Name" }, { "Plate Name" }, { "Plate Name" } };

  private CherryPickAssayPlate _assayPlate;
  private Map<String,Comparable> _columnName2Value;
  private boolean _selected = true;

  public AssayPlateRow(CherryPickAssayPlate assayPlate)
  {
    _assayPlate = assayPlate;
    makeRowValues();
  }
  
  public boolean isSelected()
  {
    return _selected;
  }

  public void setSelected(boolean selected)
  {
    this._selected = selected;
  }

  public Map<String,Comparable> getColumnName2Value()
  {
    return _columnName2Value;
  }

  public void setColumnName2Value(Map<String,Comparable> values)
  {
    this._columnName2Value = values;
  }
  
  public CherryPickAssayPlate getAssayPlate()
  {
    return _assayPlate;
  }

  private void makeRowValues()
  {
    _columnName2Value = new HashMap<String,Comparable>();
    _columnName2Value.put(ASSAY_PLATES_TABLE_COLUMNS[0], 
                          _assayPlate.getPlateOrdinal() + 1);
    _columnName2Value.put(ASSAY_PLATES_TABLE_COLUMNS[1], 
                          _assayPlate.getAttemptOrdinal() + 1);
    _columnName2Value.put(ASSAY_PLATES_TABLE_COLUMNS[2], 
                  _assayPlate.getName());
    _columnName2Value.put(ASSAY_PLATES_TABLE_COLUMNS[3], _assayPlate.getStatusLabel());
    if (!_assayPlate.isFailed() && !_assayPlate.isPlated() && !_assayPlate.isCanceled()) {
      _columnName2Value.put(ASSAY_PLATES_TABLE_COLUMNS[4], "");
      _columnName2Value.put(ASSAY_PLATES_TABLE_COLUMNS[5], "");
    }
    else {
      if (_assayPlate.getCherryPickLiquidTransfer() != null) {
        _columnName2Value.put(ASSAY_PLATES_TABLE_COLUMNS[4], 
                              _assayPlate.getCherryPickLiquidTransfer().getDateOfActivity());
        _columnName2Value.put(ASSAY_PLATES_TABLE_COLUMNS[5], 
                              _assayPlate.getCherryPickLiquidTransfer().getPerformedBy().getFullNameLastFirst());
      }
    }
    _columnName2Value.put(ASSAY_PLATES_TABLE_COLUMNS[6], 
                          _assayPlate.getCherryPickLiquidTransfer() == null ? null :
                            _assayPlate.getCherryPickLiquidTransfer().getComments());
  }
}

