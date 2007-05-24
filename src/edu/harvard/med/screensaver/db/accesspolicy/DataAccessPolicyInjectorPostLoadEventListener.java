// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db.accesspolicy;

import edu.harvard.med.screensaver.model.AbstractEntity;

import org.apache.log4j.Logger;
import org.hibernate.event.PostLoadEvent;
import org.hibernate.event.def.DefaultPostLoadEventListener;

/**
 * A Hibernate event listener that injects the specified DataAccessPolicy into
 * every AbstractEntity object loaded by Hibernate.
 * 
 * @see AbstractEntity#isRestricted()
 * 
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class DataAccessPolicyInjectorPostLoadEventListener extends DefaultPostLoadEventListener
{
  // static members

  private static final long serialVersionUID = 1L;
  private static Logger log = Logger.getLogger(DataAccessPolicyInjectorPostLoadEventListener.class);

  // instance data members
  
  private DataAccessPolicy _dataAccessPolicy;
  
  
  // public constructors and methods
  
  public DataAccessPolicyInjectorPostLoadEventListener(DataAccessPolicy dataAccessPolicy)
  {
    _dataAccessPolicy = dataAccessPolicy;
  }

  @Override
  public void onPostLoad(PostLoadEvent event)
  {
    // let the Hibernate core do its thing
    super.onPostLoad(event);
    
    if (event.getEntity() instanceof AbstractEntity) {
      AbstractEntity entity = (AbstractEntity) event.getEntity();
      entity.setDataAccessPolicy(_dataAccessPolicy);
      /*
      if (log.isDebugEnabled()) {
        log.debug("injected entity " + entity.getClass().getSimpleName() + ":" + event.getId() + " with DataAccessPolicy");
      }
      */
    }
  }

  // private methods

}

