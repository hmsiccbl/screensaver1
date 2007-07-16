// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
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
import edu.harvard.med.screensaver.ui.libraries.WellVolume;

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

  public BigDecimal findRemainingVolumeInWell(Copy copy, Well well)
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
  public Collection<WellVolume> findWellVolumes(Library libraryIn)
  {
    Library library = _dao.reloadEntity(libraryIn, true, "hbnWells");
    _dao.needReadOnly(library, "hbnCopies.hbnCopyInfos");
    String hql = "from WellVolumeAdjustment wva where wva.copy.hbnLibrary = ?"; 
    List<WellVolumeAdjustment> wellVolumeAdjustments = getHibernateTemplate().find(hql, new Object[] { library });
    List<WellVolume> result = new ArrayList<WellVolume>();
    return aggregateWellVolumeAdjustments(makeEmptyWellVolumes(library, result), wellVolumeAdjustments);
  }

  @SuppressWarnings("unchecked")
  public Collection<WellVolume> findWellVolumes(Copy copy)
  {
    // TODO: eager fetch copies and wells
    String hql = "from WellVolumeAdjustment wva where wva.copy = ?"; 
    List<WellVolumeAdjustment> wellVolumeAdjustments = getHibernateTemplate().find(hql, new Object[] { copy });
    List<WellVolume> result = new ArrayList<WellVolume>();
    return aggregateWellVolumeAdjustments(makeEmptyWellVolumes(copy, result), wellVolumeAdjustments);
  }

  @SuppressWarnings("unchecked")
  public Collection<WellVolume> findWellVolumes(Copy copy, Integer plateNumber)
  {
    // TODO: eager fetch copies and wells
    String hql = "select wva from WellVolumeAdjustment wva join wva.copy c join c.hbnCopyInfos ci where wva.copy = ? and ci.hbnPlateNumber = ?"; 
    List<WellVolumeAdjustment> wellVolumeAdjustments = getHibernateTemplate().find(hql, new Object[] { copy, plateNumber });
    List<WellVolume> result = new ArrayList<WellVolume>();
    if (wellVolumeAdjustments.size() == 0) {
      return result;
    }
    return aggregateWellVolumeAdjustments(makeEmptyWellVolumes(copy, plateNumber, result), wellVolumeAdjustments);
  }

  @SuppressWarnings("unchecked")
  public Collection<WellVolume> findWellVolumes(Integer plateNumber)
  {
    // TODO: eager fetch copies and wells
    String hql = "select wva from WellVolumeAdjustment wva join wva.copy c join c.hbnCopyInfos ci where ci.hbnPlateNumber = ?"; 
    List<WellVolumeAdjustment> wellVolumeAdjustments = getHibernateTemplate().find(hql, new Object[] { plateNumber });
    List<WellVolume> result = new ArrayList<WellVolume>();
    if (wellVolumeAdjustments.size() == 0) {
      return result;
    }
    return aggregateWellVolumeAdjustments(makeEmptyWellVolumes(wellVolumeAdjustments.get(0).getCopy(), plateNumber, result), wellVolumeAdjustments);
  }

  
  // private methods

  private List<WellVolume> makeEmptyWellVolumes(Library library, List<WellVolume> wellVolumes)
  {
    for (Copy copy : library.getCopies()) {
      makeEmptyWellVolumes(copy, wellVolumes);
    }
    return wellVolumes;
  }

  private List<WellVolume> makeEmptyWellVolumes(Copy copy, List<WellVolume> wellVolumes)
  {
    for (int plateNumber = copy.getLibrary().getStartPlate(); plateNumber <= copy.getLibrary().getEndPlate(); ++plateNumber) {
      makeEmptyWellVolumes(copy, plateNumber, wellVolumes);
    }
    return wellVolumes;
  }

  private List<WellVolume> makeEmptyWellVolumes(Copy copy, int plateNumber, List<WellVolume> wellVolumes)
  {
    for (int iRow = 0; iRow < Well.PLATE_ROWS; ++iRow) {
      for (int iCol = 0; iCol < Well.PLATE_COLUMNS; ++iCol) {
        wellVolumes.add(new WellVolume(findWell(new WellKey(plateNumber, iRow, iCol)), copy));
      }
    }
    return wellVolumes;
  }

  private Collection<WellVolume> aggregateWellVolumeAdjustments(List<WellVolume> allWellVolumes,
                                                                List<WellVolumeAdjustment> wellVolumeAdjustments)
  {
    Collections.sort(allWellVolumes, new Comparator<WellVolume>() {
      public int compare(WellVolume wv1, WellVolume wv2)
      {
        int result = wv1.getWell().compareTo(wv2.getWell());
        if (result == 0) {
          result = wv1.getCopy().getName().compareTo(wv2.getCopy().getName());
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
    Iterator<WellVolume> wvIter = allWellVolumes.iterator();
    Iterator<WellVolumeAdjustment> wvaIter = wellVolumeAdjustments.iterator();
    WellVolume wellVolume = wvIter.next();
    while (wvaIter.hasNext()) {
      WellVolumeAdjustment wellVolumeAdjustment = wvaIter.next();
      while (!wellVolume.getWell().equals(wellVolumeAdjustment.getWell()) ||
        !wellVolume.getCopy().equals(wellVolumeAdjustment.getCopy())) {
        if (!wvIter.hasNext()) {
          throw new IllegalArgumentException("wellVolumeAdjustments exist for wells that are were not in allWellVolumes: " + wellVolumeAdjustment.getWell() + ":" + wellVolumeAdjustment.getCopy().getName());
        }
        wellVolume = wvIter.next();
      }
      wellVolume.addWellVolumeAdjustment(wellVolumeAdjustment);
    }
    return allWellVolumes;
  }
}