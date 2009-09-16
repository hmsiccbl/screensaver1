// $HeadURL: svn+ssh://js163@orchestra.med.harvard.edu/svn/iccb/screensaver/trunk/.eclipse.prefs/codetemplates.xml $
// $Id: codetemplates.xml 169 2006-06-14 21:57:49Z js163 $
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

import edu.harvard.med.screensaver.model.screens.AttachedFileType;

public interface AttachedFilesEntity
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
