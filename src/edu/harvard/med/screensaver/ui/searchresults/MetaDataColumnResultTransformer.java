// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/branches/schema-upgrade-2007/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import edu.harvard.med.screensaver.db.accesspolicy.UnrestrictedEntityResultTransformer;
import edu.harvard.med.screensaver.model.AbstractEntity;
import edu.harvard.med.screensaver.ui.screenresults.MetaDataType;
import edu.harvard.med.screensaver.util.Triple;

public class MetaDataColumnResultTransformer<T extends AbstractEntity & MetaDataType> 
  extends UnrestrictedEntityResultTransformer
{
  private static final long serialVersionUID = 1L;

  @Override
  public Object transformTuple(Object[] tuple, String[] aliases)
  {
    return new Triple<T,Integer,String>((T) tuple[0], (Integer) tuple[1], (String) tuple[2]);
  }

  @Override
  protected AbstractEntity extractEntityToCheck(Object row)
  {
    return ((Triple<T,Integer,String>) row).getFirst();
  }
}