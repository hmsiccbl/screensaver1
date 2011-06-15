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
import java.math.BigDecimal;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
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
import edu.harvard.med.screensaver.util.StringUtils;

/**
 * Command-line application that updates library copy plate information.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class SmallMoleculePlateBatchUpdater
{
  private static Logger log = Logger.getLogger(SmallMoleculePlateBatchUpdater.class);

  @SuppressWarnings("static-access")
  public static void main(String[] args)
  {
    final CommandLineApplication app = new CommandLineApplication(args);
    try {
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
    catch (Exception e) {
      e.printStackTrace();
      log.error(e.toString());
      System.exit(1);
    }
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

      Integer plateNumber = row.getCell(3).getInteger();
      String copyName = row.getCell(4).getString();
      String room = row.getCell(5).getString();
      String freezer = row.getCell(6).getString();
      String shelf = row.getCell(7).getString();
      String bin = row.getCell(8).getString();
      PlateType plateType = (PlateType) new PlateTypeConverter().getAsObject(null, null, row.getCell(9).getString());
      Volume volume = row.getCell(10).isEmpty() ? null : new Volume(row.getCell(10).getBigDecimal(), VolumeUnit.MICROLITERS);
      CopyUsageType copyUsageType = (CopyUsageType) new CopyUsageTypeConverter().getAsObject(null, null, row.getCell(11).getString());
      UniversalConcentration universalConcentration = parseConcentration(row.getCell(12).getAsString());
      Solvent solvent = (Solvent) new SolventConverter().getAsObject(null, null, row.getCell(13, true).getString());
      LocalDate datePlated = row.getCell(15).getDate() == null ? null : row.getCell(15).getDate().toLocalDate();
      LocalDate dateRetired = row.getCell(16).getDate() == null ? null : row.getCell(16).getDate().toLocalDate();
      PlateStatus plateStatus = plateStatusMap.get(row.getCell(17).getString());
      String comments = row.getCell(19).getString();

      AdministratorUser recordedBy = dao.reloadEntity(recordedByIn);
      Plate plate = findPlateAndCreateCopyIfNecessary(dao, librariesDao, plateNumber, copyName, copyUsageType, recordedBy);
      log.info("updating plate " + plateNumber + ":" + copyName);
      boolean modified = false;

      if (universalConcentration.mgMl != null) {
        modified |= plateUpdater.updateMgMlConcentration(plate, universalConcentration.mgMl, recordedBy);
      }
      if (universalConcentration.molar != null) {
        modified |= plateUpdater.updateMolarConcentration(plate, universalConcentration.molar, recordedBy);
      }
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

      if (plateStatus == null) {
        throw new BusinessRuleViolationException("plate status must be specified");
      }
      // Note: this logic does not yet handle the NOT_AVAILABLE status properly;
      // will only matter if we add NOT_AVAILABLE to the plateStatusMap, above
      if (plateStatus.compareTo(PlateStatus.AVAILABLE) >= 0) {
        String datePlatedComment = "";
        if (datePlated == null) {
          datePlatedComment = ". Date plated is unknown, using Copy recorded date";
          datePlated = plate.getCopy().getDateCreated().toLocalDate();
        }
        modified |= plateUpdater.updatePlateStatus(plate,
                                                   PlateStatus.AVAILABLE,
                                                   recordedBy,
                                                   recordedBy,
                                                   datePlated);
        appendComment(plate.getLastRecordedUpdateActivityOfType(AdministrativeActivityType.PLATE_STATUS_UPDATE),
                    ". Status updated by batch update (user that created the plate is unknown)" + datePlatedComment);
      }

      String dateRetiredComment = "";
      if (dateRetired == null && plateStatus.compareTo(PlateStatus.AVAILABLE) > 0) {
        dateRetiredComment = ". Date retired is unknown, using current date";
        dateRetired = new LocalDate();
      }
      if (dateRetired != null) {
        modified |= plateUpdater.updatePlateStatus(plate,
                                                   plateStatus,
                                                   recordedBy,
                                                   recordedBy,
                                                   dateRetired);
        appendComment(plate.getLastRecordedUpdateActivityOfType(AdministrativeActivityType.PLATE_STATUS_UPDATE),
                      ". Status updated by batch update (user that retired the plate is unknown)" + dateRetiredComment);
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


  private static class UniversalConcentration
  {
    BigDecimal mgMl;
    MolarConcentration molar;

    public UniversalConcentration(BigDecimal mgMlConcentration)
    {
      mgMl = mgMlConcentration;
    }

    public UniversalConcentration(MolarConcentration molarConcentration)
    {
      molar = molarConcentration;
    }

    static Pattern MG_ML_CONCENTRATION_PATTERN = Pattern.compile("([0-9]+) mg/ml");
    static Pattern MILLIMOLAR_CONCENTRATION_PATTERN = Pattern.compile("([0-9]+) mM");
  };

  private static UniversalConcentration parseConcentration(String s)
  {
    if (StringUtils.isEmpty(s)) {
      return null;
    }
    Matcher matcher = UniversalConcentration.MG_ML_CONCENTRATION_PATTERN.matcher(s);
    if (matcher.matches()) {
      return new UniversalConcentration(new BigDecimal(matcher.group(1)));
    }
    matcher = UniversalConcentration.MILLIMOLAR_CONCENTRATION_PATTERN.matcher(s);
    if (matcher.matches()) {
      return new UniversalConcentration(new MolarConcentration(matcher.group(1), MolarUnit.MILLIMOLAR));
    }
    throw new IllegalArgumentException("unparseable concentration value: " + s);
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
