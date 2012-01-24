// $HeadURL:
// http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/lincs/ui-cleanup/core/src/main/java/edu/harvard/med/screensaver/io/screens/StudyAnnotationParser.java
// $
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.screens;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.harvard.med.screensaver.db.AbstractDAO;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.db.ScreenDAO;
import edu.harvard.med.screensaver.io.UnrecoverableParseException;
import edu.harvard.med.screensaver.io.workbook2.Cell;
import edu.harvard.med.screensaver.io.workbook2.Row;
import edu.harvard.med.screensaver.io.workbook2.Workbook;
import edu.harvard.med.screensaver.io.workbook2.Worksheet;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.AnnotationType;
import edu.harvard.med.screensaver.model.screens.Screen;

public class StudyAnnotationParser {
	private static Logger log = Logger.getLogger(StudyAnnotationParser.class);

	public static final String REGEX_SCRATCH_WORKSHEET_PATTERN = "^\\~.*";
	public static final Pattern FACILITY_SALT_BATCH_PATTERN = Pattern.compile("([^-]+)[-]([^-]+)[-]([^-]+)");

	private GenericEntityDAO _dao;
	private LibrariesDAO _librariesDao;
	private ScreenDAO _screenDao;

	public static enum KEY_COLUMN {
		RVI, WELL_ID, FACILITY_ID, COMPOUND_NAME;
	};

	/** @motivation for CGLIB2 */
	protected StudyAnnotationParser() {
	}

	public StudyAnnotationParser(GenericEntityDAO dao, LibrariesDAO librariesDao, ScreenDAO screenDao) {
		_dao = dao;
		_librariesDao = librariesDao;
		_screenDao = screenDao;
	}

	/**
	 * @param study
	 * @param parseLincsSpecificFacilityID
	 */
	@Transactional
	public void parse(Screen study, Workbook workbook, KEY_COLUMN keyColumn, boolean annotationTypeNamesInCol1,
			boolean parseLincsSpecificFacilityID) {
		if (workbook.getWorkbook() == null)
			throw new UnrecoverableParseException("Workbook not located: " + workbook.getName());
		try {
			workbook.getWorkbookSettings().setGCDisabled(true); // when GC feature is
																													// enabled,
																													// performance is much
																													// slower!

			if (annotationTypeNamesInCol1)
				parseAnnotationNamesInCol1(workbook, study);
			else
				parseAnnotationNamesInRow1(workbook, study);

			parseAnnotationValues(workbook, study, keyColumn, !annotationTypeNamesInCol1, parseLincsSpecificFacilityID);
		} catch (Exception e) {
			throw new UnrecoverableParseException(e);
		}
	}

	/**
	 * Traditional - AT's in columns, sheet 1 (limits to 256 AT's for excel <2007)
	 * row 1= name, row 2 = desc, row 3=type
	 * 
	 * @param workbook
	 * @param study
	 */
	private void parseAnnotationNamesInRow1(Workbook workbook, Screen study) {
		Worksheet sheet = workbook.getWorksheet(0);
		if (sheet == null)
			throw new UnrecoverableParseException("Sheet 0 not found, check if sheet/workbook exists: " + workbook.getName());

		Row annotationNames = sheet.getRow(0);
		for (int i = 0; i < annotationNames.getColumns(); i++) {
			new AnnotationType(study, sheet.getCell(i, 0, true).getString(), sheet.getCell(i, 1, true).getString(), i, sheet
					.getCell(i, 2, true).getBoolean());
		}
		log.info("found " + study.getAnnotationTypes().size() + " annotation types in row one of sheet: " + sheet.getName());
	}

	/**
	 * AT's in rows, col 1= name, col 2 = desc, col 3=type
	 */
	private void parseAnnotationNamesInCol1(Workbook workbook, Screen study) {
		Worksheet sheet = workbook.getWorksheet(0);
		if (sheet == null)
			throw new UnrecoverableParseException("Sheet 0 not found, check if sheet/workbook exists: " + workbook.getName());
		int ordinal = 0;
		for (Row row : sheet) {
			new AnnotationType(study, row.getCell(0, true).getString(), row.getCell(1, true).getString(), ordinal++, row
					.getCell(2, true).getBoolean());
		}
		log.info("found " + study.getAnnotationTypes().size() + " annotation types in column one of sheet: " + sheet.getName());
	}

