// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.compound;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.util.eutils.PubchemSmilesSearch;

/**
 * Standalone application for upgrading the PubChem CIDs for the compounds in a given library.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class CompoundPubchemCidListUpgrader
{
  
  // static fields
  
  private static Logger log = Logger.getLogger(CompoundPubchemCidListUpgrader.class);
  private static Set<String> _alreadyUpgradedLibraryShortNames = new HashSet<String>();
  static {
    // eg:
    //_alreadyUpgradedLibraryShortNames.add("Bionet1");
  }
  
  
  // static methods
  
  public static void main(String [] args)
  {
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] { 
      CommandLineApplication.DEFAULT_SPRING_CONFIG,
    });
    Options options = new Options();
    options.addOption("L", true, "libraryShortName");
    CommandLine commandLine;
    try {
      commandLine = new GnuParser().parse(options, args);
    }
    catch (ParseException e) {
      log.error("error parsing command line options", e);
      return;
    }
    String libraryShortName = commandLine.getOptionValue("L");
    if (libraryShortName == null) {
      log.error("usage: <program-name> -L <library-short-name>");
      System.exit(1);
    }
    GenericEntityDAO dao = (GenericEntityDAO) context.getBean("genericEntityDao");
    CompoundPubchemCidListUpgrader upgrader = new CompoundPubchemCidListUpgrader(dao);
    if (libraryShortName.equals("ALL")) {
      upgradeAllLibraries(dao, upgrader);
    }
    else {
      upgradeOneLibrary(libraryShortName, upgrader);
    }
  }
  
  private static void upgradeAllLibraries(
    GenericEntityDAO dao,
    CompoundPubchemCidListUpgrader upgrader)
  {
    log.info("upgrading pubchem cid lists for all small molecule libraries");
    for (Library library : dao.findAllEntitiesOfType(Library.class, true)) {
      if (library.getScreenType().equals(ScreenType.SMALL_MOLECULE)) {
        upgradeOneLibrary(library.getShortName(), upgrader);
      }
    }
    log.info("successfully upgraded pubchem cid lists for all small molecule libraries");
  }
  
  private static void upgradeOneLibrary(
    String libraryShortName,
    CompoundPubchemCidListUpgrader upgrader)
  {
    if (_alreadyUpgradedLibraryShortNames.contains(libraryShortName)) {
      log.info("skipping upgrade for library " + libraryShortName + " (already upgraded)..");
      return;
    }
    log.info("upgrading pubchem cid lists for library " + libraryShortName + "..");
    upgrader.upgradeLibrary(libraryShortName);
    log.info("successfully upgraded pubchem cid lists for library " + libraryShortName + ".");
  }
  
  
  // instance fields
  
  private GenericEntityDAO _dao;
  private Set<Compound> _visitedCompounds = new HashSet<Compound>();
  private PubchemSmilesSearch pubchemSmilesSearch = new PubchemSmilesSearch();
  
  
  // constructor and instance methods
  
  public CompoundPubchemCidListUpgrader(GenericEntityDAO dao)
  {
    _dao = dao;
  }
  
  public void upgradeLibrary(final String libraryShortName)
  {
    _dao.doInTransaction(new DAOTransaction()
    {
      public void runTransaction()
      {
        Library library = getLibraryForLibraryShortName(libraryShortName);
        int numCompounds = 0;
        int numCompoundsWithOldPubchemCids = 0;
        int numCompoundsWithPubchemCids = 0;
        int numPubchemCids = 0;
        int numOldPubchemCids = 0;
        //OUT:
        for (Well well : library.getWells()) {
          for (Compound compound : well.getCompounds()) {
            if (_visitedCompounds.contains(compound)) {
              continue;
            }
            numCompounds ++;
            numOldPubchemCids += compound.getNumPubchemCids();
            if (compound.getNumPubchemCids() > 0) {
              numCompoundsWithOldPubchemCids ++;
            }
            
            List<String> pubchemCids =
              pubchemSmilesSearch.getPubchemCidsForSmiles(compound.getSmiles());
            if (pubchemCids != null) {
              for (String pubchemCid : pubchemCids) {
                compound.addPubchemCid(pubchemCid);
              }
            }
            
            numPubchemCids += compound.getNumPubchemCids();
            if (compound.getNumPubchemCids() > 0) {
              numCompoundsWithPubchemCids ++;
            }
            _visitedCompounds.add(compound);
            _dao.persistEntity(compound);
            if (numCompounds % 100 == 0) {
              log.info("upgraded " + numCompounds + " compound so far..");
              //break OUT;
            }
          }
        }

        log.info("upgrade statistics for library " + libraryShortName + ":");
        log.info("  numCompounds                   = " + numCompounds);
        log.info("  numCompoundsWithOldPubchemCids = " + numCompoundsWithOldPubchemCids);
        log.info("  numCompoundsWithPubchemCids    = " + numCompoundsWithPubchemCids);
        log.info("  numPubchemCids                 = " + numPubchemCids);
        log.info("  numOldPubchemCids              = " + numOldPubchemCids);
      }
    });
  }
  
  public Library getLibraryForLibraryShortName(String libraryShortName)
  {
    Library library = _dao.findEntityByProperty(
      Library.class,
      "shortName",
      libraryShortName);
    if (library == null) {
      log.error("no library found for libraryShortName \"" + libraryShortName + "\"");
      System.exit(1);
    }
    return library;
  }
}
