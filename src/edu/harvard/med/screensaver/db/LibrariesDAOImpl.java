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
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.db.hqlbuilder.JoinType;
import edu.harvard.med.screensaver.io.libraries.LibraryCopyPlateListParserResult;
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
import edu.harvard.med.screensaver.model.libraries.PlateStatus;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.ui.arch.datatable.Criterion.Operator;
import edu.harvard.med.screensaver.util.Pair;

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

  public Set<Reagent> findReagents(ReagentVendorIdentifier rvi, 
                                   boolean latestReleasedOnly)
  {
    final HqlBuilder hql = new HqlBuilder();
    hql.from(Reagent.class, "r").
      from("r", Reagent.well, "w", JoinType.LEFT_FETCH).
      from("w", Well.library, "l", JoinType.LEFT_FETCH).
    where("r", Reagent.vendorIdentifier.getPath(), Operator.EQUAL, rvi.getVendorIdentifier()).
    where("r", Reagent.vendorName.getPath(), Operator.EQUAL, rvi.getVendorName());
    if (latestReleasedOnly) {
      hql.from("w", Well.latestReleasedReagent, "lrr").
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
      "select library from Library library where ? between library.startPlate and library.endPlate";
    List<Library> libraries = (List<Library>) getHibernateSession().createQuery(hql).setInteger(0, plateNumber).list();
    if (libraries.isEmpty()) {
      return null;
    }
    return libraries.get(0);
  }

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
    return getHibernateSession().createQuery(hql).setInteger("startPlate", startPlate).setInteger("endPlate", endPlate).list().isEmpty();
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
      _dao.reloadEntity(libraryContentsVersionIn, false, LibraryContentsVersion.library); 
    Library library = libraryContentsVersion.getLibrary();
    if (library.getReagentType().equals(SilencingReagent.class)) {
      _dao.need(library, 
                Library.wells.to(Well.reagents).to(SilencingReagent.facilityGene).to(Gene.entrezgeneSymbols));
      _dao.need(library,
                Library.wells.to(Well.reagents).to(SilencingReagent.facilityGene).to(Gene.genbankAccessionNumbers));
      _dao.need(library, 
                Library.wells.to(Well.reagents).to(SilencingReagent.vendorGene).to(Gene.entrezgeneSymbols));
      _dao.need(library,
                Library.wells.to(Well.reagents).to(SilencingReagent.vendorGene).to(Gene.genbankAccessionNumbers));
    }
    else if (library.getReagentType().equals(SmallMoleculeReagent.class)) { 
      _dao.need(library, Library.wells.to(Well.reagents).to(SmallMoleculeReagent.compoundNames));
      _dao.need(library, Library.wells.to(Well.reagents).to(SmallMoleculeReagent.molfileList));
    }
    else {
      _dao.need(library, Library.wells.to(Well.reagents));
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

  public Set<Well> findWellsForPlate(int plate)
  {
    return new TreeSet<Well>(getHibernateSession().createQuery("from Well where plateNumber = ?").setInteger(0, plate).list());
  }

  @Override
  public Map<Copy,Volume> findRemainingVolumesInWellCopies(Well well, CopyUsageType copyUsageType)
  {
    String hql = "select c, p.wellVolume, " +
    		"(select sum(wva.volume) from WellVolumeAdjustment wva where wva.copy = c and wva.well.id=?) " +
        "from Plate p join p.copy c " +
        "where p.plateNumber=? and (p.status = ? or p.status = ?) and c.usageType = ?";
    
    
    List<Object[]> copyVolumes = 
      getHibernateSession().createQuery(hql).
        setString(0, well.getWellKey().toString()).
        setInteger(1, well.getPlateNumber()).
        setParameter(2, PlateStatus.NOT_SPECIFIED). // TODO: temporarily allowing NOT_SPECIFIED status plates to be considered; see [#2750]
		    setParameter(3, PlateStatus.AVAILABLE).
        setParameter(4, CopyUsageType.CHERRY_PICK_SOURCE_PLATES).
        list();
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
    return ((Long) getHibernateSession().createQuery(hql).
      setInteger(0, startPlate).
      setInteger(1, endPlate).
      list().get(0)).intValue();
  }
  
  @Override
  public Set<ScreenType> findScreenTypesForReagents(Set<String> reagentIds)
  {
    final HqlBuilder hql = new HqlBuilder().
      select("l", "screenType").distinctProjectionValues().
      from(Reagent.class, "r").
      from("r", Reagent.well, "w").
      from("w", Well.library, "l").
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
      from("w", Well.library, "l").
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
    List<Plate> plates = (List<Plate>) getHibernateSession().createQuery(hql).
      setInteger(0, plateNumber).
      setString(1, copyName).
      list();
    if (plates.size() == 0) {
      return null;
    }
    if (plates.size() > 1) {
      throw new DuplicateEntityException(plates.get(0));
    }
    return plates.get(0);
  }

  @Override
  public Set<Integer> queryForPlateIds(LibraryCopyPlateListParserResult parserResult)
  {
    Set<Integer> plateIds = Sets.newHashSet();

    Set<String> copyNames = parserResult.getCopies();
    Set<Pair<Integer,Integer>> plateRanges = parserResult.getCompletePlateRanges();

    if (copyNames.isEmpty() && plateRanges.isEmpty()) {
      // nop
      //return plateIds;
    }
    else if (!copyNames.isEmpty() && plateRanges.isEmpty()) {
      Set<Plate> plates = findPlatesByCopyName(copyNames);
      for (Plate p : plates) {
        plateIds.add(p.getPlateId());
      }
    }

    // Finally, do the plate range separately
    if (!plateRanges.isEmpty()) {
      for (Pair<Integer,Integer> range : plateRanges) {
        if (!copyNames.isEmpty()) {
          for (String copyName : copyNames) {
            Set<Plate> plates = findPlateRangeFromCopyCaseInsensitive(range.getFirst(), range.getSecond(), copyName);
            for (Plate p : plates) {
                plateIds.add(p.getPlateId());
              }
            }
        }
        else {
          for (Plate p : findPlateRangeFromCopyCaseInsensitive(range.getFirst(), range.getSecond(), null)) {
            plateIds.add(p.getPlateId());
          }
        }
      }
    }

    return plateIds;
  }

  /**
   * Return all plates where the first <= plateNumber <= second<br/>
   * Note: if first=second, then treat as &quot;plateNumber=first&quot;
   * 
   * @param firstPlateNumber
   * @param secondPlateNumber
   * @param copyName if null return all plates in the range across all copies
   * @return
   */
  private Set<Plate> findPlateRangeFromCopyCaseInsensitive(int firstPlateNumber, int secondPlateNumber, final String copyName)
  {
    if (firstPlateNumber > secondPlateNumber) {
      int temp = firstPlateNumber;
      firstPlateNumber = secondPlateNumber;
      secondPlateNumber = temp;
    }
    final int first = firstPlateNumber;
    final int second = secondPlateNumber;

    List<Plate> plates = runQuery(new Query<Plate>() {
      public List<Plate> execute(Session session)
        {
          HqlBuilder builder = new HqlBuilder();
          builder.from(Plate.class, "p");
          if (copyName != null) builder.from("p", Plate.copy, "c", JoinType.INNER);
          builder.select("p").distinctProjectionValues();
          if (first != second) {
            builder.where("p", "plateNumber", Operator.GREATER_THAN_EQUAL, first);
            builder.where("p", "plateNumber", Operator.LESS_THAN_EQUAL, second);
          }
          else {
            // special case where range is actually as single value
            builder.where("p", "plateNumber", Operator.EQUAL, first);
          }
          if (copyName != null) builder.where("c", "name", Operator.TEXT_LIKE, copyName);
          return builder.toQuery(session, true).list();
        }
    });
    return Sets.newHashSet(plates);
  }
  
  private Set<Plate> findPlatesByCopyName(final Set<String> copies)
  {
    List<Plate> plates = runQuery(new Query<Plate>() {
      public List<Plate> execute(Session session)
        {
          HqlBuilder builder = new HqlBuilder();
          builder.from(Plate.class, "p");
          builder.from("p", Plate.copy, "c", JoinType.LEFT_FETCH);
          builder.select("p").distinctProjectionValues();
          for (String copyName : copies) {
            builder.where("c", "name", Operator.TEXT_LIKE, copyName);
          }
          return builder.toQuery(session, true).list();
        }
    });
    return Sets.newHashSet(plates);
  }

}