// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.searchresults;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.faces.model.DataModel;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.cherrypicks.CherryPickRequest;
import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick;
import edu.harvard.med.screensaver.model.cherrypicks.ScreenerCherryPick;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.PlateSize;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellCopy;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellVolume;
import edu.harvard.med.screensaver.model.libraries.WellVolumeAdjustment;
import edu.harvard.med.screensaver.model.users.AdministratorUser;
import edu.harvard.med.screensaver.test.MakeDummyEntities;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;
import edu.harvard.med.screensaver.ui.arch.view.AbstractBackingBeanTest;
import edu.harvard.med.screensaver.ui.libraries.WellCopyVolumeSearchResults;
import edu.harvard.med.screensaver.ui.libraries.WellVolumeSearchResults;

public class WellVolumeSearchResultsTest extends AbstractBackingBeanTest
{
  private static Logger log = Logger.getLogger(WellVolumeSearchResultsTest.class);

  @Autowired
  protected LibrariesDAO librariesDao;
  @Autowired
  protected WellCopyVolumeSearchResults wellCopyVolumesBrowser;
  @Autowired
  protected WellVolumeSearchResults wellVolumesBrowser;

  private Library _library;
  private CherryPickRequest _cherryPickRequest;
  private Map<WellCopy,Volume> _expectedRemainingWellCopyVolume = Maps.newHashMap();
  private Map<WellKey,Volume> _expectedRemainingWellVolume = Maps.newHashMap();


  @Override
  protected void setUp() throws Exception
  {
    super.setUp();

    initializeWellCopyVolumes();
  }

  public void testWellVolumeSearchResults()
  {
    wellCopyVolumesBrowser.searchWellsForLibrary(_library);
    doTestWellCopyVolumeSearchResult(makeWellCopies(_library));
    doTestWellVolumeSearchResult(makeWellVolumeKeys(makeWellCopies(_library)));

    List<Well> wells = new ArrayList<Well>(_library.getWells()).subList(24, 96);
    wellCopyVolumesBrowser.searchWells(makeWellKeys(wells));
    doTestWellCopyVolumeSearchResult(makeWellCopies(wells));
    doTestWellVolumeSearchResult(makeWellVolumeKeys(makeWellCopies(wells)));

    wellCopyVolumesBrowser.searchWellsForCherryPickRequest(_cherryPickRequest, false);
    doTestWellCopyVolumeSearchResult(makeWellCopies(_cherryPickRequest));
    doTestWellVolumeSearchResult(makeWellVolumeKeys(makeWellCopies(_cherryPickRequest)));
  }

  private void doTestWellCopyVolumeSearchResult(SortedSet<WellCopy> expectedWellCopies)
  {
    DataModel model = wellCopyVolumesBrowser.getDataTableModel();
    assertEquals("row count", expectedWellCopies.size(), model.getRowCount());
    int j = 0;
    for (WellCopy expectedWellCopy : expectedWellCopies) {
      model.setRowIndex(j++);
      //assertEquals("row data " + j, expectedWellCopy, ((WellCopy) model.getRowData()));
      int columnsTested = 0;
      WellCopy rowData = (WellCopy) model.getRowData();
      for (TableColumn<WellCopy,?> column : wellCopyVolumesBrowser.getColumnManager().getAllColumns()) {
        if (column.isVisible()) {
          Object cellValue = column.getCellValue(rowData);
          if (column.getName().equals("Library")) {
            assertEquals("row " + j + ", " + expectedWellCopy + ":Library",
                         librariesDao.findLibraryWithPlate(expectedWellCopy.getWell().getPlateNumber()).getLibraryName(),
                         (String) cellValue);
            ++columnsTested;
          }
          else if (column.getName().equals("Plate")) {
            assertEquals("row " + j + ", " + expectedWellCopy.getEntityId() + ":Plate",
                         (Integer) expectedWellCopy.getWell().getPlateNumber(),
                         (Integer) cellValue);
            ++columnsTested;
          }
          else if (column.getName().equals("Well")) {
            assertEquals("row " + j + ", " + expectedWellCopy.getEntityId() + ":Well",
                         expectedWellCopy.getWell().getWellKey().getWellName().toString(),
                         (String) cellValue);
            ++columnsTested;
          }
          else if (column.getName().equals("Initial Volume")) {
            assertEquals("row " + j + ", " + expectedWellCopy + ":Initial Volume",
                         expectedWellCopy.getCopy().getName().equals("C") ? new Volume(10) : new Volume(20),
                         (Volume) cellValue);
            ++columnsTested;
          }
          else if (column.getName().equals("Remaining Volume")) {
            // this tests aggregation of WVAs
            assertEquals("row " + j + ", " + expectedWellCopy  + ":Remaining Volume",
                         _expectedRemainingWellCopyVolume.get(expectedWellCopy),
                         (Volume) cellValue);
            ++columnsTested;
          }
          // TODO: test all aggregation columns!
        }
      }
      assertEquals("tested all columns for row " + j, 5, columnsTested);
    }
  }

