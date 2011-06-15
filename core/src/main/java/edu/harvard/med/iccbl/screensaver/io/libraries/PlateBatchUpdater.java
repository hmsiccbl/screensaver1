// $HeadURL: http://forge.abcd.harvard.edu/svn/screensaver/branches/iccbl/2.2.2-dev/src/edu/harvard/med/screensaver/io/libraries/WellDeprecator.java $
// $Id: WellDeprecator.java 5043 2010-11-23 13:47:40Z atolopko $
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.io.libraries;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;

import com.google.common.collect.Maps;
import org.apache.commons.cli.OptionBuilder;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.DAOTransactionRollbackException;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.db.NoSuchEntityException;
import edu.harvard.med.screensaver.io.CommandLineApplication;
import edu.harvard.med.screensaver.io.workbook2.Row;
import edu.harvard.med.screensaver.io.workbook2.Workbook;
import edu.harvard.med.screensaver.io.workbook2.Worksheet;
import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.MolarConcentration;
import edu.harvard.med.screensaver.model.MolarUnit;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.VolumeUnit;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.libraries.PlateLocation;
import edu.harvard.med.screensaver.model.libraries.PlateStatus;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.model.libraries.Solvent;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.service.libraries.PlateUpdater;
import edu.harvard.med.screensaver.ui.arch.util.converter.CopyUsageTypeConverter;
import edu.harvard.med.screensaver.ui.arch.util.converter.PlateTypeConverter;
import edu.harvard.med.screensaver.ui.arch.util.converter.SolventConverter;

