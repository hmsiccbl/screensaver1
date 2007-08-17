// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.model.AbstractEntity;

import org.apache.log4j.Logger;

public abstract class TableColumn<E>
{
  // static members

  private static Logger log = Logger.getLogger(TableColumn.class);

  
  // instance data members

  private Map<SortDirection,Comparator<E>> _comparators = new HashMap<SortDirection,Comparator<E>>();
  private String _name;
  private String _description;
  private boolean _isNumeric;


  // public constructors and methods
  
  public TableColumn()
  {
  }
  
  public TableColumn(String name, String description)
  {
    _name = name;
    _description = description;
  }
  
  public TableColumn(String name, String description, boolean isNumeric)
  {
    _name = name;
    _description = description;
    _isNumeric = isNumeric;
  }
  
  /**
   * Get the name of the column.
   * 
   * @return the name of the column
   */
  public String getName() { return _name; }
  
  /**
   * Get the descriptive text for the column. Used for mouse-over quick-help.
   * 
   * @return the descriptive text for the column
   */
  public String getDescription() { return _description; }

  /**
   * Get a comparator for sorting the column for the specified sortDirection and
   * that is null-safe.
   * 
   * @return a comparator for sorting the column
   */
  final public Comparator<E> getComparator(final SortDirection sortDirection)
  {
    if (_comparators.get(sortDirection) == null) {
      _comparators.put(sortDirection, 
                       new Comparator<E>() {
        public int compare(E o1, E o2)
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
  protected Comparator<E> getAscendingComparator()
  {
    return new Comparator<E>() {
      @SuppressWarnings("unchecked")
      public int compare(E o1, E o2)
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
        return v1.toString().compareTo(v2.toString());
      }
    };
  }

  /**
   * Get the value to be displayed for the current column and cell.
   * 
   * @param entity the entity displayed in the current cell (the row index)
   * @return the value to be displayed for the current cell
   */
  abstract public Object getCellValue(E entity);
  
  /**
   * Set the new value of the entity for the current column and cell.
   * 
   * @param entity the entity displayed in the current cell (the row index)
   * @param value the new value
   */
  public void setCellValue(E entity, Object value) {}
  
  /**
   * Get whether this table column is editable by the user. If it is, you must
   * implement {@link #setCellValue(Object, Object)}, if submitted values are
   * to update your data model. Also, {@link #isCommandLink()} and
   * {@link #isCommandLinkList()} should be return false if this method return
   * true.
   */
  public boolean isEditable() { return false; }
  
  public boolean isVisible() { return true; }
  
  /**
   * Return true whenever the cell values for the column with the specified name
   * should be a hyperlink.
   * 
   * @return true whenever the cell values for the column should be a hyperlink.
   */
  public boolean isCommandLink() { return false; }
  
  /**
   * Return true whenever the cell values for the column should be a
   * semicolon-separated list of hyperlinks. In this situation, {@link
   * #getCellValue(Object)} returns an array of values, and {@link #cellAction(Object)} is
   * called with a <code>commandValue</code> parameter equal to the results of
   * {@link #getCellValue(Object)}.
   * 
   * @return true whenever the cell values for the column should be a list of
   *         hyperlinks.
   */
  public boolean isCommandLinkList() { return false; }
  
  /**
   * Perform the action for clicking on the current cell. Return the navigation rule to go
   * along with the action for clicking on the current cell. This method is only called when
   * {@link #isCommandLink()} is true.
   * 
   * @param entity the entity displayed in the current cell (the row index)
   * @return the navigation rule to go along with the action for clicking on the current cell 
   */
  public Object cellAction(E entity) { return null; }
  
  /**
   * Return true whenever the cell values for the column should be a hyperlink.
   * @motivation isCommandLink() translates to just 'commandLink' JavaBean property name, which is not as nice as 'isCommandLink'
   * @see #isCommandLink()
   * @return true whenever the cell values for the column should be a hyperlink
   */
  final public boolean getIsCommandLink()
  {
    return isCommandLink();
  }
  
  /**
   * Return true whenever the cell values for the column should be a
   * semicolon-separated list of hyperlinks. In this situation, {@link
   * #getCellValue(Object)} returns an array of values, and {@link #cellAction(Object)} is
   * called with a <code>commandValue</code> parameter equal to the results of
   * {@link #getCellValue(Object)}.
   * 
   * @motivation isCommandLinkList() translates to just 'commandLinkList'
   *             JavaBean property name, which is not as nice as
   *             'isCommandLinkList'
   * @see #isCommandLinkList()
   * @return true whenever the cell values for the column should be a list of
   *         hyperlinks
   */
  final public boolean getIsCommandLinkList()
  {
    return isCommandLinkList();
  }

  public boolean isNumeric()
  {
    return _isNumeric;
  }
  
  @Override
  public boolean equals(Object o)
  {
    if (this == o) {
      return true;
    }
    if (o instanceof TableColumn) {
      if (getName().equals(((TableColumn) o).getName())) {
        return true;
      }
    }
    return false;
  }
  
  @Override
  public int hashCode()
  {
    return getName().hashCode();
  }
  
  @Override
  public String toString()
  {
    return "TableColumn:" + getName();
  }

  // private methods

}
