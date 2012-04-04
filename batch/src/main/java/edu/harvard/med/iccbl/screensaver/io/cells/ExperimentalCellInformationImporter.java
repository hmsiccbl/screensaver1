// $HeadURL: http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/serickson/3200/batch/src/main/java/edu/harvard/med/iccbl/screensaver/io/libraries/MasterStockPlateMappingLoader.java $
// $Id: MasterStockPlateMappingLoader.java 6949 2012-01-13 19:00:59Z seanderickson1 $
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.io.cells;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.apache.commons.cli.OptionBuilder;
import org.apache.log4j.Logger;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.harvard.med.screensaver.db.CellsDAO;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.ScreenDAO;
import edu.harvard.med.screensaver.io.CommandLineApplication;
import edu.harvard.med.screensaver.io.ParseError;
import edu.harvard.med.screensaver.io.ParseException;
import edu.harvard.med.screensaver.io.cells.CellParser;
import edu.harvard.med.screensaver.io.cells.ExperimentalCellInformationParser;
import edu.harvard.med.screensaver.io.cells.WorksheetReader;
import edu.harvard.med.screensaver.model.cells.Cell;
import edu.harvard.med.screensaver.model.cells.ExperimentalCellInformation;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.util.StringUtils;

/**
 * Import the information defining and annotating the relationship between a LINCS [Experiment(Screen) or a LINCS Study] and a Cell [Cell Line, Primary Cell..].</br>
 * Phase1: link from one screen to N cells.<br/>
 * Phase 2: link from screen to cell, N times, annotating each link as specified.<br/>
 * <br/>
 * This importer maintains the ExperimentalCellInformation relationship class.<br/>
 */
public class ExperimentalCellInformationImporter extends CommandLineApplication {
	private static final Logger log = Logger.getLogger(ExperimentalCellInformationImporter.class);

	public static void main(String[] args) {
		try {
			final ExperimentalCellInformationImporter app = new ExperimentalCellInformationImporter(args);
			app.addCommandLineOption(OptionBuilder.isRequired().hasArg(true).withArgName("file").withLongOpt("input-file")
					.withDescription("The path of a CSV file containing the Cell Information").create("f"));
			//			app.addCommandLineOption(OptionBuilder.hasArg(false).withLongOpt("delete-existing")
			//					.withDescription("Delete existing ExperimentalCellInformation").create("de"));
			app.processOptions(true, false);
			File file = app.getCommandLineOptionValue("f", File.class);
			final ExperimentalCellInformationParser parser = (ExperimentalCellInformationParser) app.getSpringBean("experimentalCellInformationParser");

			parser.load(file);
    }
    catch (Exception e) {
      log.error("application exception", e);
      System.exit(1);
    }
	}

	@SuppressWarnings("static-access")
	public ExperimentalCellInformationImporter(String[] args) {
		super(args);
	}

}
