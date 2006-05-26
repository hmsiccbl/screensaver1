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

import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.Well;

import org.springframework.transaction.annotation.Transactional;

/**
 * Interface for accessing laboratory-related entities.
 * <p>
 * <i>This interface is experimental and can (and should be) changed as soon as
 * we know what was want it to do for us. The very name is likely inappropriate.</i>
 * 
 * @motivation Needed a basic DAO to get Spring+Hibernate integration working.
 *             Haven't added complete Javadoc comments, since everything is
 *             subject to change.
 * @author ant
 */
// TODO: change as necessary, include interface name.
@Transactional
public interface LabDAO
{

  public Compound defineCompound(String name, String smiles);

  /**
   * Define a new <tt>Library</tt>. A <tt>Library</tt> must exist before
   * <tt>Well</tt>s can be defined for it.
   */
  public Library defineLibrary(String name,
                               String shortName,
                               String libraryType,
                               int startPlate,
                               int endPlate);

  /**
   * Defines a new well for a extant library.
   * @motivation enforces the constraint that setLibrary() must be called after
   *             key-forming fields are specified (plateNumber and wellName)
   */
  public Well defineLibraryWell(Library library,
                                int plateNumber,
                                String wellName);

  public void associateCompoundWithWell(Well well, Compound compound);

  /**
   * A pseudo-method, mostly for testing purposes. Might turn into a useable
   * method, if it has some real-world meaning or use.
   * 
   * @motivation So far, just for testing Spring+Hibernate transactions, using
   *             JDK5 annotations
   * @param plateNumber
   * @param wellNames
   * @param compounds
   * @param library
   * @param compound
   * @return
   */
  public List<Well> defineLibraryPlateWells(int plateNumber,
                                            List<String> wellNames,
                                            Library library,
                                            Compound compound);

  @Transactional(readOnly=true)
  public Compound findCompoundByName(String name);

  @Transactional(readOnly=true)
  public List<Compound> findAllCompounds();

  @Transactional(readOnly=true)
  public Library findLibraryByName(String libraryName);

  @Transactional(readOnly=true)
  public Set<Well> findAllLibraryWells(String libraryName);

}