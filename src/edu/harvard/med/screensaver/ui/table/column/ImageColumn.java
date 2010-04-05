// $HeadURL: http://forge.abcd.harvard.edu/svn/screensaver/branches/iccbl/1.9.2-dev/src/edu/harvard/med/screensaver/ui/table/column/BooleanColumn.java $
// $Id: BooleanColumn.java 2962 2009-02-06 22:38:16Z seanderickson1 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.table.column;


public abstract class ImageColumn<R> extends TableColumn<R,String>
{
  public ImageColumn(String name, String description, String group)
  {
    super(name,
          description,
          ColumnType.IMAGE,
          group);
  }

  @Override
  public boolean isSortableSearchable()
  {
    return false;
  }
}
