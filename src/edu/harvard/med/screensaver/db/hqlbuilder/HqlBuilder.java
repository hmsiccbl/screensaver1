// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.hqlbuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.transform.DistinctRootEntityResultTransformer;

import edu.harvard.med.screensaver.db.SortDirection;
import edu.harvard.med.screensaver.ui.table.Criterion.Operator;
import edu.harvard.med.screensaver.util.StringUtils;

public class HqlBuilder
{
  // static members

  private static Logger log = Logger.getLogger(HqlBuilder.class);

  static final String SET_ARG_SUFFIX = "Set";


  // inner classes

  private boolean _isDistinct = false;
  private boolean _isDistinctRootEntities = false;
  private StringBuilder _select = new StringBuilder();
  private StringBuilder _from = new StringBuilder();
  private CompositePredicate _where = new Conjunction();
  private CompositePredicate _having = new Conjunction();
  private List<String> _orderBy = Lists.newArrayList();
  private List<String> _groupBy = Lists.newArrayList();
  private Map<String,Object> _args = new HashMap<String,Object>();
  private Set<String> _aliases = new HashSet<String>();
  private Set<String> _selectAliases = new HashSet<String>();


  // public constructors & methods

  public HqlBuilder()
  {
  }

  public HqlBuilder distinctProjectionValues()
  {
    _isDistinct = true;
    return this;
  }

  public HqlBuilder distinctRootEntities()
  {
    _isDistinctRootEntities = true;
    return this;
  }
  
  /**
   * Select a full entity to retrieved.
   * @param alias
   */
  public HqlBuilder select(String alias)
  {
    return select(alias, null);
  }
  
  public HqlBuilder selectExpression(String expression)
  {
    if (_select.length() > 0) {
      _select.append(", ");
    }
    _select.append(expression);
    return this;
  }

  public HqlBuilder select(String alias, String property)
  {
    _selectAliases.add(alias);
    if (_select.length() > 0) {
      _select.append(", ");
    }
    _select.append(alias);
    if (property != null) {
      _select.append('.').append(property);
    } // else, select the full entity
    return this;
  }

  public HqlBuilder from(Class<?> entityClass, String alias)
  {
    checkAliasIsUnique(alias);
    if (_from.length() > 0) {
      _from.append(", ");
    }
    _from.append(entityName(entityClass)).append(' ').append(alias);
    return this;
  }

  public HqlBuilder fromFetch(String joinAlias, String joinRelationship, String alias)
  {
    return from(joinAlias, joinRelationship, alias, JoinType.LEFT_FETCH);
  }

  public HqlBuilder from(String joinAlias, String joinRelationship, String alias)
  {
    return from(joinAlias, joinRelationship, alias, JoinType.LEFT);
  }

  public HqlBuilder from(String joinAlias, String joinRelationship, String alias, JoinType joinType)
  {
    checkAliasExists(joinAlias);
    checkAliasIsUnique(alias);
    if (_from.length() == 0) {
      throw new IllegalStateException("must call from(Class, String) first");
    }
    if (joinType == JoinType.LEFT) {
      _from.append(" left join ");
    }
    else if (joinType == JoinType.LEFT_FETCH) {
      _from.append(" left join fetch ");
    }
    else {
      _from.append(" join ");
    }
    _from.append(joinAlias).append('.').append(joinRelationship).append(' ').append(alias);
    return this;
  }

  public HqlBuilder restrictFrom(String alias, String property, Operator operator, Object value)
  {
    checkAliasExists(alias);
    _from.append(" with ").append(new SimplePredicate(this, makeRef(alias, property), operator, value).toHql());
    return this;
  }

  public HqlBuilder where(String alias, String property, Operator operator, Object value)
  {
    checkAliasExists(alias);
    _where.add(simplePredicate(makeRef(alias, property), operator, value));
    return this;
  }

  /**
   * Use when lhs of where expression is a collection of values (which
   * themselves have no internal property other than the value itself).
   */
  public HqlBuilder where(String alias, Operator operator, Object value)
  {
    return where(alias, null, operator, value);
  }

  public HqlBuilder where(String alias1, String property1, Operator operator, String alias2, String property2)
  {
    checkAliasExists(alias1);
    checkAliasExists(alias2);
    _where.add(simplePredicate(makeRef(alias1, property1), makeRef(alias2, property2), operator));
    return this;
  }

  /**
   * Use when lhs and rhs of where expression is a collection of values (which
   * themselves have no internal property other than the value itself).
   */
  public HqlBuilder where(String alias1, Operator operator, String alias2)
  {
    return where(alias1, null, operator, alias2, null);
  }

  /**
   * Use when the rhs of where expression is an entity object, in which case
   * Hibernate will use the entity's ID.
   */
  public HqlBuilder where(String alias, edu.harvard.med.screensaver.model.AbstractEntity entity)
  {
    checkAliasExists(alias);
    _where.add(simplePredicate(alias, Operator.EQUAL, entity));
    return this;
  }

  public HqlBuilder whereIn(String alias, String property, Set<?> values)
  {
    checkAliasExists(alias);
    if (values.size() == 0) {
      _where.add(SimplePredicate.FALSE);
    }
    else {
      _where.add(new SimplePredicate(this, makeRef(alias, property), values));
    }
    return this;
  }

  /**
   * Use when the lhs of where..in expression is a collection of values (which
   * themselves have no internal property other than the value itself).
   */
  public HqlBuilder whereIn(String alias, Set<?> values)
  {
    return whereIn(alias, null, values);
  }

