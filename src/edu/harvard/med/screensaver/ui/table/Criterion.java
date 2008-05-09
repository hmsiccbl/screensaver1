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
import java.util.Collection;
import java.util.List;
import java.util.Observable;

import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.util.NullSafeUtils;
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.log4j.Logger;

/**
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class Criterion<T> extends Observable
{
  // static members

  private static Logger log = Logger.getLogger(Criterion.class);

  public enum OperatorClass {
    EXTANT,
    EQUALITY,
    RANKING,
    TEXT
  };

  public enum Operator {
    ANY("", OperatorClass.EXTANT, 1),
    // equality operators
    EQUAL("=", OperatorClass.EQUALITY),
    NOT_EQUAL("<>", OperatorClass.EQUALITY),
    // ranking operators
    LESS_THAN("<", OperatorClass.RANKING),
    LESS_THAN_EQUAL("<=", OperatorClass.RANKING),
    GREATER_THAN(">", OperatorClass.RANKING),
    GREATER_THAN_EQUAL(">=", OperatorClass.RANKING),
    // text operators
    TEXT_STARTS_WITH("starts with", OperatorClass.TEXT),
    TEXT_CONTAINS("contains", OperatorClass.TEXT),
    TEXT_NOT_CONTAINS("doesn't contain", OperatorClass.TEXT),
    TEXT_LIKE("matches", OperatorClass.TEXT),
    TEXT_NOT_LIKE("doesn't match", OperatorClass.TEXT),
    // empty operators
    EMPTY("blank", OperatorClass.EXTANT, 1),
    NOT_EMPTY("not blank", OperatorClass.EXTANT, 1);

    public final static List<Operator> ALL_OPERATORS = new ArrayList<Operator>();
    public final static List<Operator> COMPARABLE_OPERATORS = new ArrayList<Operator>();
    public final static List<Operator> EQUALITY_OPERATORS = new ArrayList<Operator>();
    static {
      for (Operator operator : Operator.values()) {
        ALL_OPERATORS.add(operator);
        if (operator.getOperatorClass() != OperatorClass.TEXT) {
          COMPARABLE_OPERATORS.add(operator);
        }
      }
      EQUALITY_OPERATORS.add(Operator.EQUAL);
      EQUALITY_OPERATORS.add(Operator.NOT_EQUAL);
    }

    private String _symbol;
    private OperatorClass _opClass;
    private int _argumentCount = 2; // operator is binary, by default

    private Operator(String symbol, OperatorClass opClass)
    {
      this(symbol, opClass, 2);
    }

    private Operator(String symbol, OperatorClass opClass, int argumentCount)
    {
      _symbol = symbol;
      _opClass = opClass;
      _argumentCount = argumentCount;
    }

    public String getSymbol()
    {
      return _symbol;
    }

    public String getName() { return name(); }

    public boolean isUnary() { return _argumentCount == 1; }

    public OperatorClass getOperatorClass() { return _opClass; }
  }


  // instance data members

  private Operator _operator = Operator.EQUAL;
  private T _value;
  private String _regex;


  // public constructors and methods

  public Criterion()
  {
  }


  public Criterion(Operator operator, T value)
  {
    _operator = operator;
    _value = value;
  }

  public Criterion<T> setOperatorAndValue(Operator operator, T value)
  {
    setOperator(operator);
    setValue(value);
    return this;
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
    if (_operator.isUnary()) {
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
    // to reduce user-confusion, we'll treat empty strings as undefined criterion
    if (value instanceof String) {
      if (value == "") {
        value = null;
      }
    }
    
    if (NullSafeUtils.nullSafeEquals(_value, value)) {
      return;
    }
    _value = value;
    _regex = null;
    setChanged();
    notifyObservers();
  }

  @UIControllerMethod
  public void reset()
  {
   if (_operator.isUnary()) {
     _operator = Operator.EQUAL;
   }
    setValue(null);
  }

  /**
   * An undefined criterion always matches (does not filter out any data).
   * @return
   */
  public boolean isUndefined()
  {
    return _operator == null ||
    (!_operator.isUnary() && _value == null) ||
    _operator == Operator.ANY;
  }

  @SuppressWarnings("unchecked")
  public boolean matches(Object datum)
  {
    Object criterionValue = getValue();
    Operator operator = getOperator();

    // handle collections as flattened string representations
    if (criterionValue instanceof Collection) {
      criterionValue = StringUtils.makeListString((Collection) criterionValue, " ");
    }
    if (datum instanceof Collection) {
      datum = StringUtils.makeListString((Collection) datum, " ");
    }

    boolean result;
    if (isUndefined()) {
      // non-initialized criterion value means "do not filter" (criterion is undefined by user;
      // operator negation is not considered
      return true;
    }
    if (operator == Operator.EMPTY || operator == Operator.NOT_EMPTY) {
      result = datum == null || datum.toString().length() == 0;
      if (operator == Operator.NOT_EMPTY) {
        result = !result;
      }
    }
    else if (datum == null) {
      // null data value can only match with the EMPTY operator;
      // operator negation is not considered
      return false;
    }
    else if (operator == Operator.EQUAL || operator == Operator.NOT_EQUAL) {
      result = NullSafeUtils.nullSafeEquals(criterionValue, datum);
      if (operator == Operator.NOT_EQUAL) {
        result = !result;
      }
    }
    else if (operator == Operator.GREATER_THAN || operator == Operator.LESS_THAN_EQUAL ||
      operator == Operator.LESS_THAN || operator == Operator.GREATER_THAN_EQUAL) {
      if (! (datum instanceof Comparable)) {
        throw new CriterionMatchException("expecting Comparable value", this);
      }
      int cmpResult = ((Comparable) datum).compareTo(criterionValue);
      result = operator == Operator.GREATER_THAN ? cmpResult > 0 :
        operator == Operator.LESS_THAN ? cmpResult < 0 :
          operator == Operator.GREATER_THAN_EQUAL ? cmpResult >= 0 :
            cmpResult <= 0;
    }
    else if (operator == Operator.TEXT_LIKE || operator == Operator.TEXT_NOT_LIKE ||
      operator == Operator.TEXT_STARTS_WITH ||
      operator == Operator.TEXT_CONTAINS || operator == Operator.TEXT_NOT_CONTAINS) {
      if (! (criterionValue instanceof String)) {
        throw new CriterionMatchException("expecting String criterion value", this);
      }
      result = ((String) datum).matches(getRegex((String) criterionValue));
      if (operator == Operator.TEXT_NOT_LIKE || operator == Operator.TEXT_NOT_CONTAINS) {
        result = !result;
      }
    }
    else {
      throw new CriterionMatchException(operator + " not supported", this);
    }
    return result;
  }

  private String getRegex(String expr)
  {
    if (_regex == null) {
      Operator textOperator = getOperator();
      if (textOperator == Operator.TEXT_STARTS_WITH) {
        expr = expr + "*";
      }
      else if (textOperator == Operator.TEXT_LIKE || textOperator == Operator.TEXT_NOT_LIKE) {
        // use expression exactly as provided
      }
      else if (textOperator == Operator.TEXT_CONTAINS || textOperator == Operator.TEXT_NOT_CONTAINS) {
        expr = "*" + expr + "*";
      }
      _regex = convertToRegex(expr);
    }
    return _regex;
  }

  /**
   * @param expr search string containing wildcard characters "*" and "?", which can be escaped with a preceding "\" to be treats as literals.
   */
  private String convertToRegex(String expr)
  {
    String regex = expr;
    String lastRegex;
    // note: loop is necessary to properly handle sequential '?' characters
    do {
      lastRegex = regex;
      regex = regex.replaceAll("([^\\\\])\\?|^\\?", "$1__ANY_ONE__");
      regex = regex.replaceAll("([^\\\\])\\*|^\\*", "$1__ANY_MULTI__");
    } while (! regex.equals(lastRegex));

    // escape any remaining regex special characters in user's expression
    regex = regex.replaceAll("\\\\\\\\", "\\\\\\\\");
    String[] REGEX_CHARACTERS = { ".", "^", "$", "|", "(", ")", "{", "}", "[", "]" };
    for (String c : REGEX_CHARACTERS ) {
      if (regex.contains(c)) {
        regex = regex.replace(c, "\\" + c);
      }
    }

    // convert user's wildcards to regex equivalents
    regex = regex.replaceAll("__ANY_ONE__", ".").replaceAll("__ANY_MULTI__", ".*");
    regex = "(?i)(?s)" + regex; // case insensitive, single-line mode (i.e., match across all lines)
    log.debug("regex=" + regex);
    return regex;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj instanceof Criterion) {
      Criterion<T> that = (Criterion<T>) obj;
      return this._operator == that._operator &&
      (this._value == null ? that._value == null : this._value.equals(that._value));
    }
    return false;
  }

  public String toString()
  {
    return "[" + _operator + " " + getValue() + "]";
  }
}
