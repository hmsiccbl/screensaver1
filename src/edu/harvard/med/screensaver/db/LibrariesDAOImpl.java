// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.libraries.WellType;
import edu.harvard.med.screensaver.model.libraries.WellVolumeAdjustment;
import edu.harvard.med.screensaver.model.screens.CherryPickRequest;
import edu.harvard.med.screensaver.model.screens.LabCherryPick;
import edu.harvard.med.screensaver.ui.libraries.WellCopyVolume;

import org.apache.log4j.Logger;
import org.hibernate.TransientObjectException;

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
    for (Well well : library.getWells()) {
      if (well.getWellType().equals(WellType.EXPERIMENTAL)) {
        well.setGenbankAccessionNumber(null);
        well.setIccbNumber(null);
        well.setMolfile(null);
        well.setSmiles(null);
        well.removeCompounds();
        well.removeSilencingReagents();
        well.setWellType(WellType.EMPTY);
      }
    }
    log.info("deleted library contents for " + library.getLibraryName());
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

  public BigDecimal findRemainingVolumeInWellCopy(Well well, Copy copy)
  {
    String hql;

    hql = "select ci.microliterWellVolume from CopyInfo ci where ci.hbnCopy=? and ci.hbnPlateNumber=? and ci.dateRetired is null";
    List result = getHibernateTemplate().find(hql, new Object[] { copy, well.getPlateNumber() });
    if (result == null || result.size() == 0) {
      return BigDecimal.ZERO.setScale(Well.VOLUME_SCALE);
    }
    BigDecimal initialMicroliterVolume = (BigDecimal) result.get(0);

    hql = "select sum(wva.microliterVolume) from WellVolumeAdjustment wva where wva.copy=? and wva.well=?";
    BigDecimal deltaMicroliterVolume = (BigDecimal) getHibernateTemplate().find(hql, new Object[] { copy, well }).get(0);
    if (deltaMicroliterVolume == null) {
      deltaMicroliterVolume = BigDecimal.ZERO.setScale(Well.VOLUME_SCALE);
    }
    return initialMicroliterVolume.add(deltaMicroliterVolume).setScale(Well.VOLUME_SCALE);
  }
  
  @SuppressWarnings("unchecked")
  public Collection<WellCopyVolume> findWellCopyVolumes(Library libraryIn)
  {
    Library library = _dao.reloadEntity(libraryIn, true, "hbnWells");
    _dao.needReadOnly(library, "hbnCopies.hbnCopyInfos");
    String hql = "from WellVolumeAdjustment wva where wva.copy.hbnLibrary = ?"; 
    List<WellVolumeAdjustment> wellVolumeAdjustments = getHibernateTemplate().find(hql, new Object[] { library });
    List<WellCopyVolume> result = new ArrayList<WellCopyVolume>();
    return aggregateWellVolumeAdjustments(makeEmptyWellVolumes(library, result), wellVolumeAdjustments);
  }

  @SuppressWarnings("unchecked")
  public Collection<WellCopyVolume> findWellCopyVolumes(Copy copy)
  {
    // TODO: eager fetch copies and wells
    String hql = "from WellVolumeAdjustment wva where wva.copy = ?"; 
    List<WellVolumeAdjustment> wellVolumeAdjustments = getHibernateTemplate().find(hql, new Object[] { copy });
    List<WellCopyVolume> result = new ArrayList<WellCopyVolume>();
    return aggregateWellVolumeAdjustments(makeEmptyWellVolumes(copy, result), wellVolumeAdjustments);
  }

  @SuppressWarnings("unchecked")
  public Collection<WellCopyVolume> findWellCopyVolumes(Copy copy, Integer plateNumber)
  {
    // TODO: eager fetch copies and wells
    String hql = "select wva from WellVolumeAdjustment wva join wva.copy c join c.hbnCopyInfos ci where wva.copy = ? and ci.hbnPlateNumber = ?"; 
    List<WellVolumeAdjustment> wellVolumeAdjustments = getHibernateTemplate().find(hql, new Object[] { copy, plateNumber });
    List<WellCopyVolume> result = new ArrayList<WellCopyVolume>();
    if (wellVolumeAdjustments.size() == 0) {
      return result;
    }
    return aggregateWellVolumeAdjustments(makeEmptyWellVolumes(copy, plateNumber, result), wellVolumeAdjustments);
  }

  @SuppressWarnings("unchecked")
  public Collection<WellCopyVolume> findWellCopyVolumes(Integer plateNumber)
  {
    // TODO: eager fetch copies and wells
    String hql = "select wva from WellVolumeAdjustment wva join wva.copy c join c.hbnCopyInfos ci where ci.hbnPlateNumber = ?"; 
    List<WellVolumeAdjustment> wellVolumeAdjustments = getHibernateTemplate().find(hql, new Object[] { plateNumber });
    List<WellCopyVolume> result = new ArrayList<WellCopyVolume>();
    if (wellVolumeAdjustments.size() == 0) {
      return result;
    }
    return aggregateWellVolumeAdjustments(makeEmptyWellVolumes(wellVolumeAdjustments.get(0).getCopy(), plateNumber, result), wellVolumeAdjustments);
  }

  @SuppressWarnings("unchecked")
  public Collection<WellCopyVolume> findWellCopyVolumes(WellKey wellKey)
  {
    String hql = "select distinct wva from WellVolumeAdjustment wva left join fetch wva.copy left join fetch wva.well w left join fetch w.hbnLibrary l left join fetch l.hbnCopies where w.id = ?"; 
    List<WellVolumeAdjustment> wellVolumeAdjustments = getHibernateTemplate().find(hql, new Object[] { wellKey.toString() });
    List<WellCopyVolume> result = new ArrayList<WellCopyVolume>();
    Well well = null;
    if (wellVolumeAdjustments.size() == 0) {
      well = findWell(wellKey);
      if (well == null) {
        // no such well
        return result;
      }
      // well exists, but just doesn't have any wellVolumeAdjustments (which is
      // valid); in this case we still want to return a collection that contains
      // an element for each copy of the well's library
    } 
    else {
      // wel exists, and has wellVolumeAdjustments
      well = wellVolumeAdjustments.get(0).getWell();
    }
    return aggregateWellVolumeAdjustments(makeEmptyWellVolumes(well, result), wellVolumeAdjustments);
  }
  
  @SuppressWarnings("unchecked")
  public Collection<WellCopyVolume> findWellCopyVolumes(CherryPickRequest cherryPickRequest,
                                                        boolean forUnfufilledLabCherryPicksOnly)
  {
    cherryPickRequest = _dao.reloadEntity(cherryPickRequest,
                                          true,
                                          "labCherryPicks.sourceWell.hbnLibrary");
    _dao.needReadOnly(cherryPickRequest,
                      "labCherryPicks.wellVolumeAdjustments");
    if (forUnfufilledLabCherryPicksOnly) { 
      // if filtering unfulfilled lab cherry picks, we need to fetch more relationships, to be efficient
      _dao.needReadOnly(cherryPickRequest,
                        "labCherryPicks.assayPlate.hbnCherryPickLiquidTransfer");
    }
    StringBuilder hql = new StringBuilder();
    hql.append("select distinct wva ");
    hql.append("from WellVolumeAdjustment wva, CherryPickRequest cpr join cpr.labCherryPicks lcp join lcp.sourceWell sw ");
    hql.append("where wva.well = sw and cpr = ?");
    List<WellVolumeAdjustment> wellVolumeAdjustments = new ArrayList<WellVolumeAdjustment>();
    for (LabCherryPick labCherryPick : cherryPickRequest.getLabCherryPicks()) {
      if (!forUnfufilledLabCherryPicksOnly || labCherryPick.isUnfulfilled()) {
        wellVolumeAdjustments.addAll(labCherryPick.getWellVolumeAdjustments());
      }
    }
    
    List<WellCopyVolume> result = new ArrayList<WellCopyVolume>();
    List<WellCopyVolume> emptyWellVolumes = makeEmptyWellVolumes(cherryPickRequest, 
                                                                 result, 
                                                                 forUnfufilledLabCherryPicksOnly);
    return aggregateWellVolumeAdjustments(emptyWellVolumes, wellVolumeAdjustments);
  }


  // private methods

  private List<WellCopyVolume> makeEmptyWellVolumes(Library library, List<WellCopyVolume> wellVolumes)
  {
    for (Copy copy : library.getCopies()) {
      makeEmptyWellVolumes(copy, wellVolumes);
    }
    return wellVolumes;
  }

  private List<WellCopyVolume> makeEmptyWellVolumes(Copy copy, List<WellCopyVolume> wellVolumes)
  {
    for (int plateNumber = copy.getLibrary().getStartPlate(); plateNumber <= copy.getLibrary().getEndPlate(); ++plateNumber) {
      makeEmptyWellVolumes(copy, plateNumber, wellVolumes);
    }
    return wellVolumes;
  }

  private List<WellCopyVolume> makeEmptyWellVolumes(Copy copy, int plateNumber, List<WellCopyVolume> wellVolumes)
  {
    for (int iRow = 0; iRow < Well.PLATE_ROWS; ++iRow) {
      for (int iCol = 0; iCol < Well.PLATE_COLUMNS; ++iCol) {
        wellVolumes.add(new WellCopyVolume(findWell(new WellKey(plateNumber, iRow, iCol)), copy));
      }
    }
    return wellVolumes;
  }

  private List<WellCopyVolume> makeEmptyWellVolumes(Well well, List<WellCopyVolume> result)
  {
    for (Copy copy : well.getLibrary().getCopies()) {
      result.add(new WellCopyVolume(well, copy));
    }
    return result;
  }

  private List<WellCopyVolume> makeEmptyWellVolumes(CherryPickRequest cherryPickRequest, 
                                                    List<WellCopyVolume> result,
                                                    boolean forUnfufilledLabCherryPicksOnly)
  {
    for (LabCherryPick lcp : cherryPickRequest.getLabCherryPicks()) {
      if (!forUnfufilledLabCherryPicksOnly || lcp.isUnfulfilled()) {
        makeEmptyWellVolumes(lcp.getSourceWell(), result);
      }
    }
    return result;
  }

  private Collection<WellCopyVolume> aggregateWellVolumeAdjustments(List<WellCopyVolume> wellCopyVolumes,
                                                                    List<WellVolumeAdjustment> wellVolumeAdjustments)
  {
    Collections.sort(wellCopyVolumes, new Comparator<WellCopyVolume>() {
      public int compare(WellCopyVolume wcv1, WellCopyVolume wcv2)
      {
        int result = wcv1.getWell().compareTo(wcv2.getWell());
        if (result == 0) {
          result = wcv1.getCopy().getName().compareTo(wcv2.getCopy().getName());
        }
        return result;
      }
    });
    Collections.sort(wellVolumeAdjustments, new Comparator<WellVolumeAdjustment>() {
      public int compare(WellVolumeAdjustment wva1, WellVolumeAdjustment wva2)
      {
        int result = wva1.getWell().compareTo(wva2.getWell());
        if (result == 0) {
          result = wva1.getCopy().getName().compareTo(wva2.getCopy().getName());
        }
        return result;
      }
    });
    Iterator<WellCopyVolume> wcvIter = wellCopyVolumes.iterator();
    Iterator<WellVolumeAdjustment> wvaIter = wellVolumeAdjustments.iterator();
    if (wcvIter.hasNext()) {
      WellCopyVolume wellCopyVolume = wcvIter.next();
      while (wvaIter.hasNext()) {
        WellVolumeAdjustment wellVolumeAdjustment = wvaIter.next();
        while (!wellCopyVolume.getWell().equals(wellVolumeAdjustment.getWell()) ||
          !wellCopyVolume.getCopy().equals(wellVolumeAdjustment.getCopy())) {
          if (!wcvIter.hasNext()) {
            throw new IllegalArgumentException("wellVolumeAdjustments exist for wells that were not in wellCopyVolumes: " + 
                                               wellVolumeAdjustment.getWell() + ":" + wellVolumeAdjustment.getCopy().getName());
          }
          wellCopyVolume = wcvIter.next();
        }
        wellCopyVolume.addWellVolumeAdjustment(wellVolumeAdjustment);
      }
    }
    return wellCopyVolumes;
  }
}