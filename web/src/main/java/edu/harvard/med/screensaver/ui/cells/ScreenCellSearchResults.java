// $HeadURL: http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/serickson/3200/web/src/main/java/edu/harvard/med/screensaver/ui/libraries/AnnotationSearchResults.java $
// $Id: AnnotationSearchResults.java 6949 2012-01-13 19:00:59Z seanderickson1 $

// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.

// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.cells;

import java.util.List;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.datafetcher.EntityDataFetcher;
import edu.harvard.med.screensaver.model.cells.ExperimentalCellInformation;
import edu.harvard.med.screensaver.model.meta.RelationshipPath;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.column.entity.TextEntityColumn;
import edu.harvard.med.screensaver.ui.arch.datatable.model.InMemoryEntityDataModel;
import edu.harvard.med.screensaver.ui.arch.searchresults.EntityBasedEntitySearchResults;
import edu.harvard.med.screensaver.ui.screens.ScreenViewer;
import edu.harvard.med.screensaver.ui.screens.StudyViewer;

public class ScreenCellSearchResults extends EntityBasedEntitySearchResults<ExperimentalCellInformation, Integer> {
	private static final Logger log = Logger.getLogger(ScreenCellSearchResults.class);
	private GenericEntityDAO _dao;
	private ScreenViewer _screenViewer;
	private CellViewer _cellViewer;
	private StudyViewer _studyViewer;

	/**
	 * @motivation for CGLIB2
	 */
	protected ScreenCellSearchResults() {
	}

	public ScreenCellSearchResults(ScreenCellViewer screenCellViewer, ScreenViewer screenViewer, StudyViewer studyViewer, CellViewer cellViewer, GenericEntityDAO dao) {
		super(screenCellViewer);

		_dao = dao;
		_screenViewer = screenViewer;
		_studyViewer = studyViewer;
		_cellViewer = cellViewer;
	}

	@Override
	public void initialize() {
		super.initialize();
	}

	public void searchAll() {
		EntityDataFetcher<ExperimentalCellInformation, Integer> dataFetcher = (EntityDataFetcher<ExperimentalCellInformation, Integer>) new EntityDataFetcher<ExperimentalCellInformation, Integer>(
				ExperimentalCellInformation.class, _dao);
		initialize(new InMemoryEntityDataModel<ExperimentalCellInformation, Integer, ExperimentalCellInformation>(dataFetcher));

		getColumnManager().setSortAscending(false);
	}

	// implementations of the SearchResults abstract methods

	@Override
	protected List<TableColumn<ExperimentalCellInformation, ?>> buildColumns() {
		List<TableColumn<ExperimentalCellInformation, ?>> columns = Lists.newArrayList();
		
		columns.add(new TextEntityColumn<ExperimentalCellInformation>(RelationshipPath.from(ExperimentalCellInformation.class).toProperty("screen"), "Screen or Study",
				"The screen facility id", TableColumn.UNGROUPED) {
			@Override
			public String getCellValue(ExperimentalCellInformation info) {
				return info.getScreen().getFacilityId();
//				if(info.getScreen() != null ) return info.getScreen().getFacilityId();
//				if(info.getStudy() != null ) return info.getStudy().getFacilityId();
//				else throw new RuntimeException("ExperimentalCellInformation not linked to a screen or study! " + info);
			}
			@SuppressWarnings("unchecked")
			@Override
			public Object cellAction(ExperimentalCellInformation info) {
				if(!info.getScreen().isStudyOnly() ) return _screenViewer.viewEntity(info.getScreen());
				else return _studyViewer.viewEntity(info.getScreen());
//				if(info.getStudy() != null ) return _studyViewer.viewEntity(info.getStudy());
//				else throw new RuntimeException("ExperimentalCellInformation not linked to a screen or study! " + info);
			}

			@Override
			public boolean isCommandLink() {
				return true;
			}
		});
		columns.get(columns.size() - 1).setVisible(true);

		columns.get(columns.size() - 1).setVisible(true);		columns.add(new TextEntityColumn<ExperimentalCellInformation>(RelationshipPath.from(ExperimentalCellInformation.class).toProperty("screen"), "Screen Title",
				"The screen title", TableColumn.UNGROUPED) {
			@Override
			public String getCellValue(ExperimentalCellInformation info) {
				return info.getScreen().getTitle();
//				if(info.getScreen() != null ) return info.getScreen().getTitle();
//				if(info.getStudy() != null ) return info.getStudy().getTitle();
//				else throw new RuntimeException("ExperimentalCellInformation not linked to a screen or study! " + info);
			}
		});
		columns.get(columns.size() - 1).setVisible(true);

		// TODO: show some information about the screen, since this will be displayed in the cell viewer
		columns.add(new TextEntityColumn<ExperimentalCellInformation>(RelationshipPath.from(ExperimentalCellInformation.class).toProperty("cell"), "Facility ID",
				"ID assigned to this cell by the HMS LINCS facility", TableColumn.UNGROUPED) {
			@Override
			public String getCellValue(ExperimentalCellInformation info) {
				return info.getCell().getFacilityId();
			}
			@SuppressWarnings("unchecked")
			@Override
			public Object cellAction(ExperimentalCellInformation info) {
				return _cellViewer.viewEntity(info.getCell());
			}

			@Override
			public boolean isCommandLink() {
				return true;
			}
		});
		columns.get(columns.size() - 1).setVisible(true);
		
		columns.add(new TextEntityColumn<ExperimentalCellInformation>(RelationshipPath.from(ExperimentalCellInformation.class).toProperty("cell"), "Cell Name",
				"The cell name", TableColumn.UNGROUPED) {
			@Override
			public String getCellValue(ExperimentalCellInformation info) {
				return info.getCell().getName();
			}
		});
		columns.get(columns.size() - 1).setVisible(true);
		
		// TODO: show whatever Experimental Cell Information properties there may be

		return columns;
	}

}