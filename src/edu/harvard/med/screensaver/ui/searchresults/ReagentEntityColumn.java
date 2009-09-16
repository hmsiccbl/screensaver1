package edu.harvard.med.screensaver.ui.searchresults;

import java.util.Iterator;
import java.util.Set;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.FetchPaths;
import edu.harvard.med.screensaver.ui.table.column.entity.HasFetchPaths;

/**
 * Responsibilities:
 * <ul>
 * <li>adds the null and type-safety checks needed when dereferencing
 * {@link Well#getReagent()}</li>
 * <li>adds the eager fetching ofWell.Library.LibraryContentsVersion, which is needed by the call</li>
 * <li>performs data access restriction checks on Reagent</li>
 * </ul>
 */
abstract class ReagentEntityColumn<F extends AbstractEntity, R extends Reagent,T> extends TableColumn<F,T> implements HasFetchPaths<F>
{
  private Class<R> _reagentClass;
  private TableColumn<R,T> _delegateEntityColumn;
  private RelationshipPath<F> _toWellPath;
  private HasFetchPaths<F> _fetchPaths;
  
  abstract protected R getReagent(F fromEntity);

  @Override
  public T getCellValue(F fromEntity)
  {
    R reagent = getReagent(fromEntity);
    if (reagent != null) { 
      if (_reagentClass.isAssignableFrom(reagent.getEntityClass())) {
        if (!!!reagent.isRestricted()) {
          return _delegateEntityColumn.getCellValue(reagent);
        }
      }
    }
    return null;
  }

  public ReagentEntityColumn(Class<R> reagentClass, 
                             RelationshipPath<F> toReagentPath,
                             TableColumn<R,T> delegateEntityColumn)
  {
    super(delegateEntityColumn.getName(),
          delegateEntityColumn.getDescription(),
          delegateEntityColumn.getColumnType(),
          delegateEntityColumn.getGroup());
    _reagentClass = reagentClass;
    _delegateEntityColumn = delegateEntityColumn;
    PropertyPath<R> propPath = ((HasFetchPaths<R>) delegateEntityColumn).getPropertyPath();
    Iterator<RelationshipPath<R>> iter = ((HasFetchPaths<R>) delegateEntityColumn).getRelationshipPaths().iterator(); 
    if (propPath != null) {
      _fetchPaths = new FetchPaths<F>(toReagentPath.to(propPath));
    }
    else {
      _fetchPaths = new FetchPaths<F>(toReagentPath.to(iter.next()));
    }
    while (iter.hasNext()) {
      _fetchPaths.addRelationshipPath(toReagentPath.to(iter.next()));
    }
  }
  
  @Override
  public Object cellAction(F fromEntity)
  {
    R reagent = getReagent(fromEntity);
    if (reagent != null) { 
      return _delegateEntityColumn.cellAction(reagent);
    }
    return null;
  }
  
  @Override
  public boolean isCommandLink()
  {
    return _delegateEntityColumn.isCommandLink();
  }

  @Override
  public void setCellValue(F fromEntity, Object value)
  {
    if (getReagent(fromEntity) != null) {
      _delegateEntityColumn.setCellValue(getReagent(fromEntity), value);
    }
  }

  public void addRelationshipPath(RelationshipPath<F> path)
  {
    _fetchPaths.addRelationshipPath(path);
  }
  
  public PropertyPath<F> getPropertyPath()
  {
    return _fetchPaths.getPropertyPath();
  }
  
  public Set<RelationshipPath<F>> getRelationshipPaths()
  {
    return _fetchPaths.getRelationshipPaths();
  }
}