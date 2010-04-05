package edu.harvard.med.screensaver.ui.searchresults;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.faces.model.SelectItem;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.meta.PropertyPath;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.ui.table.column.HasVocabulary;
import edu.harvard.med.screensaver.ui.table.column.TableColumn;
import edu.harvard.med.screensaver.ui.table.column.entity.FetchPaths;
import edu.harvard.med.screensaver.ui.table.column.entity.HasFetchPaths;

/**
 * Allows reuse of entity column definition from a related entity type. 
 * Responsibilities:
 * <ul>
 * <li>adds null and type-safety checks needed when dereferencing the related entity</li>
 * <li>performs data access restriction checks on the related entity</li>
 * </ul>
 */
public abstract class RelatedEntityColumn<F extends AbstractEntity, R extends AbstractEntity, T> extends TableColumn<F,T> implements HasFetchPaths<F>, HasVocabulary<T>
{
  private Class<R> _relatedEntityClass;
  private TableColumn<R,T> _delegateEntityColumn;
  private RelationshipPath<F> _toWellPath;
  private HasFetchPaths<F> _fetchPaths;
  
  abstract protected R getRelatedEntity(F fromEntity);

  @Override
  public T getCellValue(F fromEntity)
  {
    R relatedEntity = getRelatedEntity(fromEntity);
    if (relatedEntity != null) { 
      if (_relatedEntityClass.isAssignableFrom(relatedEntity.getEntityClass())) {
        if (!!!relatedEntity.isRestricted()) {
          return _delegateEntityColumn.getCellValue(relatedEntity);
        }
      }
    }
    return null;
  }

  public RelatedEntityColumn(Class<R> relatedEntityClass, 
                             RelationshipPath<F> toRelatedEntityPath,
                             TableColumn<R,T> delegateEntityColumn)
  {
    this(relatedEntityClass,
         toRelatedEntityPath,
         delegateEntityColumn,
         delegateEntityColumn.getGroup());
  }
  
  public RelatedEntityColumn(Class<R> relatedEntityClass, 
                             RelationshipPath<F> toRelatedEntityPath,
                             TableColumn<R,T> delegateEntityColumn,
                             String group)
  {
    super(delegateEntityColumn.getName(),
          delegateEntityColumn.getDescription(),
          delegateEntityColumn.getColumnType(),
          group);
    setAdministrative(delegateEntityColumn.isAdministrative());
    setConverter(delegateEntityColumn.getConverter());
    setMultiValued(delegateEntityColumn.isMultiValued());
    _relatedEntityClass = relatedEntityClass;
    _delegateEntityColumn = delegateEntityColumn;
    PropertyPath<R> propPath = ((HasFetchPaths<R>) delegateEntityColumn).getPropertyPath();
    Iterator<RelationshipPath<R>> iter = ((HasFetchPaths<R>) delegateEntityColumn).getRelationshipPaths().iterator(); 
    if (propPath != null) {
      _fetchPaths = new FetchPaths<F>(toRelatedEntityPath.to(propPath));
      iter.next();
    }
    else {
      _fetchPaths = new FetchPaths<F>(toRelatedEntityPath.to(iter.next()));
    }
    while (iter.hasNext()) {
      _fetchPaths.addRelationshipPath(toRelatedEntityPath.to(iter.next()));
    }
  }
  
  @Override
  public Object cellAction(F fromEntity)
  {
    R relatedEntity = getRelatedEntity(fromEntity);
    if (relatedEntity != null) { 
      return _delegateEntityColumn.cellAction(relatedEntity);
    }
    return null;
  }
  
  @Override
  public boolean isCommandLink()
  {
    return _delegateEntityColumn.isCommandLink();
  }
  
  @Override
  public boolean isSortableSearchable()
  {
    return _delegateEntityColumn.isSortableSearchable();
  }

  @Override
  public void setCellValue(F fromEntity, Object value)
  {
    if (getRelatedEntity(fromEntity) != null) {
      _delegateEntityColumn.setCellValue(getRelatedEntity(fromEntity), value);
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
  
  public Set<T> getVocabulary()
  {
    if (_delegateEntityColumn instanceof HasVocabulary) {
      return ((HasVocabulary<T>) _delegateEntityColumn).getVocabulary();
    }
    return Collections.emptySet();
  }
  
  public List<SelectItem> getVocabularySelections()
  {
    if (_delegateEntityColumn instanceof HasVocabulary) {
      return ((HasVocabulary<T>) _delegateEntityColumn).getVocabularySelections();
    }
    return Collections.emptyList();
  }
  
  public boolean isFetchableProperty()
  {
    return _fetchPaths.isFetchableProperty();
  }
}