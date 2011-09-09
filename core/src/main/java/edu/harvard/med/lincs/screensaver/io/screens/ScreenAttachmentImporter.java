// $HeadURL: $
// $Id: $
//
// Copyright © 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.lincs.screensaver.io.screens;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;

import org.apache.commons.cli.OptionBuilder;
import org.apache.log4j.Logger;
import com.google.common.collect.Sets;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.io.CommandLineApplication;
import edu.harvard.med.screensaver.model.AttachedFile;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenAttachedFileType;

/**
 * LINCS-only import attachments for reagents
 * 
 * @author sde4
 */
public class ScreenAttachmentImporter extends CommandLineApplication
{
  private static Logger log = Logger.getLogger(ScreenAttachmentImporter.class);

  public static final int SHORT_OPTION_INDEX = 0;
  public static final int ARG_INDEX = 1;
  public static final int LONG_OPTION_INDEX = 2;
  public static final int DESCRIPTION_INDEX = 3;
  public static final String[] OPTION_INPUT_FILE = { "f", "file", "input-file",
    "Input file, to be converted to an sdf file (molfile not supported at this time)" };
  public static final String[] OPTION_INPUT_FACILITY_ID = { "i", "facilityId", "facility-id",
    "facility id of the reagent to import the attachement for" };

  public static final Set attachmentTypes = Sets.newHashSet(new String[] { "Study-File" });

  public static String STUDY_FILE_ATTACHMENT_TYPE = "Study-File";

  //  public static final String[] OPTION_ATTACHMENT_TYPE = { "type", "file-type", "file-type",
  //    "Attachment type, one of: " + attachmentTypes };

  @SuppressWarnings("static-access")
  public ScreenAttachmentImporter(String[] cmdLineArgs)
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
    //    option = OPTION_ATTACHMENT_TYPE;
    //    addCommandLineOption(OptionBuilder.hasArg()
    //                             .isRequired()
    //                                          .withArgName(option[ARG_INDEX])
    //                                          .withDescription(option[DESCRIPTION_INDEX])
    //                                          .withLongOpt(option[LONG_OPTION_INDEX])
    //                                          .create(option[SHORT_OPTION_INDEX]));
  }

  @SuppressWarnings("static-access")
  public static void main(String[] args)
  {
    final ScreenAttachmentImporter app = new ScreenAttachmentImporter(args);

    app.processOptions(/* acceptDatabaseOptions= */false, /* acceptAdminUserOptions= */false);

    try {
      execute(app);
    }
    catch (Exception e) {
      log.error("application exception", e);
      System.exit(1);
    }
  }

  private static void execute(final ScreenAttachmentImporter app)
  {
    final GenericEntityDAO dao = (GenericEntityDAO) app.getSpringBean("genericEntityDao");
    final LibrariesDAO librariesDao = (LibrariesDAO) app.getSpringBean("librariesDao");

    dao.doInTransaction(new DAOTransaction() {

      @Override
      public void runTransaction()
      {
        String fileName = app.getCommandLineOptionValue(OPTION_INPUT_FILE[SHORT_OPTION_INDEX]);
        String facilityId = app.getCommandLineOptionValue(OPTION_INPUT_FACILITY_ID[SHORT_OPTION_INDEX]);
        //        String attachmentType = app.getCommandLineOptionValue(OPTION_ATTACHMENT_TYPE[SHORT_OPTION_INDEX], String.class);

        File file = new File(fileName);
        if (!file.exists()) {
          log.error("file does not exist: " + fileName);
          System.exit(1);
        }

        Screen screen = dao.findEntityByProperty(Screen.class, "facilityId", facilityId);
        if (screen == null) {
          log.error("no such screen found for facility id: \"" + facilityId + "\"");
          System.exit(1);
        }

        ScreenAttachedFileType attachedFileType = dao.findEntityByProperty(ScreenAttachedFileType.class, "value", STUDY_FILE_ATTACHMENT_TYPE);

        if (attachedFileType == null) {
          log.error("Could not find the attached file type: " + STUDY_FILE_ATTACHMENT_TYPE +
              ", make sure that this value has been created in the database table.");
          System.exit(1);
        }
        try {
          screen.createAttachedFile(file.getName(), attachedFileType, null, new FileInputStream(fileName));
        }
        catch (IOException e) {
          throw new DAOTransactionRollbackException("File not found: " + fileName, e);
        }

        log.info("saved attachment screen: " + facilityId);
      }
    });
  }
}
