// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table.column.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.faces.convert.Converter;
import javax.faces.model.SelectItem;

import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.model.PropertyPath;
import edu.harvard.med.screensaver.model.RelationshipPath;
import edu.harvard.med.screensaver.ui.table.column.ColumnType;

public abstract class VocabularyEntityColumn<E extends AbstractEntity,V> extends EntityColumn<E,V>
{
  private Set<V> _items;
  private ArrayList<SelectItem> _selectItems;

  public VocabularyEntityColumn(RelationshipPath<E> relationshipPath,
                          String name,
                          String description,
                          String group,
                          Converter converter, 
                          Set<V> items)
  {
    super(relationshipPath, name, description, ColumnType.VOCABULARY, group);
    setConverter(converter);
    _items = new LinkedHashSet<V>(items);
  }

  public VocabularyEntityColumn(RelationshipPath<E> relationshpPath,
                          String name,
                          String description,
                          String group,
                          Converter converter, 
                          V[] items)
  {
    this(relationshpPath, name, description, group, converter, new TreeSet<V>(Arrays.asList(items)));
  }

  public VocabularyEntityColumn(PropertyPath<E> propertyPath,
                          String name,
                          String description,
                          String group,
                          Converter converter, 
                          Set<V> items)
  {
    super(propertyPath, name, description, ColumnType.VOCABULARY, group);
    setConverter(converter);
    _items = new LinkedHashSet<V>(items);
  }

  public VocabularyEntityColumn(PropertyPath<E> propertyPath,
                          String name,
                          String description,
                          String group,
                          Converter converter, 
                          V[] items)
  {
    this(propertyPath, name, description, group, converter, new TreeSet<V>(Arrays.asList(items)));
  }

  public Set<V> getVocabulary()
  {
    return _items;
  }

  public List<SelectItem> getVocabularySelections()
  {
    if (_selectItems == null) {
      _selectItems = new ArrayList<SelectItem>();
      _selectItems.add(new SelectItem("", ""));
      for (V v : getVocabulary()) {
        _selectItems.add(new SelectItem(v));
      }
    }
    return _selectItems;
  }
}
