// $HeadURL: http://seanderickson1@forge.abcd.harvard.edu/svn/screensaver/branches/go/trunk/src/edu/harvard/med/screensaver/io/libraries/smallmolecule/StructureImageProvider.java $
// $Id: StructureImageProvider.java 3968 2010-04-08 17:04:35Z atolopko $
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.io.screens;

import edu.harvard.med.screensaver.model.Entity;
import edu.harvard.med.screensaver.ui.arch.images.ImageProvider;

/**
 * Type-safe tag interface for services that can provide images of study images.
 */
public interface StudyImageProvider<E extends Entity> extends ImageProvider<E>
{
}
