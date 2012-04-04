package edu.harvard.med.screensaver.db;

import java.util.Set;
import java.util.SortedSet;

import edu.harvard.med.screensaver.model.cells.Cell;
import edu.harvard.med.screensaver.model.cells.ExperimentalCellInformation;
import edu.harvard.med.screensaver.model.libraries.WellKey;

public interface CellsDAO {

	SortedSet<ExperimentalCellInformation> findCellExperimentsFromCLOIds(String[] split);

	Set<Cell> findCellsByCloIds(String[] cloIds);

	Set<Cell> findCellsByHMSID(String[] split);
	
	Set<String> findCanonicalCompoundsScreenedByWellId(Cell cell); 

}
