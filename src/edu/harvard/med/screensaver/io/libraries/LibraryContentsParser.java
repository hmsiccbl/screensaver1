// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
//
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.libraries;

import java.io.File;
import java.io.InputStream;

import edu.harvard.med.screensaver.model.libraries.Library;

/**
 * Parses library contents (either partial or complete) from an input
 * stream into the domain model.
 *
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public interface LibraryContentsParser
{

  /**
   * Parse library contents (either partial or complete) from an input
   * stream into a library into the entity model.
   *
   * @param library the library to load contents of
   * @param file the name of the file that contains the library contents
   * @param stream the input stream to load library contents from
   * @param startPlate the first plate in the plate range to be loaded; null
   *          okay; if endPlate is null, all plate after and including
   *          startPlate will be loaded
   * @param endPlate the last plate, inclusive, in the plate range to be loaded;
   *          null okay; if startPlate is null, all plates before and including
   *          endPlate will be loaded
   * @return the library with the contents loaded
   * @throws ParseLibraryContentsException if parse errors encountered. The
   *           exception will contain a reference to a ParseErrors object which
   *           can be inspected and/or reported to the user.
   */
  public Library parseLibraryContents(Library library, File file, InputStream stream, Integer startPlate, Integer endPlate) throws ParseLibraryContentsException;
}