	private void parseAnnotationValues(Workbook workbook, Screen study, KEY_COLUMN keyColumn, boolean reagentKeysInCol1,
			boolean parseLincsSpecificFacilityID) {
		List<AnnotationType> annotationTypes = Lists.newArrayList(study.getAnnotationTypes());

		Iterator<Worksheet> sheets = workbook.iterator();
		sheets.next();
		if (!sheets.hasNext())
			throw new UnrecoverableParseException("There is only one sheet! (no actual annotation value sheets)");
		int annotation_count = 0;
		while (sheets.hasNext()) {
			Worksheet sheet = sheets.next();
			if (sheet.getName().matches(REGEX_SCRATCH_WORKSHEET_PATTERN)) {
				log.warn("NOTE: Skipping sheet: \"" + sheet.getName() + "\" due to the REGEX_SCRATCH_WORKSHEET_PATTERN match: "
						+ REGEX_SCRATCH_WORKSHEET_PATTERN);
				continue;
			}
			if (reagentKeysInCol1) {
				annotation_count += parseAnnotationValuesWithReagentKeysInCol1(study, keyColumn, annotationTypes, sheet,
						parseLincsSpecificFacilityID);
			} else {
				annotation_count += parseAnnotationValuesWithReagentKeysInRow1(study, keyColumn, annotationTypes, sheet,
						parseLincsSpecificFacilityID);
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("created annotation types: " + Lists.transform(annotationTypes, new Function<AnnotationType, String>() {
				@Override
				public String apply(AnnotationType arg0) {
					return arg0.getName() + ": " + arg0.getAnnotationValues().size();
				}
			}));
		}

		log.info("populateStudyReagentLinkTable");
		int reagentCount = _screenDao.populateStudyReagentLinkTable(study.getScreenId());
		log.info("study saved, reagents linked to the study: " + reagentCount);

		log.info("created " + annotation_count + " annotation values");
	}

	/**
	 * annotation values in rows, well_id's/RVI's in col1 (traditional)
	 * 
	 * @param parseLincsSpecificFacilityID
	 */
	private int parseAnnotationValuesWithReagentKeysInCol1(Screen study, KEY_COLUMN keyColumn,
			List<AnnotationType> annotationTypes, Worksheet sheet, boolean parseLincsSpecificFacilityID)
			throws UnrecoverableParseException {
		int annotation_count = 0;
		int sheetAnnotationCount = study.getAnnotationTypes().size();
		long startTime = System.currentTimeMillis();
		long loopTime = startTime;
		
		log.info("begin parsing: " + sheet.getName());
		for (Row row : sheet) {
			Cell cell = row.getCell(0);
			String originalKey = cell.getString();
			String key = originalKey;
			if (StringUtils.isEmpty(originalKey)) {
				log.warn("Key in cell: " + cell + " is empty, skip the rest of the sheet: " + sheet);
				break;
			}

			if (parseLincsSpecificFacilityID && KEY_COLUMN.FACILITY_ID == keyColumn) {
				Well wellStudied = getWellStudied(originalKey);

				if (wellStudied == null) {
					throw new IllegalArgumentException("No canonical reagent well found for the input key: " + originalKey);
				}
				key = wellStudied.getFacilityId();

				if (row.getRow() == 0) {
					study.setWellStudied(wellStudied);
				} else {
					study.setWellStudied(null);
				}
			}

			Collection<Reagent> reagents = null;
			try {
				reagents = getReagents(keyColumn, key);
			} catch (UnrecoverableParseException e) {
				throw new UnrecoverableParseException("Key in cell: " + row.getCell(0) + " is invalid.", e);
			}

			for (Reagent reagent : reagents) {
				for (int iAnnot = 0; iAnnot < sheetAnnotationCount; ++iAnnot) {
					int iCol = iAnnot + 1;
					AnnotationType annotationType = annotationTypes.get(iAnnot);
					// Note: for memory performance, set populateStudyReagentLink to false
					// and do that at the end of the creation
					// TODO: consider making this a settable property, and/or,
					// consider making the Study.reagents link transient, and populate on
					// load so that this issue goes away - sde4
					boolean populateStudyReagentLink = false;
					annotationType.createAnnotationValue(reagent, row.getCell(iCol).getString(), populateStudyReagentLink);
					annotation_count++;
					if (annotation_count % (AbstractDAO.ROWS_TO_CACHE * 100) == 0) {
						long time = System.currentTimeMillis();
						long cumulativeTime = time - startTime;
						log.info("annotation count: " + annotation_count + ", cumulative time: " + (double) cumulativeTime
								/ (double) 60000 + " min, avg time per annotation: " + (double) cumulativeTime
								/ (double) annotation_count + ", loopTime: " + (time - loopTime));
						loopTime = time;
					}
				}
			}
		}
		log.info("sheet reagent columns read: " + sheet.getColumns() + ", annotations: " + annotation_count);

		// Note, as a result of memory optimization, it is unnecessary to explicitly
		// persist the study, since it is already persisted in the session,
		// and the reagents will be linked by the populateStudyReagentLinkTable -
		// sde4
		// _dao.mergeEntity(study);
		_dao.flush();
		return annotation_count;
	}

	/**
	 * annotation values in columns, well_id's/RVI's in column1, by rows
	 * 
	 * @param parseLincsSpecificFacilityID
	 */
	private int parseAnnotationValuesWithReagentKeysInRow1(Screen study, KEY_COLUMN keyColumn,
			List<AnnotationType> annotationTypes, Worksheet sheet, boolean parseLincsSpecificFacilityID)
			throws UnrecoverableParseException {
		int annotation_count = 0;
		int sheetAnnotationCount = study.getAnnotationTypes().size();
		long startTime = System.currentTimeMillis();
		long loopTime = startTime;

		log.info("begin parsing: " + sheet.getName() );
		for (int i = 0; i < sheet.getColumns(); i++) {
			Cell cell = sheet.getCell(i, 0, true);
			String originalKey = cell.getString();
			String key = originalKey;
			if (StringUtils.isEmpty(key)) {
				log.warn("Key in cell: " + cell + " is empty, skip the rest of the sheet: " + sheet);
				break;
			}

			if (parseLincsSpecificFacilityID && KEY_COLUMN.FACILITY_ID == keyColumn) {
				Well wellStudied = getWellStudied(originalKey);

				if (wellStudied == null) {
					throw new IllegalArgumentException("No canonical reagent well found for the input key: " + originalKey);
				}
				key = wellStudied.getFacilityId();

				if (i == 0) {
					study.setWellStudied(wellStudied);
				} else {
					study.setWellStudied(null);
				}
			}

			Collection<Reagent> reagents = null;
			try {
				reagents = getReagents(keyColumn, key);
			} catch (UnrecoverableParseException e) {
				throw new UnrecoverableParseException("WellKey in cell: " + sheet.getCell(i, 0) + " is invalid.", e);
			}

			for (Reagent reagent : reagents) {
				for (int iAnnot = 0; iAnnot < sheetAnnotationCount; ++iAnnot) {
					int iRow = iAnnot + 1;
					AnnotationType annotationType = annotationTypes.get(iAnnot);
					// Note: for memory performance, set populateStudyReagentLink to false
					// and do that at the end of the creation
					// TODO: consider making this a settable property, and/or,
					// consider making the Study.reagents link transient, and populate on
					// load so that this issue goes away - sde4
					boolean populateStudyReagentLink = false;
					annotationType.createAnnotationValue(reagent, sheet.getCell(i, iRow).getString(), populateStudyReagentLink);
					annotation_count++;
					if (annotation_count % (AbstractDAO.ROWS_TO_CACHE * 100) == 0) {
						long time = System.currentTimeMillis();
						long cumulativeTime = time - startTime;
						log.info("annotation count: " + annotation_count + ", cumulative time: " + (double) cumulativeTime
								/ (double) 60000 + " min, avg time per annotation: " + (double) cumulativeTime
								/ (double) annotation_count + ", loopTime: " + (time - loopTime));
						loopTime = time;
					}
				}
			}
		}
		log.info("sheet reagent columns read: " + sheet.getColumns() + ", annotations: " + annotation_count);

		// Note, as a result of memory optimization, it is unnecessary to explicitly
		// persist the study, since it is already persisted in the session,
		// and the reagents will be linked by the populateStudyReagentLinkTable -
		// sde4
		// _dao.mergeEntity(study);
		_dao.flush();
		return annotation_count;
	}

	private Well getWellStudied(String originalKey) throws IllegalArgumentException {
		Well wellStudied;
		Matcher matcher = FACILITY_SALT_BATCH_PATTERN.matcher(originalKey);
		if (matcher.matches()) {
			String facilityId = matcher.group(1);
			log.info("Found facility id: " + facilityId + " in the key: " + originalKey);
			Integer saltId = null;
			try {
				saltId = Integer.parseInt(matcher.group(2));
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Salt ID must be an integer: " + originalKey);
			}
			Integer batchId = null;
			try {
				batchId = Integer.parseInt(matcher.group(3));
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Batch ID must be an integer: " + originalKey);
			}

			wellStudied = _librariesDao.findCanonicalReagentWell(facilityId, saltId, batchId);
		} else {
			log.info("no specific key identified for: " + originalKey);
			wellStudied = _librariesDao.findCanonicalReagentWell(originalKey, null, null);
		}
		return wellStudied;
	}

	private Collection<Reagent> getReagents(KEY_COLUMN keyColumn, String key) throws UnrecoverableParseException {
		Collection<Reagent> reagents = Lists.newArrayList();

		switch (keyColumn) {
		case RVI:
			ReagentVendorIdentifier rvi = new ReagentVendorIdentifier(key.split(":", 2)[0], key.split(":", 2)[1]);
			Map<String, Object> queryProps = Maps.newHashMap();
			queryProps.put(Reagent.vendorIdentifier.getPath(), rvi.getVendorIdentifier());
			queryProps.put(Reagent.vendorName.getPath(), rvi.getVendorName());
			reagents = _dao.findEntitiesByProperties(Reagent.class, queryProps);

			// NOTE: removing the eager fetching of relationships, because:
			// Reagent-Study link will be done by ScreenDAO.populateReagentStudyLink,
			// and
			// AnnotationType->annotationValues (Map[Reagent,AnnotationValue]) is a
			// one-to-many relationship, managed by the AV, and
			// Reagent->annotationValues (Map[AnnotationType,AnnotationValue]) is also
			// a one-to-many reln, also managed by the AV
			// (so AV is essentially the link table)
			// reagents = _dao.findEntitiesByProperties(Reagent.class,
			// queryProps,
			// true,
			// Reagent.annotationValues);
			break;
		case FACILITY_ID:
			List<Well> wells = _dao.findEntitiesByProperty(Well.class, "facilityId", key);
			for (Well well : wells) {
				reagents.addAll(well.getReagents().values());
			}
			break;
		case WELL_ID:
			WellKey wellKey = new WellKey(key);
			Well well = _dao.findEntityById(Well.class, wellKey.toString(), true, Well.reagents);
			if (well == null) {
				throw new UnrecoverableParseException("No such well with identifier: " + wellKey);
			}
			reagents = well.getReagents().values();
			break;
		case COMPOUND_NAME: // SM only
			reagents.addAll(_dao.findEntitiesByHql(SmallMoleculeReagent.class,
					"from SmallMoleculeReagent where ? in elements (compoundNames)", key));

			break;
		default:
			throw new IllegalArgumentException("Unknown key column for the StudyAnnotationParser: " + keyColumn);
		}

		if (reagents.isEmpty()) {
			throw new UnrecoverableParseException("no such " + keyColumn + " identifier found: " + key);
		}
		log.info("found " + reagents.size() + " reagents for the identifier: " + key);
		return reagents;
	}

}
