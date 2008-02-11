// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.compound.upgraders;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.util.eutils.PubchemSmilesOrInchiSearch;

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
  private static int NUM_COMPOUNDS_UPGRADED_PER_TRANSACTION = 100;
  
  
  // static methods
  
  public static void main(String [] args)
  {
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] { 
      CommandLineApplication.DEFAULT_SPRING_CONFIG,
    });
    GenericEntityDAO dao = (GenericEntityDAO) context.getBean("genericEntityDao");
    CompoundPubchemCidListUpgrader upgrader = new CompoundPubchemCidListUpgrader(dao);
    upgrader.upgradeAllNonUpgradedCompounds();
  }
  
  
  // instance fields
  
  private GenericEntityDAO _dao;
  private PubchemSmilesOrInchiSearch pubchemSmilesOrInchiSearch = new PubchemSmilesOrInchiSearch();
  private int _numCompoundsUpgraded = 0;
  
  // constructor and instance methods
  
  public CompoundPubchemCidListUpgrader(GenericEntityDAO dao)
  {
    _dao = dao;
  }
  
  public void upgradeAllNonUpgradedCompounds()
  {
    List<Compound> nonUpgradedCompoundsList = _dao.findEntitiesByHql(
      Compound.class,
      "from Compound where size(pubchemCids) = 0");
    log.info(
      "retrieved " + nonUpgradedCompoundsList.size() +
      " compounds needing an upgrade");
    System.exit(0);
    final Iterator<Compound> nonUpgradedCompounds =
      nonUpgradedCompoundsList.iterator();
    while (nonUpgradedCompounds.hasNext()) {
      _dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          int i = 0;
          while (nonUpgradedCompounds.hasNext() && i < NUM_COMPOUNDS_UPGRADED_PER_TRANSACTION) {
            Compound compound = _dao.reloadEntity(nonUpgradedCompounds.next());
            for (Well well : compound.getWells()) {
              List<String> pubchemCids =
                pubchemSmilesOrInchiSearch.getPubchemCidsForSmilesOrInchi(well.getMolfile());
              if (pubchemCids != null) {
                for (String pubchemCid : pubchemCids) {
                  compound.addPubchemCid(pubchemCid);
                }
              }
            }
            _dao.saveOrUpdateEntity(compound);
            incrementNumCompoundsUpgraded();
            i ++;
          }
          log.info("upgraded " + getNumCompoundsUpgraded() + " compounds so far..");
        }
      });
    }
  }
  
  private int getNumCompoundsUpgraded()
  {
    return _numCompoundsUpgraded;
  }
  
  private void incrementNumCompoundsUpgraded()
  {
    _numCompoundsUpgraded ++;
  }
}
