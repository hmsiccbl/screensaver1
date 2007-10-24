// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.service.libraries.rnai;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import edu.harvard.med.screensaver.AbstractSpringPersistenceTest;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.LibrariesDAO;
import edu.harvard.med.screensaver.model.MakeDummyEntities;
import edu.harvard.med.screensaver.model.cherrypicks.LabCherryPick;
import edu.harvard.med.screensaver.model.cherrypicks.RNAiCherryPickRequest;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellType;
import edu.harvard.med.screensaver.model.screens.Screen;
import edu.harvard.med.screensaver.model.screens.ScreenType;

public class LibraryPoolToDuplexWellMapperTest extends AbstractSpringPersistenceTest
{
  // static members

  private static Logger log = Logger.getLogger(LibraryPoolToDuplexWellMapperTest.class);

  // instance data members

  protected LibraryPoolToDuplexWellMapper libraryPoolToDuplexWellMapper;
  protected LibrariesDAO librariesDao;


  // public constructors and methods

  // TODO: consider testing same gene in 2 libs, but with different silencing reagents (shared gene, above, also sahres same silencing reagents between the 2 libraries)
  public void testMap()
  {
    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Library poolLibrary1 = new Library("Pools Library1", "pools lib1", ScreenType.RNAI, LibraryType.COMMERCIAL, 1, 1);
        Library duplexLibrary1 = new Library("Duplexes Library1", "duplexes lib1", ScreenType.RNAI, LibraryType.COMMERCIAL, 2, 5);
        Library poolLibrary2 = new Library("Pools Library2", "pools lib2", ScreenType.RNAI, LibraryType.COMMERCIAL, 6, 61);
        Library duplexLibrary2 = new Library("Duplexes Library2", "duplexes lib2", ScreenType.RNAI, LibraryType.COMMERCIAL, 7, 10);

        Gene gene1 = new Gene("ANT1", 1, "ant1", "Human");
        Gene gene2 = new Gene("ANT2", 2, "ant2", "Human");
        Gene gene3 = new Gene("ANT3", 3, "ant3", "Human");

        // shared by lib1 and lib2
        SilencingReagent silencingReagent1_1 = gene1.createSilencingReagent(SilencingReagentType.SIRNA, "GATTACA");
        SilencingReagent silencingReagent1_2 = gene1.createSilencingReagent(SilencingReagentType.SIRNA, "GATTACC");
        SilencingReagent silencingReagent1_3 = gene1.createSilencingReagent(SilencingReagentType.SIRNA, "GATTACT");
        SilencingReagent silencingReagent1_4 = gene1.createSilencingReagent(SilencingReagentType.SIRNA, "GATTACG");

        // lib1 only
        SilencingReagent silencingReagent2_1 = gene2.createSilencingReagent(SilencingReagentType.SIRNA, "GATTAAA");
        SilencingReagent silencingReagent2_2 = gene2.createSilencingReagent(SilencingReagentType.SIRNA, "GATTAAC");
        SilencingReagent silencingReagent2_3 = gene2.createSilencingReagent(SilencingReagentType.SIRNA, "GATTAAT");
        SilencingReagent silencingReagent2_4 = gene2.createSilencingReagent(SilencingReagentType.SIRNA, "GATTAAG");

        // lib2 only
        SilencingReagent silencingReagent3_1 = gene3.createSilencingReagent(SilencingReagentType.SIRNA, "GATTAGA");
        SilencingReagent silencingReagent3_2 = gene3.createSilencingReagent(SilencingReagentType.SIRNA, "GATTAGC");
        SilencingReagent silencingReagent3_3 = gene3.createSilencingReagent(SilencingReagentType.SIRNA, "GATTAGT");
        SilencingReagent silencingReagent3_4 = gene3.createSilencingReagent(SilencingReagentType.SIRNA, "GATTAGG");

        // gene 1
        Well poolWell1_1 = poolLibrary1.createWell(new WellKey(1,  "A01"),  WellType.EXPERIMENTAL);
        poolWell1_1.addSilencingReagent(silencingReagent1_1);
        poolWell1_1.addSilencingReagent(silencingReagent1_2);
        poolWell1_1.addSilencingReagent(silencingReagent1_3);
        poolWell1_1.addSilencingReagent(silencingReagent1_4);

        Well duplexWell1_1_1 = duplexLibrary1.createWell(new WellKey(2,  "A01"),  WellType.EXPERIMENTAL);
        Well duplexWell1_1_2 = duplexLibrary1.createWell(new WellKey(3,  "B02"),  WellType.EXPERIMENTAL);
        Well duplexWell1_1_3 = duplexLibrary1.createWell(new WellKey(4,  "C03"),  WellType.EXPERIMENTAL);
        Well duplexWell1_1_4 = duplexLibrary1.createWell(new WellKey(5,  "D04"),  WellType.EXPERIMENTAL);
        duplexWell1_1_1.addSilencingReagent(silencingReagent1_1);
        duplexWell1_1_2.addSilencingReagent(silencingReagent1_2);
        duplexWell1_1_3.addSilencingReagent(silencingReagent1_3);
        duplexWell1_1_4.addSilencingReagent(silencingReagent1_4);

        Well poolWell2_1 = poolLibrary2.createWell(new WellKey(6,  "A01"),  WellType.EXPERIMENTAL);
        poolWell2_1.addSilencingReagent(silencingReagent1_1);
        poolWell2_1.addSilencingReagent(silencingReagent1_2);
        poolWell2_1.addSilencingReagent(silencingReagent1_3);
        poolWell2_1.addSilencingReagent(silencingReagent1_4);

        Well duplexWell2_1_1 = duplexLibrary2.createWell(new WellKey(7,  "A01"),  WellType.EXPERIMENTAL);
        Well duplexWell2_1_2 = duplexLibrary2.createWell(new WellKey(8,  "B02"),  WellType.EXPERIMENTAL);
        Well duplexWell2_1_3 = duplexLibrary2.createWell(new WellKey(9,  "C03"),  WellType.EXPERIMENTAL);
        Well duplexWell2_1_4 = duplexLibrary2.createWell(new WellKey(10,  "D04"),  WellType.EXPERIMENTAL);
        duplexWell2_1_1.addSilencingReagent(silencingReagent1_1);
        duplexWell2_1_2.addSilencingReagent(silencingReagent1_2);
        duplexWell2_1_3.addSilencingReagent(silencingReagent1_3);
        duplexWell2_1_4.addSilencingReagent(silencingReagent1_4);

        // gene 2
        Well poolWell1_2 = poolLibrary1.createWell(new WellKey(1,  "B02"),  WellType.EXPERIMENTAL);
        poolWell1_2.addSilencingReagent(silencingReagent2_1);
        poolWell1_2.addSilencingReagent(silencingReagent2_2);
        poolWell1_2.addSilencingReagent(silencingReagent2_3);
        poolWell1_2.addSilencingReagent(silencingReagent2_4);

        Well duplexWell1_2_1 = duplexLibrary1.createWell(new WellKey(2,  "A02"),  WellType.EXPERIMENTAL);
        Well duplexWell1_2_2 = duplexLibrary1.createWell(new WellKey(3,  "B03"),  WellType.EXPERIMENTAL);
        Well duplexWell1_2_3 = duplexLibrary1.createWell(new WellKey(4,  "C04"),  WellType.EXPERIMENTAL);
        Well duplexWell1_2_4 = duplexLibrary1.createWell(new WellKey(5,  "D05"),  WellType.EXPERIMENTAL);
        duplexWell1_2_1.addSilencingReagent(silencingReagent2_1);
        duplexWell1_2_2.addSilencingReagent(silencingReagent2_2);
        duplexWell1_2_3.addSilencingReagent(silencingReagent2_3);
        duplexWell1_2_4.addSilencingReagent(silencingReagent2_4);

        // gene 3
        Well poolWell2_2 = poolLibrary2.createWell(new WellKey(6,  "B02"),  WellType.EXPERIMENTAL);
        poolWell2_2.addSilencingReagent(silencingReagent3_1);
        poolWell2_2.addSilencingReagent(silencingReagent3_2);
        poolWell2_2.addSilencingReagent(silencingReagent3_3);
        poolWell2_2.addSilencingReagent(silencingReagent3_4);

        Well duplexWell2_2_1 = duplexLibrary2.createWell(new WellKey(7,  "A02"),  WellType.EXPERIMENTAL);
        Well duplexWell2_2_2 = duplexLibrary2.createWell(new WellKey(8,  "B03"),  WellType.EXPERIMENTAL);
        Well duplexWell2_2_3 = duplexLibrary2.createWell(new WellKey(9,  "C04"),  WellType.EXPERIMENTAL);
        Well duplexWell2_2_4 = duplexLibrary2.createWell(new WellKey(10,  "D05"),  WellType.EXPERIMENTAL);
        duplexWell2_2_1.addSilencingReagent(silencingReagent3_1);
        duplexWell2_2_2.addSilencingReagent(silencingReagent3_2);
        duplexWell2_2_3.addSilencingReagent(silencingReagent3_3);
        duplexWell2_2_4.addSilencingReagent(silencingReagent3_4);

        genericEntityDao.saveOrUpdateEntity(poolLibrary1);
        genericEntityDao.saveOrUpdateEntity(duplexLibrary1);
        genericEntityDao.saveOrUpdateEntity(poolLibrary2);
        genericEntityDao.saveOrUpdateEntity(duplexLibrary2);
      }
    });

    genericEntityDao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        // cherry pick pool whose gene is in both library 1 and 2, but only cherry picked from library 1
        {
          Screen screen = MakeDummyEntities.makeDummyScreen(1, ScreenType.RNAI);
          RNAiCherryPickRequest rnaiCherryPickRequest = (RNAiCherryPickRequest) screen.createCherryPickRequest();
          Set<WellKey> actualDuplexCherryPickWellKeys = createLabCherryPicksForPoolWells(rnaiCherryPickRequest,
                                                                                         new WellKey(1, "A01"));
          TreeSet<WellKey> expectedDuplexCherryPickWellKeys = new TreeSet<WellKey>();
          expectedDuplexCherryPickWellKeys.add(new WellKey(2, "A01"));
          expectedDuplexCherryPickWellKeys.add(new WellKey(3, "B02"));
          expectedDuplexCherryPickWellKeys.add(new WellKey(4, "C03"));
          expectedDuplexCherryPickWellKeys.add(new WellKey(5, "D04"));

          assertEquals(expectedDuplexCherryPickWellKeys, actualDuplexCherryPickWellKeys);
          genericEntityDao.saveOrUpdateEntity(screen); // avoid hib errors on flush
        }

        // cherry pick pool whose gene is in both library 1 and 2, and cherry picked from both libraries
        {
          Screen screen = MakeDummyEntities.makeDummyScreen(2, ScreenType.RNAI);
          RNAiCherryPickRequest rnaiCherryPickRequest = (RNAiCherryPickRequest) screen.createCherryPickRequest();
          Set<WellKey> actualDuplexCherryPickWellKeys = createLabCherryPicksForPoolWells(rnaiCherryPickRequest,
                                                                                  new WellKey(1, "A01"),
                                                                                  new WellKey(6, "A01"));
          TreeSet<WellKey> expectedDuplexCherryPickWellKeys = new TreeSet<WellKey>();
          expectedDuplexCherryPickWellKeys.add(new WellKey(2, "A01"));
          expectedDuplexCherryPickWellKeys.add(new WellKey(3, "B02"));
          expectedDuplexCherryPickWellKeys.add(new WellKey(4, "C03"));
          expectedDuplexCherryPickWellKeys.add(new WellKey(5, "D04"));
          expectedDuplexCherryPickWellKeys.add(new WellKey(7, "A01"));
          expectedDuplexCherryPickWellKeys.add(new WellKey(8, "B02"));
          expectedDuplexCherryPickWellKeys.add(new WellKey(9, "C03"));
          expectedDuplexCherryPickWellKeys.add(new WellKey(10, "D04"));

          assertEquals(expectedDuplexCherryPickWellKeys, actualDuplexCherryPickWellKeys);
          genericEntityDao.saveOrUpdateEntity(screen); // avoid hib errors on flush
        }

        // cherry pick pool whose gene is only in library1
        {
          Screen screen = MakeDummyEntities.makeDummyScreen(31, ScreenType.RNAI);
          RNAiCherryPickRequest rnaiCherryPickRequest = (RNAiCherryPickRequest) screen.createCherryPickRequest();
          Set<WellKey> actualDuplexCherryPickWellKeys = createLabCherryPicksForPoolWells(rnaiCherryPickRequest,
                                                                                  new WellKey(1, "B02"));
          TreeSet<WellKey> expectedDuplexCherryPickWellKeys = new TreeSet<WellKey>();
          expectedDuplexCherryPickWellKeys.add(new WellKey(2, "A02"));
          expectedDuplexCherryPickWellKeys.add(new WellKey(3, "B03"));
          expectedDuplexCherryPickWellKeys.add(new WellKey(4, "C04"));
          expectedDuplexCherryPickWellKeys.add(new WellKey(5, "D05"));

          assertEquals(expectedDuplexCherryPickWellKeys, actualDuplexCherryPickWellKeys);
          genericEntityDao.saveOrUpdateEntity(screen); // avoid hib errors on flush
        }

        // cherry pick pool whose gene is only in library2
        {
          Screen screen = MakeDummyEntities.makeDummyScreen(4, ScreenType.RNAI);
          RNAiCherryPickRequest rnaiCherryPickRequest = (RNAiCherryPickRequest) screen.createCherryPickRequest();
          Set<WellKey> actualDuplexCherryPickWellKeys = createLabCherryPicksForPoolWells(rnaiCherryPickRequest,
                                                                                         new WellKey(6, "B02"));
          TreeSet<WellKey> expectedDuplexCherryPickWellKeys = new TreeSet<WellKey>();
          expectedDuplexCherryPickWellKeys.add(new WellKey(7, "A02"));
          expectedDuplexCherryPickWellKeys.add(new WellKey(8, "B03"));
          expectedDuplexCherryPickWellKeys.add(new WellKey(9, "C04"));
          expectedDuplexCherryPickWellKeys.add(new WellKey(10, "D05"));

          assertEquals(expectedDuplexCherryPickWellKeys, actualDuplexCherryPickWellKeys);
          genericEntityDao.saveOrUpdateEntity(screen); // avoid hib errors on flush
        }

        // test reverse lookup, from duplex to pool
        {
          // TODO: this test code is relying upon state created above, which is not great practice; should make this into an independent test
          List<Well> duplexWells = Arrays.asList(librariesDao.findWell(new WellKey(2, "A02")),
                                                 librariesDao.findWell(new WellKey(3, "B03")),
                                                 librariesDao.findWell(new WellKey(4, "C04")),
                                                 librariesDao.findWell(new WellKey(5, "D05")),
                                                 librariesDao.findWell(new WellKey(7, "A02")),
                                                 librariesDao.findWell(new WellKey(8, "B03")),
                                                 librariesDao.findWell(new WellKey(9, "C04")),
                                                 librariesDao.findWell(new WellKey(10, "D05")));
          Map<Well,Well> poolWells = libraryPoolToDuplexWellMapper.mapDuplexWellsToPoolWells(new HashSet<Well>(duplexWells));
          int i = 0;
          assertEquals("duplex-to-pool lookup", poolWells.get(duplexWells.get(i++)), librariesDao.findWell(new WellKey(1, "B02")));
          assertEquals("duplex-to-pool lookup", poolWells.get(duplexWells.get(i++)), librariesDao.findWell(new WellKey(1, "B02")));
          assertEquals("duplex-to-pool lookup", poolWells.get(duplexWells.get(i++)), librariesDao.findWell(new WellKey(1, "B02")));
          assertEquals("duplex-to-pool lookup", poolWells.get(duplexWells.get(i++)), librariesDao.findWell(new WellKey(1, "B02")));
          assertEquals("duplex-to-pool lookup", poolWells.get(duplexWells.get(i++)), librariesDao.findWell(new WellKey(6, "B02")));
          assertEquals("duplex-to-pool lookup", poolWells.get(duplexWells.get(i++)), librariesDao.findWell(new WellKey(6, "B02")));
          assertEquals("duplex-to-pool lookup", poolWells.get(duplexWells.get(i++)), librariesDao.findWell(new WellKey(6, "B02")));
          assertEquals("duplex-to-pool lookup", poolWells.get(duplexWells.get(i++)), librariesDao.findWell(new WellKey(6, "B02")));
        }
      }

    });
  }


  // private methods

  private Set<WellKey> createLabCherryPicksForPoolWells(RNAiCherryPickRequest rnaiCherryPickRequest, WellKey... wellKeys)
  {
    for (WellKey wellKey : wellKeys) {
      Well poolWell = librariesDao.findWell(wellKey);
      rnaiCherryPickRequest.createScreenerCherryPick(poolWell);
    }
    libraryPoolToDuplexWellMapper.createDuplexLabCherryPicksforPoolScreenerCherryPicks(rnaiCherryPickRequest);
    Set<LabCherryPick> actualDuplexCherryPickWells = rnaiCherryPickRequest.getLabCherryPicks();
    Set<WellKey> actualDuplexCherryPickWellKeys = new TreeSet<WellKey>();
    for (LabCherryPick labCherryPick : actualDuplexCherryPickWells) {
      actualDuplexCherryPickWellKeys.add(labCherryPick.getSourceWell().getWellKey());
    }
    return actualDuplexCherryPickWellKeys;
  }
}
