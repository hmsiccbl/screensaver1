// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccb.screensaver.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellVolumeAdjustment;
import edu.harvard.med.screensaver.model.libraries.WellVolumeCorrectionActivity;
import edu.harvard.med.screensaver.model.users.AdministratorUser;

import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;

public class WellRetirer
{
  // static members

  private static Logger log = Logger.getLogger(WellRetirer.class);

  public static void main(String[] args) throws ParseException
  {
    CommandLineApplication app = new CommandLineApplication(args);
    if (!app.processOptions(true, true)) {
      System.exit(1);
    }

    final GenericEntityDAO dao = (GenericEntityDAO) app.getSpringBean("genericEntityDao");
    final LibrariesDAO libariesDao = (LibrariesDAO) app.getSpringBean("librariesDao");

    dao.doInTransaction(new DAOTransaction() {
      public void runTransaction()
      {
        AdministratorUser performedBy = dao.findEntityByProperty(AdministratorUser.class,
                                                                 "ECommonsId",
                                                                 "sr50");
        WellVolumeCorrectionActivity wellVolumeCorrectionActivity = new WellVolumeCorrectionActivity(performedBy, new LocalDate());
        wellVolumeCorrectionActivity.setComments("Retired well copies C and D on plates 50093-50185 having at least one plated lab cherry pick.  Addresses problem whereby well copies were being overdrawn.");

        for (int plateNumber = 50093; plateNumber <= 50185; ++plateNumber) {
          retirePlateRangeCopy(dao, libariesDao, wellVolumeCorrectionActivity, plateNumber, "C");
          retirePlateRangeCopy(dao, libariesDao, wellVolumeCorrectionActivity, plateNumber, "D");
        }
        dao.persistEntity(wellVolumeCorrectionActivity);
      }
    });
 }

  private static void retirePlateRangeCopy(GenericEntityDAO dao,
                                           LibrariesDAO libariesDao,
                                           WellVolumeCorrectionActivity wvca,
                                           int plateNumber,
                                           String copyName)
  {
    List<Well> wellsOnPlate = dao.findEntitiesByProperty(Well.class, "plateNumber", plateNumber);
    Library library = libariesDao.findLibraryWithPlate(plateNumber);

    Map<String,Object> findCopyProps = new HashMap<String,Object>();
    findCopyProps.put("library", library);
    findCopyProps.put("name", copyName);
    Copy copy = dao.findEntityByProperties(Copy.class, findCopyProps);

    for (Well well : wellsOnPlate) {
      if (isWellCopyCherryPicked(well, copy, dao)) {
        Volume remainingVolume = libariesDao.findRemainingVolumeInWellCopy(well, copy);
        if (remainingVolume.compareTo(Volume.ZERO) > 0) {
          wvca.createWellVolumeAdjustment(copy, well, remainingVolume.negate());
          log.info("retired well copy " + well + ":" + copy + " adjusting by " + remainingVolume);
        }
      }
    }
  }

  private static boolean isWellCopyCherryPicked(Well well, Copy copy, GenericEntityDAO dao)
  {
    List<WellVolumeAdjustment> wvas =
      dao.findEntitiesByHql(WellVolumeAdjustment.class,
                            "from WellVolumeAdjustment wva where wva.well = ? and wva.copy = ? and wva.labCherryPick is not null",
                            well,
                            copy);
    return wvas.size() > 0;
  }

  // instance data members

  // public constructors and methods

  // private methods

}