/**
 * Command-line application that updates library copy plate information.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class PlateBatchUpdater
{
  private static Logger log = Logger.getLogger(PlateBatchUpdater.class);

  @SuppressWarnings("static-access")
  public static void main(String[] args) throws FileNotFoundException
  {
    final CommandLineApplication app = new CommandLineApplication(args);
    app.addCommandLineOption(OptionBuilder.hasArg().isRequired().withArgName("file").withLongOpt("input-file").withDescription("workbook file containing plate information").create("f"));
    app.addCommandLineOption(OptionBuilder.hasArg(false).withLongOpt("no-update").withDescription("do not perform update to database (test run only)").create("n"));
    app.processOptions(true, true);

    final GenericEntityDAO dao = (GenericEntityDAO) app.getSpringBean("genericEntityDao");
    final LibrariesDAO librariesDao = (LibrariesDAO) app.getSpringBean("librariesDao");
    final PlateUpdater plateUpdater = (PlateUpdater) app.getSpringBean("plateUpdater");
    final AdministratorUser recordedBy = app.findAdministratorUser();
    final boolean isNoUpdateMode = app.isCommandLineFlagSet("n");
    File file = app.getCommandLineOptionValue("f", File.class);
    Workbook wbk = new Workbook(file);
    final Worksheet worksheet = wbk.getWorksheet(0).forOrigin(0, 1);

    dao.doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        int nPlatesUpdated = updatePlates(worksheet,
                                          dao,
                                          librariesDao,
                                          plateUpdater,
                                          recordedBy);
        log.info("successfully updated " + nPlatesUpdated + " plates");
        if (isNoUpdateMode) {
          throw new DAOTransactionRollbackException("performed test run only; aborting updates");
        }
      }
    });
  }

  private static Map<String,PlateStatus> plateStatusMap = Maps.newHashMap();
  static {
    plateStatusMap.put(PlateStatus.AVAILABLE.toString(), PlateStatus.AVAILABLE);
    plateStatusMap.put(PlateStatus.RETIRED.toString(), PlateStatus.RETIRED);
    plateStatusMap.put(PlateStatus.GIVEN_AWAY.toString(), PlateStatus.GIVEN_AWAY);
    plateStatusMap.put(PlateStatus.DISCARDED.toString(), PlateStatus.DISCARDED);
    plateStatusMap.put("Vol Xferred & Discarded", PlateStatus.VOLUME_TRANSFERRED_AND_DISCARDED);
    plateStatusMap.put(PlateStatus.LOST.toString(), PlateStatus.LOST);
  }

  private static int updatePlates(Worksheet worksheet,
                                  GenericEntityDAO dao,
                                  LibrariesDAO librariesDao,
                                  PlateUpdater plateUpdater,
                                  AdministratorUser recordedByIn)
  {
    int n = 0;
    for (final Row row : worksheet) {
      if (row.isEmpty()) {
        break;
      }
      log.info("parsing row " + (row.getRow() + 1));

      Integer plateNumber = row.getCell(0, true).getInteger();
      String copyName = row.getCell(1, true).getString();
      MolarConcentration molarConcentration = row.getCell(4).isEmpty() ? null
        : new MolarConcentration(row.getCell(4).getBigDecimal(), MolarUnit.MICROMOLAR);
      Volume volume = row.getCell(5).isEmpty() ? null : new Volume(row.getCell(5).getBigDecimal(), VolumeUnit.MICROLITERS);
      LocalDate datePlated = row.getCell(7, true).getDate().toLocalDate();
      LocalDate dateRetired = row.getCell(8).isEmpty() ? null : row.getCell(8).getDate().toLocalDate();
      Solvent solvent = (Solvent) new SolventConverter().getAsObject(null, null, row.getCell(9, true).getString());
      CopyUsageType copyUsageType = (CopyUsageType) new CopyUsageTypeConverter().getAsObject(null, null, row.getCell(10, true).getString());
      String room = row.getCell(11).getString();
      String freezer = row.getCell(12).getString();
      String shelf = row.getCell(13).getString();
      String bin = row.getCell(14).getString();
      PlateType plateType = (PlateType) new PlateTypeConverter().getAsObject(null, null, row.getCell(15, true).getString());
      PlateStatus plateStatus = plateStatusMap.get(row.getCell(16, true).getString());
      String comments = row.getCell(17).getString();

      AdministratorUser recordedBy = dao.reloadEntity(recordedByIn);
      Plate plate = findPlateAndCreateCopyIfNecessary(dao, librariesDao, plateNumber, copyName, copyUsageType, recordedBy);
      log.info("updating plate " + plateNumber + ":" + copyName);
      boolean modified = false;

      modified |= plateUpdater.updateMolarConcentration(plate, molarConcentration, recordedBy);
      modified |= plateUpdater.updateWellVolume(plate, volume, recordedBy);
      modified |= plateUpdater.updatePlateType(plate, plateType, recordedBy);
      if (comments != null) {
        plate.createUpdateActivity(AdministrativeActivityType.COMMENT,
                                   recordedBy,
                                   comments);
        modified = true;
      }


      if (plate.getStatus() != PlateStatus.NOT_SPECIFIED) {
        log.warn("resetting plate status to 'Not Specified' before updating status");
        plate.setStatus(PlateStatus.NOT_SPECIFIED);
      }

      modified |= plateUpdater.updatePlateStatus(plate,
                                                 PlateStatus.AVAILABLE,
                                                 recordedBy,
                                                 recordedBy,
                                                 datePlated);
      appendComment(plate.getLastRecordedUpdateActivityOfType(AdministrativeActivityType.PLATE_STATUS_UPDATE),
                    ". Status updated by batch update (user that created the plate is unknown)");
      if (dateRetired != null) {
        modified |= plateUpdater.updatePlateStatus(plate,
                                                   plateStatus,
                                                   recordedBy,
                                                   recordedBy,
                                                   dateRetired);
        appendComment(plate.getLastRecordedUpdateActivityOfType(AdministrativeActivityType.PLATE_STATUS_UPDATE),
                      ". Status updated by batch update (user that retired the plate is unknown)");
      }

      if (room != null || freezer != null || shelf != null || bin != null) {
        PlateLocation location = new PlateLocation(room, freezer, shelf, bin);
        modified |= plateUpdater.updatePlateLocation(plate,
                                                     location,
                                                     recordedBy,
                                                     recordedBy,
                                                     new LocalDate());
        appendComment(plate.getLastRecordedUpdateActivityOfType(AdministrativeActivityType.PLATE_LOCATION_TRANSFER),
                      ". Location updated by batch update (user that performed the location transfer and date of transfer is unknown)");
      }

      if (plate.getCopy().getLibrary().getSolvent() != solvent) {
        log.warn("changing library " + plate.getCopy().getLibrary().getLibraryName() +
                 " solvent from " + plate.getCopy().getLibrary().getSolvent() +
                 " to " + solvent);
        plate.getCopy().getLibrary().setSolvent(solvent);
      }

      if (plate.getCopy().getUsageType() != copyUsageType) {
        log.warn("changing copy " + plate.getCopy().getLibrary().getLibraryName() + ":" + plate.getCopy().getName() +
                 " copy usage type from " + plate.getCopy().getUsageType() + " to " + copyUsageType);
        plate.getCopy().setUsageType(copyUsageType);
      }

      modified |= plateUpdater.updateFacilityId(plate, recordedBy);

      dao.flush();
      dao.clear();
      ++n;
    }
    log.info("validating plate locations...");
    plateUpdater.validateLocations();
    log.info("plates updated: " + n);
    return n;
  }

  private static void appendComment(AdministrativeActivity activity, String comment)
  {
    activity.setComments(activity.getComments() + comment);
  }

  public static Plate findPlateAndCreateCopyIfNecessary(GenericEntityDAO dao,
                                                        LibrariesDAO librariesDao,
                                                        Integer plateNumber,
                                                        String copyName,
                                                        CopyUsageType copyUsageType,
                                                        AdministratorUser recordedBy)
  {
    Plate plate = librariesDao.findPlate(plateNumber, copyName);
    if (plate == null) {
      Library library = librariesDao.findLibraryWithPlate(plateNumber);
      if (library == null) {
        throw NoSuchEntityException.forProperty(Library.class, "plateNumber", plateNumber);
      }
      Copy copy = library.createCopy(recordedBy, copyUsageType, copyName);
      log.info("created new copy " + copyName + " for library " + library.getLibraryName());
      dao.flush();
      plate = copy.findPlate(plateNumber);
    }
    return plate;
  }
}