  private void doTestWellVolumeSearchResult(SortedSet<WellKey> expectedWellKeys)
  {
    DataModel model = wellVolumesBrowser.getDataTableModel();
    assertEquals("row count", expectedWellKeys.size(), model.getRowCount());
    int j = 0;
    for (WellKey expectedKey : expectedWellKeys) {
      model.setRowIndex(j++);
      assertEquals("row data " + j,
                   expectedKey,
                   ((WellVolume) model.getRowData()).getWell().getWellKey());
      int columnsTested = 0;
      WellVolume rowData = (WellVolume) model.getRowData();
      for (TableColumn<WellVolume,?> column : wellVolumesBrowser.getColumnManager().getAllColumns()) {
        if (column.isVisible()) {
          Object cellValue = column.getCellValue(rowData);
          if (column.getName().equals("Library")) {
            assertEquals("row " + j + ", " + expectedKey + ":Library",
                         librariesDao.findLibraryWithPlate(expectedKey.getPlateNumber()).getLibraryName(),
                         (String) cellValue);
            ++columnsTested;
          }
          else if (column.getName().equals("Plate")) {
            assertEquals("row " + j + ", " + expectedKey + ":Plate",
                         (Integer) expectedKey.getPlateNumber(),
                         (Integer) cellValue);
            ++columnsTested;
          }
          else if (column.getName().equals("Well")) {
            assertEquals("row " + j + ", " + expectedKey  + ":Well",
                         expectedKey.getWellName(),
                         (String) cellValue);
            ++columnsTested;
          }
          else if (column.getName().equals("Total Initial Copy Volume")) {
            assertEquals("row " + j + ", " + expectedKey  + ":Total Initial Copy Volume",
                         new Volume(30),
                         (Volume) cellValue);
            ++columnsTested;
          }
          else if (column.getName().equals("Consumed Volume")) {
            // this tests aggregation of WVAs
            assertEquals("row " + j + ", " + expectedKey  + ":Consumed Volume",
                         new Volume(30).subtract((Volume) _expectedRemainingWellVolume.get(expectedKey)),
                         (Volume) cellValue);
            ++columnsTested;
          }
          else if (column.getName().equals("Copies")) {
            // this tests aggregation of WVAs
            assertEquals("row " + j + ", " + expectedKey  + ":Copies",
                         ImmutableSortedSet.of("C", "D"),
                         cellValue);
            ++columnsTested;
          }

          // TODO: test all aggregation columns!
        }
      }
      assertEquals("tested all columns for row " + j, 6, columnsTested);
    }
  }

