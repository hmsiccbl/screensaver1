// $HeadURL:
// svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml
// $
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table.column.entity;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.PropertyPath;
import edu.harvard.med.screensaver.model.RelationshipPath;
import edu.harvard.med.screensaver.ui.util.VocabularlyConverter;


public abstract class EnumEntityColumn<E extends AbstractEntity, ENUM extends Enum<ENUM>> extends VocabularyEntityColumn<E,ENUM>
{
  public EnumEntityColumn(PropertyPath<E> propertyPath,
                          String name,
                          String description,
                          String group,
                          ENUM[] items)
  {
    super(propertyPath,
          name,
          description,
          group,
          new VocabularlyConverter<ENUM>(items),
          items);
  }

  public EnumEntityColumn(RelationshipPath<E> relationshipPath,
                          String name,
                          String description,
                          String group,
                          ENUM[] items)
  {
    super(relationshipPath,
          name,
          description,
          group,
          new VocabularlyConverter<ENUM>(items),
          items);
  }
}
