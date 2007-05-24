// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellType;

import org.apache.log4j.Logger;
import org.hibernate.TransientObjectException;

public class LibrariesDAOImpl extends AbstractDAO implements LibrariesDAO
{
  // static members

  private static Logger log = Logger.getLogger(LibrariesDAO.class);


  // instance data members
  
  private GenericEntityDAO _dao;
  

  // public constructors and methods
  
  /**
   * @motivation for CGLIB dynamic proxy creation
   */
  public LibrariesDAOImpl()
  {
  }
  
  public LibrariesDAOImpl(GenericEntityDAO dao)
  {
    _dao = dao;
  }
  
  public Well findWell(WellKey wellKey)
  {
    return _dao.findEntityById(Well.class, wellKey.getKey());
  }

  public SilencingReagent findSilencingReagent(
    Gene gene,
    SilencingReagentType silencingReagentType,
    String sequence)
  {
    return _dao.findEntityById(SilencingReagent.class,
                               gene.toString() + ":" + 
                               silencingReagentType.toString() + ":" + 
                               sequence);
  }
  
  @SuppressWarnings("unchecked")
  public Library findLibraryWithPlate(Integer plateNumber)
  {
    String hql =
      "select library from Library library where " +
      plateNumber + " between library.startPlate and library.endPlate";
    List<Library> libraries = (List<Library>) getHibernateTemplate().find(hql);
    if (libraries.size() == 0) {
      return null;
    }
    return libraries.get(0); 
  }
  
  public void deleteLibraryContents(Library library)
  {
    log.error("call TODO daoImpl.deleteLibraryContents");
    for (Well well : library.getWells()) {
      well.setGenbankAccessionNumber(null);
      well.setIccbNumber(null);
      well.setMolfile(null);
      well.setSmiles(null);
      well.removeCompounds();
      well.removeSilencingReagents();
      well.setWellType(WellType.EMPTY);
    }
  }

  @SuppressWarnings("unchecked")
  public Set<Well> findWellsForPlate(int plate)
  {
    return new TreeSet<Well>(getHibernateTemplate().find("from Well where plateNumber = ?", plate));
  }

  @SuppressWarnings("unchecked")
  public void loadOrCreateWellsForLibrary(Library library)
  {
    // this might not perform awesome, but:
    //   - is correct, in terms of the "load" part of method contract, since it is
    //     always possible that some but not all of the library's wells have already
    //     been loaded into the session.
    //   - presumably this method is not called in time-critical sections of code
    // further performance improvements possible by checking if a single well (or
    // something like that) was in the session, but this fails to be correct, in
    // terms of the "load" part of the method contract, although it will not cause
    // any errors, just perf problems later when code is forced to get wells one at
    // a time.
    Collection<Well> wells;
    try {
      wells = library.getWells();
    }
    catch (TransientObjectException e) {
      wells = getHibernateTemplate().find(
        "from Well where plateNumber >= ? and plateNumber <= ?",
        new Object [] { library.getStartPlate(), library.getEndPlate() });
    }
    if (wells.size() > 0) {
      return;
    }
    for (int iPlate = library.getStartPlate(); iPlate <= library.getEndPlate(); ++iPlate) {
      for (int iRow = 0; iRow < Well.PLATE_ROWS; ++iRow) {
        for (int iCol = 0; iCol < Well.PLATE_COLUMNS; ++iCol) {
          _dao.persistEntity(new Well(library, new WellKey(iPlate, iRow, iCol), WellType.EMPTY));
        }
      }
    }
    log.info("created wells for library " + library.getLibraryName());
  }
  
  @SuppressWarnings("unchecked")
  public List<Library> findLibrariesDisplayedInLibrariesBrowser()
  {
    // TODO: make this HQL type-safe by using LibraryType enum to obtain the values
    return new ArrayList<Library>(getHibernateTemplate().find(
      "from Library where libraryType not in ('Annotation', 'DOS', 'NCI', 'Discrete')")); 
  }
  
  
  // private methods

}

