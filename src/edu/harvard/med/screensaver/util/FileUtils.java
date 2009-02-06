// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.util;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

/**
 * File utilities.
 * 
 * @author <a mailto="andrew_tolopko@hms.harvard.edu">Andrew Tolopko</a>
 * @author <a mailto="john_sullivan@hms.harvard.edu">John Sullivan</a>
 */
public class FileUtils
{
  /**
   * Returns a File object with a new parent directory and/or a new file
   * extension.
   * 
   * @param file the File object to be modified
   * @param newDirectory the output directory; if null the file original
   *          file directory is used.
   * @param newExtension the extension to use when saving the file,
   *          replacing the file's original filename extension; if null
   *          original filename extension is used. A leading period will be
   *          added if it does not exist.
   * @return the modified File object
   */
  public static File modifyFileDirectoryAndExtension(
    File file,
    File newDirectory,
    String newExtension)
  {
    if (newExtension == null) {
      newExtension = FilenameUtils.getExtension(file.getName());
    } 
    else {
      if (newExtension.startsWith(".")) {
        newExtension = newExtension.substring(1);
      }
    }

    if (newDirectory == null) {
      newDirectory = file.getParentFile();
    }
    File outputFile = new File(newDirectory,
                               FilenameUtils.getBaseName(file.getName()) + "." + newExtension);
    return outputFile;
  }
  
  /**
   * Returns a File object with a new parent directory and/or a new file
   * extension.
   * 
   * @param file the File object to be modified
   * @param newDirectory the output directory; if null the file's original
   *          file directory is used.
   * @param newExtension the extension to use when saving the file,
   *          replacing the file's original filename extension; if null
   *          original filename extension is used. A leading period will be
   *          added if it does not exist.
   * @return the modified File object
   */
  public static File modifyFileDirectoryAndExtension(
    File file,
    String newDirectory,
    String newExtension)
  {
    return modifyFileDirectoryAndExtension(file, 
                                           newDirectory == null ? null
                                             : new File(newDirectory), 
                                           newExtension);
  }
  
}
