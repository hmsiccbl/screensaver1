// $HeadURL: http://forge.abcd.harvard.edu/svn/screensaver/branches/go/trunk/src/edu/harvard/med/screensaver/model/users/UserAttachedFileType.java $
// $Id: UserAttachedFileType.java 3968 2010-04-08 17:04:35Z atolopko $
//
// Copyright © 2006, 2010, 2011, 2012 by the President and Fellows of Harvard College.
// 
// Screensaver is an open-source project developed by the ICCB-L and NSRB labs
// at Harvard Medical School. This software is distributed under the terms of
// the GNU General Public License.

package edu.harvard.med.screensaver.model.libraries;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.hibernate.annotations.Immutable;

import edu.harvard.med.screensaver.model.AttachedFileType;

@Entity
@Immutable
@DiscriminatorValue("reagent")
@org.hibernate.annotations.Proxy
public class ReagentAttachedFileType extends AttachedFileType
{
  private static final long serialVersionUID = 1L;
  
  private ReagentAttachedFileType()
  {
  }
  
  public ReagentAttachedFileType(String value)
  {
    super(value);
  }
}
