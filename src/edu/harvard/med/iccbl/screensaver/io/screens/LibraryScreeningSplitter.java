// $HeadURL: http://forge.abcd.harvard.edu/svn/screensaver/branches/iccbl/screening-status/src/edu/harvard/med/iccbl/screensaver/io/libraries/LibraryScreeningSplitter.java $
// $Id: LibraryScreeningSplitter.java 4054 2010-04-27 01:09:11Z atolopko $
//
// Copyright © 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.iccbl.screensaver.io.screens;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.db.Query;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.screenresults.AssayPlate;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.model.users.ScreeningRoomUser;

/**
 * Command-line application that splits LibraryScreenings into two or more, to bring legacy data into compliance with
 * the current domain model, where a library screening may only contain one assay plate per library plate number and
 * replicate ordinal.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class LibraryScreeningSplitter
{
  private static final String MIGRATION_COMMENT_TEMPLATE = "This library screening was created automatically during data migration.  It was created to accommodate library plates that were screened multiple times within the original library screening (#).";
  private static Logger log = Logger.getLogger(LibraryScreeningSplitter.class);

  public static void main(String[] args) throws ParseException
  {
    final CommandLineApplication app = new CommandLineApplication(args);

    if (!app.processOptions(true, false, true)) {
      System.exit(1);
    }

    final GenericEntityDAO dao = (GenericEntityDAO) app.getSpringBean("genericEntityDao");

    dao.doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        Set<LibraryScreening> libraryScreeningsWithDuplicatePlateNumbers = findLibraryScreeningsWithDuplicatePlateNumbers(dao);
        for (LibraryScreening libraryScreening : libraryScreeningsWithDuplicatePlateNumbers) {
          splitLibraryScreening(libraryScreening, dao);
        }
        //throw new DAOTransactionRollbackException("debug");
      }
    });
  }

  private static void splitLibraryScreening(LibraryScreening libraryScreening, GenericEntityDAO dao)
  {
    do {
      libraryScreening = dao.reloadEntity(libraryScreening);
      List<AssayPlate> assayPlatesToMove = findAssayPlatesToMove(libraryScreening, dao);
      if (assayPlatesToMove.isEmpty()) break;
      log.info("splitting " + libraryScreening);
      libraryScreening = cloneLibraryScreening(libraryScreening, assayPlatesToMove, dao);
      log.info("created " + libraryScreening + " with assay plates " + assayPlatesToMove);
      dao.flush();
      dao.clear();
    } while (true);
  }

  private static List<AssayPlate> findAssayPlatesToMove(LibraryScreening libraryScreening, GenericEntityDAO dao)
  {
    List<AssayPlate> assayPlatesToMove = Lists.newArrayList();
    List<AssayPlate> allAssayPlates = dao.findEntitiesByProperty(AssayPlate.class, "libraryScreening", libraryScreening);
    Collections.sort(allAssayPlates, new Comparator<AssayPlate>() {
      @Override
      public int compare(AssayPlate a1, AssayPlate a2)
      {
        int result = Integer.valueOf(a1.getPlateNumber()).compareTo(Integer.valueOf(a2.getPlateNumber()));
        if (result == 0) {
          result = a1.getPlateScreened().getCopy().getName().compareTo(a2.getPlateScreened().getCopy().getName());
        }
        if (result == 0) {
          result = Integer.valueOf(a1.getReplicateOrdinal()).compareTo(Integer.valueOf(a2.getReplicateOrdinal()));
        }
        return result;
      }
    });
    Map<Integer,Plate> platesToKeep = Maps.newHashMap();
    for (AssayPlate assayPlate : allAssayPlates) {
      Plate plateToKeep = platesToKeep.get(assayPlate.getPlateNumber());
      if (plateToKeep == null) {
        platesToKeep.put(assayPlate.getPlateNumber(), assayPlate.getPlateScreened());
      }
      else if (!plateToKeep.equals(assayPlate.getPlateScreened())) {
        assayPlatesToMove.add(assayPlate);
      }
    }
    return assayPlatesToMove;
  }

  private static LibraryScreening cloneLibraryScreening(LibraryScreening libraryScreening,
                                                        List<AssayPlate> assayPlatesToMove,
                                                        GenericEntityDAO dao)
  {
    AdministratorUser createdBy = null;
    if (libraryScreening.getCreatedBy() != null) {
      createdBy = dao.findEntityById(AdministratorUser.class, libraryScreening.getCreatedBy().getEntityId());
    }
    ScreeningRoomUser performedBy = dao.findEntityById(ScreeningRoomUser.class, libraryScreening.getPerformedBy().getEntityId());
    LibraryScreening newLibraryScreening =
      libraryScreening.getScreen().createLibraryScreening(createdBy,
                                                          performedBy,
                                                          libraryScreening.getDateOfActivity());
    newLibraryScreening.setAbaseTestsetId(libraryScreening.getAbaseTestsetId());
    newLibraryScreening.setAssayProtocol(libraryScreening.getAssayProtocol());
    newLibraryScreening.setAssayProtocolLastModifiedDate(libraryScreening.getAssayProtocolLastModifiedDate());
    newLibraryScreening.setAssayProtocolType(libraryScreening.getAssayProtocolType());
    newLibraryScreening.setComments(MIGRATION_COMMENT_TEMPLATE.replaceFirst("#", Integer.toString(libraryScreening.getEntityId())) +
      "\n" +
      (libraryScreening.getComments() == null ? "" : libraryScreening.getComments()));
    newLibraryScreening.setConcentration(libraryScreening.getConcentration());
    newLibraryScreening.setForExternalLibraryPlates(libraryScreening.isForExternalLibraryPlates());
    newLibraryScreening.setNumberOfReplicates(libraryScreening.getNumberOfReplicates());
    newLibraryScreening.setVolumeTransferredPerWell(libraryScreening.getVolumeTransferredPerWell());
    for (AssayPlate assayPlate : assayPlatesToMove) {
      assayPlate.setLibraryScreening(newLibraryScreening);
      dao.saveOrUpdateEntity(assayPlate);
    }
    dao.saveOrUpdateEntity(newLibraryScreening);
    return newLibraryScreening;
  }

  private static Set<LibraryScreening> findLibraryScreeningsWithDuplicatePlateNumbers(GenericEntityDAO dao)
  {
    List<Integer> libraryScreeningIds = dao.runQuery(new Query() {
      @Override
      public List execute(Session session)
      {
        SQLQuery sqlQuery = session.createSQLQuery("select distinct library_screening_id from assay_plate ap join lab_activity la on(la.activity_id=ap.library_screening_id) join screen s on(s.screen_id=la.screen_id) group by library_screening_id, plate_number, replicate_ordinal having count(*) > 1;");
        return sqlQuery.list();
      }
    });
    Set<LibraryScreening> result = Sets.newHashSet();
    for (Integer id : libraryScreeningIds) {
      result.add(dao.findEntityById(LibraryScreening.class, id));
    }
    return result;
  }
}
