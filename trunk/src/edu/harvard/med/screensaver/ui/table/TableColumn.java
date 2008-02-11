// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.convert.BigDecimalConverter;
import javax.faces.convert.BooleanConverter;
import javax.faces.convert.Converter;
import javax.faces.convert.DateTimeConverter;
import javax.faces.convert.DoubleConverter;
import javax.faces.convert.IntegerConverter;
import javax.faces.model.SelectItem;

import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.ui.table.Criterion.Operator;
import edu.harvard.med.screensaver.ui.util.NoOpStringConverter;

import org.apache.log4j.Logger;

/**
 *
 * @param R the row data type
 * @param T the column data type
 */
public abstract class TableColumn<R,T>
{
  // static members

  private static Logger log = Logger.getLogger(TableColumn.class);

  private static DateTimeConverter dateTimeConverter = new DateTimeConverter();
  static {
    dateTimeConverter.setDateStyle("short");
  }

  public enum ColumnType {
    TEXT(false, new NoOpStringConverter(), Operator.ALL_OPERATORS),
    INTEGER(true, new IntegerConverter(), Operator.COMPARABLE_OPERATORS),
    REAL(true, new DoubleConverter(), Operator.COMPARABLE_OPERATORS),
    FIXED_DECIMAL(true, new BigDecimalConverter(), Operator.COMPARABLE_OPERATORS),
    DATE(false, dateTimeConverter, Operator.COMPARABLE_OPERATORS),
    BOOLEAN(false, new BooleanConverter(), Operator.EQUALITY_OPERATORS),
    VOCABULARY(false, null, Operator.COMPARABLE_OPERATORS);

    private Converter _converter = NoOpStringConverter.getInstance();
    private boolean _isNumeric;
    private List<Operator> _validOperators;
    private List<SelectItem> _operatorSelections = new ArrayList<SelectItem>();

    private ColumnType(boolean isNumeric,
                       Converter converter,
                       List<Operator> validOperators)
    {
      _isNumeric = isNumeric;
      _converter = converter;
      _validOperators = validOperators;
      for (Operator operator : validOperators) {
        _operatorSelections.add(new SelectItem(operator, operator.getSymbol()));
      }
    }

    /**
     * @motivation for JSF EL expression usage
     */
    public String getName()
    {
      return toString();
    }

    public boolean isNumeric()
    {
      return _isNumeric;
    }

    public Converter getConverter()
    {
      return _converter;
    }

    public List<Operator> getValidOperators()
    {
      return _validOperators;
    }

    public List<SelectItem> getOperatorSelections()
    {
      return _operatorSelections;
    }
  };


  // instance data members

  private Map<SortDirection,Comparator<R>> _comparators = new HashMap<SortDirection,Comparator<R>>();
  private ColumnType _columnType;
  private String _name;
  private String _description;
  private boolean _isNumeric;
  private Criterion _criterion = new Criterion<T>();
  private Converter _converter = NoOpStringConverter.getInstance();


  // public constructors and methods

  public TableColumn()
  {
  }

  public TableColumn(String name, String description, ColumnType columnType)
  {
    _columnType = columnType;
    _name = name;
    _description = description;
    _isNumeric = columnType.isNumeric();
    _converter = columnType.getConverter();
  }

  public ColumnType getColumnType() { return _columnType; }

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

  public Converter getConverter()
  {
    return _converter;
  }

  public void setConverter(Converter converter)
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
      _comparators.put(sortDirection,
                       new Comparator<R>() {
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
        return v1.toString().compareTo(v2.toString());
      }
    };
  }

  public Criterion getCriterion()
  {
    return _criterion;
  }

  public void setCriterion(Criterion criterion)
  {
    _criterion = criterion;
  }

  /**
   * Returns whether the specified entity matches the criterion for the column.
   * @param e the entity to be matched
   * @return boolean
   */
  public boolean matches(R e)
  {
    return getCriterion().matches(getCellValue(e));
  }

  /**
   * Get the value to be displayed for the current column and cell.
   *
   * @param entity the entity displayed in the current cell (the row index)
   * @return the value to be displayed for the current cell
   */
  abstract public T getCellValue(R entity);

  /**
   * Set the new value of the entity for the current column and cell.
   *
   * @param entity the entity displayed in the current cell (the row index)
   * @param value the new value
   */
  public void setCellValue(R entity, Object value) {}

  /**
   * Get whether this table column is editable by the user. If it is, you must
   * implement {@link #setCellValue(R, T)}, if submitted values are
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
   * #getCellValue(R)} returns an array of values, and {@link #cellAction(R)} is
   * called with a <code>commandValue</code> parameter equal to the results of
   * {@link #getCellValue(R)}.
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
  public Object cellAction(R entity) { return null; }

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
   * #getCellValue(R)} returns an array of values, and {@link #cellAction(R)} is
   * called with a <code>commandValue</code> parameter equal to the results of
   * {@link #getCellValue(R)}.
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
