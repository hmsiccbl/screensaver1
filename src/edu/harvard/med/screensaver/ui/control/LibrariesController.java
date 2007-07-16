// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.ui.control;

import edu.harvard.med.screensaver.model.libraries.Compound;
import edu.harvard.med.screensaver.model.libraries.Gene;
import edu.harvard.med.screensaver.model.libraries.Library;
import edu.harvard.med.screensaver.model.libraries.SilencingReagentType;
import edu.harvard.med.screensaver.model.libraries.Well;
import edu.harvard.med.screensaver.ui.libraries.CompoundLibraryContentsImporter;
import edu.harvard.med.screensaver.ui.libraries.NaturalProductsLibraryContentsImporter;
import edu.harvard.med.screensaver.ui.libraries.RNAiLibraryContentsImporter;
import edu.harvard.med.screensaver.ui.libraries.WellFinder;
import edu.harvard.med.screensaver.ui.libraries.WellSearchResultsViewer;
import edu.harvard.med.screensaver.ui.libraries.WellVolumeSearchResults;
import edu.harvard.med.screensaver.ui.searchresults.SearchResults;
import edu.harvard.med.screensaver.ui.searchresults.WellSearchResults;

import org.apache.myfaces.custom.fileupload.UploadedFile;

/**
 * @motivation Allows Spring to create an AOP proxy for our
 *             LibraryControllerImpl concrete class, which can then be injected
 *             into other beans expecting a ScreensController type. If
 *             LibrariesController was the concrete class, its proxy would not
 *             be injectable into other beans.
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 */
public interface LibrariesController
{

  /**
   * Go to the {@link WellFinder} page.
   * @return the control code "findWells"
   */
  @UIControllerMethod
  public String findWells();

  /**
   * Find the well with the specified plate number and well name, and go to the appropriate next
   * page depending on the result.
   * @return the control code for the appropriate next page
   */
  @UIControllerMethod
  public String findWell(String plateNumber, String wellName);

  /**
   * Find the wells specified in the plate-well list, and go to the {@link WellSearchResultsViewer}
   * page.
   * @return the controler code for the next appropriate page
   */
  @UIControllerMethod
  public String findWells(String plateWellList);

  @UIControllerMethod
  public String browseLibraries();

  @UIControllerMethod
  public String viewLibrary();

  @UIControllerMethod
  public String viewLibrary(final Library libraryIn,
                            SearchResults<Library> librarySearchResults);

  @UIControllerMethod
  public String viewLibraryContents(final Library libraryIn);

  @UIControllerMethod
  public String viewWellSearchResults(WellSearchResults wellSearchResults);

  @UIControllerMethod
  public String viewWell();

  @UIControllerMethod
  /**
   * @param wellSearchResults <code>null</code> if well was not found within
   *          the context of a search result
   */
  public String viewWell(final Well wellIn, WellSearchResults wellSearchResults);

  @UIControllerMethod
  public String viewGene(final Gene geneIn, WellSearchResults wellSearchResults);

  @UIControllerMethod
  public String viewCompound(final Compound compoundIn,
                             final WellSearchResults wellSearchResults);

  /**
   * Go to the {@link CompoundLibraryContentsImporter} page.
   * @param library the library to import
   * @return the control code "importCompoundLibraryContents"
   */
  @UIControllerMethod
  public String importCompoundLibraryContents(Library library);

  /**
   * Load the compound library contents from the file, and go to the appropriate next page
   * depending on the result.
   * @return the control code for the appropriate next page
   */
  @UIControllerMethod
  public String importCompoundLibraryContents(final Library libraryIn,
                                              final UploadedFile uploadedFile);

  /**
   * Go to the {@link NaturalProductsLibraryContentsImporter} page.
   * @param library the library to import
   * @return the control code "importNaturalProductsLibraryContents"
   */
  @UIControllerMethod
  public String importNaturalProductsLibraryContents(Library library);

  /**
   * Load the natural products library contents from the file, and go to the appropriate next page
   * depending on the result.
   * @return the control code for the appropriate next page
   */
  @UIControllerMethod
  public String importNaturalProductsLibraryContents(final Library libraryIn,
                                                     final UploadedFile uploadedFile);
  
  /**
   * Go to the {@link RNAiLibraryContentsImporter} page.
   * @param library the library to import
   * @return the control code "importRNAiLibraryContents"
   */
  @UIControllerMethod
  public String importRNAiLibraryContents(Library library);

  /**
   * Load the RNAi library contents from the file, and go to the appropriate next page
   * depending on the result.
   * @return the control code for the appropriate next page
   */
  @UIControllerMethod
  public String importRNAiLibraryContents(final Library libraryIn,
                                          final UploadedFile uploadedFile,
                                          final SilencingReagentType silencingReagentType);

  @UIControllerMethod
  public String unloadLibraryContents(final Library libraryIn, final SearchResults<Library> results);

  public String viewLibraryWellVolumes(Library library);

  public String viewWellVolumeSearchResults(WellVolumeSearchResults wellVolumeSearchResults);

}
