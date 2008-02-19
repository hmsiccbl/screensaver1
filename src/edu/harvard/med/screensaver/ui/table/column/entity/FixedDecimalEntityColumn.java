// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table.column.entity;

import java.math.BigDecimal;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.PropertyPath;
import edu.harvard.med.screensaver.model.RelationshipPath;
import edu.harvard.med.screensaver.ui.table.column.ColumnType;

public abstract class FixedDecimalEntityColumn<E extends AbstractEntity> extends EntityColumn<E,BigDecimal>
{
  public FixedDecimalEntityColumn(RelationshipPath<E> relationshipPath, String name, String description, String group)
  {
    super(relationshipPath, name, description, ColumnType.FIXED_DECIMAL, group);
  }

  public FixedDecimalEntityColumn(PropertyPath<E> propertyPath, String name, String description, String group)
  {
    super(propertyPath, name, description, ColumnType.FIXED_DECIMAL, group);
  }
}
