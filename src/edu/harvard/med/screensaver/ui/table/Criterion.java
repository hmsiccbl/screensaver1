// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import javax.faces.model.SelectItem;

import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.util.NullSafeUtils;

import org.apache.log4j.Logger;

/**
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class Criterion<T> extends Observable
{
  // static members

  private static Logger log = Logger.getLogger(Criterion.class);

  public enum Operator {
    ANY(""),
    EMPTY("{}"),
    NOT_EMPTY("{*}"),
    EQUAL("="),
    NOT_EQUAL("!="),
    // operators for Comparable objects
    LESS_THAN("<"),
    LESS_THAN_EQUAL("<="),
    GREATER_THAN(">"),
    GREATER_THAN_EQUAL(">="),
    // operators for String objects
    STARTS_WITH("^"),
    CONTAINS("*"),
    NOT_CONTAINS("!*"),
    LIKE("~"),
    NOT_LIKE("!~");

    public static List<Operator> ALL_OPERATORS = new ArrayList<Operator>();
    public static List<SelectItem> ALL_OPERATOR_SELECTIONS = new ArrayList<SelectItem>();
    static {
      for (Operator operator : Operator.values()) {
        ALL_OPERATORS.add(operator);
        ALL_OPERATOR_SELECTIONS.add(new SelectItem(operator, operator.getSymbol()));
      }
    }

    private String _symbol;

    private Operator(String symbol)
    {
      _symbol = symbol;
    }

    public String getSymbol()
    {
      return _symbol;
    }

    public String getName() { return name(); }
  }


  // instance data members

  private Operator _operator = Operator.EQUAL;
  private T _value;
  private String _regex;


  // public constructors and methods

  public Criterion()
  {
  }

  /**
   * @motivation for JSF EL expressions
   * @return
   */
  public List<SelectItem> getOperatorSelections()
  {
    return Operator.ALL_OPERATOR_SELECTIONS;
  }

  public Operator getOperator()
  {
    return _operator;
  }

  public void setOperator(Operator operator)
  {
    if (NullSafeUtils.nullSafeEquals(_operator, operator)) {
      return;
    }
    _operator = operator;
    _regex = null;
    if (_operator == Operator.EMPTY) {
      _value = null;
    }
    setChanged();
    notifyObservers();
  }

  public T getValue()
  {
    return _value;
  }

  public void setValue(T value)
  {
    if (NullSafeUtils.nullSafeEquals(_value, value)) {
      return;
    }
    // to reduce user-confusion, we'll treat empty strings as undefined criterion
    if (value instanceof String) {
      if (value == "") {
        value = null;
      }
    }
    _value = value;
    _regex = null;

    setChanged();
    notifyObservers();
  }

  @UIControllerMethod
  public void reset()
  {
    //_operator = null;
    setValue(null);
  }

  /**
   * An undefined criterion always matches (does not filter out any data).
   * @return
   */
  public boolean isUndefined()
  {
    return _operator == null || (_operator != Operator.EMPTY && _value == null);
  }

  @SuppressWarnings("unchecked")
  public boolean matches(Object inputValue)
  {
    Object criterionValue = getValue();
    Operator operator = getOperator();

    boolean result;
    if (isUndefined()) {
      // non-initialized criterion value means "do not filter" (criterion is undefined by user;
      // operator negation is not considered
      return true;
    }
    if (operator == Operator.ANY) {
      return true;
    }
    if (operator == Operator.EMPTY || operator == Operator.NOT_EMPTY) {
      result = inputValue == null || inputValue.toString().length() == 0;
      if (operator == Operator.NOT_EMPTY) {
        result = !result;
      }
    }
    else if (inputValue == null) {
      // null data value can only match with the EMPTY operator;
      // operator negation is not considered
      return false;
    }
    else if (operator == Operator.EQUAL || operator == Operator.NOT_EQUAL) {
      result = NullSafeUtils.nullSafeEquals(criterionValue, inputValue);
      if (operator == Operator.NOT_EQUAL) {
        result = !result;
      }
    }
    else if (operator == Operator.GREATER_THAN || operator == Operator.LESS_THAN_EQUAL ||
      operator == Operator.LESS_THAN || operator == Operator.GREATER_THAN_EQUAL) {
      if (! (inputValue instanceof Comparable)) {
        throw new CriterionMatchException("expecting Comparable value", this);
      }
      int cmpResult = ((Comparable) inputValue).compareTo(criterionValue);
      result = operator == Operator.GREATER_THAN ? cmpResult > 0 :
        operator == Operator.LESS_THAN ? cmpResult < 0 :
          operator == Operator.GREATER_THAN_EQUAL ? cmpResult >= 0 :
            cmpResult <= 0;
    }
    else if (operator == Operator.LIKE || operator == Operator.NOT_LIKE ||
      operator == Operator.STARTS_WITH ||
      operator == Operator.CONTAINS || operator == Operator.NOT_CONTAINS) {
      if (! (criterionValue instanceof String)) {
        throw new CriterionMatchException("expecting String criterion value", this);
      }
      result = ((String) inputValue).matches(getRegex());
      if (operator == Operator.NOT_LIKE || operator == Operator.NOT_CONTAINS) {
        result = !result;
      }
    }
    else {
      throw new CriterionMatchException(operator + " not supported", this);
    }
    return result;
  }

  // TODO: handle escape sequences '\*' and '\?'
  private String getRegex()
  {
    if (_regex == null) {
      assert _value != null;
      if (_operator == Operator.STARTS_WITH) {
        _regex = _value + ".*";
      }
      else if (_operator == Operator.LIKE || _operator == Operator.NOT_LIKE) {
        _regex = ((String) _value).replaceAll("\\?", ".").replaceAll("\\*", ".*");
      }
      else if (_operator == Operator.CONTAINS || _operator == Operator.NOT_CONTAINS) {
        _regex = ".*" + _value + ".*";
      }
      _regex = "(?i)(?s)" + _regex; // case insensitive, single-line mode (i.e., match across all lines)
      log.debug("regex=" + _regex);
    }
    return _regex;
  }
}
