// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.screens;

import java.util.Collection;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.UsersDAO;
import edu.harvard.med.screensaver.db.datafetcher.DataFetcherUtil;
import edu.harvard.med.screensaver.db.datafetcher.EntityDataFetcher;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.model.cells.Cell;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.Study;
import edu.harvard.med.screensaver.ui.arch.datatable.model.InMemoryEntityDataModel;
import edu.harvard.med.screensaver.ui.arch.view.EditResult;
import edu.harvard.med.screensaver.ui.cells.CellSearchResults;

public class StudyDetailViewer extends AbstractStudyDetailViewer<Study>
{
  private static Logger log = Logger.getLogger(ScreenDetailViewer.class);

	private CellSearchResults _cellSearchResults;

	/**
   * @motivation for CGLIB2
   */
  protected StudyDetailViewer()
  {
  }

  public StudyDetailViewer(StudyDetailViewer thisProxy,
                           StudyViewer studyViewer,
                           GenericEntityDAO dao,
                           UsersDAO usersDao,
                           CellSearchResults cellSearchResults)
  {
    super(thisProxy, dao, ScreensaverConstants.VIEW_STUDY, usersDao);
    _cellSearchResults = cellSearchResults;
    getIsPanelCollapsedMap().put("cellsForScreen", false);
  }
  
  @Override
  protected void initializeViewer(final Study entity) {
  	super.initializeViewer(entity);
    if(isLINCS())
    {
  		_cellSearchResults.initialize(new InMemoryEntityDataModel<Cell, Integer, Cell>(
				new EntityDataFetcher<Cell, Integer>(Cell.class, getDao()) {
					@Override
					public void addDomainRestrictions(HqlBuilder hql) {
						DataFetcherUtil.addDomainRestrictions(hql, Cell.experimentalCellInformationSetPath.to("screen"), entity,	getRootAlias());
					}
				}));
    
    	getDao().needReadOnly(((Screen)entity), Screen.experimentalCellInfomationSet);
    }
  }
  
  @Override
  public boolean isEditable()
  {
    return false;
  }

  @Override
  protected String postEditAction(EditResult editResult)
  {
    return null;
  }
  
  
  public CellSearchResults getCellSearchResults()
  {
  	return _cellSearchResults;
  }
  
  public boolean getHasCells()
  {
  	Collection c = ((Screen)getEntity()).getExperimentalCellInformationSet();
  	return c == null ? false : !c.isEmpty();
  }

  
}

