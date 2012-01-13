// $HeadURL$
// $Id$
//
// Copyright © 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.libraries;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.db.Criterion.Operator;
import edu.harvard.med.screensaver.model.MolarConcentration;
import edu.harvard.med.screensaver.model.MolarUnit;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.VolumeUnit;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryContentsVersion;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.libraries.PlateSize;
import edu.harvard.med.screensaver.model.libraries.PlateStatus;
import edu.harvard.med.screensaver.model.libraries.PlateType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.ScreensaverUserRole;
import edu.harvard.med.screensaver.service.libraries.LibraryContentsVersionManager;
import edu.harvard.med.screensaver.service.libraries.LibraryCreator;
import edu.harvard.med.screensaver.service.libraries.PlateUpdater;
import edu.harvard.med.screensaver.ui.arch.datatable.column.TableColumn;
import edu.harvard.med.screensaver.ui.arch.view.AbstractBackingBeanTest;

public class LibraryCopyDetailViewerTest extends AbstractBackingBeanTest
{
  private static Logger log = Logger.getLogger(LibraryCopyDetailViewerTest.class);

  @Autowired
  protected LibraryCopyDetail libraryCopyDetail;
  @Autowired
  protected LibraryContentsVersionManager libraryContentsVersionManager;
  @Autowired
  protected LibraryCreator libraryCreator;
  @Autowired
  protected LibrariesDAO librariesDao;

  private Copy _copyC;
  private Copy _copyD;
  private Library _library;
  private Library _library2;
  private Copy _copy2;

  private MolarConcentration minMolarConcentration = MolarConcentration.makeConcentration("100", MolarUnit.PICOMOLAR);
  private MolarConcentration maxMolarConcentration = MolarConcentration.makeConcentration("100", MolarUnit.MILLIMOLAR);
  private MolarConcentration primaryMolarConcentration = MolarConcentration.makeConcentration("200", MolarUnit.MICROMOLAR);
  private BigDecimal minMgMlConcentration = new BigDecimal("0.010");
  private BigDecimal maxMgMlConcentration = new BigDecimal("5.000");
  private BigDecimal primaryMgMlConcentration = new BigDecimal("3.000");
  private BigDecimal primaryMgMlConcentration2 = new BigDecimal("2.000");

  protected void setUp() throws Exception
  {
    super.setUp();

    genericEntityDao.doInTransaction(new DAOTransaction() {

      @Override
      public void runTransaction()
      {
        _admin.addScreensaverUserRole(ScreensaverUserRole.LIBRARY_COPIES_ADMIN);
        _admin.addScreensaverUserRole(ScreensaverUserRole.LIBRARIES_ADMIN);
        _admin = genericEntityDao.mergeEntity(_admin);
        _library = new Library(null, "lib", "lib", ScreenType.SMALL_MOLECULE, LibraryType.COMMERCIAL, 1, 6, PlateSize.WELLS_96);
        libraryCreator.createLibrary(_library);
        for (Well well : _library.getWells())
        {
          well.setLibraryWellType(LibraryWellType.EXPERIMENTAL);
        }

        _library = genericEntityDao.mergeEntity(_library);
        _copyC = _library.createCopy(null, CopyUsageType.LIBRARY_SCREENING_PLATES, "C");
        _copyD = _library.createCopy(null, CopyUsageType.LIBRARY_SCREENING_PLATES, "D");

        _library2 = new Library(null, "lib2", "lib2", ScreenType.SMALL_MOLECULE, LibraryType.COMMERCIAL, 7, 12, PlateSize.WELLS_96);
        libraryCreator.createLibrary(_library2);
        for (Well well : _library2.getWells())
        {
          well.setLibraryWellType(LibraryWellType.EXPERIMENTAL);
        }

        _library2 = genericEntityDao.mergeEntity(_library2);
        _copy2 = _library2.createCopy(null, CopyUsageType.LIBRARY_SCREENING_PLATES, "2");

        libraryCopyDetail.getCurrentScreensaverUser().setScreensaverUser(_admin);

        genericEntityDao.flush();

        LibraryContentsVersion lcv = _library.createContentsVersion(_admin);
        genericEntityDao.persistEntity(lcv);
        lcv = _library2.createContentsVersion(_admin);
        genericEntityDao.persistEntity(lcv);
        genericEntityDao.flush(); //?
        _admin = genericEntityDao.reloadEntity(_admin);
        _library = genericEntityDao.reloadEntity(_library, false, Library.contentsVersions);
        libraryContentsVersionManager.releaseLibraryContentsVersion(_library.getLatestContentsVersion(), _admin);
        _library2 = genericEntityDao.reloadEntity(_library2, false, Library.contentsVersions);
        libraryContentsVersionManager.releaseLibraryContentsVersion(_library2.getLatestContentsVersion(), _admin);

        // Create a non-available plate, which should be ignored for concentration calculations at the copy level
        Map<String,Object> properties = Maps.newHashMap();
        properties.put("copy", _copyC);
        properties.put("plateNumber", new Integer(2));
        Plate p = genericEntityDao.findEntityByProperties(Plate.class, properties, true);
        p.setStatus(PlateStatus.LOST);
      }
    });
  }