  public HqlBuilder where(Predicate predicate)
  {
    _where.add(predicate);
    return this;
  }

  public Disjunction disjunction()
  {
    return new Disjunction();
  }

  public Conjunction conjunction()
  {
    return new Conjunction();
  }

  public SimplePredicate simplePredicate(String lhs, Operator operator, Object value)
  {
    SimplePredicate simplePredicate = new SimplePredicate(this, lhs, operator, value);
    return simplePredicate;
  }

  public SimplePredicate simplePredicate(String lhs, String rhs, Operator operator)
  {
    SimplePredicate simplePredicate = new SimplePredicate(this, lhs, rhs, operator);
    return simplePredicate;
  }

  public HqlBuilder orderBy(String alias, String property)
  {
    return orderBy(alias, property, SortDirection.ASCENDING);
  }

  /**
   * Use when you need to order by a collection of values (which
   * themselves have no internal property other than the value itself).
   */
  public HqlBuilder orderBy(String alias, SortDirection sortDirection)
  {
    return orderBy(alias, null, sortDirection);
  }

  public HqlBuilder orderBy(String alias, String property, SortDirection sortDirection)
  {
    checkAliasExists(alias);
    StringBuilder orderBy = new StringBuilder(alias);
    if (!!!StringUtils.isEmpty(property)) {
      orderBy.append('.').append(property);
    }
    if (sortDirection == SortDirection.DESCENDING) {
      orderBy.append(" desc");
    }
    _orderBy.add(orderBy.toString());
    return this;
  }

  public String toHql()
  {
    StringBuilder _hql = new StringBuilder();
    if (!_aliases.containsAll(_selectAliases)) {
      Set<String> _undefinedSelectAliases = new HashSet<String>(_selectAliases);
      _undefinedSelectAliases.removeAll(_aliases);
      throw new RuntimeException("select aliases " + _undefinedSelectAliases + " undefined");
    }
    if (_select.length() > 0) {
      _hql.append("select ");
      if (_isDistinct) {
        _hql.append("distinct ");
      }
      _hql.append(_select).append(' ');
    }
    if (_from.length() == 0) {
      throw new RuntimeException("empty from clause");
    }
    _hql.append("from " ).append(_from);

    if (_where.size() > 0) {
      _hql.append(" where ").append(_where.toHql());
    }

    if (!!!_groupBy.isEmpty()) {
      // note: if we're using "group by", we also need to explicitly group by
      // any fields that we're ordering on, and these need to be first if
      // we're going to respect the requested ordering
      // TODO: as convenience, we could also group on any select fields that are non-aggregate expressions 
      List<String> nonRedundantGroupBy = Lists.newArrayList(_groupBy);
      nonRedundantGroupBy.removeAll(_orderBy);
      _hql.append(" group by ").append(Joiner.on(", ").join(Iterables.concat(_orderBy, nonRedundantGroupBy)));
    }
    if (_having.size() > 0) {
      _hql.append(" having ").append(_having.toHql());
    }
    if (!!!_orderBy.isEmpty()) {
      _hql.append(" order by ").append(Joiner.on(", ").join(_orderBy));
    }
    return _hql.toString();
  }

  public Map<String,Object> args()
  {
    return _args;
  }

  public Object arg(String name)
  {
    return _args.get(name);
  }

  public Query toQuery(Session session, boolean isReadOnly)
  {
    org.hibernate.Query query = session.createQuery(toHql());
    for (Map.Entry<String,Object> arg : args().entrySet()) {
      if (arg.getKey().endsWith(SET_ARG_SUFFIX)) {
        // HACK: handle 'list' type parameters, used with the 'IN (?)' operator
        query.setParameterList(arg.getKey(), (Set<?>) arg.getValue());
      }
      else {
        query.setParameter(arg.getKey(), arg.getValue());
      }
    }
    query.setReadOnly(true);
    if (_isDistinctRootEntities) {
      query.setResultTransformer(new DistinctRootEntityResultTransformer());
    }
    query.setReadOnly(isReadOnly);
    return query;
  }

  @Override
  public String toString()
  {
    return toHql() + " " + args();
  }


  // private methods

  private String entityName(Class<?> entityClass)
  {
    return entityClass.getSimpleName();
  }

  private void checkAliasExists(String alias)
  {
    if (!_aliases.contains(alias)) {
      throw new IllegalArgumentException("alias " + alias + " not defined");
    }
  }

  private void checkAliasIsUnique(String alias)
  {
    if (!_aliases.add(alias)) {
      throw new IllegalArgumentException("alias " + alias + " is already used");
    }
  }
  
  private String makeRef(String alias, String property)
  {
    if (!!!StringUtils.isEmpty(property)) {
      return alias + "." + property;
    }
    return alias;
  }

  public HqlBuilder groupBy(String alias)
  {
    return groupBy(alias, null);
  }

  public HqlBuilder groupBy(String alias, String property)
  {
    checkAliasExists(alias);
    StringBuilder groupBy = new StringBuilder(alias);
    if (!!!StringUtils.isEmpty(property)) {
      groupBy.append('.').append(property);
    }
    _groupBy.add(groupBy.toString());
    return this;
  }
  
  public HqlBuilder having(Predicate havingPredicate) 
  {
    _having.add(havingPredicate);
    return this;
  }
}
