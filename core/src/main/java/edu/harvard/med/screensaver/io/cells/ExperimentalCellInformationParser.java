package edu.harvard.med.screensaver.io.cells;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.harvard.med.screensaver.db.CellsDAO;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.ScreenDAO;
import edu.harvard.med.screensaver.io.ParseError;
import edu.harvard.med.screensaver.io.ParseException;
import edu.harvard.med.screensaver.model.cells.Cell;
import edu.harvard.med.screensaver.model.cells.ExperimentalCellInformation;
import edu.harvard.med.screensaver.model.screens.Screen;

/**
 * Import the information defining and annotating the relationship between a LINCS [Experiment(Screen) or a LINCS Study]
 * and a Cell [Cell Line, Primary Cell..].</br> Phase1: link from one screen to N cells.<br/>
 * Phase 2: link from screen to cell, N times, annotating each link as specified.<br/>
 * <br/>
 * This importer maintains the ExperimentalCellInformation relationship class.<br/>
 */
public class ExperimentalCellInformationParser {
	private static final Logger log = Logger.getLogger(ExperimentalCellInformationParser.class);

	private GenericEntityDAO _dao;
	private ScreenDAO _screenDao;
	private CellsDAO _cellsDao;
	
	protected ExperimentalCellInformationParser() {}

	public ExperimentalCellInformationParser(GenericEntityDAO dao, ScreenDAO screenDao, CellsDAO cellsDao) {
		_dao = dao;
		_screenDao = screenDao;
		_cellsDao = cellsDao;
	}

	@Transactional(rollbackForClassName="Exception")
	public void load(final File file) throws FileNotFoundException, ParseException {
		log.info("begin parsing");
		Function<Screen, String> screenToFacilityId = new Function<Screen, String>() {
			public String apply(Screen from) {
				return from.getFacilityId();
			}
		};
		Map<String, Screen> screensByFacilityID = Maps.uniqueIndex(_screenDao.findAllScreens(), screenToFacilityId);
		Map<String, Screen> studiesByFacilityID = Maps.uniqueIndex(_screenDao.findAllStudies(), screenToFacilityId);

		WorksheetReader reader = new WorksheetReader(file);
		String[] line = reader.parseNext();

		while ((line = reader.parseNext()) != null) {
			String screenFacilityId = line[0];
			String temp = line[1];
			// TODO: verify columns are in the right locations (someday)
			if (StringUtils.isEmpty(temp)) {
				 // TODO, if re-loading incrementally, this could be used to signal deletion of current connections.
				log.warn("no cells defined for " + screenFacilityId);
			}
			String[] cellIds = line[1].split(",");
			Set<Cell> cells = _cellsDao.findCellsByHMSID(cellIds);
			if (cells.size() < cellIds.length) {
				throw new ParseException(new ParseError("Number of cells found: " + cells.size()
						+ ", is less than number specified:  " + cellIds.length + ": ids: " + temp + ", cells: " + cells,
						"at line " + reader.getLinesRead()));
			}
			if (screensByFacilityID.containsKey(screenFacilityId)) {
				Screen screen = screensByFacilityID.get(screenFacilityId);
				SortedSet<ExperimentalCellInformation> ecis = Sets.newTreeSet();
				for (Cell cell : cells) {
					ecis.add(new ExperimentalCellInformation(cell,screen));
				}
				screen.setExperimentalCellInformationSet(ecis);
			} else if (studiesByFacilityID.containsKey(screenFacilityId)) {  //TODO: it has become unnecessary to split this into screens/studies
				Screen study = studiesByFacilityID.get(screenFacilityId);
				SortedSet<ExperimentalCellInformation> ecis = Sets.newTreeSet();
				for (Cell cell : cells) {
					ecis.add(new ExperimentalCellInformation(cell,study));
				}
				study.setExperimentalCellInformationSet(ecis);
			} else {
				throw new ParseException(new ParseError("Neither Screen nor Study found for: \"" + screenFacilityId + "\"",
						"at line " + reader.getLinesRead()));
			}
		}
		log.info("rows read: " + reader.getLinesRead());
	}
}
