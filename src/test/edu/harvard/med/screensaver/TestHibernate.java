// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver;

import java.util.Iterator;

import edu.harvard.med.screensaver.beans.libraries.Compound;
import edu.harvard.med.screensaver.beans.libraries.Library;
import edu.harvard.med.screensaver.beans.libraries.Well;
import edu.harvard.med.screensaver.db.HibernateSessionFactory;

import org.hibernate.Session;

/**
 * 
 *
 * @author john sullivan
 * @hibernate.class
 */
public class TestHibernate {

  /**
   * @param args Not used.
   */
  @SuppressWarnings("unchecked")
  public static void main(String[] args) {
    Compound compound;
    Well well;
    Library library;
    Iterator<Compound> compounds;
    Iterator<Well> wells;
    
    Session session = HibernateSessionFactory.currentSession();
    
    //// create a new compound
    session.beginTransaction();
    compound = new Compound();
    compound.setCompoundName("compound p");
    compound.setSmiles("P");
    session.save(compound);
    session.getTransaction().commit();

    //// look up a compound and modify it
    session.beginTransaction();
    compound = (Compound)
      session.createQuery(
        "from Compound as c where c.compoundName = 'compound p'").uniqueResult();
    compound.setSmiles("P'");
    session.save(compound);
    session.getTransaction().commit();    

    //// create a new well, add compound p to it
    session.beginTransaction();
    library = new Library();
    library.setLibraryName("library Q");
    library.setShortName("Q");
    library.setLibraryType("DOS");
    library.setStartPlate(1);
    library.setEndPlate(2);
    well = new Well();
    well.setPlateNumber(27);
    well.setWellName("A01");
    well.setLibrary(library);
    compound.addWell(well);
    session.save(compound);
    session.getTransaction().commit();
    
    //// iterate over compounds
    session.beginTransaction();
    compounds = session.createQuery("from Compound").list().iterator();
    System.out.println("compounds:");
    while (compounds.hasNext()) {
      compound = compounds.next();
      System.out.println("compoundId:   " + compound.getCompoundId());
      System.out.println("compoundName: " + compound.getCompoundName());
      System.out.println("smiles:       " + compound.getSmiles());
      wells = compound.getWells().iterator();
      while (wells.hasNext()) {
        well = wells.next();
        System.out.println("well plate:   " + well.getPlateNumber());
        System.out.println("well well:    " + well.getWellName());
      }
      System.out.println();
    }
    session.getTransaction().commit();    
  }
}
