// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table.column;

import java.util.ArrayList;
import java.util.List;

import javax.faces.convert.BigDecimalConverter;
import javax.faces.convert.BooleanConverter;
import javax.faces.convert.Converter;
import javax.faces.convert.DoubleConverter;
import javax.faces.convert.IntegerConverter;
import javax.faces.model.SelectItem;

import edu.harvard.med.screensaver.ui.table.Criterion.Operator;
import edu.harvard.med.screensaver.ui.util.NoOpStringConverter;
import edu.harvard.med.screensaver.ui.util.VolumeConverter;

public enum ColumnType {

  TEXT(false,
       new NoOpStringConverter(),
       Operator.ALL_OPERATORS,
       Operator.TEXT_STARTS_WITH),
  INTEGER(true,
          new IntegerConverter(),
          Operator.COMPARABLE_OPERATORS,
          Operator.EQUAL),
  REAL(true,
       new DoubleConverter(),
       Operator.COMPARABLE_OPERATORS,
       Operator.EQUAL),
  FIXED_DECIMAL(true,
                new BigDecimalConverter(),
                Operator.COMPARABLE_OPERATORS,
                Operator.EQUAL),
  VOLUME(true,
         new VolumeConverter(),
         Operator.COMPARABLE_OPERATORS,
         Operator.EQUAL),
  DATE(false,
       new LocalDateConverter(),
       Operator.COMPARABLE_OPERATORS,
       Operator.EQUAL),
  BOOLEAN(false,
          new BooleanConverter(),
          Operator.COMPARABLE_OPERATORS,
          Operator.EQUAL),
  VOCABULARY(false,
             null,
             Operator.COMPARABLE_OPERATORS,
             Operator.EQUAL),
  LIST(false,
       new NoOpStringConverter(),
       Operator.ALL_OPERATORS,
       Operator.TEXT_CONTAINS);

  private Converter _converter = NoOpStringConverter.getInstance();
  private boolean _isNumeric;
  private List<Operator> _validOperators;
  private List<SelectItem> _operatorSelections = new ArrayList<SelectItem>();
  private Operator _defaultOperator;

  private ColumnType(boolean isNumeric,
                     Converter converter,
                     List<Operator> validOperators,
                     Operator defaultOperator)
  {
    _isNumeric = isNumeric;
    _converter = converter;
    _validOperators = validOperators;
    _defaultOperator = defaultOperator;
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

  public Operator getDefaultOperator()
  {
    return _defaultOperator;
  }
}