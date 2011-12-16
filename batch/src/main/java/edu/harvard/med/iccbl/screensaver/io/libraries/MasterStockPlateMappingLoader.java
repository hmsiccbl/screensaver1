// $HeadURL: $
// $Id: $
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.io.libraries;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;

import com.google.common.collect.Sets;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.io.CommandLineApplication;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.libraries.Quadrant;
import edu.harvard.med.screensaver.model.libraries.StockPlateMapping;

/**
 * The file format for the loader is a CSV file with three fields per row:
 * <code>&lt;96-well stock plate number&gt;,&lt;stock plate number&gt;,&lt;quadrant&gt;</code> where &lt;quadrant&gt;
 * is the string value of a {@link Quadrant} enum value. The 96-well stock plates (referenced in the first field) may
 * only have copy plates with usage type {@link CopyUsageType#MASTER_STOCK_PLATES}.
 */
public class MasterStockPlateMappingLoader extends CommandLineApplication
{
  private static final Logger log = Logger.getLogger(MasterStockPlateMappingLoader.class);

  @SuppressWarnings("static-access")
  public static void main(String[] args) throws IOException
  {
    final MasterStockPlateMappingLoader app = new MasterStockPlateMappingLoader(args);
    app.addCommandLineOption(OptionBuilder.isRequired().hasArg(true).withArgName("file").withLongOpt("input-file").withDescription("The path of a CSV file containing the stock plate mapping data (master stock plate number, stock plate number, quadrant").create("f"));
    app.processOptions(true, false);
    GenericEntityDAO dao = (GenericEntityDAO) app.getSpringBean("genericEntityDao");
    dao.doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        try {
          app.load();
        }
        catch (Exception e) {
          throw new DAOTransactionRollbackException(e);
        }
      }
    });
  }

  public MasterStockPlateMappingLoader(String[] args)
  {
    super(args);
  }

  public void load() throws ParseException, IOException
  {
    File file = getCommandLineOptionValue("f", File.class);
    GenericEntityDAO dao = (GenericEntityDAO) getSpringBean("genericEntityDao");
    BufferedReader reader = new BufferedReader(new FileReader(file));
    String row = null;
    int nPlatesUpdated = 0;
    int nPlateNumbersUpdated = 0;
    while ((row = reader.readLine()) != null) {
      String[] fields = row.split(",");
      Integer masterStockPlateNumber = Integer.valueOf(fields[0]);
      Integer stockPlateNumber = Integer.valueOf(fields[1]);
      Quadrant quadrant = Quadrant.valueOf(fields[2]);
      Set<Plate> copyPlatesForPlateNumber = Sets.newHashSet(dao.findEntitiesByProperty(Plate.class, "plateNumber", masterStockPlateNumber));
      int copiesUpdated = 0;
      for (Plate masterStockPlate : copyPlatesForPlateNumber) {
        log.debug("for plate: " + masterStockPlateNumber + ", found copy: " + masterStockPlate.getCopy().getName() + ", usage type: " + masterStockPlate.getCopy().getUsageType() );
        if (masterStockPlate.getCopy().getUsageType() == CopyUsageType.MASTER_STOCK_PLATES) {
          masterStockPlate.setStockPlateMapping(new StockPlateMapping(stockPlateNumber, quadrant));
          log.info("updated plate " + masterStockPlate.getPlateNumber() + " copy " + masterStockPlate.getCopy().getName() +
            " with " + masterStockPlate.getStockPlateMapping());
          ++nPlatesUpdated;
          ++copiesUpdated;
        }
      }
      if (copiesUpdated == 0) {
        throw new IllegalArgumentException("plate " + masterStockPlateNumber +
          " has no master stock plate copies, according to the copy usage types found");
      }
      ++nPlateNumbersUpdated;
    }
    log.info("updated " + nPlateNumbersUpdated + " plate number(s)");
    log.info("updated " + nPlatesUpdated + " plate(s)");
  }
}
