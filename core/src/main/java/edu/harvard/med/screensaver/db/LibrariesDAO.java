// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.joda.time.DateTime;

import edu.harvard.med.screensaver.io.libraries.LibraryCopyPlateListParserResult;
import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.CopyUsageType;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryContentsVersion;
import edu.harvard.med.screensaver.model.libraries.Plate;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
import edu.harvard.med.screensaver.model.libraries.SmallMoleculeReagent;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screens.ScreenType;

public interface LibrariesDAO
{
  /**
   * Find and return the well. If the well is in the Hibernate session, but not
   * yet in the database, this method will return the managed instance of the
   * Well.
   * 
   * @param wellKey the wellKey
   * @return the well, null if there is no well
   */
  public Well findWell(WellKey wellKey);

  /**
   * Find and return the latest released version of the reagents with the
   * specified {@link ReagentVendorIdentifier}.
   * 
   * @param rvi the {@link ReagentVendorIdentifier}
   * @param latestReleasedVersionsOnly
   * @return Set of reagents (possibly an empty set), where are reagent is the
   *         latest released version.
   */
  public Set<Reagent> findReagents(ReagentVendorIdentifier rvi, boolean latestReleasedVersionsOnly);

  /**
   * NOTE: this is a LINCS-only feature
   */
  public Set<SmallMoleculeReagent> findReagents(String facilityId,
                                                Integer saltId,
                                                Integer batchId,
                                                boolean latestReleasedVersionsOnly);

  /**
   * Find and return the library that contains the specified plate, or null if
   * no such library contains the plate.
   *
   * @param plateNumber the plate number
   * @return the library that contains the specified plate. return null if no
   *         such library contains the plate.
   */
  public Library findLibraryWithPlate(Integer plateNumber);

  /**
   * Delete library contents for the specified library version.
   */
  public void deleteLibraryContentsVersion(LibraryContentsVersion libraryContentsVersion);

  public Set<Well> findWellsForPlate(int plate);

  public boolean isPlateRangeAvailable(Integer startPlate, Integer endPlate);

  public Map<Copy,Volume> findRemainingVolumesInWellCopies(Well well, CopyUsageType copyUsageType);

  public int countExperimentalWells(int startPlate, int endPlate);

  public Set<ScreenType> findScreenTypesForWells(Set<WellKey> wellKeys);

  public Set<ScreenType> findScreenTypesForReagents(Set<String> reagentIds);

  public Plate findPlate(int plateNumber, String copyName);

  public Set<Integer> queryForPlateIds(LibraryCopyPlateListParserResult parserResult);

  /**
   * Find Wells containing Reagents where the items in the list match either the reagent's compound names 
   * (using case insensitive, greedy match).
   * @param limitSize imposes a LIMIT clause on the underlying SQL
   */
  public Set<WellKey> findWellKeysForCompoundName(final String compoundSearchName, int limitSize);
  
  /**
   * Find Wells containing Reagents where the vendor reagent id matches the items.
   * @param limitSize imposes a LIMIT clause on the underlying SQL
   */
  public Set<WellKey> findWellKeysForReagentVendorID(final String facilityVendorId, int limitSize);

  public void calculatePlateScreeningStatistics(Collection<Plate> plates);

  public void calculateCopyVolumeStatistics(Collection<Copy> copies);

  public void calculatePlateVolumeStatistics(Collection<Plate> plates);
  
  public void calculateCopyScreeningStatistics(Collection<Copy> copies);

  public Set<Well> findAllCanonicalReagentWells();

  public Set<String> findCanonicalReagentWellIds(Set<String> wellIds);

  public Well findCanonicalReagentWell(String facilityId,
                                       Integer saltId,
                                       Integer facilityBatchId);
  
	public DateTime getLatestDataLoadingDate();
  
}
