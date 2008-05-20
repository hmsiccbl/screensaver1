// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.cherrypicks;

import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.List;

import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellType;
import edu.harvard.med.screensaver.model.screenresults.ResultValueType;
import edu.harvard.med.screensaver.model.screenresults.ScreenResult;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;

import org.apache.log4j.Logger;

public class CompoundCherryPickRequestTest extends CherryPickRequestTest<CompoundCherryPickRequest>
{

  // private static datum

  private static Logger log = Logger.getLogger(CompoundCherryPickRequestTest.class);


  // private instance datum

  protected LibrariesDAO librariesDao;

  
  // public instance methods
  
  public CompoundCherryPickRequestTest() throws IntrospectionException
  {
    super(CompoundCherryPickRequest.class);
  }
  
  public void testCherryPickAllowance()
  {
    schemaUtil.truncateTablesOrCreateSchema();
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Library library = new Library("Compound Library", "clib", ScreenType.SMALL_MOLECULE, LibraryType.COMMERCIAL, 1, 10);
        librariesDao.loadOrCreateWellsForLibrary(library);
        int iSmiles = 0;
        List<Well> wells = new ArrayList<Well>(library.getWells());
        Compound compound = null;
        for (Well well : wells) {
          well.setWellType(WellType.EXPERIMENTAL);
          // every compound in two distinct wells
          if (iSmiles++ % 2 == 0) {
            int suffix = (int) (iSmiles / 2);
            compound = new Compound("smiles" + suffix, "inchi" + suffix);
          }
          well.addCompound(compound);
        }
        genericEntityDao.saveOrUpdateEntity(library);
        
        Screen screen = MakeDummyEntities.makeDummyScreen(1);
        
        ScreenResult screenResult = screen.createScreenResult();
        ResultValueType resultValueType = screenResult.createResultValueType("values");
        for (Well well : wells) {
          resultValueType.createResultValue(well, "1.0");
        }
        
        CompoundCherryPickRequest cherryPickRequest = (CompoundCherryPickRequest) screen.createCherryPickRequest();
        for (int i = 0; i < 200; ++i) {
          cherryPickRequest.createScreenerCherryPick(wells.get(i));
        }

        assertEquals((int) ((10 * 384) / 2 * 0.003), cherryPickRequest.getCherryPickAllowance());
        assertEquals(200, cherryPickRequest.getCherryPickAllowanceUsed());

        genericEntityDao.saveOrUpdateEntity(screen.getLeadScreener());
        genericEntityDao.saveOrUpdateEntity(screen.getLabHead());
        genericEntityDao.saveOrUpdateEntity(screen);
      }
    });
  }
}

