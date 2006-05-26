// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Well;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * A Hibernate-specific implementation of the {@link #LabDAO} interface.
 * @author ant
 */
public class LabDAOImpl extends HibernateDaoSupport implements LabDAO
{
  public Library defineLibrary(String name,
                               String shortName,
                               String libraryType,
                               int startPlate,
                               int endPlate)
  {
    Library library = new Library();
    library.setLibraryName(name);
    library.setShortName(shortName);
    library.setLibraryType(libraryType);
    library.setStartPlate(startPlate);
    library.setEndPlate(endPlate);
    getHibernateTemplate().save(library);
    return library;
  }
  
  public Well defineLibraryWell(Library library,
                                int plateNumber,
                                String wellName)
  {
    Well well = new Well(wellName, library, plateNumber);
    getHibernateTemplate().save(well);
    return well;
  }
  
  public Compound defineCompound(String name, String smiles) {
    Compound compound = new Compound(name);
    compound.setSmiles(smiles);
    getHibernateTemplate().save(compound);
    return compound;
  }

  public void associateCompoundWithWell(Well well, Compound compound) {
    well.addCompound(compound);
  }

  public Compound findCompoundByName(String name) {
    List list = 
      getHibernateTemplate().find("from Compound as c where c.compoundName = ?",
                              name);
    if ( list.size() > 0 ) {
      return (Compound) list.get(0);
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public List<Compound> findAllCompounds() {
    return (List<Compound>) getHibernateTemplate().loadAll(Compound.class);
  }

  public Library findLibraryByName(String libraryName) {
    return (Library) getHibernateTemplate().find("from Library l where l.libraryName = ?",
                                                 libraryName).iterator().next();
  }

  @SuppressWarnings("unchecked")
  public Set<Well> findAllLibraryWells(String libraryName) {
    return new HashSet<Well>(getHibernateTemplate().find("from Well as w where w.library.libraryName = ?",
                                                         libraryName));
  }
  
  public List<Well> defineLibraryPlateWells(int plateNumber,
                                            List<String> wellNames,
                                            Library library,
                                            Compound compound)
  {
    List<Well> result = new ArrayList<Well>(wellNames.size());
    for (String wellName : wellNames) {
      // HACK: as the Javadoc comment explains, this is a (temporary) method for
      // testing purposes.
      // TODO: remove hack!
      if (wellName.startsWith("C")) {
        throw new IllegalArgumentException("bad well name value...rolling back txn!");
      }
      Well well = defineLibraryWell(library, plateNumber, wellName);
      if ( compound != null ) {
        associateCompoundWithWell(well, compound);
      }
      result.add(well);
    }
    return result;
  }

}
