// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.db;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import edu.harvard.med.screensaver.model.Volume;
import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Copy;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.LibraryType;
import edu.harvard.med.screensaver.model.libraries.SilencingReagent;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.model.libraries.WellKey;
import edu.harvard.med.screensaver.model.screens.ScreenType;

public interface LibrariesDAO
{

  public List<Library> findLibrariesOfType(LibraryType[] libraryTypes,
                                           ScreenType[] screenTypes);

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
   * Find and return the well. If the well is in the Hibernate session, but not
   * yet in the database, this method will <i>not</i> return the managed
   * instance of the Well.
   * 
   * @param wellKey the wellKey
   * @param loadContents if true, then load all the compounds, silencing
   *          reagents and genes associated with the well
   * @return the well, null if there is no well
   */
  public Well findWell(WellKey wellKey, boolean loadContents);

  /**
   * Find and return the silencing reagent. Return null if there is no matching
   * silencing reagent.
   * @param gene the gene the silencing reagent silences
   * @param silencingReagentType the type of silencing reagent
   * @param sequence the sequence of the silencing reagent
   * @return the silencing reagent. Return null if there is no matching
   * silencing reagent.
   */
  public SilencingReagent findSilencingReagent(Gene gene,
                                               SilencingReagentType silencingReagentType,
                                               String sequence);

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
   * Delete library contents. Null out all the content-oriented content of all the
   * wells; delete any dangling {@link Compound Compounds}, {@link SilencingReagent
   * SilencingReagents}, and {@link Gene Genes}.
   * @param library the library to delete the contents of
   */
  public void deleteLibraryContents(Library library);

  public Set<Well> findWellsForPlate(int plate);

  public boolean isPlateRangeAvailable(Integer startPlate, Integer endPlate);

  public void loadOrCreateWellsForLibrary(Library library);

  public Volume findRemainingVolumeInWellCopy(Well well, Copy copy);

  public Collection<String> findAllVendorNames();

}
