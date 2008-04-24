// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.compound.upgraders;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.util.eutils.CompoundIdType;
import edu.harvard.med.screensaver.util.eutils.EutilsException;
import edu.harvard.med.screensaver.util.eutils.PubchemSmilesOrInchiSearch;
import edu.harvard.med.screensaver.util.eutils.PubchemSmilesOrInchiStandardizer;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

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
  private static int NUM_COMPOUNDS_UPGRADED_PER_TRANSACTION = 1;
  
  
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
  private PubchemSmilesOrInchiStandardizer pubchemSmilesOrInchiStandardizer = new PubchemSmilesOrInchiStandardizer();
  
  // constructor and instance methods
  
  public CompoundPubchemCidListUpgrader(GenericEntityDAO dao)
  {
    _dao = dao;
  }
  
  public void upgradeAllNonUpgradedCompounds()
  {
    List<Compound> nonUpgradedCompoundsList = _dao.findEntitiesByHql(
      Compound.class,
      "from Compound c left join c.pubchemCids p where p is null");
    log.info("retrieved " + nonUpgradedCompoundsList.size() + " compounds needing an upgrade");
    final Iterator<Compound> nonUpgradedCompounds =
      nonUpgradedCompoundsList.iterator();
    while (nonUpgradedCompounds.hasNext()) {
      int numCompoundsProcessed = 0;
      int numCompoundsUpgraded = 0;
      int numErrors = 0;
      while (nonUpgradedCompounds.hasNext()) {
        try {
          final Compound compound = _dao.findEntityById(Compound.class, 
                                                        nonUpgradedCompounds.next().getEntityId(),
                                                        true,
                                                        "wells");
          ++numCompoundsProcessed;
          Set<String> smilesSet = new HashSet<String>();
          for (Well well : compound.getWells()) {
            smilesSet.add(well.getSmiles());
          }
          Set<String> newPubchemCids = new HashSet<String>();
          for (String smiles : smilesSet) {
            String standardizedSmiles = pubchemSmilesOrInchiStandardizer.getPubchemStandardizedSmilesOrInchi(smiles, CompoundIdType.SMILES);
            if (!standardizedSmiles.equals(smiles)) {
              log.debug("standardized smiles from " + smiles + " to " + standardizedSmiles);
            }
            newPubchemCids.addAll(pubchemSmilesOrInchiSearch.getPubchemCidsForSmilesOrInchi(standardizedSmiles));
          }
          if (newPubchemCids.size() > 0) {
            ++numCompoundsUpgraded;
            for (final String pubchemCid : newPubchemCids) {
              _dao.doInTransaction(new DAOTransaction() {
                public void runTransaction()
                {
                  _dao.reattachEntity(compound);
                  compound.addPubchemCid(pubchemCid);
                }
              });
              log.debug("added new Pubchem CID " + pubchemCid + " to compound " + compound.getCompoundId());
            }
          }
          else {
            log.debug("no new Pubchem CIDs found for compound " + compound.getCompoundId());
          }
        }
        catch (EutilsException e) {
          ++numErrors;
          log.error(e);
        }
        finally {
          // save progress in batches, and reduce memory usage
          if (numCompoundsProcessed % NUM_COMPOUNDS_UPGRADED_PER_TRANSACTION == 0) {
            _dao.flush();
            _dao.clear();
            log.info("processed " + numCompoundsProcessed + 
                     "; upgraded " + numCompoundsUpgraded + " (" + String.format("%.2f%%", numCompoundsUpgraded / (double) numCompoundsProcessed) + ")" +
                     "; errors " + numErrors + " (" + String.format("%.2f%%", numErrors / (double) numCompoundsProcessed) + ") ");
          }
        }
      }
    }
  }
}
