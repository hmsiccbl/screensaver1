// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.accesspolicy;

import java.util.ArrayList;
import java.util.List;

import edu.harvard.med.screensaver.model.AbstractEntity;

import org.hibernate.transform.ResultTransformer;

public class UnrestrictedEntityResultTransformer implements ResultTransformer
{
  private static final long serialVersionUID = 1L;

  public List transformList(List collection)
  {
    List<Object> result = new ArrayList<Object>(collection.size()); 
    for (Object row : collection) {
      AbstractEntity entity = extractEntityToCheck(row);
      if (!entity.isRestricted()) {
        result.add(row);
      }
    }
    return result;
  }

  public Object transformTuple(Object[] tuple, String[] aliases)
  {
    return tuple;
  }
  
  protected AbstractEntity extractEntityToCheck(Object row)
  {
    return (AbstractEntity) row;
  }
}
