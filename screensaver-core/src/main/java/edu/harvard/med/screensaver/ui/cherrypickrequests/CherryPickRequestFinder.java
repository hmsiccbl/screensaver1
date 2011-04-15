// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.cherrypickrequests;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.CherryPickRequestDAO;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.ui.arch.view.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.arch.view.aspects.UICommand;

/**
 *
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class CherryPickRequestFinder extends AbstractBackingBean
{

  // private static final fields

  private static final Logger log = Logger.getLogger(CherryPickRequestFinder.class);


  // private instance fields

  private CherryPickRequestDAO _cherryPickRequestDao;
  private CherryPickRequestViewer _cherryPickRequestViewer;

  private Integer _cherryPickRequestNumber;


  // constructors

  /**
   * @motivation for CGLIB2
   */
  protected CherryPickRequestFinder()
  {
  }

  public CherryPickRequestFinder(CherryPickRequestDAO cherryPickRequestDao,
                                 CherryPickRequestViewer cherryPickRequestViewer)
  {
    _cherryPickRequestDao = cherryPickRequestDao;
    _cherryPickRequestViewer = cherryPickRequestViewer;
  }


  // public instance methods

  public Integer getCherryPickRequestNumber()
  {
    return _cherryPickRequestNumber;
  }

  public void setCherryPickRequestNumber(Integer cherryPickRequestNumber)
  {
    _cherryPickRequestNumber = cherryPickRequestNumber;
  }

  @UICommand
  public String findCherryPickRequest()
  {
    if (_cherryPickRequestNumber != null) {
      CherryPickRequest cherryPickRequest = _cherryPickRequestDao.findCherryPickRequestByNumber(_cherryPickRequestNumber);
      if (cherryPickRequest != null) {
        resetSearchFields();
        return _cherryPickRequestViewer.viewEntity(cherryPickRequest);
      }
      else {
        showMessage("noSuchEntity",
                    "Cherry Pick Request " + _cherryPickRequestNumber);
        resetSearchFields(); // if we don't reset now, and the user decides to not search again, the search criteria remain in the input fields
      }
    }
    else {
      showMessage("cherryPickRequests.cherryPickRequestNumberRequired",
                  _cherryPickRequestNumber);
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
  
  private void resetSearchFields()
  {
    _cherryPickRequestNumber = null;
  }
  
}
