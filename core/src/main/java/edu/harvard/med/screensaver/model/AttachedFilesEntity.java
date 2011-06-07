// $HeadURL$
// $Id$
//
// Copyright © 2006, 2010 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Set;

import org.joda.time.LocalDate;


public interface AttachedFilesEntity<A extends AttachedFileType,K extends Serializable> extends Entity<K>
{

  Set<AttachedFile> getAttachedFiles();

  AttachedFile createAttachedFile(String filename,
                                  A fileType,
                                  LocalDate fileDate,
                                  String fileContents) throws IOException;

  AttachedFile createAttachedFile(String filename,
                                  A fileType,
                                  LocalDate fileDate,
                                  InputStream fileContents) throws IOException;

  void removeAttachedFile(AttachedFile attachedFile);

}
