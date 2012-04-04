package edu.harvard.med.screensaver.io.cells;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.SortedSet;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.io.ParseError;
import edu.harvard.med.screensaver.io.ParseException;
import edu.harvard.med.screensaver.io.UnrecoverableParseException;
import edu.harvard.med.screensaver.model.cells.Cell;
import edu.harvard.med.screensaver.model.cells.CellLineage;
import edu.harvard.med.screensaver.model.cells.PrimaryCell;

public class CellParser
{
	private static final Logger log = Logger.getLogger(CellParser.class);

	private GenericEntityDAO _dao;
	public static String DELIMITER_INSIDE_FIELD = ";";
	public static String DELIMITER_FIELD = ",";

	private static int i = 0;
	// public static final int COL_TYPE = i++;
	public static final int COL_FACILITY_ID = i++; // Added HMS column, not in the
																									// DWG Standards
	// CL:1 CL_Name
	public static final int COL_NAME = i++;
	public static final int COL_CLOID = i++;
	
	public static final int COL_ALTERNATE_NAME = i++;
	public static final int COL_ALTERNATE_ID = i++;
	public static final int COL_CENTER_NAME = i++;
	public static final int COL_CENTER_SPECIFIC_ID = i++;

	public static final int COL_VENDOR = i++;
	public static final int COL_VENDOR_CAT = i++;
	public static final int COL_BATCH_ID = i++;
	public static final int COL_ORGANISM = i++;
	
	public static final int COL_ORGAN = i++;
	
	public static final int COL_TISSUE = i++;
	public static final int COL_CELL_TYPE = i++;
	public static final int COL_DISEASE = i++;
	public static final int COL_GROWTH_PROPERTIES = i++;
	public static final int COL_GENETIC_MOD = i++;
	public static final int COL_RELATED_PROJECTS = i++;
	public static final int COL_REC_CULTURE_COND = i++;
	public static final int COL_VERIFICATION = i++;
	public static final int COL_MUTATIONS = i++;
	public static final int COL_ORGANISM_GENDER = i++;
	// this column marks the end of the generic columns
	public static final int COLUMN_COUNT_MIN = COL_ORGANISM_GENDER + 1;

	// These are for primary cells
	public static final int COL_DONOR_ETHNICITY = i++;
	public static final int COL_AGE_IN_YEARS = i++;
	public static final int COL_DONOR_HEALTH_STATUS = i++;
	public static final int COL_CELL_MARKERS = i++;
	public static final int COL_PASSAGE_NUMBER = i++;

	private int[] columnPositions = new int[i];
	
	protected CellParser() {}
	
	public CellParser(GenericEntityDAO dao) {
		_dao = dao;
	}

	@Transactional(rollbackForClassName="Exception")
	public void load(File file) throws ParseException, IOException 
	{
		log.info("begin parsing");
		WorksheetReader reader = new WorksheetReader(file);
		String[] line = reader.parseNext(); // Header Row, blanks indicate non-LINCS columns
		parseColumnPositions(line); // determine where the blanks are, exclude these columns

		reader.parseNext(); // second row is not used programmatically

		while ((line = reader.parseNext()) != null) {
			if (line.length < columnPositions.length) {
				log.info("warn, line is short: " + Joiner.on(",").join(line) + ", padding");
				line = Arrays.copyOf(line, columnPositions.length);
			}
			try {
				Cell cell = parse(line);
				_dao.persistEntity(cell);
				_dao.flush();
				_dao.clear(); // flush and clear so errors are reported early
			} catch (ParseException e) {
				e.getError().setErrorLocation("at line: " + reader.getLinesRead());
				throw e;
			}
		}
		log.info("rows read: " + reader.getLinesRead());
	}
	
	/*
	 * Re-index the columns, ignoring the columns with empty headers.
	 */
	private void parseColumnPositions(String[] line) {
		int j = 0;
		log.info("headers: " + Joiner.on(",").join(line));
		for (int i = 0; i < line.length; i++) {
			if (StringUtils.isEmpty(line[i])) {
				continue;
			} else {
				if (j > columnPositions.length - 1)
					throw new UnrecoverableParseException("Unrecognized column: " + line[i] + ", only " + columnPositions.length
							+ ", column positions are defined.  Please delete this column, or define more columns to be parsed.");
				columnPositions[j] = i;
				j++;
			}
		}
		log.info("parsed: " + j + " columns from line: " + Joiner.on(",").join(line) + ", " + line.length);
	}

