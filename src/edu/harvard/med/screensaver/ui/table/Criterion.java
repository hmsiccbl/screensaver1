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
import java.util.Arrays;
import java.util.List;
import java.util.Observable;

import javax.faces.model.SelectItem;

import edu.harvard.med.screensaver.ui.UIControllerMethod;
import edu.harvard.med.screensaver.util.NullSafeUtils;
import edu.harvard.med.screensaver.util.StringUtils;

import org.apache.log4j.Logger;

import com.sun.org.apache.xalan.internal.xsltc.compiler.Pattern;
import com.sun.org.apache.xerces.internal.impl.xs.identity.Selector.Matcher;

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
    // equality operators
    EQUAL("="),
    NOT_EQUAL("<>"),
    // ranking operators
    LESS_THAN("<"),
    LESS_THAN_EQUAL("<="),
    GREATER_THAN(">"),
    GREATER_THAN_EQUAL(">="),
    // text operators
    TEXT_STARTS_WITH("starts with"),
    TEXT_CONTAINS("contains"),
    TEXT_NOT_CONTAINS("doesn't contain"),
    TEXT_LIKE("matches"),
    TEXT_NOT_LIKE("doesn't match"),
    // empty operators
    EMPTY("blank"),
    NOT_EMPTY("not blank");

    public static List<Operator> ALL_OPERATORS = new ArrayList<Operator>();
    public static List<Operator> COMPARABLE_OPERATORS = new ArrayList<Operator>();
    public static List<Operator> EQUALITY_OPERATORS = new ArrayList<Operator>();
    static {
      for (Operator operator : Operator.values()) {
        ALL_OPERATORS.add(operator);
        if (!operator.name().startsWith("TEXT")) {
          COMPARABLE_OPERATORS.add(operator);
        }
      }
      EQUALITY_OPERATORS.add(Operator.EQUAL);
      EQUALITY_OPERATORS.add(Operator.NOT_EQUAL);
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
    else if (operator == Operator.TEXT_LIKE || operator == Operator.TEXT_NOT_LIKE ||
      operator == Operator.TEXT_STARTS_WITH ||
      operator == Operator.TEXT_CONTAINS || operator == Operator.TEXT_NOT_CONTAINS) {
      if (! (criterionValue instanceof String)) {
        throw new CriterionMatchException("expecting String criterion value", this);
      }
      result = ((String) inputValue).matches(getRegex());
      if (operator == Operator.TEXT_NOT_LIKE || operator == Operator.TEXT_NOT_CONTAINS) {
        result = !result;
      }
    }
    else {
      throw new CriterionMatchException(operator + " not supported", this);
    }
    return result;
  }

  private String getRegex()
  {
    if (_regex == null) {
      String expr = (String) _value;
      assert _value != null;
      if (_operator == Operator.TEXT_STARTS_WITH) {
        expr = expr + "*";
      }
      else if (_operator == Operator.TEXT_LIKE || _operator == Operator.TEXT_NOT_LIKE) {
        // use expression exactly as provided
      }
      else if (_operator == Operator.TEXT_CONTAINS || _operator == Operator.TEXT_NOT_CONTAINS) {
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
}
