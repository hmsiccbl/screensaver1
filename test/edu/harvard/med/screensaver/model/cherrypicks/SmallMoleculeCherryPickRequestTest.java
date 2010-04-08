// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.cherrypicks;

import java.beans.IntrospectionException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestSuite;

import edu.harvard.med.iccbl.screensaver.policy.cherrypicks.SmallMoleculeCherryPickRequestAllowancePolicy;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.AdministrativeActivity;
import edu.harvard.med.screensaver.model.AdministrativeActivityType;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.MolecularFormula;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screenresults.AssayWell;
import edu.harvard.med.screensaver.model.screenresults.DataColumn;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.model.users.AdministratorUser;

import org.apache.log4j.Logger;
import org.joda.time.LocalDate;

public class SmallMoleculeCherryPickRequestTest extends CherryPickRequestTest<SmallMoleculeCherryPickRequest>
{
  public static TestSuite suite()
  {
    return buildTestSuite(SmallMoleculeCherryPickRequestTest.class, SmallMoleculeCherryPickRequest.class);
  }

  private static Logger log = Logger.getLogger(SmallMoleculeCherryPickRequestTest.class);

  protected LibrariesDAO librariesDao;
  protected SmallMoleculeCherryPickRequestAllowancePolicy smallMoleculeCherryPickRequestAllowancePolicy;

  
  public SmallMoleculeCherryPickRequestTest() throws IntrospectionException
  {
    super(SmallMoleculeCherryPickRequest.class);
  }
  
  public void testCherryPickAllowance()
  {
    schemaUtil.truncateTablesOrCreateSchema();
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Library library = new Library("Small Molecule Library", "smlib", ScreenType.SMALL_MOLECULE, LibraryType.COMMERCIAL, 1, 10);
        AdministratorUser admin = new AdministratorUser("Admin", "User", "", "", "", "", "", "");
        /*LibraryContentsVersion libraryContentsVersion =*/ 
        library.createContentsVersion(new AdministrativeActivity(admin, new LocalDate(), AdministrativeActivityType.LIBRARY_CONTENTS_LOADING));
        librariesDao.loadOrCreateWellsForLibrary(library);
        int iSmiles = 0;
        List<Well> wells = new ArrayList<Well>(library.getWells());
        for (Well well : wells) {
          well.setLibraryWellType(LibraryWellType.EXPERIMENTAL);
          // every small molecule reagent in two distinct wells
          if (iSmiles++ % 2 == 0) {
            int suffix = (int) (iSmiles / 2);
            well.createSmallMoleculeReagent(new ReagentVendorIdentifier("vendor", "smiles"),
                                            "molfile",
                                            "smiles" + suffix, 
                                            "inchi" + suffix, 
                                            new BigDecimal("1.011"), 
                                            new BigDecimal("1.011"), 
                                            new MolecularFormula("C3"));
          }
        }
        library.getLatestContentsVersion().release(new AdministrativeActivity((AdministratorUser) library.getLatestContentsVersion().getLoadingActivity().getPerformedBy(), new LocalDate(), AdministrativeActivityType.LIBRARY_CONTENTS_VERSION_RELEASE));
        genericEntityDao.saveOrUpdateEntity(library);
        
        Screen screen = MakeDummyEntities.makeDummyScreen(1);
        
        ScreenResult screenResult = screen.createScreenResult();
        DataColumn dataColumn = screenResult.createDataColumn("values");
        for (Well well : wells) {
          AssayWell assayWell = screenResult.createAssayWell(well);
          dataColumn.createResultValue(assayWell, 1.0);
        }
        
        SmallMoleculeCherryPickRequest cherryPickRequest = (SmallMoleculeCherryPickRequest) screen.createCherryPickRequest((AdministratorUser) screen.getCreatedBy());
        for (int i = 0; i < 200; ++i) {
          cherryPickRequest.createScreenerCherryPick(wells.get(i));
        }
        genericEntityDao.persistEntity(cherryPickRequest.getScreen());
        genericEntityDao.flush();

        assertEquals((int) ((10 * 384) / 2 * 0.003), smallMoleculeCherryPickRequestAllowancePolicy.getCherryPickAllowance(cherryPickRequest));
        assertEquals(200, smallMoleculeCherryPickRequestAllowancePolicy.getCherryPickAllowanceUsed(cherryPickRequest));

        genericEntityDao.saveOrUpdateEntity(screen.getLeadScreener());
        genericEntityDao.saveOrUpdateEntity(screen.getLabHead());
        genericEntityDao.saveOrUpdateEntity(screen);
      }
    });
  }
}

