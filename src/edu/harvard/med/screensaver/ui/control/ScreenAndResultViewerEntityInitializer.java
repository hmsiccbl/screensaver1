// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.control;

import edu.harvard.med.screensaver.db.DAO;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.ui.util.ViewEntityInitializer;

import org.apache.log4j.Logger;

public abstract class ScreenAndResultViewerEntityInitializer extends ViewEntityInitializer
{
  // static members

  private static Logger log = Logger.getLogger(ScreenAndResultViewerEntityInitializer.class);
  

  // instance data members
  
  protected Screen _screen;
  protected ScreenResult _screenResult;


  // public constructors and methods

  public ScreenAndResultViewerEntityInitializer(DAO dao, Screen screen)
  {
    super(dao);
    _screen = screen;
  }
  
  protected void revivifyAndPrimeEntities()
  {
    //reattach(_screen);
    _screen = (Screen) reload(_screen);
    
    // TODO: HACK: makes screenResult access data-access-permissions aware 
    _screenResult = null;
    if (_screen.getScreenResult() != null) {
      _screenResult = (ScreenResult) reload(_screen.getScreenResult());
    }
    
    need(_screen.getAbaseTestsets());
    need(_screen.getAssayReadoutTypes());
    need(_screen.getHbnCollaborators());
    need(_screen.getAttachedFiles());
    need(_screen.getBillingInformation());
    need(_screen.getFundingSupports());
    need(_screen.getKeywords());
    need(_screen.getLettersOfSupport());
    need(_screen.getPublications());
    need(_screen.getStatusItems());
    need(_screen.getVisits());
    need(_screen.getLabHead());
    need(_screen.getLabHead().getLabMembers());
    need(_screen.getLeadScreener());
    need(_screen.getScreenResult());

    if (_screenResult != null) {
      need(_screenResult.getPlateNumbers());
      need(_screenResult.getResultValueTypes());
      need(_screenResult.getWells());
      for (ResultValueType rvt : _screenResult.getResultValueTypes()) {
        rvt.getDerivedTypes();
        rvt.getTypesDerivedFrom();
        //rvt.getResultValues(); // major performance hit!  fortunateyl, screenResultViewer is expressly designed to not use this
      }
    }
  }


  // private methods

}

