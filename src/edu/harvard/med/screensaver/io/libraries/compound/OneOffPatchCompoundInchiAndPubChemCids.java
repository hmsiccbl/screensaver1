// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries.compound;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import edu.harvard.med.screensaver.CommandLineApplication;
import edu.harvard.med.screensaver.db.DAOTransaction;
import edu.harvard.med.screensaver.db.GenericEntityDAO;
import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.util.eutils.PubchemCidListProvider;

public class OneOffPatchCompoundInchiAndPubChemCids
{
  private static Logger log = Logger.getLogger(OneOffPatchCompoundInchiAndPubChemCids.class);

  private static GenericEntityDAO _dao;
  private static boolean _noneLeft = false;
  
  public static void main(String[] args) {
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] { 
      CommandLineApplication.DEFAULT_SPRING_CONFIG,
    });
    _dao = (GenericEntityDAO) context.getBean("genericEntityDao");
    while (! _noneLeft) {
      _dao.doInTransaction(new DAOTransaction() {
        public void runTransaction() {
          List<Compound> badCompounds = _dao.findEntitiesByProperty(Compound.class, "inchi", "");
          log.info("bad compound count = " + badCompounds.size());
          if (badCompounds.size() == 0) {
            _noneLeft = true;
            return;
          }
          log.info("patching records..");
          int i = 0;
          for (Compound badCompound : badCompounds) {
            patchCompoundFromSmiles(badCompound);
            _dao.persistEntity(badCompound);
            if (++ i % 100 == 0) {
              log.info("patched " + i + " records..");
            }
            if (i % 1000 == 0) {
              break;
            }
          }
          log.info("done patching!");
        }
      });
    }
  }

  private static void patchCompoundFromSmiles(Compound compound)
  {
    String smiles = compound.getSmiles();
    String inchi = new OpenBabelClient().convertSmilesToInchi(smiles);
    if (inchi.equals("")) {
      log.warn("inchi still \"\" for smiles \"" + smiles + "\"");
    }
    compound.setInchi(inchi);
    compound.getPubchemCids().clear();
    PubchemCidListProvider provider = new PubchemCidListProvider();
    for (String pubchemCid : provider.getPubchemCidListForInchi(inchi)) {
      compound.addPubchemCid(pubchemCid);
    }
  }
}

