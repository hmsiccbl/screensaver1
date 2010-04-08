// $HeadURL:
// svn+ssh://ant4@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/ui/table/TableColumn.java
// $
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table.column;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.faces.convert.Converter;

import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.ui.table.ColumnVisibilityChangedEvent;
import edu.harvard.med.screensaver.ui.table.Criterion;
import edu.harvard.med.screensaver.ui.util.NoOpStringConverter;

import org.apache.log4j.Logger;

/**
 * @param R the row type
 * @param T the column data type
 */
public abstract class TableColumn<R,T> extends Observable implements Observer
{
  // static members

  private static Logger log = Logger.getLogger(TableColumn.class);

  public static final String UNGROUPED = "";


  // instance data members

  private Map<SortDirection,Comparator<R>> _comparators = new HashMap<SortDirection,Comparator<R>>();
  private ColumnType _columnType;
  private String _name;
  private String _description;
  private String _group;
  private boolean _isAdministrative;
  private boolean _isVisible;
  private boolean _isNumeric;
  private boolean _isMultiValued;
  private List<Criterion<T>> _criteria = new ArrayList<Criterion<T>>();
  private Converter _converter = NoOpStringConverter.getInstance();



  // public constructors and methods

  public TableColumn(String name,
                     String description,
                     ColumnType columnType,
                     String group)
  {
    this(name, description, columnType, group, false);
  }

  public TableColumn(String name,
                     String description,
                     ColumnType columnType,
                     String group,
                     boolean isMultiValued)
  {
    _name = name;
    _description = description;
    _columnType = columnType;
    _group = group;
    _isNumeric = columnType.isNumeric();
    _converter = columnType.getConverter();
    _isVisible = true;
    _isMultiValued = isMultiValued;
    addCriterion(new Criterion<T>(_columnType.getDefaultOperator(), null));
  }

  public ColumnType getColumnType()
  {
    return _columnType;
  }

  /**
   * Get the name of the column.
   *
   * @return the name of the column
   */
  public String getName()
  {
    return _name;
  }

  /**
   * Get the descriptive text for the column. Used for mouse-over quick-help.
   *
   * @return the descriptive text for the column
   */
  public String getDescription()
  {
    return _description;
  }

  public Converter getConverter()
  {
    return _converter;
  }

  protected void setConverter(Converter converter)
  {
    _converter = converter;
  }

  /**
   * Get a comparator for sorting the column for the specified sortDirection and
   * that is null-safe.
   *
   * @return a comparator for sorting the column
   */
  final public Comparator<R> getComparator(final SortDirection sortDirection)
  {
    if (_comparators.get(sortDirection) == null) {
      _comparators.put(sortDirection, new Comparator<R>() {
        public int compare(R o1, R o2)
        {
          int result = getAscendingComparator().compare(o1, o2);
          if (sortDirection.equals(SortDirection.DESCENDING)) {
            result *= -1;
          }
          return result;
        }
      });
    }
    return _comparators.get(sortDirection);
  }

  /**
   * Get a comparator for sorting the column that sorts its values in ascending
   * order. It is acceptable if the implementation instantiates a new Comparator
   * on each call, since TableColumn will only call this method once for a given
   * instance.
   *
   * @return a comparator for sorting the column
   */
  protected Comparator<R> getAscendingComparator()
  {
    return new Comparator<R>() {
      @SuppressWarnings("unchecked")
      public int compare(R o1, R o2)
      {
        Object v1 = getCellValue(o1);
        Object v2 = getCellValue(o2);
        if (v1 == null) {
          if (v2 == null) {
            return 0;
          }
          return -1;
        }
        if (v2 == null) {
          return 1;
        }
        if (v1 instanceof Comparable) {
          return ((Comparable) v1).compareTo(v2);
        }
        return v1.toString()
                 .compareTo(v2.toString());
      }
    };
  }

  public List<Criterion<T>> getCriteria()
  {
    return _criteria;
  }

  public Criterion<T> getCriterion()
  {
    if (_criteria.size() > 0) {
      return _criteria.get(0);
    }
    return null;
  }

  public void update(Observable o, Object arg)
  {
    // notify observers that a criterion in this column has changed
    setChanged();
    notifyObservers(o);
  }

  /**
   * @return the TableColumn, for chaining additional {@link #addCriterion(Criterion)} call
   */
  public TableColumn<R,T> addCriterion(Criterion<T> criterion)
  {
    _criteria.add(criterion);
    criterion.addObserver(this);
    // notify observers that a criterion has been added to this column
    setChanged();
    notifyObservers(criterion);
    return this;
  }

