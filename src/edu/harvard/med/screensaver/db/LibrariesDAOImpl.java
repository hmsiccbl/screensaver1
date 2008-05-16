// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellType;
import edu.harvard.med.screensaver.model.screens.ScreenType;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.springframework.orm.hibernate3.HibernateCallback;

public class LibrariesDAOImpl extends AbstractDAO implements LibrariesDAO
{
  // static members

  private static Logger log = Logger.getLogger(LibrariesDAOImpl.class);


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

  public Well findWell(WellKey wellKey, boolean loadContents)
  {
    return _dao.findEntityById(
      Well.class,
      wellKey.getKey(),
      false,
      "compounds",
      "silencingReagents.gene");
  }

  public List<String> findAllVendorNames()
  {
    String hql = "select distinct l.vendor from Library l where l.vendor is not null";
    @SuppressWarnings("unchecked")
    List<String> vendorNames = getHibernateTemplate().find(hql);
    return vendorNames;
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

  @SuppressWarnings("unchecked")
  public boolean isPlateRangeAvailable(Integer startPlate, Integer endPlate)
  {
    if (startPlate <= 0 || endPlate <= 0) {
      return false;
    }
    // swap, if necessary
    if (startPlate > endPlate) {
      Integer tmp = endPlate;
      endPlate = startPlate;
      startPlate = tmp;
    }
    String hql =
      "from Library library where not" +
      "(library.startPlate > :endPlate or library.endPlate < :startPlate)";
    List<Library> libraries = (List<Library>)
    getHibernateTemplate().findByNamedParam(hql,
                                            new String[] {"startPlate", "endPlate"},
                                            new Integer[] {startPlate, endPlate});
    return libraries.size() == 0;
  }

  public void deleteLibraryContents(Library library)
  {
    library = _dao.reloadEntity(library, 
                                false,
                                "wells.compounds", 
                                "wells.silencingReagents");
    for (Well well : library.getWells()) {
      if (well.getWellType().equals(WellType.EXPERIMENTAL)) {
        well.setGenbankAccessionNumber(null);
        well.setIccbNumber(null);
        well.setMolfile(null);
        well.setSmiles(null);
        well.removeCompounds();
        well.removeSilencingReagents();
        well.setWellType(WellType.EMPTY);
        well.setReagent(null); // do this after well type, exp well must have reagent!
      }
    }
    log.info("deleted library contents for " + library.getLibraryName());
  }

  @SuppressWarnings("unchecked")
  public Set<Well> findWellsForPlate(int plate)
  {
    return new TreeSet<Well>(getHibernateTemplate().find("from Well where plateNumber = ?", plate));
  }

  /**
   * Efficiently loads all wells for the specified library into the current
   * Hibernate session, avoiding database accesses when any of the library's
   * wells are subsequently accessed. If the library has not had its wells
   * created yet, they will be created. Created wells will be in the Hibernate
   * session after invoking this method, but will not yet be flushed to the
   * database (HQL queries will fail to find them). If the library is transient
   * (i.e., never persisted, and thus not in the Hibernate session), it will be
   * persisted (entity ID will be assigned, it will be managed by the Hibernate
   * session, but not flushed to the database). If the library is detached, it
   * will be reattached to the session.
   * 
   * @throws IllegalArgumentException if the provided library instance is not
   *           the same as the one in the current Hibernate session.
   */
  @SuppressWarnings("unchecked")
  public void loadOrCreateWellsForLibrary(Library library)
  {
    // Cases that must be handled:
    // 1. Library instance is transient (not in Hibernate session), and not in database
    // 2. Library instance is detached (not in Hibernate session), but is in database
    // 3. Library instance is managed (in Hibernate session), but not in database (awaiting flush)
    // 4. Library instance is managed (in Hibernate session), and in database

    if (library.getLibraryId() == null) { // case 1
      log.debug("library is transient");
    }
    else { // case 2, 3, or 4
      // reload library, fetching all wells; 
      // if library instance is detached (case 2), we load it from the database
      // if library instance is already in session (case 3 or 4), we obtain that instance
      Library reloadedLibrary = _dao.reloadEntity(library, false, "wells");
      if (reloadedLibrary == null) { // case 2
        log.debug("library is Hibernate-managed, but not yet persisted in database");
        _dao.saveOrUpdateEntity(library);
      }
      else { // case 4
        log.debug("library is Hibernate-managed and persisted in database");
        // if library instance is not same instance as the one in the session, this method cannot be called
        if (reloadedLibrary != library) {
          throw new IllegalArgumentException("provided Library instance is not the same as the one in the current Hibernate session; cannot load/create wells for that provided library");
        }           
      }
    }
    
    // create wells for library, if needed
    if (library.getWells().size() == 0) {
      for (int iPlate = library.getStartPlate(); iPlate <= library.getEndPlate(); ++iPlate) {
        for (int iRow = 0; iRow < Well.PLATE_ROWS; ++iRow) {
          for (int iCol = 0; iCol < Well.PLATE_COLUMNS; ++iCol) {
            library.createWell(new WellKey(iPlate, iRow, iCol), WellType.EMPTY);
          }
        }
      }
      // persistEntity() call will place all wells in session *now*
      // (as opposed t saveOrUpdate(), which does upon session flush), so that
      // subsequent code can find them in the Hibernate session
      _dao.persistEntity(library);
      log.info("created wells for library " + library.getLibraryName());
    }
  }

  @SuppressWarnings("unchecked")
  public List<Library> findLibrariesOfType(final LibraryType[] libraryTypes,
                                           final ScreenType[] screenTypes)
  {
    return (List<Library>) getHibernateTemplate().executeFind(new HibernateCallback() {
      public Object doInHibernate(org.hibernate.Session session)
      throws org.hibernate.HibernateException, java.sql.SQLException
      {
        Query query = session.createQuery("from Library where libraryType in (:libraryTypes) and screenType in (:screenTypes)");
        query.setParameterList("libraryTypes", libraryTypes);
        query.setParameterList("screenTypes", screenTypes);
        return query.list();
      }
    });
  }

  public Volume findRemainingVolumeInWellCopy(Well well, Copy copy)
  {
    String hql;

    hql = "select ci.wellVolume from CopyInfo ci where ci.copy=? and ci.plateNumber=? and ci.dateRetired is null";
    List result = getHibernateTemplate().find(hql, new Object[] { copy, well.getPlateNumber() });
    if (result == null || result.size() == 0) {
      return Volume.ZERO;
    }
    Volume initialVolume = (Volume) result.get(0);

    hql = "select sum(wva.volume) from WellVolumeAdjustment wva where wva.copy=? and wva.well=?";
    Volume deltaVolume = (Volume) getHibernateTemplate().find(hql, new Object[] { copy, well }).get(0);
    if (deltaVolume == null) {
      deltaVolume = Volume.ZERO;
    }
    return initialVolume.add(deltaVolume);
  }

  // private methods

}