  public void setupInTransaction_concentrations_heterogeneous()
  {
    genericEntityDao.doInTransaction(new DAOTransaction() {

      @Override
      public void runTransaction()
      {
        _admin = genericEntityDao.reloadEntity(_admin);
        _library = genericEntityDao.reloadEntity(_library, false, Library.contentsVersions);
        LibraryContentsVersion lcv = _library.createContentsVersion(_admin);
        genericEntityDao.persistEntity(lcv);
        genericEntityDao.flush(); //?
      }
    });

    genericEntityDao.doInTransaction(new DAOTransaction() {

      @Override
      public void runTransaction()
      {
        _library = genericEntityDao.reloadEntity(_library, false, Library.wells);

        // ignore plate 4
        for (Copy c : _library.getCopies()) {
          for (Plate p : c.getPlates().values())
          {
            p.setStatus(PlateStatus.AVAILABLE);
            if (p.getPlateNumber() == 4) {
              p.setStatus(PlateStatus.DISCARDED);
            }
          }
        }

        // Plate 1 heterogeneous
        for (Well well : librariesDao.findWellsForPlate(1))
          {
            if (well.getColumn() == 0 && well.getRow() == 0) { // set a few min
              well.setMgMlConcentration(minMgMlConcentration);
              well.setMolarConcentration(minMolarConcentration);
            }
            else if (well.getColumn() == 2 && well.getRow() == 0) { // set a few max
              well.setMgMlConcentration(maxMgMlConcentration);
              well.setMolarConcentration(maxMolarConcentration);
            }
            else {
              well.setMgMlConcentration(primaryMgMlConcentration);
              well.setMolarConcentration(primaryMolarConcentration);
            }
          }
          // Plate 2 homogeneous
          for (Well well : librariesDao.findWellsForPlate(2))
          {
            well.setMgMlConcentration(primaryMgMlConcentration);
            well.setMolarConcentration(primaryMolarConcentration);
          }
          // Plate 3 homogeneous
          for (Well well : librariesDao.findWellsForPlate(3))
          {
            well.setMgMlConcentration(primaryMgMlConcentration);
            well.setMolarConcentration(primaryMolarConcentration);
          }
          // Plate 4 homogeneous
          for (Well well : librariesDao.findWellsForPlate(4))
          {
            well.setMgMlConcentration(primaryMgMlConcentration);
            well.setMolarConcentration(primaryMolarConcentration);
          }
          // Plate 5 homogeneous
          for (Well well : librariesDao.findWellsForPlate(5))
          {
            well.setMgMlConcentration(primaryMgMlConcentration);
            well.setMolarConcentration(primaryMolarConcentration);
          }

          genericEntityDao.mergeEntity(_library);
          genericEntityDao.flush();
        }

    });

    genericEntityDao.doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        _admin = genericEntityDao.reloadEntity(_admin);
        _library = genericEntityDao.reloadEntity(_library, false, Library.contentsVersions);
        libraryContentsVersionManager.releaseLibraryContentsVersion(_library.getLatestContentsVersion(), _admin);
      }
    });
  }

  public void setupInTransaction_concentrations()
  {
    genericEntityDao.doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        _admin = genericEntityDao.reloadEntity(_admin);
        _library = genericEntityDao.reloadEntity(_library, false, Library.contentsVersions);
        LibraryContentsVersion lcv = _library.createContentsVersion(_admin);
        _library2 = genericEntityDao.reloadEntity(_library2, false, Library.contentsVersions);
        lcv = _library2.createContentsVersion(_admin);
        genericEntityDao.persistEntity(lcv);
        genericEntityDao.flush(); //?
      }
    });

    genericEntityDao.doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        _library = genericEntityDao.reloadEntity(_library, false, Library.wells);

        for (Copy c : _library.getCopies()) {
          for (Plate p : c.getPlates().values())
          {
            p.setStatus(PlateStatus.AVAILABLE);
            if (p.getPlateNumber() == 4) {
              p.setStatus(PlateStatus.DISCARDED);
            }
          }
        }

        int i = 0;
        for (Well well : _library.getWells()) {
          well.setMgMlConcentration(primaryMgMlConcentration);
          well.setMolarConcentration(primaryMolarConcentration);
        }
        genericEntityDao.mergeEntity(_library);

        // library 2 has no molar concentrations
        _library2 = genericEntityDao.reloadEntity(_library2, false, Library.wells);
        for (Copy c : _library2.getCopies()) {
          for (Plate p : c.getPlates().values())
            {
              p.setStatus(PlateStatus.AVAILABLE);
            }
          }
          for (Well well : librariesDao.findWellsForPlate(7))
          {
            well.setMgMlConcentration(primaryMgMlConcentration2);
            log.info("well: " + well.getMolarConcentration() + ", " + well.getLibrary().getShortName());
          }
          genericEntityDao.mergeEntity(_library2);

        }
    });

    // release last, as the releaseLibraryContentsVersion() call will invoke the plateUpdater
    genericEntityDao.doInTransaction(new DAOTransaction() {
      @Override
      public void runTransaction()
      {
        _admin = genericEntityDao.reloadEntity(_admin);
        _library = genericEntityDao.reloadEntity(_library, false, Library.contentsVersions);
        libraryContentsVersionManager.releaseLibraryContentsVersion(_library.getLatestContentsVersion(), _admin);
        _library2 = genericEntityDao.reloadEntity(_library2, false, Library.contentsVersions);
        libraryContentsVersionManager.releaseLibraryContentsVersion(_library2.getLatestContentsVersion(), _admin);
        _copy2 = genericEntityDao.reloadEntity(_copy2);
        assertNotNull(_copy2);
        assertNull("_copy2.getPrimaryWellMolarConcentration(): " + _copy2.getPrimaryWellMolarConcentration(), _copy2.getPrimaryWellMolarConcentration());
      }
    });
  }

  @Transactional
  public void testSetMgMlConcentration() throws Exception
  {
    setupInTransaction_concentrations();

    _copyC = genericEntityDao.reloadEntity(_copyC);
    assertNotNull(_copyC);

    libraryCopyDetail.setEntity(_copyC);
    libraryCopyDetail.setMgMlConcentration(new BigDecimal("1"));
    libraryCopyDetail.save();

    _copyC = genericEntityDao.reloadEntity(_copyC);
    assertNotNull(_copyC);

    assertTrue("actual:" + _copyC.getWellConcentrationDilutionFactor(),
               new BigDecimal("3.0").compareTo(_copyC.getWellConcentrationDilutionFactor()) == 0);
    assertTrue("actual: " + _copyC +
      _copyC.getNullSafeConcentrationStatistics().getDilutedPrimaryWellMgMlConcentration(_copyC.getWellConcentrationDilutionFactor()),
               BigDecimal.ONE.compareTo(_copyC.getNullSafeConcentrationStatistics().getDilutedPrimaryWellMgMlConcentration(_copyC.getWellConcentrationDilutionFactor())) == 0);
  }

  @Transactional
  public void testSetMolarConcentration() throws Exception
  {
    setupInTransaction_concentrations();

    _copyC = genericEntityDao.reloadEntity(_copyC);
    assertNotNull(_copyC);

    libraryCopyDetail.setEntity(_copyC);
    //    // on plate = 200 MicroMolar, actual = 333 NanoMolar, or .333 MicroMolar, pdf = 600.600600... pdf_rounded = 600.60, rounded, on plate: .330 uM, actual .333000333000333... uM
    BigDecimal pdf_rounded = new BigDecimal("600.60");
    libraryCopyDetail.setMolarConcentrationValue("333");
    libraryCopyDetail.getMolarConcentrationType().setSelection(MolarUnit.NANOMOLAR);
    libraryCopyDetail.save();

    _copyC = genericEntityDao.reloadEntity(_copyC);
    assertNotNull(_copyC);

    assertEquals("for copy: " + _copyC, primaryMgMlConcentration, _copyC.getPrimaryWellMgMlConcentration());
    assertEquals("for copy: " + _copyC, primaryMolarConcentration, _copyC.getPrimaryWellMolarConcentration());
    assertTrue("actual:" + _copyC.getWellConcentrationDilutionFactor(),
               pdf_rounded.compareTo(_copyC.getWellConcentrationDilutionFactor()) == 0);
    // this involves roundipdf_roundedng, scale = pdf.scale
    assertTrue("actual:" +
      _copyC.getNullSafeConcentrationStatistics().getDilutedPrimaryWellMolarConcentration(_copyC.getWellConcentrationDilutionFactor()),
               MolarConcentration.makeConcentration("330.000", MolarUnit.NANOMOLAR)
                 .compareTo(_copyC.getNullSafeConcentrationStatistics().getDilutedPrimaryWellMolarConcentration(_copyC.getWellConcentrationDilutionFactor())) == 0);

  }

  @Transactional
  public void testSetMgMlConcentration_rounded() throws Exception
  {
    setupInTransaction_concentrations();

    _copyC = genericEntityDao.reloadEntity(_copyC);
    assertNotNull(_copyC);

    libraryCopyDetail.setEntity(_copyC);
    // Simple test: original: 3, target 2.11, pdf (exact) = 1.4218, pdf (rounded, scale=2) =  1.42
    BigDecimal target = new BigDecimal("2.11");
    BigDecimal pdf_rounded = new BigDecimal("1.42");
    libraryCopyDetail.setMgMlConcentration(target);
    libraryCopyDetail.getMolarConcentrationType().setSelection(MolarUnit.NANOMOLAR);
    libraryCopyDetail.save();

    _copyC = genericEntityDao.reloadEntity(_copyC);
    assertNotNull(_copyC);

    assertEquals("for copy: " + _copyC, primaryMgMlConcentration, _copyC.getPrimaryWellMgMlConcentration());
    assertTrue("actual:" + _copyC.getWellConcentrationDilutionFactor(),
               pdf_rounded.compareTo(_copyC.getWellConcentrationDilutionFactor()) == 0);
    assertTrue("actual:" +
      _copyC.getNullSafeConcentrationStatistics().getDilutedPrimaryWellMgMlConcentration(_copyC.getWellConcentrationDilutionFactor()),
               target.compareTo(_copyC.getNullSafeConcentrationStatistics().getDilutedPrimaryWellMgMlConcentration(_copyC.getWellConcentrationDilutionFactor())) == 0);
    List<Plate> plates = genericEntityDao.findEntitiesByProperty(Plate.class, "copy", _copyC, true, Plate.updateActivities.castToSubtype(Plate.class));
    assertEquals(6, plates.size());
    for (Plate p : plates) {
      assertTrue("for plate: " + p + " actual:" + p.getPrimaryWellMgMlConcentration(),
                 primaryMgMlConcentration.compareTo(p.getPrimaryWellMgMlConcentration()) == 0);
      assertTrue("for plate: " + p + " actual:" +
        p.getNullSafeConcentrationStatistics().getDilutedPrimaryWellMgMlConcentration(_copyC.getWellConcentrationDilutionFactor()),
                 target.compareTo(p.getNullSafeConcentrationStatistics().getDilutedPrimaryWellMgMlConcentration(_copyC.getWellConcentrationDilutionFactor())) == 0);
      assertTrue("for plate: " + p + " actual:" + p.getWellConcentrationDilutionFactor(),
                 pdf_rounded.compareTo(p.getWellConcentrationDilutionFactor()) == 0);
    }
  }

  @Transactional
  public void testSetMolarError() throws Exception
  {
    setupInTransaction_concentrations();

    _copy2 = genericEntityDao.reloadEntity(_copy2);
    assertNotNull(_copy2);
    assertNull(_copy2.getPrimaryWellMolarConcentration());

    libraryCopyDetail.setEntity(_copy2);
    //    // on plate = 100 MicroMolar, actual = 333 NanoMolar, or .333 MicroMolar, pdf = 300.300300... pdf_rounded = 300.30, rounded, on plate: .330 uM, actual .333000333000333... uM
    BigDecimal pdf_rounded = new BigDecimal("300.30");
    libraryCopyDetail.setMolarConcentrationValue("333");
    libraryCopyDetail.getMolarConcentrationType().setSelection(MolarUnit.NANOMOLAR);
    libraryCopyDetail.save(); // should throw an error as there is no molar concentration on the copy
    
    assertFalse(libraryCopyDetail.getMessages().getQueuedMessages().isEmpty());
    assertTrue("libraryCopyDetail.getMessages().getQueuedMessages().get(0).getSecond().getSummary(): " + libraryCopyDetail.getMessages().getQueuedMessages().get(0).getSecond().getSummary(), 
               libraryCopyDetail.getMessages().getQueuedMessages().get(0).getSecond().getSummary().contains("Set using the dilution factor instead"));
    
     _copy2 = genericEntityDao.reloadEntity(_copy2);
    assertNotNull(_copy2);
    assertTrue(BigDecimal.ONE.compareTo(_copy2.getWellConcentrationDilutionFactor())==0);
    assertNull(_copy2.getNullSafeConcentrationStatistics().getPrimaryWellMolarConcentration());
  }
}
