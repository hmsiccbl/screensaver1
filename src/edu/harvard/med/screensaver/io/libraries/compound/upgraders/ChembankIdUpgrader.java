// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/src/edu/harvard/med/screensaver/io/libraries/compound/upgraders/CompoundPubchemCidListUpgrader.java $
// $Id: CompoundPubchemCidListUpgrader.java 2089 2008-01-08 16:44:51Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.compound.upgraders;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.mit.broad.chembank.shared.mda.webservices.service.ArrayOfMolecule;
import edu.mit.broad.chembank.shared.mda.webservices.service.FindBySimilarity1Fault1;
import edu.mit.broad.chembank.shared.mda.webservices.service.Molecule;
import edu.mit.broad.chembank.shared.mda.webservices.service.MoleculeWebService;
import edu.mit.broad.chembank.shared.mda.webservices.service.MoleculeWebService_Service;

/**
 * Standalone application for upgrading the ChemBank IDs for the complete compound collection.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public class ChembankIdUpgrader
{
  
  // static fields
  
  private static Logger log = Logger.getLogger(ChembankIdUpgrader.class);
  private static int NUM_COMPOUNDS_UPGRADED_PER_TRANSACTION = 100;
  
  
  // static methods
  
  public static void main(String [] args)
  {
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] { 
      CommandLineApplication.DEFAULT_SPRING_CONFIG,
    });
    GenericEntityDAO dao = (GenericEntityDAO) context.getBean("genericEntityDao");
    ChembankIdUpgrader upgrader = new ChembankIdUpgrader(dao);
    upgrader.upgradeAllCompoundsWithNoChembankIds();
  }
  
  
  // instance fields
  
  private GenericEntityDAO _dao;
  private MoleculeWebService _moWebService;
  private int _numCompoundsUpgraded = 0;


  // constructor and instance methods
  
  public ChembankIdUpgrader(GenericEntityDAO dao)
  {
    _dao = dao;
    _moWebService = new MoleculeWebService_Service().getMoleculeWebService();
  }
  
  public void upgradeAllCompoundsWithNoChembankIds()
  {
    List<Compound> nonUpgradedCompoundsList = _dao.findEntitiesByHql(
      Compound.class,
      "from Compound where size(chembankIds) = 0");
    log.info("retrieved " + nonUpgradedCompoundsList.size() + " compounds needing an upgrade");
    final Iterator<Compound> nonUpgradedCompounds = nonUpgradedCompoundsList.iterator();
    while (nonUpgradedCompounds.hasNext()) {
      _dao.doInTransaction(new DAOTransaction()
      {
        public void runTransaction()
        {
          int i = 0;
          while (nonUpgradedCompounds.hasNext() && i < NUM_COMPOUNDS_UPGRADED_PER_TRANSACTION) {
            Compound compound = _dao.reloadEntity(nonUpgradedCompounds.next());
            List<String> chembankIds = getChembankIdsForSmiles(compound.getSmiles());
            for (String chembankId : chembankIds) {
              compound.addChembankId(chembankId);
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
  
  private List<String> getChembankIdsForSmiles(String smiles)
  {
    log.debug("query chembank against SMILES " + smiles);
    List<String> chembankIds = new ArrayList<String>();
    try {
      ArrayOfMolecule arrayOfMolecule = _moWebService.findBySimilarity(smiles, 1.0);
      for (Molecule molecule : arrayOfMolecule.getMolecule()) {
        // you would think they would give you an empty list, instead of a list containing null..
        if (molecule != null) {
          log.debug("  got hit " + molecule.getChembankId() + ": " + molecule.getSmiles());
          chembankIds.add(molecule.getChembankId());
        }
      }
    }
    catch (FindBySimilarity1Fault1 e) {
      log.error("MoleculeWebService threw exception for smiles '" + smiles + "': " + e.getMessage());
      e.printStackTrace();
    }
    catch (javax.xml.ws.soap.SOAPFaultException e) {
      log.error("MoleculeWebService threw exception for smiles '" + smiles + "': " + e.getMessage());
      e.printStackTrace();      
    }
    return chembankIds;
  }
}