	private int getColumnForField(int i) {
		if (i > columnPositions.length - 1)
			new UnrecoverableParseException("Unrecognized column: " + i + ", only " + columnPositions.length
					+ ", column positions are defined.  Please delete this column, or define more columns to be parsed.");
		return columnPositions[i];
	}
	public Cell parse(String[] fields) throws ParseException {
		// CellLineType type = (new
		// VocabularyTermParser<CellLineType>(CellLineType.class)).forValue(fields[COL_TYPE]);
		// if (type == null)
		// throw new ParseException(new
		// ParseError("unknown cell line type, allowed values: "
		// + Joiner.on(",").join(CellLineType.values()) + ", found: " +
		// fields[COL_TYPE], COL_TYPE));
		Cell cell = null;

		// TODO: will use a command line switch to determine type
		// switch (type) {
		// case LINE:
		cell = new CellLineage();
		parseCellLineage(cell, fields);
		// break;
		// case PRIMARY:
		// cell = new PrimaryCell();
		// parseCellLineage(cell, fields);
		// parsePrimaryCell((PrimaryCell) cell, fields);
		// break;
		// }

		return cell;
	}

	public void parseCellLineage(Cell cell, String[] fields) throws ParseException {
		cell.setFacilityId(fields[getColumnForField(COL_FACILITY_ID)]);
		cell.setName(fields[getColumnForField(COL_NAME)]);
		cell.setCloId(fields[getColumnForField(COL_CLOID)]);
		cell.setAlternateName(fields[getColumnForField(COL_ALTERNATE_NAME)]);
		cell.setAlternateId(fields[getColumnForField(COL_ALTERNATE_ID)]);
		cell.setCenterName(fields[getColumnForField(COL_CENTER_NAME)]);
		cell.setCenterSpecificId(fields[getColumnForField(COL_CENTER_SPECIFIC_ID)]);
		cell.setVendor(fields[getColumnForField(COL_VENDOR)]);
		cell.setVendorCatalogId(fields[getColumnForField(COL_VENDOR_CAT)]);
		cell.setBatchId(fields[getColumnForField(COL_BATCH_ID)]);
		cell.setOrganism(fields[getColumnForField(COL_ORGANISM)]);
		cell.setOrgan(fields[getColumnForField(COL_ORGAN)]);
		cell.setTissue(fields[getColumnForField(COL_TISSUE)]);
		cell.setCellType(fields[getColumnForField(COL_CELL_TYPE)]);
		cell.setDisease(fields[getColumnForField(COL_DISEASE)]);
		String temp = fields[getColumnForField(COL_GROWTH_PROPERTIES)];
		if (!StringUtils.isEmpty(temp)) {
			cell.setGrowthProperties(Sets.newTreeSet(Lists.newArrayList(temp.split(DELIMITER_INSIDE_FIELD))));
		}
		cell.setGeneticModification(fields[getColumnForField(COL_GENETIC_MOD)]);
		temp = fields[getColumnForField(COL_RELATED_PROJECTS)];
		if (!StringUtils.isEmpty(temp)) {
			cell.setRelatedProjects(Sets.newTreeSet(Lists.newArrayList(temp.split(DELIMITER_INSIDE_FIELD))));
		}
		cell.setVerification(fields[getColumnForField(COL_VERIFICATION)]);
		cell.setRecommendedCultureConditions(fields[getColumnForField(COL_REC_CULTURE_COND)]);
		cell.setMutations(fields[getColumnForField(COL_MUTATIONS)]);
		cell.setOrganismGender(fields[getColumnForField(COL_ORGANISM_GENDER)]);
	}

	public void parsePrimaryCell(PrimaryCell cell, String[] fields) throws ParseException {
		cell.setDonorEthnicity(fields[getColumnForField(COL_DONOR_ETHNICITY)]);
		try {
			cell.setAgeInYears(Integer.parseInt(fields[getColumnForField(COL_AGE_IN_YEARS)]));
		} catch (NumberFormatException e) {
			throw new ParseException(new ParseError("age must be an integer: " + fields[getColumnForField(COL_AGE_IN_YEARS)],
					COL_AGE_IN_YEARS));
		}
		cell.setDonorHealthStatus(fields[getColumnForField(COL_DONOR_HEALTH_STATUS)]);
		SortedSet<String> temp = Sets.newTreeSet(Lists.newArrayList(fields[getColumnForField(COL_CELL_MARKERS)]
				.split(DELIMITER_INSIDE_FIELD)));
		cell.setCellMarkers(temp);
		try {
			cell.setPassageNumber(Integer.parseInt(fields[getColumnForField(COL_PASSAGE_NUMBER)]));
		} catch (NumberFormatException e) {
			throw new ParseException(new ParseError("Passage Number must be an integer: "
					+ fields[getColumnForField(COL_PASSAGE_NUMBER)], COL_PASSAGE_NUMBER));
		}

	}
}
