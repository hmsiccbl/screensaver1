// $HeadURL: http://forge.abcd.harvard.edu/svn/screensaver/branches/serickson/lincs-3107/core/src/main/java/edu/harvard/med/screensaver/ui/arch/datatable/column/SelectableColumnTreeNode.java $
// $Id: SelectableColumnTreeNode.java 5158 2011-01-06 14:26:53Z atolopko $
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.arch.datatable.column;

import java.util.AbstractList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import org.apache.myfaces.custom.tree2.TreeNodeBase;

/**
 * A TreeNode for a JSF Tree2 component that selects/deselects all of its selectable children nodes, but will not render
 * its children.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class SelectableColumnGroupTreeNode<R> extends TreeNodeBase
{
  private static final long serialVersionUID = 1L;
  
  class Children extends AbstractList<TreeNodeBase>
  {
    private List<TreeNodeBase> _columns = Lists.newArrayList();

    @Override
    public boolean add(TreeNodeBase e)
    {
      return _columns.add(e);
    }

    @Override
    public void add(int index, TreeNodeBase element)
    {
      _columns.add(index, element);
    }

    @Override
    public boolean addAll(Collection<? extends TreeNodeBase> c)
    {
      return _columns.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends TreeNodeBase> c)
    {
      return _columns.addAll(index, c);
    }

    @Override
    public TreeNodeBase get(int index)
    {
      return null;
    }

    @Override
    public int size()
    {
      return 0;
    }

    List<TreeNodeBase> getTableColumns()
    {
      return _columns;
    }
  }

  private Children _children = new Children();
  private boolean _selected;

  public SelectableColumnGroupTreeNode(String name)
  {
    super("selectableGroup", name, true);
  }
  
  public boolean isChecked()
  {
    return _selected;
  }
  
  public void setChecked(boolean checked)
  {
    _selected = checked;
    for (Object child : _children.getTableColumns()) {
      // TODO: eliminate these dual if checks by creating a SelectableTreeNode abstract base class
      if (child instanceof SelectableColumnTreeNode) {
        ((SelectableColumnTreeNode) child).setChecked(checked);
      }
      if (child instanceof SelectableColumnGroupTreeNode) {
        ((SelectableColumnGroupTreeNode) child).setChecked(checked);
      }
    }
  }

  @Override
  public List getChildren()
  {
    return _children;
  }
}