// $HeadURL$
// $Id$
//
// Copyright 2006 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;


public interface AttachedFilesEntity extends Entity
{

  Set<AttachedFile> getAttachedFiles();

  AttachedFile createAttachedFile(String filename,
                                  AttachedFileType fileType,
                                  String fileContents) throws IOException;

  AttachedFile createAttachedFile(String filename,
                                  AttachedFileType fileType,
                                  InputStream fileContents) throws IOException;

  void removeAttachedFile(AttachedFile attachedFile);

}
