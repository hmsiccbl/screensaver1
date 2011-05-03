// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.hqlbuilder;

import java.util.Set;

import edu.harvard.med.screensaver.ui.arch.datatable.Criterion.Operator;
import edu.harvard.med.screensaver.ui.arch.datatable.Criterion.OperatorClass;

public class SimplePredicate extends Predicate
{
  public final static Predicate FALSE = new Predicate() {
    @Override
    public String toHql()
    {
      return "false = true";
    }
  };
  
  private final HqlBuilder _hqlBuilder;
  private String _whereExpression;

  public SimplePredicate(HqlBuilder hqlBuilder,
                         String lhs,
                         Operator operator,
                         Object value)
  {
    _hqlBuilder = hqlBuilder;
    _whereExpression = makeWhereExpression(lhs, operator, ":" + argName());

    if (operator.getOperatorClass() == OperatorClass.TEXT) {
      _hqlBuilder.args().put(argName(),
                             getHibernateWildcardExpression(value.toString(), operator));
    }
    else if (operator != Operator.EMPTY && operator != Operator.NOT_EMPTY
      /*TODO: criterion.getOperator().getArgumentCount() == 1*/) {
      _hqlBuilder.args().put(argName(), value);
    }

  }

  public SimplePredicate(HqlBuilder hqlBuilder,
                         String lhs,
                         Set<?> values)
  {
    _hqlBuilder = hqlBuilder;
    StringBuilder s = new StringBuilder();
    String argName = argName() + HqlBuilder.SET_ARG_SUFFIX;
    s.append(lhs).append(" in (:").append(argName).append(')');
    _hqlBuilder.args().put(argName, values);
    _whereExpression = s.toString();
  }

  public SimplePredicate(HqlBuilder hqlBuilder,
                         String lhs,
                         String rhs,
                         Operator operator)
  {
    _hqlBuilder = hqlBuilder;
    StringBuilder s = new StringBuilder();
    s.append(lhs).append(toHqlOperator(operator)).append(rhs);
    _whereExpression = s.toString();
  }

  @Override
  public String toHql()
  {
    return _whereExpression;
  }


  // private methods

  private String argName()
  {
    return "arg" + _hqlBuilder.args().size();
  }

  @SuppressWarnings("unchecked")
  private String makeWhereExpression(String lhs,
                                     Operator operator,
                                     String argName)
  {
    if (operator == Operator.ANY) {
      // 'any' operator means "do not filter"
      return "";
    }

    if (operator == Operator.EMPTY) {
      return "(" + lhs + " is null or length(cast( " + lhs + " as text)) = 0)";
    }
    else if (operator == Operator.NOT_EMPTY) {
      return "(" + lhs + " is not null and length(cast(" + lhs + " as text)) > 0)";
    }
    else if (operator.getOperatorClass() == OperatorClass.TEXT) {
      return String.format("lower(%s)%slower(%s)", lhs, toHqlOperator(operator), argName);
    }
    else {
      return String.format("%s%s%s", lhs, toHqlOperator(operator), argName);
    }
  }

  private String toHqlOperator(Operator operator)
  {
    if (operator == Operator.EQUAL) {
      return "=";
    }
    if (operator == Operator.NOT_EQUAL) {
      return "<>";
    }
    if (operator == Operator.GREATER_THAN) {
      return ">";
    }
    if (operator == Operator.GREATER_THAN_EQUAL) {
      return ">=";
    }
    if (operator == Operator.LESS_THAN) {
      return "<";
    }
    if (operator == Operator.LESS_THAN_EQUAL) {
      return "<=";
    }
    if (operator == Operator.TEXT_CONTAINS) {
      return " like ";
    }
    if (operator == Operator.TEXT_NOT_CONTAINS) {
      return " not like ";
    }
    if (operator == Operator.TEXT_LIKE) {
      return " like ";
    }
    if (operator == Operator.TEXT_NOT_LIKE) {
      return " not like ";
    }
    if (operator == Operator.TEXT_STARTS_WITH) {
      return " like ";
    }
    throw new IllegalArgumentException("unsupported operator " + operator);
  }

  private String getHibernateWildcardExpression(String wildcardExpression,
                                                Operator textOperator)
  {
    if (textOperator.getOperatorClass() != OperatorClass.TEXT) {
      throw new IllegalArgumentException("textOperator must be, well, a text operator");
    }

    String expr = wildcardExpression.replaceAll("\\*", "%");

    if (textOperator == Operator.TEXT_STARTS_WITH) {
      if (!expr.endsWith("%")) {
        expr = expr + "%";
      }
    }
    else if (textOperator == Operator.TEXT_CONTAINS || textOperator == Operator.TEXT_NOT_CONTAINS) {
      if (!expr.startsWith("%")) {
        expr = "%" + expr;
      }
      if (!expr.endsWith("%")) {
        expr = expr + "%";
      }
    }
    return expr;
  }
}