  /**
   * 00001:A01:C: 1 lcp
   * 00001:A01:D: 1 lcp
   * 00001:A02:C: -
   * 00001:A02:D: -
   * 00001:B01:C: 2 wvas
   * 00001:B01:D: -
   * 00001:B02:C: 1 wva
   * 00001:B02:D: 1 wva
   * 00002:A01:C: 1 lcp
   * 00002:A01:D: -
   * 00002:B01:C: -
   * 00002:B01:D: 1 lcp, 1 wva
   */
  private void initializeWellCopyVolumes()
  {
    genericEntityDao.doInTransaction(new DAOTransaction() {
      public void runTransaction() {
        _library = MakeDummyEntities.makeRNAiDuplexLibrary("library", 1, 2, PlateSize.WELLS_384);
        Copy copyC = _library.createCopy((AdministratorUser) _library.getCreatedBy(), CopyUsageType.CHERRY_PICK_SOURCE_PLATES, "C");
        Copy copyD = _library.createCopy((AdministratorUser) _library.getCreatedBy(), CopyUsageType.CHERRY_PICK_SOURCE_PLATES, "D");
        copyC.findPlate(1).setWellVolume(new Volume(10));
        copyC.findPlate(2).setWellVolume(new Volume(10));
        copyD.findPlate(1).setWellVolume(new Volume(20));
        copyD.findPlate(2).setWellVolume(new Volume(20));
        genericEntityDao.saveOrUpdateEntity(_library);

        Well plate1WellA01 = genericEntityDao.findEntityById(Well.class, "00001:A01");
        Well plate1WellB01 = genericEntityDao.findEntityById(Well.class, "00001:B01");
        Well plate1WellB02 = genericEntityDao.findEntityById(Well.class, "00001:B02");
        Well plate2WellA01 = genericEntityDao.findEntityById(Well.class, "00002:A01");
        Well plate2WellB01 = genericEntityDao.findEntityById(Well.class, "00002:B01");

        _cherryPickRequest = MakeDummyEntities.createRNAiCherryPickRequest(1, new Volume(3));
        ScreenerCherryPick dummyScreenerCherryPick = _cherryPickRequest.createScreenerCherryPick(plate1WellA01);
        // note: 2 LCPs for the same well (which have to be in 2 separate CPRs) to test aggregation of 2 LCPs in the same well
        CherryPickRequest cherryPickRequest2 = MakeDummyEntities.createRNAiCherryPickRequest(2, new Volume(3));
        ScreenerCherryPick dummyScreenerCherryPick2 = cherryPickRequest2.createScreenerCherryPick(plate1WellA01);
        dummyScreenerCherryPick.createLabCherryPick(plate1WellA01).setAllocated(copyC);
        dummyScreenerCherryPick2.createLabCherryPick(plate1WellA01).setAllocated(copyD);
        dummyScreenerCherryPick.createLabCherryPick(plate2WellA01).setAllocated(copyC);
        dummyScreenerCherryPick.createLabCherryPick(plate2WellB01).setAllocated(copyD);
        genericEntityDao.saveOrUpdateEntity(_cherryPickRequest.getScreen().getLeadScreener());
        genericEntityDao.saveOrUpdateEntity(_cherryPickRequest.getScreen().getLabHead());
        genericEntityDao.saveOrUpdateEntity(_cherryPickRequest.getScreen());
        genericEntityDao.saveOrUpdateEntity(cherryPickRequest2.getScreen());

        genericEntityDao.persistEntity(new WellVolumeAdjustment(copyC, plate1WellB01, new Volume(-1), null));
        genericEntityDao.persistEntity(new WellVolumeAdjustment(copyC, plate1WellB01, new Volume(-1), null));
        genericEntityDao.persistEntity(new WellVolumeAdjustment(copyC, plate1WellB02, new Volume(-1), null));
        genericEntityDao.persistEntity(new WellVolumeAdjustment(copyD, plate1WellB02, new Volume(-1), null));
        genericEntityDao.persistEntity(new WellVolumeAdjustment(copyD, plate2WellB01, new Volume(-1), null));

        _expectedRemainingWellCopyVolume.put(new WellCopy(plate1WellA01, copyC), new Volume(7));
        _expectedRemainingWellCopyVolume.put(new WellCopy(plate1WellA01, copyD), new Volume(17));
        _expectedRemainingWellCopyVolume.put(new WellCopy(plate1WellB01, copyC), new Volume(8));
        _expectedRemainingWellCopyVolume.put(new WellCopy(plate1WellB02, copyC), new Volume(9));
        _expectedRemainingWellCopyVolume.put(new WellCopy(plate1WellB02, copyD), new Volume(19));
        _expectedRemainingWellCopyVolume.put(new WellCopy(plate2WellA01, copyC), new Volume(7));
        _expectedRemainingWellCopyVolume.put(new WellCopy(plate2WellB01, copyD), new Volume(16));

        for (Well well : _library.getWells()) {
          Volume expectedRemainingWellVolume = new Volume(0);
          for (Copy copy : _library.getCopies()) {
            WellCopy wellCopy = new WellCopy(well, copy);
            if (!_expectedRemainingWellCopyVolume.containsKey(wellCopy)) {
              Volume expectedRemainingWellCopyVolume = copy.getPlates().values().iterator().next().getWellVolume();
              _expectedRemainingWellCopyVolume.put(wellCopy, expectedRemainingWellCopyVolume);
              expectedRemainingWellVolume = expectedRemainingWellVolume.add(expectedRemainingWellCopyVolume);
            }
            else {
              expectedRemainingWellVolume = expectedRemainingWellVolume.add((Volume) _expectedRemainingWellCopyVolume.get(wellCopy));
            }
          }
          _expectedRemainingWellVolume.put(well.getWellKey(), expectedRemainingWellVolume);
        }
      }
    });
  }

  private static Set<WellKey> makeWellKeys(List<Well> wells)
  {
    Set<WellKey> wellKeys = new HashSet<WellKey>();
    for (Well well : wells) {
      wellKeys.add(well.getWellKey());
    }
    return wellKeys;
  }

  private static SortedSet<WellCopy> makeWellCopies(Library library)
  {
    SortedSet<WellCopy> wellCopies = Sets.newTreeSet();
    for (Well well : library.getWells()) {
      for (Copy copy : library.getCopies()) {
        wellCopies.add(new WellCopy(well, copy));
      }
    }
    return wellCopies;
  }

  private static SortedSet<WellCopy> makeWellCopies(List<Well> wells)
  {
    SortedSet<WellCopy> wellCopies = Sets.newTreeSet();
    for (Well well : wells) {
      for (Copy copy : well.getLibrary().getCopies()) {
        wellCopies.add(new WellCopy(well, copy));
      }
    }
    return wellCopies;
  }

  private static SortedSet<WellCopy> makeWellCopies(CherryPickRequest cherryPickRequest)
  {
    SortedSet<WellCopy> wellCopies = Sets.newTreeSet();
    for (LabCherryPick labCherryPick : cherryPickRequest.getLabCherryPicks()) {
      Well well = labCherryPick.getSourceWell();
      for (Copy copy : well.getLibrary().getCopies()) {
        wellCopies.add(new WellCopy(well, copy));
      }
    }
    return wellCopies;
  }

  private SortedSet<WellKey> makeWellVolumeKeys(SortedSet<WellCopy> wellCopies)
  {
    SortedSet<WellKey> wellKeys = new TreeSet<WellKey>();
    for (WellCopy wellCopy : wellCopies) {
      wellKeys.add(wellCopy.getWell().getWellKey());
    }
    return wellKeys;
  }
}
