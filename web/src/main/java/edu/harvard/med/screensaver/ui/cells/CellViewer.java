// $HeadURL: http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/serickson/3200/web/src/main/java/edu/harvard/med/screensaver/ui/libraries/LibraryViewer.java $
// $Id: LibraryViewer.java 6946 2012-01-13 18:24:30Z seanderickson1 $
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.cells;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.harvard.med.screensaver.db.CellsDAO;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.datafetcher.DataFetcherUtil;
import edu.harvard.med.screensaver.db.datafetcher.EntityDataFetcher;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.model.cells.Cell;
import edu.harvard.med.screensaver.model.cells.ExperimentalCellInformation;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.ui.arch.datatable.model.InMemoryEntityDataModel;
import edu.harvard.med.screensaver.ui.arch.view.SearchResultContextEntityViewerBackingBean;
import edu.harvard.med.screensaver.ui.libraries.WellSearchResults;
import edu.harvard.med.screensaver.util.Pair;

/**
 */
public class CellViewer extends SearchResultContextEntityViewerBackingBean<Cell, Cell> 
{
	private static Logger log = Logger.getLogger(CellViewer.class);

	private CellsDAO _cellsDao;
//	private ScreenCellSearchResults _screenCellSearchResults;
	private DataModel _reagentsScreened;

	private Set<String> _wellsScreened;
	private WellSearchResults _wellSearchResults;

	private Cell _wellSearchCell;

	private DataModel _screensAndStudiesDataModel;

	/**
	 * @motivation for CGLIB2
	 */
	protected CellViewer() {
	}

	public CellViewer(CellViewer thisProxy, CellSearchResults cellSearchResults, WellSearchResults wellSearchResults, ScreenCellSearchResults screenCellSearchResults, GenericEntityDAO dao, CellsDAO cellsDao) {
		super(thisProxy, Cell.class, BROWSE_CELLS, VIEW_CELL, dao, cellSearchResults);
		_cellsDao = cellsDao;
//		_screenCellSearchResults = screenCellSearchResults;
		_wellSearchResults = wellSearchResults;
		getIsPanelCollapsedMap().put("cellPanel", false);
		getIsPanelCollapsedMap().put("reagentsScreened", true );
		getIsPanelCollapsedMap().put("screensForCellPanel", true);
		getIsPanelCollapsedMap().put("screensAndStudies", true);
	}

	@Override
	protected void initializeEntity(Cell cell) {
		getDao().needReadOnly(cell, Cell.experimentalCellInformationSetPath);
	}

	@Override
	protected void initializeViewer(final Cell cell) {
//		_screenCellSearchResults
//				.initialize(new InMemoryEntityDataModel<ExperimentalCellInformation, Integer, ExperimentalCellInformation>(
//						new EntityDataFetcher<ExperimentalCellInformation, Integer>(ExperimentalCellInformation.class, getDao()) {
//							@Override
//							public void addDomainRestrictions(HqlBuilder hql) {
//								DataFetcherUtil.addDomainRestrictions(hql, ExperimentalCellInformation.cellProperty, cell,
//										getRootAlias());
//							}
//						}));
		_wellSearchCell = null;
		_wellsScreened = _cellsDao.findCanonicalCompoundsScreenedByWellId(cell);
		_screensAndStudiesDataModel = null;
	}
//
//	public ScreenCellSearchResults getScreenCellSearchResults() {
//		return _screenCellSearchResults;
//	}
	
	public WellSearchResults getWellSearchResults()
	{
		if(_wellSearchCell == null && getEntity() != null && _wellsScreened != null)
		{
			// TODO: could reimplement wsr to show screens.
			_wellSearchCell = getEntity();
			 _wellSearchResults.searchWellByWellId(_wellsScreened, "Compounds screened with cell: " +_wellSearchCell.getFacilityId(),
						Sets.newHashSet(ScreenType.SMALL_MOLECULE));
		}
		 return _wellSearchResults;
	}

	
	public boolean getHasReagents()
	{
		return _wellsScreened != null && !_wellsScreened.isEmpty();
	}
	
	public boolean getHasScreens()
	{
		return (getEntity() != null) && ! getEntity().getExperimentalCellInformationSet().isEmpty();
	}
	
	public DataModel getScreensAndStudiesDataModel()
	{
		if(_screensAndStudiesDataModel == null)
		{
			List<Pair<Screen,List<Cell>>> screenCellList = Lists.newArrayList();
	  	// TODO, find out why the fetched cells.getExperimentalInformationSet only ever returns the first attached screen
	  	Set<ExperimentalCellInformation> ecis = Sets.newHashSet(getDao().findEntitiesByProperty(ExperimentalCellInformation.class, "cell", getEntity()));
			for(ExperimentalCellInformation eci:ecis) {
				if(eci.getScreen() != null) 
				{
					Screen s = eci.getScreen();
					Set<ExperimentalCellInformation> otherEcis = Sets.newHashSet(getDao().findEntitiesByProperty(ExperimentalCellInformation.class, "screen", s));;
			  	List<Cell> cells = Lists.newArrayList();
					for(ExperimentalCellInformation eci1:otherEcis) {
						cells.add(eci1.getCell());
					}
					Collections.sort(cells);
					screenCellList.add(new Pair<Screen,List<Cell>>(s, cells));
//				}else {
//					Screen s = eci.getStudy();
//					Set<ExperimentalCellInformation> otherEcis = Sets.newHashSet(getDao().findEntitiesByProperty(ExperimentalCellInformation.class, "study", s));;
//			  	List<Cell> cells = Lists.newArrayList();
//					for(ExperimentalCellInformation eci1:otherEcis) {
//						cells.add(eci1.getCell());
//					}
//					Collections.sort(cells);
//					screenCellList.add(new Pair<Screen,List<Cell>>(s, cells));
				}
			}
			Collections.sort(screenCellList, new Comparator<Pair<Screen,List<Cell>>>() {

				@Override
				public int compare(Pair<Screen,List<Cell>> o1, Pair<Screen,List<Cell>> o2) {
					return o1.getFirst().getFacilityId().compareTo(o2.getFirst().getFacilityId());
				}});
			_screensAndStudiesDataModel = new ListDataModel(screenCellList);
		}
		return _screensAndStudiesDataModel;
	}
	@Override
	public String viewEntity() {
		// TODO Auto-generated method stub
		return super.viewEntity();
	}
}
