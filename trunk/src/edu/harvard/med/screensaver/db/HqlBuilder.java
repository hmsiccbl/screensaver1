// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

public class HqlBuilder
{
  // static members

  private static Logger log = Logger.getLogger(HqlBuilder.class);


  // instance data members

  private StringBuilder _hql;
  private StringBuilder _select = new StringBuilder();
  private StringBuilder _from = new StringBuilder();
  private StringBuilder _where= new StringBuilder();
  private StringBuilder _orderBy = new StringBuilder();
  private List<Object> _args = new ArrayList<Object>();
  private Set<String> _selectAliases = new HashSet<String>();
  private Set<String> _aliases = new HashSet<String>();

  // public constructors and methods

  public HqlBuilder()
  {
  }

  public HqlBuilder select(String alias, String property)
  {
    _selectAliases.add(alias);
    if (_select.length() > 0) {
      _select.append(", ");
    }
    _select.append(alias).append('.').append(property);
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

  public HqlBuilder from(String joinAlias, String joinRelationship, String alias)
  {
    checkAliasExists(joinAlias);
    checkAliasIsUnique(alias);
    if (_from.length() == 0) {
      throw new IllegalStateException("must call from(Class, String) first");
    }
    _from.append(" join ").append(joinAlias).append('.').append(joinRelationship).append(' ').append(alias);
    return this;
  }

  public HqlBuilder where(String alias, String property, Object arg)
  {
    checkAliasExists(alias);
    if (_where.length() > 0) {
      _where.append(" and ");
    }
    _where.append(alias).append('.').append(property).append("=?");
    _args.add(arg);
    return this;
  }

  public HqlBuilder where(String alias, edu.harvard.med.screensaver.model.AbstractEntity entity)
  {
    checkAliasExists(alias);
    if (_where.length() > 0) {
      _where.append(" and ");
    }
    _where.append(alias).append("=?");
    _args.add(entity);
    return this;
  }

  public HqlBuilder orderBy(String alias, String property)
  {
    checkAliasExists(alias);
    return orderBy(alias, property, SortDirection.ASCENDING);
  }

  public HqlBuilder orderBy(String alias, String property, SortDirection sortDirection)
  {
    checkAliasExists(alias);
    if (_orderBy.length() > 0) {

      _orderBy.append(", ");
    }
    _orderBy.append(alias).append('.').append(property);
    if (sortDirection == SortDirection.DESCENDING) {
      _orderBy.append(" desc");
    }
    return this;
  }

  public String hql()
  {
    if (_hql == null) {
      _hql = new StringBuilder();
      if (!_aliases.containsAll(_selectAliases)) {
        Set<String> _undefinedSelectAliases = new HashSet<String>(_selectAliases);
        _undefinedSelectAliases.removeAll(_aliases);
        throw new RuntimeException("select aliases " + _undefinedSelectAliases + " undefined");
      }
      if (_select.length() > 0) {
        _hql.append("select ").append(_select).append(' ');
      }
      if (_from.length() == 0) {
        throw new RuntimeException("empty from clause");
      }
      _hql.append("from " ).append(_from);

      if (_where.length() > 0) {
        _hql.append(" where ").append(_where);
      }

      if (_orderBy.length() > 0) {
        _hql.append(" order by ").append(_orderBy);
      }
    }
    return _hql.toString();
  }

  public List<Object> args()
  {
    return _args;
  }

  public Object arg(int i)
  {
    return _args.get(i);
  }


  // private methods

  private String entityName(Class<?> entityClass)
  {
    return entityClass.getSimpleName();
  }

  private void checkAliasExists(String alias)
  {
    if (!_aliases.contains(alias)) {
      throw new RuntimeException("alias " + alias + " not defined");
    }
  }
  private void checkAliasIsUnique(String alias)
  {
    if (!_aliases.add(alias)) {
      throw new RuntimeException("alias " + alias + " already used");
    }
  }
}
