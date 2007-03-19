// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.cherrypicks;

import java.util.Comparator;

import edu.harvard.med.screensaver.model.libraries.Copy;

/**
 * Defines an ordering of Copy entities that respects the order in which copies
 * should be considered when selecting source copies for a cherry pick.
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class SourceCopyComparator implements Comparator<Copy>
{
  static private SourceCopyComparator _instance = new SourceCopyComparator();
  
  public static SourceCopyComparator getInstance() 
  {
    return _instance;
  }
  
  public int compare(Copy copy1, Copy copy2)
  {
    return copy1.getName().compareTo(copy2.getName());
  }
}

