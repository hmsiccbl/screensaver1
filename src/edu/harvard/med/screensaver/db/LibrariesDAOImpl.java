// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.base.Functions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.db.hqlbuilder.JoinType;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.VolumeUnit;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryContentsVersion;
import edu.harvard.med.screensaver.model.libraries.LibraryWellType;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.ui.table.Criterion.Operator;

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

  public List<String> findAllVendorNames()
  {
    String hql = "select distinct l.vendor from Library l where l.vendor is not null";
    @SuppressWarnings("unchecked")
    List<String> vendorNames = getHibernateTemplate().find(hql);
    return vendorNames;
  }

  public Set<Reagent> findReagents(ReagentVendorIdentifier rvi, 
                                   boolean latestReleasedOnly)
  {
    final HqlBuilder hql = new HqlBuilder();
    hql.from(Reagent.class, "r").
    from("r", Reagent.well.getLeaf(), "w", JoinType.LEFT_FETCH).
    from("w", Well.library.getLeaf(), "l", JoinType.LEFT_FETCH).
    where("r", Reagent.vendorIdentifier.getPath(), Operator.EQUAL, rvi.getVendorIdentifier()).
    where("r", Reagent.vendorName.getPath(), Operator.EQUAL, rvi.getVendorName());
    if (latestReleasedOnly) {
      hql.from("w", "latestReleasedReagent", "lrr").
      where("lrr", Operator.EQUAL, "r");
    }
    hql.select("r");
    log.debug(hql.toString());
    List<Reagent> reagents = _dao.runQuery(new Query<Reagent>() {
      public List<Reagent> execute(Session session)
      {
        return hql.toQuery(session, true).list();
      }
    });
    return Sets.newHashSet(reagents);
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

  /**
   * Delete the reagents of the specified contents version, as well the contents
   * version record itself. The contents version must not have been previously
   * "released" (see
   * {@link LibraryContentsVersion#release(edu.harvard.med.screensaver.model.AdministrativeActivity)}
   * ). Converts each well in the library back to an undefined well if there are
   * no contents versions remaining after the deletion.
   */
  public void deleteLibraryContentsVersion(LibraryContentsVersion libraryContentsVersionIn)
  {
    if (libraryContentsVersionIn.isReleased()) {
      throw new BusinessRuleViolationException("cannot delete a library contents version that has been released");
    }
    LibraryContentsVersion libraryContentsVersion = 
      _dao.reloadEntity(libraryContentsVersionIn, false, LibraryContentsVersion.library.getPath()); 
    Library library = libraryContentsVersion.getLibrary();
    if (library.getReagentType().equals(SilencingReagent.class)) {
      _dao.need(library, 
                Library.wells.to(Well.reagents).to(SilencingReagent.facilityGene).to(Gene.entrezgeneSymbols).getPath(),
                Library.wells.to(Well.reagents).to(SilencingReagent.facilityGene).to(Gene.genbankAccessionNumbers).getPath());
      _dao.need(library, 
                Library.wells.to(Well.reagents).to(SilencingReagent.vendorGene).to(Gene.entrezgeneSymbols).getPath(),
                Library.wells.to(Well.reagents).to(SilencingReagent.vendorGene).to(Gene.genbankAccessionNumbers).getPath());
    }
    else if (library.getReagentType().equals(SmallMoleculeReagent.class)) { 
      _dao.need(library, Library.wells.to(Well.reagents).to(SmallMoleculeReagent.compoundNames).getPath());
      _dao.need(library, Library.wells.to(Well.reagents).to(SmallMoleculeReagent.molfileList).getPath());
    }
    else {
      _dao.need(library, Library.wells.to(Well.reagents).getPath());
    }
    
    library.getContentsVersions().remove(libraryContentsVersion); // will be deleted by Hibernate, thanks to delete-orphan cascade
    for (Well well : library.getWells()) {
      Reagent reagent = well.getReagents().remove(libraryContentsVersion);  // will be deleted by Hibernate, thanks to delete-orphan cascade
      if (reagent != null) {
        _dao.deleteEntity(reagent);
      }
      if (library.getContentsVersions().isEmpty()) {
        well.setFacilityId(null);
        well.setLibraryWellType(LibraryWellType.UNDEFINED);
      }
    }
    log.info("deleted library contents version " + libraryContentsVersion.getVersionNumber() + " for library " + library.getLibraryName());
  }

  @SuppressWarnings("unchecked")
  public Set<Well> findWellsForPlate(int plate)
  {
    return new TreeSet<Well>(getHibernateTemplate().find("from Well where plateNumber = ?", plate));
  }

  /**
   * Efficiently loads all wells for the specified library into the current
   * Hibernate session, thus avoiding subsequent database accesses when any of the library's
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
          throw new IllegalArgumentException("provided Library instance is not the same as the one in the current Hibernate session; cannot load/create wells for that library");
        }
      }
    }

    // create wells for library, if needed;
    // it is expected that either all wells will have been created, or none were created, but if 
    int nominalWellCount = ((library.getEndPlate() - library.getStartPlate()) + 1) * library.getPlateSize().getWellCount();
    int nWellsCreated = 0;
    if (library.getWells().size() <= nominalWellCount) {
      for (int iPlate = library.getStartPlate(); iPlate <= library.getEndPlate(); ++iPlate) {
        for (int iRow = 0; iRow < library.getPlateSize().getRows(); ++iRow) {
          for (int iCol = 0; iCol < library.getPlateSize().getColumns(); ++iCol) {
            WellKey wellKey = new WellKey(iPlate, iRow, iCol);
            try {
              library.createWell(wellKey, LibraryWellType.UNDEFINED);
              ++nWellsCreated;
            }
            catch (DuplicateEntityException e) {
              log.debug("ignore failed creation attempt for any well that has been created in the past: " + e.getMessage());
              // ignore failed creation attempt for any well that has been created in the past
            }
          }
        }
      }
      // persistEntity() call will place all wells in session *now*
      // (as opposed t saveOrUpdate(), which does upon session flush), so that
      // subsequent code can find them in the Hibernate session
      _dao.persistEntity(library);
      log.info("created " + nWellsCreated + " wells for library " + library.getLibraryName());
      if (nWellsCreated < nominalWellCount) {
        log.warn("needed to create only " + nWellsCreated + " of " + nominalWellCount + " wells for library " + library.getLibraryName());
      }
    }
  }

  @SuppressWarnings("unchecked")
  public Map<Copy,Volume> findRemainingVolumesInWellCopies(Well well)
  {
    String hql = "select c, ci.wellVolume, " +
    		"(select sum(wva.volume) from WellVolumeAdjustment wva where wva.copy = c and wva.well.id=?) " +
    		"from Plate ci join ci.copy c " +
        "where ci.plateNumber=? and ci.dateRetired is null and c.usageType = ?";
    
    
    List<Object[]> copyVolumes = 
      getHibernateTemplate().find(hql, 
                                  new Object[] { well.getWellKey().toString(), well.getPlateNumber(),
                                    CopyUsageType.CHERRY_PICK_STOCK_PLATES });
    Map<Copy,Volume> remainingVolumes = Maps.newHashMap();
    for (Object[] row : copyVolumes) {
      Volume remainingVolume = 
        ((Volume) row[1]).add(row[2] == null 
                                            ? VolumeUnit.ZERO 
                                            : (Volume) row[2]);
      remainingVolumes.put((Copy) row[0], remainingVolume);
    }
    return remainingVolumes;
  }

  @Override
  public int countExperimentalWells(int startPlate, int endPlate)
  {
    String hql = "select count(*) from Well w where w.plateNumber between ? and ? and w.libraryWellType = 'experimental'";
    return ((Long) getHibernateTemplate().find(hql, Lists.newArrayList(startPlate, endPlate).toArray()).get(0)).intValue(); 
  }
  
  @Override
  public Set<ScreenType> findScreenTypesForReagents(Set<String> reagentIds)
  {
    final HqlBuilder hql = new HqlBuilder().
      select("l", "screenType").distinctProjectionValues().
      from(Reagent.class, "r").
      from("r", Reagent.well.getLeaf(), "w").
      from("w", Well.library.getLeaf(), "l").
      whereIn("r", Reagent.vendorIdentifier.getPath(), reagentIds);
    List<ScreenType> screenTypes = runQuery(new Query<ScreenType>() {
      public List<ScreenType> execute(Session session)
      {
        return hql.toQuery(session, true).list();
      }
    });
    return Sets.newTreeSet(screenTypes);
  }

  @Override
  public Set<ScreenType> findScreenTypesForWells(Set<WellKey> wellKeys)
  {
    Set<String> wellIds = Sets.newHashSet(Iterables.transform(wellKeys, Functions.toStringFunction()));
    final HqlBuilder hql = new HqlBuilder().
    select("l", "screenType").distinctProjectionValues().
    from(Well.class, "w").
    from("w", Well.library.getLeaf(), "l").
    whereIn("w", "id", wellIds);
    List<ScreenType> screenTypes = runQuery(new Query<ScreenType>() {
      public List<ScreenType> execute(Session session)
      {
        return hql.toQuery(session, true).list();
      }
    });
    return Sets.newTreeSet(screenTypes);
  }
  
  @Override
  public Plate findPlate(int plateNumber, String copyName)
  {
    String hql = "select p from Plate p left join fetch p.copy c left join fetch c.library where p.plateNumber = ? and c.name = ?";
    List<Plate> plates = (List<Plate>) getHibernateTemplate().find(hql, new Object[] { Integer.valueOf(plateNumber), copyName });
    if (plates.size() == 0) {
      return null;
    }
    if (plates.size() > 1) {
      throw new DuplicateEntityException(plates.get(0));
    }
    return plates.get(0);
  }
}