// $HeadURL$
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.faces.model.SelectItem;

import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screenresults.ResultValue;
import edu.harvard.med.screensaver.ui.arch.datatable.column.BooleanColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.HasVocabulary;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.HasFetchPaths;

public abstract class ViewPolicyAwareResultValueColumn<R,T extends Object> extends TableColumn<R,T> implements HasFetchPaths<ResultValue>, HasVocabulary<T>
{
  private TableColumn<R,T> _baseColumn;
  private DataColumn _dataColumn;

  public ViewPolicyAwareResultValueColumn(TableColumn<R,T> baseColumn,
                                          DataColumn dataColumn)
  {
    super(baseColumn.getName(), 
          baseColumn.getDescription(),
          baseColumn.getColumnType(), 
          baseColumn.getGroup(), 
          baseColumn.isMultiValued());
    setAdministrative(baseColumn.isAdministrative());
    setConverter(baseColumn.getConverter());
    setMultiValued(baseColumn.isMultiValued());
    _baseColumn = baseColumn;
    _dataColumn = dataColumn;
    initialize();
  }

  abstract protected void initialize();

  abstract protected boolean isResultValueRestricted(R row);

  public boolean isCommandLink()
  {
    return _baseColumn.isCommandLink();
  }

  public Object cellAction(R row)
  {
    return _baseColumn.cellAction(row);
  }

  public T getCellValue(R row)
  {
    if (isResultValueRestricted(row)) {
      return null;
    }
    return _baseColumn.getCellValue(row);
  }

  public void setCellValue(R row, T value)
  {
    _baseColumn.setCellValue(row, value);
  }

  public boolean isSortableSearchable()
  {
    // if related screen result is restricted, this means that we're
    // only allowed to share mutual positives; in this case we do not
    // allow sorting or filtering on this column, since it would allow
    // hidden values to be inferred by the user
    if (_dataColumn.getScreenResult().isRestricted()) {
      return false;
    }
    return _baseColumn.isSortableSearchable();
  }

  @Override
  public void addRelationshipPath(RelationshipPath<ResultValue> path)
  {
    if (!(_baseColumn instanceof HasFetchPaths)) {
      throw new UnsupportedOperationException("base column does not implement HasFetchPaths");
    }
    ((HasFetchPaths<ResultValue>) _baseColumn).addRelationshipPath(path);
  }

  @Override
  public PropertyPath<ResultValue> getPropertyPath()
  {
    if (!(_baseColumn instanceof HasFetchPaths)) {
      throw new UnsupportedOperationException("base column does not implement HasFetchPaths");
    }
    return ((HasFetchPaths<ResultValue>) _baseColumn).getPropertyPath();
  }

  @Override
  public Set<RelationshipPath<ResultValue>> getRelationshipPaths()
  {
    if (!(_baseColumn instanceof HasFetchPaths)) {
      throw new UnsupportedOperationException("base column does not implement HasFetchPaths");
    }
    return ((HasFetchPaths<ResultValue>) _baseColumn).getRelationshipPaths();
  }

  @Override
  public boolean isFetchableProperty()
  {
    if (!(_baseColumn instanceof HasFetchPaths)) {
      throw new UnsupportedOperationException("base column does not implement HasFetchPaths");
    }
    return ((HasFetchPaths) _baseColumn).isFetchableProperty();
  }

  public Set<T> getVocabulary()
  {
    if (_baseColumn instanceof HasVocabulary) {
      return ((HasVocabulary<T>) _baseColumn).getVocabulary();
    }
    return Collections.emptySet();
  }

  public List<SelectItem> getVocabularySelections()
  {
    if (_baseColumn instanceof HasVocabulary) {
      return ((HasVocabulary<T>) _baseColumn).getVocabularySelections();
    }
    return Collections.emptyList();
  }

  public List<SelectItem> getBooleanSelections()
  {
    if (_baseColumn instanceof BooleanColumn) {
      return ((BooleanColumn<R>) _baseColumn).getBooleanSelections();
    }
    return Collections.emptyList();
  }

}