  public void removeCriterion(Criterion<T> criterion)
  {
    criterion.deleteObserver(this);
    boolean removed = _criteria.remove(criterion);
    if (removed) {
      // notify observers that a criterion has been removed from this column
      setChanged();
      notifyObservers(criterion); // TODO: should indicate that it was removed
    }
  }

  /**
   * Remove all filtering criteria from this column.
   * @return the TableColumn, for chaining a {@link #addCriterion(Criterion)} call
   */
  public TableColumn<R,T> clearCriteria()
  {
    for (Iterator<Criterion<T>> iter = _criteria.iterator(); iter.hasNext();) {
      // note: we can't just call removeCriterion(), as this would cause a
      // ConcurrentModificationException on _criteria.remove()
      Criterion<T> criterion = (Criterion<T>) iter.next();
      criterion.deleteObserver(this);
      iter.remove();
      setChanged();
      notifyObservers(criterion);
    }
    return this;
  }

  /**
   * Replace all existing filtering criteria with the single, default criterion.
   * @return the cleared Criterion, for chaining Criterion mutator method calls
   */
  public Criterion<T> resetCriteria()
  {
    clearCriteria();
    addCriterion(new Criterion<T>(getColumnType().getDefaultOperator(), null));
    return getCriterion();
  }

  /**
   * @return true if has at least 1 defined criterion
   */
  public boolean hasCriteria()
  {
    for (Criterion criterion : _criteria) {
      if (!criterion.isUndefined()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Get the value to be displayed for the current column and cell.
   *
   * @param row the row displayed in the current cell (the row index)
   * @return the value to be displayed for the current cell
   */
  abstract public T getCellValue(R row);

  /**
   * Set the new value of the row for the current column and cell.
   *
   * @param row the row displayed in the current cell (the row index)
   * @param value the new value
   */
  public void setCellValue(R row, Object value) {}

  /**
   * Get whether this table column is editable by the user. If it is, you must
   * implement {@link #setCellValue(Object, Object)}, if submitted values are to
   * update your data model. Also, {@link #isCommandLink()} should return false
   * if this method returns true.
   */
  public boolean isEditable()
  {
    return false;
  }

  final public void setVisible(boolean isVisible)
  {
    if (isVisible != _isVisible) {
      _isVisible = isVisible;
      setChanged();
      ColumnVisibilityChangedEvent event = new ColumnVisibilityChangedEvent();
      if (isVisible) {
        event.added(this);
      }
      else {
        event.removed(this);
      }
      notifyObservers(event);
    }
  }

  final public boolean isVisible()
  {
    return _isVisible;
  }
  
  /**
   * Return true whenever the cell values for the column with the specified name
   * should be a hyperlink.
   *
   * @return true whenever the cell values for the column should be a hyperlink.
   */
  public boolean isCommandLink()
  {
    return false;
  }

  /**
   * Perform the action for clicking on the current cell. Return the navigation
   * rule to go along with the action for clicking on the current cell. This
   * method is only called when {@link #isCommandLink()} is true.
   *
   * @param row the row displayed in the current cell (the row index)
   * @return the navigation rule to go along with the action for clicking on the
   *         current cell
   */
  public Object cellAction(R row)
  {
    return null;
  }

  /**
   * JavaBean property wrapper for {@link #isCommandLink()}.
   *
   * @motivation isCommandLink() translates to just 'commandLink' JavaBean
   *             property name, which is not as nice as 'isCommandLink'
   */
  final public boolean getIsCommandLink()
  {
    return isCommandLink();
  }

  public boolean isNumeric()
  {
    return _isNumeric;
  }

//  @Override
//  public boolean equals(Object o)
//  {
//    if (this == o) {
//      return true;
//    }
//    if (o instanceof TableColumn) {
//      if (getName().equals(((TableColumn) o).getName())) {
//        return true;
//      }
//    }
//    return false;
//  }
//
//  @Override
//  public int hashCode()
//  {
//    return getName().hashCode();
//  }

  public boolean isMultiValued()
  {
    return _isMultiValued;
  }

  protected void setMultiValued(boolean multiValued)
  {
    _isMultiValued = multiValued;
  }

  @Override
  public String toString()
  {
    return "TableColumn:" + getName();
  }

  public String getGroup()
  {
    return _group;
  }

  public boolean isAdministrative()
  {
    return _isAdministrative;
  }

  public void setAdministrative(boolean isAdministrative)
  {
    _isAdministrative = isAdministrative;
  }

  public boolean isSortableSearchable()
  {
    return true;
  }
}
