// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.TypedQuery;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.harvard.med.screensaver.ScreensaverConstants;
import edu.harvard.med.screensaver.db.Criterion.Operator;
import edu.harvard.med.screensaver.db.hqlbuilder.HqlBuilder;
import edu.harvard.med.screensaver.db.hqlbuilder.JoinType;
import edu.harvard.med.screensaver.io.libraries.LibraryCopyPlateListParserResult;
import edu.harvard.med.screensaver.model.BusinessRuleViolationException;
import edu.harvard.med.screensaver.model.DuplicateEntityException;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.VolumeUnit;
import edu.harvard.med.screensaver.model.activities.AdministrativeActivityType;
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
import edu.harvard.med.screensaver.model.libraries.ScreeningStatistics;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.libraries.VolumeStatistics;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screenresults.AssayPlate;
import edu.harvard.med.screensaver.model.screens.LibraryScreening;
import edu.harvard.med.screensaver.model.screens.ScreenType;
import edu.harvard.med.screensaver.util.Pair;
import edu.harvard.med.screensaver.util.StringUtils;

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
    if (log.isDebugEnabled()) {
      log.debug(hql.toString());
    }
    List<Reagent> reagents = _dao.runQuery(new Query<Reagent>() {
      public List<Reagent> execute(Session session)
      {
        return hql.toQuery(session, true).list();
      }
    });
    return Sets.newHashSet(reagents);
  }

  /**
   * @param facilityId required
   * @param saltId optional, if null, return all
   * @param batchId optional if null, return all
   */
  public Set<SmallMoleculeReagent> findReagents(String facilityId, Integer saltId, Integer batchId, boolean latestReleasedVersionsOnly)
  {
    final HqlBuilder hql = new HqlBuilder();
    hql.from(SmallMoleculeReagent.class, "r").
      from("r", SmallMoleculeReagent.well, "w", JoinType.LEFT_FETCH).
      from("w", Well.library, "l", JoinType.LEFT_FETCH).
      where("w", "facilityId", Operator.EQUAL, facilityId);
    if(saltId != null) hql.where("r", "saltFormId", Operator.EQUAL, saltId);
    if(batchId != null) hql.where("r", SmallMoleculeReagent.facilityBatchId.getPropertyName(), Operator.EQUAL, batchId);
    if (latestReleasedVersionsOnly) {
      hql.from("w", Well.latestReleasedReagent, "lrr").
      where("lrr", Operator.EQUAL, "r");
    }
    hql.select("r");
    if (log.isDebugEnabled()) {
      log.debug(hql.toString());
    }
    List<SmallMoleculeReagent> reagents = _dao.runQuery(new Query<SmallMoleculeReagent>() {
      public List<SmallMoleculeReagent> execute(Session session)
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
   * {@link LibraryContentsVersion#release(edu.harvard.med.screensaver.model.activities.AdministrativeActivity)} ).
   * Converts each well in the library back to an undefined well if there are
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
        "where p.plateNumber=? and p.status = ? and c.usageType = ?";

    List<Object[]> copyVolumes = 
      getHibernateSession().createQuery(hql).
        setString(0, well.getWellKey().toString()).
        setInteger(1, well.getPlateNumber()).
        setParameter(2, PlateStatus.AVAILABLE).
        setParameter(3, CopyUsageType.CHERRY_PICK_SOURCE_PLATES).
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

  @Override
  public void calculateCopyScreeningStatistics(Collection<Copy> copies)
  {
    // get copy-based statistics: screening_count, assay_plate_count, first/last date screened, data_loading_count
    final HqlBuilder builder = new HqlBuilder();

    Map<Integer,Copy> result = Maps.newHashMap();
    for (Copy c : copies) {
      c.setScreeningStatistics(new ScreeningStatistics());
      result.put(c.getEntityId(), c);
    }

    builder.from(Plate.class, "p")
           .from(AssayPlate.class, "ap")
           .from("ap", AssayPlate.libraryScreening, "ls")
           .from("ap", AssayPlate.screenResultDataLoading, "dl")
           .whereIn("p", Plate.copy.getLeaf() + ".id", result.keySet())
           .where("ap", AssayPlate.plateScreened.getLeaf(), Operator.EQUAL, "p", "id")
           .groupBy("p", "copy")
           .select("p", "copy.id")
           .selectExpression("count(distinct ls)")
           .selectExpression("count(distinct ap)")
           .selectExpression("count(distinct dl)")
           .selectExpression("min(dl.dateOfActivity)")
           .selectExpression("max(dl.dateOfActivity)")
           .selectExpression("min(ls.dateOfActivity)")
           .selectExpression("max(ls.dateOfActivity)");

    List<Object> results = _dao.runQuery(new Query() {
      @Override
      public List<Object> execute(Session session)
        {
          return builder.toQuery(session, true).list();
        }
    });
    for (Object o : results) {
      int i = 0;
      Integer copyId = (Integer) ((Object[]) o)[i++];
      ScreeningStatistics css = result.get(copyId).getScreeningStatistics();
      css.setScreeningCount(((Long) ((Object[]) o)[i++]).intValue());
      css.setAssayPlateCount(((Long) ((Object[]) o)[i++]).intValue());
      css.setDataLoadingCount(((Long) ((Object[]) o)[i++]).intValue());
      css.setFirstDateDataLoaded(((LocalDate) ((Object[]) o)[i++]));
      css.setLastDateDataLoaded(((LocalDate) ((Object[]) o)[i++]));
      css.setFirstDateScreened(((LocalDate) ((Object[]) o)[i++]));
      css.setLastDateScreened(((LocalDate) ((Object[]) o)[i++]));
    }

    // calculate plate count - the number of plates per copy 
    final HqlBuilder builder1 = new HqlBuilder();
    builder1.from(Plate.class, "p")
           .whereIn("p", Plate.copy.getLeaf() + ".id", result.keySet())
           .groupBy("p", Plate.copy.getLeaf() + ".id")
           .select("p", Plate.copy.getLeaf() + ".id")
           .selectExpression("count(*)");

    results = _dao.runQuery(new Query() {
      @Override
      public List<Object> execute(Session session)
        {
          return builder1.toQuery(session, true).list();
        }
    });

    for (Object o : results) {
      Integer copyId = (Integer) ((Object[]) o)[0];
      Integer count = ((Long) ((Object[]) o)[1]).intValue();
      ScreeningStatistics css = result.get(copyId).getScreeningStatistics();
      css.setPlateCount(count);
    }

    // calculate plate_screening_count - the total number of times individual plates from this copy have been screened, ignoring replicates)
    final HqlBuilder builder2 = new HqlBuilder();
    builder2.from(LibraryScreening.class, "ls")
           .from("ls", LibraryScreening.assayPlatesScreened, "ap")
           .from("ap", AssayPlate.plateScreened, "p")
           .from("p", Plate.copy, "c")
           .whereIn("p", Plate.copy.getLeaf() + ".id", result.keySet())
           .where("ap", "replicateOrdinal", Operator.EQUAL, 0)
           .groupBy("c", "id")
           .select("c", "id")
           .selectExpression("count(*)");

    results = _dao.runQuery(new Query() {
      @Override
      public List<Object> execute(Session session)
        {
          return builder2.toQuery(session, true).list();
        }
    });

    for (Object o : results) {
      Integer copyId = (Integer) ((Object[]) o)[0];
      Integer count = ((Long) ((Object[]) o)[1]).intValue();
      ScreeningStatistics css = result.get(copyId).getScreeningStatistics();
      css.setPlateScreeningCount(count);
    }
  }

  @Override
  public void calculatePlateScreeningStatistics(Collection<Plate> plates)
  {
    // get plate-based statistics: screening_count, assay_plate_count, first/last date screened, data_loading_count
    final HqlBuilder builder = new HqlBuilder();

    Map<Integer,Plate> result = Maps.newHashMap();
    for (Plate p : plates) {
      p.setScreeningStatistics(new ScreeningStatistics());
      result.put(p.getEntityId(), p);
    }

    builder.from(Plate.class, "p")
           .from(AssayPlate.class, "ap")
           .from("ap", AssayPlate.libraryScreening, "ls")
           .from("ap", AssayPlate.screenResultDataLoading, "dl")
           .whereIn("p", "id", result.keySet())
           .where("ap", AssayPlate.plateScreened.getLeaf(), Operator.EQUAL, "p", "id")
           .groupBy("p", "id")
           .select("p", "id")
           .selectExpression("count(distinct ls)")
           .selectExpression("count(distinct ap)")
           .selectExpression("count(distinct dl)")
           .selectExpression("min(dl.dateOfActivity)")
           .selectExpression("max(dl.dateOfActivity)")
           .selectExpression("min(ls.dateOfActivity)")
           .selectExpression("max(ls.dateOfActivity)");

    List<Object> results = _dao.runQuery(new Query() {
      @Override
      public List<Object> execute(Session session)
        {
          return builder.toQuery(session, true).list();
        }
    });
    for (Object o : results) {
      int i = 0;
      Integer plateId = (Integer) ((Object[]) o)[i++];
      ScreeningStatistics css = new ScreeningStatistics();
      result.get(plateId).setScreeningStatistics(css);
      css.setPlateCount(1);
      css.setScreeningCount(((Long) ((Object[]) o)[i++]).intValue());
      css.setAssayPlateCount(((Long) ((Object[]) o)[i++]).intValue());
      css.setDataLoadingCount(((Long) ((Object[]) o)[i++]).intValue());
      css.setFirstDateDataLoaded(((LocalDate) ((Object[]) o)[i++]));
      css.setLastDateDataLoaded(((LocalDate) ((Object[]) o)[i++]));
      css.setFirstDateScreened(((LocalDate) ((Object[]) o)[i++]));
      css.setLastDateScreened(((LocalDate) ((Object[]) o)[i++]));
    }

    // calculate plate_screening_count - the total number of times individual plates from this copy have been screened, ignoring replicates)
    final HqlBuilder builder1 = new HqlBuilder();
    builder1.from(LibraryScreening.class, "ls")
           .from("ls", LibraryScreening.assayPlatesScreened, "ap")
           .from("ap", AssayPlate.plateScreened, "p")
           .from("p", Plate.copy, "c")
           .whereIn("p", "id", result.keySet())
           .where("ap", "replicateOrdinal", Operator.EQUAL, 0)
           .groupBy("p", "id");
    builder1.select("p", "id");
    builder1.selectExpression("count(*)");

    results = _dao.runQuery(new Query() {
      @Override
      public List<Object> execute(Session session)
        {
          return builder1.toQuery(session, true).list();
        }
    });

    for (Object o : results) {
      Integer plateId = (Integer) ((Object[]) o)[0];
      Integer count = ((Long) ((Object[]) o)[1]).intValue();

      ScreeningStatistics css = result.get(plateId).getScreeningStatistics();
      css.setPlateScreeningCount(css.getPlateScreeningCount() + count);
    }
  }

  @Override
  public void calculateCopyVolumeStatistics(Collection<Copy> copies)
  {
    if (copies.isEmpty()) {
      return;
    }
    // note: we are forced to use native SQL query, as HQL does not perform volume multiplication properly (always results in value of 0)
    String sql =
      "select prv.copy_id, avg(prv.plate_remaining_volume), min(prv.plate_remaining_volume), max(prv.plate_remaining_volume) from "
        + "(select p.copy_id, p.well_volume - sum(la.volume_transferred_per_well_from_library_plates) as plate_remaining_volume "
        + "from plate p join assay_plate ap using(plate_id) join screening ls on(ls.activity_id = ap.library_screening_id) join lab_activity la using(activity_id) "
        + "where p.copy_id in (:copyIds) and ap.replicate_ordinal = 0 "
        + "group by p.copy_id, p.plate_id, p.well_volume) as prv "
        + "group by prv.copy_id";
    javax.persistence.Query query = getEntityManager().createNativeQuery(sql);
    Map<Serializable,Copy> copiesById = Maps.uniqueIndex(copies, Copy.ToEntityId);
    query.setParameter("copyIds", copiesById.keySet());
    for (Object[] row : (List<Object[]>) query.getResultList()) {
      Copy copy = copiesById.get(row[0]);
      VolumeStatistics volumeStatistics = new VolumeStatistics();
      copy.setVolumeStatistics(volumeStatistics);
      volumeStatistics.setAverageRemaining(toPlateVolume((BigDecimal) row[1]));
      volumeStatistics.setMinRemaining(toPlateVolume((BigDecimal) row[2]));
      volumeStatistics.setMaxRemaining(toPlateVolume((BigDecimal) row[3]));
    }
  }

  private static Volume toPlateVolume(BigDecimal volumeLiters)
  {
    if (volumeLiters == null) {
      return null;
    }
    return new Volume(volumeLiters.setScale(ScreensaverConstants.VOLUME_SCALE, RoundingMode.HALF_UP), VolumeUnit.LITERS).convert(VolumeUnit.MICROLITERS);
  }

  @Override
  public void calculatePlateVolumeStatistics(Collection<Plate> plates)
  {
    // note: we are forced to use native SQL query, as HQL does not perform volume multiplication properly (always results in value of 0)
    String sql =
      "select p.plate_id, p.well_volume - sum(la.volume_transferred_per_well_from_library_plates)"
        + "from plate p join assay_plate ap using(plate_id) join screening ls on(ls.activity_id = ap.library_screening_id) join lab_activity la using(activity_id) "
        + "where p.plate_id in (:plateIds) and ap.replicate_ordinal = 0 "
        + "group by p.plate_id, p.well_volume";
    javax.persistence.Query query = getEntityManager().createNativeQuery(sql);
    Map<Serializable,Plate> platesById = Maps.uniqueIndex(plates, Plate.ToEntityId);
    query.setParameter("plateIds", platesById.keySet());
    // set statistics for all requested plates, to properly handle plates that have not been screened at all (and which will not have a result in the query, due to lack of left joins)
    for (Plate plate : plates) {
      VolumeStatistics volumeStatistics = new VolumeStatistics();
      plate.setVolumeStatistics(volumeStatistics);
      volumeStatistics.setAverageRemaining(plate.getWellVolume());
    }
    for (Object[] row : (List<Object[]>) query.getResultList()) {
      Plate plate = platesById.get(row[0]);
      BigDecimal avgRemainingVolumeLiters = (BigDecimal) row[1];
      plate.getVolumeStatistics().setAverageRemaining(toPlateVolume(avgRemainingVolumeLiters));
    }
  }
  
  @Override
  public SortedSet<WellKey> findWellKeysForCompoundName(final String compoundSearchName, final int limitSize)
  {
    log.info("findWellKeysForCompoundName: " + compoundSearchName + ", limitSize: " + limitSize);
    List<SmallMoleculeReagent> reagents = null;

    if (StringUtils.isEmpty(compoundSearchName)) {  // NOTE: for large databases, this may exceed the heap size!
      reagents = _dao.runQuery(new Query<SmallMoleculeReagent>() {
        public List<SmallMoleculeReagent> execute(Session session)
        {
          HqlBuilder hql = new HqlBuilder();
          hql.from(SmallMoleculeReagent.class, "r").
            from("r", SmallMoleculeReagent.well, "w", JoinType.LEFT_FETCH);
          hql.select("r");
          org.hibernate.Query q = hql.toQuery(session, true);
          q.setMaxResults(limitSize);
          return q.list();
        }
      });
    }
    else {
      // TODO: find an HQL-way to do this.  Currently, can find no way to search, insensitively, and greedily (i.e. using substrings) for 
      // names in the SmallMoleculeReagent.compoundNames collection.  If we make compoundName into a first class 
      // Hibernate entity, I believe then we could use the HQL Operator.TEXT_LIKE to do the match - sde4 
      final Object[] result = new Object[1];
      runQuery(new edu.harvard.med.screensaver.db.Query() {
        public List<?> execute(Session session)
        {
          String sql = "select reagent_id from small_molecule_compound_name where lower(compound_name) like :name LIMIT " + limitSize;
          org.hibernate.Query query = session.createSQLQuery(sql);
          query.setParameter("name", "%" + compoundSearchName.toLowerCase() + "%");
          result[0] = query.list();
          return null;
        }
      });
      Set<Integer> reagent_ids = Sets.newHashSet((List<Integer>) result[0]);
      final HqlBuilder hql = new HqlBuilder();
      hql.from(SmallMoleculeReagent.class, "r").
            from("r", SmallMoleculeReagent.well, "w", JoinType.LEFT_FETCH).
            whereIn("r", "id", reagent_ids);
      hql.select("r");
      reagents = _dao.runQuery(new Query<SmallMoleculeReagent>() {
        public List<SmallMoleculeReagent> execute(Session session)
            {
              return hql.toQuery(session, true).list();
            }
      });
    }

    SortedSet<WellKey> wellKeys = Sets.newTreeSet(Lists.transform(reagents, new Function<SmallMoleculeReagent,WellKey>() {
      @Override
      public WellKey apply(SmallMoleculeReagent from)
      {
        return from.getWell().getWellKey();
      }
    }));
    log.info("keys found: " + wellKeys.size());
    return wellKeys;
  } 
   

//  public Set<WellKey> findWellKeysForReagentVendorID(final String facilityVendorId)
//  {
//    log.debug("findWellKeysForReagentVendorID: " + facilityVendorId);
//    if (facilityVendorId == null) return null; 
//    //    //TODO: will need a dual table query if -salt-batch id are specified (i.e. "HMSL10097-101-4")
//    //    Matcher matcher = FACILITY_SALT_BATCH_PATTERN.matcher(facilityVendorId);
//    //    String tempS = null;
//    //    Integer tempI = null;
//    //    if (matcher.matches()) {
//    //      tempS = matcher.group(2);
//    //      String temp = matcher.group(3);
//    //      tempI = Integer.parseInt(temp);
//    //    }
//    //    final String saltId = tempS;
//    //    final Integer batchId = tempI;
//    List<Reagent> reagents = runQuery(new Query<Reagent>() {
//      public List<Reagent> execute(Session session)
//        {
//          HqlBuilder builder = new HqlBuilder();
//          builder.from(Reagent.class, "r");
//          builder.from("r", Reagent.well, "w", JoinType.INNER);
//          builder.select("r").distinctProjectionValues();
//          Disjunction orClause = builder.disjunction();
//          //          if(batchId != null) {
//          //            Conjunction andClause = builder.conjunction();
//          //            andClause.add(builder.simplePredicate("r.facilityBatchId", Operator.EQUAL, batchId));
//          //            andClause.add(builder.simplePredicate("w.facilityId", Operator.TEXT_CONTAINS, facilityVendorId));
//          //            orClause.add(andClause);
//          //          }
//          //          if(saltId != null) {
//          //            
//          //          }
//          orClause.add(builder.simplePredicate("w.facilityId", Operator.TEXT_CONTAINS, facilityVendorId));
//          orClause.add(builder.simplePredicate("r." + Reagent.vendorIdentifier.getPropertyName(), Operator.TEXT_CONTAINS, facilityVendorId));
//          builder.where(orClause);
//          log.info("Hql: " + builder.toHql());
//          return builder.toQuery(session, true).list();
//        }
//    });
//
//    Set<WellKey> wellKeys = Sets.newHashSet(Lists.transform(reagents, new Function<Reagent,WellKey>() {
//      @Override
//      public WellKey apply(Reagent from)
//      {
//        return from.getWell().getWellKey();
//      }
//    }));
//    return wellKeys;
//    
//  }  
  
  @Override
  public Set<WellKey> findWellKeysForReagentVendorID(final String facilityVendorId, int limitSize)
  {
    log.info("findWellKeysForReagentVendorID: " + facilityVendorId + ", limitSize: " + limitSize);
    if (facilityVendorId == null) return null; 

    String sql =
        "select w.plate_number, w.well_name " +
        "from well w " +
        "join reagent r on(w.latest_released_reagent_id=r.reagent_id) " +
        "join small_molecule_reagent smr using(reagent_id) " +
        "where strpos(w.facility_id || '-' || coalesce(''||smr.salt_form_id,'') || '-' || coalesce(''||r.facility_batch_id,''), :searchString ) > 0" +
        " or r.vendor_identifier like :searchString2 LIMIT " + limitSize;
    if (log.isDebugEnabled()) {
      log.debug("sql: " + sql);
    }
    javax.persistence.Query query = getEntityManager().createNativeQuery(sql);
    
    query.setParameter("searchString", facilityVendorId);
    query.setParameter("searchString2", "%" + facilityVendorId + "%");
    
    List<Object[]> wellIds = query.getResultList();
    
    Set<WellKey> keys = Sets.newHashSet();
    
    for(Object[] id: wellIds) {
      keys.add( new WellKey((Integer)id[0], (String)id[1]));
    }
    log.info("keys found: " + keys.size());
    return keys;
  }
  
  @Override
  public Set<Well> findAllCanonicalReagentWells()
  {
    return Sets.newHashSet(getEntityManager().createNamedQuery("findAllCanonicalReagentWells", Well.class).getResultList());
  }

  @Override
  public Set<String> findCanonicalReagentWellIds(Set<String> wellIds)
  {
    return Sets.newHashSet(getEntityManager().createNamedQuery("findCanonicalReagentWellIds", String.class).setParameter("wellIds", wellIds).getResultList());
  }

  @Override
  public Well findCanonicalReagentWell(String facilityId,
                                       Integer saltId,
                                       Integer facilityBatchId)
  {
    String q = "select w from Well w join w.library l join w.latestReleasedReagent r where w.facilityId = :facilityId and r.facilityBatchId = :facilityBatchId and l.shortName like 'R-%'";
    if (saltId != null) {
      q += " and r.saltFormId = :saltId";
    }
    TypedQuery<Well> query = getEntityManager().createQuery(q, Well.class);
    query.setParameter("facilityId", facilityId);
    query.setParameter("facilityBatchId", 1);
    if (saltId != null) {
      query.setParameter("saltId", saltId);
    }
    if (facilityBatchId != null) {
      query.setParameter("facilityBatchId", facilityBatchId);
    }

    //return query.getSingleResult();
    query.setMaxResults(2);
    List<Well> result = query.getResultList();
    if (result.size() == 0) {
      log.warn("no canonical reagent well found for facility ID " + facilityId);
      return null;
    }
    if (result.size() > 1) {
      log.warn("more than one canonical reagent well found for facility ID " + facilityId);
    }
    return result.get(0);
  }
  
  @Override
	public DateTime getLatestDataLoadingDate() {
		String hql = "select max(a.dateCreated)  from AdministrativeActivity a  where a.type = ?";
		List<DateTime> time = (List<DateTime>) getHibernateSession().createQuery(hql)
				.setParameter(0, AdministrativeActivityType.LIBRARY_CONTENTS_LOADING).list();
		if (time.isEmpty()) {
			return null;
		}
		return time.get(0);
	}
}