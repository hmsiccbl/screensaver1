// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryContentsVersion;
import edu.harvard.med.screensaver.model.libraries.Reagent;
import edu.harvard.med.screensaver.model.libraries.ReagentVendorIdentifier;
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

  public void loadOrCreateWellsForLibrary(Library library);

  public Map<Copy,Volume> findRemainingVolumesInWellCopies(Well well);

  public Collection<String> findAllVendorNames();

  public int countExperimentalWells(int startPlate, int endPlate);

  public Set<ScreenType> findScreenTypesForWells(Set<WellKey> wellKeys);

  public Set<ScreenType> findScreenTypesForReagents(Set<String> reagentIds);

}
