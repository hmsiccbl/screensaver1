// $HeadURL: $
// $Id: $
//
// Copyright © 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.lincs.screensaver.io.libraries;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;

import com.google.common.collect.Sets;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.io.CommandLineApplication;
import edu.harvard.med.screensaver.model.AttachedFile;
import edu.harvard.med.screensaver.model.libraries.ReagentAttachedFileType;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;

/**
 * LINCS-only import attachments for reagents
 * 
 * @author sde4
 */
public class ReagentAttachmentImporter extends CommandLineApplication
{
  private static Logger log = Logger.getLogger(ReagentAttachmentImporter.class);

  public static final int SHORT_OPTION_INDEX = 0;
  public static final int ARG_INDEX = 1;
  public static final int LONG_OPTION_INDEX = 2;
  public static final int DESCRIPTION_INDEX = 3;
  public static final String[] OPTION_INPUT_FILE = { "f", "file", "input-file",
    "Input file, to be converted to an sdf file (molfile not supported at this time)" };
  public static final String[] OPTION_INPUT_FACILITY_ID = { "i", "facilityId", "facility-id",
    "facility id of the reagent to import the attachement for" };
  public static final String[] OPTION_INPUT_SALT_ID = { "sid", "salt-id", "salt-id",
    "salt id the reagent to import the attachement for" };
  public static final String[] OPTION_INPUT_BATCH_ID = { "bid", "batch-id", "batch-id",
    "batch id of the reagent to import the attachement for" };

  public static final Set attachmentTypes = Sets.newHashSet(new String[] { "QC-NMR", "QC-LCMS", "QC-HPLC", "Data-File" });

  public static final String[] OPTION_INPUT_QC_ATTACMENT_TYPE = { "type", "qc-type", "qc-type",
    "QC attachement type, one of: " + attachmentTypes };

  @SuppressWarnings("static-access")
  public ReagentAttachmentImporter(String[] cmdLineArgs)
  {
    super(cmdLineArgs);

    String[] option = OPTION_INPUT_FILE;
    addCommandLineOption(OptionBuilder.hasArg()
                             .isRequired()
                                          .withArgName(option[ARG_INDEX])
                                          .withDescription(option[DESCRIPTION_INDEX])
                                          .withLongOpt(option[LONG_OPTION_INDEX])
                                          .create(option[SHORT_OPTION_INDEX]));
    option = OPTION_INPUT_FACILITY_ID;
    addCommandLineOption(OptionBuilder.hasArg()
                             .isRequired()
                                          .withArgName(option[ARG_INDEX])
                                          .withDescription(option[DESCRIPTION_INDEX])
                                          .withLongOpt(option[LONG_OPTION_INDEX])
                                          .create(option[SHORT_OPTION_INDEX]));
    option = OPTION_INPUT_SALT_ID;
    addCommandLineOption(OptionBuilder.hasArg()
                             .isRequired()
                                          .withArgName(option[ARG_INDEX])
                                          .withDescription(option[DESCRIPTION_INDEX])
                                          .withLongOpt(option[LONG_OPTION_INDEX])
                                          .create(option[SHORT_OPTION_INDEX]));
    option = OPTION_INPUT_BATCH_ID;
    addCommandLineOption(OptionBuilder.hasArg()
                             .isRequired()
                                          .withArgName(option[ARG_INDEX])
                                          .withDescription(option[DESCRIPTION_INDEX])
                                          .withLongOpt(option[LONG_OPTION_INDEX])
                                          .create(option[SHORT_OPTION_INDEX]));
    option = OPTION_INPUT_QC_ATTACMENT_TYPE;
    addCommandLineOption(OptionBuilder.hasArg()
                             .isRequired()
                                          .withArgName(option[ARG_INDEX])
                                          .withDescription(option[DESCRIPTION_INDEX])
                                          .withLongOpt(option[LONG_OPTION_INDEX])
                                          .create(option[SHORT_OPTION_INDEX]));
  }

  @SuppressWarnings("static-access")
  public static void main(String[] args) 
  {
    final ReagentAttachmentImporter app = new ReagentAttachmentImporter(args);

    app.processOptions(/* acceptDatabaseOptions= */false, /* acceptAdminUserOptions= */false);

    try {
      execute(app);
    }
    catch (Exception e) {
      log.error("application exception", e);
      System.exit(1);
    }
  }

  private static void execute(final ReagentAttachmentImporter app)
  {
    final GenericEntityDAO dao = (GenericEntityDAO) app.getSpringBean("genericEntityDao");
    final LibrariesDAO librariesDao = (LibrariesDAO) app.getSpringBean("librariesDao");

    dao.doInTransaction(new DAOTransaction() {

      @Override
      public void runTransaction()
      {
        String fileName = app.getCommandLineOptionValue(OPTION_INPUT_FILE[SHORT_OPTION_INDEX]);
        String facilityId = app.getCommandLineOptionValue(OPTION_INPUT_FACILITY_ID[SHORT_OPTION_INDEX]);
        Integer saltId = app.getCommandLineOptionValue(OPTION_INPUT_SALT_ID[SHORT_OPTION_INDEX], Integer.class);
        Integer batchId = app.getCommandLineOptionValue(OPTION_INPUT_BATCH_ID[SHORT_OPTION_INDEX], Integer.class);
        String attachmentType = app.getCommandLineOptionValue(OPTION_INPUT_QC_ATTACMENT_TYPE[SHORT_OPTION_INDEX], String.class);

        File file = new File(fileName);
        if (!file.exists()) {
          log.error("file does not exist: " + fileName);
          System.exit(1);
        }
        Set<SmallMoleculeReagent> reagents = librariesDao.findReagents(facilityId, saltId, batchId, true);

        if (reagents.isEmpty()) {
          log.error("no reagents found!");
          System.exit(1);
        }

        ReagentAttachedFileType attachedFileType = dao.findEntityByProperty(ReagentAttachedFileType.class, "value", attachmentType);

        if (attachedFileType == null) {
          log.error("Could not find the attached file type: " + attachmentType +
              ", make sure that this value has been created in the database table.");
          System.exit(1);
        }

        for (SmallMoleculeReagent reagent : reagents)
        {
          try {
            AttachedFile attachedFile1 = reagent.createAttachedFile(file.getName(), attachedFileType, null, new FileInputStream(fileName));
            dao.saveOrUpdateEntity(attachedFile1);
            dao.saveOrUpdateEntity(reagent);
          }
          catch (IOException e) {
            throw new DAOTransactionRollbackException("File not found: " + fileName, e);
          }
        }

        log.info("saved attachment for reagent(s): ");
        for (SmallMoleculeReagent reagent : reagents) {
          log.info("\tattached to:" + reagent.getWell().getWellKey() + ": " + reagent.getWell().getFacilityId() + "-" +
            reagent.getSaltFormId() + "-" + reagent.getFacilityBatchId());
        }
      }
    });
  }
}
