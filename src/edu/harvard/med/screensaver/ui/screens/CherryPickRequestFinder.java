// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screens;

import edu.harvard.med.screensaver.db.CherryPickRequestDAO;
import edu.harvard.med.screensaver.model.screens.CherryPickRequest;
import edu.harvard.med.screensaver.ui.AbstractBackingBean;
import edu.harvard.med.screensaver.ui.control.UIControllerMethod;

import org.apache.log4j.Logger;

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

  public void setCherryPickRequestNumber(Integer screenNumber)
  {
    _cherryPickRequestNumber = screenNumber;
  }

  @UIControllerMethod
  public String findCherryPickRequest()
  {
    if (_cherryPickRequestNumber != null) {
      CherryPickRequest cherryPickRequest = _cherryPickRequestDao.findCherryPickRequestByNumber(_cherryPickRequestNumber);
      if (cherryPickRequest != null) {
        return _cherryPickRequestViewer.viewCherryPickRequest(cherryPickRequest);
      }
      else {
        showMessage("noSuchEntity",
                    "Cherry Pick Request " + _cherryPickRequestNumber);
      }
    }
    else {
      showMessage("cherryPickRequests.cherryPickRequestNumberRequired",
                  _cherryPickRequestNumber);
    }
    return REDISPLAY_PAGE_ACTION_RESULT;
  }
}
