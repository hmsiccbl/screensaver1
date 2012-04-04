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
import java.io.IOException;

import org.apache.commons.cli.OptionBuilder;
import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.io.CommandLineApplication;
import edu.harvard.med.screensaver.io.ParseException;
import edu.harvard.med.screensaver.io.cells.CellParser;

/**
 * 
 */
public class CellImporter extends CommandLineApplication {
	private static final Logger log = Logger.getLogger(CellImporter.class);

	public static void main(String[] args){
		try {
			final CellImporter app = new CellImporter(args);
			app.addCommandLineOption(OptionBuilder.isRequired().hasArg(true).withArgName("file").withLongOpt("input-file")
					.withDescription("The path of a CSV file containing the Cell Information").create("f"));
			app.processOptions(true, false);

			File file = app.getCommandLineOptionValue("f", File.class);
			final CellParser importer = (CellParser) app.getSpringBean("cellParser");
			importer.load(file);
    }
    catch (Exception e) {
      log.error("application exception", e);
      System.exit(1);
    }
	}

	@SuppressWarnings("static-access")
	public CellImporter(String[] args) {
		super(args);
	}

}
